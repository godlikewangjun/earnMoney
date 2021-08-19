package com.wj.makebai.utils.diffcallback

import androidx.recyclerview.widget.DiffUtil
import com.wj.commonlib.data.mode.AppTaskMode
import com.wj.makebai.data.mode.HomeTypeMode

/**
 * 任务比对
 * @author admin
 * @version 1.0
 * @date 2020/9/7
 */
class TaskDiffCallback() : DiffUtil.Callback() {

    private var oldData: List<HomeTypeMode>? = null
    private var newData: List<HomeTypeMode>? = null

    constructor(oldData: List<HomeTypeMode>, newData: List<HomeTypeMode>) : this() {
        this.oldData = oldData
        this.newData = newData
    }

    override fun getOldListSize(): Int {
        return oldData!!.size
    }

    override fun getNewListSize(): Int {
        return newData!!.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldUser = oldData!![oldItemPosition]
        val newUser = newData!![newItemPosition]
        return (oldUser.mode as AppTaskMode).idtask == (newUser.mode as AppTaskMode).idtask
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldUser = oldData!![oldItemPosition].mode as AppTaskMode
        val newUser = newData!![newItemPosition].mode as AppTaskMode

        return !(oldUser.task_name != newUser.task_name || oldUser.task_dcount != newUser.task_dcount || oldUser.countSuccess != newUser.countSuccess)
    }
}