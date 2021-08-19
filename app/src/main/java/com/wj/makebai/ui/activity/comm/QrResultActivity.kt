package com.wj.makebai.ui.activity.comm

import android.view.View
import com.wj.commonlib.utils.CommTools
import com.wj.ktutils.startActivity
import com.wj.makebai.R
import com.wj.makebai.ui.activity.base.MakeActivity
import kotlinx.android.synthetic.main.activity_qrresult.*

/**
 * 扫一扫结构返回
 * @author dchain
 * @version 1.0
 * @date 2019/9/29
 */
class QrResultActivity :MakeActivity(),View.OnClickListener{

    override fun bindLayout(): Int {
        return R.layout.activity_qrresult
    }

    override fun initData() {
        title_content.text=getString(R.string.qr_result)

        val result=intent.getStringExtra("data")
        qr_text.text=result
        if(result.startsWith("http")){
            enter.visibility= View.VISIBLE
        }else{
            enter.visibility= View.GONE
        }

        copy.setOnClickListener(this)
        enter.setOnClickListener(this)
    }
    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.copy->{
                CommTools.copy(activity,qr_text)
                finish()
            }
            R.id.enter->{
                startActivity<WebActivity>("url" to qr_text.text.toString())
            }
        }
    }
}