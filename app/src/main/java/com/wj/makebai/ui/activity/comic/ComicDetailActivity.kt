package com.wj.makebai.ui.activity.comic

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.abase.util.GsonUtil
import com.abase.view.weight.RecyclerSpace
import com.wj.commonlib.data.mode.ComicDetailData
import com.wj.commonlib.data.mode.ComicItem
import com.wj.commonlib.http.HttpManager
import com.wj.commonlib.ui.weight.CustomGridManager
import com.wj.ktutils.WjSP
import com.wj.ktutils.isNull
import com.wj.ktutils.startActivity
import com.wj.makebai.R
import com.wj.makebai.data.db.AppDatabase
import com.wj.makebai.data.db.TypesEnum
import com.wj.makebai.statice.XmlCodes
import com.wj.makebai.ui.activity.base.MakeActivity
import com.wj.makebai.ui.adapter.ComicSeriesAdapter
import com.wj.makebai.ui.weight.NoData
import com.wj.ui.interfaces.RecyerViewItemListener
import kotlinx.android.synthetic.main.activity_comicdetail.*
import kotlinx.android.synthetic.main.novel_header_item_layout.view.*

/**
 * 漫画详情
 * @author dchain
 * @version 1.0
 * @date 2019/9/17
 */
class ComicDetailActivity : MakeActivity(), View.OnClickListener {
    private lateinit var headerView: View
    private var mode: ComicDetailData?=null
    override fun bindLayout(): Int {
        return R.layout.activity_comicdetail
    }

    @SuppressLint("SetTextI18n")
    override fun initData() {
        setState(NoData.DataState.LOADING, false)
        val id = intent.getStringExtra("id")
        val data = GsonUtil.gson2Object( WjSP.getInstance()
            .getValues(XmlCodes.PAGEDATA,""), ComicItem::class.java)


        headerView = LayoutInflater.from(activity).inflate(R.layout.novel_header_item_layout, null)

        if (!data!!.cover.isNull()) glide.load(data.cover).into(headerView.image)
        title_content.text = data.title
        headerView.state.text = "描述:${data.descs}    更新日期:${data.updateTime}"
        headerView.desc.text = "简介: ${data.descs}"

        HttpManager.comicDetail(id!!, {
            if (it.data.data.size < 1) {
                setState(NoData.DataState.NULL, false)
                return@comicDetail
            }
            mode=it.data
            val adapter = ComicSeriesAdapter(it.data.data).apply {
                onItemClickListener = object : RecyerViewItemListener {
                    override fun click(view: View, position: Int) {
                        //防止数据太大需要重新处理 临时xml再删除
                        WjSP.getInstance()
                            .setValues(XmlCodes.PAGEDATA, GsonUtil.gson2String(it.data))
                        startActivity<WatchComicActivity>(
                            "position" to
                                    if (headerView!!.sort.isSelected)
                                        list.size - choose
                                    else choose
                        )
                    }
                }
            }
            adapter.headerView = headerView

            var watchChoose = -1
            AppDatabase.db.readHistoryDao()
                .get(data.cartoonId, TypesEnum.COMIC.ordinal).observe(this) {
                   if(it!=null){
                       adapter.choose = it.value.toInt()
                       watchChoose = adapter.choose
                       adapter.notifyDataSetChanged()
                       headerView.continue_to.visibility=View.VISIBLE
                       headerView.continue_to.text =
                           resources.getString(R.string.continue_read) + watchChoose + "话"
                   }
                }


            headerView.sort.setOnClickListener {
                headerView.sort.isSelected = !headerView.sort.isSelected
                adapter.list = adapter.list.reversed() as ArrayList
                if (adapter.choose != -1) {
                    adapter.choose = adapter.list.size - adapter.choose
                }
                adapter.notifyDataSetChanged()
            }

            recyclerView.adapter = adapter
            val gridLayoutManager = CustomGridManager(activity, 3)
            recyclerView.layoutManager = gridLayoutManager
            gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (position == 0) {
                        gridLayoutManager.spanCount
                    } else 1
                }
            }
            recyclerView.addItemDecoration(RecyclerSpace(10))

            setState(NoData.DataState.GONE, false)

        }, fun(code, _, _) {
            if (code == 503) {
                setState(NoData.DataState.REFRESH, false)
            }
            if (code == -1) {
                setState(NoData.DataState.NO_NETWORK, false)
            }
        })
        headerView.start.setOnClickListener(this)
        headerView.continue_to.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        WjSP.getInstance()
            .setValues(XmlCodes.PAGEDATA, GsonUtil.gson2String(mode))
        when (v!!.id) {
            R.id.start -> {
                startActivity<WatchComicActivity>("position" to 0)
            }
            R.id.continue_to -> {
                startActivity<WatchComicActivity>(
                    "position" to (recyclerView.adapter as ComicSeriesAdapter).choose
                )
            }
        }
    }

    override fun onDestroy() {
        WjSP.getInstance().setValues(XmlCodes.PAGEDATA, "")
        super.onDestroy()
    }
}