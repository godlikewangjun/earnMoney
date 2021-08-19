package com.wj.makebai.ui.adapter

import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemLongClickListener
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.tools.DateUtils
import com.wj.commonlib.utils.CommTools
import com.wj.makebai.R
import com.wj.ui.base.BaseAdapter
import com.wj.ui.base.viewhoder.CustomVhoder
import kotlinx.android.synthetic.main.gv_filter_image.view.*
import java.util.*

/**
 * 图片加载
 */
class GridImageAdapter(
    /**
     * 点击添加图片跳转
     */
    private val mOnAddPicClickListener: onAddPicClickListener
) : BaseAdapter() {
    var list = ArrayList<LocalMedia>()
    private var selectMax = 9

    interface onAddPicClickListener {
        fun onAddPicClick()
    }

    /**
     * 删除
     */
    fun delete(position: Int) {
        try {
            if (position != RecyclerView.NO_POSITION && list!!.size > position) {
                list.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, list!!.size)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setSelectMax(selectMax: Int):GridImageAdapter {
        this.selectMax = selectMax
        return this
    }

    val data: List<LocalMedia>
        get() = if (list == null) ArrayList() else list!!

    fun remove(position: Int) {
        if (list != null) {
            list.removeAt(position)
        }
    }

    override fun getItemCount(): Int {
        return if (list.size < selectMax) {
            list.size + 1
        } else {
            list.size
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isShowAddItem(position)) {
            TYPE_CAMERA
        } else {
            TYPE_PICTURE
        }
    }

    
    private fun isShowAddItem(position: Int): Boolean {
        val size = if (list.size == 0) 0 else list.size
        return position == size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        super.onCreateViewHolder(parent, viewType)
        return CustomVhoder( inflater!!.inflate(R.layout.gv_filter_image,parent,false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        if (getItemViewType(position) == TYPE_CAMERA) {
            holder.itemView.fiv.setImageResource(R.drawable.ic_add_image)
            holder.itemView.fiv.setOnClickListener { v: View? -> mOnAddPicClickListener.onAddPicClick() }
            holder.itemView.iv_del.visibility = View.INVISIBLE
            holder.itemView.ad_size.visibility = View.INVISIBLE
        } else {
            holder.itemView.ad_size.visibility = View.VISIBLE
             holder.itemView.iv_del.visibility = View.VISIBLE
             holder.itemView.iv_del.setOnClickListener { view: View? ->
                val index = holder.adapterPosition
                // 这里有时会返回-1造成数据下标越界,具体可参考getAdapterPosition()源码，
// 通过源码分析应该是bindViewHolder()暂未绘制完成导致，知道原因的也可联系我~感谢
                if (index != RecyclerView.NO_POSITION && list!!.size > index) {
                    list.removeAt(index)
                    notifyItemRemoved(index)
                    notifyItemRangeChanged(index, list.size)
                }
            }
            val media = list!![position]
            if (media == null
                || TextUtils.isEmpty(media.path)
            ) {
                return
            }
            val chooseModel = media.chooseModel
            val path: String
            path = if (media.isCut && !media.isCompressed) { // 裁剪过
                media.cutPath
            } else if (media.isCompressed || media.isCut && media.isCompressed) { // 压缩过,或者裁剪同时压缩过,以最终压缩过图片为准
                media.compressPath
            } else { // 原图
                media.path
            }
            val duration = media.duration
            holder.itemView.tv_duration.visibility =
                if (PictureMimeType.eqVideo(media.mimeType)) View.VISIBLE else View.GONE
            if (chooseModel == PictureMimeType.ofAudio()) {
                 holder.itemView.tv_duration.visibility = View.VISIBLE
                 holder.itemView.tv_duration.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.picture_icon_audio,
                    0,
                    0,
                    0
                )
            } else {
                 holder.itemView.tv_duration.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.picture_icon_video,
                    0,
                    0,
                    0
                )
            }
             holder.itemView.tv_duration.text = DateUtils.formatDurationTime(
                duration
            )
            if (chooseModel == PictureMimeType.ofAudio()) {
                holder.itemView.fiv.setImageResource(R.drawable.picture_audio_placeholder)
            } else {
                Glide.with(holder.itemView.context)
                    .load(path)
                    .centerCrop()
                    .placeholder(R.color.gray)
                    .into(holder.itemView.fiv)
                holder.itemView.ad_size.text=CommTools.kbM(media.size)
            }
            //itemView 的点击事件
            if (mItemClickListener != null) {
                holder.itemView.setOnClickListener { v: View? ->
                    val adapterPosition = holder.adapterPosition
                    mItemClickListener!!.onItemClick(null, v, adapterPosition, 0)
                }
            }
            if (mItemLongClickListener != null) {
                holder.itemView.setOnLongClickListener { v: View? ->
                    val adapterPosition = holder.adapterPosition
                    mItemLongClickListener!!.onItemLongClick(
                        null,
                        holder.itemView,
                        adapterPosition,
                        0
                    )
                    true
                }
            }
        }
    }

    private var mItemClickListener: AdapterView.OnItemClickListener? = null
    fun setOnItemClickListener(l: AdapterView.OnItemClickListener?) {
        mItemClickListener = l
    }

    private var mItemLongClickListener: OnItemLongClickListener? = null
    fun setItemLongClickListener(l: OnItemLongClickListener?) {
        mItemLongClickListener = l
    }

    companion object {
        const val TYPE_CAMERA = 1
        const val TYPE_PICTURE = 2
    }

}