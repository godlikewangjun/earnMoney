package com.wj.makebai.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.qq.e.ads.nativ.NativeExpressADView
import com.wj.makebai.R
import com.wj.makebai.data.mode.BannerMode
import com.youth.banner.adapter.BannerAdapter
import kotlinx.android.synthetic.main.banner_image_item.view.*

/**
 * banner
 * @author admin
 * @version 1.0
 * @date 2020/8/24
 */
class ViewBannerAdapter : BannerAdapter<BannerMode, ViewBannerAdapter.BannerViewHolder> {


    constructor(datas: List<BannerMode>) : super(datas){
    }

    class BannerViewHolder(@NonNull view: View) : RecyclerView.ViewHolder(view) {

    }

    fun addData(data: List<BannerMode>){
        mDatas.addAll(data)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return mDatas[getRealPosition(position)].type
    }

    override fun onCreateHolder(parent: ViewGroup?, viewType: Int): BannerViewHolder {
        return  BannerViewHolder(LayoutInflater.from(parent!!.context).inflate(R.layout.banner_image_item,parent,false))
    }
    override fun onBindView(
        holder: BannerViewHolder?,
        data: BannerMode?,
        position: Int,
        size: Int
    ) {
        if(data!!.type==1) {
            val imageView=holder!!.itemView.imgView
            Glide.with(imageView).load(data.data as String).into(imageView)
        }else {
            val adView=data.data as NativeExpressADView

//            if (holder!!.itemView.express_ad_container.childCount > 0
//                && holder.itemView.express_ad_container.getChildAt(0) === adView
//            ) {
//                return
//            }

            if (holder!!.itemView.express_ad_container.childCount > 0) {
                holder.itemView.express_ad_container.removeAllViews()
            }

            if (adView.parent != null) {
                (adView.parent as ViewGroup).removeView(adView)
            }

            holder.itemView.express_ad_container.addView(adView)
            adView.render()
        }
    }
}