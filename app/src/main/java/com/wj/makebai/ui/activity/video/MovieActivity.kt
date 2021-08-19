package com.wj.makebai.ui.activity.video

import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abase.okhttp.OhHttpClient
import com.abase.okhttp.OhObjectListener
import com.abase.util.AbAppUtil
import com.wj.commonlib.data.mode.MovieTypeMode
import com.wj.commonlib.http.Urls
import com.wj.commonlib.ui.ViewControl
import com.wj.commonlib.utils.LoadDialog
import com.wj.ktutils.WjSP
import com.wj.ktutils.isNull
import com.wj.ktutils.showTip
import com.wj.makebai.R
import com.wj.makebai.data.db.AppDatabase
import com.wj.makebai.data.mode.MoviesMode
import com.wj.makebai.data.mode.db.SearchHistoryMode
import com.wj.makebai.data.db.TypesEnum
import com.wj.makebai.statice.StaticData
import com.wj.makebai.ui.activity.base.MakeActivity
import com.wj.makebai.ui.adapter.MovieTypeAdapter
import com.wj.makebai.ui.adapter.MoviesAdapter
import com.wj.makebai.ui.adapter.SearchHistoryAdapter
import com.wj.makebai.ui.weight.NoData
import com.wj.ui.interfaces.RecyerViewItemListener
import kotlinx.android.synthetic.main.activity_movie_layout.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.nodes.Document


/**
 * 电视综艺电影等视频列表
 * @author dchain
 * @version 1.0
 * @date 2019/9/5
 */
class MovieActivity : MakeActivity(), View.OnClickListener {
    private var moviesAdapter: MoviesAdapter? = null
    private var isLoad = false
    private var search: String? = null
    private var thread: Job? = null
    private var page = 1
    private var movieTypes = ArrayList<MovieTypeMode>()//按照地区分类
    private var movieOrderTypes = ArrayList<MovieTypeMode>()//按照类型分类
    private var movieTypeId = ""
    private var failCount = 0
    private var urlHtml = ""
    override fun bindLayout(): Int {
        return R.layout.activity_movie_layout
    }

