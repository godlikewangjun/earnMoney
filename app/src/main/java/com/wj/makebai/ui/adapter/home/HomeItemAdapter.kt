package com.wj.makebai.ui.adapter.home

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.wj.commonlib.data.mode.HomeToolsMode
import com.wj.makebai.R
import com.wj.makebai.ui.control.CommControl
import com.wj.ui.base.BaseAdapter
import com.wj.ui.base.viewhoder.CustomVhoder
import kotlinx.android.synthetic.main.home_item_layout.view.*


/**
 *推荐分类
 */
class HomeItemAdapter(private val mList: List<HomeToolsMode>) : BaseAdapter() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        super.onCreateViewHolder(parent, viewType)
        return CustomVhoder(
            inflater!!.inflate(
                R.layout.home_item_layout,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val mode = mList[position]
        holder.itemView.desc.text = mode.describe
        if (!mode.img_url.endsWith(".json")) glide!!.load(mode.img_url).apply(RequestOptions.circleCropTransform()).into(
            holder.itemView.image
        )
        else
            holder.itemView.image.setAnimationFromUrl(mode.img_url)
        holder.itemView.setOnClickListener {
            CommControl.homeTools(context!!, mode.tools_link, mode.describe)
        }
    }


    override fun getItemCount(): Int {
        return mList.size
    }

}
