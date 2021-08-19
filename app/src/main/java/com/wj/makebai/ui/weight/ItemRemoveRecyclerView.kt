package com.wj.makebai.ui.weight

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Scroller
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView

import com.wj.ui.interfaces.RecyerViewItemListener

/**
 * 侧滑删除
 * @author dchain
 * @version 1.0
 * @date 2019/10/29
 */
class ItemRemoveRecyclerView @JvmOverloads constructor(
    private val mContext: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RecyclerView(mContext, attrs, defStyle) {

    //上一次的触摸点
    private var mLastX: Int = 0
    private var mLastY: Int = 0
    //当前触摸的item的位置
    private var mPosition: Int = 0

    //item对应的布局
    private var mItemLayout: View? = null
    //删除按钮
    private var mDelete: TextView? = null

    //最大滑动距离(即删除按钮的宽度)
    private var mMaxLength: Int = 0
    //是否在垂直滑动列表
    private var isDragging: Boolean = false
    //item是在否跟随手指移动
    private var isItemMoving: Boolean = false

    //item是否开始自动滑动
    private var isStartScroll: Boolean = false
    //删除按钮状态   0：关闭 1：将要关闭 2：将要打开 3：打开
    private var mDeleteBtnState: Int = 0

    //检测手指在滑动过程中的速度
    private val mVelocityTracker: VelocityTracker = VelocityTracker.obtain()
    private val mScroller: Scroller = Scroller(mContext, LinearInterpolator())
    private var mListener: RecyerViewItemListener? = null
    var isRomove=false
    /**设置删除按钮 */
    var item_delete: Int = 0

    /**设置删除监听 */
    fun setListener(mListener: RecyerViewItemListener) {
        this.mListener = mListener
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        if(!isRomove) return super.onTouchEvent(e)
        when (e.action) {
            MotionEvent.ACTION_DOWN -> if (mItemLayout == null || mItemLayout!!.scrollX != 0) {
                return true
            }
        }
        return super.onTouchEvent(e)
    }

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        if(!isRomove) return super.onInterceptTouchEvent(e)
        mVelocityTracker.addMovement(e)

        val x = e.x.toInt()
        val y = e.y.toInt()
        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                parent.requestDisallowInterceptTouchEvent(isItemMoving)
                when (mDeleteBtnState) {
                    0 -> {
                        val view = findChildViewUnder(x.toFloat(), y.toFloat()) ?: return false

                        val viewHolder = getChildViewHolder(view)

                        mItemLayout = viewHolder.itemView
                        mItemLayout!!.isClickable = true
                        mPosition = viewHolder.adapterPosition

                        if(mItemLayout!!.findViewById<View>(item_delete)==null) return true
                        mDelete = mItemLayout!!.findViewById<View>(item_delete) as TextView
                        mMaxLength = mDelete!!.width
                        mDelete!!.setOnClickListener { v ->
                            mListener?.click(v, mPosition)
                            mItemLayout?.scrollTo(0, 0)
                            mDeleteBtnState = 0
                        }
                    }
                    3 -> {
                        mScroller.startScroll(mItemLayout!!.scrollX, 0, -mMaxLength, 0, 200)
                        invalidate()
                        mDeleteBtnState = 0
                        return false
                    }
                    else -> return false
                }
            }
            MotionEvent.ACTION_MOVE -> {

                val dx = mLastX - x
                val dy = mLastY - y

                if (dy < 80 && x >= mItemLayout!!.width / 2 || mItemLayout!!.scrollX > 0) {
                    isItemMoving = true
                    val scrollX = mItemLayout!!.scrollX
                    if (Math.abs(dx) > Math.abs(dy)) {//左边界检测
                        if (scrollX + dx <= 0) {
                            mItemLayout!!.scrollTo(0, 0)
                            return true
                        } else if (scrollX + dx >= mMaxLength) {//右边界检测
                            mItemLayout!!.scrollTo(mMaxLength, 0)
                            return true
                        }
                        mItemLayout!!.scrollBy(dx, 0)//item跟随手指滑动
                    }
                } else {
                    isItemMoving = false
                }
                parent.requestDisallowInterceptTouchEvent(isItemMoving)
                return false
            }
            MotionEvent.ACTION_UP -> {

                mVelocityTracker.computeCurrentVelocity(1000)//计算手指滑动的速度
                val xVelocity = mVelocityTracker.xVelocity//水平方向速度（向左为负）
                val yVelocity = mVelocityTracker.yVelocity//垂直方向速度

                var deltaX = 0
                val upScrollX = mItemLayout!!.scrollX

                if (Math.abs(xVelocity) > 100 && Math.abs(xVelocity) > Math.abs(yVelocity)) {
                    if (xVelocity <= -100) {//左滑速度大于100，则删除按钮显示
                        deltaX = mMaxLength - upScrollX
                        mDeleteBtnState = 2
                    } else if (xVelocity > 100) {//右滑速度大于100，则删除按钮隐藏
                        deltaX = -upScrollX
                        mDeleteBtnState = 1
                    }
                } else {
                    if (upScrollX >= mMaxLength / 2) {//item的左滑动距离大于删除按钮宽度的一半，则则显示删除按钮
                        deltaX = mMaxLength - upScrollX
                        mDeleteBtnState = 2
                    } else if (upScrollX < mMaxLength / 2) {//否则隐藏
                        deltaX = -upScrollX
                        mDeleteBtnState = 1
                    }
                }

                //item自动滑动到指定位置
                mScroller.startScroll(upScrollX, 0, deltaX, 0, 200)
                isStartScroll = true
                parent.requestDisallowInterceptTouchEvent(false)

                mVelocityTracker.clear()
            }
        }

        mLastX = x
        mLastY = y
        return super.onInterceptTouchEvent(e)
    }

    override fun computeScroll() {
        if (mScroller.computeScrollOffset()) {
            mItemLayout!!.scrollTo(mScroller.currX, mScroller.currY)
            invalidate()
        } else if (isStartScroll) {
            isStartScroll = false
            if (mDeleteBtnState == 1) {
                mDeleteBtnState = 0
            }

            if (mDeleteBtnState == 2) {
                mDeleteBtnState = 3
            }
        }
    }

    override fun onDetachedFromWindow() {
        mVelocityTracker.recycle()
        super.onDetachedFromWindow()
    }

    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)
        isDragging = state == RecyclerView.SCROLL_STATE_DRAGGING
    }
}
