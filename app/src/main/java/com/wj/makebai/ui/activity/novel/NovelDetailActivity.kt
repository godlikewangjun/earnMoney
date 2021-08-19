package com.wj.makebai.ui.activity.novel

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import com.abase.util.GsonUtil
import com.abase.view.weight.RecyclerSpace
import com.wj.commonlib.data.mode.NovelItem
import com.wj.commonlib.data.mode.NovelPageDataMode
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
 * 小说详情
 * @author dchain
 * @version 1.0
 * @date 2019/9/20
 */
class NovelDetailActivity : MakeActivity(), View.OnClickListener {
    var data: NovelPageDataMode? = null
    private lateinit var headerView: View
    private var novelTitle = ""
    private lateinit var id:String
    override fun bindLayout(): Int {
        return R.layout.activity_comicdetail
    }

    @SuppressLint("SetTextI18n")
    override fun initData() {
        setState(NoData.DataState.LOADING, false)
        id = intent.getStringExtra("id")
        val mode = GsonUtil.gson2Object( WjSP.getInstance()
            .getValues(XmlCodes.PAGEDATA,""),NovelItem::class.java)

        headerView = LayoutInflater.from(activity).inflate(R.layout.novel_header_item_layout, null)

        title_content.text = mode.title
        if (!mode.cover.isNull()) glide.load(mode.cover).into(headerView.image)
        headerView.state.text = "类型:${mode.fictionType}"
        headerView.desc.text = "简介: ${mode.descs}"
        novelTitle = mode.title

        HttpManager.novelDetail(id, {
            if (it.data == null) {
                setState(NoData.DataState.NULL, false)
                return@novelDetail
            }
            data = it.data


            val adapter = ComicSeriesAdapter(it.data.data).apply {
                onItemClickListener = object : RecyerViewItemListener {
                    override fun click(view: View, position: Int) {
                        WjSP.getInstance()
                            .setValues(XmlCodes.PAGEDATA, GsonUtil.gson2String(it.data.data))
                        startActivity<WatchNovelActivity>(
                            "position" to if (headerView!!.sort.isSelected)
                                list.size - choose
                            else choose,
                            "title" to  title_content.text
                        )
                    }
                }
            }
            adapter.headerView = headerView

            var watchChoose = -1
            AppDatabase.db.readHistoryDao()
                .get(mode.fictionId, TypesEnum.NOVEL.ordinal).observe(this) {
                   if(it!=null){
                       adapter.choose = it.value.toInt()
                       watchChoose = adapter.choose
                       adapter.notifyDataSetChanged()
                       headerView.continue_to.visibility=View.VISIBLE
                       headerView.continue_to.text =
                           resources.getString(R.string.continue_read) + watchChoose + "章"
                   }
                }

            headerView.sort.setOnClickListener {
                headerView.sort.isSelected = !headerView.sort.isSelected
                adapter.list = adapter.list.reversed() as ArrayList
                if (adapter.choose != -1) {
                    adapter.list.size - adapter.choose
                }
                adapter.notifyDataSetChanged()
            }

            recyclerView.adapter = adapter
            val gridLayoutManager = CustomGridManager(activity, 3)
            recyclerView.layoutManager = gridLayoutManager
            gridLayoutManager.spanSizeLookup = object : SpanSizeLookup() {
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
            .setValues(XmlCodes.PAGEDATA, GsonUtil.gson2String(data!!.data))
        when (v!!.id) {
            R.id.start -> {
                startActivity<WatchNovelActivity>("position" to 0, "title" to  title_content.text)
            }
            R.id.continue_to -> {
                startActivity<WatchNovelActivity>(
                    "position" to (recyclerView.adapter as ComicSeriesAdapter).choose,
                    "title" to  title_content.text
                )
            }
        }
    }

    override fun onDestroy() {
        WjSP.getInstance().setValues(XmlCodes.PAGEDATA, "")
        super.onDestroy()
    }
}