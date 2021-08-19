package com.wj.makebai.ui.activity.appTask

import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.abase.util.GsonUtil
import com.wj.commonlib.data.mode.AppTaskMode
import com.wj.commonlib.data.mode.PageMode
import com.wj.commonlib.http.HttpManager
import com.wj.commonlib.http.Urls
import com.wj.commonlib.ui.ViewControl
import com.wj.makebai.R
import com.wj.makebai.data.emu.HomeEmu
import com.wj.makebai.data.mode.HomeTypeMode
import com.wj.makebai.ui.activity.base.MakeActivity
import com.wj.makebai.ui.adapter.task.TaskHallListAdapter
import com.wj.makebai.ui.weight.NoData
import kotlinx.android.synthetic.main.activity_searchtask.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * 搜索任务
 * @author admin
 * @version 1.0
 * @date 2020/11/19
 */
class SearchTaskActivity:MakeActivity() {
    private var page = 0
    private var startId = -1
    private var adapter: TaskHallListAdapter? = null
    private var isLoad=false

    override fun bindLayout(): Int {
        return R.layout.activity_searchtask
    }

    override fun initData() {
        title.visibility=View.GONE
        search_bar.onActionViewExpanded()
        search_bar.setOnQueryTextListener(object :SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                getData(true)
                search_bar.setQuery("",false)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        adapter = TaskHallListAdapter()
        task_list.adapter = adapter
        ViewControl.loadMore(activity,task_list,adapter!!){
            if(adapter!=null && !adapter!!.isFinish)getData(false)
        }
//        getData(true)
        noData.bindLifecycle(lifecycle)
    }
    private fun getData(isRefresh: Boolean){
        if(isLoad)return
        isLoad=true
        HttpManager.allList<AppTaskMode>(Urls.taskHall, page, startId,null, search_bar.query.toString(), {
            bindData(it,isRefresh)
            isLoad=false
        }, fun(code, _, _) {
            isLoad=false
            runOnUiThread {
                if (code == 503) {
                    noData?.type = NoData.DataState.REFRESH
                } else if (code == -1) {
                    noData?.type = NoData.DataState.NO_NETWORK
                }
            }
        })
    }
    /**
     * 绑定数据
     */
    private fun bindData(mode: PageMode<AppTaskMode>, isRefresh: Boolean){
        if (mode.list.isNotEmpty()) {//解决多重泛型问题，使用这个接口
            GlobalScope.launch(Dispatchers.IO) {
                mode.list = GsonUtil.Gson2ArryList(
                    GsonUtil.gson2String(mode.list),
                    Array<AppTaskMode>::class.java
                ) as ArrayList<AppTaskMode>


                launch(Dispatchers.Main) {
                    when {
                        adapter!!.list.size < 2 -> {
                            adapter!!.list.addAll(mode.list)
                            adapter!!.notifyItemRangeInserted(1, adapter!!.list.size - 1)
                        }
                        adapter!!.list.isNotEmpty() -> {

                            if (isRefresh) adapter!!.list.clear()
                            val oldPosition = adapter!!.list.size
                            adapter!!.list.addAll(mode.list)
                            if (isRefresh) adapter!!.notifyDataSetChanged()
                            else adapter!!.notifyItemRangeInserted(
                                oldPosition,
                                mode.list.size
                            )
                        }
                    }
                    adapter!!.isFinish = adapter!!.list.isNullOrEmpty() || page>=mode.pageCount-1 || mode.list.size<8
                    startId=mode.list[mode.list.size-1].idtask
                    page++
                }
            }
            noData?.type = NoData.DataState.GONE
        } else {
            noData?.type = NoData.DataState.NULL
            adapter?.list?.clear()
            adapter?.notifyDataSetChanged()
        }
    }
}