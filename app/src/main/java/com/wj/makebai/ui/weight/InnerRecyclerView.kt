package com.wj.makebai.ui.weight

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import com.wj.makebai.ui.fragment.HomeFragment.Companion.finalHeight
import kotlin.math.abs

/**
 * Created by Administrator on 2018/9/5.
 * Description : InnerRecyclerView
 */
class InnerRecyclerView : RecyclerView {
    private var downX=0f //按下时 的X坐标 = 0f
    private var downY=0f //按下时 的Y坐标 = 0f
    private var maxY = 0
    var isTouch=true

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )


    override fun onTouchEvent(e: MotionEvent): Boolean {
        if(!isTouch) return false
        val x = e.x
        val y = e.y
        maxY = finalHeight
        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                //将按下时的坐标存储
                downX = x
                downY = y
                parent.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_MOVE -> {
                //获取到距离差
                val dx = x - downX
                val dy = y - downY
                //通过距离差判断方向
                val orientation = getOrientation(dx, dy)
                val location = intArrayOf(0, 0)
                getLocationOnScreen(location)
//                println(orientation + "   内部  -------------- " + location[1] + " -------" + maxY)
                when (orientation) {
                    "b" ->  //内层RecyclerView下拉到最顶部时候不再处理事件
                        if (!canScrollVertically(-1)) {
                            parent.requestDisallowInterceptTouchEvent(false)
//                            println("内部不处理 -------------------")
                        } else {
                            parent.requestDisallowInterceptTouchEvent(true)
                        }
                    "t" -> {
                        Log.d("maxY", maxY.toString() + "")
                        Log.d("location[1]", location[1].toString() + "")
                        if (location[1] <= maxY) {
                            parent.requestDisallowInterceptTouchEvent(true)
                        } else {
                            parent.requestDisallowInterceptTouchEvent(false)
                        }
                    }
                    "r", "l" -> parent.requestDisallowInterceptTouchEvent(false)
                }
            }
        }
        return super.onTouchEvent(e)
    }

    private fun getOrientation(dx: Float, dy: Float): String {
        return if (abs(dx) > abs(dy)) { //X轴移动
            if (dx > 0) "r" else "l" //右,左
        } else { //Y轴移动
            if (dy > 0) "b" else "t" //下//上
        }
    }

}