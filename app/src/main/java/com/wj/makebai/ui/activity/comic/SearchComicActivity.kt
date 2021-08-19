package com.wj.makebai.ui.activity.comic

import android.view.View
import androidx.lifecycle.lifecycleScope
import com.abase.util.AbAppUtil
import com.abase.util.GsonUtil
import com.wj.commonlib.http.HttpManager
import com.wj.commonlib.ui.ViewControl
import com.wj.ktutils.WjSP
import com.wj.ktutils.isNull
import com.wj.ktutils.startActivity
import com.wj.makebai.R
import com.wj.makebai.data.db.*
import com.wj.makebai.data.mode.db.SearchHistoryMode
import com.wj.makebai.statice.StaticData
import com.wj.makebai.statice.XmlCodes
import com.wj.makebai.ui.activity.base.MakeActivity
import com.wj.makebai.ui.adapter.*
import com.wj.makebai.ui.weight.FlowLayoutManager
import com.wj.makebai.ui.weight.MySearchView
import com.wj.makebai.ui.weight.NoData
import com.wj.ui.interfaces.RecyerViewItemListener
import kotlinx.android.synthetic.main.activity_search_comic_layout.*
import kotlinx.android.synthetic.main.activity_search_comic_layout.back
import kotlinx.android.synthetic.main.activity_search_comic_layout.clear_text
import kotlinx.android.synthetic.main.activity_search_comic_layout.pushRecyclerView
import kotlinx.android.synthetic.main.activity_search_comic_layout.recyclerView
import kotlinx.android.synthetic.main.activity_search_comic_layout.refresh_layout
import kotlinx.android.synthetic.main.activity_search_comic_layout.searchView
import kotlinx.android.synthetic.main.activity_search_comic_layout.search_history
import kotlinx.android.synthetic.main.activity_search_comic_layout.view1
import kotlinx.android.synthetic.main.activity_search_novel_layout.*
import kotlinx.android.synthetic.main.activity_search_pc_movie_layout.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 漫画搜索
 * @author dchain
 * @version 1.0
 * @date 2019/9/17
 */
class SearchComicActivity :MakeActivity(),View.OnClickListener{
    private var moviesAdapter: ComicListAdapter?=null
    private var isLoad = false
    private var page=1
    private var search=""
    override fun bindLayout(): Int {
        return R.layout.activity_search_comic_layout
    }

    override fun initData() {
        title.visibility=View.GONE

        back.setOnClickListener(this)
        refresh_layout.setOnRefreshListener {
            refreshData()
        }
        if (!StaticData.initMode?.novel_kw.isNull()) {
            searchView.setHint(StaticData.initMode?.novel_kw)
        }
        search = searchView.getHint()
        pushRecyclerView.adapter = TagsAdapter().apply {
            list = arrayListOf("海贼王", "火影", "犬夜叉", "鬼灭之刃")
            onItemClickListener = object : RecyerViewItemListener {
                override fun click(view: View, position: Int) {
                    searchView.setText(list[position])
                    searchView.setOnSearchListener.invoke()
                }

            }
        }
        pushRecyclerView.layoutManager = FlowLayoutManager()

        loadHistory(searchView)

        clear_text.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                AppDatabase.db.searchHistoryDao().removeAll( TypesEnum.COMIC.ordinal)
            }
            view1.visibility = View.GONE
            search_history.adapter?.let {
                (it as SearchTagsAdapter).apply {
                    list.clear()
                    notifyDataSetChanged()
                }
            }
        }
        searchView.setOnClearListener={
            recyclerView.adapter?.let {
                (it as ComicListAdapter).apply {
                    list.clear()
                    notifyDataSetChanged()
                }
            }
        }
        searchView.setOnSearchListener={
            search = searchView.getText()
            refreshData()
            //插入数据库
            lifecycleScope.launch {
                AppDatabase.db.searchHistoryDao().delete(search, TypesEnum.COMIC.ordinal)
                AppDatabase.db.searchHistoryDao().insert(SearchHistoryMode(0,search, TypesEnum.COMIC.ordinal))
            }
            view1.visibility=View.VISIBLE
        }
    }
    /**
     * 刷新处理
     */
    override fun refreshData(){
        page=1
        isLoad=false
        searchData(true)
    }
    /**
     * 加载数据
     */
    private fun searchData(isSearch: Boolean) {
        if (isLoad) return
        isLoad = true
        recyclerView.visibility = View.VISIBLE
        HttpManager.comicList(isSearch,search,page, {
            if (it.data != null && it.data.data.isNotEmpty()) {
                if(moviesAdapter==null){
                    moviesAdapter = ComicListAdapter(it.data.data).apply {
                        onItemClickListener=object :RecyerViewItemListener{
                            override fun click(view: View, position: Int) {
                                WjSP.getInstance()
                                    .setValues(XmlCodes.PAGEDATA, GsonUtil.gson2String(list[position]))
                                startActivity<ComicDetailActivity>("id" to list[position].cartoonId)
                            }

                        }
                    }
                    recyclerView.adapter = moviesAdapter
                    moviesAdapter!!.isFinish = true
                    ViewControl.loadMore(activity,recyclerView,moviesAdapter!!){
                        searchData(isSearch)
                    }
                }else{
                    if(page==1){
                        moviesAdapter!!.list=it.data.data
                        moviesAdapter!!.notifyDataSetChanged()
                        recyclerView.layoutManager?.scrollToPosition(0)
                    }else{
                        val old=moviesAdapter!!.list.size
                        moviesAdapter!!.list.addAll(it.data.data)
                        moviesAdapter!!.notifyItemRangeInserted(old,it.data.data.size)
                    }
                }

                isLoad = false
                refresh_layout.finishRefresh()
            }
            page++
        },fun(code, _, _){
            if(code==503){
                setState( NoData.DataState.REFRESH,true)
            }
            if(code==-1){
                setState( NoData.DataState.NO_NETWORK,true)
            }
        })
    }

    /**
     * 加载历史搜索数据
     */
    private fun loadHistory(search_text: MySearchView) {
        AppDatabase.db.searchHistoryDao().getAll(TypesEnum.COMIC.ordinal).observe(this){
            if (it.isEmpty()) {
                search_history.visibility = View.GONE
                view1.visibility = View.GONE
                return@observe
            } else {
                search_history.visibility = View.VISIBLE
                view1.visibility = View.VISIBLE
            }
            val adapter = SearchTagsAdapter().apply {
                list = it as ArrayList<SearchHistoryMode>
            }

            if (it.isNotEmpty()) search_history.visibility = View.VISIBLE
            adapter.onItemClickListener = object : RecyerViewItemListener {
                override fun click(view: View, position: Int) {
                    search_text.setText(adapter.list[position].name)
                    search = search_text.getText()

                    refreshData()
                    AbAppUtil.closeSoftInput(activity)
                }
            }
            search_history.adapter = adapter
            search_history.layoutManager = FlowLayoutManager()
        }
    }

    override fun onBackPressed() {
        if (recyclerView.visibility == View.VISIBLE &&
            recyclerView.adapter != null &&
            (recyclerView.adapter as ComicListAdapter).list.isNotEmpty()) {
            (recyclerView.adapter as ComicListAdapter).apply {
                list.clear()
                notifyDataSetChanged()
            }

            recyclerView.visibility = View.GONE
        } else {
            super.onBackPressed()
        }

    }
    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.back->finish()
        }
    }

}