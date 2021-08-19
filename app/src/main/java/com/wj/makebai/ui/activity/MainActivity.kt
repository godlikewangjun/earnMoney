package com.wj.makebai.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.os.SystemClock
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.abase.okhttp.OhHttpClient
import com.abase.util.AbLogUtil
import com.lahm.library.EasyProtectorLib
import com.umeng.analytics.MobclickAgent
import com.umeng.message.IUmengCallback
import com.umeng.message.PushAgent
import com.wj.commonlib.statices.Statics
import com.wj.eventbus.WjEventBus
import com.wj.ktutils.WjSP
import com.wj.ktutils.isNull
import com.wj.ktutils.showTip
import com.wj.ktutils.startActivity
import com.wj.makebai.BuildConfig
import com.wj.makebai.R
import com.wj.makebai.data.db.MySqlHelper
import com.wj.makebai.statice.Codes
import com.wj.makebai.statice.StaticData
import com.wj.makebai.statice.UmEventCode
import com.wj.makebai.statice.XmlCodes
import com.wj.makebai.ui.activity.base.MakeActivity
import com.wj.makebai.ui.activity.user.LoginActivity
import com.wj.makebai.ui.adapter.home.HomeTabAdapter
import com.wj.makebai.ui.control.CommControl
import com.wj.makebai.ui.fragment.HomeFragment
import com.wj.makebai.ui.fragment.TaskHallFragment
import com.wj.makebai.ui.fragment.UserFragment
import com.wj.makebai.ui.weight.rain.giftrain.RedPacketViewHelper
import com.wj.makebai.ui.weight.rain.model.BoxInfo
import com.wj.makebai.ui.weight.rain.model.BoxPrizeBean
import com.wj.makebai.utils.MbTools
import com.yanbo.lib_screen.manager.ClingManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.home_top_item_layout.*
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.set


class MainActivity : MakeActivity() {
    public var mHomeFragment: HomeFragment? = null///发现
    private var mTaskHallFragment: TaskHallFragment? = null
    private var mUserFragment: UserFragment? = null//论坛
//    private var mConversationFragment: ConversationFragment? = null//聊天

    //    private var mContactFragment: ContactFragment? = null//好友
    private var mRedPacketViewHelper: RedPacketViewHelper? = null//红包雨

    companion object {
        var isLive = false
        var choose = -1
    }

    override fun bindLayout(): Int {
        return R.layout.activity_main
    }

    override fun initData() {
        isLive = true
        isAnim = false//关掉关闭动画
        title_systembar.visibility = View.GONE
        title.visibility = View.GONE
        createTabs()
        title.setBackgroundResource(R.color.colorAccent)

        //延迟判断是否应该升级
        title_content.postDelayed({
            if (!BuildConfig.DEBUG) {//不是测试的时候检测
                checkZb()
            }
//            mRedPacketViewHelper = RedPacketViewHelper(this)
//            rain()

            //投屏
            ClingManager.getInstance().init(this)
            ClingManager.getInstance().startClingService()


        }, 2000)

        //有公告通知就显示
        if (StaticData.announcement != null) {
            val hasRead = WjSP.getInstance().getValues(XmlCodes.HAS_READ, "")
            if (hasRead.isNull() || hasRead != StaticData.announcement!!.value) {
                CommControl.announcementDialog(activity, StaticData.announcement!!.value)
            }
        }

        if (intent.hasExtra("scheme")) {
            MbTools.schemeOpen(
                activity,
                intent.getStringExtra("scheme")!!,
                intent.getStringExtra("schemeType")!!
            )
        }
        //推送
        if (Statics.userMode != null) {
            PushAgent.getInstance(this).setAlias(Statics.userMode!!.userid.toString(),Statics.UMPUSHTYPE
            ) { p0, p1 -> }
//            loginIM()
        }
        if(PushAgent.getInstance(this).isPushCheck && WjSP.getInstance().getValues(XmlCodes.JPUSH_OPEN,true))
            PushAgent.getInstance(this).enable(object : IUmengCallback {
                override fun onSuccess() {
                }

                override fun onFailure(p0: String?, p1: String?) {
                }
            })

        WjEventBus.getInit().subscribe(Codes.USERLOGIN, Int::class.java) {
//            loginIM()
        }
    }

