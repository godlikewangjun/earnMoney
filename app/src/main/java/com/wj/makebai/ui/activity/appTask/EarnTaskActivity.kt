package com.wj.makebai.ui.activity.appTask

import android.annotation.SuppressLint
import android.content.Intent
import android.view.View
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import com.abase.util.AbViewUtil
import com.gyf.immersionbar.ImmersionBar
import com.umeng.socialize.UMShareAPI
import com.wj.commonlib.http.HttpManager
import com.wj.commonlib.statices.Statics
import com.wj.ktutils.WjSP
import com.wj.ktutils.startActivity
import com.wj.makebai.R
import com.wj.makebai.statice.XmlCodes
import com.wj.makebai.ui.activity.article.ChannelArticleActivity
import com.wj.makebai.ui.activity.base.MakeActivity
import com.wj.makebai.ui.activity.comm.LuckPanActivity
import com.wj.makebai.ui.activity.comm.SignActivity
import com.wj.makebai.ui.activity.user.PaymentDetailsActivity
import com.wj.makebai.ui.weight.NoData
import com.wj.makebai.utils.MbTools
import com.xianwan.sdklibrary.helper.XWADPage
import com.xianwan.sdklibrary.helper.XWADPageConfig
import kotlinx.android.synthetic.main.activity_earntask.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 赚钱的集合页面
 * @author Administrator
 * @version 1.0
 * @date 2019/12/16
 */
class EarnTaskActivity : MakeActivity(), View.OnClickListener {
    override fun bindLayout(): Int {
        return R.layout.activity_earntask
    }

