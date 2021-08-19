package com.wj.commonlib.data.mode

/**
 * 任务提交的信息
 * @author admin
 * @version 1.0
 * @date 2020/9/21
 */
data class TaskStepMode(
    val taskStepId: Int,
    val taskStepInfo: Any,
    val taskStepType: Int
)