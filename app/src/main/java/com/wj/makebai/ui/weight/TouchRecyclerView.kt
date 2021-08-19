package com.wj.makebai.ui.weight

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

/**
 *
 * @author Administrator
 * @version 1.0
 * @date 2019/12/26
 */
class TouchRecyclerView:RecyclerView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )
    var isScroll=true
    var maxHeight=0
    var getY=fun ():Int{
        return 0
    }
    var scrollChild=fun (){
    }
    override fun onInterceptTouchEvent(e: MotionEvent?): Boolean {
//        println("$maxHeight -------onInterceptTouchEvent----- ${getY.invoke()} ------- $isScroll")
        if(!canScroll(getY.invoke()))return false
        return super.onInterceptTouchEvent(e)
    }
    override fun onTouchEvent(e: MotionEvent?): Boolean {
//        println("$maxHeight -------onTouchEvent----- ${getY.invoke()} ---------$isScroll")
        if(!canScroll(getY.invoke()))return false
        return super.onTouchEvent(e)
    }
    private fun canScroll(scrollY:Int):Boolean{
        var  canScroll=isScroll
        if(scrollY!=0 && scrollY<=maxHeight) canScroll= false
        if(!canScroll)scrollChild.invoke()
        return canScroll
    }
}