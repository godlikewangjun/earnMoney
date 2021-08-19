package com.wj.makebai.ui.activity.comm

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.abase.util.GsonUtil
import com.abase.util.Tools
import com.gyf.immersionbar.ImmersionBar
import com.wj.commonlib.http.HttpManager
import com.wj.commonlib.http.Urls
import com.wj.commonlib.statices.Statics
import com.wj.commonlib.statices.XmlConfigs
import com.wj.commonlib.ui.ViewControl
import com.wj.commonlib.utils.CommTools
import com.wj.ktutils.WjSP
import com.wj.ktutils.startActivity
import com.wj.makebai.R
import com.wj.makebai.ui.activity.base.MakeActivity
import com.wj.makebai.ui.adapter.DaysAdapter
import com.wj.makebai.ui.weight.NoData
import com.wj.makebai.utils.MbTools
import kotlinx.android.synthetic.main.activty_sign.*
import kotlinx.android.synthetic.main.dialog_sign.view.*

/**
 * 签到页面
 * @author Administrator
 * @version 1.0
 * @date 2019/11/12
 */
class SignActivity : MakeActivity(), View.OnClickListener {

    override fun bindLayout(): Int {
        return R.layout.activty_sign
    }

    override fun initData() {
        ImmersionBar.with(this).statusBarDarkFont(false).init()

        setThemeColor(R.color.orange1)
        title_content.text = getString(R.string.sign)
        title_content.setTextColor(resources.getColor(R.color.white))
        backto_img.setImageResource(R.drawable.ic_back_w)
        other.text=getString(R.string.jf_shop)
        other.setTextColor(resources.getColor(R.color.white))
        title_line.isVisible=false

        setState(NoData.DataState.LOADING,false)
        days.adapter = DaysAdapter()
        days.layoutManager = GridLayoutManager(activity, 7)



        jfcj.layoutParams.height=Tools.getScreenWH(activity)[0]/3

        sign_rule.setOnClickListener(this)
        sign.setOnClickListener(this)
        other_down.setOnClickListener(this)
        jfcj.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        //设置积分
        if (Statics.userMode != null) {
            user_points.text = Statics.userMode!!.userbalance.toString()
            HttpManager.signInfo {
                days_count.text =
                    String.format(getString(R.string.sign_count_str), it.days)
                val adapter = (days.adapter as DaysAdapter)
                adapter.days = it.days
                adapter.isSign = it.sign
                adapter.notifyDataSetChanged()
                if (it.sign) {
                    sign.isEnabled = false
                    sign.text = getString(R.string.has_sign)
                }
                setState(NoData.DataState.GONE,false)
            }
        }else{
            setState(NoData.DataState.GONE,false)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.sign_rule -> {//签到规则
                startActivity<WebActivity>("url" to Urls.signRule)
            }
            R.id.other_down->{//积分商城
                startActivity<ExchangeActivity>()
            }
            R.id.jfcj->{//积分抽奖
                startActivity<LuckPanActivity>()
            }
            R.id.sign -> {//签到
                if(MbTools.isLogin(activity)){
                    HttpManager.sign {
                        Statics.userMode!!.userbalance = it.userPoints
                        Statics.userMode!!.usermoney = CommTools.point2Price(it.userPoints.toDouble())


                        WjSP.getInstance().setValues(
                            XmlConfigs.USERMODE, GsonUtil.getGson().toJson(Statics.userMode))

                        sign.isEnabled = false
                        sign.text = getString(R.string.has_sign)
                        user_points.text = it.userPoints.toString()
                        days_count.text =
                            String.format(getString(R.string.sign_count_str), it.sing_count)

                        val adapter = (days.adapter as DaysAdapter)
                        adapter.days = it.sing_count
                        adapter.isSign = true
                        adapter.notifyDataSetChanged()

                        val dialogView= LayoutInflater.from(activity).inflate(R.layout.dialog_sign,null)
                        val dialog= ViewControl.customAlertDialog(activity,dialogView,null)
                        dialogView.sign_points.text="+${it.points}"
                        dialogView.ok.setOnClickListener { dialog.cancel() }
                        dialogView.close.setOnClickListener { dialog.cancel() }
                    }
                }
            }
        }
    }
}