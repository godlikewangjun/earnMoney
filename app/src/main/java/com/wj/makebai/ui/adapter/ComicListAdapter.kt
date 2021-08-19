package com.wj.makebai.ui.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abase.util.GsonUtil
import com.wj.commonlib.data.mode.ComicItem
import com.wj.commonlib.ui.adapter.base.FootAdapter
import com.wj.ktutils.isNull
import com.wj.ktutils.startActivity
import com.wj.makebai.R
import com.wj.makebai.ui.activity.comic.ComicDetailActivity
import com.wj.makebai.utils.MbTools.load
import com.wj.makebai.utils.MbTools.loadCover
import com.wj.ui.base.viewhoder.CustomVhoder
import kotlinx.android.synthetic.main.comic_item_layout.view.*
import kotlinx.android.synthetic.main.comic_item_layout.view.author
import kotlinx.android.synthetic.main.comic_item_layout.view.image
import kotlinx.android.synthetic.main.comic_item_layout.view.title
import kotlinx.android.synthetic.main.comic_item_layout.view.tv_tag
import kotlinx.android.synthetic.main.vipmovie_item_layout.view.*

/**
 * 漫画的适配器
 * @author dchain
 * @version 1.0
 * @date 2019/9/10
 */
class ComicListAdapter() : FootAdapter(){
    var list= arrayListOf<ComicItem>()
    constructor(list:ArrayList<ComicItem>) : this() {
        this.list=list
    }


    @SuppressLint("SetTextI18n")
    override fun bindVH(holder: RecyclerView.ViewHolder, position: Int) {
        val comicItem=list[position]
        holder.itemView.image.loadCover(glide!!,comicItem.cover)
        holder.itemView.title.text=comicItem.title
        if(!comicItem.descs.isNull())holder.itemView.state.text=comicItem.descs else holder.itemView.state.text=context!!.resources.getString(R.string.not_yet)
        if(!comicItem.cartoonType.isNull())holder.itemView.tv_tag.text=comicItem.cartoonType else holder.itemView.tv_tag.text=context!!.resources.getString(R.string.not_yet)
        if(!comicItem.author.isNull())holder.itemView.author.text=comicItem.author else holder.itemView.author.text=context!!.resources.getString(R.string.not_yet)
    }

    override fun creatdVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CustomVhoder(inflater!!.inflate(R.layout.comic_item_layout,parent,false))
    }

    override fun getItemCount(): Int {
        count=list.size
        return super.getItemCount()
    }

}