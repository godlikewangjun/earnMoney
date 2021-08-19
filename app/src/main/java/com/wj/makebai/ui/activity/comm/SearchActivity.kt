package com.wj.makebai.ui.activity.comm

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.transition.Explode
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.abase.util.AbAppUtil
import com.wj.commonlib.data.mode.ArticleMode
import com.wj.commonlib.data.mode.PageMode
import com.wj.commonlib.ui.ViewControl
import com.wj.ktutils.isNull
import com.wj.ktutils.startActivity
import com.wj.makebai.R
import com.wj.makebai.data.db.AppDatabase
import com.wj.makebai.data.emu.ArtEmu
import com.wj.makebai.data.mode.ArtTypeMode
import com.wj.makebai.data.mode.db.SearchHistoryMode
import com.wj.makebai.data.db.TypesEnum
import com.wj.makebai.ui.activity.base.MakeActivity
import com.wj.makebai.ui.adapter.ContentAdapter
import com.wj.makebai.ui.adapter.SearchHistoryAdapter
import com.wj.ui.interfaces.RecyerViewItemListener
import com.xiaomi.push.it
import kotlinx.android.synthetic.main.activity_search_layout.*
import kotlinx.android.synthetic.main.include_search_layout.*
import kotlinx.coroutines.launch

/**
 * 搜索的页面
 * @author dchain
 * @version 1.0
 * @date 2019/9/2
 */
class SearchActivity : MakeActivity(), View.OnClickListener {
    private var mAdapter: ContentAdapter? = null
    private var search = ""
    private var page = 0
    private var startId = -1
    private var isLoad = false

    override fun onCreate(p0: Bundle?) {
        window.exitTransition = Explode().setDuration(500)
        super.onCreate(p0)
    }

    override fun bindLayout(): Int {
        return R.layout.activity_search_layout
    }

    override fun initData() {
        title.visibility = View.GONE

        btn_cancel.setOnClickListener(this)
        search_close_btn.setOnClickListener(this)
        search_text.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (search_text.text!!.isNotEmpty()) {
                    recyclerView.visibility = View.VISIBLE
                    search_history.visibility = View.GONE
                    search_close_btn.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.GONE
                    search_history.visibility = View.VISIBLE
                    search_close_btn.visibility = View.GONE
                }
            }
        })
        search_text.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if ((event != null && KeyEvent.KEYCODE_ENTER == event.keyCode && KeyEvent.ACTION_DOWN == event.action)) {
                    refreshData()
                    return true
                }
                return false
            }

        })
        loadHistory()
    }

    /**
     * 开始搜索
     */
    override fun refreshData() {
//        search = search_text.hint.toString()
        if (!search_text.text.isNull()) search = search_text.text.toString()
        lifecycleScope.launch {
            AppDatabase.db.searchHistoryDao().delete(search, TypesEnum.ARTICLE.ordinal)
            AppDatabase.db.searchHistoryDao().insert(SearchHistoryMode(0,search, TypesEnum.ARTICLE.ordinal))
        }
        isLoad = false
        page = 0
        startId = -1
        mAdapter?.mList?.clear()
        loadData()
    }

    /**
     * 加载数据
     */
    private fun loadData() {
        activity!!.startActivity<WebActivity>("url" to "https://www.baidu.com/s?wd=$search")
//        HttpManager.allList<ArticleMode>(Urls.articleList, page, startId, null, search, {
//            if (it.list.isNotEmpty()) {//解决多重泛型问题，使用这个接口
//                doAsync {
//                    it.list = GsonUtil.Gson2ArryList(
//                        GsonUtil.gson2String(it.list),
//                        Array<ArticleMode>::class.java
//                    ) as ArrayList<ArticleMode>
//
//                    runOnUiThread {
//                        setData(it)
//                    }
//                }
//            }
//            else{
//                setData(it)
//            }
//        }, fun(code, _, _) {
//            if (code == 503) {
//                setState(NoData.DataState.REFRESH, true)
//            }
//            if (code == -1) {
//                setState(NoData.DataState.NO_NETWORK, true)
//            }
//        })
    }

    /**
     * 设置数据
     */
    private fun setData(mode: PageMode<ArticleMode>){
        val arts = ArrayList<ArtTypeMode>()
        //转换
       for (index in mode.list){
            arts.add(ArtTypeMode(index, ArtEmu.ART))
        }
        if (mAdapter == null) {
            mAdapter = ContentAdapter(arts)
            recyclerView!!.adapter = mAdapter

            ViewControl.loadMore(activity, recyclerView!!, mAdapter!!) {
                if (isLoad) return@loadMore
                isLoad = true
                page++
                startId = (mAdapter!!.mList[mAdapter!!.mList.size - 1].mode as ArticleMode).articleid
                loadData()
            }
        } else {
            mAdapter!!.mList.addAll(arts)
            mAdapter!!.notifyDataSetChanged()
        }
        if (page == 0 && mode.list.size < 1) {
            activity!!.startActivity<WebActivity>("url" to "https://www.baidu.com/s?wd=$search")
        }
        mAdapter!!.isFinish = mode.isEnd

        isLoad = false
    }

    override fun onResume() {
        super.onResume()
        if (search_history != null) loadHistory()
    }

    override fun onDestroy() {
        AbAppUtil.closeSoftInput(activity)
        super.onDestroy()
    }

    /**
     * 加载历史搜索数据
     */
    private fun loadHistory() {
        AppDatabase.db.searchHistoryDao().getAll(TypesEnum.ARTICLE.ordinal).observe(this){
            val adapter = SearchHistoryAdapter(it as ArrayList<SearchHistoryMode>)
            adapter.onItemClickListener = object : RecyerViewItemListener {
                override fun click(view: View, position: Int) {
                    search_text.setText(adapter.list[position].name)
                    AbAppUtil.closeSoftInput(activity)
                    refreshData()
                }
            }
            search_history.adapter = adapter
            search_history.layoutManager = LinearLayoutManager(activity)
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.search_close_btn -> {
                search_text.setText("")
            }
            R.id.btn_cancel -> {//关闭
               onBackPressed()
            }
        }
    }
}