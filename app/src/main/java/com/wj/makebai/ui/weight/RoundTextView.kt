package com.wj.makebai.ui.weight

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import java.text.DecimalFormat

/**
 * @author Administrator
 * @version 1.0
 * @date 2019/11/19
 */
class RoundTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {
    private var mValueAnimator: ValueAnimator? = null
    private var mDf: DecimalFormat? = null

    init {
        init()
    }

    private fun init() {
        //格式化小数（保留小数点后两位）
        mDf = DecimalFormat("0.00")//"0.00"
        initAnim()
    }

    /**
     * 初始化动画
     */
    private fun initAnim() {
        mValueAnimator = ValueAnimator.ofInt(0, 0)//由于金钱是小数所以这里使用ofFloat方法
        mValueAnimator!!.duration = 1000//动画时间为1秒
        mValueAnimator!!.addUpdateListener { animation ->
            val value = animation.animatedValue as Int
            if (value > 0) {//当数值大于0的时候才赋值
                text = value.toString()//mDf!!.format(value)
            }
        }
    }

    /**
     * 设置要显示的金钱
     * @param money
     */
    fun setMoney(money: Int) {
        mValueAnimator!!.setIntValues(0, money)//重新设置数值的变化区间
        mValueAnimator!!.start()//开启动画
    }

    /**
     * 取消动画和动画监听（优化内存）
     */
    fun cancle() {
        mValueAnimator!!.removeAllUpdateListeners()//清除监听事件
        mValueAnimator!!.cancel()//取消动画
    }
}