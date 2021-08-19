package com.wj.makebai.ui.weight.channel

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.MotionEventCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.wj.commonlib.data.mode.ArticleKeyMode
import com.wj.makebai.R
import com.wj.makebai.ui.weight.channel.helper.OnDragVHListener
import com.wj.makebai.ui.weight.channel.helper.OnItemMoveListener
import com.wj.ui.base.viewhoder.CustomVhoder

/**
 *
 * @author dchain
 * @version 1.0
 * @date 2019/9/29
 */
/**
 * 拖拽排序 + 增删
 * Created by YoKeyword on 15/12/28.
 */
class ChannelAdapter(context: Context, private val mItemTouchHelper: ItemTouchHelper, val mMyChannelItems: MutableList<ArticleKeyMode>, val mOtherChannelItems: MutableList<ArticleKeyMode>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    OnItemMoveListener {

    // touch 点击开始时间
    private var startTime: Long = 0

    private val mInflater: LayoutInflater = LayoutInflater.from(context)

    // 是否为 编辑 模式
    private var isEditMode: Boolean = false

    // 我的频道点击事件
    private var mChannelItemClickListener: OnMyChannelItemClickListener? = null

    private val delayHandler = Handler()

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {    // 我的频道 标题部分
            TYPE_MY_CHANNEL_HEADER
        } else if (position == mMyChannelItems.size + 1) {    // 其他频道 标题部分
            TYPE_OTHER_CHANNEL_HEADER
        } else if (position > 0 && position < mMyChannelItems.size + 1) {
            TYPE_MY
        } else {
            TYPE_OTHER
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        when (viewType) {
            TYPE_MY_CHANNEL_HEADER -> {
                view = mInflater.inflate(R.layout.item_my_channel_header, parent, false)
                val holder = MyChannelHeaderViewHolder(view)
                holder.tvBtnEdit.setOnClickListener {
                    if (!isEditMode) {
                        startEditMode(parent as RecyclerView)
                        holder.tvBtnEdit.setText(R.string.finish)
                    } else {
                        cancelEditMode(parent as RecyclerView)
                        holder.tvBtnEdit.setText(R.string.edit)
                    }
                }
                return holder
            }

            TYPE_MY -> {
                view = mInflater.inflate(R.layout.item_my, parent, false)
                val myHolder = MyViewHolder(view)

                myHolder.textView.setOnClickListener { v ->
                    val position = myHolder.adapterPosition
                    if (isEditMode && position != 1) {
                        val recyclerView = parent as RecyclerView
                        val targetView = recyclerView.layoutManager!!.findViewByPosition(mMyChannelItems.size + COUNT_PRE_OTHER_HEADER)
                        val currentView = recyclerView.layoutManager!!.findViewByPosition(position)
                        // 如果targetView不在屏幕内,则indexOfChild为-1  此时不需要添加动画,因为此时notifyItemMoved自带一个向目标移动的动画
                        // 如果在屏幕内,则添加一个位移动画
                        if (recyclerView.indexOfChild(targetView) >= 0) {
                            val targetX: Int
                            val targetY: Int

                            val manager = recyclerView.layoutManager
                            val spanCount = (manager as GridLayoutManager).spanCount

                            // 移动后 高度将变化 (我的频道Grid 最后一个item在新的一行第一个)
                            if ((mMyChannelItems.size - COUNT_PRE_MY_HEADER) % spanCount == 0) {
                                val preTargetView = recyclerView.layoutManager!!.findViewByPosition(mMyChannelItems.size + COUNT_PRE_OTHER_HEADER - 1)
                                targetX = preTargetView!!.left
                                targetY = preTargetView.top
                            } else {
                                targetX = targetView!!.left
                                targetY = targetView.top
                            }

                            moveMyToOther(myHolder)
                            startAnimation(recyclerView, currentView!!, targetX.toFloat(), targetY.toFloat())

                        } else {
                            moveMyToOther(myHolder)
                        }
                    } else {
                        mChannelItemClickListener!!.onItemClick(v, position - COUNT_PRE_MY_HEADER)
                    }
                }

                myHolder.textView.setOnLongClickListener {
                    if (!isEditMode) {
                        val recyclerView = parent as RecyclerView
                        startEditMode(recyclerView)

                        // header 按钮文字 改成 "完成"
                        val view = recyclerView.getChildAt(0)
                        if (view === recyclerView.layoutManager!!.findViewByPosition(0)) {
                            val tvBtnEdit = view.findViewById<View>(R.id.tv_btn_edit) as TextView
                            tvBtnEdit.setText(R.string.finish)
                        }
                    }

                    mItemTouchHelper.startDrag(myHolder)
                    true
                }

                myHolder.textView.setOnTouchListener { v, event ->
                    if (isEditMode) {
                        when (MotionEventCompat.getActionMasked(event)) {
                            MotionEvent.ACTION_DOWN -> startTime = System.currentTimeMillis()
                            MotionEvent.ACTION_MOVE -> if (System.currentTimeMillis() - startTime > SPACE_TIME) {
                                mItemTouchHelper.startDrag(myHolder)
                            }
                            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> startTime = 0
                        }

                    }
                    false
                }
                return myHolder
            }

            TYPE_OTHER_CHANNEL_HEADER -> {
                view = mInflater.inflate(R.layout.item_other_channel_header, parent, false)
                return object : RecyclerView.ViewHolder(view) {

                }
            }

            TYPE_OTHER -> {
                view = mInflater.inflate(R.layout.item_other, parent, false)
                val otherHolder = OtherViewHolder(view)
                otherHolder.textView.setOnClickListener {
                    if(!isEditMode) return@setOnClickListener
                    val recyclerView = parent as RecyclerView
                    val manager = recyclerView.layoutManager
                    val currentPiosition = otherHolder.adapterPosition
                    // 如果RecyclerView滑动到底部,移动的目标位置的y轴 - height
                    val currentView = manager!!.findViewByPosition(currentPiosition)
                    // 目标位置的前一个item  即当前MyChannel的最后一个
                    val preTargetView = manager.findViewByPosition(mMyChannelItems.size - 1 + COUNT_PRE_MY_HEADER)

                    // 如果targetView不在屏幕内,则为-1  此时不需要添加动画,因为此时notifyItemMoved自带一个向目标移动的动画
                    // 如果在屏幕内,则添加一个位移动画
                    if (recyclerView.indexOfChild(preTargetView) >= 0) {
                        var targetX = preTargetView!!.left
                        var targetY = preTargetView.top

                        val targetPosition = mMyChannelItems.size - 1 + COUNT_PRE_OTHER_HEADER

                        val gridLayoutManager = manager as GridLayoutManager
                        val spanCount = gridLayoutManager.spanCount
                        // target 在最后一行第一个
                        if ((targetPosition - COUNT_PRE_MY_HEADER) % spanCount == 0) {
                            val targetView = manager.findViewByPosition(targetPosition)
                            targetX = targetView!!.left
                            targetY = targetView.top
                        } else {
                            targetX += preTargetView.width

                            // 最后一个item可见
                            if (gridLayoutManager.findLastVisibleItemPosition() == itemCount - 1) {
                                // 最后的item在最后一行第一个位置
                                if ((itemCount - 1 - mMyChannelItems.size - COUNT_PRE_OTHER_HEADER) % spanCount == 0) {
                                    // RecyclerView实际高度 > 屏幕高度 && RecyclerView实际高度 < 屏幕高度 + item.height
                                    val firstVisiblePostion = gridLayoutManager.findFirstVisibleItemPosition()
                                    if (firstVisiblePostion == 0) {
                                        // FirstCompletelyVisibleItemPosition == 0 即 内容不满一屏幕 , targetY值不需要变化
                                        // // FirstCompletelyVisibleItemPosition != 0 即 内容满一屏幕 并且 可滑动 , targetY值 + firstItem.getTop
                                        if (gridLayoutManager.findFirstCompletelyVisibleItemPosition() != 0) {
                                            val offset = -recyclerView.getChildAt(0).top - recyclerView.paddingTop
                                            targetY += offset
                                        }
                                    } else { // 在这种情况下 并且 RecyclerView高度变化时(即可见第一个item的 position != 0),
                                        // 移动后, targetY值  + 一个item的高度
                                        targetY += preTargetView.height
                                    }
                                }
                            } else {
                                println("current--No")
                            }
                        }

                        // 如果当前位置是otherChannel可见的最后一个
                        // 并且 当前位置不在grid的第一个位置
                        // 并且 目标位置不在grid的第一个位置

                        // 则 需要延迟250秒 notifyItemMove , 这是因为这种情况 , 并不触发ItemAnimator , 会直接刷新界面
                        // 导致我们的位移动画刚开始,就已经notify完毕,引起不同步问题
                        if (currentPiosition == gridLayoutManager.findLastVisibleItemPosition()
                            && (currentPiosition - mMyChannelItems.size - COUNT_PRE_OTHER_HEADER) % spanCount != 0
                            && (targetPosition - COUNT_PRE_MY_HEADER) % spanCount != 0) {
                            moveOtherToMyWithDelay(otherHolder)
                        } else {
                            moveOtherToMy(otherHolder)
                        }
                        startAnimation(recyclerView, currentView!!, targetX.toFloat(), targetY.toFloat())

                    } else {
                        moveOtherToMy(otherHolder)
                    }
                }
                return otherHolder
            }
        }
        return CustomVhoder(View(parent.context))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MyViewHolder) {

            holder.textView.text = mMyChannelItems[position - COUNT_PRE_MY_HEADER].describe
            if (isEditMode && position > 0) {
                holder.imgEdit.visibility = View.VISIBLE
            } else {
                holder.imgEdit.visibility = View.INVISIBLE
            }
            if (position == 1) {
                holder.textView.setTextColor(holder.itemView.context.resources.getColor(R.color.text_gray))

            }

        } else if (holder is OtherViewHolder) {

            holder.textView.text = "＋ " + mOtherChannelItems[position - mMyChannelItems.size - COUNT_PRE_OTHER_HEADER].describe

        } else if (holder is MyChannelHeaderViewHolder) {

            if (isEditMode) {
                holder.tvBtnEdit.setText(R.string.finish)
            } else {
                holder.tvBtnEdit.setText(R.string.edit)
            }
        }
    }

    override fun getItemCount(): Int {
        // 我的频道  标题 + 我的频道.size + 其他频道 标题 + 其他频道.size
        return mMyChannelItems.size + mOtherChannelItems.size + COUNT_PRE_OTHER_HEADER
    }

    /**
     * 开始增删动画
     */
    private fun startAnimation(recyclerView: RecyclerView, currentView: View, targetX: Float, targetY: Float) {
        val viewGroup = recyclerView.parent as ViewGroup
        val mirrorView = addMirrorView(viewGroup, recyclerView, currentView)

        val animation = getTranslateAnimator(
            targetX - currentView.left, targetY - currentView.top)
        currentView.visibility = View.INVISIBLE
        mirrorView.startAnimation(animation)

        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                viewGroup.removeView(mirrorView)
                if (currentView.visibility == View.INVISIBLE) {
                    currentView.visibility = View.VISIBLE
                }
            }

            override fun onAnimationRepeat(animation: Animation) {

            }
        })
    }

    /**
     * 我的频道 移动到 其他频道
     *
     * @param myHolder
     */
    private fun moveMyToOther(myHolder: MyViewHolder) {
        val position = myHolder.adapterPosition

        val startPosition = position - COUNT_PRE_MY_HEADER
        if (startPosition > mMyChannelItems.size - 1) {
            return
        }
        val item = mMyChannelItems[startPosition]
        mMyChannelItems.removeAt(startPosition)
        mOtherChannelItems.add(0, item)

        notifyItemMoved(position, mMyChannelItems.size + COUNT_PRE_OTHER_HEADER)
    }

    /**
     * 其他频道 移动到 我的频道
     *
     * @param otherHolder
     */
    private fun moveOtherToMy(otherHolder: OtherViewHolder) {
        val position = processItemRemoveAdd(otherHolder)
        if (position == -1) {
            return
        }
        notifyItemMoved(position, mMyChannelItems.size - 1 + COUNT_PRE_MY_HEADER)
    }

    /**
     * 其他频道 移动到 我的频道 伴随延迟
     *
     * @param otherHolder
     */
    private fun moveOtherToMyWithDelay(otherHolder: OtherViewHolder) {
        val position = processItemRemoveAdd(otherHolder)
        if (position == -1) {
            return
        }
        delayHandler.postDelayed({ notifyItemMoved(position, mMyChannelItems.size - 1 + COUNT_PRE_MY_HEADER) },
            ANIM_TIME
        )
    }

    private fun processItemRemoveAdd(otherHolder: OtherViewHolder): Int {
        val position = otherHolder.adapterPosition

        val startPosition = position - mMyChannelItems.size - COUNT_PRE_OTHER_HEADER
        if (startPosition > mOtherChannelItems.size - 1) {
            return -1
        }
        val item = mOtherChannelItems[startPosition]
        mOtherChannelItems.removeAt(startPosition)
        mMyChannelItems.add(item)
        return position
    }


    /**
     * 添加需要移动的 镜像View
     */
    private fun addMirrorView(parent: ViewGroup, recyclerView: RecyclerView, view: View): ImageView {
        /**
         * 我们要获取cache首先要通过setDrawingCacheEnable方法开启cache，然后再调用getDrawingCache方法就可以获得view的cache图片了。
         * buildDrawingCache方法可以不用调用，因为调用getDrawingCache方法时，若果cache没有建立，系统会自动调用buildDrawingCache方法生成cache。
         * 若想更新cache, 必须要调用destoryDrawingCache方法把旧的cache销毁，才能建立新的。
         * 当调用setDrawingCacheEnabled方法设置为false, 系统也会自动把原来的cache销毁。
         */
        view.destroyDrawingCache()
        view.isDrawingCacheEnabled = true
        val mirrorView = ImageView(recyclerView.context)
        val bitmap = Bitmap.createBitmap(view.drawingCache)
        mirrorView.setImageBitmap(bitmap)
        view.isDrawingCacheEnabled = false
        val locations = IntArray(2)
        view.getLocationOnScreen(locations)
        val parenLocations = IntArray(2)
        recyclerView.getLocationOnScreen(parenLocations)
        val params = FrameLayout.LayoutParams(bitmap.width, bitmap.height)
        params.setMargins(locations[0], locations[1] - parenLocations[1], 0, 0)
        parent.addView(mirrorView, params)

        return mirrorView
    }
    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        val item = mMyChannelItems[fromPosition - COUNT_PRE_MY_HEADER]
        mMyChannelItems.removeAt(fromPosition - COUNT_PRE_MY_HEADER)
        mMyChannelItems.add(toPosition - COUNT_PRE_MY_HEADER, item)
        notifyItemMoved(fromPosition, toPosition)
    }

    /**
     * 开启编辑模式
     *
     * @param parent
     */
    private fun startEditMode(parent: RecyclerView) {
        isEditMode = true

        val visibleChildCount = parent.childCount
        for (i in 2 until visibleChildCount) {
            val view = parent.getChildAt(i)
            val imgEdit = view.findViewById<View>(R.id.img_edit)
            if (imgEdit != null) {
                imgEdit.visibility = View.VISIBLE
            }
        }
    }

    /**
     * 完成编辑模式
     *
     * @param parent
     */
    private fun cancelEditMode(parent: RecyclerView) {
        isEditMode = false

        val visibleChildCount = parent.childCount
        for (i in 0 until visibleChildCount) {
            val view = parent.getChildAt(i)
            val imgEdit = view.findViewById<View>(R.id.img_edit)
            if (imgEdit != null) {
                imgEdit.visibility = View.INVISIBLE
            }
        }
    }

    /**
     * 获取位移动画
     */
    private fun getTranslateAnimator(targetX: Float, targetY: Float): TranslateAnimation {
        val translateAnimation = TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0f,
            Animation.ABSOLUTE, targetX,
            Animation.RELATIVE_TO_SELF, 0f,
            Animation.ABSOLUTE, targetY)
        // RecyclerView默认移动动画250ms 这里设置360ms 是为了防止在位移动画结束后 remove(view)过早 导致闪烁
        translateAnimation.duration =
            ANIM_TIME
        translateAnimation.fillAfter = true
        return translateAnimation
    }

    interface OnMyChannelItemClickListener {
        fun onItemClick(v: View, position: Int)
    }

    fun setOnMyChannelItemClickListener(listener: OnMyChannelItemClickListener) {
        this.mChannelItemClickListener = listener
    }

    /**
     * 我的频道
     */
    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), OnDragVHListener {
        val textView: TextView = itemView.findViewById<View>(R.id.tv) as TextView
        val imgEdit: ImageView = itemView.findViewById<View>(R.id.img_edit) as ImageView

        /**
         * item 被选中时
         */
        override fun onItemSelected() {
            textView.setBackgroundResource(R.drawable.bg_channel_p)
        }

        /**
         * item 取消选中时
         */
        override fun onItemFinish() {
            textView.setBackgroundResource(R.drawable.bg_channel)
        }
    }

    /**
     * 其他频道
     */
    inner class OtherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById<View>(R.id.tv) as TextView

    }

    /**
     * 我的频道  标题部分
     */
    inner class MyChannelHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvBtnEdit: TextView = itemView.findViewById<View>(R.id.tv_btn_edit) as TextView

    }

    companion object {
        // 我的频道 标题部分
        val TYPE_MY_CHANNEL_HEADER = 0
        // 我的频道
        val TYPE_MY = 1
        // 其他频道 标题部分
        val TYPE_OTHER_CHANNEL_HEADER = 2
        // 其他频道
        val TYPE_OTHER = 3

        // 我的频道之前的header数量  该demo中 即标题部分 为 1
        private val COUNT_PRE_MY_HEADER = 1
        // 其他频道之前的header数量  该demo中 即标题部分 为 COUNT_PRE_MY_HEADER + 1
        private val COUNT_PRE_OTHER_HEADER = COUNT_PRE_MY_HEADER + 1

        private val ANIM_TIME = 360L
        // touch 间隔时间  用于分辨是否是 "点击"
        private val SPACE_TIME: Long = 100
    }
}