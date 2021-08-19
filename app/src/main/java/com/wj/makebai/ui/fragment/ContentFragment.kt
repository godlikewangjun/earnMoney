package com.wj.makebai.ui.fragment

import android.os.Bundle
import android.view.View
import com.abase.util.GsonUtil
import com.qq.e.ads.cfg.VideoOption
import com.qq.e.ads.nativ.ADSize
import com.qq.e.ads.nativ.NativeExpressAD
import com.qq.e.ads.nativ.NativeExpressADView
import com.qq.e.ads.nativ.NativeExpressMediaListener
import com.qq.e.comm.constants.AdPatternType
import com.qq.e.comm.util.AdError
import com.wj.commonlib.data.mode.ArticleMode
import com.wj.commonlib.data.mode.PageMode
import com.wj.commonlib.http.HttpManager
import com.wj.commonlib.http.Urls
import com.wj.commonlib.ui.ViewControl
import com.wj.commonlib.ui.weight.CustomLinearLayoutManager
import com.wj.ktutils.isNull
import com.wj.makebai.R
import com.wj.makebai.data.emu.ArtEmu
import com.wj.makebai.data.mode.ArtTypeMode
import com.wj.makebai.data.mode.db.ReadHistoryMode
import com.wj.makebai.data.db.LikeDb
import com.wj.makebai.data.db.TypesEnum
import com.wj.makebai.statice.StaticData
import com.wj.makebai.ui.activity.base.MakeBaseFragment
import com.wj.makebai.ui.adapter.ContentAdapter
import com.wj.makebai.ui.weight.NoData
import com.wj.im.utils.GDTAdTools
import com.xiaomi.push.it
import kotlinx.android.synthetic.main.fragment_content.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * 新闻内容
 */
class ContentFragment : MakeBaseFragment() {
    companion object {
        var readList: ArrayList<ReadHistoryMode>? = null//点赞过的

        fun newInstance(fragmentIndex: String): ContentFragment {

            val fragment = ContentFragment()
            val bundle = Bundle()
            bundle.putString("key", fragmentIndex)
            fragment.arguments = bundle
            return fragment
        }
    }

    private var mAdapter: ContentAdapter? = null
    private var key = ""
    private var mLayoutManager: CustomLinearLayoutManager? = null
    var mScrollListener: ((Int) -> Unit)? = null
    private var page = 0
    private var startId = -1
    private var isLoad = false

    //获取原生广告
    private var mADManager: NativeExpressAD? = null
    private var mAdViewList: List<NativeExpressADView>? = null
    private val mAdViewPositionMap = java.util.HashMap<NativeExpressADView, Int>()

    override fun setContentView(): Int {
        super.setContentView()
        return R.layout.fragment_content
    }

    override fun setPageName(): String {
        return this::class.java.simpleName
    }

    override fun init() {
        super.init()
        val bundle = arguments
        if (bundle != null) {
            if (bundle.containsKey("key")) key = bundle.getString("key")!!

            if (!bundle.getBoolean("refresh")) {
                refreshLayout.setEnableRefresh(false)//默认关闭刷新
                refreshLayout.setEnableNestedScroll(false)
            }
        }
        noData.type = NoData.DataState.LOADING
        refreshLayout.setEnableLoadMore(false)//是否启用上拉加载功能

        recyclerView!!.addOnScrollListener(object :
            androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(
                recyclerView: androidx.recyclerview.widget.RecyclerView,
                dx: Int,
                dy: Int
            ) {
                mScrollListener?.invoke(dy)
            }
        })
        initRecyclerView()


        refreshLayout.setOnRefreshListener {
            refresh()
        }

