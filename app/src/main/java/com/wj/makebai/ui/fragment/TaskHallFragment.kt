package com.wj.makebai.ui.fragment

import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.abase.util.Tools
import com.anbetter.danmuku.model.DanMuModel
import com.anbetter.danmuku.model.utils.DimensionUtil
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.qq.e.ads.cfg.VideoOption
import com.qq.e.ads.nativ.ADSize
import com.qq.e.ads.nativ.NativeExpressAD
import com.qq.e.ads.nativ.NativeExpressADView
import com.qq.e.comm.constants.AdPatternType
import com.qq.e.comm.util.AdError
import com.wj.commonlib.data.mode.AppTaskMode
import com.wj.commonlib.data.mode.PageMode
import com.wj.commonlib.data.mode.TaskTypeMode
import com.wj.commonlib.http.HttpManager
import com.wj.commonlib.http.TaskHallRequests
import com.wj.commonlib.statices.XmlConfigs
import com.wj.commonlib.utils.bubbleDialog
import com.wj.commonlib.utils.setOnClick
import com.wj.im.utils.GDTAdTools
import com.wj.ktutils.WjSP
import com.wj.ktutils.startActivity
import com.wj.makebai.R
import com.wj.makebai.data.mode.BannerMode
import com.wj.makebai.statice.StaticData
import com.wj.makebai.statice.XmlCodes
import com.wj.makebai.ui.activity.appTask.EarnTaskActivity
import com.wj.makebai.ui.activity.appTask.SearchTaskActivity
import com.wj.makebai.ui.activity.appTask.VideoTaskActivity
import com.wj.makebai.ui.activity.base.MakeBaseFragment
import com.wj.makebai.ui.activity.bbs.BBSActivity
import com.wj.makebai.ui.adapter.ViewBannerAdapter
import com.wj.makebai.ui.adapter.home.HomeAdapter
import com.wj.makebai.ui.control.CommControl
import com.wj.makebai.utils.MbTools
import com.xiaomi.push.it
import com.youth.banner.Banner
import com.youth.banner.transformer.AlphaPageTransformer
import kotlinx.android.synthetic.main.activity_moviedetail.*
import kotlinx.android.synthetic.main.activity_moviedetail.recyclerView
import kotlinx.android.synthetic.main.fragment_home_layout.*
import kotlinx.android.synthetic.main.fragment_task_hall.*
import kotlinx.android.synthetic.main.fragment_task_hall.danmu
import kotlinx.android.synthetic.main.fragment_task_hall.search_bar
import kotlinx.android.synthetic.main.home_top_item_layout.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 任务大厅
 * @author Administrator
 * @version 1.0
 * @date 2020/7/15
 */
class TaskHallFragment : MakeBaseFragment() {

    companion object {
        var list: PageMode<AppTaskMode>? = null
        var types: ArrayList<TaskTypeMode>? = null
    }

    private var mADManager: NativeExpressAD? = null
    val fragments = arrayListOf<Fragment>()
    override fun setPageName(): String {
        return this::class.java.simpleName
    }

    override fun setContentView(): Int {
        super.setContentView()
        return R.layout.fragment_task_hall
    }


