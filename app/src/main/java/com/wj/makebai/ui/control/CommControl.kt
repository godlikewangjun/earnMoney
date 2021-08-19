package com.wj.makebai.ui.control

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.abase.util.AbWifiUtil
import com.abase.util.GsonUtil
import com.abase.view.weight.LoadWeb
import com.abase.view.weight.RecyclerSpace
import com.abase.view.weight.X5WebView
import com.abase.view.weight.web.WebMethodsListener
import com.bumptech.glide.RequestManager
import com.qq.e.ads.nativ.NativeExpressADView
import com.qq.e.ads.nativ.NativeExpressMediaListener
import com.qq.e.comm.constants.AdPatternType
import com.qq.e.comm.util.AdError
import com.tencent.smtt.sdk.WebView
import com.umeng.analytics.MobclickAgent
import com.wj.commonlib.app.BaseApplication
import com.wj.commonlib.data.mode.AdMode
import com.wj.commonlib.data.mode.ArticleMode
import com.wj.commonlib.http.HttpManager
import com.wj.commonlib.ui.ViewControl
import com.wj.commonlib.ui.weight.CustomGridManager
import com.wj.eventbus.WjEventBus
import com.wj.ktutils.WjSP
import com.wj.ktutils.isNull
import com.wj.ktutils.showTip
import com.wj.ktutils.startActivity
import com.wj.makebai.R
import com.wj.makebai.data.mode.BannerMode
import com.wj.makebai.data.mode.MoviesMode
import com.wj.makebai.data.db.TypesEnum
import com.wj.makebai.statice.UmEventCode
import com.wj.makebai.statice.XmlCodes
import com.wj.makebai.ui.activity.appTask.AppListActivity
import com.wj.makebai.ui.activity.appTask.EarnTaskActivity
import com.wj.makebai.ui.activity.appTask.GameTaskActivity
import com.wj.makebai.ui.activity.article.ArticleDetailActivity
import com.wj.makebai.ui.activity.comic.ComicListActivity
import com.wj.makebai.ui.activity.comm.MoreActivity
import com.wj.makebai.ui.activity.comm.WebActivity
import com.wj.makebai.ui.activity.novel.NovelListActivity
import com.wj.makebai.ui.activity.parsing.VipActivity
import com.wj.makebai.ui.activity.video.MovieActivity
import com.wj.makebai.ui.activity.video.MovieDetailActivity
import com.wj.makebai.ui.activity.zip.ImageZipActivity
import com.wj.makebai.ui.adapter.ImgAdapter
import com.wj.makebai.ui.adapter.ViewBannerAdapter
import com.wj.makebai.ui.weight.NoData
import com.wj.makebai.utils.MbTools
import com.wj.ui.base.BaseAdapter
import com.youth.banner.Banner
import com.youth.banner.indicator.CircleIndicator
import com.youth.banner.listener.OnBannerListener
import kotlinx.android.synthetic.main.article_item_layout.view.*
import kotlinx.android.synthetic.main.article_item_layout.view.date
import kotlinx.android.synthetic.main.article_item_layout.view.title
import kotlinx.android.synthetic.main.dialog_announcement_layout.view.*
import kotlinx.android.synthetic.main.movie_item_layout.view.*

/**
 * 公共的ui处理
 * @author dchain
 * @version 1.0
 * @date 2019/4/10
 */
object CommControl {
    /**
     * 断网处理
     */
    fun noNetWork(
        activity: Context,
        nodata: NoData,
        title_content: TextView?,
        webview: X5WebView,
        progressbar: ProgressBar,
        contentView: View?
    ) {
        //浏览器处理
        nodata.type = NoData.DataState.NO_NETWORK
        nodata.visibility = View.GONE
        nodata.setBackgroundResource(android.R.color.white)
        if (!AbWifiUtil.isConnectivity(activity)) nodata.visibility = View.VISIBLE

        webview.webMethodsListener = object : WebMethodsListener() {
            override fun onX5ProgressChanged(p0: WebView?, p1: Int) {
                if (p1 == 100) {
                    progressbar.visibility = View.GONE
                } else {
                    progressbar.progress = p1
                    progressbar.visibility = View.VISIBLE
                }
            }
        }

        WjEventBus.getInit().subscribe(LoadWeb.LOADFINSH, Int::class.java) {
            if (AbWifiUtil.isConnectivity(activity)) {
                if (title_content != null && title_content.text.isEmpty()) title_content.text =
                    webview.title
                nodata.visibility = View.GONE
                contentView?.visibility = View.VISIBLE
            } else {
                contentView?.visibility = View.GONE
                nodata.visibility = View.VISIBLE
            }
            progressbar.visibility = View.GONE
        }


        WjEventBus.getInit().subscribe(LoadWeb.LOADERROE, Int::class.java) {
            if (AbWifiUtil.isConnectivity(activity)) {
                nodata.visibility = View.GONE
                contentView?.visibility = View.VISIBLE
            } else {
                nodata.visibility = View.VISIBLE
                contentView?.visibility = View.GONE
            }
            progressbar.visibility = View.GONE
        }
        nodata.reshListener = View.OnClickListener {
            if (!AbWifiUtil.isConnectivity(activity)) {
                activity.showTip(activity.getString(R.string.not_network))
                return@OnClickListener
            }
            webview.reload()
            nodata.visibility = View.GONE
            progressbar.visibility = View.VISIBLE
        }
    }

