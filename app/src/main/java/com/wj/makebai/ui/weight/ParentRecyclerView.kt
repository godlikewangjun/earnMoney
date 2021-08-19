package com.wj.makebai.ui.weight

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView



/**
 * 屏蔽recyview的滑动事件
 * @author Administrator
 * @version 1.0
 * @date 2019/8/8
 */
class ParentRecyclerView : RecyclerView {
    var move=false

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {}

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
       return move
    }

    override fun onTouchEvent(e: MotionEvent?): Boolean {
        return false
    }
}