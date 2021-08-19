package com.wj.makebai.ui.weight

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.LinearLayout
import androidx.core.view.NestedScrollingChild
import androidx.core.view.NestedScrollingChildHelper
import androidx.core.view.ViewCompat

/**
 * 实现header跟随手指触发整体滑动
 */
class NestedLinearLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), NestedScrollingChild {

    private val offset = IntArray(2)
    private val consumed = IntArray(2)

    private var mScrollingChildHelper: NestedScrollingChildHelper? = null
    private var lastY: Int = 0

    private val scrollingChildHelper: NestedScrollingChildHelper
        get() {
            if (mScrollingChildHelper == null) {
                mScrollingChildHelper = NestedScrollingChildHelper(this)
            }
            return mScrollingChildHelper!!
        }

    init {
        isNestedScrollingEnabled = true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastY = event.y.toInt()
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL)
            }
            MotionEvent.ACTION_MOVE -> {
                val y = event.y.toInt()
                //dy < 0 下滑， dy>0 上滑
                val dy = lastY - y
                lastY = y
                dispatchNestedPreScroll(0, dy, consumed, offset)
            }
        }
        return true
    }


    override fun setNestedScrollingEnabled(enabled: Boolean) {
        scrollingChildHelper.isNestedScrollingEnabled = enabled
    }

    override fun isNestedScrollingEnabled(): Boolean {
        return scrollingChildHelper.isNestedScrollingEnabled
    }

    override fun startNestedScroll(axes: Int): Boolean {
        return scrollingChildHelper.startNestedScroll(axes)
    }

    override fun stopNestedScroll() {
        scrollingChildHelper.stopNestedScroll()
    }

    override fun hasNestedScrollingParent(): Boolean {
        return scrollingChildHelper.hasNestedScrollingParent()
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int, dyConsumed: Int,
        dxUnconsumed: Int, dyUnconsumed: Int, offsetInWindow: IntArray?
    ): Boolean {
        return scrollingChildHelper.dispatchNestedScroll(
            dxConsumed,
            dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow
        )
    }

    override fun dispatchNestedPreScroll(
        dx: Int, dy: Int, consumed: IntArray?,
        offsetInWindow: IntArray?
    ): Boolean {
        return scrollingChildHelper.dispatchNestedPreScroll(
            dx, dy,
            consumed, offsetInWindow
        )
    }

    override fun dispatchNestedFling(
        velocityX: Float, velocityY: Float,
        consumed: Boolean
    ): Boolean {
        return scrollingChildHelper.dispatchNestedFling(
            velocityX,
            velocityY, consumed
        )
    }

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        return scrollingChildHelper.dispatchNestedPreFling(
            velocityX,
            velocityY
        )
    }
}
