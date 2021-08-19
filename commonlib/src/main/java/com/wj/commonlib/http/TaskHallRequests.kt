package com.wj.commonlib.http

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.wj.commonlib.data.mode.AppTaskMode
import com.wj.commonlib.data.mode.TaskRecodeMode
import com.wj.commonlib.data.mode.TaskTypeMode
import com.wj.commonlib.statices.Statics
import com.wj.ktutils.HttpRequests
import com.wj.ktutils.http
import com.wj.ktutils.isNull

/**
 * 任务大厅
 * @author Administrator
 * @version 1.0
 * @date 2020/7/15
 */
object TaskHallRequests {
    /**
     * 领取审核任务
     */
    fun getTask(taskId: Int, success_: (TaskRecodeMode) -> Unit) {
        val json = JsonObject()
        json.addProperty("taskId", taskId)
        Urls.getTask.post(HttpManager.getParams(json.toString()), success_)
    }

    /**
     * 获取审核任务类型列表
     */
    fun getTaskType(success_: (ArrayList<TaskTypeMode>) -> Unit) {
        Urls.taskTypeHall.post(HttpManager.getParams(null), success_)
    }

    /**
     * 查询审核任务状态
     */
    fun selectTask(taskId: Int, success_: (TaskRecodeMode) -> Unit) {
        val json = JsonObject()
        json.addProperty("idtaskrecord", taskId)
        http {
            url = Urls.selectTask
            requestType = HttpRequests.POST
            ohhttpparams = HttpManager.getParams(json.toString())
            success = {
                if ((it as String).isNull()) success_(TaskRecodeMode::class.java.newInstance()) else
                    HttpManager.getResult<TaskRecodeMode>(it, {}) {
                        success_(it)
                    }
            }
        }
    }

    /**
     * 提交审核任务
     * @taskInfo 需要传入步骤的id和类型，还有值
     */
    fun submitTask(taskId: Int,taskInfo: JsonArray, success_: (String) -> Unit) {
        val json = JsonObject()
        json.addProperty("taskId", taskId)
        json.add("taskInfo", taskInfo)
        Urls.submitTask.post(HttpManager.getParams(json.toString()), success_)
    }
    /**
     * 取消任务
     */
    fun taskCancel(taskId: Int,success_: (String) -> Unit) {
        val json = JsonObject()
        json.addProperty("taskId", taskId)
        Urls.taskCancel.post(HttpManager.getParams(json.toString()), success_)
    }

    /**
     * 获取任务审核状态列表
     */
    fun taskStatus(startId: Int, type: Int, success_: (ArrayList<AppTaskMode>) -> Unit, finish_: () -> Unit) {
        val json = JsonObject()
        json.addProperty("userId", Statics.userMode?.userid)
        json.addProperty("pageSize",10)
        json.addProperty("startId",startId)
        json.addProperty("audit_state",type)

        Urls.taskStatus.post(
            HttpManager.getParams(if (Statics.userMode != null) json.toString() else null), success_)
    }
}