    override fun initData() {
        ImmersionBar.with(this).statusBarDarkFont(false).init()
        title.visibility = View.GONE
        title_systembar.visibility = View.GONE

        title_content.setTextColor(resources.getColor(R.color.white))
        backto_img.setImageResource(R.drawable.ic_back_w)


        do_game.setOnClickListener(this)
        do_app.setOnClickListener(this)
        do_art.setOnClickListener(this)
        do_sign.setOnClickListener(this)
        do_luck.setOnClickListener(this)
        do_share.setOnClickListener(this)
        do_video.setOnClickListener(this)
        view1.setOnClickListener(this)
        back.setOnClickListener(this)
        scrollView.setOnScrollChangeListener { v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            if (scrollView.scrollY > AbViewUtil.dp2px(activity, 60f)) {
                back.setImageResource(R.drawable.ic_back2)
                view.setTextColor(resources.getColor(R.color.text))
                view1.setTextColor(resources.getColor(R.color.text))
                top.setBackgroundResource(R.color.white)
            } else {
                back.setImageResource(R.drawable.ic_back_w)
                view.setTextColor(resources.getColor(R.color.white))
                view1.setTextColor(resources.getColor(R.color.white))
                top.setBackgroundResource(android.R.color.transparent)
            }
        }

        setState(NoData.DataState.LOADING, false)

    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    /**
     * 数据刷新
     */
    @SuppressLint("SetTextI18n")
    private fun refresh() {
        if (Statics.userMode != null) {
            HttpManager.taskInfo({
                read_art_count.text = String.format(
                    resources.getString(R.string.earn_game_tip4),
                    it.artShareCount,
                    it.artAllShareCount
                )
                luck_count.text =
                    String.format(resources.getString(R.string.earn_game_tip8), 3 - it.luckCount, 3)
                share_count.text = String.format(
                    resources.getString(R.string.earn_game_tip10),
                    it.shareCount,
                    it.shareAllCount
                )
                video_count.text = String.format(
                    resources.getString(R.string.earn_game_tip12),
                    it.videoCount,
                    it.videoAllCount
                )

                if (it.artShareCount >= it.artAllShareCount) {
                    do_art.isEnabled = false
                    do_art.setBackgroundResource(R.drawable.shape_earn_enable)
                }

                if (it.luckCount < 1) {
                    do_luck.isEnabled = false
                    do_luck.setBackgroundResource(R.drawable.shape_earn_enable)
                }

                if (it.shareCount >= it.shareAllCount) {
                    do_share.isEnabled = false
                    do_share.setBackgroundResource(R.drawable.shape_earn_enable)
                }

                if (!it.isSign) {
                    do_sign.isEnabled = false
                    do_sign.setBackgroundResource(R.drawable.shape_earn_enable)
                }
                do_video.isEnabled = true
                if (it.videoCount >= it.videoAllCount) {
                    do_video.isEnabled = false
                    do_video.setBackgroundResource(R.drawable.shape_earn_enable)
                } else if (it.videoCount == 1) {
                    timer()
                }
            }) {
                setState(NoData.DataState.GONE, false)
            }
            view1.visibility = View.VISIBLE
        } else {
            read_art_count.text = String.format(resources.getString(R.string.earn_game_tip4), 0, 0)
            luck_count.text = String.format(resources.getString(R.string.earn_game_tip8), 0, 3)
            share_count.text = String.format(resources.getString(R.string.earn_game_tip10), 0, 0)
            video_count.text = String.format(resources.getString(R.string.earn_game_tip12), 0, 0)
            view1.visibility = View.GONE
            setState(NoData.DataState.GONE, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun timer() {
        lifecycleScope.launch(Dispatchers.IO) {
            val time = WjSP.getInstance().getValues(XmlCodes.VIDEOWATCH, 0L)
            val cutTime = System.currentTimeMillis() - time
            val countTime = 10 * 60 * 1000
            with(Dispatchers.Main) {
                if (time == 0L || cutTime > countTime) {
                    do_video.isEnabled = true
                    video_tip.text = getString(R.string.earn_game_tip12_)
                } else {
                    do_video.isEnabled = false
                    do_video.setBackgroundResource(R.drawable.shape_earn_enable)
                    video_tip.text = getString(R.string.earn_game_tip12_) + String.format(
                        getString(R.string.earn_game_tip12_tip),
                        ((countTime - cutTime) / 1000).toString()
                    )
                }
            }
            delay(1000)
            timer()
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.do_game -> {//游戏任务
                if (MbTools.isLogin(activity)) {
                    XWADPage.jumpToAD(
                        XWADPageConfig.Builder(Statics.userMode!!.userid.toString()) //必传参数
                            .pageType(XWADPageConfig.PAGE_AD_LIST) // PAGE_AD_LIST -> 进入列表页、 PAGE_AD_DETAIL->进入详情页
                            .actionBarBgColor("#FFFFFF") // 设置广告页actionbar 颜色
                            .actionBarBackImageRes(R.drawable.ic_back2) //设置返回按钮图片
                            .actionBarTitle(getString(R.string.most_game)) //设置广告列表页面首次加载时标题
                            .actionBarTitleColor("#333333") //设置广告页面标题颜色
                            .msaOAID(Statics.OAID) // 获取不到可不用设置 或者传 空/null 不可乱传
                            .build()
                    )
                }
            }
            R.id.do_app -> {//应用任务
                startActivity<AppListActivity>()
            }
            R.id.do_art -> {//文章任务
                startActivity<ChannelArticleActivity>()
            }
            R.id.do_sign -> {//签到
                startActivity<SignActivity>()
            }
            R.id.do_luck -> {//抽奖
                startActivity<LuckPanActivity>()
            }
            R.id.do_video -> {//看视频
                do_video.isEnabled = false
                if (MbTools.isLogin(activity)) startActivity<VideoTaskActivity>()
            }
            R.id.view1 -> {//积分明细
                if (MbTools.isLogin(activity)) startActivity<PaymentDetailsActivity>()
            }
            R.id.do_share -> {//分享
               MbTools.shareDialog(activity){
                   refresh()
               }
            }
            R.id.back -> {
                finish()
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }

}