    /**
     * 腾讯Im登录处理
     */
    /*private fun loginIM() {
        if (Statics.userMode != null) {
            //腾讯IM登录
            // 获取userSig函数
            try {
                val userSig = GenerateTestUserSig.genTestUserSig(Statics.userMode!!.userid.toString())
                TUIKit.login(
                    Statics.userMode!!.userid.toString(),
                    userSig,
                    object : IUIKitCallBack {
                        override fun onError(module: String, code: Int, desc: String) {
                            AbLogUtil.e(
                                MainActivity::class.java,
                                "登录失败, errCode = $code, errInfo = $desc"
                            )
                        }

                        override fun onSuccess(data: Any?) {
                            //设置自己的资料
                            val map = HashMap<String, Any>()
                            map[TIMUserProfile.TIM_PROFILE_TYPE_KEY_NICK] =
                                Statics.userMode!!.username
                            map[TIMUserProfile.TIM_PROFILE_TYPE_KEY_FACEURL] =
                                Statics.userMode!!.usericon
                            TIMFriendshipManager.getInstance().modifySelfProfile(
                                map,
                                object : TIMCallBack {
                                    override fun onSuccess() {
                                    }

                                    override fun onError(p0: Int, p1: String?) {
                                    }

                                })
                            //自动加群
                            TIMGroupManager.getInstance()
                                .applyJoinGroup("@TGS#2MP7WOQGS", "", object : TIMCallBack {
                                    override fun onError(i: Int, s: String) {
                                        AbLogUtil.e(
                                            AddMoreActivity::class.java,
                                            "Error code = $i, desc = $s"
                                        )
                                    }

                                    override fun onSuccess() {
                                        //                                ToastUtil.toastShortMessage("加群请求已发送")
                                        //                                finish()
                                    }
                                })
                        }
                    })
            } catch (e: Exception) {
            }
        }
    }*/

    /**
     * 作弊检查上报
     */
    private fun checkZb() {
        //检测模拟器
        EasyProtectorLib.checkIsRunningInEmulator(
            activity
        ) { emulatorInfo ->
            if (!emulatorInfo.isNull()) {
                val music = HashMap<String, Any>()
                music["emulatorInfo"] = emulatorInfo!!
                if (Statics.userMode != null) music["userid"] = Statics.userMode!!.userid
                music["type"] = "Emulator"
                MobclickAgent.onEventObject(activity, UmEventCode.MONIQI_XUNIJI, music)
            }
        }
        //多开
        EasyProtectorLib.checkIsRunningInVirtualApk("123") {
            val music = HashMap<String, Any>()
            if (Statics.userMode != null) music["userid"] = Statics.userMode!!.userid
            music["type"] = "VirtualApk"
            MobclickAgent.onEventObject(activity, UmEventCode.MONIQI_XUNIJI, music)
        }
        //root
        if (EasyProtectorLib.checkIsRoot()) {
            val music = HashMap<String, Any>()
            if (Statics.userMode != null) music["userid"] = Statics.userMode!!.userid
            music["type"] = "Root"
            MobclickAgent.onEventObject(activity, UmEventCode.MONIQI_XUNIJI, music)
        }
        //root
        if (EasyProtectorLib.checkIsXposedExist()) {
            val music = HashMap<String, Any>()
            if (Statics.userMode != null) music["userid"] = Statics.userMode!!.userid
            music["type"] = "Xposed"
            MobclickAgent.onEventObject(activity, UmEventCode.MONIQI_XUNIJI, music)
        }
    }

    /**
     * 重启重写防止viewpager崩溃
     */
    override fun recreate() {
        clearFragment()
        super.recreate()
    }

