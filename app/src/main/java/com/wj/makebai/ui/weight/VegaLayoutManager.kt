package com.wj.makebai.ui.weight

import android.content.Context
import android.graphics.Rect
import android.util.ArrayMap
import android.util.SparseArray
import android.util.SparseBooleanArray
import android.view.View
import android.view.ViewGroup

import androidx.recyclerview.widget.RecyclerView

import com.wj.commonlib.ui.weight.CustomLinearLayoutManager

/**
 * Created by xmuSistone on 2017/9/20.
 */
class VegaLayoutManager(context: Context) : CustomLinearLayoutManager(context) {

    private var scroll = 0
    private val locationRects = SparseArray<Rect>()
    private val attachedItems = SparseBooleanArray()
    private val viewTypeHeightMap = ArrayMap<Int, Int>()

    private var needSnap = false
    private var lastDy = 0
    private var maxScroll = -1
    private var adapter: RecyclerView.Adapter<*>? = null
    private var recycler: RecyclerView.Recycler? = null

    // scroll变大，属于列表往下走，往下找下一个为snapView
    val snapHeight: Int
        get() {
            if (!needSnap || locationRects.size()<1) {
                return 0
            }
            needSnap = false

            val displayRect = Rect(0, scroll, width, height + scroll)
            val itemCount = itemCount
            for (i in 0 until itemCount) {
                val itemRect = locationRects.get(i)
                if (displayRect.intersect(itemRect)) {

                    if (lastDy > 0) {
                        if (i < itemCount - 1) {
                            val nextRect = locationRects.get(i + 1)
                            return nextRect.top - displayRect.top
                        }
                    }
                    return itemRect.top - displayRect.top
                }
            }
            return 0
        }

    init {
        isAutoMeasureEnabled = true
    }

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onAdapterChanged(
        oldAdapter: RecyclerView.Adapter<*>?,
        newAdapter: RecyclerView.Adapter<*>?
    ) {
        super.onAdapterChanged(oldAdapter, newAdapter)
        this.adapter = newAdapter
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        this.recycler = recycler // 二话不说，先把recycler保存了
        if (state!!.isPreLayout) {
            return
        }
        if (adapter != null){
            buildLocationRects()
            // 先回收放到缓存，后面会再次统一layout
            detachAndScrapAttachedViews(recycler!!)
            layoutItemsOnCreate(recycler)
        }else{
            super.onLayoutChildren(recycler, state)
        }

    }

    private fun buildLocationRects() {
        locationRects.clear()
        attachedItems.clear()

        var tempPosition = paddingTop
        val itemCount = itemCount
        for (i in 0 until itemCount) {
            // 1. 先计算出itemWidth和itemHeight
            val viewType = adapter!!.getItemViewType(i)
            val itemHeight: Int
            if (viewTypeHeightMap.containsKey(viewType)) {
                itemHeight = viewTypeHeightMap[viewType]!!
            } else {
                val itemView = recycler!!.getViewForPosition(i)
                addView(itemView)
                measureChildWithMargins(
                    itemView,
                    View.MeasureSpec.UNSPECIFIED,
                    View.MeasureSpec.UNSPECIFIED
                )
                itemHeight = getDecoratedMeasuredHeight(itemView)
                viewTypeHeightMap[viewType] = itemHeight
            }

            // 2. 组装Rect并保存
            val rect = Rect()
            rect.left = paddingLeft
            rect.top = tempPosition
            rect.right = width - paddingRight
            rect.bottom = rect.top + itemHeight
            locationRects.put(i, rect)
            attachedItems.put(i, false)
            tempPosition = tempPosition + itemHeight
        }

        if (itemCount == 0) {
            maxScroll = 0
        } else {
            computeMaxScroll()
        }
    }

    /**
     * 对外提供接口，找到第一个可视view的index
     */
    override fun findFirstVisibleItemPosition(): Int {
        val count = locationRects.size()
        val displayRect = Rect(0, scroll, width, height + scroll)
        for (i in 0 until count) {
            if (Rect.intersects(displayRect, locationRects.get(i)) && attachedItems.get(i)) {
                return i
            }
        }
        return 0
    }

    /**
     * 计算可滑动的最大值
     */
    private fun computeMaxScroll() {
        maxScroll = locationRects.get(locationRects.size() - 1).bottom - height
        if (maxScroll < 0) {
            maxScroll = 0
            return
        }

        val itemCount = itemCount
        var screenFilledHeight = 0
        for (i in itemCount - 1 downTo 0) {
            val rect = locationRects.get(i)
            screenFilledHeight = screenFilledHeight + (rect.bottom - rect.top)
            if (screenFilledHeight > height) {
                val extraSnapHeight = height - (screenFilledHeight - (rect.bottom - rect.top))
                maxScroll = maxScroll + extraSnapHeight
                break
            }
        }
    }

