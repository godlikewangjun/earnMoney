package com.wj.makebai.ui.fragment

import android.Manifest
import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.bertsir.zbar.QrConfig
import cn.bertsir.zbar.QrManager
import com.abase.util.AbDoubleTool
import com.abase.util.AbViewUtil
import com.abase.util.Tools
import com.qq.e.ads.cfg.VideoOption
import com.qq.e.ads.nativ.ADSize
import com.qq.e.ads.nativ.NativeExpressAD
import com.qq.e.ads.nativ.NativeExpressADView
import com.qq.e.comm.util.AdError
import com.wj.commonlib.data.mode.uc.UCpdMode
import com.wj.commonlib.data.mode.uc.UcListMode
import com.wj.commonlib.http.UcRequests
import com.wj.commonlib.ui.weight.CustomLinearLayoutManager
import com.wj.ktutils.isNull
import com.wj.ktutils.showTip
import com.wj.ktutils.startActivity
import com.wj.makebai.R
import com.wj.makebai.data.emu.HomeEmu
import com.wj.makebai.data.mode.HomeTypeMode
import com.wj.makebai.statice.StaticData
import com.wj.makebai.ui.activity.article.ChannelArticleActivity
import com.wj.makebai.ui.activity.base.MakeBaseFragment
import com.wj.makebai.ui.activity.comm.QrResultActivity
import com.wj.makebai.ui.activity.comm.SearchActivity
import com.wj.makebai.ui.adapter.home.HomeAdapter
import com.wj.makebai.ui.weight.NoData
import com.wj.im.utils.GDTAdTools
import com.wj.permission.PermissionUtils
import kotlinx.android.synthetic.main.fragment_content.*
import kotlinx.android.synthetic.main.fragment_home_layout.*
import kotlinx.android.synthetic.main.fragment_home_layout.noData
import kotlinx.android.synthetic.main.fragment_home_layout.recyclerView
import kotlinx.android.synthetic.main.home_top_item_layout.*
import kotlin.math.abs


/**
 * 首页
 * @author dchain
 * @version 1.0
 * @date 2019/4/16
 */
class HomeFragment : MakeBaseFragment(), View.OnClickListener {
    companion object {
        var ucNewsList: UcListMode? = null
        var ucPd: UCpdMode? = null
        var finalHeight = 0
        var open = {
            //打开
        }
    }

    private var mADManager: NativeExpressAD? = null
    var isClose = false
    private val adapter = HomeAdapter()

    override fun setContentView(): Int {
        return R.layout.fragment_home_layout
    }

    override fun setPageName(): String {
        return this::class.java.simpleName
    }

