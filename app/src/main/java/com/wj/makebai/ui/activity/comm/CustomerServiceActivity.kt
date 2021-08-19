package com.wj.makebai.ui.activity.comm

import android.annotation.SuppressLint
import android.view.View
import com.wj.commonlib.data.mode.Announcement
import com.wj.commonlib.http.HttpManager
import com.wj.commonlib.utils.CommTools
import com.wj.ktutils.isNull
import com.wj.ktutils.showTip
import com.wj.makebai.R
import com.wj.makebai.ui.activity.base.MakeActivity
import com.wj.makebai.ui.weight.NoData
import kotlinx.android.synthetic.main.activity_customerservice.*

/**
 * 客服服务
 * @author dchain
 * @version 1.0
 * @date 2019/10/18
 */
class CustomerServiceActivity : MakeActivity(), View.OnClickListener {
    var mode=ArrayList<Announcement>()
    override fun bindLayout(): Int {
        return R.layout.activity_customerservice
    }

    @SuppressLint("SetTextI18n")
    override fun initData() {
        title_content.text = getString(R.string.cooperation)

        setState(NoData.DataState.LOADING,false)
        refreshData()

        qq_qun.setOnClickListener(this)
        qq_sw.setOnClickListener(this)
        qq_js.setOnClickListener(this)
    }

    override fun refreshData() {
        HttpManager.customerServiceQQ {
            mode=it
            setState(NoData.DataState.GONE,false)
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.qq_qun -> {
                val value=mode.getValue(6)
                if(value.isNull()){
                    return showTip("暂无群")
                }else{
                    CommTools.joinQQGroup(activity, value)
                }
            }
            R.id.qq_sw -> {
                val value=mode.getValue(7)
                if(value.isNull()){
                    return showTip("暂无商务QQ")
                }else{
                    CommTools.openQq(activity)
                    CommTools.copy(activity,value)
                }
            }
            R.id.qq_js -> {
                val value=mode.getValue(8)
                if(value.isNull()){
                    return showTip("暂无技术QQ")
                }else{
                    CommTools.openQq(activity)
                    CommTools.copy(activity,value)
                }
            }
        }
    }

    /**
     * 获取值
     */
    fun ArrayList<Announcement>.getValue(type:Int):String{
        var value=""
        for (index in mode){
            if(6==index.type){
                value=index.value
                break
            }
        }
        return value
    }
}