package com.wj.makebai.ui.activity.user

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Handler
import android.os.Message
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.wj.commonlib.http.DzApi
import com.wj.commonlib.http.HttpManager
import com.wj.commonlib.statices.Statics
import com.wj.ktutils.isNull
import com.wj.ktutils.showTip
import com.wj.ktutils.startActivity
import com.wj.makebai.R
import com.wj.makebai.ui.activity.base.MakeActivity
import kotlinx.android.synthetic.main.activity_bind_phone.*

/**
 * 绑定手机号
 * @author admin
 * @version 1.0
 * @date 2020/8/26
 */
class BindPhoneActivity :MakeActivity(),View.OnClickListener{
    private var mHandler: Handler? = null

    override fun bindLayout(): Int {
        return R.layout.activity_bind_phone
    }

    override fun initData() {
        title_content.text = getString(R.string.bind_phone)
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
        btn_register.isEnabled =
            !TextUtils.isEmpty(phone) && !TextUtils.isEmpty(code)
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
        HttpManager.bindPhone(edit_phone.text.toString(),code, {
            Statics.userMode!!.username=phoneNum
            Statics.userMode!!.user_phone=phoneNum
            Statics.userMode=Statics.userMode!!
            DzApi.dzReg(Statics.userMode!!.apply { userPwd=phoneNum })
            showTip("绑定成功")
            RegisterActivity.mCount = RegisterActivity.TOTAL
            finish()
        }) {

        }
    }

    override fun onBackPressed() {
       AlertDialog.Builder(activity).setTitle("账号安全").setMessage("为了账号安全请绑定手机号").setNegativeButton("确定"
       ) { dialog, which -> dialog?.cancel() }.setPositiveButton("暂不绑定"
       ) { dialog, which -> finish() }.show()
    }

    override fun onDestroy() {
        if (Statics.userMode!!.usertype == 2 && Statics.userMode!!.user_phone.isNull())
            Statics.userMode=null
        super.onDestroy()
    }
}