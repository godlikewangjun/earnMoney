package com.wj.makebai.ui.adapter.home

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.abase.util.Tools
import com.abase.view.weight.RecyclerSpace
import com.bumptech.glide.Glide
import com.qq.e.ads.nativ.NativeExpressADView
import com.sunfusheng.marqueeview.MarqueeView
import com.wj.commonlib.data.mode.Message
import com.wj.commonlib.http.Urls
import com.wj.commonlib.ui.weight.CustomGridManager
import com.wj.ktutils.startActivity
import com.wj.makebai.R
import com.wj.makebai.data.emu.HomeEmu
import com.wj.makebai.data.mode.ArtTypeMode
import com.wj.makebai.data.mode.BannerMode
import com.wj.makebai.data.mode.HomeTypeMode
import com.wj.makebai.statice.StaticData
import com.wj.makebai.ui.activity.MainActivity
import com.wj.makebai.ui.activity.appTask.GameTaskActivity
import com.wj.makebai.ui.activity.article.UcChannelActivity
import com.wj.makebai.ui.activity.comm.LuckPanActivity
import com.wj.makebai.ui.activity.comm.MessageActivity
import com.wj.makebai.ui.activity.comm.SignActivity
import com.wj.makebai.ui.activity.comm.WebActivity
import com.wj.makebai.ui.activity.video.MovieActivity
import com.wj.makebai.ui.adapter.TabAdapter
import com.wj.makebai.ui.adapter.ViewBannerAdapter
import com.wj.makebai.ui.control.CommControl
import com.wj.makebai.ui.fragment.HomeFragment
import com.wj.makebai.ui.fragment.UcListFragment
import com.wj.ui.base.BaseAdapter
import com.wj.ui.base.viewhoder.CustomVhoder
import com.youth.banner.Banner
import kotlinx.android.synthetic.main.home_tab_new_layout.view.*
import kotlinx.android.synthetic.main.home_top_item_layout.view.*


/**
 * 首页的adapter
 * @author Administrator
 * @version 1.0
 * @date 2019/12/26
 */
class HomeAdapter() : BaseAdapter() {
    var fragmentManager: FragmentManager? = null
    var list = ArrayList<HomeTypeMode>()
    var ucNewsUcListFragment: UcListFragment? = null
    var adList:ArrayList<NativeExpressADView>?=null

    constructor(list: ArrayList<HomeTypeMode>) : this() {
        this.list = list
    }

