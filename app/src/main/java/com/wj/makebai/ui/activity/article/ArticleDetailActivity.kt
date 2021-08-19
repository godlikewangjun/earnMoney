package com.wj.makebai.ui.activity.article

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abase.util.GsonUtil
import com.abase.util.Tools
import com.qq.e.ads.cfg.VideoOption
import com.qq.e.ads.nativ.ADSize
import com.qq.e.ads.nativ.NativeExpressAD
import com.qq.e.ads.nativ.NativeExpressADView
import com.qq.e.ads.nativ.NativeExpressMediaListener
import com.qq.e.comm.constants.AdPatternType
import com.qq.e.comm.util.AdError
import com.sum.slike.SuperLikeLayout
import com.umeng.socialize.UMShareListener
import com.umeng.socialize.bean.SHARE_MEDIA
import com.wj.commonlib.data.mode.ArticleMode
import com.wj.commonlib.http.HttpManager
import com.wj.commonlib.http.Urls
import com.wj.commonlib.statices.Statics
import com.wj.commonlib.ui.ViewControl
import com.wj.commonlib.utils.ShareManager
import com.wj.ktutils.isNull
import com.wj.makebai.R
import com.wj.makebai.data.mode.ArticleDetailTypeMode
import com.wj.makebai.data.db.LikeDb
import com.wj.makebai.data.db.TypesEnum
import com.wj.makebai.statice.StaticData
import com.wj.makebai.ui.activity.base.MakeActivity
import com.wj.makebai.ui.adapter.ArticleDetailAdapter
import com.wj.makebai.ui.weight.BitmapProviderFactory
import com.wj.makebai.ui.weight.NoData
import com.wj.im.utils.GDTAdTools
import com.yalantis.contextmenu.lib.ContextMenuDialogFragment
import com.yalantis.contextmenu.lib.MenuObject
import com.yalantis.contextmenu.lib.MenuParams
import kotlinx.android.synthetic.main.activity_article_layout.*
import kotlinx.android.synthetic.main.arttaskfinsh_dailog_layout.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.abs

/**
 * 文章的详情页
 * @author dchain
 * @version 1.0
 * @date 2019/8/20
 */
class ArticleDetailActivity : MakeActivity() {
    private var mAdapter: ArticleDetailAdapter? = null
    private var key = ""
    private var page = 0
    private var startId = -1
    private var isLoad = false
    private var mode: ArticleMode? = null
    private lateinit var contextMenuDialogFragment: ContextMenuDialogFragment
    //计时
    private var timer: Timer? = null//计时器
    private var time = 0L//倒计时时间
    private var maxTime = 10 * 10 //倒计时时间默认40
    private var isTask = true
    //获取原生广告
    private var mADManager: NativeExpressAD? = null
    private var mAdViewList: List<NativeExpressADView>? = null
    private val mAdViewPositionMap = HashMap<NativeExpressADView, Int>()

    override fun bindLayout(): Int {
        return R.layout.activity_article_layout
    }

    override fun initData() {
        other_icon.setImageResource(R.drawable.ic_artmore)
        other_down.setOnClickListener {
            initMenuFragment()

            showContextMenuDialogFragment()
        }
        if (intent.hasExtra("data")) {
            mode = intent.getSerializableExtra("data") as ArticleMode
            setData()
        } else if (intent.hasExtra("id")) {
            HttpManager.articleDetail(intent.getStringExtra("id"), {
                loadAD(true)

                mode = it
                setData()
            }, fun(code, _, _) {
                if (code == 503) {
                    setState(NoData.DataState.REFRESH, false)
                }
                if (code == -1) {
                    setState(NoData.DataState.NO_NETWORK, false)
                }
            })
        }
        progress_horizontal.max = maxTime
    }

