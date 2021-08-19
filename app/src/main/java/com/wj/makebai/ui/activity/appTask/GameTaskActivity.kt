package com.wj.makebai.ui.activity.appTask

import android.view.View
import com.wj.commonlib.http.HttpManager
import com.wj.commonlib.statices.Statics
import com.wj.ktutils.startActivity
import com.wj.makebai.R
import com.wj.makebai.ui.activity.base.MakeActivity
import com.wj.makebai.ui.activity.comm.WebActivity
import com.wj.makebai.utils.MbTools
import com.xianwan.sdklibrary.helper.XWADPage
import com.xianwan.sdklibrary.helper.XWADPageConfig
import kotlinx.android.synthetic.main.activity_game_task.*


/**
 * 游戏任务类
 * @author Administrator
 * @version 1.0
 * @date 2019/12/16
 */
class GameTaskActivity :MakeActivity(),View.OnClickListener{
    override fun bindLayout(): Int {
        return R.layout.activity_game_task
    }

    override fun initData() {
        title_content.text=getString(R.string.all_game)


        online.setOnClickListener(this)
        game.setOnClickListener(this)

        if(intent.hasExtra("online")) {
            game.performClick()
            finish()
        }
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.online->{//在线游戏
                HttpManager.gameInfo{
                    startActivity<WebActivity>("url" to it.value,"title" to "在线游戏")
                }
            }
            R.id.game->{//获取积分类游戏
                if(MbTools.isLogin(activity)){
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
        }
    }
}