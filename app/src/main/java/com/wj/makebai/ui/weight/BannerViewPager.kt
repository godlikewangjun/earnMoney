package com.wj.makebai.ui.weight

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

/**
 * @author Administrator
 * @version 1.0
 * @date 2019/12/20
 */
class BannerViewPager : ViewPager {
    private var scrollable = true

    constructor(context: Context?) : super(context!!)
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!,
        attrs
    )

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return if (scrollable) {
            if (currentItem == 0 && childCount == 0) {
                false
            } else super.onTouchEvent(ev)
        } else {
            false
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return if (scrollable) {
            if (currentItem == 0 && childCount == 0) {
                false
            } else super.onInterceptTouchEvent(ev)
        } else {
            false
        }
    }

    fun setScrollable(scrollable: Boolean) {
        this.scrollable = scrollable
    }
}