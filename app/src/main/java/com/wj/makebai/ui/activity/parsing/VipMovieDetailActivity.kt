package com.wj.makebai.ui.activity.parsing

import android.annotation.SuppressLint
import android.graphics.PixelFormat
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.abase.util.GsonUtil
import com.abase.util.Tools
import com.gyf.immersionbar.ImmersionBar
import com.qq.e.ads.nativ.*
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
import com.wj.commonlib.data.mode.VipMovieItem
import com.wj.commonlib.data.mode.VipMovieItemMode
import com.wj.commonlib.data.mode.VipParsMovieMode
import com.wj.ktutils.WjSP
import com.wj.makebai.R
import com.wj.makebai.data.db.AppDatabase
import com.wj.makebai.data.db.TypesEnum
import com.wj.makebai.data.mode.VipMovieDetailTypeMode
import com.wj.makebai.data.mode.db.ReadHistoryMode
import com.wj.makebai.statice.XmlCodes
import com.wj.makebai.ui.activity.base.MakeActivity
import com.wj.makebai.ui.adapter.VipMoviesDetailAdapter
import com.wj.ui.interfaces.RecyerViewItemListener
import kotlinx.android.synthetic.main.activity_moviedetail.recyclerView
import kotlinx.android.synthetic.main.activity_moviedetail.video_play
import kotlinx.android.synthetic.main.activity_vipplaydetail.*
import kotlinx.android.synthetic.main.activity_watchcomic.*
import kotlinx.android.synthetic.main.video_layout_cover.view.*
import kotlinx.coroutines.*
import java.util.*


/**
 * 影视详情
 * @author dchain
 * @version 1.0
 * @date 2019/9/11
 */
class VipMovieDetailActivity : MakeActivity(), View.OnClickListener {

    private var gsyVideoOptionBuilder: GSYVideoOptionBuilder? = null
    private var moviesDetailAdapter = VipMoviesDetailAdapter()
    private lateinit var detailMode: VipMovieItem
    private var playUrl = ""
    private var choose = 0
    private lateinit var vipMovieDetailMode: VipParsMovieMode

    override fun bindLayout(): Int {
        return R.layout.activity_vipplaydetail
    }

    override fun before() {
        super.before()
        window.setFormat(PixelFormat.TRANSLUCENT)
    }

    override fun initData() {
        setThemeColor(R.color.black)
        ImmersionBar.with(this).statusBarDarkFont(false).init()
        vipMovieDetailMode = GsonUtil.gson2Object(
            WjSP.getInstance()
                .getValues(XmlCodes.PAGEDATA, ""), VipParsMovieMode::class.java
        )
        detailMode = GsonUtil.gson2Object(intent.getStringExtra("detail"), VipMovieItem::class.java)
        gsyVideoOptionBuilder = GSYVideoOptionBuilder()

        frame_play.layoutParams.height = Tools.getScreenWH(activity)[0] / 16 * 9


        title.visibility = View.GONE
        title.postDelayed({
            bindData()
        }, 200)

    }


    /**
     * 绑定数据
     */
    private fun bindData() {
        //数据添加
        var mode = VipMovieDetailTypeMode()
        mode.type = VipMoviesDetailAdapter.VipMovieDetailTypes.DETAIL
        mode.mode = detailMode

        moviesDetailAdapter.list.add(mode)
        //集数
        mode = VipMovieDetailTypeMode()
        mode.type = VipMoviesDetailAdapter.VipMovieDetailTypes.PLAY_LIST
        mode.mode = vipMovieDetailMode.data.data

        moviesDetailAdapter.list.add(mode)


        AppDatabase.db.readHistoryDao().get(detailMode.videoId, TypesEnum.VIPVIDEO.ordinal).observe(
            this
        ){
            if (it!=null)choose=it.value.toInt()

            bindVideo(choose)
            moviesDetailAdapter.apply {
                index=choose
                playClick = object : RecyerViewItemListener {
                    override fun click(view: View, position: Int) {
                        GSYVideoManager.onPause()
                        GSYVideoManager.releaseAllVideos()
                        choose = position
                        loadAd()
                    }
                }
            }
            recyclerView.adapter = moviesDetailAdapter
            recyclerView.layoutManager = LinearLayoutManager(activity)
        }
        loadAd()
    }
    /**
     * 播放视频
     */
    @SuppressLint("SetTextI18n")
    private fun playVideo(mode: VipMovieItemMode) {
        if (video_play.fullCoverVideo != null) {
            video_play.fullCoverVideo!!.setUp(playUrl, true, mode.title)
            video_play.fullCoverVideo!!.start.performClick()
        } else {
            video_play.setUp(playUrl, true, mode.title)
            video_play.startButton.performClick()
        }

        if (video_play.fullCoverVideo != null) {
            video_play.fullCoverVideo!!.titleTextView.text =
                detailMode.title + "——" + mode.title
        } else {
            video_play.titleTextView.text =
                detailMode.title + "——" + mode.title
        }
    }
    override fun refreshData() {
        bindData()
    }


