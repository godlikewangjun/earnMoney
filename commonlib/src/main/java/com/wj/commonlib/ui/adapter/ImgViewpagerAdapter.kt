package com.wj.commonlib.ui.adapter

import androidx.viewpager.widget.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.wj.commonlib.R
import com.wj.ui.interfaces.RecyerViewItemListener

/**
 * 图片浏览
 * @author Administrator
 * @version 1.0
 * @date 2018/10/11/011
 */
class ImgViewpagerAdapter(): PagerAdapter() {
    var imageViews=ArrayList<String>()
    var onClickListener:RecyerViewItemListener?=null

    constructor(imageViews: ArrayList<String>) : this() {
        this.imageViews = imageViews
    }

    override fun getCount(): Int {
        return imageViews.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val photoView=LayoutInflater.from(container.context).inflate(R.layout.commlib_photoview,container,false)
        container.addView(photoView)
        Glide.with(container.context).load(imageViews[position]).into(photoView as ImageView)
        photoView.setOnClickListener { if(onClickListener!=null)onClickListener!!.click(photoView,position)  }
        return photoView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}