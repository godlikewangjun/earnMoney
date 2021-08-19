package com.wj.makebai.ui.activity.comm

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import com.abase.util.Tools
import com.bumptech.glide.request.RequestOptions
import com.wj.commonlib.http.HttpManager
import com.wj.commonlib.statices.Statics
import com.wj.commonlib.ui.weight.CustomLinearLayoutManager
import com.wj.ktutils.startActivity
import com.wj.makebai.R
import com.wj.makebai.ui.activity.appTask.EarnTaskActivity
import com.wj.makebai.ui.activity.base.MakeActivity
import com.wj.makebai.ui.adapter.WelfareListAdapter
import com.wj.makebai.ui.weight.NoData
import kotlinx.android.synthetic.main.activity_welfare.*

/**
 * 福利
 * @author Administrator
 * @version 1.0
 * @date 2019/11/14
 */
class WelfareActivity : MakeActivity() {
    override fun bindLayout(): Int {
        return R.layout.activity_welfare
    }

    override fun initData() {
        title_content.text = getString(R.string.welfare_center)
        view.layoutParams.height = (Tools.getScreenWH(activity)[0] / 3).toInt()

        setState(NoData.DataState.LOADING, false)
        HttpManager.welfareList {
            recyclerView.adapter = WelfareListAdapter(it)
            recyclerView.layoutManager = CustomLinearLayoutManager(activity)

            setState(NoData.DataState.GONE, false)
        }

        more.setOnClickListener {
            startActivity<EarnTaskActivity>()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Statics.userMode != null) {
            glide!!.load(Statics.userMode!!.usericon).apply(RequestOptions().circleCrop())
                .into(user_icon)
            user_name.text = String.format(
                getString(R.string.name_points),
                Statics.userMode!!.username, Statics.userMode!!.userbalance
            )
            val index = user_name.text.toString().indexOf(Statics.userMode!!.userbalance.toString())

            //修改大小
            val builder = SpannableStringBuilder(
                user_name
                    .text.toString()
            )
            builder.setSpan(
                AbsoluteSizeSpan(25, true),
                index,
                index + Statics.userMode!!.userbalance.toString().length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            builder.setSpan(
                ForegroundColorSpan(resources.getColor(R.color.red)),
                index,
                index + Statics.userMode!!.userbalance.toString().length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            user_name.text = builder

        } else user.visibility = View.GONE

    }
}