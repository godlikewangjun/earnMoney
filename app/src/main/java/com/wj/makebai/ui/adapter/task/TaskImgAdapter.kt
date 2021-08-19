package com.wj.makebai.ui.adapter.task

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.abase.util.AbViewUtil
import com.abase.util.Tools
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.listener.OnResultCallbackListener
import com.wj.commonlib.data.mode.TaskImageMode
import com.wj.commonlib.ui.ViewControl
import com.wj.commonlib.utils.GlideEngine
import com.wj.ktutils.showTip
import com.wj.makebai.R
import com.wj.makebai.ui.weight.WaterMarkBg
import com.wj.ui.base.BaseAdapter
import com.wj.ui.base.viewhoder.CustomVhoder
import kotlinx.android.synthetic.main.activity_images_zip.*
import kotlinx.android.synthetic.main.imageview_layout.view.*

/**
 * 截图任务
 * @author Administrator
 * @version 1.0
 * @date 2020/8/17
 */
class TaskImgAdapter() : BaseAdapter() {
    private var imgs = ArrayList<TaskImageMode>()
    var type = 0
    var imageChange: ((ArrayList<String>) -> Unit?)? = null
    var isGetTask = false//是否已经领取任务了
    var maxSelect = 0

    constructor(imgs: ArrayList<TaskImageMode>) : this() {
        this.imgs = imgs
        maxSelect = imgs.size * 2
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        super.onCreateViewHolder(parent, viewType)
        return CustomVhoder(inflater!!.inflate(R.layout.imageview_layout, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        if (type == 0 || type != 0 && position < itemCount - 1) {
            val layoutParams = holder.itemView.layoutParams
            layoutParams.height = AbViewUtil.dp2px(context!!, 180f)
            holder.itemView.layoutParams = layoutParams
            glide!!.load(imgs[position].path).into(holder.itemView.imgView)

            if (imgs[position].type == 0) {
                holder.itemView.mark.setBackgroundDrawable(
                    WaterMarkBg(
                        context!!,
                        arrayListOf("示例"), 45, 14
                    )
                )
                holder.itemView.iv_del.visibility = View.GONE


                val imgStr = ArrayList<String>()
                for (index in imgs) if (index.type == 0) imgStr.add(index.path)
                holder.itemView.setOnClickListener {
                    ViewControl.photoViewDialog(
                        context!!,
                        position,
                        imgStr
                    )
                }
            } else {
                holder.itemView.mark.setBackgroundDrawable(null)
                holder.itemView.setOnClickListener(null)

                if (imgs[position].type != 1) {
                    holder.itemView.tv_des.visibility = View.VISIBLE
                    holder.itemView.iv_del.visibility = View.GONE
                } else {
                    holder.itemView.tv_des.visibility = View.GONE
                    holder.itemView.iv_del.visibility = View.VISIBLE
                }
            }
        } else if (type != 0 && position == itemCount - 1) {
            holder.itemView.iv_del.visibility = View.GONE
            holder.itemView.visibility =
                if (maxSelect == imgs.size) View.GONE else View.VISIBLE

            holder.itemView.imgView.setImageResource(R.drawable.addimg)
            holder.itemView.setOnClickListener {
                if (isGetTask) return@setOnClickListener context!!.showTip("请先领取任务")
                PictureSelector.create(context as Activity)
                    .openGallery(PictureMimeType.ofImage())
                    .imageSpanCount(3)
                    .compress(false)
                    .maxSelectNum(maxSelect - imgs.size)
                    .loadImageEngine(GlideEngine.createGlideEngine()) // 请参考Demo GlideEngine.java
                    .forResult { result ->
                        for (media in result) {
                            val path: String = if (media.isCut && !media.isCompressed) { // 裁剪过
                                media.cutPath
                            } else if (media.isCompressed || media.isCut && media.isCompressed) { // 压缩过,或者裁剪同时压缩过,以最终压缩过图片为准
                                media.compressPath
                            } else { // 原图
                                media.path
                            }
                            imgs.add(TaskImageMode(1, path))
                        }
                        notifyDataSetChanged()
                        imageChange?.invoke(getSubmitInfo())
                    }
            }
        }

        holder.itemView.iv_del.setOnClickListener {
            imgs.removeAt(position)
            notifyItemRemoved(position)
            imageChange?.invoke(getSubmitInfo())
        }
    }

    override fun getItemCount(): Int {
        return if (type == 0) imgs.size else {
            imgs.size + 1
        }
    }

    /**
     * 获取提交的截图信息
     */
    private fun getSubmitInfo(): ArrayList<String> {
        val submitImg = ArrayList<String>()
        for (index in imgs) {
            if (index.type == 1) submitImg.add(index.path)
        }
        return submitImg
    }

    private class TaskImgDiffCallback : DiffUtil.ItemCallback<TaskImageMode>() {
        override fun areItemsTheSame(oldItem: TaskImageMode, newItem: TaskImageMode): Boolean {
            return oldItem.path == newItem.path
        }

        override fun areContentsTheSame(oldItem: TaskImageMode, newItem: TaskImageMode): Boolean {
            return oldItem == newItem
        }
    }
}