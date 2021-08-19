package com.wj.makebai.ui.activity.appTask

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.abase.util.GsonUtil
import com.bumptech.glide.request.RequestOptions
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.wj.commonlib.data.mode.AppTaskMode
import com.wj.commonlib.http.HttpManager
import com.wj.commonlib.http.TaskHallRequests
import com.wj.commonlib.http.Urls
import com.wj.commonlib.statices.Info
import com.wj.commonlib.statices.XmlConfigs
import com.wj.commonlib.ui.ViewControl
import com.wj.commonlib.utils.*
import com.wj.ktutils.WjSP
import com.wj.ktutils.showTip
import com.wj.ktutils.startActivity
import com.wj.makebai.R
import com.wj.makebai.ui.activity.base.MakeActivity
import com.wj.makebai.ui.activity.comm.WebActivity
import com.wj.makebai.ui.adapter.TaskStepAdapter
import com.xujiaji.happybubble.BubbleDialog
import kotlinx.android.synthetic.main.activity_taskdetail.*
import kotlinx.android.synthetic.main.dialog_task_rule.view.*
import kotlinx.coroutines.*
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat

/**
 * 任务详情
 * @author Administrator
 * @version 1.0
 * @date 2020/8/11
 */
class TaskDetailActivity : MakeActivity() {
    private var adapter: TaskStepAdapter? = null
    private var state = -1
    private var timeCount = 0L
    private val imgPath = HashMap<Int, ArrayList<String>>()
    private val imgUrl = HashMap<Int, ArrayList<String>>()
    private lateinit var mode: AppTaskMode
    private var runnable: Job? = null
    private var dialog: BubbleDialog? = null
    private var dialog1: Dialog? = null
    override fun bindLayout(): Int {
        return R.layout.activity_taskdetail
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun initData() {
        title_content.text = getString(R.string.taskDetail)
        other_icon.setImageResource(R.drawable.ic_task_rule)

        mode = GsonUtil.gson2Object(intent.getStringExtra("data"), AppTaskMode::class.java)
        glide.load(mode.task_icon).apply(RequestOptions().circleCrop()).into(task_icon)
        task_name.text = mode.task_name
        task_desc.text = mode.task_desc
        task_price.text = "+ ${(mode.task_point * Info.money2Points).toInt()}金币"

        other_icon.setOnClickListener {
            dialog?.cancel()
            dialog1?.cancel()
            startActivity<WebActivity>("url" to Urls.taskRule)
        }

        //任务提示
        if (WjSP.getInstance().getValues(XmlConfigs.TASKSTART, -1) == -1) taskTips()
        else //第一次打开需要显示引导
            if (WjSP.getInstance().getValues(XmlConfigs.G_TASKDETAIL, -1) == -1)
                other_icon.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                    override fun onGlobalLayout() {

                        dialog = bubbleDialog(activity, other_icon, "查看接单的规则")
                        WjSP.getInstance().setValues(XmlConfigs.G_TASKDETAIL, 0)
                        other_icon.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                })

        adapter = TaskStepAdapter(mode.task_steps)
        recyclerView.adapter = adapter
        recyclerView.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        TaskHallRequests.selectTask(mode.idtaskrecord) {
            state = it.state
            adapter!!.isGetTask = state <= 1
            adapter!!.notifyDataSetChanged()
            if (it.state == 0) timeCount =
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(it.taskTime)!!.time - System.currentTimeMillis()
            changState()
            if (it.taskInfo != null)
                for (step in adapter!!.taskSteps!!.indices) {
                    for (index in it.taskInfo!!.indices) {
                        if (it.taskInfo!![index].taskStepId == mode.task_steps[step].taskStepId) {
                            adapter!!.taskSteps!![step].isCommit = true
                            adapter!!.taskSteps!![step].commitInfo =
                                it.taskInfo!![index].taskStepInfo
                            adapter!!.notifyItemChanged(step)
                        }
                    }
                }
        }

        getTask.setOnClick {
            LoadDialog.show(activity)
            if (state == -1 || state == 5)
                TaskHallRequests.getTask(mode.idtask) {
                    if (it.state == 0) timeCount =
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(it.taskTime)!!.time - System.currentTimeMillis()
                    state = 0
                    changState()
                    showTip("领取成功")
                    LoadDialog.cancle()
                }
            else {
                //拼装任务信息
                LoadDialog.show(activity)
                for (item in adapter!!.submitInfo.keys) {
                    for (index in mode.task_steps) {
                        if (index.taskStepId == item) {
                            //遍历需要上传的图片信息
                            if (index.stepType == 0) {
                                imgPath[item] = adapter!!.submitInfo[item] as ArrayList<String>
                            }
                        }
                    }
                }
                if(imgPath.isNotEmpty())
                    for (path in imgPath.entries) {
                    upLoadFile(path.key, path.value)
                }
                else submitTask()
            }
        }

        cancelTask.setOnClick {
            AlertDialog.Builder(activity).setTitle("提示").setMessage("是否取消任务").setPositiveButton(
                "确定"
            ) { dialog, _ ->
                TaskHallRequests.taskCancel(mode.idtask) {
                    cancelTask.visibility = View.GONE
                    getTask.visibility = View.VISIBLE
                    getTask.text = getString(R.string.get_task)
                    getTask.isEnabled = true
                    time.visibility = View.GONE
                    state = -1
                    showTip("取消成功")
                    dialog.cancel()
                }
            }.setNegativeButton("取消") { _, _ -> }.show()
        }
    }