    /**
     * 拉取广告
     */
    private fun loadAD(isDetail: Boolean) {

        val adSize = ADSize(
            ADSize.FULL_WIDTH,
            ADSize.AUTO_HEIGHT
        ) // 消息流中用AUTO_HEIGHT

        mADManager = NativeExpressAD(
            activity,
            adSize,
            if (!isDetail) StaticData.JDT_ART_AD else StaticData.JDT_ART_DETAIL_AD,
            object : NativeExpressAD.NativeExpressADListener {
                override fun onADCloseOverlay(p0: NativeExpressADView?) {
                }

                override fun onADLoaded(adList: MutableList<NativeExpressADView>?) {
                    if (!isDetail) {
                        mAdViewList = adList
                        for (i in mAdViewList!!.indices) {
                            var position: Int =
                                mAdapter!!.mList.size - 15 + 3 * i
                            if (mAdViewPositionMap.isEmpty()) position = 0
                            if (position < mAdapter!!.mList.size) {
                                val view = mAdViewList!![i]
                                if (view.boundData.adPatternType == AdPatternType.NATIVE_VIDEO) {
                                    view.setMediaListener(mediaListener)
                                }
                                view.render()
                                mAdViewPositionMap[view] = position // 把每个广告在列表中位置记录下来
                                if (mAdapter != null && mAdapter!!.mList != null) {
//                                mAdapter!!.mList.add(position,ArtTypeMode(mAdViewList!![i],ArtEmu.AD))
                                    if (position >= 0 && position < mAdapter!!.mList.size && mAdViewList!![i] != null) {
                                        mAdapter!!.mList.add(
                                            position,
                                            ArticleDetailTypeMode(
                                                ArticleDetailAdapter.ArticleDetailTypes.AD,
                                                mAdViewList!![i]
                                            )
                                        )
                                    }
                                }
                            }
                            mAdapter!!.notifyDataSetChanged()
                        }
                    } else {
                        val view = adList!![0]
                        if (view.boundData.adPatternType == AdPatternType.NATIVE_VIDEO) {
                            view.setMediaListener(mediaListener)
                        }
                        express_ad_container.addView(view)
                        view.render() // 调用render方法后sdk才会开始展示广告
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

        mADManager!!.loadAD(5)
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
     * 设置初始化的数据
     */
    private fun setData() {
        //设置点赞的效果
        super_like_layout.provider = BitmapProviderFactory.getHDProvider(activity!!)

        mAdapter = ArticleDetailAdapter()
        mAdapter!!.isAd = intent.getBooleanExtra("ad", true)
        recyclerView!!.adapter = mAdapter

        var typeMode =
            ArticleDetailTypeMode(ArticleDetailAdapter.ArticleDetailTypes.TITLE, mode!!.articlename)
        mAdapter!!.mList.addSort(typeMode)

        typeMode = ArticleDetailTypeMode(ArticleDetailAdapter.ArticleDetailTypes.CONTENT, mode!!)
        mAdapter!!.mList.addSort(typeMode)

        typeMode = ArticleDetailTypeMode(ArticleDetailAdapter.ArticleDetailTypes.PUSH_TITLE, "")
        mAdapter!!.mList.addSort(typeMode)

        ViewControl.loadMore(activity, recyclerView!!, mAdapter!!) {
            if (isLoad) return@loadMore
            isLoad = true
            page++
            startId = (mAdapter!!.mList[mAdapter!!.mList.size - 1].mode as ArticleMode).articleid
            loadData()
        }

        startId = mode!!.articleid
        key = mode!!.key
        loadData()
        if (Statics.userMode != null && intent.getBooleanExtra("ad", true)) task()
    }

    override fun refreshData() {
        loadData()
    }

    /**
     * 任务计时
     */
    private fun task() {
        if (timer != null) return
        task_progress.visibility = View.VISIBLE
        timer = Timer()
        var state = -1
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                state = if (abs(dy) > 50) 0 else -1
            }
        })
        timer!!.schedule(object : TimerTask() {
            override fun run() {
                if (!isTask) timer?.cancel()
                if (state != -1) {
                    time++
                    runOnUiThread {
                        progress_horizontal.progress = time.toInt()
                        if (time >= maxTime) {
                            isTask = false
                            timer?.cancel()

                            HttpManager.artTask(mode!!.articleid, mode!!.articlename, {
                                showSuccess(it.points)
                            }) {
                                time = 0
                            }
                        }
                    }
                    state = -1
                }
            }
        }, 1000, 100)
    }

    /**
     * 弹出阅读任务完成
     */
    @SuppressLint("SetTextI18n")
    private fun showSuccess(points: Int) {
        val view = LayoutInflater.from(activity).inflate(R.layout.arttaskfinsh_dailog_layout, null)
        val taskDialog = ViewControl.customAlertDialog(activity, view, 250f)
        view.imgView.progress = 0.2f
        view.imgView.playAnimation()
        view.imgView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                val alpha = ObjectAnimator.ofFloat(view.name, "alpha", 0.2f, 1f).setDuration(100)
                alpha.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator?) {
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        task_progress.visibility=View.GONE
                        contentView.postDelayed({taskDialog.cancel()},1000)
                    }

                    override fun onAnimationCancel(animation: Animator?) {
                    }

                    override fun onAnimationStart(animation: Animator?) {
                    }

                })
                alpha.start()
                view.name.text = String.format(getString(R.string.add_points), points)
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }

        })
    }

    /**
     * 初始化分享
     */
    private fun initMenuFragment() {
        val menuParams = MenuParams(
            actionBarSize = resources.getDimension(R.dimen.tab_height).toInt(),
            menuObjects = getMenuObjects(),
            isClosableOutside = false
        ).apply {
            isClosableOutside = true
            isFitsSystemWindow = true
        }
        contextMenuDialogFragment = ContextMenuDialogFragment.newInstance(menuParams).apply {
            menuItemClickListener = { _, position ->
                val listener = object : UMShareListener {
                    override fun onResult(p0: SHARE_MEDIA?) {
                    }

                    override fun onCancel(p0: SHARE_MEDIA?) {
                    }

                    override fun onError(p0: SHARE_MEDIA?, p1: Throwable?) {
                    }

                    override fun onStart(p0: SHARE_MEDIA?) {
                    }

                }
                val link = Urls.share_art + mode!!.articleid
                var type: SHARE_MEDIA? = null
                when (position) {
                    2 -> {//微信好友
                        type = SHARE_MEDIA.WEIXIN
                    }
                    3 -> {//微信朋友圈
                        type = SHARE_MEDIA.WEIXIN_CIRCLE
                    }
                    4 -> {//qq
                        type = SHARE_MEDIA.QQ
                    }
                    5 -> {//qq空间
                        type = SHARE_MEDIA.QZONE
                    }
                }
                if (type != null) {
                    var images = mode!!.imageurls.replace("\\\\", "").replace("\\", "")
                    if (images.startsWith("\"")) images = images.substring(1, images.length - 1)
                    ShareManager.shareManager!!.share(
                        activity!!,
                        type,
                        link,
                        mode!!.articlename,
                        if (!mode!!.imageurls.isNull()) {
                            GsonUtil.Gson2ArryList(
                                images,
                                Array<String>::class.java
                            )[0]
                        } else null,
                        mode!!.digest,
                        listener
                    )
                } else {
                    HttpManager.likeOrRead(mode!!.articleid, 3) {
                        val x = Tools.getScreenWH(activity)[0] / 2
                        val y = Tools.getScreenWH(activity)[1] / 2
                        contentView.findViewById<SuperLikeLayout>(R.id.super_like_layout)
                            .launch(x, y)
                        (mAdapter!!.mList[1].mode as ArticleMode).like_count += 1
                        mAdapter!!.notifyDataSetChanged()
                        //插入数据库
                        LikeDb.insert(
                            activity!!,
                            mode!!.articlename,
                            TypesEnum.ARTICLE,
                            GsonUtil.gson2String(mode)
                        )
                    }
                }
            }
            menuItemLongClickListener = { view, position ->
            }
        }
    }

    private fun getMenuObjects() = mutableListOf<MenuObject>().apply {
        val close = MenuObject().apply { setResourceValue(R.drawable.ic_close) }
        val like =
            MenuObject(getString(R.string.like_store)).apply { setResourceValue(R.drawable.ic_like) }
        val send = MenuObject(getString(R.string.wx)).apply { setResourceValue(R.drawable.ic_wx) }
        val addFriend =
            MenuObject(getString(R.string.wx_circle)).apply { setResourceValue(R.drawable.ic_pyq) }
        val qq = MenuObject(getString(R.string.qq)).apply { setResourceValue(R.drawable.ic_qq) }
        val qqzone =
            MenuObject(getString(R.string.qq_zone)).apply { setResourceValue(R.drawable.ic_qqzone) }

        add(close)
        add(like)
        add(send)
        add(addFriend)
        add(qq)
        add(qqzone)
    }

    private fun showContextMenuDialogFragment() {
        if (supportFragmentManager.findFragmentByTag(ContextMenuDialogFragment.TAG) == null) {
            contextMenuDialogFragment.show(supportFragmentManager, ContextMenuDialogFragment.TAG)
        }
    }

    /**
     * 加载数据
     */
    private fun loadData() {
        HttpManager.allList<ArticleMode>(Urls.articleList, page, startId, key, null, {
            if (it.list.isNotEmpty()) {//解决多重泛型问题，使用这个接口
                GlobalScope.launch(Dispatchers.IO) {
                    it.list = GsonUtil.Gson2ArryList(
                        GsonUtil.gson2String(it.list),
                        Array<ArticleMode>::class.java
                    ).toList() as ArrayList<ArticleMode>
                    runOnUiThread {
                        for (index in it.list) {
                            val typeMode = ArticleDetailTypeMode(
                                ArticleDetailAdapter.ArticleDetailTypes.PUSHLIST,
                                index
                            )
                            mAdapter!!.mList.addSort(typeMode)
                        }
//                        mAdapter!!.notifyDataSetChanged()
                        loadAD(false)
                    }
                }
            }
            mAdapter!!.isFinsh = it.isEnd
            isLoad = false
        }, fun(code, _, _) {
            if (code == 503) {
                setState(NoData.DataState.REFRESH, false)
            }
            if (code == -1) {
                setState(NoData.DataState.NO_NETWORK, false)
            }
        })
    }

    override fun onDestroy() {
        (contentView!! as ViewGroup).removeAllViews()
        timer?.cancel()
        contentView.handler.removeCallbacksAndMessages(null)
        super.onDestroy()
        // 使用完了每一个NativeExpressADView之后都要释放掉资源。
        if (mAdViewList != null) {
            for (view in mAdViewList!!) {
                view.destroy()
            }
        }
    }

    /**
     * 按照枚举排序
     */
    private fun java.util.ArrayList<ArticleDetailTypeMode>.addSort(mode: ArticleDetailTypeMode) {
//        val sort =
//            Comparator<ArticleDetailTypeMode> { o1, o2 -> o1!!.type!!.ordinal.compareTo(o2.type!!.ordinal) }
        this.add(mode)
//        this.sortWith(sort)
    }
}