    override fun initData() {
        title.visibility = View.GONE

        back.setOnClickListener(this)
        refresh_layout.setOnRefreshListener {
            refreshData()
        }
        if (!StaticData.initMode?.video_kw.isNull()) {
            search_text.queryHint = StaticData.initMode?.video_kw
        }
        setState(NoData.DataState.LOADING, false)
        loadData()
        search_text.setOnSearchClickListener {
            val layoutParams = search_text.layoutParams
            layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT
            search_text.requestLayout()

            loadHistory(search_history, search_text)
            search_history.visibility = View.VISIBLE
        }
        search_text.setOnCloseListener {
            val layoutParams = search_text.layoutParams
            layoutParams.width = RelativeLayout.LayoutParams.WRAP_CONTENT
            search_text.requestLayout()
            search_history.visibility = View.GONE
            false
        }
        search_text.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query.isNullOrEmpty()) return false
                search = query
                refreshData()
                search_history.visibility = View.GONE
                //插入数据库
                lifecycleScope.launch {
                    AppDatabase.db.searchHistoryDao().delete(query, TypesEnum.VIDEO.ordinal)
                    AppDatabase.db.searchHistoryDao()
                        .insert(SearchHistoryMode(0, query, TypesEnum.VIDEO.ordinal))
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

    /**
     * 刷新处理
     */
    override fun refreshData() {
        page = 1
        isLoad = false
        moviesAdapter?.list?.clear()
        moviesAdapter?.notifyDataSetChanged()
        LoadDialog.show(activity)
        loadData()
    }


    /**
     * 加载数据
     */
    private fun loadData() {
        if (isLoad) return
        isLoad = true
        urlHtml = String.format(
            if (movieTypeId == "") Urls.movie_list else Urls.movie_type_list,
                if (movieTypeId == "") {
                "vod-index-pg-$page"
            } else {
                "$movieTypeId-pg-$page"
            }
        )
        if (moviesAdapter == null) urlHtml = Urls.movie_home
        if (!search.isNull()) {
            urlHtml = String.format(Urls.movie_search, page, search)
        }
        OhHttpClient.getInit().get(urlHtml, object : OhObjectListener<String>() {
            override fun onFailure(p0: Int, p1: String?, p2: Throwable?) {
            }

            override fun onSuccess(p0: String?) {
                jspData(p0!!, urlHtml)
            }

            override fun onFinish() {
            }

        })
    }

    /**
     *解析html
     */
    private fun jspData(html: String, url: String) {
        thread = GlobalScope.launch(Dispatchers.IO) {
            try {
                var doc: Document
                doc = Jsoup.parse(html, url)
                var newsHeadlines =
                    doc.select("body > div.xing_vb")[0]
                if (newsHeadlines == null) {
                    val html = WjSP.getInstance().getValues(url, "")
                    if (!html.isNull()) {
                        doc = Jsoup.parse(html, url)
                        newsHeadlines = doc.select("ul")[0]
                    } else {
                        runOnUiThread {
                            LoadDialog.cancle()
                            if (moviesAdapter == null) setState(NoData.DataState.NULL, true)
                            moviesAdapter?.isFinish = true
                            showTip("没有数据")
                        }
                        return@launch
                    }
                }
                val movies = newsHeadlines.getElementsByClass("xing_vb")[0].children()
                movies.removeAt(0)
                movies.removeAt(movies.size-1)
                movies.removeAt(movies.size-1)
                val arrayList = ArrayList<MoviesMode>()
                for (headline in movies) {
                    val trs = headline.child(0).children()
                    val tr = trs[1]
                    val link = tr.child(1)
                    val detail = link.absUrl("href")
                    val name = link.text()
                    val type =trs[2].text()
                    val time = trs[3].text()
                    val moviesMode = MoviesMode(
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        time,
                        "",
                        name,
                        type,
                        detail
                    )
                    arrayList.add(moviesMode)
                }
                //解析分类
                if (movieTypes.isEmpty()) {
                    val movieType =
                        doc.selectFirst("#sddm").children()
                    movieType.removeAt(movieType.size-1)//删除最后一个分类，根据网站决定
                    for (index in movieType) {
                        val a = index.child(0)
                        val name = a.text()
                        val url = a.attr("href")
                        var parentId = "0"
                        if (url.contains(".html")) {
                            parentId = url.substring(url.indexOf("?") + 1, url.indexOf(".html"))
                        }
                        movieTypes.add(MovieTypeMode(parentId, name, ""))

                        if (index.children().size < 2) continue
                        val smallMovieType = index.child(1).children()
                        for (small in smallMovieType) {
                            val nameType = small.text()
                            val urlLink = small.attr("href")
                            var id = "0"
                            if (urlLink.contains(".html")) {
                                id = urlLink.substring(
                                    urlLink.indexOf("?") + 1,
                                    urlLink.indexOf(".html")
                                )
                            }
                            movieOrderTypes.add(MovieTypeMode(id, nameType, parentId))
                        }

                    }

                    runOnUiThread {
                        recyclerView_parent_type.apply {
                            layoutManager =
                                LinearLayoutManager(
                                    activity,
                                    LinearLayoutManager.HORIZONTAL,
                                    false
                                )
                        }.adapter = MovieTypeAdapter(movieTypes).apply {
                            choose = 0
                            onItemClickListener = object : RecyerViewItemListener {
                                override fun click(view: View, position: Int) {
                                    recyclerView_type.isVisible = choose != 0
                                    recyclerView_type.adapter?.let {
                                        it as MovieTypeAdapter
                                        val childList=movieOrderTypes.filter { it.parentId== movieTypes[position].id} as ArrayList<MovieTypeMode>
                                        it.choose=-1
                                        if(childList.isNotEmpty())it.list=childList
                                        else it.list.clear()
                                        recyclerView_type.scrollToPosition(0)
                                        it.notifyDataSetChanged()
                                    }
                                    search = ""
                                    movieTypeId =
                                        if (choose == -1) "" else list[position].id
                                    if(choose==0) movieTypeId=""
                                    refreshData()
                                }
                            }
                        }

                        recyclerView_type.apply {
                            layoutManager =
                                LinearLayoutManager(
                                    activity,
                                    LinearLayoutManager.HORIZONTAL,
                                    false
                                )
                        }.adapter = MovieTypeAdapter(movieOrderTypes).apply {
                            onItemClickListener = object : RecyerViewItemListener {
                                override fun click(view: View, position: Int) {
                                    search = ""
                                    movieTypeId =
                                        if (choose == -1) "vod-index-pg-" else list[position].id
                                    refreshData()
                                }
                            }
                        }

                    }
                }
                runOnUiThread {
                    setData(arrayList)
                    LoadDialog.cancle()
                    failCount = 0
                }
                WjSP.getInstance().setValues(
                    urlHtml, String(
                        doc.toString().toByteArray()
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
                failCount++
                runOnUiThread {
                    if (failCount >= 3) {
                        setState(NoData.DataState.GONE, false)
                        LoadDialog.cancle()
                        loadData()
                    } else {
                        loadData()
                    }
                }
            }
        }
    }

    /**
     * 设置数据
     */
    private fun setData(mode: ArrayList<MoviesMode>) {
        if (recyclerView.adapter == null) {
            moviesAdapter = MoviesAdapter(mode)
            recyclerView.adapter = moviesAdapter
            ViewControl.loadMore(activity, recyclerView, moviesAdapter!!) {
                loadData()
            }
        } else {
            val position = moviesAdapter!!.list.size
            moviesAdapter!!.list.addAll(mode)
            moviesAdapter!!.notifyItemRangeInserted(position, mode.size - 1)
        }
        moviesAdapter!!.isFinish = mode.size < 50
        isLoad = false
        refresh_layout.finishRefresh()

        setState(NoData.DataState.GONE, true)
        page++
    }

    /**
     * 加载历史搜索数据
     */
    private fun loadHistory(search_history: RecyclerView, search_text: SearchView) {
        AppDatabase.db.searchHistoryDao().getAll(TypesEnum.VIDEO.ordinal).observe(this) {
            val adapter = SearchHistoryAdapter(it as ArrayList<SearchHistoryMode>)
            adapter.onItemClickListener = object : RecyerViewItemListener {
                override fun click(view: View, position: Int) {
                    search_text.setQuery(adapter.list[position].name, false)
                    search = search_text.query.toString()

                    search_history.visibility = View.GONE
                    refreshData()
                    AbAppUtil.closeSoftInput(activity)
                }
            }
            search_history.adapter = adapter
            search_history.layoutManager = LinearLayoutManager(activity)
        }
    }

    override fun onBackPressed() {
        if (search_history.visibility == View.VISIBLE) {
            search_history.visibility = View.GONE
            search_text.isIconified = true
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        thread?.cancel()
    }


    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.back -> finish()
        }
    }

}