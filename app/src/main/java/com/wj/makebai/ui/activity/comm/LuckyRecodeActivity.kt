package com.wj.makebai.ui.activity.comm

import androidx.recyclerview.widget.LinearLayoutManager
import com.wj.commonlib.http.HttpManager
import com.wj.makebai.R
import com.wj.makebai.ui.activity.base.MakeActivity
import com.wj.makebai.ui.adapter.LuckyRecodeAdapter
import kotlinx.android.synthetic.main.activity_luckyrecode.*

/**
 * 抽奖记录
 * @author Administrator
 * @version 1.0
 * @date 2019/11/21
 */
class LuckyRecodeActivity : MakeActivity() {
    override fun bindLayout(): Int {
        return R.layout.activity_luckyrecode
    }

    override fun initData() {
        title_content.text = getString(R.string.lucky_recode)
        HttpManager.luckyRecode {
            recyclerView.adapter =  LuckyRecodeAdapter(it)
            recyclerView.layoutManager=LinearLayoutManager(activity)
        }
    }
}