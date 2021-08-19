package com.wj.makebai.ui.activity.comm

import androidx.recyclerview.widget.LinearLayoutManager
import com.abase.view.weight.RecyclerSpace
import com.wj.commonlib.http.HttpManager
import com.wj.makebai.R
import com.wj.makebai.ui.activity.base.MakeActivity
import com.wj.makebai.ui.adapter.HasBindAdapter
import kotlinx.android.synthetic.main.activity_paymentdetails.*

/**
 * 邀请用户绑定的列表
 * @author admin
 * @version 1.0
 * @date 2020/1/7
 */
class HasBindListActivity : MakeActivity(){
    override fun bindLayout(): Int {
        return R.layout.activity_paymentdetails
    }

    override fun initData() {
        title_content.text=getString(R.string.has_bind_list)
        HttpManager.selectHasBind({
            recyclerView.adapter =  HasBindAdapter(it)
            recyclerView.layoutManager= LinearLayoutManager(activity)
            recyclerView.addItemDecoration(RecyclerSpace(1,resources.getColor(R.color.div)))
        }){

        }
    }
}