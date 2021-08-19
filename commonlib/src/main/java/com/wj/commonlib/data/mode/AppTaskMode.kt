package com.wj.commonlib.data.mode

/**
 *
 * @author Administrator
 * @version 1.0
 * @date 2020/7/21
 */
data class AppTaskMode(
    val address: String,
    val idtask: Int,
    val idtaskrecord: Int,
    val task_count: Int,
    val task_creatime: String,
    val task_dcount: Int,
    val task_desc: String,
    val auditDesc: String,
    val task_end_date: String,
    val task_icon: String,
    val task_imagecount: Int,
    val task_info: Any,
    var task_name: String,
    val task_point: Double,
    val task_real_money: Double,
    val task_start_date: String,
    val task_state: Int,
    val task_steps: List<TaskStep>,
    val task_time: Int,
    val task_totalMoney: Int,
    val task_type: Int,
    val task_url: String,
    val userbalance: Int,
    val usericon: String,
    val userid: Int,
    val usermoney: Double,
    val username: String,
    val audit_state:Int,
    val userspendingtotal: Double,
    val withdrawal_total: Int,
    val state: String,
    val countSuccess: Int,
    val withdrawal_total_money: Double
)

data class TaskStep(
    val stepData: Any,
    val stepDesc: String,
    val stepType: Int,
    val taskId: Int,
    val taskStepId: Int,
    val stepCommit: Int,
    var isCommit: Boolean = false,
    var commitInfo: Any
)