    /**
     * 视频绑定
     */
    private fun bindVideo(position: Int) {
        choose = position
        playUrl=vipMovieDetailMode.data.data[choose].chapterUrl

        //增加封面
        video_play.loadCoverImage(vipMovieDetailMode.data.data[choose].chapterUrl, R.color.gray)
        gsyVideoOptionBuilder!!
            .setUrl(playUrl)
            .setVideoTitle(detailMode.title)
            .setCacheWithPlay(false)
            .setRotateViewAuto(true)
            .setLockLand(true)
            .setPlayTag(playUrl)
            .setIsTouchWigetFull(true)
            .setIsTouchWiget(true)
            .setCacheWithPlay(true)
            .setShowFullAnimation(true)
            .setNeedLockFull(true)
            .setPlayPosition(choose).setVideoAllCallBack(object : GSYSampleCallBack() {
                override fun onStartPrepared(url: String?, vararg objects: Any?) {
                    video_play.postDelayed({ video_play.back.visibility = View.VISIBLE }, 2000)
                }
            }).build(video_play)

        video_play.speed = 0f
        //设置返回键
        video_play.back.setOnClickListener { onBackPressed() }

        //设置全屏按键功能
        video_play.fullscreenButton
            .setOnClickListener { resolveFullBtn(video_play) }

    }


    override fun onClick(v: View?) {
        playUrl=vipMovieDetailMode.data.data[choose].chapterUrl
        GSYVideoManager.onPause()
        video_play.setUp(playUrl, true, vipMovieDetailMode.data.data[choose].title)
        video_play.start.performClick()
    }

    /**
     * 加载激励视频
     */
    private fun loadAd() {
        ad_view_load.loadAd(lifecycle){
            val playMode=vipMovieDetailMode.data.data[choose]
            playUrl = playMode.chapterUrl
            playVideo(playMode)
        }
    }

    /**
     * 全屏幕按键处理
     */
    private fun resolveFullBtn(standardGSYVideoPlayer: StandardGSYVideoPlayer) {
        standardGSYVideoPlayer.startWindowFullscreen(activity, false, false)
    }

    override fun onPause() {
        super.onPause()
        GSYVideoManager.onPause()
    }


    override fun onResume() {
        super.onResume()
        GSYVideoManager.onResume()
    }

    override fun onStop() {
        super.onStop()
        GSYVideoManager.onPause()
    }

    override fun onDestroy() {
        //插入
        if(recyclerView.adapter!=null){
            GlobalScope.launch(Dispatchers.IO){
                AppDatabase.db.readHistoryDao().delete(
                    detailMode.videoId,
                    TypesEnum.VIPVIDEO.ordinal
                )
                AppDatabase.db.readHistoryDao().insert(
                    ReadHistoryMode(
                        0,
                        TypesEnum.VIPVIDEO.ordinal, detailMode.videoId, choose.toString()
                    )
                )
            }
        }
        super.onDestroy()
        //存入播放时间
        GSYVideoManager.onPause()
        GSYVideoManager.releaseAllVideos()
    }

    override fun onBackPressed() {
        if (GSYVideoManager.backFromWindowFull(this)) {
            return
        }
        super.onBackPressed()
    }
}