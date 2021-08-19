package com.wj.makebai.ui.fragment

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import com.abase.util.GsonUtil
import com.wj.commonlib.http.HttpManager
import com.wj.commonlib.http.TaskHallRequests
import com.wj.commonlib.ui.ViewControl
import com.wj.commonlib.ui.weight.CustomLinearLayoutManager
import com.wj.ktutils.startActivity
import com.wj.makebai.R
import com.wj.makebai.ui.activity.appTask.TaskDetailActivity
import com.wj.makebai.ui.activity.base.MakeBaseFragment
import com.wj.makebai.ui.adapter.task.TaskStateListAdapter
import com.wj.makebai.ui.weight.NoData
import com.wj.makebai.utils.diffcallback.TaskListDiffCallback
import com.wj.ui.interfaces.RecyerViewItemListener
import kotlinx.android.synthetic.main.fragment_taskstatelist.*

/**
 * 任务状态列表
 * @author admin
 * @version 1.0
 * @date 2020/9/27
 */
class TaskStateListFragment(val type: Int) : MakeBaseFragment() {
    private var startId = -1
    private var adapter: TaskStateListAdapter? = null
    override fun setPageName(): String {
        return this::class.java.simpleName
    }

    override fun setContentView(): Int {
        super.setContentView()
        return R.layout.fragment_taskstatelist
    }

    override fun init() {
        super.init()
        refresh_layout.setOnRefreshListener {
            startId = -1
            getList()
        }
    }

    override fun onResume() {
        super.onResume()
        if (adapter == null) getList()
    }

    private fun getList() {
        if (adapter != null && adapter!!.isLoading) return
        adapter?.isLoading = true
        TaskHallRequests.taskStatus(startId, type, {
            if (adapter == null) {
                adapter = TaskStateListAdapter((it))
                recyclerView.adapter = adapter
                adapter!!.isFinish = it.isEmpty() || it.size < 10
                recyclerView.layoutManager = CustomLinearLayoutManager(activity)

                adapter!!.onItemClickListener = object : RecyerViewItemListener {
                    override fun click(view: View, position: Int) {
                        activity!!.startActivity<TaskDetailActivity>(
                            "data" to GsonUtil.gson2String(
                                adapter!!.list[position]
                            )
                        )
                    }
                }

                ViewControl.loadMore(activity!!, recyclerView, adapter!!) {
                    getList()
                }
            } else {
                if (startId == -1) {
                    adapter!!.list.clear()
                    adapter!!.list.addAll(it)
                    adapter!!.notifyItemRangeInserted(0, adapter!!.list.size - 1)
                } else
                    DiffUtil.calculateDiff(TaskListDiffCallback(adapter!!.list, it))
                        .dispatchUpdatesTo(adapter!!)
            }
            if (adapter!!.list.isNotEmpty()){
                noData.type=NoData.DataState.GONE
                startId = adapter!!.list[adapter!!.list.size - 1].idtask
            }
            else noData.type=NoData.DataState.NULL
        }) {
            refresh_layout.finishRefresh()
            adapter?.isLoading = false
        }
    }
}