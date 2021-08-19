package com.wj.makebai.ui.adapter

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abase.util.Tools
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.wj.commonlib.ui.ViewControl
import com.wj.makebai.R
import com.wj.ui.base.BaseAdapter
import com.wj.ui.base.viewhoder.CustomVhoder
import kotlinx.android.synthetic.main.watchcomic_item_b_layout.view.*
import kotlinx.android.synthetic.main.watchcomic_item_layout.view.*

/**
 * 浏览漫画的适配器
 * @author dchain
 * @version 1.0
 * @date 2019/9/18
 */
class WatchComicAdapter() :BaseAdapter(){
    var list = ArrayList<String>()
    var index=-1//当前看的第几话
    var total=-1//一共几话

    constructor(list: ArrayList<String>) : this() {
        this.list = list
    }

    override fun getItemCount(): Int {
        return  if(list.size>0) list.size+1 else 0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(position!=itemCount-1){
            val mode=list[position]
            holder.itemView.loading.visibility=View.VISIBLE
            holder.itemView.loading.playAnimation()
            glide!!.load(mode.trim()).listener(object :RequestListener<Drawable>{
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    holder.itemView.loading.visibility=View.GONE
                    holder.itemView.loading.pauseAnimation()
                    return false
                }

            }).apply(RequestOptions.encodeQualityOf(100)).into(holder.itemView.image)

            holder.itemView.setOnLongClickListener { ViewControl.photoViewDialog(context!!,position,list);true}
        }else{
            when {
                index>=total -> {
                    holder.itemView.before.visibility=View.VISIBLE
                    holder.itemView.next.visibility=View.GONE
                }
                index in 1 until total -> {
                    holder.itemView.before.visibility=View.VISIBLE
                    holder.itemView.next.visibility=View.VISIBLE
                }
                else -> {
                    holder.itemView.before.visibility=View.GONE
                    holder.itemView.next.visibility=View.VISIBLE
                }
            }
            holder.itemView.before.setOnClickListener { onItemClickListener?.click(it,index-1) }
            holder.itemView.next.setOnClickListener { onItemClickListener?.click(it,index+1) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        super.onCreateViewHolder(parent, viewType)
        return if(viewType!=-1) CustomVhoder(inflater!!.inflate(R.layout.watchcomic_item_layout,parent,false))
        else  CustomVhoder(inflater!!.inflate(R.layout.watchcomic_item_b_layout,parent,false))
    }

    override fun getItemViewType(position: Int): Int {
        if(position<list.size)  super.getItemViewType(position) else  return -1
        return super.getItemViewType(position)
    }


}