        noData.reshListener = View.OnClickListener {
            refresh()
        }

    }

    /**
     * 清空删除数据
     */
    fun clear() {
        mAdapter?.mList?.clear()
        mAdapter?.notifyItemRangeRemoved(0, mAdapter?.mList?.size!!)
        noData.type = NoData.DataState.NULL
    }

    /**
     * 刷新
     */
    private fun refresh() {
        isLoad = false
        page = 0
        startId = -1
        mAdapter?.mList?.clear()
        readList?.clear()
        loadData()
    }

    fun isMove(isMove: Boolean) {
        refreshLayout.setEnableRefresh(isMove)
        mLayoutManager!!.setScrollEnabled(isMove)
        //展开了就直接滑动到顶部 防止滑动冲突，目前只有这样处理
        if (!isMove) {
            refreshLayout.finishRefresh()
            mLayoutManager!!.scrollToPositionWithOffset(0, 0)
        }
    }

    private fun initRecyclerView() {
        mLayoutManager = CustomLinearLayoutManager(context!!)
        recyclerView!!.layoutManager = mLayoutManager
    }

    override fun onResume() {
        super.onResume()
        if (mAdapter == null && recyclerView != null || mAdapter!!.mList.isNullOrEmpty()) {
            loadData()
        }
    }

    /**
     * 加载数据
     */
    private fun loadData() {
        if (key.isNull()) {
            LikeDb.query(activity!!, TypesEnum.ARTICLE) {
                if (it.isNullOrEmpty()) {
                    noData.type = NoData.DataState.NULL
                    return@query
                }
                noData.type = NoData.DataState.GONE

                val mode = PageMode(true, ArrayList<ArticleMode>(), 0, 1, 100)
                for (index in it) {
                    mode.list.add(GsonUtil.gson2Object(index.value, ArticleMode::class.java))
                }
                setData(mode)
            }

        } else {
            HttpManager.allList<ArticleMode>(Urls.articleList, page, startId, key, null, {
                if (it.list.isNotEmpty()) {//解决多重泛型问题，使用这个接口
                    GlobalScope.launch(Dispatchers.IO) {
                        it.list = GsonUtil.Gson2ArryList(
                            GsonUtil.gson2String(it.list),
                            Array<ArticleMode>::class.java
                        ).toList() as ArrayList<ArticleMode>

                        activity!!.runOnUiThread {
                            setData(it)
                        }
                    }
                } else {
                    setData(it)
                }
            }, fun(code, _, _) {
                if (code == 503) {
                    noData?.type = NoData.DataState.REFRESH
                } else if (code == -1) {
                    noData?.type = NoData.DataState.NO_NETWORK
                }
                refreshLayout.finishRefresh()
            })
        }
    }

    /**
     * 拉取广告
     */
    private fun loadAD() {

        val adSize = ADSize(
            ADSize.FULL_WIDTH,
            ADSize.AUTO_HEIGHT
        ) // 消息流中用AUTO_HEIGHT

        mADManager = NativeExpressAD(
            activity,
            adSize,
            StaticData.JDT_ART_AD,
            object : NativeExpressAD.NativeExpressADListener {
                override fun onADCloseOverlay(p0: NativeExpressADView?) {
                }

                override fun onADLoaded(adList: MutableList<NativeExpressADView>?) {
                    mAdViewList = adList
                    for (i in mAdViewList!!.indices) {
                        val position: Int =  mAdapter!!.mList.size - 15 + GDTAdTools.FIRST_AD_POSITION + GDTAdTools.ITEMS_PER_AD * i
                        if (position < mAdapter!!.mList.size) {
                            val view = mAdViewList!![i]
                            if (view.boundData.adPatternType == AdPatternType.NATIVE_VIDEO) {
                                view.setMediaListener(mediaListener)
                            }
                            mAdViewPositionMap[view] = position // 把每个广告在列表中位置记录下来
                            if (mAdapter != null && mAdapter!!.mList != null) {
//                                mAdapter!!.mList.add(position,ArtTypeMode(mAdViewList!![i],ArtEmu.AD))
                                if (position >= 0 && position < mAdapter!!.mList.size && mAdViewList!![i] != null) {
                                    mAdapter!!.mList.add(
                                        position,
                                        ArtTypeMode(mAdViewList!![i], ArtEmu.AD)
                                    )
                                }
                            }
                            mAdapter!!.notifyItemInserted(position)
                        }
//                        mAdapter!!.notifyItemInserted(mAdapter!!.mList.size-10,mAdapter!!.mList.size-1)
                    }
                }

                override fun onADOpenOverlay(p0: NativeExpressADView?) {
                }

                override fun onRenderFail(p0: NativeExpressADView?) {
                }

                override fun onADExposure(p0: NativeExpressADView?) {
                }

                override fun onADClosed(adView: NativeExpressADView?) {
                    if (mAdapter != null) {
                        val removedPosition = mAdViewPositionMap[adView]!!
                        mAdapter!!.mList.removeAt(removedPosition)
                        mAdapter!!.notifyItemRemoved(removedPosition) // position为adView在当前列表中的位置
                        mAdapter!!.notifyItemRangeChanged(0, mAdapter!!.mList.size - 1)
                    }
                }

                override fun onADLeftApplication(p0: NativeExpressADView?) {
                }

                override fun onNoAD(p0: AdError?) {
                }

                override fun onADClicked(p0: NativeExpressADView?) {
                }

                override fun onRenderSuccess(p0: NativeExpressADView?) {
                }

            }
        )
        mADManager!!.setMaxVideoDuration(10)
        /**
         * 如果广告位支持视频广告，强烈建议在调用loadData请求广告前调用setVideoPlayPolicy，有助于提高视频广告的eCPM值 <br/>
         * 如果广告位仅支持图文广告，则无需调用
         */

        /**
         * 设置本次拉取的视频广告，从用户角度看到的视频播放策略<p/>
         *
         * "用户角度"特指用户看到的情况，并非SDK是否自动播放，与自动播放策略AutoPlayPolicy的取值并非一一对应 <br/>
         *
         * 如自动播放策略为AutoPlayPolicy.WIFI，但此时用户网络为4G环境，在用户看来就是手工播放的
         */
        /**
         * 如果广告位支持视频广告，强烈建议在调用loadData请求广告前调用setVideoPlayPolicy，有助于提高视频广告的eCPM值 <br></br>
         * 如果广告位仅支持图文广告，则无需调用
         */
        /**
         * 设置本次拉取的视频广告，从用户角度看到的视频播放策略
         *
         *
         *
         * "用户角度"特指用户看到的情况，并非SDK是否自动播放，与自动播放策略AutoPlayPolicy的取值并非一一对应 <br></br>
         *
         * 如自动播放策略为AutoPlayPolicy.WIFI，但此时用户网络为4G环境，在用户看来就是手工播放的
         */
        mADManager!!.setVideoPlayPolicy(
            GDTAdTools.getVideoPlayPolicy(
                VideoOption.AutoPlayPolicy.ALWAYS,
                activity!!
            )
        ) // 本次拉回的视频广告，在用户看来是否为自动播放的

        mADManager!!.loadAD(GDTAdTools.AD_COUNT)
    }

    private val mediaListener: NativeExpressMediaListener = object :
        NativeExpressMediaListener {
        override fun onVideoInit(nativeExpressADView: NativeExpressADView) {
        }

        override fun onVideoLoading(nativeExpressADView: NativeExpressADView) {
        }

        override fun onVideoCached(p0: NativeExpressADView?) {
        }

        override fun onVideoReady(
            nativeExpressADView: NativeExpressADView,
            l: Long
        ) {
        }

        override fun onVideoStart(nativeExpressADView: NativeExpressADView) {
        }

        override fun onVideoPause(nativeExpressADView: NativeExpressADView) {
        }

        override fun onVideoComplete(nativeExpressADView: NativeExpressADView) {
        }

        override fun onVideoError(
            nativeExpressADView: NativeExpressADView,
            adError: AdError
        ) {
        }

        override fun onVideoPageOpen(nativeExpressADView: NativeExpressADView) {
        }

        override fun onVideoPageClose(nativeExpressADView: NativeExpressADView) {
        }
    }

    /**
     * 设置数据
     */
    private fun setData(mode: PageMode<ArticleMode>) {
        val arts = ArrayList<ArtTypeMode>()
        //处理是否阅读
        for (index in mode.list) {
            if (readList != null) for (like in readList!!) {
                if (index.articlename == like.title) {
                    index.isRead = true
                    break
                }
            }
            arts.add(ArtTypeMode(index, ArtEmu.ART))
        }
        if (mAdapter == null) {
            mAdapter = ContentAdapter(arts)
            val bundle = arguments
            if (bundle != null) {
                mAdapter!!.isAd = !bundle.containsKey("refresh")
            }

            recyclerView!!.adapter = mAdapter

            ViewControl.loadMore(context!!, recyclerView!!, mAdapter!!) {
                if (isLoad) return@loadMore
                isLoad = true
                page++
                startId =
                    (mAdapter!!.mList[mAdapter!!.mList.size - 1].mode as ArticleMode).articleid
                loadData()
            }
        } else {
            val position = mAdapter!!.mList.size
            mAdapter!!.mList.addAll(arts)
            if (page != 0) mAdapter!!.notifyItemRangeInserted(
                position,
                mode.list.size - 1
            ) else mAdapter!!.notifyDataSetChanged()
        }
        loadAD()

        if (page == 0) refreshLayout.finishRefresh()
        refreshLayout.finishLoadMore()
        mAdapter!!.isFinish = mode.isEnd

        isLoad = false
        if (mAdapter?.mList!!.isNotEmpty()) {
            noData.type = NoData.DataState.GONE
        } else {
            noData?.type = NoData.DataState.NULL
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 使用完了每一个NativeExpressADView之后都要释放掉资源。
        if (mAdViewList != null) {
            for (view in mAdViewList!!) {
                view.destroy()
            }
        }
    }
}
