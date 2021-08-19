package com.wj.makebai.ui.weight

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.abase.util.AbViewUtil
import com.wj.commonlib.utils.setOnClick
import com.wj.ktutils.startActivity
import com.wj.makebai.R
import com.wj.makebai.ui.activity.appTask.EarnTaskActivity
import kotlinx.android.synthetic.main.nodata_layout.view.*

/**
 * 空状态
 *
 * @author wangjun
 * @version 1.0
 * @date 2018/7/16
 */
class NoData : FrameLayout {
    enum class DataState {
        LOADING, REFRESH, NULL, GONE, NO_NETWORK, TASK_FINISH, DZ_NULL
    }

    var clickListener: OnClickListener? = null
    var type = DataState.LOADING
        set(value) {
            field = value
            visibility = View.VISIBLE
            refresh.visibility = View.GONE
            when (field) {
                DataState.LOADING -> {//正在加载
                    imgView.setAnimation("loading.json")
                    imgView.playAnimation()
                }
                DataState.NULL -> {//空
                    imgView.setAnimation("empty-box.json")
                    text.text = "什么都没有哦~"
                    imgView.playAnimation()
                }
                DataState.NO_NETWORK -> {//没有网络
                    imgView.setAnimation("no-internet.json")
                    text.text = "网络连接有问题,请检查网络设置"
                    refresh.visibility = View.VISIBLE
                    imgView.playAnimation()
                }
                DataState.REFRESH -> {//连接超时需要刷新
                    imgView.setAnimation("timeout.json")
                    text.text = "网络连接超时"
                    refresh.visibility = View.VISIBLE
                    imgView.playAnimation()
                }

                DataState.TASK_FINISH -> {//任务已经做完了
                    imgView.setAnimation("task_all_finish.json")
                    text.text = "恭喜您，任务做完了！\n发现里面更多任务哦！"
                    text.setTextColor(context.resources.getColor(R.color.text_gray))
                    imgView.playAnimation()
                    (imgView.layoutParams as LinearLayout.LayoutParams).topMargin =
                        AbViewUtil.dp2px(context, 100f)
                    go_fund.visibility = View.VISIBLE
                    go_fund.setOnClick {
                        context.startActivity<EarnTaskActivity>()
                    }
                }
                DataState.DZ_NULL -> {//连接超时需要刷新
                    imgView.setAnimation("no_data.json")
                    text.text = "还没有人发帖哦"
                    imgView.playAnimation()
                }
                else -> {//隐藏
                    imgView.cancelAnimation()
                    visibility = View.GONE
                }
            }
        }
    var reshListener: OnClickListener? = null//刷新

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    /**
     * 初始化
     */
    private fun init() {
        val view = LayoutInflater.from(context).inflate(R.layout.nodata_layout, this, false)
        visibility = View.GONE
        setBackgroundColor(context.resources.getColor(android.R.color.white))
        addView(view)

        noData_finish.setOnClickListener { (context as Activity).finish() }

        refresh.setOnClickListener {  visibility = View.GONE;clickListener?.onClick(it) }
    }

    /**
     * 绑定生命周期
     */
    fun bindLifecycle(lifecycle: Lifecycle) {
        lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (Lifecycle.Event.ON_DESTROY == event) imgView.cancelAnimation()
                else if (Lifecycle.Event.ON_PAUSE == event) imgView.pauseAnimation()
                else if (Lifecycle.Event.ON_RESUME == event) if (imgView.visibility == View.VISIBLE) imgView.resumeAnimation()
            }
        })
    }

    fun showTitle() {
        noData_title.visibility = View.VISIBLE
    }

    fun hideTitle() {
        noData_title.visibility = View.GONE
    }
}
