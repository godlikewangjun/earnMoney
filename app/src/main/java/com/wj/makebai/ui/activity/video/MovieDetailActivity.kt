package com.wj.makebai.ui.activity.video

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.abase.okhttp.OhHttpClient
import com.abase.util.AbDoubleTool
import com.abase.util.Tools
import com.anbetter.danmuku.model.DanMuModel
import com.anbetter.danmuku.model.utils.DimensionUtil
import com.gyf.immersionbar.ImmersionBar
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
import com.wj.commonlib.statices.XmlConfigs
import com.wj.commonlib.ui.weight.CustomLinearLayoutManager
import com.wj.commonlib.utils.setOnClick
import com.wj.ktutils.WjSP
import com.wj.ktutils.isNull
import com.wj.ktutils.showTip
import com.wj.ktutils.startActivity
import com.wj.makebai.R
import com.wj.makebai.data.db.AppDatabase
import com.wj.makebai.data.db.TypesEnum
import com.wj.makebai.data.mode.MovieDetailTypeMode
import com.wj.makebai.data.mode.MoviesMode
import com.wj.makebai.data.mode.db.ReadHistoryMode
import com.wj.makebai.ui.activity.base.MakeActivity
import com.wj.makebai.ui.activity.comm.WebActivity
import com.wj.makebai.ui.adapter.MoviesDetailAdapter
import com.wj.makebai.ui.weight.NoData
import com.wj.ui.interfaces.RecyerViewItemListener
import com.xiaomi.push.it
import kotlinx.android.synthetic.main.activity_moviedetail.*
import kotlinx.android.synthetic.main.activity_moviedetail.ad_view_load
import kotlinx.android.synthetic.main.activity_moviedetail.recyclerView
import kotlinx.android.synthetic.main.activity_moviedetail.video_play
import kotlinx.android.synthetic.main.activity_vipplaydetail.*
import kotlinx.android.synthetic.main.video_layout_cover.view.*
import kotlinx.coroutines.*
import okhttp3.Request
import org.json.JSONObject
import org.jsoup.Jsoup


/**
 * 影视详情
 * @author dchain
 * @version 1.0
 * @date 2019/9/11
 */
class MovieDetailActivity : MakeActivity() {
    private var gsyVideoOptionBuilder: GSYVideoOptionBuilder? = null
    private var moviesDetailAdapter = MoviesDetailAdapter()
    val TAG = MovieDetailActivity::class.java.simpleName
    private var playUrl = ""
    private lateinit var mode: MoviesMode
    private val playUrls = ArrayList<String>()
    private val playWebUrls = ArrayList<String>()
    private val playM3u8Urls = ArrayList<String>()
    private val playNames = ArrayList<String>()
    private var playIndex = 0//播放的第几集
    private var defaultType = 0 // 0是优先取迅雷 1m3u8


    override fun bindLayout(): Int {
        return R.layout.activity_moviedetail
    }