    override fun init() {
        super.init()

        noData.type = NoData.DataState.LOADING
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView1: RecyclerView, dx: Int, dy: Int) {
                val xy = IntArray(2)
                home_top.getLocationInWindow(xy)
                if (finalHeight == 0) {
                    finalHeight = home_top.height
                }
                scrollAlpha(if (xy[1] < 0) (finalHeight + xy[1]).toDouble() else xy[1].toDouble())
            }
        })
        recyclerView.viewTreeObserver.addOnGlobalLayoutListener(object :
            OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (recyclerView.maxHeight != 0) {
                    recyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
                recyclerView.maxHeight = top.height
            }
        })
        recyclerView.getY = {
            if (banner != null) {
                val xy = IntArray(2)
                home_top.getLocationInWindow(xy)
                if (xy[1] < 0) finalHeight + xy[1] else xy[1]
            } else {
                0
            }
        }
        scan.setOnClickListener(this)
        search_bar.setOnClickListener(this)
        open = {
            scroll2Top()
        }
        go_top.setOnClickListener { scroll2Top() }

    }

    /**
     * 拉取广告
     */
    private fun loadAD() {

        val adSize = ADSize(
            ADSize.FULL_WIDTH,
            Tools.getScreenWH(activity)[0] / 2
        ) // 消息流中用AUTO_HEIGHT

        mADManager = NativeExpressAD(
            activity,
            adSize,
            StaticData.JDT_HOME_BANNER_AD,
            object : NativeExpressAD.NativeExpressADListener {
                override fun onADCloseOverlay(p0: NativeExpressADView?) {
                }

                override fun onADLoaded(adList: MutableList<NativeExpressADView>?) {
                    if(adList!=null){
                        (recyclerView.adapter as HomeAdapter).adList=ArrayList<NativeExpressADView>().apply { addAll(adList.toList()) }
                        recyclerView.adapter?.notifyDataSetChanged()
                    }
                    noData.type = NoData.DataState.GONE
                }

                override fun onADOpenOverlay(p0: NativeExpressADView?) {
                }

                override fun onRenderFail(p0: NativeExpressADView?) {
                        (recyclerView.adapter as HomeAdapter).adList!!.remove(p0)
                }

                override fun onADExposure(p0: NativeExpressADView?) {
                }

                override fun onADClosed(adView: NativeExpressADView?) {
                }

                override fun onADLeftApplication(p0: NativeExpressADView?) {
                }

                override fun onNoAD(p0: AdError?) {
                    noData.type = NoData.DataState.GONE
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

        mADManager!!.loadAD(2)
    }



    /**
     * 加载数据
     */
    private fun loadData() {
        if( adapter.fragmentManager!=null) return
        if (!StaticData.initMode?.article_kw.isNull()) {
            search_text?.hint = StaticData.initMode?.article_kw
        }
      /*  if (adapter.videoModes == null) ShortVideoRequest().getList(Urls.shortVideo, false) {
            activity!!.runOnUiThread {
                if (it.isNotEmpty()) {
                    adapter.videoModes = it
                    initViewData()
                } else initViewData()
            }
        }
        else */initViewData()
    }


    /**
     * 初始化分页
     */
    private fun initViewData() {
        if (!isAdded) return
        if(adapter.fragmentManager!=null) return
        adapter.fragmentManager = childFragmentManager

        var mHomeTypeMOde = HomeTypeMode(null, HomeEmu.TOP)
        adapter.list.add(mHomeTypeMOde)

        recyclerView.adapter = adapter
        val layoutManager = CustomLinearLayoutManager(activity)
        layoutManager.initialPrefetchItemCount = 20//预加载
        recyclerView.setItemViewCacheSize(20)//设置缓存数量
        recyclerView.layoutManager = layoutManager
        recyclerView.isNestedScrollingEnabled = true



        mHomeTypeMOde = HomeTypeMode(null, HomeEmu.TAB)
        adapter.list.add(mHomeTypeMOde)
        adapter.notifyItemInserted(1)

        loadAD()
        //监听不能滑动
        recyclerView.scrollChild = {
            scrollClose()
        }
    }


    /**
     * 滑动效果
     */
    private fun scrollAlpha(scrollY: Double) {
        if (scrollY == 0.0) return
        val contentScrollY = 1 - AbDoubleTool.div(
            scrollY - top.measuredHeight,
            finalHeight.toDouble()
        )
        val miniWidth = AbViewUtil.dip2px(activity, 45f)
        val width = Tools.getScreenWH(activity)[0]
        val widthScan = 1 - abs(
            AbDoubleTool.div(
                scrollY,
                finalHeight.toDouble()
            )
        )
        if (contentScrollY <= 1) {
            recyclerView.isScroll = true
            isClose = false
            top.setBackgroundResource(android.R.color.transparent)
            var changWidth = AbDoubleTool.mul(widthScan, width.toDouble()).toInt()
            if (changWidth < miniWidth) {
                changWidth = miniWidth.toInt()
//                search_bar.background = null
            } else {
                search_bar.background =
                    requireActivity().resources.getDrawable(R.drawable.shape_gray)
            }
            search_bar.layoutParams.width = changWidth
            search_bar.requestLayout()
            title_back.alpha = contentScrollY.toFloat()
            title_line.visibility = View.GONE
//            val rgb = ((1 - contentScrollY) * 102).toInt()

//            //扫一扫
//            val drawable = ContextCompat.getDrawable(activity!!, R.drawable.ic_scan)
//            val wrap = DrawableCompat.wrap(drawable!!).mutate()
//            DrawableCompat.setTint(wrap, Color.rgb(rgb, rgb, rgb))
//            //简单的使用tint改变drawable颜色
//            scan.setImageDrawable(wrap)
        } else {
            recyclerView.isScroll = false
            if (recyclerView.scrollY != recyclerView.getChildAt(0).height - top.height && !isClose || recyclerView.scrollY == 0) {
                scrollClose()
            }

            go_top.visibility=View.VISIBLE
            title_back.alpha = 1f
            title_line.visibility = View.VISIBLE
            search_bar.layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT
            top.setBackgroundResource(R.color.white)

        }
    }

    /**
     * 折叠之后的操作
     */
    private fun scrollClose() {
        if (isClose) return
        isClose = true
        recyclerView.stopScroll()
        (recyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
            0,
            -(home_top.height - top.height)
        )

        val fragment = childFragmentManager.findFragmentByTag("home_news")
        if (fragment != null) (fragment as UcListFragment).refreshLayout?.setEnableRefresh(true)
        changeState()
    }

    /**
     * 滚动到顶部
     */
    private fun scroll2Top() {
        isClose = false
        go_top.visibility=View.GONE
        recyclerView.smoothScrollToPosition(0)
        changeState()
    }

    /**
     * 修改状态
     */
    private fun changeState() {
        //关闭了就修改提示
//        if (isClose) {
//            activity!!.navigation.menu.getItem(0).title =
//                resources.getString(com.wj.makebai.R.string.home_tab_)

//            //利用ContextCompat工具类获取drawable图片资源
//            val imageView = activity!!.navigation.getChildAt(0)
//                .findViewById<ImageView>(com.wj.makebai.R.id.icon)
//            val drawable = imageView.drawable
//            val wrap = DrawableCompat.wrap(drawable!!).mutate()
//            DrawableCompat.setTint(wrap, Color.RED)
//            //简单的使用tint改变drawable颜色wrap
//
//            imageView.setImageDrawable(drawable)

        //修改文字颜色
//            val group =
//                ((activity!!.navigation.getChildAt(0) as ViewGroup).getChildAt(0) as ViewGroup).getChildAt(
//                    1
//                ) as ViewGroup
//            for (index in 0 until group.childCount) {
//                val child = group.getChildAt(index) as TextView
//                child.setTextColor(resources.getColor(com.wj.makebai.R.color.red))
//            }

//        } else {
        //利用ContextCompat工具类获取drawable图片资源
//            val imageView = activity!!.navigation.getChildAt(0)
//                .findViewById<ImageView>(com.wj.makebai.R.id.icon)
//            val drawable = imageView.drawable
//            val wrap = DrawableCompat.wrap(drawable!!).mutate()
//            if(MainActivity.choose==0)DrawableCompat.setTint(wrap, activity!!.resources.getColor(R.color.colorPrimary))
//            //简单的使用tint改变drawable颜色wrap
//
//            imageView.setImageDrawable(drawable)

//            activity!!.navigation.menu.getItem(0).title =
//                resources.getString(com.wj.makebai.R.string.home_tab)
//
//            //修改文字颜色
//            activity!!.navigation.itemTextColor =
//                resources.getColorStateList(com.wj.makebai.R.color.selector_tab)
//        }
    }

    fun onBackPressed(): Boolean {
        if (isClose) {
            scroll2Top()
            return true
        }
        return false
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.scan -> {//扫一扫需要加载插件
                PermissionUtils.permission(
                    requireActivity(),
                    Manifest.permission.CAMERA
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
                        }

                        override fun onGranted(permissionsGranted: ArrayList<String>) {
                            start()

                        }
                    }).request()
            }
            R.id.more -> {//频道分页更多
                requireActivity().startActivity<ChannelArticleActivity>()
            }
            R.id.search_bar -> {//点击搜索
                requireActivity().startActivity(
                    Intent(activity, SearchActivity::class.java),
                    ActivityOptions.makeSceneTransitionAnimation(
                        activity,
                        search_bar,
                        "search_bar"
                    ).toBundle()
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (marqueeView != null) {
            banner.stop()
        }
    }

    override fun onStop() {
        super.onStop()
        if (marqueeView != null) {
            banner.stop()
        }
    }

    override fun onResume() {
        super.onResume()
        if (banner != null) {
            banner.start()
        }
        if(!search_text?.hint.isNull()) return
        if( ucPd==null){
            //预加载 新闻频道数据
            UcRequests.ucPdList({ uc ->
                ucPd = uc
//                        UcRequests.ucPdNewsList(uc.data.channel[0].id, null, { list ->
//                            HomeFragment.ucNewsList = list
//                        }, fun(_, _, _) {
//                        }) {
//
//                        }
                loadData()
            }, {})
        }else
            loadData()
    }

    /**
     * 开启扫描二维码
     */
    private fun start() {
        val scanType = 0
        val scanViewType = 0
        val screen = 1

        val qrConfig = QrConfig.Builder()
            .setDesText("")//扫描框下文字
            .setShowDes(true)//是否显示扫描框下面文字
            .setShowLight(true)//显示手电筒按钮
            .setShowTitle(true)//显示Title
            .setShowAlbum(true)//显示从相册选择按钮
            .setNeedCrop(false)//是否从相册选择后裁剪图片
            .setCornerColor(Color.parseColor("#E42E30"))//设置扫描框颜色
            .setLineColor(Color.parseColor("#E42E30"))//设置扫描线颜色
            .setLineSpeed(QrConfig.LINE_MEDIUM)//设置扫描线速度
            .setScanType(scanType)//设置扫码类型（二维码，条形码，全部，自定义，默认为二维码）
            .setScanViewType(scanViewType)//设置扫描框类型（二维码还是条形码，默认为二维码）
            .setCustombarcodeformat(QrConfig.BARCODE_EAN13)//此项只有在扫码类型为TYPE_CUSTOM时才有效
            .setPlaySound(true)//是否扫描成功后bi~的声音
            .setDingPath(R.raw.qrcode)//设置提示音(不设置为默认的Ding~)
            .setIsOnlyCenter(false)//是否只识别框中内容(默认为全屏识别)
            .setTitleText("扫一扫")//设置Tilte文字
            .setTitleBackgroudColor(Color.parseColor("#262020"))//设置状态栏颜色
            .setTitleTextColor(Color.WHITE)//设置Title文字颜色
            .setShowZoom(false)//是否开始滑块的缩放
            .setAutoZoom(false)//是否开启自动缩放(实验性功能，不建议使用)
            .setFingerZoom(true)//是否开始双指缩放
            .setDoubleEngine(true)//是否开启双引擎识别(仅对识别二维码有效，并且开启后只识别框内功能将失效)
            .setScreenOrientation(screen)//设置屏幕方式
            .setOpenAlbumText("选择要识别的图片")//打开相册的文字
            .setLooperScan(false)//是否连续扫描二维码
            .create()
        QrManager.getInstance().init(qrConfig).startScan(
            activity
        ) { result ->
            if (!result.isNull()) requireActivity().startActivity<QrResultActivity>("data" to result) else showTip(
                getString(
                    R.string.noData
                )
            )
        }
    }
}