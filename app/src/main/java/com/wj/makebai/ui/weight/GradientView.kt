package com.wj.makebai.ui.weight

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.wj.makebai.R


/**
 * 动态渐变
 * @author dchain
 * @version 1.0
 * @date 2019/4/19
 */
class GradientView : View {
    private var animatedValue: Int = 0
    private var duration=1000*8L
    private var max=255
    private var Red=max
    private var Green=50
    private var Blude=max
    private var colorEnd: Int =0
    private var colorStart: Int = 0

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()

    }

    fun init() {
        colorStart=context.resources.getColor(R.color.colorPrimary)
        start()
    }

    /**
     * 开始渐变
     */
    private fun start(){
        val animator = ValueAnimator.ofInt(0, max)
        animator.duration = duration
        animator.addUpdateListener { animation ->
            animatedValue = animation.animatedValue as Int
            colorStart = Color.rgb(Red, animatedValue, Blude - animatedValue)
            colorEnd = Color.rgb(animatedValue, Green, Blude - animatedValue)
            if (animatedValue == max) {
                animator.cancel()
                reLoad()
            }
            invalidate()
        }
        animator.start()
    }

    /**
     * 返回渐变
     */
    private fun reLoad(){
        val animator = ValueAnimator.ofInt(0, max)
        animator.duration = duration
        animator.addUpdateListener { animation ->
            animatedValue = animation.animatedValue as Int
            colorStart = Color.rgb(Red, max-animatedValue,  animatedValue)
            colorEnd = Color.rgb(max-animatedValue, Green,  animatedValue)
            if (animatedValue == max) {
                animator.cancel()
                start()
            }
            invalidate()
        }
        animator.start()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //获取View的宽高
        val width = width
        val height = height

        val paint = Paint()
        val backGradient = LinearGradient(
            width.toFloat(),
            0f,
            0f,
            0f,
            intArrayOf(colorStart, colorEnd),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.shader = backGradient
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }
}