    override fun initData() {
        ImmersionBar.with(this).statusBarDarkFont(false).init()
        setThemeColor(R.color.black)
        title_content.text = intent.getStringExtra("title")
        mode = intent.getParcelableExtra("data")
        gsyVideoOptionBuilder = GSYVideoOptionBuilder()

        title.visibility = View.GONE

        postLayout.setOnClickListener {
            startActivity<WebActivity>("url" to playWebUrls[playIndex])
        }
        play_content.layoutParams.height =
            AbDoubleTool.mul(Tools.getScreenWH(this)[0].toDouble(), 0.5625).toInt()
        setState(NoData.DataState.LOADING, false)
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val builder: Request.Builder =
                    Request.Builder().url(mode.detail).tag(mode.detail) // 设置tag
                builder.get()
                val response = OhHttpClient.getInit().client.newCall(builder.build()).execute()
                //获取详情
                val detailUrl = Jsoup.parse(response.body!!.string(), mode.detail)
                var image = ""

                val imageElements =
                    detailUrl.select("body > div.warp > div:nth-child(1) > div > div > div.vodImg > img")
                if (imageElements.size > 0) image = imageElements[0].attr(
                    "src"
                )
                var actors = ""
                val actorsElements =
                    detailUrl.select("body > div.warp > div:nth-child(1) > div > div > div.vodInfo > div.vodinfobox > ul > li:nth-child(3) > span")
                if (actorsElements.size > 0) actors = actorsElements[0].text()
                var director = ""
                val directorElements =
                    detailUrl.select("body > div.warp > div:nth-child(1) > div > div > div.vodInfo > div.vodinfobox > ul > li:nth-child(2) > span")
                if (directorElements.size > 0) director = directorElements[0].text()
                val name =
                    detailUrl.select("body > div.warp > div:nth-child(1) > div > div > div.vodInfo > div.vodh > h2")[0].text()
                val state =
                    detailUrl.select("body > div.warp > div:nth-child(1) > div > div > div.vodInfo > div.vodinfobox > ul > li:nth-child(7) > span")[0].text()
                val pushDate =
                    detailUrl.select("body > div.warp > div:nth-child(1) > div > div > div.vodInfo > div.vodinfobox > ul > li:nth-child(7) > span")[0].text()
                val introduction =
                    detailUrl.select("body > div.warp > div:nth-child(2) > div.vodplayinfo")[0].text()
                val language =
                    detailUrl.select("body > div.warp > div:nth-child(1) > div > div > div.vodInfo > div.vodinfobox > ul > li:nth-child(6) > span")[0].text()
                val area =
                    detailUrl.select("body > div.warp > div:nth-child(1) > div > div > div.vodInfo > div.vodinfobox > ul > li:nth-child(5) > span")[0].text()
                val type =
                    detailUrl.select("body > div.warp > div:nth-child(1) > div > div > div.vodInfo > div.vodinfobox > ul > li:nth-child(4) > span")[0].text()

                //酷云 默认m3u8 有迅雷取迅雷地址
                val list =
                    detailUrl.select("#play_1")[0].children()[1].children()
                val m3u8 =
                    detailUrl.select("#play_2")[0].children()[1].children()
                val tBody = if (list.size > 0) {
                    defaultType = 0
                    list
                } else {
                    defaultType = 1
                    m3u8
                }

                for (index in 0 until tBody.size) {
                    val all = tBody[index].text().split("$")
                    playUrls.add(all[1])
                    playNames.add(all[0])
                }
                if (defaultType == 0) for (index in 0 until m3u8.size) {
                    val all = m3u8[index].text().split("$")
                    playM3u8Urls.add(all[1])
                }else playM3u8Urls.addAll(playUrls)
                //加载网页播放地址
                val kunyun =
                    detailUrl.select("#play_2")[0].children()[1].children()
                for (index in 0 until kunyun.size) {
                    val all = kunyun[index].text().split("$")
                    playWebUrls.add(all[1])
                }
                video_play.next_play.visibility = if (playUrls.size > 1) View.VISIBLE else View.GONE
                mode = MoviesMode(
                    actors,
                    area,
                    director,
                    image,
                    introduction,
                    "",
                    language,
                    pushDate,
                    state,
                    name,
                    type, mode.detail
                )
                launch(Dispatchers.Main) {
                    bindData()
                    setState(NoData.DataState.GONE, false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread { showTip("解析错误") }
                finishTo()
            }
        }
    }

