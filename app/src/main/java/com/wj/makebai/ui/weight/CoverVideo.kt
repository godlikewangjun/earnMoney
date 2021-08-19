package com.wj.makebai.ui.weight

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.abase.util.AbViewUtil
import com.bumptech.glide.Glide
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.utils.Debuger
import com.shuyu.gsyvideoplayer.utils.GSYVideoType
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView
import com.wj.eventbus.WjEventBus
import com.wj.ktutils.runOnUiThread
import com.wj.ktutils.showTip
import com.wj.makebai.R
import com.wj.makebai.ui.adapter.TvAdapter
import com.yanbo.lib_screen.callback.ControlCallback
import com.yanbo.lib_screen.entity.RemoteItem
import com.yanbo.lib_screen.event.DeviceEvent
import com.yanbo.lib_screen.manager.ClingManager
import com.yanbo.lib_screen.manager.ControlManager
import com.yanbo.lib_screen.manager.DeviceManager
import kotlinx.android.synthetic.main.dialog_tv_list.view.*
import kotlinx.android.synthetic.main.video_layout_cover.view.*
import org.fourthline.cling.support.model.item.Item

/**
 * 带封面
 * Created by guoshuyu on 2017/9/3.
 */

open class CoverVideo : StandardGSYVideoPlayer {

    lateinit var mCoverImage: ImageView

    lateinit var mCoverOriginUrl: String

    var mDefaultRes: Int = 0
    private var speed = 1//播放速度
    private val speedList = arrayListOf(0.5f, 1.0f, 1.5f, 2.0f, 3.0f)

    var localItem: Item? = null
    var remoteItem: RemoteItem? = null
    var isTv = false//是否在投屏

    var nextPlay: OnClickListener? = null//播放下一集

    var fullCoverVideo: CoverVideo? = null//全屏的播放器

    /******************* 下方重载方法，在播放开始不显示底部进度和按键，不需要可屏蔽  */

    private var byStartedClick: Boolean = false

    constructor(context: Context, fullFlag: Boolean?) : super(context, fullFlag!!)

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun getLayoutId(): Int {
        return R.layout.video_layout_cover
    }

    override fun init(context: Context) {
        super.init(context)
        mCoverImage = findViewById<View>(R.id.thumbImage) as ImageView

        if (mThumbImageViewLayout != null && (mCurrentState == -1 || mCurrentState == GSYVideoView.CURRENT_STATE_NORMAL || mCurrentState == GSYVideoView.CURRENT_STATE_ERROR)) {
            mThumbImageViewLayout.visibility = View.VISIBLE
        }
        voice_play.setOnClickListener {
            voice_play.isSelected = !voice_play.isSelected
            //静音
            GSYVideoManager.instance().isNeedMute = voice_play.isSelected
        }
        //适配全面屏
        setNeedAutoAdaptation(true)

        set_speed.visibility = View.GONE
        set_speed.setOnClickListener {
            if (speed == speedList.size - 1) speed = 0 else speed++
            val playSpeed = speedList[speed]
            set_speed.text = "$playSpeed 倍速"
            setSpeedPlaying(playSpeed, false)
        }
        tv.setOnClickListener {
            val dialog = AlertDialog.Builder(context)
                .setTitle("搜索的TV列表")
                .setNegativeButton(
                    "播放"
                ) { _, _ ->
                    tvPlay()
                }.create()
            val list = LayoutInflater.from(mContext).inflate(R.layout.dialog_tv_list, null)
            val adapter = TvAdapter()
            list.recyclerView.adapter = adapter
            list.recyclerView.layoutManager = LinearLayoutManager(context)
            dialog.setView(list)
            dialog.show()

            WjEventBus.getInit()
                .subscribe(DeviceManager.TV_ADD_DEVICE_TAG, DeviceEvent::class.java) {
                    context.runOnUiThread { adapter.refresh() }
                }
        }
        exit_tv.setOnLongClickListener(OnLongClickListener {
            stopCast()
            exit_tv.visibility = View.GONE
            isTv = false
            true
        })
        next_play.setOnClickListener { nextPlay?.onClick(null) }
    }

