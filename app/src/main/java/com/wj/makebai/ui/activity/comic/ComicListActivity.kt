package com.wj.makebai.ui.activity.comic

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.abase.util.GsonUtil
import com.wj.commonlib.http.HttpManager
import com.wj.commonlib.ui.ViewControl
import com.wj.ktutils.WjSP
import com.wj.ktutils.startActivity
import com.wj.makebai.R
import com.wj.makebai.statice.XmlCodes
import com.wj.makebai.ui.activity.base.MakeActivity
import com.wj.makebai.ui.adapter.ComicListAdapter
import com.wj.makebai.ui.adapter.FictionTypeAdapter
import com.wj.makebai.ui.weight.NoData
import com.wj.ui.interfaces.RecyerViewItemListener
import kotlinx.android.synthetic.main.activity_comic_layout.back
import kotlinx.android.synthetic.main.activity_comic_layout.fictionType
import kotlinx.android.synthetic.main.activity_comic_layout.recyclerView
import kotlinx.android.synthetic.main.activity_comic_layout.refresh_layout
import kotlinx.android.synthetic.main.activity_comic_layout.search_text

/**
 * 漫画搜索
 * @author dchain
 * @version 1.0
 * @date 2019/9/17
 */
class ComicListActivity :MakeActivity(),View.OnClickListener{
    private var moviesAdapter: ComicListAdapter?=null
    private var isLoad = false
    private var page=1
    private var search=""
    override fun bindLayout(): Int {
        return R.layout.activity_comic_layout
    }

    override fun initData() {
        title.visibility = View.GONE
        setState(NoData.DataState.LOADING,true)

        back.setOnClickListener(this)
        refresh_layout.setOnRefreshListener {
            refreshData()
        }
        search_text.setOnClickListener{
            startActivity<SearchComicActivity>()
        }
        fictionType.adapter= FictionTypeAdapter().apply {
            list= arrayListOf("少年","青年","少女","霸总","科幻","励志","战争","修真","玄幻","架空","生活","真人","恋爱","校园","搞笑","后宫","悬疑","恐怖","穿越","冒险","灵异","热血")
            onItemClickListener=object :RecyerViewItemListener{
                override fun click(view: View, position: Int) {
                    search=list[position]
                    refreshData()
                }
            }
        }
        fictionType.layoutManager=LinearLayoutManager(activity)
        search="少年"
        searchData(false)
    }

    /**
     * 刷新处理
     */
    override fun refreshData() {
        page=1
        isLoad = false
        searchData(false)
    }

    /**
     * 搜索数据
     */
    private fun searchData(isSearch:Boolean) {
        if (isLoad) return
        isLoad = true
        HttpManager.comicList(isSearch,search,page, {
            setState(NoData.DataState.GONE,true)
            if (it.data != null) {
                if(moviesAdapter==null){
                    moviesAdapter = ComicListAdapter(it.data.data).apply {
                        onItemClickListener=object :RecyerViewItemListener {
                            override fun click(view: View, position: Int) {
                                WjSP.getInstance()
                                    .setValues(XmlCodes.PAGEDATA, GsonUtil.gson2String(list[position]))
                                startActivity<ComicDetailActivity>("id" to list[position].cartoonId)
                            }
                        }
                    }
                    moviesAdapter!!.isFinish = true
                    recyclerView.adapter = moviesAdapter
                    recyclerView.layoutManager = LinearLayoutManager(activity)
                    ViewControl.loadMore(activity,recyclerView,moviesAdapter!!){
                        searchData(isSearch)
                    }
                }else{
                    if(page==1){
                        moviesAdapter?.list=it.data.data
                        moviesAdapter?.notifyDataSetChanged()
                    }else{
                        val oldPosition=moviesAdapter!!.list.size
                        moviesAdapter!!.list.addAll(it.data.data)
                        moviesAdapter!!.notifyItemRangeInserted(oldPosition,it.data.data.size)
                    }

                }

                refresh_layout.finishRefresh()
            } else {
                if(moviesAdapter!=null && moviesAdapter!!.list.isEmpty() || moviesAdapter==null) setState(NoData.DataState.NULL,true)
            }
            page++
            isLoad = false

        },fun(code, _, _){
            if(code==503){
                setState( NoData.DataState.REFRESH,true)
            }
            if(code==-1){
                setState( NoData.DataState.NO_NETWORK,true)
            }
        })
    }


    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.back -> finish()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        WjSP.getInstance()
            .setValues(XmlCodes.PAGEDATA, "")
    }
}