package com.wj.commonlib.ui.weight

import android.content.Context
import android.util.AttributeSet
import android.util.DisplayMetrics
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView


/**
 * @author Admin
 * @version 1.0
 * @date 2018/5/15
 */
open class CustomLinearLayoutManager : androidx.recyclerview.widget.LinearLayoutManager {
    private var isScrollEnabled = true
    private var isScrollHorizontallyEnabled = true


    constructor(context: Context?, orientation: Int, reverseLayout: Boolean) : super(context, orientation, reverseLayout)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)
    constructor(context: Context?) : super(context)


    fun setScrollEnabled(flag: Boolean): CustomLinearLayoutManager {
        this.isScrollEnabled = flag
        return this
    }
    fun setScrollHorizontallyEnabled(flag: Boolean): CustomLinearLayoutManager {
        this.isScrollHorizontallyEnabled = flag
        return this
    }

    override fun canScrollVertically(): Boolean {
        //Similarly you can customize "canScrollHorizontally()" for managing horizontal scroll
        return isScrollEnabled && super.canScrollVertically()
    }

    override fun canScrollHorizontally(): Boolean {
        return isScrollHorizontallyEnabled && super.canScrollHorizontally()
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: Exception) {
        }
    }
    override fun smoothScrollToPosition(
        recyclerView: RecyclerView,
        state: RecyclerView.State?,
        position: Int
    ) {
        val smoothScroller: LinearSmoothScroller =
            object : LinearSmoothScroller(recyclerView.context) {
                override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float { // 返回：滑过1px时经历的时间(ms)。
                    return 25f / displayMetrics.densityDpi
                }

                override fun calculateDtToFit(
                    viewStart: Int,
                    viewEnd: Int,
                    boxStart: Int,
                    boxEnd: Int,
                    snapPreference: Int
                ): Int {
                    return boxStart - viewStart
                }
            }
        smoothScroller.targetPosition = position
        startSmoothScroll(smoothScroller)
    }
}
