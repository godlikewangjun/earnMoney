package com.wj.makebai.ui.fragment

import android.Manifest
import android.os.Bundle
import android.view.View
import com.abase.util.GsonUtil
import com.amap.api.location.AMapLocationClient
import com.qq.e.ads.cfg.VideoOption
import com.qq.e.ads.nativ.ADSize
import com.qq.e.ads.nativ.NativeExpressAD
import com.qq.e.ads.nativ.NativeExpressADView
import com.qq.e.ads.nativ.NativeExpressMediaListener
import com.qq.e.comm.constants.AdPatternType
import com.qq.e.comm.util.AdError
import com.scwang.smartrefresh.layout.api.RefreshFooter
import com.scwang.smartrefresh.layout.api.RefreshHeader
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.constant.RefreshState
import com.scwang.smartrefresh.layout.listener.OnMultiPurposeListener
import com.umeng.analytics.MobclickAgent
import com.wj.commonlib.app.BaseApplication
import com.wj.commonlib.data.mode.ArticleMode
import com.wj.commonlib.data.mode.PageMode
import com.wj.commonlib.data.mode.uc.UcArticleItem
import com.wj.commonlib.data.mode.uc.UcLisData
import com.wj.commonlib.data.mode.uc.UcListMode
import com.wj.commonlib.http.UcRequests
import com.wj.commonlib.statices.XmlConfigs
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
import com.wj.makebai.statice.UmEventCode
import com.wj.makebai.ui.activity.base.MakeBaseFragment
import com.wj.makebai.ui.weight.NoData
import com.wj.im.utils.GDTAdTools
import com.wj.ktutils.WjSP
import com.wj.permission.PermissionUtils
import kotlinx.android.synthetic.main.fragment_content.*
import org.json.JSONObject
import com.wj.makebai.ui.adapter.uc.UcAdapter

/**
 * 新闻内容
 */
class UcListFragment : MakeBaseFragment() {
    companion object {
        var readList: ArrayList<ReadHistoryMode>? = null//阅读过的

        fun newInstance(fragmentIndex: Long): UcListFragment {

            val fragment = UcListFragment()
            val bundle = Bundle()
            bundle.putLong("key", fragmentIndex)
            fragment.arguments = bundle
            return fragment
        }
    }