    /**
     * 任务步骤提示
     */
    private fun taskTips() {
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_task_rule, null)
        dialog1 = ViewControl.customAlertDialog(activity, view, null)
        view.task_rule.setOnClick {
            startActivity<WebActivity>("url" to Urls.taskRule)
        }
        view.task_start.setOnClick {
            dialog1?.cancel()
        }
        view.close.setOnClick{
            dialog1?.cancel()
        }
        dialog1?.setOnCancelListener {
            if(view.task_not_tips.isChecked)   WjSP.getInstance().setValues(XmlConfigs.TASKSTART, 0)
        }
    }

    private fun upLoadFile(key: Int, img: ArrayList<String>) {
        HttpManager.upload(img.toFiles()) {
            imgUrl[key] = arrayListOf(it)
            if (imgUrl.size == imgPath.size) {
                submitTask()
            }

        }
    }

    /**
     * 提交任务
     */
    private fun submitTask(){
        val array = JsonArray()

        var jsonObject: JsonObject
        for (item in adapter!!.submitInfo.keys) {
            for (index in mode.task_steps) {
                if (index.taskStepId == item) {
                    jsonObject = JsonObject()
                    jsonObject.addProperty("taskStepId", index.taskStepId)
                    jsonObject.addProperty("taskStepType", index.stepType)
                    jsonObject.addProperty("taskStepDesc", index.stepDesc)
                    jsonObject.add(
                        "taskStepInfo",
                        GsonUtil.getGson()
                            .toJsonTree(if (imgUrl.containsKey(item)) imgUrl[item] else adapter!!.submitInfo[item])
                    )

                    array.add(jsonObject)
                }
            }
        }
        imgPath.clear()
        TaskHallRequests.submitTask(mode.idtask, array) {
            LoadDialog.cancle()
            showTip("等待审核")
            finish()
        }
    }

    private fun ArrayList<String>.toFiles(): ArrayList<File> {
        val files = ArrayList<File>()
        for (index in this) {
            files.add(File(index))
        }
        return files
    }

    /**
     * 修改状态
     */
    private fun changState() {
        cancelTask.visibility = View.GONE
        time.visibility = View.GONE
        getTask.text =
            if (state == -1 || state == 5) {
                getString(R.string.get_task)
            } else {
                time()
                time.visibility = View.VISIBLE
                cancelTask.visibility = View.VISIBLE
                getString(R.string.submit_task)
            }
        getTask.setTextColor(resources.getColor(R.color.text))
        if (state == 1) {
            getTask.isEnabled = false
            getTask.text = "正在审核"
            getTask.setTextColor(resources.getColor(R.color.white))
            getTask.setBackgroundResource(R.color.gray)
        } else if (state == 4) {
            time.text = "已超时"
            getTask.visibility = View.GONE
        }
    }

    //开始倒计时
    @SuppressLint("SetTextI18n")
    private fun time() {
        if (state != 0) return
        runnable?.cancel()
        runnable = GlobalScope.launch(Dispatchers.IO) {
            while (true){
                timeCount--
                delay(1)
                launch(Dispatchers.Main) {
                    if (timeCount < 1) {
                        time.text = "已超时"
                        getTask.isEnabled = false
                        getTask.visibility = View.GONE
                        return@launch
                    }
                    val str = "请在 ${timeCount.getEndTime()} 内提交任务完成"
                    val span = SpannableStringBuilder(str)
                    span.setSpan(
                        ForegroundColorSpan(Color.parseColor("#CA1A1A")),
                        str.indexOf("在") + 1,
                        str.indexOf("内"),
                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                    )
                    time.text = span
                }
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun Long.getEndTime(): String {
        var strTime: String? = null
        // 按照传入的格式生成一个simpledateformate对象
        // 按照传入的格式生成一个simpledateformate对象
        val nd = 1000 * 24 * 60 * 60.toLong() // 一天的毫秒数
        val nh = 1000 * 60 * 60.toLong() // 一小时的毫秒数
        val nm = 1000 * 60.toLong() // 一分钟的毫秒数
        val ns: Long = 1000 // 一秒钟的毫秒数
        val diff = this
        var day = 0L
        try {
            day = diff / nd // 计算差多少天
            val hour = diff % nd / nh // 计算差多少小时
            val min = diff % nd % nh / nm // 计算差多少分钟 86,400
            val sec = diff % nd % nh % nm / ns // 计算差多少秒
            // 输出结果
            strTime =
                if (day > 0 || hour > 0 || sec > 0 || min > 0) {
                    /* day.toString() + "天" +*/ "${if (day < 1) "" else "$day 天"} ${if (hour < 10) "0$hour" else hour}:${if (min < 10) "0$min" else min}:${if (sec < 10) "0$sec" else sec}"
                } else {
                    "已超时"
                }
            return strTime
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return "已超时"
    }

    override fun onDestroy() {
        super.onDestroy()
        runnable?.cancel()
    }
}