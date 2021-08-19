package com.wj.makebai.ui.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.wj.commonlib.app.BaseApplication
import com.wj.commonlib.data.configs.Configs
import com.wj.commonlib.http.HttpManager
import com.wj.commonlib.statices.Statics
import com.wj.ktutils.isNull
import com.wj.ktutils.startActivity
import com.wj.makebai.R
import com.wj.makebai.statice.StaticData
import com.wj.makebai.ui.activity.PermissionCheckActivity
import com.wj.makebai.ui.activity.appTask.TaskStateListActivity
import com.wj.makebai.ui.activity.base.MakeBaseFragment
import com.wj.makebai.ui.activity.comm.*
import com.wj.makebai.ui.activity.user.*
import com.wj.makebai.utils.MbTools
import kotlinx.android.synthetic.main.fragment_user.*

/**
 * 个人中心
 * @author Administrator
 * @version 1.0
 * @date 2019/11/18
 */
class UserFragment : MakeBaseFragment(), View.OnClickListener {

    override fun setPageName(): String {
        return this::class.java.simpleName
    }

    override fun setContentView(): Int {
        super.setContentView()
        return R.layout.fragment_user
    }

    @SuppressLint("SetTextI18n")
    override fun init() {
        super.init()

        refreshLayout.setOnRefreshListener {
            if (Statics.userMode != null) {
                HttpManager.userDetail({
                    Statics.userMode=it
                    //设置用户数据
                    setUserData()
                    refreshLayout.finishRefresh()
                }) { _, _, _ ->
                    refreshLayout.finishRefresh()
                }
            } else {
                refreshLayout.finishRefresh()
            }
        }

        user_icon.setOnClickListener(this)
        suggestions.setOnClickListener(this)
        bbs.setOnClickListener(this)
        setting.setOnClickListener(this)
        about.setOnClickListener(this)
        store.setOnClickListener(this)
        share.setOnClickListener(this)
        cooperation.setOnClickListener(this)
        exchange.setOnClickListener(this)
        payment_details.setOnClickListener(this)
        auditList.setOnClickListener(this)
        user_message.setOnClickListener(this)
        imgView.setOnClickListener(this)
        user_name.setOnClickListener(this)
        task_list.setOnClickListener(this)
        watch.setOnClickListener(this)
        user_bind.setOnClickListener(this)
        open_vip.setOnClickListener(this)
        val taskClick = View.OnClickListener {
            var type = -1
            type = when (it.id) {
                R.id.task_state_0 -> 0
                R.id.task_state_1 -> 1
                R.id.task_state_2 -> 3
                else -> 2
            }
            requireActivity().startActivity<TaskStateListActivity>("type" to type)
        }
        task_state_0.setOnClickListener(taskClick)
        task_state_1.setOnClickListener(taskClick)
        task_state_2.setOnClickListener(taskClick)
        task_state_3.setOnClickListener(taskClick)

    }

    override fun onResume() {
        super.onResume()
        //设置用户数据
        setUserData()
        refreshLayout.finishRefresh()
    }

