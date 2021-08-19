package com.wj.makebai.ui.activity.comm

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.wj.commonlib.data.mode.Message
import com.wj.commonlib.http.HttpManager
import com.wj.commonlib.ui.ViewControl
import com.wj.makebai.R
import com.wj.makebai.ui.activity.base.MakeActivity
import com.wj.makebai.ui.adapter.MessageAdapter
import com.wj.makebai.ui.weight.NoData
import kotlinx.android.synthetic.main.activity_message.*

/**
 * 消息列表
 * @author Administrator
 * @version 1.0
 * @date 2019/12/25
 */
class MessageActivity : MakeActivity() {
    private var startId = Int.MAX_VALUE
    private var mAdapter: MessageAdapter? = null
    private var isLoad = false
    override fun bindLayout(): Int {
        return R.layout.activity_message
    }

    override fun initData() {
        title_content.text = getString(R.string.msg_tips)
        title_line.visibility = View.VISIBLE
        refreshLayout.setEnableLoadMore(false)
        refreshLayout.setOnRefreshListener {
            isLoad = false
            startId = Int.MAX_VALUE
            mAdapter?.isFinish = false
            noData?.type = NoData.DataState.GONE

            loadData()
        }

        loadData()
    }

    private fun loadData() {
        if (mAdapter != null && mAdapter!!.isFinish) return
        if (isLoad) return
        isLoad = true
        HttpManager.announcements(
            startId,
            if (!intent.hasExtra("type")) null else intent.getIntExtra("type", 0),
            {
                if (it.isNotEmpty()) {
                    setData(it)
                } else if (mAdapter != null && mAdapter!!.list.size < 1 || mAdapter == null) {
                    noData?.type = NoData.DataState.NULL
                    mAdapter?.isFinish = true
                } else if (mAdapter != null) {
                    mAdapter?.isFinish = true
                }
            }) {
            refreshLayout.finishRefresh()
            isLoad = false
        }
    }

    /**
     * 设置数据
     */
    private fun setData(mode: ArrayList<Message>) {
        if (recyclerView.adapter == null) {
            mAdapter = MessageAdapter(mode)
            recyclerView.adapter = mAdapter
            recyclerView.layoutManager = LinearLayoutManager(activity)
            ViewControl.loadMore(activity!!, recyclerView!!, mAdapter!!) {
                startId =
                    mAdapter!!.list[mAdapter!!.list.size - 1].messageid
                loadData()
            }
        } else {
            if (startId == -1) mAdapter?.list?.clear()
            mAdapter!!.list.addAll(mode)
            mAdapter!!.notifyDataSetChanged()
        }
    }
}