    /**
     * 清理fragment
     */
    private fun clearFragment(){
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        for (fragment in supportFragmentManager.fragments)  fragmentTransaction.remove(fragment)
        fragmentTransaction.commitNow()
    }
    /**
     * 设置菜单和viewpager
     */
    private fun createTabs() {
        mTaskHallFragment = TaskHallFragment()
        mHomeFragment = HomeFragment()
//        mConversationFragment = ConversationFragment()
//        mContactFragment = ContactFragment()
        mUserFragment = UserFragment()

        val transaction = supportFragmentManager.beginTransaction()
        for (fragment in supportFragmentManager.fragments) {
            supportFragmentManager.popBackStack()
            transaction.remove(fragment)
        }
        transaction.commitAllowingStateLoss()

        val fragments = ArrayList<Fragment>().apply {
            add(mTaskHallFragment!!)
//            add(mConversationFragment!!)
//            add(mContactFragment!!)
            add(mHomeFragment!!)
            add(mUserFragment!!)
        }
        val tabAdapter = HomeTabAdapter(this, fragments)
        /*  supportFragmentManager,
          FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT*/
        viewPager.offscreenPageLimit = fragments.size
        viewPager.adapter = tabAdapter
        viewPager.isUserInputEnabled = false
        viewPager.isSaveEnabled=false

        navigation.getChildAt(0).isSelected = true
        for (index in 0 until navigation.childCount) {
//            val text=(navigation.getChildAt(index) as TextView)
            navigation.getChildAt(index).setOnClickListener {
                viewPager.setCurrentItem(index, false)
            }
        }
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            var oldPosition = 0
            override fun onPageSelected(position: Int) {
//                if (position == 1 && Statics.userMode == null) {
//                    activity!!.startActivity<LoginActivity>()
//                    showTip("请登录")
//                }
                oldPosition = position
                for (index in 0 until navigation.childCount) {
                    navigation.getChildAt(index).isSelected = false
                }
                navigation.getChildAt(position).isSelected = true
            }
        })
    }

    private var exitTimeStamp: Long = 0
    override fun onBackPressed() {
        if (mHomeFragment!!.onBackPressed()) {
            return
        } else {
            if (SystemClock.uptimeMillis() - exitTimeStamp < 2000) {
//                OhHttpClient.destroy()
                //返回桌面
                val home = Intent(Intent.ACTION_MAIN)
                home.addCategory(Intent.CATEGORY_HOME)
                startActivity(home)
//                finshTo()
            } else {
                exitTimeStamp = SystemClock.uptimeMillis()
                showTip("再按一次返回")
            }
        }
    }

    /**
     * 开启红包雨
     */
    private fun rain() {
        mRedPacketViewHelper!!.endGiftRain()
        window.decorView.postDelayed({
            val boxInfos: MutableList<BoxInfo> = ArrayList()
            for (i in 0..99) {
                val boxInfo = BoxInfo()
                boxInfo.awardId = i
                boxInfo.voucher = "ice $i"
                if (i % 2 == 0) boxInfo.type = 1
                boxInfos.add(boxInfo)
            }
            mRedPacketViewHelper!!.launchGiftRainRocket(
                0,
                boxInfos,
                object : RedPacketViewHelper.GiftRainListener {
                    override fun startRain() {
                        banner.stop()
                    }

                    override fun openGift(boxPrizeBean: BoxPrizeBean?) {
                    }

                    override fun endRain() {
                        banner.start()
                    }

                    override fun startLaunch() {
                    }
                })
        }, 500)
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
//        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onDestroy() {
        if (title_content != null) {
            title_content.handler?.removeCallbacksAndMessages(null)
            isLive = false
            MySqlHelper.getInstance(application).close()
            mRedPacketViewHelper?.endGiftRain()
        }
        OhHttpClient.getInit().destroyAll()
        ClingManager.getInstance().stopClingService()
        super.onDestroy()
    }
}
