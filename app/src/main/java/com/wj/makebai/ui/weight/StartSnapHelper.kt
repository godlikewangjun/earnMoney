package com.wj.makebai.ui.weight

import android.view.View
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView


/**
 * 定位到第一个子View的SnapHelper
 * Created by xmuSistone on 2017/9/19.
 */
class StartSnapHelper : LinearSnapHelper() {

    override fun calculateDistanceToFinalSnap(
        layoutManager: RecyclerView.LayoutManager,
        targetView: View
    ): IntArray? {
        val out = IntArray(2)
        out[0] = 0
        out[1] = (layoutManager as VegaLayoutManager).snapHeight
        return out
    }

    override fun findSnapView(layoutManager: RecyclerView.LayoutManager): View? {
        val custLayoutManager = layoutManager as VegaLayoutManager
        return custLayoutManager.findSnapView()
    }
}