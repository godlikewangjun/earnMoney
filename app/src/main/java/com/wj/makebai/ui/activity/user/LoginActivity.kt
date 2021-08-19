package com.wj.makebai.ui.activity.user

import android.animation.ObjectAnimator
import android.graphics.Paint
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import com.umeng.analytics.MobclickAgent
import com.umeng.message.PushAgent
import com.umeng.message.UTrack
import com.umeng.socialize.UMShareAPI
import com.umeng.socialize.UMShareConfig
import com.wj.commonlib.http.DzApi
import com.wj.commonlib.http.HttpManager
import com.wj.commonlib.http.Urls
import com.wj.commonlib.statices.EventCodes
import com.wj.commonlib.statices.Statics
import com.wj.commonlib.ui.ViewControl
import com.wj.commonlib.utils.LoadDialog
import com.wj.eventbus.WjEventBus
import com.wj.ktutils.isNull
import com.wj.ktutils.isPhoneNum
import com.wj.ktutils.showTip
import com.wj.ktutils.startActivity
import com.wj.makebai.R
import com.wj.makebai.statice.Codes
import com.wj.makebai.statice.UmEventCode
import com.wj.makebai.ui.activity.MainActivity
import com.wj.makebai.ui.activity.base.MakeActivity
import com.wj.makebai.ui.activity.comm.WebActivity
import ebz.lsds.qamj.os.OffersManager
import kotlinx.android.synthetic.main.activity_login.*


/**
 * 登录
 * @author dchain
 * @version 1.0
 * @date 2019/9/25
 */
class LoginActivity : MakeActivity(), View.OnClickListener {
    private var mAnimator: ObjectAnimator? = null

    override fun bindLayout(): Int {
        return R.layout.activity_login
    }

    override fun initData() {
        title_systembar.visibility = View.GONE
        title.visibility = View.GONE

        val config = UMShareConfig()
        config.isNeedAuthOnGetUserInfo(true)
        UMShareAPI.get(activity).setShareConfig(config)

        title_content.text = getString(R.string.login)

        rule.paint.flags = Paint.UNDERLINE_TEXT_FLAG
        rule.paint.isAntiAlias = true

        rule1.paint.flags = Paint.UNDERLINE_TEXT_FLAG
        rule1.paint.isAntiAlias = true


        val textWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val phone: String = edit_phone.text.toString()
                val pwd: String = edit_pwd.text.toString()
                btn_login.isEnabled = !TextUtils.isEmpty(phone) && !TextUtils.isEmpty(pwd)
            }

            override fun afterTextChanged(s: Editable) {}
        }
        edit_phone.addTextChangedListener(textWatcher)
        edit_pwd.addTextChangedListener(textWatcher)
        rule.setOnClickListener(this)
        rule1.setOnClickListener(this)
        login.setOnClickListener(this)
        ck_rule.setOnClickListener(this)
        btn_login.setOnClickListener(this)
        btn_register.setOnClickListener(this)
        btn_forget_pwd.setOnClickListener(this)

        //统计
        val music = HashMap<String, Any>()
        MobclickAgent.onEventObject(activity, UmEventCode.LOGIN_VIEW, music)
        WjEventBus.getInit().subscribe(Codes.LOGINFINSH,Int::class.java){
            finish()
        }
    }


    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.login -> {
                if (ck_rule.isChecked) {
                    val music = HashMap<String, Any>()
                    music["type"] = "微信"
                    MobclickAgent.onEventObject(activity, UmEventCode.LOGIN_CLICK, music)

                    ViewControl.login(activity) {
                        loginSuccess("微信")
                        if (Statics.userMode!!.usertype == 2 && Statics.userMode!!.user_phone.isNull())
                            startActivity<BindPhoneActivity>()
                        else
                            startActivity<MainActivity>()
                        finish()
                    }
                } else {
                    showTip(getString(R.string.ok_rule))
                }
            }
            R.id.rule -> {
                startActivity<WebActivity>("url" to Urls.userRule)
            }
            R.id.rule1 -> {
                startActivity<WebActivity>("url" to Urls.userPrivacyRule)
            }
            R.id.btn_register -> {
                startActivity<RegisterActivity>()
            }
            R.id.btn_forget_pwd -> {
                startActivity<FindPwdActivity>()
            }
            R.id.btn_login -> {
                val phoneNum: String = edit_phone.text.toString().trim { it <= ' ' }
                if (TextUtils.isEmpty(phoneNum)) {
                    edit_phone.error = getString(R.string.login_input_phone)
                    edit_phone.requestFocus()
                    return
                }
                if (!phoneNum.isPhoneNum()) {
                    edit_phone.error = getString(R.string.login_phone_error)
                    edit_phone.requestFocus()
                    return
                }
                val pwd: String = edit_pwd.text.toString().trim { it <= ' ' }
                if (TextUtils.isEmpty(pwd)) {
                    edit_pwd.error = getString(R.string.login_input_pwd)
                    edit_pwd.requestFocus()
                    return
                }
                //统计
                val music = HashMap<String, Any>()
                music["phoneNum"] = phoneNum
                music["type"] = "手机"
                MobclickAgent.onEventObject(activity, UmEventCode.LOGIN_CLICK, music)

                LoadDialog.show(activity)
                HttpManager.pwLogin(edit_phone.text.toString(), edit_pwd.text.toString(), {
                    Statics.userMode = it

                    loginSuccess("手机")

//                    if (Statics.userMode!!.usertype == 2 && Statics.userMode!!.otherid.isNull())
//                        startActivity<BindWeChatActivity>()
//                    else
                        title_content.postDelayed({
                            LoadDialog.cancle()
                            startActivity<MainActivity>()
                            finish()
                        }, 100)
                }) {
                    LoadDialog.cancle()
                }
            }
        }
    }

    private fun loginSuccess(type: String) {
        //友盟推送
        PushAgent.getInstance(this).setAlias(Statics.userMode!!.userid.toString(),Statics.UMPUSHTYPE
        ) { p0, p1 -> }
        //有米积分墙设置id
        OffersManager.getInstance(this).customUserId =
            Statics.userMode!!.userid.toString()
        //友盟统计
        MobclickAgent.onProfileSignIn(type, Statics.userMode!!.userid.toString())
        //发送登录成功
        WjEventBus.getInit().post(Codes.USERLOGIN, 0)

        //统计
        val music1 = HashMap<String, Any>()
        music1["userid"] = Statics.userMode!!.userid.toString()
        music1["type"] = type
        MobclickAgent.onEventObject(activity, UmEventCode.LOGIN_SUCCESS, music1)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mAnimator != null) {
            mAnimator!!.cancel()
        }
        WjEventBus.getInit().remove(Codes.LOGINFINSH)
        mAnimator = null
        title_content.handler.removeCallbacksAndMessages(null)
    }
}