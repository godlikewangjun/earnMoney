package com.wj.makebai.ui.adapter.home

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.wj.commonlib.data.mode.HomeToolsMode
import com.wj.makebai.R
import com.wj.makebai.ui.control.CommControl
import com.wj.ui.base.BaseAdapter
import com.wj.ui.base.viewhoder.CustomVhoder
import kotlinx.android.synthetic.main.home_header_item_layout.view.*
import kotlinx.android.synthetic.main.home_item_layout.view.*

/**
 * 首页的工具适配器
 * @author Administrator
 * @version 1.0
 * @date 2019/12/7
 */
class HomeToolsAdapter(private val mList: List<HomeToolsMode>) : BaseAdapter() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        super.onCreateViewHolder(parent, viewType)
        return if (viewType != -1) {
            val view = inflater!!.inflate(
                R.layout.home_item_layout,
                parent,
                false
            )
            view.tag = false
            CustomVhoder(view)
        } else {
            val view = inflater!!.inflate(
                R.layout.home_header_item_layout,
                parent,
                false
            )
            view.tag = true
            CustomVhoder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val mode = mList[position]
        if (mode.type != -1) {
            holder.itemView.desc.text = mode.describe
            if (!mode.img_url.endsWith(".json")) glide!!.load(mode.img_url).apply(RequestOptions.circleCropTransform()).into(
                holder.itemView.image
            )
            else
                holder.itemView.image.setAnimationFromUrl(mode.img_url)
            holder.itemView.setOnClickListener {
                CommControl.homeTools(context!!, mode.tools_link, mode.describe)
            }
        } else {
            holder.itemView.title.text = mode.describe
        }
    }


    override fun getItemCount(): Int {
        return mList.size
    }

    override fun getItemViewType(position: Int): Int {
        return mList[position].type
    }
}