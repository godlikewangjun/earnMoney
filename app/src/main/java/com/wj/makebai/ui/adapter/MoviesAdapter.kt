package com.wj.makebai.ui.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wj.commonlib.ui.adapter.base.FootAdapter
import com.wj.makebai.R
import com.wj.makebai.data.mode.MoviesMode
import com.wj.makebai.ui.control.CommControl
import com.wj.ui.base.viewhoder.CustomVhoder

/**
 * 影视的适配器
 * @author dchain
 * @version 1.0
 * @date 2019/9/10
 */
class MoviesAdapter() : FootAdapter(){
    var list= arrayListOf<MoviesMode>()
    constructor(list:ArrayList<MoviesMode>) : this() {
        this.list=list
    }


    @SuppressLint("SetTextI18n")
    override fun bindVH(holder: RecyclerView.ViewHolder, position: Int) {
        val moviesMode=list[position]
        CommControl.bindMoview(context!!,glide!!,holder,moviesMode)
    }

    override fun creatdVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CustomVhoder(inflater!!.inflate(R.layout.movie_item_layout,parent,false))
    }

    override fun getItemCount(): Int {
        count=list.size
        return super.getItemCount()
    }

}