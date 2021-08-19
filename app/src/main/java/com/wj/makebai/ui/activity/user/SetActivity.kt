package com.wj.makebai.ui.activity.user

import android.annotation.SuppressLint
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import com.abase.util.AbDoubleTool
import com.abase.util.AbFileUtil
import com.umeng.message.IUmengCallback
import com.umeng.message.PushAgent
import com.wj.commonlib.http.HttpManager
import com.wj.commonlib.statices.Statics
import com.wj.commonlib.utils.LoadDialog
import com.wj.eventbus.WjEventBus
import com.wj.ktutils.WjSP
import com.wj.ktutils.showTip
import com.wj.ktutils.startActivity
import com.wj.makebai.R
import com.wj.makebai.statice.Codes
import com.wj.makebai.statice.XmlCodes
import com.wj.makebai.ui.activity.base.MakeActivity
import com.wj.makebai.utils.MbTools
import kotlinx.android.synthetic.main.activity_set.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File


/**
 * 设置
 * @author dchain
 * @version 1.0
 * @date 2019/10/11
 */
class SetActivity : MakeActivity(), View.OnClickListener {
    override fun bindLayout(): Int {
        return R.layout.activity_set
    }

    @SuppressLint("SetTextI18n")
    override fun initData() {
        title_content.text = getString(R.string.setting)


        GlobalScope.launch(Dispatchers.IO) {
            val size = AbDoubleTool.div(
                getFolderSize(cacheDir).toDouble(),
                1024.0 * 1024
            )
            runOnUiThread {
                cache_size.text = "$size M"
            }
        }


        val enable =
            WjSP.getInstance().getValues<Boolean>(XmlCodes.PUSH_STATE, true)
        push.isChecked = enable
        kg(enable)

        clear_cache.setOnClickListener(this)
        push.setOnCheckedChangeListener { _, isChecked ->
            kg(isChecked)
        }

        light_dark.isChecked = WjSP.getInstance().getValues(XmlCodes.APP_DN, false)
        if (light_dark.isChecked) light_dark.text =
            getString(R.string.night_mode) else light_dark.text =
            getString(R.string.day_mode)
        light_dark.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                WjSP.getInstance().setValues(XmlCodes.APP_DN, true)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)//夜间模式
                light_dark.text = getString(R.string.night_mode)
                light_dark.setCompoundDrawables(
                    resources.getDrawable(R.drawable.ic_dark),
                    null,
                    null,
                    null
                )
            } else {
                WjSP.getInstance().setValues(XmlCodes.APP_DN, false)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)//白天模式
                light_dark.text = getString(R.string.day_mode)
                light_dark.setCompoundDrawables(
                    resources.getDrawable(R.drawable.ic_day),
                    null,
                    null,
                    null
                )
            }
            recreate()
        }

        if (Statics.userMode != null) {
            login_out.visibility = View.VISIBLE
        } else {
            login_out.visibility = View.GONE
        }

        login_out.setOnClickListener(this)
    }

    /**
     * 开关状态切换
     */
    private fun kg(isChecked: Boolean) {
        if (isChecked) {
            push.text = String.format(getString(R.string.push_kg), getString(R.string.open))
            PushAgent.getInstance(activity).enable(object : IUmengCallback {
                override fun onSuccess() {
                }

                override fun onFailure(p0: String?, p1: String?) {
                }
            })
        } else {
            push.text = String.format(getString(R.string.push_kg), getString(R.string.close))
            PushAgent.getInstance(activity).disable(object : IUmengCallback {
                override fun onSuccess() {
                }

                override fun onFailure(p0: String?, p1: String?) {
                }
            })
        }
    }

    /**
     * 获取文件夹大小
     * @param file File实例
     * @return long
     */
    private fun getFolderSize(file: File): Long {
        var size: Long = 0
        try {
            val fileList = file.listFiles()
            if(fileList!=null)for (i in fileList.indices) {
                size += if (fileList[i].isDirectory) {
                    getFolderSize(fileList[i])
                } else {
                    fileList[i].length()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return size
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.clear_cache -> {
                LoadDialog.show(activity)
                GlobalScope.launch(Dispatchers.IO) {
                    AbFileUtil.deleteFile(cacheDir)
                    runOnUiThread {
                        cache_size.text = "0 M"
                        LoadDialog.cancle()
                        showTip(getString(R.string.clearSuccess))
                    }
                }
            }
            R.id.login_out -> {//退出登录
                HttpManager.loginOut {
                    MbTools.loginOut()
                    WjEventBus.getInit().post(Codes.USERLOGOUT, 0)
                    login_out.visibility = View.GONE
                    showTip(getString(R.string.loginOutSuccess))

                    startActivity<LoginActivity>()
//                    TIMManager.getInstance().logout(object : TIMCallBack {
//                        override fun onSuccess() {
//
//                        }
//
//                        override fun onError(p0: Int, p1: String?) {
//                        }
//                    })
                }
            }
        }
    }
}