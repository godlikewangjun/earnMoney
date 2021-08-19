package com.wj.makebai.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.qq.e.ads.nativ.NativeExpressADView
import com.wj.commonlib.data.mode.ArticleMode
import com.wj.commonlib.ui.adapter.base.FootAdapter
import com.wj.makebai.R
import com.wj.makebai.data.emu.ArtEmu
import com.wj.makebai.data.mode.ArtTypeMode
import com.wj.makebai.ui.control.CommControl
import com.wj.ui.base.viewhoder.CustomVhoder
import kotlinx.android.synthetic.main.gdt_ad_item_layout.view.*

/**
 * 文章内容页
 */
class ContentAdapter(val mList: ArrayList<ArtTypeMode>) : FootAdapter() {
    var isAd=true

    override fun creatdVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ArtEmu.ART.ordinal)  CustomVhoder(inflater!!.inflate(R.layout.article_item_layout, parent, false)) else CustomVhoder(//广告
            inflater!!.inflate(
                R.layout.gdt_ad_item_layout,
                parent,
                false
            )
        )
    }

    override fun bindVH(holder: RecyclerView.ViewHolder, position: Int) {
        if (mList[position].type == ArtEmu.ART) {
            val mode = mList[position].mode as ArticleMode
            CommControl.bindArticleData(context!!,glide!!,this,holder,mode,position,isAd)
        }else{
            val adView = mList[position].mode as NativeExpressADView

            if (holder.itemView.express_ad_container.childCount > 0
                && holder.itemView.express_ad_container.getChildAt(0) === adView
            ) {
                return
            }

            if (holder.itemView.express_ad_container.childCount > 0) {
                holder.itemView.express_ad_container.removeAllViews()
            }

            if (adView.parent != null) {
                (adView.parent as ViewGroup).removeView(adView)
            }

            holder.itemView.express_ad_container.addView(adView)
            adView.render()// 调用render方法后sdk才会开始展示广告
        }

    }


    override fun getItemCount(): Int {
        count = mList.size
        return super.getItemCount()
    }
    override fun getItemViewType(position: Int): Int {
        return if (position < count) mList[position].type!!.ordinal else return LOADMORE
    }
}