    /**
     * 初始化的时候，layout子View
     */
    private fun layoutItemsOnCreate(recycler: RecyclerView.Recycler) {
        val itemCount = itemCount
        val displayRect = Rect(0, scroll, width, height + scroll)
        for (i in 0 until itemCount) {
            val thisRect = locationRects.get(i)
            if (Rect.intersects(displayRect, thisRect)) {
                val childView = recycler.getViewForPosition(i)
                addView(childView)
                measureChildWithMargins(
                    childView,
                    View.MeasureSpec.UNSPECIFIED,
                    View.MeasureSpec.UNSPECIFIED
                )
                layoutItem(childView, locationRects.get(i))
                attachedItems.put(i, true)
                childView.pivotY = 0f
                childView.pivotX = (childView.measuredWidth / 2).toFloat()
                if (thisRect.top - scroll > height) {
                    break
                }
            }
        }
    }


    /**
     * 初始化的时候，layout子View
     */
    private fun layoutItemsOnScroll() {
        val childCount = childCount
        // 1. 已经在屏幕上显示的child
        val itemCount = itemCount
        val displayRect = Rect(0, scroll, width, height + scroll)
        var firstVisiblePosition = -1
        var lastVisiblePosition = -1
        for (i in childCount - 1 downTo 0) {
            val child = getChildAt(i) ?: continue
            val position = getPosition(child)
            if (!Rect.intersects(displayRect, locationRects.get(position))) {
                // 回收滑出屏幕的View
                removeAndRecycleView(child, recycler!!)
                attachedItems.put(position, false)
            } else {
                // Item还在显示区域内，更新滑动后Item的位置
                if (lastVisiblePosition < 0) {
                    lastVisiblePosition = position
                }

                if (firstVisiblePosition < 0) {
                    firstVisiblePosition = position
                } else {
                    firstVisiblePosition = Math.min(firstVisiblePosition, position)
                }

                layoutItem(child, locationRects.get(position)) //更新Item位置
            }
        }

        // 2. 复用View处理
        if (firstVisiblePosition > 0) {
            // 往前搜索复用
            for (i in firstVisiblePosition - 1 downTo 0) {
                if (Rect.intersects(displayRect, locationRects.get(i)) && !attachedItems.get(i)) {
                    reuseItemOnSroll(i, true)
                } else {
                    break
                }
            }
        }
        // 往后搜索复用
        for (i in lastVisiblePosition + 1 until itemCount) {
            if (Rect.intersects(displayRect, locationRects.get(i)) && !attachedItems.get(i)) {
                reuseItemOnSroll(i, false)
            } else {
                break
            }
        }
    }

    /**
     * 复用position对应的View
     */
    private fun reuseItemOnSroll(position: Int, addViewFromTop: Boolean) {
        val scrap = recycler!!.getViewForPosition(position)
        measureChildWithMargins(scrap, 0, 0)
        scrap.pivotY = 0f
        scrap.pivotX = (scrap.measuredWidth / 2).toFloat()

        if (addViewFromTop) {
            addView(scrap, 0)
        } else {
            addView(scrap)
        }
        // 将这个Item布局出来
        layoutItem(scrap, locationRects.get(position))
        attachedItems.put(position, true)
    }


    private fun layoutItem(child: View, rect: Rect) {
        val topDistance = scroll - rect.top
        val layoutTop: Int
        val layoutBottom: Int
        val itemHeight = rect.bottom - rect.top
        if (topDistance < itemHeight && topDistance > 0) {
            val rate1 = topDistance.toFloat() / itemHeight
            val rate2 = 1 - rate1 * rate1 / 3
            val rate3 = 1 - rate1 * rate1
            child.scaleX = rate2
            child.scaleY = rate2
            child.alpha = rate3
            layoutTop = 0
            layoutBottom = itemHeight
        } else {
            child.scaleX = 1f
            child.scaleY = 1f
            child.alpha = 1f

            layoutTop = rect.top - scroll
            layoutBottom = rect.bottom - scroll
        }
        layoutDecorated(child, rect.left, layoutTop, rect.right, layoutBottom)
    }

    override fun canScrollVertically(): Boolean {
        return true
    }

    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler?,
        state: RecyclerView.State?
    ): Int {
        if (itemCount == 0 || dy == 0 || locationRects.size()<1) {
            return super.scrollVerticallyBy(dy, recycler, state)
        }
        var travel = dy
        if (dy + scroll < 0) {
            travel = -scroll
        } else if (dy + scroll > maxScroll) {
            travel = maxScroll - scroll
        }
        scroll += travel //累计偏移量
        lastDy = dy
        if (!state!!.isPreLayout && childCount > 0) {
            layoutItemsOnScroll()
        }

        return travel
    }

    override fun onAttachedToWindow(view: RecyclerView?) {
        super.onAttachedToWindow(view)
        StartSnapHelper().attachToRecyclerView(view)
    }

    override fun onScrollStateChanged(state: Int) {
        if (state == RecyclerView.SCROLL_STATE_DRAGGING) {
            needSnap = true
        }
        super.onScrollStateChanged(state)
    }

    fun findSnapView(): View? {
        return if (childCount > 0) {
            getChildAt(0)
        } else null
    }
}
