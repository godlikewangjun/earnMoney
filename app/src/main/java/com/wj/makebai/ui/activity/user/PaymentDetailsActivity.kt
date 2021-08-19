package com.wj.makebai.ui.activity.user

import androidx.recyclerview.widget.LinearLayoutManager
import com.wj.commonlib.http.HttpManager
import com.wj.makebai.R
import com.wj.makebai.ui.activity.base.MakeActivity
import com.wj.makebai.ui.adapter.PaymentDetailsAdapter
import kotlinx.android.synthetic.main.activity_paymentdetails.*

/**
 * 收支明细
 * @author Administrator
 * @version 1.0
 * @date 2019/11/21
 */
class PaymentDetailsActivity :MakeActivity(){
    override fun bindLayout(): Int {
        return R.layout.activity_paymentdetails
    }

    override fun initData() {
        title_content.text=getString(R.string.payment_details)
        HttpManager.billList {
            recyclerView.adapter =  PaymentDetailsAdapter(it)
            recyclerView.layoutManager= LinearLayoutManager(activity)
        }
    }
}