    /**
     * 电视播放
     */
    fun tvPlay() {
        //设置网络投屏的信息
        val itemurl1 =
            RemoteItem(
                titleTextView.text.toString(),
                (System.currentTimeMillis() / 1000).toString(),
                titleTextView.text.toString(),
                107362668,
                duration.toString(),
                "1280x720",
                mOriginUrl
            )

        //添加网络投屏的信息
        ClingManager.getInstance().remoteItem = itemurl1

        localItem = ClingManager.getInstance().localItem
        remoteItem = ClingManager.getInstance().remoteItem
        GSYVideoManager.onPause()
        play()
        isTv = true
        exit_tv.visibility = View.VISIBLE
    }

    /**
     * 播放开关
     */
    private fun play() {
        if (ControlManager.getInstance().state === ControlManager.CastState.STOPED) {
            if (localItem != null) {
                newPlayCastLocalContent()
            } else {
                newPlayCastRemoteContent()
            }
        } else if (ControlManager.getInstance().state === ControlManager.CastState.PAUSED) {
            playCast()
        } else if (ControlManager.getInstance().state === ControlManager.CastState.PLAYING) {
            pauseCast()
        } else {
            context.showTip("正在连接设备，稍后操作")
        }
    }

    private fun newPlayCastLocalContent() {
        ControlManager.getInstance().state = ControlManager.CastState.TRANSITIONING
        ControlManager.getInstance().newPlayCast(localItem, object : ControlCallback {
            override fun onSuccess() {
                ControlManager.getInstance().state = ControlManager.CastState.PLAYING
                ControlManager.getInstance().initScreenCastCallback()
            }

            override fun onError(code: Int, msg: String) {
                ControlManager.getInstance().state = ControlManager.CastState.STOPED
                context.showTip(String.format("New play cast local content failed %s", msg))
            }
        })
    }

    private fun newPlayCastRemoteContent() {
        ControlManager.getInstance().state = ControlManager.CastState.TRANSITIONING
        ControlManager.getInstance().newPlayCast(remoteItem, object : ControlCallback {
            override fun onSuccess() {
                ControlManager.getInstance().state = ControlManager.CastState.PLAYING
                ControlManager.getInstance().initScreenCastCallback()
            }

            override fun onError(code: Int, msg: String) {
                ControlManager.getInstance().state = ControlManager.CastState.STOPED
                context.showTip(String.format("New play cast remote content failed %s", msg))
            }
        })
    }

    private fun playCast() {
        ControlManager.getInstance().playCast(object : ControlCallback {
            override fun onSuccess() {
                ControlManager.getInstance().state = ControlManager.CastState.PLAYING
            }

            override fun onError(code: Int, msg: String) {
                context.showTip(String.format("Play cast failed %s", msg))
            }
        })
    }

    fun pauseCast() {
        ControlManager.getInstance().pauseCast(object : ControlCallback {
            override fun onSuccess() {
                ControlManager.getInstance().state = ControlManager.CastState.PAUSED
            }

            override fun onError(code: Int, msg: String) {
                context.showTip(String.format("Pause cast failed %s", msg))
            }
        })
    }

    fun stopCast() {
        ControlManager.getInstance().stopCast(object : ControlCallback {
            override fun onSuccess() {
                ControlManager.getInstance().state = ControlManager.CastState.STOPED
            }

            override fun onError(code: Int, msg: String) {
                context.showTip(String.format("Stop cast failed %s", msg))
            }
        })
    }

    fun loadCoverImage(url: String, res: Int) {
        mCoverOriginUrl = url
        mDefaultRes = res
        Glide.with(mCoverImage)
            .load(url)
            .into(mCoverImage)

    }

    //修改全屏图标
    override fun getShrinkImageRes(): Int {
        return R.drawable.ic_back_screen
    }

    override fun getEnlargeImageRes(): Int {
        return R.drawable.ic_full_screen
    }

