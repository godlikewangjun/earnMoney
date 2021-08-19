package com.wj.makebai.ui.weight;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Mycroft on 2017/1/11.
 */
public final class FlowLayoutManager extends RecyclerView.LayoutManager {


    public FlowLayoutManager() {
        setAutoMeasureEnabled(true);
    }

    public FlowLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        setAutoMeasureEnabled(true);
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getItemCount() == 0) {
            detachAndScrapAttachedViews(recycler);
            return;
        }

        // 如果正在进行动画，则不进行布局
        if (getChildCount() == 0 && state.isPreLayout()) {
            return;
        }

        detachAndScrapAttachedViews(recycler);

        // 进行布局
        layout(recycler, state, 0);
    }

    /**
     * @return 可以纵向滑动
     */
    @Override
    public boolean canScrollVertically() {
        return true;
    }

    /**
     * 纵向偏移量
     */
    private int mVerticalOffset = 0;

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        // 如果滑动距离为0, 或是没有任何item view, 则不移动
        if (dy == 0 || getChildCount() == 0) {
            return 0;
        }

        // 实际滑动的距离，到达边界时需要进行修正
        int realOffset = dy;
        if (mVerticalOffset + realOffset < 0) {
            realOffset = -mVerticalOffset;
        } else if (realOffset > 0) {
            // 手指上滑，判断是否到达下边界
            final View lastChildView = getChildAt(getChildCount() - 1);
            if (getPosition(lastChildView) == getItemCount() - 1) {
                int maxBottom = getDecoratedBottom(lastChildView);

                int lastChildTop = getDecoratedTop(lastChildView);
                for (int i = getChildCount() - 2; i >= 0; i--) {
                    final View child = getChildAt(i);
                    if (getDecoratedTop(child) == lastChildTop) {
                        maxBottom = Math.max(maxBottom, getDecoratedBottom(getChildAt(i)));
                    } else {
                        break;
                    }
                }

                int gap = getHeight() - getPaddingBottom() - maxBottom;
                if (gap > 0) {
                    realOffset = -gap;
                } else if (gap == 0) {
                    realOffset = 0;
                } else {
                    realOffset = Math.min(realOffset, -gap);
                }
            }
        }

        realOffset = layout(recycler, state, realOffset);

        mVerticalOffset += realOffset;

        offsetChildrenVertical(-realOffset);

        return realOffset;
    }

    private final SparseArray<Rect> mItemRects = new SparseArray<>();

    /**
     * 布局操作
     *
     * @param recycler
     * @param state
     * @param dy       用于判断回收、显示item, 对布局/定位本身没有影响
     * @return
     */
    private int layout(RecyclerView.Recycler recycler, RecyclerView.State state, int dy) {

        int firstVisiblePos = 0;

        // 纵向计算偏移量，考虑padding
        int topOffset = getPaddingTop();
        // 横向计算偏移量，考虑padding
        int leftOffset = getPaddingLeft();
        // 行高，以最高的item作为参考
        int maxLineHeight = 0;

        int childCount = getChildCount();

        // 当是滑动进入时（在onLayoutChildren方法里面，移除了所有的child view, 所以只有可能从scrollVerticalBy方法里面进入这个方法）
        if (childCount > 0) {
            // 计算滑动后，需要被回收的child view

            if (dy > 0) {
                // 手指上滑，可能需要回收顶部的view
                for (int i = 0; i < childCount; i++) {
                    final View child = getChildAt(i);
                    if (getDecoratedBottom(child) - dy < topOffset) {
                        // 超出顶部的item
                        removeAndRecycleView(child, recycler);
                        i--;
                        childCount--;
                    } else {
                        firstVisiblePos = i;
                        break;
                    }
                }
            } else if (dy < 0) {
                // 手指下滑，可能需要回收底部的view
                for (int i = childCount - 1; i >= 0; i--) {
                    final View child = getChildAt(i);
                    if (getDecoratedTop(child) - dy > getHeight() - getPaddingBottom()) {
                        // 超出底部的item
                        removeAndRecycleView(child, recycler);
                    } else {
                        break;
                    }
                }
            }
        }

        // 进行布局
        if (dy >= 0) {
            // 手指上滑，按顺序布局item

            int minPosition = firstVisiblePos;
            if (getChildCount() > 0) {
                final View lastVisibleChild = getChildAt(getChildCount() - 1);
                // 修正当前偏移量
                topOffset = getDecoratedTop(lastVisibleChild);
                leftOffset = getDecoratedRight(lastVisibleChild);
                // 修正第一个应该进行布局的item view
                minPosition = getPosition(lastVisibleChild) + 1;

                // 使用排在最后一行的所有的child view进行高度修正
                maxLineHeight = Math.max(maxLineHeight, getDecoratedMeasurementVertical(lastVisibleChild));
                for (int i = getChildCount() - 2; i >= 0; i--) {
                    final View child = getChildAt(i);
                    if (getDecoratedTop(child) == topOffset) {
                        maxLineHeight = Math.max(maxLineHeight, getDecoratedMeasurementVertical(child));
                    } else {
                        break;
                    }
                }
            }

            // 布局新的 item view
            for (int i = minPosition; i < getItemCount(); i++) {

                // 获取item view, 添加、测量、获取尺寸
                final View itemView = recycler.getViewForPosition(i);
                addView(itemView);
                measureChildWithMargins(itemView, 0, 0);

                final int sizeHorizontal = getDecoratedMeasurementHorizontal(itemView);
                final int sizeVertical = getDecoratedMeasurementVertical(itemView);
                // 进行布局
                if (leftOffset + sizeHorizontal <= getHorizontalSpace()) {
                    // 如果这行能够布局，则往后排
                    // layout
                    layoutDecoratedWithMargins(itemView, leftOffset, topOffset, leftOffset + sizeHorizontal, topOffset + sizeVertical);
                    final Rect rect = new Rect(leftOffset, topOffset + mVerticalOffset, leftOffset + sizeHorizontal, topOffset + sizeVertical + mVerticalOffset);
                    // 保存布局信息
                    mItemRects.put(i, rect);

                    // 修正横向计算偏移量
                    leftOffset += sizeHorizontal;
                    maxLineHeight = Math.max(maxLineHeight, sizeVertical);
                } else {
                    // 如果当前行不够，则往下一行挪
                    // 修正计算偏移量、行高
                    topOffset += maxLineHeight;
                    maxLineHeight = 0;
                    leftOffset = getPaddingLeft();

                    // layout
                    if (topOffset - dy > getHeight() - getPaddingBottom()) {
                        // 如果超出下边界
                        // 移除并回收该item view
                        removeAndRecycleView(itemView, recycler);
                        break;
                    } else {
                        // 如果没有超出下边界，则继续布局
                        layoutDecoratedWithMargins(itemView, leftOffset, topOffset, leftOffset + sizeHorizontal, topOffset + sizeVertical);
                        final Rect rect = new Rect(leftOffset, topOffset + mVerticalOffset, leftOffset + sizeHorizontal, topOffset + sizeVertical + mVerticalOffset);
                        // 保存布局信息
                        mItemRects.put(i, rect);
                        // 修正计算偏移量、行高
                        leftOffset += sizeHorizontal;
                        maxLineHeight = Math.max(maxLineHeight, sizeVertical);
                    }
                }
            }
        } else {
            // 手指下滑，逆序布局新的child
            int maxPos = getItemCount() - 1;
            if (getChildCount() > 0) {
                maxPos = getPosition(getChildAt(0)) - 1;
            }

            for (int i = maxPos; i >= 0; i--) {
                Rect rect = mItemRects.get(i);
                // 判断底部是否在上边界下面
                if (rect.bottom - mVerticalOffset - dy >= getPaddingTop()) {
                    // 获取item view, 添加、设置尺寸、布局
                    final View itemView = recycler.getViewForPosition(i);
                    addView(itemView, 0);
                    measureChildWithMargins(itemView, 0, 0);
                    layoutDecoratedWithMargins(itemView, rect.left, rect.top - mVerticalOffset, rect.right, rect.bottom - mVerticalOffset);
                }
            }
        }

        return dy;
    }

    /* 对数据改变时的一些修正 */

    @Override
    public void onItemsChanged(RecyclerView recyclerView) {
        mVerticalOffset = 0;
        mItemRects.clear();
    }

    @Override
    public void onItemsAdded(RecyclerView recyclerView, int positionStart, int itemCount) {
        mVerticalOffset = 0;
        mItemRects.clear();
    }

    @Override
    public void onItemsMoved(RecyclerView recyclerView, int from, int to, int itemCount) {
        mVerticalOffset = 0;
        mItemRects.clear();
    }

    @Override
    public void onItemsRemoved(RecyclerView recyclerView, int positionStart, int itemCount) {
        mVerticalOffset = 0;
        mItemRects.clear();
    }

    @Override
    public void onItemsUpdated(RecyclerView recyclerView, int positionStart, int itemCount) {
        mVerticalOffset = 0;
        mItemRects.clear();
    }

    @Override
    public void onItemsUpdated(RecyclerView recyclerView, int positionStart, int itemCount, Object payload) {
        mVerticalOffset = 0;
        mItemRects.clear();
    }

    @Override
    public void onAdapterChanged(RecyclerView.Adapter oldAdapter, RecyclerView.Adapter newAdapter) {
        mVerticalOffset = 0;
        mItemRects.clear();
    }

    /**
     * 获取 child view 横向上需要占用的空间，margin计算在内
     *
     * @param view item view
     * @return child view 横向占用的空间
     */
    private int getDecoratedMeasurementHorizontal(View view) {
        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
                view.getLayoutParams();
        return getDecoratedMeasuredWidth(view) + params.leftMargin
                + params.rightMargin;
    }

    /**
     * 获取 child view 纵向上需要占用的空间，margin计算在内
     *
     * @param view item view
     * @return child view 纵向占用的空间
     */
    private int getDecoratedMeasurementVertical(View view) {
        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
        return getDecoratedMeasuredHeight(view) + params.topMargin + params.bottomMargin;
    }

    /**
     * @return 横向的可布局的空间
     */
    private int getHorizontalSpace() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }
}
