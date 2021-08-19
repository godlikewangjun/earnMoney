package com.wj.makebai.ui.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abase.util.GsonUtil
import com.wj.commonlib.data.mode.NovelItem
import com.wj.commonlib.ui.adapter.base.FootAdapter
import com.wj.ktutils.startActivity
import com.wj.makebai.R
import com.wj.makebai.ui.activity.novel.NovelDetailActivity
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
 * 小说
 * @author dchain
 * @version 1.0
 * @date 2019/9/20
 */
class NovelListAdapter() : FootAdapter(){
    var list= arrayListOf<NovelItem>()
    constructor(list:ArrayList<NovelItem>) : this() {
        this.list=list
    }


    @SuppressLint("SetTextI18n")
    override fun bindVH(holder: RecyclerView.ViewHolder, position: Int) {
        val comicItem=list[position]
        holder.itemView.image.loadCover(glide!!,comicItem.cover)
        holder.itemView.title.text=comicItem.title
        holder.itemView.state.text=comicItem.descs
        holder.itemView.tv_tag.text=comicItem.fictionType
        holder.itemView.author.text=comicItem.author
    }

    override fun creatdVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CustomVhoder(inflater!!.inflate(R.layout.comic_item_layout,parent,false))
    }

    override fun getItemCount(): Int {
        count=list.size
        return super.getItemCount()
    }

}