    override fun startWindowFullscreen(
        context: Context,
        actionBar: Boolean,
        statusBar: Boolean
    ): GSYBaseVideoPlayer {
        val gsyBaseVideoPlayer = super.startWindowFullscreen(context, actionBar, statusBar)
        gsyBaseVideoPlayer.set_speed.visibility = View.VISIBLE
        val layoutParams = gsyBaseVideoPlayer.layout_top.layoutParams as RelativeLayout.LayoutParams
        layoutParams.topMargin = AbViewUtil.dp2px(context, 25f)
        fullCoverVideo = gsyBaseVideoPlayer as CoverVideo
//        fullCoverVideo!!.titleTextView.text=titleTextView.text
        fullCoverVideo!!.loadCoverImage(mCoverOriginUrl, mDefaultRes)
        fullCoverVideo!!.next_play.setOnClickListener { nextPlay?.onClick(null) }
        return this
    }

    override fun onBackFullscreen() {
        super.onBackFullscreen()
        fullCoverVideo = null
    }

    override fun showSmallVideo(
        size: Point,
        actionBar: Boolean,
        statusBar: Boolean
    ): GSYBaseVideoPlayer {
        //下面这里替换成你自己的强制转化
        val sampleCoverVideo = super.showSmallVideo(size, actionBar, statusBar) as CoverVideo
        sampleCoverVideo.mStartButton.visibility = View.GONE
        sampleCoverVideo.mStartButton = null
        return sampleCoverVideo
    }


    /******************* 下方两个重载方法，在播放开始前不屏蔽封面，不需要可屏蔽  */
    override fun onSurfaceUpdated(surface: Surface?) {
        super.onSurfaceUpdated(surface)
        if (mThumbImageViewLayout != null && mThumbImageViewLayout.visibility == View.VISIBLE) {
            mThumbImageViewLayout.visibility = View.INVISIBLE
        }
    }

    override fun setViewShowState(view: View?, visibility: Int) {
        if (view === mThumbImageViewLayout && visibility != View.VISIBLE) {
            return
        }
        super.setViewShowState(view, visibility)
    }

    override fun onSurfaceAvailable(surface: Surface) {
        super.onSurfaceAvailable(surface)
        if (GSYVideoType.getRenderType() != GSYVideoType.TEXTURE) {
            if (mThumbImageViewLayout != null && mThumbImageViewLayout.visibility == View.VISIBLE) {
                mThumbImageViewLayout.visibility = View.INVISIBLE
            }
        }
    }

    override fun onClickUiToggle() {
        if (mIfCurrentIsFullscreen && mLockCurScreen && mNeedLockFull) {
            setViewShowState(mLockScreen, View.VISIBLE)
            return
        }
        byStartedClick = true
        super.onClickUiToggle()

    }

    override fun setProgressAndTime(
        progress: Int,
        secProgress: Int,
        currentTime: Int,
        totalTime: Int,
        forceChange: Boolean
    ) {
        super.setProgressAndTime(progress, secProgress, currentTime, totalTime, forceChange)
    }

    public override fun changeUiToNormal() {
        super.changeUiToNormal()
        byStartedClick = false
    }

    override fun changeUiToPreparingShow() {
        super.changeUiToPreparingShow()
        setViewShowState(mBottomContainer, View.INVISIBLE)
        setViewShowState(mStartButton, View.INVISIBLE)
    }

    override fun changeUiToPlayingBufferingShow() {
        super.changeUiToPlayingBufferingShow()
        if (!byStartedClick) {
            setViewShowState(mBottomContainer, View.INVISIBLE)
            setViewShowState(mStartButton, View.INVISIBLE)
        }
    }

    override fun changeUiToPlayingShow() {
        super.changeUiToPlayingShow()
        if (!byStartedClick) {
            setViewShowState(mBottomContainer, View.INVISIBLE)
            setViewShowState(mStartButton, View.INVISIBLE)
        }
    }

    override fun startAfterPrepared() {
        super.startAfterPrepared()
        setViewShowState(mBottomContainer, View.INVISIBLE)
        setViewShowState(mStartButton, View.INVISIBLE)
        setViewShowState(mBottomProgressBar, View.VISIBLE)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        byStartedClick = true
        super.onStartTrackingTouch(seekBar)
    }
}
