package com.wj.makebai.ui.activity.user

import android.annotation.SuppressLint
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.abase.util.Tools
import com.tencent.bugly.beta.Beta
import com.wj.commonlib.http.HttpManager
import com.wj.commonlib.http.Urls
import com.wj.commonlib.ui.ViewControl
import com.wj.commonlib.utils.LoadDialog
import com.wj.ktutils.showTip
import com.wj.ktutils.startActivity
import com.wj.makebai.R
import com.wj.makebai.ui.activity.base.MakeActivity
import com.wj.makebai.ui.activity.comm.WebActivity
import kotlinx.android.synthetic.main.activity_about.*
import java.text.SimpleDateFormat
import java.util.*


/**
 * 关于，展示协议和APP、公司信息
 * @author dchain
 * @version 1.0
 * @date 2019/10/11
 */
class AboutActivity :MakeActivity(),View.OnClickListener{
    override fun bindLayout(): Int {
        return R.layout.activity_about
    }

    @SuppressLint("SimpleDateFormat")
    override fun initData() {

        version.text=Tools.getAppVersionName(activity)

        copy_right.text=String.format(getString(R.string.copy_right,SimpleDateFormat("yyyy").format(Date())))

        user_rule.setOnClickListener(this)
        user_privacy.setOnClickListener(this)
        about.setOnClickListener(this)
        textView.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.user_rule->{
                startActivity<WebActivity>("url" to Urls.userRule)
            }
            R.id.user_privacy->{
                startActivity<WebActivity>("url" to Urls.userPrivacyRule)
            }
            R.id.about->{
                AlertDialog.Builder(activity).setTitle("介绍").setMessage("致力于构建打发闲暇时间，又可以赚钱的实用工具APP").setPositiveButton("确定"
                ) { _, _ -> }.show()
            }
            R.id.textView -> {//更新
                LoadDialog.show(activity!!)
                //更新
                HttpManager.update({
                    ViewControl.update(activity!!, it, false)
                }, fun(_, _, _) {
                    activity!!.runOnUiThread {
                        showTip("当前为最新版本")
                    }
                }) {
                    LoadDialog.cancle()
                }
            }
        }
    }
}