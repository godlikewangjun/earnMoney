package com.wj.makebai.ui.activity.base

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.abase.view.parent.BaseActivity
import com.gyf.immersionbar.ImmersionBar
import com.umeng.analytics.MobclickAgent
import com.wj.commonlib.utils.LoadDialog
import com.wj.makebai.R
import com.wj.makebai.ui.activity.MainActivity
import com.wj.makebai.ui.weight.NoData


/**
 *
 * @author Admin
 * @version 1.0
 * @date 2018/6/4
 */
@Suppress("DEPRECATION")
abstract class MakeActivity : BaseActivity() {
    private var noData: NoData? = null
    var isFrist = true
    var isAnim = true
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        if (!MainActivity.isLive) {
            val i = baseContext.packageManager.getLaunchIntentForPackage(baseContext.packageName)
            i?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(i)
            return
        }
        super.onCreate(savedInstanceState, persistentState)
    }

    override fun setContentView(): Int {
        return bindLayout()
    }

    override fun init() {
        setThemeColor(R.color.white)
        ImmersionBar.with(this).statusBarDarkFont(true).init()

        title_line.visibility = View.VISIBLE
        title_line.setBackgroundResource(R.color.div)
        title_content.setTextColor(resources.getColor(R.color.black))
        backto_img.layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
        backto_img.layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
        backto_img.setImageResource(R.drawable.ic_back2)

        //添加默认的加载状态
        noData = NoData(activity)
        noData!!.setOnClickListener { }
        noData!!.clickListener = View.OnClickListener { refreshData() }
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        titlebar.addView(noData, 2, layoutParams)

        initData()
        isFrist = false
    }

    /**
     * 公共处理刷新
     */
    open fun refreshData() {

    }

    /**
     * 设置加载状态
     */
    fun setState(state: NoData.DataState, showFinish: Boolean) {
        noData?.type = state
        contentView.isVisible = state == NoData.DataState.GONE

        if (showFinish) noData?.showTitle() else noData?.hideTitle()
    }

    override fun onResume() {
        super.onResume()
        MobclickAgent.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        MobclickAgent.onPause(this)
    }

    override fun onDestroy() {
        LoadDialog.cancle()
        super.onDestroy()

    }

    override fun finish() {
        super.finish()
        if (isAnim) overridePendingTransition(R.anim.alpha_in, R.anim.anim_right_out)
    }

    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
        overridePendingTransition(R.anim.anim_right_in, R.anim.alpha_out)
    }

    abstract fun bindLayout(): Int
    abstract fun initData()
}