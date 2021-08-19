package com.wj.makebai.ui.activity.parsing

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.abase.util.GsonUtil
import com.wj.commonlib.http.HttpManager
import com.wj.commonlib.ui.ViewControl
import com.wj.ktutils.WjSP
import com.wj.ktutils.showTip
import com.wj.ktutils.startActivity
import com.wj.makebai.BuildConfig
import com.wj.makebai.R
import com.wj.makebai.statice.XmlCodes
import com.wj.makebai.ui.activity.base.MakeActivity
import com.wj.makebai.ui.adapter.FictionTypeAdapter
import com.wj.makebai.ui.adapter.VipMoviesAdapter
import com.wj.makebai.ui.weight.NoData
import com.wj.ui.interfaces.RecyerViewItemListener
import kotlinx.android.synthetic.main.activity_comic_layout.*
import kotlinx.android.synthetic.main.activity_pc_movie_layout.*
import kotlinx.android.synthetic.main.activity_pc_movie_layout.back
import kotlinx.android.synthetic.main.activity_pc_movie_layout.fictionType
import kotlinx.android.synthetic.main.activity_pc_movie_layout.recyclerView
import kotlinx.android.synthetic.main.activity_pc_movie_layout.refresh_layout
import kotlinx.android.synthetic.main.activity_pc_movie_layout.search_text
import kotlinx.android.synthetic.main.activity_search_novel_layout.*


/**
 * 电视综艺电影等视频列表
 * @author dchain
 * @version 1.0
 * @date 2019/9/5
 */
class VipMovieActivity : MakeActivity(), View.OnClickListener {
    private var moviesAdapter: VipMoviesAdapter? = null
    private var search: String? = null
    private var defaultPage=1
    private var page = 1
    private var isLoad = false
    override fun bindLayout(): Int {
        return R.layout.activity_pc_movie_layout
    }

    override fun initData() {
        title.visibility = View.GONE
        setState(NoData.DataState.LOADING,true)

        back.setOnClickListener(this)
        refresh_layout.setOnRefreshListener {
            refreshData()
        }
        search_text.setOnClickListener{
            startActivity<SearchVipMovieActivity>()
        }
        fictionType.adapter= FictionTypeAdapter().apply {
            list= arrayListOf("国产剧","搞笑","科幻片","国产动漫","欧美动漫","海外动漫","内地综艺","韩国剧","日本剧","脱口秀","纪录片","剧情片","微电影"
                ,"历史","恐怖","选秀","冒险","偶像","福利","伦理","古装","童年","轻小说","泡面番","武侠","儿童","动画","穿越悬疑"
                ,"战斗","科幻","神魔","格斗","海外剧"
                ,"少女","神话","校园","刑侦","犯罪","竞技","言情","科幻"
                ,"动作","推理","访谈","歌舞","剧情","文艺")
            onItemClickListener=object :RecyerViewItemListener{
                override fun click(view: View, position: Int) {
                    search=list[position]
                    refreshData()
                }
            }
        }
        fictionType.layoutManager=LinearLayoutManager(activity)
        search="国产剧"
        searchData(false)

        if(BuildConfig.DEBUG)jump.visibility=View.VISIBLE
        else jump.visibility=View.GONE

        ok.setOnClickListener{
            val jump=jump_page.text
            if(jump.isNotEmpty()){
                page=jump.toString().toInt()
                defaultPage=page
                searchData(false)
            }
        }
    }

    /**
     * 刷新处理
     */
    override fun refreshData() {
        page = defaultPage
        isLoad = false
        searchData(false)
    }
    /**
     * 加载数据
     */
    private fun searchData(isSearch:Boolean) {
        if (isLoad) return
        isLoad = true
        HttpManager.vipMovieList(isSearch,search!!, page, { it ->
            setState(NoData.DataState.GONE, true)
            if (it.data != null && it.data.size > 0) {
                if (moviesAdapter == null) {
                    moviesAdapter = VipMoviesAdapter(it.data.data).apply {
                        onItemClickListener = object : RecyerViewItemListener {
                            override fun click(view: View, position: Int) {
                                HttpManager.vipMovieDetail(list[position].videoId) {mode->
                                    try {
                                        if (mode == null) {
                                            return@vipMovieDetail showTip("没有数据")
                                        }
                                        //防止数据太大需要重新处理 临时xml再删除
                                        WjSP.getInstance()
                                            .setValues(XmlCodes.PAGEDATA, GsonUtil.gson2String(mode))
                                        startActivity<VipMovieDetailActivity>("detail" to GsonUtil.gson2String(list[position]))
                                    } catch (e: Exception) {
                                    }
                                }
                            }
                        }
                    }
                    recyclerView.adapter = moviesAdapter
                    ViewControl.loadMore(activity!!, recyclerView, moviesAdapter!!) {
                        searchData(isSearch)
                    }
                } else {
                    if(page==defaultPage){
                        moviesAdapter!!.list=it.data.data
                        moviesAdapter!!.notifyDataSetChanged()
                    }else{
                        val old = moviesAdapter!!.list.size
                        moviesAdapter!!.list.addAll(it.data.data)
                        moviesAdapter!!.notifyItemRangeInserted(old, it.data.data.size)
                    }
                }
                isLoad = false
                page++
                moviesAdapter!!.isFinish = it.data.data.size < 10
                refresh_layout.finishRefresh()

            } else {
                if (moviesAdapter != null && moviesAdapter!!.list.isEmpty() || moviesAdapter == null) setState(
                    NoData.DataState.NULL,
                    true
                )
            }
        }, fun(code, _, _) {
            if (code == 503) {
                setState(NoData.DataState.REFRESH, true)
            }
            if (code == -1) {
                setState(NoData.DataState.NO_NETWORK, true)
            }
        })
    }


    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.back -> finish()
        }
    }

    override fun onDestroy() {
        WjSP.getInstance().setValues(XmlCodes.PAGEDATA, "")
        super.onDestroy()
    }

}