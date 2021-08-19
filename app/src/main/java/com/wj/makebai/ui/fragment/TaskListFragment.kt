package com.wj.makebai.ui.fragment

import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import com.abase.util.GsonUtil
import com.wj.commonlib.data.mode.AppTaskMode
import com.wj.commonlib.data.mode.PageMode
import com.wj.commonlib.http.HttpManager
import com.wj.commonlib.http.Urls
import com.wj.commonlib.statices.EventCodes
import com.wj.commonlib.ui.ViewControl
import com.wj.commonlib.ui.weight.CustomLinearLayoutManager
import com.wj.eventbus.WjEventBus
import com.wj.ktutils.isNull
import com.wj.ktutils.runOnUiThread
import com.wj.makebai.R
import com.wj.makebai.statice.Codes
import com.wj.makebai.ui.activity.base.MakeBaseFragment
import com.wj.makebai.ui.adapter.task.TaskHallListAdapter
import com.wj.makebai.ui.weight.NoData
import kotlinx.android.synthetic.main.fragment_tasklist.*
import kotlinx.android.synthetic.main.fragment_tasklist.noData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * 审核任务列表
 * @author admin
 * @version 1.0
 * @date 2020/11/18
 */
class TaskListFragment() : MakeBaseFragment() {
    private var page = 0
    private var startId = -1
    private var key = ""
    private var adapter: TaskHallListAdapter? = null
    private var isLoad = false

    constructor(key: String) : this() {
        this.key = key
    }


    override fun setContentView(): Int {
        return R.layout.fragment_tasklist
    }

    override fun setPageName(): String {
        return this::class.java.simpleName
    }

    override fun init() {
        (task_hall.itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false

        adapter = TaskHallListAdapter()
        task_hall.adapter = adapter
        task_hall.layoutManager = CustomLinearLayoutManager(activity)

        noData.bindLifecycle(lifecycle)
    }

    /**
     * 加载更多
     */
    fun loadMore() {
        if (!adapter!!.isFinish) getData(false)
    }

    /**
     * 刷新
     */
    fun refreshLayout() {
        page = 0
        startId = -1
        getData(true)
    }

    override fun onResume() {
        super.onResume()
        if (adapter != null && adapter!!.list.isEmpty() || noData?.type != NoData.DataState.TASK_FINISH) getData(
            false
        )
    }

    /**
     * 获取数据
     */
    private fun getData(isRefresh: Boolean) {
        if (TaskHallFragment.list != null && key.isNull()) {
            bindData(TaskHallFragment.list!!, isRefresh)
            TaskHallFragment.list = null
            return
        }
        if (isLoad) return
        isLoad = true
        HttpManager.allList<AppTaskMode>(
            Urls.taskHall,
            page,
            startId,
            if (key.isNull()) null else key,
            null,
            {
                bindData(it, isRefresh)
                isLoad = false
            },
            fun(code, _, _) {
                isLoad = false
                runOnUiThread {
                    if (code == 503) {
                        noData?.type = NoData.DataState.REFRESH
                        task_hall.visibility = View.GONE
                    } else if (code == -1) {
                        noData?.type = NoData.DataState.NO_NETWORK
                        task_hall.visibility = View.GONE
                    }
                }
            })
    }

    /**
     * 绑定数据
     */
    private fun bindData(mode: PageMode<AppTaskMode>, isRefresh: Boolean) {
        if (mode.list.isNotEmpty()) {//解决多重泛型问题，使用这个接口
            GlobalScope.launch(Dispatchers.IO) {
                mode.list = GsonUtil.Gson2ArryList(
                    GsonUtil.gson2String(mode.list),
                    Array<AppTaskMode>::class.java
                ) as ArrayList<AppTaskMode>

                launch(Dispatchers.Main) {
                    if (isRefresh) {
                        adapter!!.list = mode.list
                        adapter!!.notifyDataSetChanged()
                    } else {
                        val oldPosition = adapter!!.list.size
                        adapter!!.list.addAll(mode.list)
                        adapter!!.notifyItemRangeInserted(
                            oldPosition,
                            mode.list.size
                        )
                    }
                    adapter!!.isFinish =
                        adapter!!.list.isNullOrEmpty() || page >= mode.pageCount - 1 || mode.list.size < 10
                    startId = mode.list[mode.list.size - 1].idtask
                    page++
                }
            }
            noData?.type = NoData.DataState.GONE
            task_hall.visibility = View.VISIBLE
//            WjEventBus.getInit().post(Codes.RESHFINSH,0)
        } else if (adapter != null && adapter!!.list.isNullOrEmpty()) {
            noData?.type = NoData.DataState.TASK_FINISH
            task_hall.visibility = View.GONE
//            WjEventBus.getInit().post(Codes.RESHFINSH,0)
        }
    }
}