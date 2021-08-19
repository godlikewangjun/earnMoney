package com.wj.makebai.ui.activity.comm

import com.wj.ktutils.startActivity
import com.wj.makebai.R
import com.wj.makebai.ui.activity.appTask.EarnTaskActivity
import com.wj.makebai.ui.activity.base.MakeActivity
import kotlinx.android.synthetic.main.activity_exchange_success.*

/**
 * 兑换成功的页面
 * @author Administrator
 * @version 1.0
 * @date 2019/11/28
 */
class ExchangeSuccessActivity : MakeActivity() {
    override fun bindLayout(): Int {
        return R.layout.activity_exchange_success
    }

    override fun initData() {
        title_content.text=getString(R.string.exchange_success)

        ok.setOnClickListener {
            startActivity<EarnTaskActivity>()
            finish()
        }
    }
}