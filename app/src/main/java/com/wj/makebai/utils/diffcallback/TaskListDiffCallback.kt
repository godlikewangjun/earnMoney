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
class TaskListDiffCallback() : DiffUtil.Callback() {

    private var oldData: List<AppTaskMode>? = null
    private var newData: List<AppTaskMode>? = null
    constructor(oldData: List<AppTaskMode>, newData: List<AppTaskMode>):this(){
        this.oldData=oldData
        this.newData=newData
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
        return oldUser.idtask == newUser.idtask
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldUser = oldData!![oldItemPosition]
        val newUser = newData!![newItemPosition]
        return oldUser == newUser
    }
}