package com.wj.makebai.ui.activity.user

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Message
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import com.wj.commonlib.http.HttpManager
import com.wj.ktutils.showTip
import com.wj.makebai.R
import com.wj.makebai.ui.activity.base.MakeActivity
import kotlinx.android.synthetic.main.activity_find_pwd.*
import kotlinx.android.synthetic.main.activity_find_pwd.btn_code
import kotlinx.android.synthetic.main.activity_find_pwd.btn_register
import kotlinx.android.synthetic.main.activity_find_pwd.edit_code
import kotlinx.android.synthetic.main.activity_find_pwd.edit_phone
import kotlinx.android.synthetic.main.activity_find_pwd.edit_pwd_1
import kotlinx.android.synthetic.main.activity_find_pwd.edit_pwd_2
import kotlinx.android.synthetic.main.activity_register.*

/**
 * 找回密码
 * @author admin
 * @version 1.0
 * @date 2020/8/26
 */
class FindPwdActivity :MakeActivity(),View.OnClickListener{
    private var mHandler: Handler? = null

    override fun bindLayout(): Int {
        return R.layout.activity_find_pwd
    }

    override fun initData() {
        title_content.text = getString(R.string.find_pwd)
        btn_code.setOnClickListener(this)
        btn_register.setOnClickListener(this)


        edit_phone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (RegisterActivity.mCount != RegisterActivity.TOTAL) btn_code.isEnabled = s.length == 11
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
                RegisterActivity.mCount--
                if (RegisterActivity.mCount > 0) {
                    btn_code.text = getString(R.string.reg_get_code_again) + "(" + RegisterActivity.mCount + "s)"
                    if (mHandler != null) {
                        mHandler!!.sendEmptyMessageDelayed(0, 1000)
                    }
                    btn_code.isEnabled = false
                } else {
                    btn_code.text = getString(R.string.reg_get_code)
                    RegisterActivity.mCount = RegisterActivity.TOTAL
                    btn_code.isEnabled = true
                }
            }
        }

        if (RegisterActivity.mCount != RegisterActivity.TOTAL) mHandler!!.sendEmptyMessage(0)

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
        HttpManager.findPwd(edit_phone.text.toString(), edit_pwd_1.text.toString(),code, {
            showTip("修改成功")
            RegisterActivity.mCount = RegisterActivity.TOTAL
            finish()
        }) {

        }
    }
}