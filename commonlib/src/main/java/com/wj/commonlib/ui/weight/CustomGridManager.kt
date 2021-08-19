package com.wj.commonlib.ui.weight

import android.content.Context

/**
 *
 * @author wangjun
 * @version 1.0
 * @date 2018/7/10
 */
class CustomGridManager(context: Context?, spanCount: Int) : androidx.recyclerview.widget.GridLayoutManager(context, spanCount) {
    private var isScrollEnabled = true
    fun setScrollEnabled(flag: Boolean): CustomGridManager {
        this.isScrollEnabled = flag
        return this
    }
    override fun canScrollVertically(): Boolean {
        return isScrollEnabled && super.canScrollVertically()
    }
}