    /**
     * 绑定数据
     */
    @SuppressLint("SetTextI18n")
    private fun bindData() {
        if (!mode.actors.isNull()) actor.text = String.format(
            getString(R.string.movie_performer),
            mode.actors
        ) else {
            actor.visibility = View.GONE
        }
        if (!mode.introduction.isNull()) desc.text = String.format(
            getString(R.string.movie_introduce),
            mode.introduction
        )
        more.text = "${mode.area}  ${mode.language}  ${mode.pushDate} ${mode.director}"
        if (defaultType == 0) {
            playType.isVisible = true
            playType.text = "m3u8(切换源)"
        } else playType.isVisible = false
        playType.setOnClick {
            if (defaultType == 0) {
                defaultType = 1
                playType.text = "迅雷(切换源)"
            } else {
                defaultType = 0
                playType.text = "m3u8(切换源)"
            }
            playVideo()
        }

        desc.setOnClickListener {
            desc.isSelected = !desc.isSelected
            if (desc.isSelected) {
                desc.maxLines = 100
            } else {
                desc.maxLines = 2
            }
        }
        //数据添加
        val payList = MovieDetailTypeMode()
        payList.type = MoviesDetailAdapter.MovieDetailTypes.PLAY_LIST
        payList.mode = playNames

        moviesDetailAdapter.list.add(payList)

        moviesDetailAdapter.notifyItemInserted(0)

        recyclerView.layoutManager = CustomLinearLayoutManager(activity)
        moviesDetailAdapter.isFinish = true

        AppDatabase.db.readHistoryDao()
            .get(this@MovieDetailActivity.mode.detail, TypesEnum.VIDEO.ordinal).observe(
                this
            ) {
            if (it != null) playIndex = it.value.toInt()

            moviesDetailAdapter.apply {
                playClick = object : RecyerViewItemListener {
                    override fun click(view: View, position: Int) {
                        GSYVideoManager.onPause()
                        GSYVideoManager.releaseAllVideos()
                        playIndex = position
                        loadAd()
                    }
                }
                index = playIndex
                recyclerView.adapter = this
            }
            video_play.nextPlay = View.OnClickListener { playNext() }

            bindVideo(playIndex)
        }

    }

    override fun refreshData() {
    }


    /**
     * 播放视频
     */
    @SuppressLint("SetTextI18n")
    private fun playVideo() {
        if (playUrls.isEmpty()) return
       setPlayUul()
        if (video_play.fullCoverVideo != null) {
            video_play.fullCoverVideo!!.setUp(playUrl, true, mode.title)
            video_play.fullCoverVideo!!.start.performClick()
        } else {
            video_play.setUp(playUrl, true, mode.title)
            video_play.startButton.performClick()
        }
        tv_open.isVisible = true
        setVideoTitle()
    }

    /**
     * 加载激励视频
     */
    private fun loadAd() {
        tv_open.isVisible = false
        ad_view_load.loadAd(lifecycle) {
            playVideo()
        }
    }

    private fun getModel(text: String): DanMuModel {
        val danMuView = DanMuModel()
        danMuView.displayType = DanMuModel.RIGHT_TO_LEFT
        danMuView.priority = DanMuModel.NORMAL
        danMuView.marginLeft = DimensionUtil.dpToPx(activity, 30)

        // 显示的文本内容
        danMuView.textSize = DimensionUtil.spToPx(activity, 16).toFloat()
        danMuView.textColor = ContextCompat.getColor(activity, R.color.white)
        danMuView.textMarginLeft = DimensionUtil.dpToPx(activity, 5)

        danMuView.text = text

        // 弹幕文本背景
//        danMuView.textBackground = ContextCompat.getDrawable(activity, R.drawable.shape_gray)
        danMuView.textBackgroundMarginLeft = DimensionUtil.dpToPx(activity, 15)
        danMuView.textBackgroundPaddingTop = DimensionUtil.dpToPx(activity, 3)
        danMuView.textBackgroundPaddingBottom = DimensionUtil.dpToPx(activity, 3)
        danMuView.textBackgroundPaddingRight = DimensionUtil.dpToPx(activity, 15)
        return danMuView
    }

