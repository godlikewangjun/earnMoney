package com.wj.commonlib.data.mode

/**
 * 审核任务的记录
 * @author admin
 * @version 1.0
 * @date 2020/9/16
 */
data class TaskRecodeMode(
    val gotTime: String,
    val idtaskrecord: Int,
    val state: Int,
    val taskId: Int,
    val taskTime: String,
    val userId: Int,
    val taskInfo:List<TaskStepMode>?
)