    init {
        setHasStableIds(true)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        when (list[position].type) {
            HomeEmu.TOP -> {
                //banner
                //动态设置高度确保广告图的适应性
                val layoutParams = holder.itemView.banner.layoutParams
                layoutParams.height = Tools.getScreenWH(context)[0] / 2
                CommControl.setAdBanner(
                    StaticData.initMode!!.homeBanner,
                    adList,
                    holder.itemView.banner as Banner<BannerMode, ViewBannerAdapter>,
                    Glide.with(context!!)
                )

                //请求首页分类的入口
                if (holder.itemView.header_content.itemDecorationCount < 1) holder.itemView.header_content.addItemDecoration(
                    RecyclerSpace(5)
                )
                holder.itemView.header_content.adapter =
                    HomeItemAdapter(StaticData.initMode!!.homeType)
                val layoutManager = CustomGridManager(
                    context,
                    if (StaticData.initMode!!.homeType.size < 6) 5 else 4
                ).setScrollEnabled(false)
                holder.itemView.header_content.layoutManager = layoutManager
                //消息部分
                if (StaticData.initMode!!.message.isNotEmpty()) {
                    holder.itemView.marqueeView.removeAllViews()
                    (holder.itemView.marqueeView as MarqueeView<Message>).startWithList(StaticData.initMode!!.message)
                    holder.itemView.marqueeView.setOnItemClickListener { positionIndex, _ ->
                        context!!.startActivity<WebActivity>(
                            "url" to Urls.announcement + StaticData.initMode!!.message[positionIndex].messageid
                        )
                    }
                }
                //短视频
//                if (!videoModes.isNullOrEmpty()) {
//                    val adapter = HomePushVideoAdapter(videoModes!!)
//                    adapter.likeListener = object : RecyerViewItemListener {
//                        override fun click(view: View, position: Int) {
//                            context!!.startActivity<VideoDetailActivity>("url" to (videoModes!![position].mode as VideoMode).url)
//                        }
//                    }
//                    holder.itemView.video_list.visibility = View.VISIBLE
//                    holder.itemView.video.adapter = adapter
//                    holder.itemView.video.layoutManager =
//                        LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
//                    holder.itemView.video_more.setOnClickListener { context!!.startActivity<VideoActivity>() }
//                } else {
//                    holder.itemView.video_list.visibility = View.GONE
//                }

                holder.itemView.message_list.setOnClickListener(clickListener)
                holder.itemView.luck_pan.setOnClickListener(clickListener)
                holder.itemView.sign.setOnClickListener(clickListener)
                holder.itemView.do_video.setOnClickListener(clickListener)
                holder.itemView.go_ad_task.setOnClickListener(clickListener)
                holder.itemView.user_guide.setOnClickListener(clickListener)
                holder.itemView.earn_guide.setOnClickListener(clickListener)
                holder.itemView.news_more.setOnClickListener(clickListener)
            }
            HomeEmu.TAB -> {
                if (HomeFragment.ucPd != null) {
                    if(ucNewsUcListFragment!=null) (context!! as MainActivity).mHomeFragment!!.childFragmentManager.beginTransaction()
                        .remove(ucNewsUcListFragment!!).commitAllowingStateLoss()
                    holder.itemView.home_news.postDelayed({
                        if(ucNewsUcListFragment==null)ucNewsUcListFragment =
                            UcListFragment.newInstance(HomeFragment.ucPd!!.data.channel[0].id)
                        (context!! as MainActivity).mHomeFragment!!.childFragmentManager.beginTransaction()
                            .replace(R.id.home_news, ucNewsUcListFragment!!, "home_news")
                            .commitAllowingStateLoss()

                        HomeFragment.ucPd = null
                    }, 100)
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        super.onCreateViewHolder(parent, viewType)
        return when (viewType) {
            HomeEmu.TOP.ordinal -> CustomVhoder(
                inflater!!.inflate(
                    R.layout.home_top_item_layout,
                    parent,
                    false
                )
            )
            else -> CustomVhoder(
                inflater!!.inflate(
                    R.layout.home_tab_new_layout,
                    parent,
                    false
                )
            )
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return list[position].type!!.ordinal
    }



    private val clickListener = View.OnClickListener { v ->
        when (v!!.id) {
            R.id.sign -> {//签到
                context!!.startActivity<SignActivity>()
            }
            R.id.luck_pan -> {//转盘
                context!!.startActivity<LuckPanActivity>()
            }
            R.id.do_video -> {//视频
                context!!.startActivity<MovieActivity>()
            }
            R.id.message_list -> {//消息列表
                context!!.startActivity<MessageActivity>()
            }
            R.id.user_guide -> {//APP的使用教程
                context!!.startActivity<WebActivity>(
                    "title" to "使用教程",
                    "url" to Urls.userGuide + StaticData.initMode!!.app_guide
                )
            }
            R.id.earn_guide -> {//赚积分教程
                context!!.startActivity<WebActivity>(
                    "title" to "积分攻略",
                    "url" to Urls.userGuide + StaticData.initMode!!.earn_guide
                )
            }
            R.id.go_ad_task -> {//去完成任务
                context!!.startActivity<GameTaskActivity>("online" to true)
//                context!!.startActivity<EarnTaskActivity>()
            }
            R.id.news_more -> {//更多新闻
                context!!.startActivity<UcChannelActivity>()
            }
        }
    }


}