    /**
     * 设置播放地址
     */
    private fun setPlayUul(){
        playUrl = if (defaultType == 0) playUrls[playIndex] else playM3u8Urls[playIndex]
        println(playUrl+" ------------------ ")
    }
    /**
     * 视频绑定
     */
    @SuppressLint("SetTextI18n")
    private fun bindVideo(position: Int) {
        if (playUrls.isEmpty()) return
        setPlayUul()
        //增加封面
        video_play.loadCoverImage(mode.image, R.color.gray)
        gsyVideoOptionBuilder!!
            .setIsTouchWiget(false)
            .setVideoTitle(mode.title)
            .setCacheWithPlay(true)
            .setRotateViewAuto(true)
            .setLockLand(true)
            .setPlayTag(TAG)
            .setIsTouchWigetFull(true)
            .setIsTouchWiget(true)
            .setShowFullAnimation(true)
            .setNeedLockFull(true)
            .setPlayPosition(position).setVideoAllCallBack(object : GSYSampleCallBack() {
                override fun onPrepared(url: String?, vararg objects: Any?) {
                    if (video_play.isTv) video_play.tvPlay()
                }

                override fun onAutoComplete(url: String?, vararg objects: Any?) {
                    playNext()
                }

                override fun onEnterFullscreen(url: String?, vararg objects: Any?) {
                    setVideoTitle()
                }

                override fun onQuitFullscreen(url: String?, vararg objects: Any?) {
                    setVideoTitle()
                }
            }).build(video_play)
        setVideoTitle()
        //增加title
        video_play.speed = 0f

        //设置返回键
        val cache = WjSP.getInstance().getValues(XmlConfigs.VIDEO_POSITION, "")
        if (!cache.isNull()) {
            val positionJson = JSONObject(cache)
            if (positionJson.get("name") == playUrl) {
                video_play.playPosition = positionJson.getInt("position")
            }
        }

        video_play.back.setOnClickListener { onBackPressed() }

        //设置全屏按键功能
        video_play.fullscreenButton
            .setOnClickListener { resolveFullBtn(video_play) }

        danmu.isEnabled = false
        danmu.prepare()
        val array = resources.getStringArray(R.array.video_loading)
        lifecycleScope.launch {
            for (index in array) {
                withContext(Dispatchers.Main) {
                    danmu.add(getModel(index))
                }
                delay(500)
            }
        }
        close.setOnClickListener {
            danmu.hideAllDanMuView(true)
            close.visibility = View.GONE
        }
        lifecycleScope.launch {
            delay(10 * 1000)
            danmu.hideAllDanMuView(true)
            close.visibility = View.GONE
        }
        loadAd()
    }

    /**
     * 播放下一集
     */
    private fun playNext() {
        if (playUrls.isEmpty() || playIndex > playUrls.size - 1) return
        playIndex++
        if (playIndex < playUrls.size) {
            moviesDetailAdapter.playAdapter!!.choose = playIndex
            moviesDetailAdapter.playAdapter!!.notifyDataSetChanged()
            playVideo()
        }
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
    }

    /**
     * 设置标题
     */
    @SuppressLint("SetTextI18n")
    fun setVideoTitle() {
        if (video_play.fullCoverVideo != null) {
            video_play.fullCoverVideo!!.titleTextView.text =
                video_play.fullCoverVideo!!.titleTextView.text.toString() + "——" + playNames[playIndex]
        } else {
            video_play.titleTextView.text =
                video_play.titleTextView.text.toString() + "——" + playNames[playIndex]
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
        if (video_play.isTv) video_play.pauseCast()
    }


    override fun onResume() {
        super.onResume()
        if (!video_play.isTv) GSYVideoManager.onResume()
    }

    override fun onStop() {
        super.onStop()
        GSYVideoManager.onPause()
    }

    override fun onDestroy() {
        val json = JSONObject()
        json.put("name", playUrl)
        json.put("position", video_play.playPosition)
        WjSP.getInstance().setValues(XmlConfigs.VIDEO_POSITION, json.toString())
        if (video_play.isTv) video_play.stopCast()

        //插入
        if (recyclerView.adapter != null) {
            GlobalScope.launch(Dispatchers.IO) {
                AppDatabase.db.readHistoryDao().delete(
                    mode.detail,
                    TypesEnum.VIDEO.ordinal
                )
                AppDatabase.db.readHistoryDao().insert(
                    ReadHistoryMode(
                        0,
                        TypesEnum.VIDEO.ordinal, mode.detail, playIndex.toString()
                    )
                )
            }
        }
        super.onDestroy()
        danmu.hideAllDanMuView(true)
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