    private var mAdapter: UcAdapter? = null
    private var key = 0L
    private var cityCode: String? = null
    private var mLayoutManager: CustomLinearLayoutManager? = null
    var mScrollListener: ((Int) -> Unit)? = null
    private var isLoad = false
    private var isResh = false
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
            if (bundle.containsKey("key")) key = bundle.getLong("key", 0L)
        }
        //设置加载
        noData.type = NoData.DataState.LOADING

        refreshLayout.setEnableLoadMore(false)
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
            if (recyclerView.isTouch) refresh() else refreshLayout.finishRefresh()
        }

        noData.reshListener = View.OnClickListener {
            noData.type = NoData.DataState.GONE
            refresh()
        }
        //上传打开的uc频道
        val music = HashMap<String, Any>()
        music["code"] = key//自定义参数
        MobclickAgent.onEventObject(context, UmEventCode.UC_PD, music)
    }

    /**
     * 刷新
     */
    private fun refresh() {
        if (isResh) return
        isLoad = false
        isResh = true
        mAdapter?.mList?.clear()
        readList?.clear()
        loadData()
    }

    private fun initRecyclerView() {
        mLayoutManager = CustomLinearLayoutManager(activity).setScrollEnabled(true)
        recyclerView!!.isNestedScrollingEnabled = true

        recyclerView!!.layoutManager = mLayoutManager
        refreshLayout.setOnMultiPurposeListener(object : OnMultiPurposeListener {
            override fun onFooterMoving(
                footer: RefreshFooter?,
                isDragging: Boolean,
                percent: Float,
                offset: Int,
                footerHeight: Int,
                maxDragHeight: Int
            ) {
            }

            override fun onHeaderStartAnimator(
                header: RefreshHeader?,
                headerHeight: Int,
                maxDragHeight: Int
            ) {
            }

            override fun onFooterReleased(
                footer: RefreshFooter?,
                footerHeight: Int,
                maxDragHeight: Int
            ) {
            }

            override fun onStateChanged(
                refreshLayout: RefreshLayout,
                oldState: RefreshState,
                newState: RefreshState
            ) {
            }

            override fun onHeaderMoving(
                header: RefreshHeader?,
                isDragging: Boolean,
                percent: Float,
                offset: Int,
                headerHeight: Int,
                maxDragHeight: Int
            ) {
                activity!!.runOnUiThread {
                    if (offset > headerHeight+80) {
                        recyclerView.isTouch = false
                        refreshLayout.finishRefresh()
                        HomeFragment.open.invoke()
                        recyclerView.postDelayed({ recyclerView.isTouch = true; refreshLayout.setEnableRefresh(false) }, 500)
                    }
                }

            }

            override fun onFooterFinish(footer: RefreshFooter?, success: Boolean) {
            }

            override fun onFooterStartAnimator(
                footer: RefreshFooter?,
                footerHeight: Int,
                maxDragHeight: Int
            ) {
            }

            override fun onHeaderReleased(
                header: RefreshHeader?,
                headerHeight: Int,
                maxDragHeight: Int
            ) {
            }

            override fun onLoadMore(refreshLayout: RefreshLayout) {
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {
            }

            override fun onHeaderFinish(header: RefreshHeader?, success: Boolean) {
            }

        })
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
        if (key == 0L) {
            LikeDb.query(requireActivity(), TypesEnum.UC) {
                if (it.isNullOrEmpty()) {
                    noData.type = NoData.DataState.NULL
                    return@query
                }
                noData.type = NoData.DataState.GONE

                val mode = PageMode(true, ArrayList<ArticleMode>(), 0, 1, 100)
                for (index in it) {
                    mode.list.add(GsonUtil.gson2Object(index.value, ArticleMode::class.java))
                }
//                setData(mode)
            }
        } else {
            if (key == 200L) {
                val saveCode =
                    WjSP.getInstance().getValues(
                        XmlConfigs.UC_CITYCODES, ""
                    )
                if (!saveCode.isNull()) {
                    try {
                        val json = JSONObject(saveCode)
                        if (System.currentTimeMillis() - json.getLong("time") < 24 * 60 * 60 * 1000) {
                            cityCode = json.getString("code")
                        }
                    } catch (e: Exception) {
                        WjSP.getInstance()
                            .setValues(
                                XmlConfigs.UC_CITYCODES, ""
                            )
                    }
                }
                if (cityCode.isNull()) {
                    PermissionUtils.permission(
                        requireActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                        .rationale(object : PermissionUtils.OnRationaleListener {
                            override fun rationale(shouldRequest: PermissionUtils.OnRationaleListener.ShouldRequest) {
                                shouldRequest.again(true)
                            }
                        })
                        .callback(object : PermissionUtils.FullCallback {
                            override fun onDenied(
                                permissionsDeniedForever: ArrayList<String>?,
                                permissionsDenied: ArrayList<String>
                            ) {
                                noData.type = NoData.DataState.NULL
                            }

                            override fun onGranted(permissionsGranted: ArrayList<String>) {
                                //初始化定位
                                val mLocationClient = AMapLocationClient(activity)
                                //设置定位回调监听
                                mLocationClient.setLocationListener { p0 ->
                                    cityCode = p0?.cityCode!!
                                    val json = JSONObject()
                                    json.put("code", cityCode)
                                    json.put("time", System.currentTimeMillis())
                                    WjSP.getInstance()
                                        .setValues(
                                            XmlConfigs.UC_CITYCODES, json.toString()
                                        )
                                    if (!cityCode.isNull()) {
                                        mLocationClient.stopLocation()
                                        geNewsList()
                                    }
                                }
                                //启动定位
                                mLocationClient.startLocation()
                            }
                        }).request()

                } else geNewsList()
            } else {
                geNewsList()
            }
        }
    }

    /**
     * 获取新闻列表
     */
    private fun geNewsList() {
        val success = fun(mode: UcListMode) {
            //处理是否阅读
            if (readList != null) for (index in mode.data.items) {
                for (like in readList!!) {
                    if (index.id == like.title) {
                        index.isRead = true
                        break
                    }
                }
            }
            setData(mode.data)
        }
        if (HomeFragment.ucNewsList != null) {
            success.invoke(HomeFragment.ucNewsList!!)
            HomeFragment.ucNewsList = null
            return
        }
        UcRequests.ucPdNewsList(key, cityCode, success, fun(code, _, _) {
            when (code) {
                503 -> noData.type = NoData.DataState.REFRESH
                -1 -> noData.type = NoData.DataState.NO_NETWORK
            }
        }) {
            refreshLayout.finishRefresh()
            isResh = false
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
                        val position: Int =
                            mAdapter!!.mList.size - 13  + GDTAdTools.ITEMS_PER_AD * i
                        if (position < mAdapter!!.mList.size) {
                            val view = mAdViewList!![i]
                            if (view.boundData.adPatternType == AdPatternType.NATIVE_VIDEO) {
                                view.setMediaListener(mediaListener)
                            }
//                            view.render()
                            mAdViewPositionMap[view] = position // 把每个广告在列表中位置记录下来
                            if (mAdapter != null && mAdapter!!.mList != null) {
//                                mAdapter!!.mList.add(position,ArtTypeMode(mAdViewList!![i],ArtEmu.AD))
                                if (position >= 0 && position < mAdapter!!.mList.size && mAdViewList!![i] != null) {
                                    mAdapter!!.mList.add(
                                        position,
                                        ArtTypeMode(mAdViewList!![i], ArtEmu.AD)
                                    )
                                }
                                mAdapter!!.notifyItemInserted(position)
                            }
                        }
//                        mAdapter!!.notifyItemRangeChanged(mAdapter!!.mList.size-mAdViewList!!.size,mAdapter!!.mList.size)
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
                requireActivity()
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
    private fun setData(mode: UcLisData) {
        val arts = ArrayList<ArtTypeMode>()
        try {
            for (index in mode.items) {
                val ucArticleItem = GsonUtil.gson2Object(
                    GsonUtil.gson2String(mode.articles.getAsJsonObject(index.id)),
                    UcArticleItem::class.java
                )
                if (ucArticleItem.show_impression_url.isNull()) {//广告过滤 uc收益太低
                    ucArticleItem.isRead = index.isRead
                    arts.add(ArtTypeMode(ucArticleItem, ArtEmu.ART))
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (arts.size < 1) {
            if (mAdapter == null || mAdapter!!.mList.size < 1) {
                noData.type = NoData.DataState.NULL
            }
            return
        }
        if (mAdapter == null || mAdapter!!.mList.size < 2) {
            mAdapter = UcAdapter(arts)
            recyclerView!!.adapter = mAdapter

            ViewControl.loadMore(requireActivity(), recyclerView!!, mAdapter!!) {
                if (isLoad) return@loadMore
                isLoad = true
                loadData()
            }
        } else {
//            val position = mAdapter!!.mList.size
            mAdapter!!.mList.addAll(arts)
//            mAdapter!!.notifyItemRangeInserted(
//                position,
//                mode.items.size - 1
//            )
        }
        if(mAdapter!!.mList.isNotEmpty()) loadAD()
        refreshLayout.finishRefresh()
        if (mAdapter != null && mAdapter!!.mList.size > 0) {
            noData.type = NoData.DataState.GONE
        }
        mAdapter?.isFinish = mAdapter!!.mList.size < 10

        isLoad = false
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
