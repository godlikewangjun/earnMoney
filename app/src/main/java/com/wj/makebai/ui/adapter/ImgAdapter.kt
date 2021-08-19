package com.wj.makebai.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abase.util.AbViewUtil
import com.abase.util.Tools
import com.wj.commonlib.utils.setOnClick
import com.wj.ktutils.showTip
import com.wj.makebai.R
import com.wj.ui.base.BaseAdapter
import com.wj.ui.base.viewhoder.CustomVhoder
import com.wj.ui.interfaces.RecyerViewItemListener
import kotlinx.android.synthetic.main.imageview_layout.view.*

/**
 * 图片
 * @author wangjun
 * @version 1.0
 * @date 2018/7/10
 */
class ImgAdapter() : BaseAdapter() {
    var imgs = ArrayList<String>()
    var type = 0
    var max = -1
    var onClickListener:RecyerViewItemListener?=null

    constructor(images: ArrayList<String>) : this() {
        this.imgs = images
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        super.onCreateViewHolder(parent, viewType)
        return CustomVhoder(inflater!!.inflate(R.layout.imageview_layout, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        holder.itemView.setOnClick{
            onClickListener?.click(it,position)
        }
        when (type) {
            0 -> {
                if (position != 0) glide!!.asBitmap().load(imgs[position - 1])
                    .into(holder.itemView.imgView)
                else holder.itemView.imgView.setImageResource(R.drawable.addimg)

                val layoutParams = holder.itemView.layoutParams
                layoutParams.height =
                    (Tools.getScreenWH(context)[0] / 3 - AbViewUtil.dip2px(context, 10f)).toInt()
                holder.itemView.layoutParams = layoutParams

                holder.itemView.setOnClickListener {
                    if (position == 0) {
                        if (max != -1 && max > imgs.size) {
                            onItemClickListener!!.click(it, position)
                        } else {
                            context!!.showTip("最多选${max}张")
                        }
                    } else {
                        if (position - 1 >= 0) {
                            imgs.removeAt(position - 1)
                            notifyItemRemoved(position - 1)
                        }
                    }
                }

            }
            1 -> {
                val layoutParams = holder.itemView.layoutParams
                layoutParams.height = AbViewUtil.dp2px(context!!, 170f)
                holder.itemView.layoutParams = layoutParams
                glide!!.asBitmap() .load(imgs[position]).into(holder.itemView.imgView)
            }
            2 -> {
                val layoutParams = holder.itemView.layoutParams
                layoutParams.height = AbViewUtil.dp2px(context!!, 220f)
                holder.itemView.layoutParams = layoutParams
                glide!!.asBitmap() .load(imgs[position]).into(holder.itemView.imgView)
            }
            3 -> {
                val layoutParams = holder.itemView.layoutParams
                layoutParams.height = AbViewUtil.dp2px(context!!, 120f)
                holder.itemView.layoutParams = layoutParams
                glide!!.asBitmap() .load(imgs[position]).into(holder.itemView.imgView)
                holder.itemView.setOnClickListener{
                    onItemClickListener?.click(holder.itemView, position)
                }
            }
        }

    }

    override fun getItemCount(): Int {
        return if (type == 0) imgs.size + 1 else {
            if(imgs.size<3) imgs.size else 3
        }
    }
}