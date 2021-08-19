package com.wj.commonlib.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.Window
import androidx.core.view.isVisible
import com.abase.util.AbAppUtil
import com.abase.util.AbDoubleTool
import com.tencent.bugly.beta.Beta
import com.tencent.bugly.beta.download.DownloadListener
import com.tencent.bugly.beta.download.DownloadTask
import com.wj.commonlib.R
import kotlinx.android.synthetic.main.commlib_update.*

/**
 *
 * @author admin
 * @version 1.0
 * @date 2021/2/18
 */
class UpgradeActivity : Activity() {
    private var isBack=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.commlib_update)

        /*获取下载任务，初始化界面信息*/updateBtn(Beta.getStrategyTask())
        version.text = Beta.getUpgradeInfo().versionName

        text_content.text = Beta.getUpgradeInfo().newFeature

        /*为下载按钮设置监听*/
        btn_update.setOnClickListener {
            val task: DownloadTask = Beta.startDownload()
            updateBtn(task)
        }
        btn_cancel.setOnClickListener{
            Beta.cancelDownload()
            finish()
        }

        /*注册下载监听，监听下载事件*/
        Beta.registerDownloadListener(object : DownloadListener {
            @SuppressLint("SetTextI18n")
            override fun onReceive(task: DownloadTask) {
                updateBtn(task)
                val progressNum =
                    AbDoubleTool.mul(AbDoubleTool.div(task.savedLength.toDouble(), task.totalLength.toDouble()), 100.0)
                @SuppressLint("SetTextI18n")
                progress.text = "正在下载 $progressNum%..."
                progress.isVisible=true
            }

            @SuppressLint("SetTextI18n")
            override fun onCompleted(task: DownloadTask) {
                updateBtn(task)
                progress.text = "点击安装"
                isBack=true
                AbAppUtil.installApk(this@UpgradeActivity,task.saveFile)
                progress.setOnClickListener{
                    AbAppUtil.installApk(this@UpgradeActivity,task.saveFile)
                }
            }

            override fun onFailed(task: DownloadTask, code: Int, extMsg: String?) {
                updateBtn(task)
                btn_update.text = "下载失败"
                progress.isVisible=false
            }
        })
    }

    override fun onBackPressed() {
        if(isBack)super.onBackPressed()
    }


    override fun onDestroy() {
        super.onDestroy()

        /*注销下载监听*/
        Beta.unregisterDownloadListener()
    }

    fun updateBtn(task: DownloadTask) {

        /*根据下载任务状态设置按钮*/
        when (task.status) {
            DownloadTask.INIT, DownloadTask.DELETED, DownloadTask.FAILED -> {
                btn_update.text = "开始下载"
            }
            DownloadTask.COMPLETE -> {
                btn_update.text = "安装"
            }
            DownloadTask.DOWNLOADING -> {
                btn_update.text = "暂停"
            }
            DownloadTask.PAUSED -> {
                btn_update.text = "继续下载"
            }
        }
    }
}