    /**
     * 工具跳转
     */
    fun homeTools(context: Context, link: String, title: String) {
        if (link.isNull()) return context.showTip("暂未开放")
        when {
            link.startsWith("http") -> {
                val intent = Intent(BaseApplication.mApplication!!.get()!!, WebActivity::class.java)
                intent.putExtra("url", link)
                intent.putExtra("title", title)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
            link == "game" -> context.startActivity<GameTaskActivity>("online" to true)
            link == "earnGame" -> context.startActivity<GameTaskActivity>("online" to true)
            link == "movie" -> context.startActivity<MovieActivity>()
            link == "comic" -> context.startActivity<ComicListActivity>()
            link == "novel" -> context.startActivity<NovelListActivity>()
            link == "vip" -> context.startActivity<VipActivity>()
            link == "jfq" -> context.startActivity<AppListActivity>()
            link == "earn" -> context.startActivity<EarnTaskActivity>()
            link == "zipImg" -> context.startActivity<ImageZipActivity>()
            link == "more" -> context.startActivity<MoreActivity>()// 展开更多
        }
        val music = HashMap<String, Any>()
        music["type"] = link//自定义参数
        MobclickAgent.onEventObject(context, UmEventCode.UM_HOMETYPE, music)
    }

    /**
     * 设置banner
     */
    fun setBanner(
        list: ArrayList<AdMode>,
        banner: Banner<BannerMode, ViewBannerAdapter>,
        glide: RequestManager
    ) {
        val imgList = ArrayList<BannerMode>()
        for (index in list) {
            if (index.img_url != null && !index.img_url.isNull()) {
                imgList.add(BannerMode(1, index.img_url))
                glide.load(index.img_url).preload()
            }
        }
        banner.setStartPosition(0)
        banner.indicator = CircleIndicator(banner.context)
        banner.adapter = ViewBannerAdapter(imgList)
        banner.setLoopTime(5000)
        var click = false
        banner.setOnBannerListener(object : OnBannerListener<BannerMode> {
            override fun OnBannerClick(data: BannerMode?, position: Int) {
                if (position >= list.size) return
                if (list[position].ads_link.isNull() || click) return//不跳转
                click = true
                homeTools(banner.context, list[position].ads_link, "")

                banner.postDelayed({ click = false }, 1000)
            }
        })
        //样式
        banner.start()
    }

    /**
     * 设置banner
     */
    fun setAdBanner(
        list: ArrayList<AdMode>,
        adList:ArrayList<NativeExpressADView>?,
        banner: Banner<BannerMode, ViewBannerAdapter>,
        glide: RequestManager
    ) {
        val imgList = ArrayList<BannerMode>()
        for (index in list) {
            if (index.img_url != null && !index.img_url.isNull()) {
                imgList.add(BannerMode(1, index.img_url))
                glide.load(index.img_url).preload()
            }
        }
       if(adList!=null) for (view in adList) {
            if (view.boundData.adPatternType == AdPatternType.NATIVE_VIDEO) {
                view.setMediaListener(mediaListener)
            }
            view.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            imgList.add(BannerMode(2, view))
        }
        banner.setStartPosition(0)
        banner.indicator = CircleIndicator(banner.context)
        banner.adapter = ViewBannerAdapter(imgList)
        banner.setLoopTime(5000)
        var click = false
        banner.setOnBannerListener(object : OnBannerListener<BannerMode> {
            override fun OnBannerClick(data: BannerMode?, position: Int) {
                if (position >= list.size) return
                if (list[position].ads_link.isNull() || click) return//不跳转
                click = true
                homeTools(banner.context, list[position].ads_link, "")

                banner.postDelayed({ click = false }, 1000)
            }
        })
        //样式
        banner.start()
    }
    val mediaListener: NativeExpressMediaListener = object :
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
     * 文章列表样式处理设置
     */
    fun bindArticleData(
        context: Context,
        glide: RequestManager,
        adapter: BaseAdapter,
        holder: RecyclerView.ViewHolder,
        mode: ArticleMode,
        position: Int,
        isAd: Boolean
    ) {
        holder.itemView.title.text = mode.articlename
        holder.itemView.desc.text = mode.source
        if (mode.isRead) {
            holder.itemView.title.setTextColor(context.resources.getColor(R.color.text_gray))
        } else {
            holder.itemView.title.setTextColor(context.resources.getColor(R.color.black))
        }
        holder.itemView.date.text = mode.creatime
        if (!mode.imageurls.isNull()) {
            try {
                val images =
                    GsonUtil.Gson2ArryList(
                        mode.imageurls.replace("\\", ""),
                        Array<String>::class.java
                    )
                if (images.size == 1) {
                    holder.itemView.image.visibility = View.VISIBLE
                    holder.itemView.recyclerView.visibility = View.GONE

                    glide.asBitmap().load(images[0]).into(holder.itemView.image)
                } else {
                    holder.itemView.image.visibility = View.GONE
                    holder.itemView.recyclerView.visibility = View.VISIBLE

                    val imgAdapter = ImgAdapter(images as ArrayList<String>)
                    holder.itemView.recyclerView.adapter = imgAdapter
                    if (images.size > 1) imgAdapter.type = 3 else imgAdapter.type = 2
                    holder.itemView.recyclerView.layoutManager =
                        CustomGridManager(context, 3).setScrollEnabled(false)
                    if (holder.itemView.recyclerView.itemDecorationCount < 1) holder.itemView.recyclerView.addItemDecoration(
                        RecyclerSpace(5)
                    )
                }
            } catch (e: Exception) {
            }

        }
        holder.itemView.setOnClickListener {
            HttpManager.likeOrRead(mode.articleid, 2) {}
            if (mode.articlecontent.startsWith("http")) context.startActivity<WebActivity>(
                "url" to mode.articlecontent, "ad" to true
            ) else {
                context.startActivity<ArticleDetailActivity>("data" to mode, "ad" to true)
            }
            adapter.notifyItemChanged(position)
        }
    }

    /**
     * 绑定视频
     */
    @SuppressLint("SetTextI18n")
    fun bindMoview(
        context: Context,
        glide: RequestManager,
        holder: RecyclerView.ViewHolder,
        mode: MoviesMode
    ) {
        holder.itemView.title.text = mode.title
//        holder.itemView.area.text = mode.area
//        holder.itemView.actor.text = mode.actors
//        holder.itemView.director.text = mode.director
        holder.itemView.date.text =
            String.format(mode.pushDate)
//        holder.itemView.create_date.text =
//            String.format(context.resources.getString(R.string.update_time), mode.pushDate)
//        holder.itemView.state.text = mode.state
//        holder.itemView.desc.text = mode.introduction
        holder.itemView.type.text = mode.type
//        holder.itemView.language.text = mode.language
//        glide.load(mode.image).into(holder.itemView.image)

        holder.itemView.setOnClickListener {
            context.startActivity<MovieDetailActivity>(
                "title" to mode.title,
                "data" to mode
            )
        }
    }

    /**
     * 显示公告
     */
    fun announcementDialog(activity: Context, content: String) {
        if (activity is Activity && activity.isFinishing) return
        val contentView =
            LayoutInflater.from(activity).inflate(R.layout.dialog_announcement_layout, null)
        contentView.text_content.text = Html.fromHtml(content)
        val dialog = ViewControl.customAlertDialog(activity, contentView, 300f)
        contentView.close.setOnClickListener { dialog.cancel() }
        contentView.ok.setOnClickListener {
            dialog.cancel()
            WjSP.getInstance().setValues(XmlCodes.HAS_READ, content)
        }

        MbTools.openWebText(activity, contentView.text_content) { _, url ->
            activity.startActivity<WebActivity>("url" to url)
        }

    }
}