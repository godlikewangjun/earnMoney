package com.wj.makebai.ui.activity.comm

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.wj.commonlib.http.HttpManager
import com.wj.commonlib.statices.Statics
import com.wj.makebai.R
import com.wj.makebai.ui.activity.base.MakeActivity
import com.wj.makebai.ui.adapter.WithdrawaListAdapter
import kotlinx.android.synthetic.main.activity_auditlist.*

/**
 * 审核列表
 * @author Administrator
 * @version 1.0
 * @date 2019/11/29
 */
class AuditListActivity :MakeActivity(){
    override fun bindLayout(): Int {
        return R.layout.activity_auditlist
    }

    override fun initData() {
        title_content.text=getString(R.string.audit_list)

        if(Statics.userMode!=null)HttpManager.withdrawList(null) {
            if (it == null || it.isEmpty()) {
                recyclerView.visibility = View.GONE
            } else {
                recyclerView.visibility = View.VISIBLE
                recyclerView.adapter = WithdrawaListAdapter(it)
                recyclerView.layoutManager = LinearLayoutManager(this)
            }
        }
    }
}