package com.wj.makebai.ui.activity.user

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Message
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import com.umeng.analytics.MobclickAgent
import com.wj.commonlib.http.DzApi
import com.wj.commonlib.http.HttpManager
import com.wj.commonlib.statices.Statics
import com.wj.eventbus.WjEventBus
import com.wj.ktutils.isNull
import com.wj.ktutils.showTip
import com.wj.ktutils.startActivity
import com.wj.makebai.R
import com.wj.makebai.statice.Codes
import com.wj.makebai.statice.UmEventCode
import com.wj.makebai.ui.activity.base.MakeActivity
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.fragment_user.*

/**
 * 注册
 * @author admin
 * @version 1.0
 * @date 2020/8/26
 */
class RegisterActivity : MakeActivity(), View.OnClickListener {
    private var mHandler: Handler? = null

    companion object {
        val TOTAL = 60
        var mCount = TOTAL
    }

    override fun bindLayout(): Int {
        return R.layout.activity_register
    }

    override fun initData() {
        title_content.text = getString(R.string.reg_register)
        btn_code.setOnClickListener(this)
        btn_register.setOnClickListener(this)


        edit_phone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (mCount != TOTAL) btn_code.isEnabled = s.length == 11
                changeEnable()
            }

            override fun afterTextChanged(s: Editable) {}
        })
        val textWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                changeEnable()
            }

            override fun afterTextChanged(s: Editable) {}
        }
        edit_code.addTextChangedListener(textWatcher)
        edit_pwd_1.addTextChangedListener(textWatcher)
        edit_pwd_2.addTextChangedListener(textWatcher)
        mHandler = @SuppressLint("HandlerLeak")
        object : Handler() {
            override fun handleMessage(msg: Message) {
                mCount--
                if (mCount > 0) {
                    btn_code.text = getString(R.string.reg_get_code_again) + "(" + mCount + "s)"
                    if (mHandler != null) {
                        mHandler!!.sendEmptyMessageDelayed(0, 1000)
                    }
                    btn_code.isEnabled = false
                } else {
                    btn_code.text = getString(R.string.reg_get_code)
                    mCount = TOTAL
                    btn_code.isEnabled = true
                    mHandler!!.removeCallbacksAndMessages(null)
                }
            }
        }

        if (mCount != TOTAL) mHandler!!.sendEmptyMessage(0)

        //统计
        val music = HashMap<String, Any>()
        MobclickAgent.onEventObject(activity, UmEventCode.REG_VIEW, music)
    }

    private fun changeEnable() {
        val phone: String = edit_phone.text.toString()
        val code: String = edit_code.text.toString()
        val pwd1: String = edit_pwd_1.text.toString()
        val pwd2: String = edit_pwd_2.text.toString()
        btn_register.isEnabled =
            !TextUtils.isEmpty(phone) && !TextUtils.isEmpty(code) && !TextUtils.isEmpty(
                pwd1
            ) && !TextUtils.isEmpty(pwd2)
    }

    override fun onClick(p0: View?) {
        when (p0!!.id) {
            R.id.btn_code -> getCode()
            R.id.btn_register -> register()

        }
    }

    /**
     * 获取验证码
     */
    private fun getCode() {
        val phoneNum: String = edit_phone.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(phoneNum)) {
            showTip(getString(R.string.reg_input_phone))
            edit_phone.requestFocus()
            return
        }
        HttpManager.sendSms(phoneNum,{
            showTip("发送成功")
            edit_code.requestFocus()
            mHandler!!.sendEmptyMessage(0)
        }){

        }
    }

    /**
     * 注册并登陆
     */
    private fun register() {
        val phoneNum: String = edit_phone.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(phoneNum)) {
            showTip(getString(R.string.reg_input_phone))
            edit_phone.requestFocus()
            return
        }
        if (phoneNum.length != 11) {
            showTip(getString(R.string.phone_error))
            edit_phone.requestFocus()
            return
        }
        val code: String = edit_code.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(code)) {
            showTip(getString(R.string.reg_input_code))
            edit_code.requestFocus()
            return
        }
        val pwd: String = edit_pwd_1.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(pwd)) {
            showTip(getString(R.string.reg_input_pwd_1))
            edit_pwd_1.requestFocus()
            return
        }
        val pwd2: String = edit_pwd_2.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(pwd2)) {
            showTip(getString(R.string.reg_input_pwd_2))
            edit_pwd_2.requestFocus()
            return
        }
        if (pwd != pwd2) {
            showTip(getString(R.string.reg_pwd_error))
            edit_pwd_2.requestFocus()
            return
        }
        //统计
        val music = HashMap<String, Any>()
        music["phoneNum"] =  phoneNum
        music["type"] =  "手机"
        MobclickAgent.onEventObject(activity, UmEventCode.REG_CLICK, music)

        HttpManager.userRegister(edit_phone.text.toString(), edit_pwd_1.text.toString(),code, {
            Statics.userMode=it
            mCount = TOTAL
            HttpManager.userDetail({
                Statics.userMode=it
                val music1 = HashMap<String, Any>()
                music1["userid"] =   Statics.userMode!!.userid.toString()
                music["phoneNum"] =  phoneNum
                music["type"] =  "手机"
                MobclickAgent.onEventObject(activity, UmEventCode.REG_SUCCESS, music1)

                DzApi.dzReg(Statics.userMode!!.apply { userPwd= pwd2})

                WjEventBus.getInit().post(Codes.LOGINFINSH,0)

                showTip("注册成功")

                startActivity<BindWeChatActivity>()

                finish()
            }) { _, _, _ ->
            }

        }) {

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler!!.removeCallbacksAndMessages(null)
    }
}