package com.wj.makebai.ui.activity.comm

import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.abase.util.Tools
import com.abase.view.weight.RecyclerSpace
import com.wj.commonlib.data.mode.PaymentInfoMode
import com.wj.commonlib.http.HttpManager
import com.wj.commonlib.http.Urls
import com.wj.commonlib.statices.Statics
import com.wj.commonlib.ui.ViewControl
import com.wj.commonlib.ui.weight.CustomGridManager
import com.wj.commonlib.utils.CommTools
import com.wj.commonlib.utils.LoadDialog
import com.wj.ktutils.isNull
import com.wj.ktutils.showTip
import com.wj.ktutils.startActivity
import com.wj.makebai.R
import com.wj.makebai.ui.activity.base.MakeActivity
import com.wj.makebai.ui.adapter.ExchangeProjectAdapter
import com.wj.makebai.ui.adapter.WithdrawaListAdapter
import com.wj.makebai.ui.weight.NoData
import kotlinx.android.synthetic.main.activity_exchange.*
import kotlinx.android.synthetic.main.dialog_payment_bind.view.*

/**
 * 兑换页面
 * @author Administrator
 * @version 1.0
 * @date 2019/11/15
 */
class ExchangeActivity : MakeActivity(), View.OnClickListener {
    private var paymentInfoMode: PaymentInfoMode? = null
    private var isExchange = true
    private var welfareid=-1

    override fun bindLayout(): Int {
        return R.layout.activity_exchange
    }

    override fun initData() {
        title_content.text = getString(R.string.exchange)

        top.layoutParams.height = (Tools.getScreenWH(activity)[0] / 2.5).toInt()
        other.text=getString(R.string.welfare_center)


        setState(NoData.DataState.LOADING, false)
        HttpManager.exchangeProjects {
            setState(NoData.DataState.GONE, false)
            recyclerView.adapter = ExchangeProjectAdapter(it)
            recyclerView.layoutManager = CustomGridManager(activity, 3)
            if (recyclerView.itemDecorationCount < 1) recyclerView.addItemDecoration(
                RecyclerSpace(
                    10
                )
            )
            if(Statics.userMode!=null)HttpManager.welfareUseList{
                if(it.size>0){
                    val str=getString(R.string.has_welfare)
                    welfare_tips.text= String.format(str,it[0].title)
                    val index=str.length-2
                    Tools.changTextViewColor( welfare_tips,index,welfare_tips.text.length,resources.getColor(R.color.red))

                    welfareid=it[0].welfareid
                    var adapter=recyclerView.adapter
                    if( adapter!=null){
                        adapter=adapter as ExchangeProjectAdapter
                        adapter.sale=it[0].value
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }


        rule.setOnClickListener(this)
        enter.setOnClickListener(this)
        other_down.setOnClickListener(this)


        if (Statics.userMode != null) {
            HttpManager.withdrawList(0) {
                if (it == null || it.isEmpty()) {
                    none_tx.visibility = View.VISIBLE
                    bill_recyclerView.visibility = View.GONE
                } else {
                    bill_recyclerView.visibility = View.VISIBLE
                    bill_recyclerView.adapter = WithdrawaListAdapter(it)
                    bill_recyclerView.layoutManager = LinearLayoutManager(this)
                }
            }

        } else {
            view1.visibility = View.GONE
            view2.visibility = View.GONE
            welfare_tips.text=""
        }
    }

    override fun onResume() {
        super.onResume()
        if (Statics.userMode != null) {
            HttpManager.userDetail({
                user_points.text = it.userbalance.toString()
            },null)

            if (isExchange){
                isExchange=false
                HttpManager.paymentInfo {
                    enter.isEnabled = true
                    paymentInfoMode = it
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.rule -> {//提现规则说明
                startActivity<WebActivity>("url" to Urls.withdrawalRule)
            }
            R.id.other_down->{//福利中心
                startActivity<WelfareActivity>()
            }
            R.id.enter -> {//提现
                isExchange=true
                if (!paymentInfoMode!!.isFlow || paymentInfoMode!!.payment == null) {
                    showTip("请先完成绑定")
                    val view =
                        LayoutInflater.from(activity).inflate(R.layout.dialog_payment_bind, null)

                    val dialog = ViewControl.customAlertDialog(activity, view, null)
                    view.copy.setOnClickListener {
                        CommTools.copy(activity, view.user_code)
                        CommTools.openWx(activity)
                        dialog.cancel()
                    }
                    if (paymentInfoMode!!.isFlow) {
                        view.copy.isEnabled = false
                        view.copy.text = getString(R.string.has_flow)
                        view.copy.setBackgroundResource(R.drawable.shape_has_bind)
                    }

                    view.close.setOnClickListener {
                        dialog.cancel()
                    }
                    view.bind.setOnClickListener {
                        if (!paymentInfoMode!!.isFlow) {
                            showTip("请先关注公众号")
                        } else {
                            when {
                                view.user_bind_wx.text.isNull() -> {
                                    showTip("请填写微信号")
                                }
                                view.user_bind_phone.text.isNull() -> {
                                    showTip("请填写手机号")
                                }
                                view.user_bind_phone.text!!.length != 11 -> {
                                    showTip("请填写正确手机号")
                                }
                                else -> {
                                    HttpManager.bindPaymentInfo(
                                        view.user_bind_wx.text.toString(),
                                        view.user_bind_phone.text.toString()
                                    ) {
                                        HttpManager.paymentInfo {
                                            paymentInfoMode = it
                                        }
                                        showTip("绑定成功，可以提现了")
                                        dialog.cancel()
                                    }
                                }
                            }
                        }
                    }
                } else {//提现
                    val adapter = recyclerView.adapter as ExchangeProjectAdapter
                    if (Statics.userMode!!.userbalance < CommTools.price2Point(adapter.list[adapter.choose].withdrawal_point.toDouble())) {
                        return showTip("余额不足")
                    }
                    LoadDialog.show(activity)
                    HttpManager.withdrawal(adapter.list[adapter.choose].idwithdrawal_type,welfareid,{
                        startActivity<ExchangeSuccessActivity>()
                        finish()
                    }) {
                        LoadDialog.cancle()
                    }
                }
            }
        }
    }
}