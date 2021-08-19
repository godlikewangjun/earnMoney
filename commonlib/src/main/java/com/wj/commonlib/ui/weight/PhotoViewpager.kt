package com.wj.commonlib.ui.weight

import android.content.Context
import androidx.viewpager.widget.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent

/**
 * 处理防止放大缩小出现错误
 * @author Administrator
 * @version 1.0
 * @date 2018/12/27
 */
class PhotoViewpager : ViewPager {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    /**
     * 重写onInterceptTouchEvent()方法来解决图片点击缩小时候的Crash问题
     *
     */
    override fun onInterceptHoverEvent(event: MotionEvent?): Boolean {
        try {
            return super.onInterceptTouchEvent(event)
        } catch (e:IllegalArgumentException ) {
            e.printStackTrace()
        }
        return false
    }
}