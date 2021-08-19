package com.wj.makebai.ui.control

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.abase.util.AbDoubleTool
import com.abase.util.AbWifiUtil
import com.abase.view.weight.MyDialog
import com.bumptech.glide.Glide
import com.wj.commonlib.http.HttpManager
import com.wj.commonlib.statices.Statics
import com.wj.commonlib.ui.ViewControl
import com.wj.commonlib.utils.CommTools
import com.wj.eventbus.WjEventBus
import com.wj.ktutils.isNull
import com.wj.ktutils.runOnUiThread
import com.wj.ktutils.showTip
import com.wj.makebai.R
import com.wj.makebai.statice.Codes
import com.wj.makebai.statice.StaticData
import com.wj.makebai.ui.weight.CustomProgressBar
import ebz.lsds.qamj.os.df.*


/**
 *
 * @author Admin
 * @version 1.0
 * @date 2018/7/27
 */
object TaskConctor {

    /**
     *做任务控制
     */
    fun cpaDialog(context: Context, mode: Any): AlertDialog {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.taskdialog_layout, null)
        val popAlertDialog = ViewControl.customAlertDialog(context, dialogView, null)
        var appIcon = ""
        var appName = ""
        var appSize = ""
        var appPrice = ""
        var appSignPrice = ""
        var taskDesc = ""
        if (mode is AppSummaryObject) {
            appIcon = mode.iconUrl
            appName = mode.appName
            appSize = mode.appSize
            appPrice =  AbDoubleTool.mul(
                mode.points.toDouble(),
                StaticData.initMode!!.scale
            ).toString()
            taskDesc = mode.taskSteps
            DiyOfferWallManager.getInstance(context).loadAppDetailData(mode, object :
                AppDetailDataInterface {
                override fun onLoadAppDetailDataFailedWithErrorCode(p0: Int) {
                }

                override fun onLoadAppDetailDataFailed() {
                }

                override fun onLoadAppDetailDataSuccess(p0: Context?, p1: AppDetailObject?) {

                    var point = 0
                    for (index in 0 until p1!!.extraTaskList!!.size()) {
                        point += p1.extraTaskList!![index].points
                    }
                    appSignPrice = AbDoubleTool.mul(
                        point.toDouble(),
                        StaticData.initMode!!.scale
                    ).toString()

                    context.runOnUiThread {
                        if (!appSignPrice.isNull()) {
                            dialogView.findViewById<TextView>(R.id.ad_priced_).text =
                                Html.fromHtml(",签到<font color='#EE4646'>$appSignPrice</font>积分")
                            dialogView.findViewById<TextView>(R.id.ad_priced_).visibility =
                                View.VISIBLE
                        } else {
                            dialogView.findViewById<TextView>(R.id.ad_priced_).visibility =
                                View.GONE
                        }
                    }
                }
            })
            val icon = dialogView.findViewById<ImageView>(R.id.ad_icon)
            Glide.with(context).load(appIcon).into(icon)
            dialogView.findViewById<View>(R.id.close).setOnClickListener {
                popAlertDialog.cancel()
            }
            dialogView.findViewById<TextView>(R.id.ad_name).text = appName
            dialogView.findViewById<TextView>(R.id.ad_size).text = appSize
            dialogView.findViewById<TextView>(R.id.ad_priced).text =
                Html.fromHtml("安装<font color='#EE4646'>$appPrice</font>积分")
            if (!appSignPrice.isNull()) {
                dialogView.findViewById<TextView>(R.id.ad_priced_).text =
                    Html.fromHtml(",签到<font color='#EE4646'>$appSignPrice</font>积分")
                dialogView.findViewById<TextView>(R.id.ad_priced_).visibility = View.VISIBLE
            } else {
                dialogView.findViewById<TextView>(R.id.ad_priced_).visibility = View.GONE
            }

            dialogView.findViewById<TextView>(R.id.ad_ms).text = Html.fromHtml(taskDesc).toString()
            popAlertDialog.setOnDismissListener {
            }
            yoimiControl(context, dialogView, mode, popAlertDialog)
        }
        return popAlertDialog
    }

    /**
     *做签到任务控制
     */
    fun cpaSignDialog(context: Context, position: Int?, mode: Any): AlertDialog {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.taskdialog_layout, null)
        val popAlertDialog = ViewControl.customAlertDialog(context, dialogView, null)
        var appIcon = ""
        var appName = ""
        var appSize = ""
        var appPrice = ""
        var taskDesc = ""
        if (mode is AppSummaryObject) {
            appIcon = mode.iconUrl
            appName = mode.appName
            appSize = mode.appSize
            appPrice = if (position != null)
                CommTools.getPrice(mode.extraTaskList[position].points.toDouble())
            else
                CommTools.getPrice(mode.extraTaskList[0].points.toDouble())
            taskDesc = if (position != null)
                mode.extraTaskList[position].adText
            else
                mode.extraTaskList[0].adText
            yoimiControl(context, dialogView, mode, popAlertDialog)
        }

        val icon = dialogView.findViewById<ImageView>(R.id.ad_icon)
        Glide.with(context).load(appIcon).into(icon)
        dialogView.findViewById<View>(R.id.close).setOnClickListener {
            popAlertDialog.cancel()
        }
        dialogView.findViewById<TextView>(R.id.ad_name).text = appName
        dialogView.findViewById<TextView>(R.id.ad_size).text = appSize
        dialogView.findViewById<TextView>(R.id.ad_priced).text =
            Html.fromHtml("签到<font color='#EE4646'>$appPrice</font>积分")

        dialogView.findViewById<TextView>(R.id.ad_ms).text = Html.fromHtml(taskDesc).toString()
        popAlertDialog.setOnDismissListener() {
        }
        return popAlertDialog
    }

    /**
     * 下载进度条控制
     */
    fun yoimiControl(
        context: Context,
        view: View,
        appDetailObject: AppSummaryObject,
        dialog: AlertDialog?
    ) {
        if (Statics.userMode == null) {
            context.showTip("登录之后才能获得积分哦！！")
        }
        val hander = Handler()
        val down = view.findViewById<Button>(R.id.down)
        val progressbar = view.findViewById<CustomProgressBar>(R.id.progress)

        val downListener = object : DiyAppNotify {
            override fun onDownloadStart(p0: Int) {
                val state = appDetailObject.adTaskStatus
                if (state == AdTaskStatus.NOT_COMPLETE) {
                    down.text = "返回继续任务"
                    down.isEnabled = true
                } else {
                    down.text = "正在申请任务..."
                    down.isEnabled = false
                }
            }

            override fun onDownloadFailed(p0: Int) {
                down.text = "任务抢光了"
                down.isEnabled = true
                down.setOnClickListener {
                    if (dialog != null) {
                        try {
                            dialog.cancel()
                        } catch (e: Exception) {
                        }
                    }
                }
            }

            override fun onDownloadProgressUpdate(p0: Int, p1: Long, p2: Long, p3: Int, p4: Long) {
                down.visibility = View.GONE
                progressbar.visibility = View.VISIBLE
                progressbar.progress = p3
                progressbar.setState(CustomProgressBar.STATE_DOWNLOADING)
            }

            override fun onInstallSuccess(p0: Int) {
                down.text = "返回继续任务"
                down.isEnabled = true
            }

            override fun onDownloadSuccess(p0: Int) {
                down.visibility = View.VISIBLE
                progressbar.visibility = View.GONE
                down.text = "点击安装"
                down.isEnabled = true
            }

        }
        down.setOnClickListener {
            if (!AbWifiUtil.isConnectivity(context)) {
                return@setOnClickListener
            }
            if (down.text.toString() == "完成任务") {
                dialog!!.cancel()
                return@setOnClickListener
            }
            down.text = "返回继续任务"

            DiyOfferWallManager.getInstance(context)
                .openOrDownloadApp(context as Activity?, appDetailObject)

            DiyOfferWallManager.getInstance(context).removeListener(downListener)
            DiyOfferWallManager.getInstance(context).registerListener(downListener)

            hander.postDelayed(object : Runnable {
                override fun run() {
                    if (appDetailObject.adId != null)
                        HttpManager.youmiDetail(appDetailObject.adId) {
                            if (it == "success") {
                                try {
                                    if (!dialog!!.isShowing) {
                                        return@youmiDetail
                                    }
                                } catch (e: Exception) {
                                    return@youmiDetail
                                }
                                context.runOnUiThread {
                                    down.text = "完成任务"
                                    down.isEnabled = true
                                    context.showTip("完成任务")
                                    down.setOnClickListener {
                                        dialog.cancel()
                                    }
                                    hander.removeCallbacksAndMessages(null)
                                    WjEventBus.getInit()
                                        .post(Codes.TASKFINSH, appDetailObject.adId)
                                    progressbar.visibility = View.GONE
                                }
                            } else hander.postDelayed(this, 1000 * 30)
                        }
                }
            }, 1000 * 30)

        }
        dialog?.setOnCancelListener {

            hander.removeCallbacksAndMessages(null)
            DiyOfferWallManager.getInstance(context).removeListener(downListener)
        }
    }

}