    /**
     * 设置用户数据
     */
    @SuppressLint("SetTextI18n")
    private fun setUserData() {
        if (Statics.userMode != null) {
            Glide.with(requireActivity()).load(Statics.userMode!!.usericon)
                .apply(RequestOptions.circleCropTransform()).into(user_icon)
            user_name.text = Statics.userMode!!.username
            if (Statics.userMode!!.usertype == 2) user_type.text = "普通用户"
            else user_type.text = "会员"
            if (Statics.userMode!!.userbalance > 0) user_balance.setMoney(Statics.userMode!!.userbalance) else user_balance.text =
                "0"
            user_money.visibility = View.VISIBLE
            user_id.text = "UID:" + Statics.userMode!!.userid
            if (Statics.userMode!!.audiPoint > 0) user_balance_.setMoney(Statics.userMode!!.audiPoint) else user_balance_.text =
                "0"
            if (Configs.CHANNCODE != "guanfang" && Configs.CHANNCODE != "debug" && Statics.userMode!!.userbalance > 50) imgView.visibility =
                View.VISIBLE else imgView.visibility = View.INVISIBLE

            if (Statics.userMode!!.usertype == 2) {
                user_bind.isVisible=true
                when {
                    Statics.userMode!!.user_phone.isNull() -> user_bind.text = resources.getString(R.string.go_bind_phone)
                    Statics.userMode!!.otherid.isNull() -> user_bind.text = resources.getString(R.string.go_bind_wx)
                    else -> user_bind.isVisible=false
                }
            } else user_bind.visibility = View.GONE

            become_vip.visibility=View.VISIBLE
        } else {
            become_vip.visibility=View.GONE

            user_name.text = getString(R.string.click_login)
            user_id.text = ""
            user_icon.setImageResource(R.drawable.ic_user_center)
            user_type.text = getString(R.string.login_tips)
            user_money.visibility = View.GONE
            imgView.visibility = View.INVISIBLE
        }

        if (StaticData.BBS.isNull()) {
            bbs.visibility = View.GONE
        } else {
            bbs.visibility = View.VISIBLE
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.user_icon, R.id.user_name -> //用户登录或者进入详情页
                if (Statics.userMode == null) requireActivity().startActivity<LoginActivity>()
                else requireActivity().startActivity<UserSetActivity>()

            R.id.setting -> //设置
                requireActivity().startActivity<SetActivity>()

            //任务状态列表
            R.id.task_list -> requireActivity().startActivity<TaskStateListActivity>()
            R.id.imgView -> {//切换到官方模式
                val dialog = AlertDialog.Builder(requireActivity()).setTitle("切换到官方模式")
                    .setMessage("官方模式里面有更多的功能和攒积分的方式哦,需要重启APP")
                    .setPositiveButton(
                        "切换"
                    ) { dialog, _ ->
                        Configs.setChannel("guanfang")

                        BaseApplication.mHandler.postDelayed({
                            val intent = Intent(activity, PermissionCheckActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            android.os.Process.killProcess(android.os.Process.myPid())

                            dialog!!.cancel()
                        }, 500)
                    }.setNegativeButton("取消", null).create()
                dialog.show()
            }
            R.id.cooperation -> //客服
                requireActivity().startActivity<CustomerServiceActivity>()

            R.id.share -> //分享
                requireActivity().startActivity<ShareAppActivity>()

            R.id.exchange -> //积分兑换
                requireActivity().startActivity<ExchangeActivity>()
            R.id.user_bind -> {//绑定
                if (Statics.userMode?.usertype == 2 && Statics.userMode?.user_phone.isNull())
                    startActivity<BindPhoneActivity>()
                else if (Statics.userMode?.usertype == 2 && Statics.userMode?.otherid.isNull())
                    startActivity<BindWeChatActivity>()
            }

            R.id.user_message -> //消息
                if (Statics.userMode != null)
                    requireActivity().startActivity<MessageActivity>("type" to 1)


            R.id.auditList -> //审核列表
                if (MbTools.isLogin(requireActivity()))
                    requireActivity().startActivity<AuditListActivity>()


            R.id.payment_details -> //收支明细
                if (MbTools.isLogin(requireActivity()))
                    requireActivity().startActivity<PaymentDetailsActivity>()


            R.id.suggestions -> //反馈
                if (Statics.userMode != null) {
                    val openid = Statics.userMode!!.userid // 用户的openid
                    val nickname = Statics.userMode!!.username // 用户的nickname
                    val headimgurl = Statics.userMode!!.usericon // 用户的头像url
                    /* 准备post参数 */
                    val postData = "nickname=$nickname&avatar=$headimgurl&openid=$openid"
                    requireActivity().startActivity<WebActivity>(
                        "url" to StaticData.TUCAO,
                        "data" to postData
                    )
                } else {
                    requireActivity().startActivity<WebActivity>("url" to StaticData.TUCAO)
                }

            R.id.bbs -> //论坛
                requireActivity().startActivity<WebActivity>("url" to StaticData.BBS)

            R.id.about -> //关于
                requireActivity().startActivity<AboutActivity>()

            R.id.store -> //收藏
                requireActivity().startActivity<StoreActivity>()

            R.id.watch ->//查看审核中的积分
                requireActivity().startActivity<TaskStateListActivity>("type" to 1)
            R.id.open_vip ->//充值会员
                requireActivity().startActivity<VipCenterActivity>()

        }
    }
}