    override fun init() {
        super.init()
        refresh_layout.apply {
            setOnRefreshListener {
                if (tab.selectedTabPosition != -1) {
                    (fragments[tab.selectedTabPosition] as TaskListFragment).refreshLayout()
                    refresh_layout.finishRefresh()
                }
            }
            setFooterHeight(0f)
            setOnLoadMoreListener {
                if (tab.selectedTabPosition != -1) {
                    (fragments[tab.selectedTabPosition] as TaskListFragment).loadMore()
                    refresh_layout.finishLoadMore()
                }
            }
        }
        //动态设置高度确保广告图的适应性
        val layoutParams = banner_content.layoutParams
        layoutParams.height = Tools.getScreenWH(context)[0] / 3
        CommControl.setBanner(
            StaticData.initMode!!.homeBanner,
            banner as Banner<BannerMode, ViewBannerAdapter>,
            Glide.with(requireContext())
        )
        banner.setPageTransformer(AlphaPageTransformer())
        loadAD()

        if (types != null) {
            setTab(types!!)
            types = null
        } else
            TaskHallRequests.getTaskType {
                setTab(it)
            }

        search_bar.setOnClick(onClick)
        bbs.setOnClick(onClick)

        //第一次打开需要显示引导
        if (WjSP.getInstance().getValues(XmlConfigs.G_BBS, -1) == -1) {
            bbs.viewTreeObserver.addOnGlobalLayoutListener(object :
                OnGlobalLayoutListener {
                override fun onGlobalLayout() {

                    bubbleDialog(activity!!, bbs, "论坛交流在这里")
                    WjSP.getInstance().setValues(XmlConfigs.G_BBS, 0)
                    bbs.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
        }
        danmu.isEnabled = false
        danmu.prepare()
        val array = resources.getStringArray(R.array.earn_money)
        lifecycleScope.launch {
            delay(1000)
            for (index in array) {
                withContext(Dispatchers.Main) {
                    danmu.add(getModel(index))
                }
                delay(500)
            }
        }

        lifecycleScope.launch {
            delay(1000)
            ad_video.playAnimation()
        }
        ad_video.setOnClick {
            startActivity<EarnTaskActivity>()
        }
        HttpManager.taskInfo({taskMode->
            ad_video.setOnClick {
                if (MbTools.isLogin(requireContext())) {
                    //判断视频任务是否做了
                    val videoTime = WjSP.getInstance()
                        .getValues(XmlCodes.VIDEOWATCH, 0L)
                    if (taskMode.videoCount<taskMode.videoAllCount && (videoTime == 0L || System.currentTimeMillis() - videoTime >= 10 * 60 * 1000)) {
                        startActivity<VideoTaskActivity>()
                    } else startActivity<EarnTaskActivity>()
                }
            }
        }){

        }
    }

    private fun getModel(text: String): DanMuModel {
        val danMuView = DanMuModel()
        danMuView.displayType = DanMuModel.RIGHT_TO_LEFT
        danMuView.priority = DanMuModel.NORMAL
        danMuView.marginLeft = DimensionUtil.dpToPx(activity, 30)

        // 显示的文本内容
        danMuView.textSize = DimensionUtil.spToPx(
            activity,
            if (text == "2021年加油！！！！") 16 else if (text == "我要暴富！！！") 18 else 15
        ).toFloat()
        danMuView.textColor = ContextCompat.getColor(
            requireActivity(),
            if (text == "2021年加油！！！！") R.color.red1 else if (text == "我要暴富！！！") R.color.red else R.color.white
        )
        danMuView.textMarginLeft = DimensionUtil.dpToPx(activity, 5)

        danMuView.text = text

        // 弹幕文本背景
//        if (text == "2021年加油！！！！" || text == "我要暴富！！！")
//            danMuView.textBackground = ContextCompat.getDrawable(activity!!, R.drawable.shape_black_back)
        danMuView.textBackgroundMarginLeft = DimensionUtil.dpToPx(activity, 15)
        danMuView.textBackgroundPaddingTop = DimensionUtil.dpToPx(activity, 3)
        danMuView.textBackgroundPaddingBottom = DimensionUtil.dpToPx(activity, 3)
        danMuView.textBackgroundPaddingRight = DimensionUtil.dpToPx(activity, 15)
        return danMuView
    }

    /**
     * 点击
     */
    private val onClick = View.OnClickListener {
        when (it!!.id) {
            //论坛
            R.id.bbs -> if (MbTools.isLogin(requireActivity())) startActivity<BBSActivity>()
            //搜索
            R.id.search_bar -> startActivity<SearchTaskActivity>()
        }
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
                    if (adList != null) {
                        (banner.adapter as ViewBannerAdapter).apply {
                            val imgList = ArrayList<BannerMode>()
                            for (view in adList) {
                                if (view.boundData.adPatternType == AdPatternType.NATIVE_VIDEO) {
                                    view.setMediaListener(CommControl.mediaListener)
                                }
                                view.layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                )
                                imgList.add(BannerMode(2, view))
                            }
                            addData(imgList)
                        }
                    }

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
                }

                override fun onADClicked(p0: NativeExpressADView?) {
                }

                override fun onRenderSuccess(p0: NativeExpressADView?) {
                }

            }
        )
        mADManager!!.setMaxVideoDuration(10)
        mADManager!!.loadAD(2)
    }

    private fun setTab(typeList: ArrayList<TaskTypeMode>) {
        if (!isAdded) return
        typeList.add(0, TaskTypeMode(-1, -1, "推荐"))
        for (index in typeList) {
            fragments.add(TaskListFragment(if (index.taskTypeId != -1) index.typeEnum.toString() else ""))
            tab?.addTab(tab.newTab().setText(index.typeName))
        }
        childFragmentManager.beginTransaction()
            .add(R.id.container, fragments[0], "TaskListFragment0").commitNow()

        tab.apply {
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    if (childFragmentManager.findFragmentByTag("TaskListFragment" + tab!!.position) != null)
                        childFragmentManager.beginTransaction()
                            .hide(fragments[tab.position])
                            .show(fragments[tab.position]).commitNow()
                    else
                        childFragmentManager.beginTransaction()
                            .add(
                                R.id.container,
                                fragments[tab.position],
                                "TaskListFragment" + tab.position
                            ).commitNow()
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    if (childFragmentManager.findFragmentByTag("TaskListFragment" + tab!!.position)?.isAdded!!)
                        childFragmentManager.beginTransaction()
                            .hide(fragments[tab.position]).commitNow()
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        danmu?.hideAllDanMuView(true)
    }

    override fun onPause() {
        super.onPause()
        if (ad_video.isAnimating) ad_video.pauseAnimation()
    }

    override fun onResume() {
        super.onResume()
        ad_video.resumeAnimation()
    }
}