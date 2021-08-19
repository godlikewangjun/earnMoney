package com.wj.makebai.ui.adapter.uc

import android.text.format.DateUtils
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.recyclerview.widget.RecyclerView
import com.abase.view.weight.RecyclerSpace
import com.qq.e.ads.nativ.NativeExpressADView
import com.wj.commonlib.data.mode.uc.Thumbnail
import com.wj.commonlib.data.mode.uc.UcArticleItem
import com.wj.commonlib.http.UcRequests
import com.wj.commonlib.http.Urls
import com.wj.commonlib.ui.adapter.base.FootAdapter
import com.wj.commonlib.ui.weight.CustomGridManager
import com.wj.ktutils.isNull
import com.wj.ktutils.startActivity
import com.wj.makebai.R
import com.wj.makebai.data.emu.ArtEmu
import com.wj.makebai.data.mode.ArtTypeMode
import com.wj.makebai.data.db.TypesEnum
import com.wj.makebai.ui.activity.comm.WebActivity
import com.wj.makebai.ui.adapter.ImgAdapter
import com.wj.makebai.ui.weight.ScaleInAnimation
import com.wj.ui.base.viewhoder.CustomVhoder
import com.wj.ui.interfaces.RecyerViewItemListener
import kotlinx.android.synthetic.main.gdt_ad_item_layout.view.*
import kotlinx.android.synthetic.main.ucnew3_item_layout.view.*
import kotlinx.android.synthetic.main.ucnew_item_layout.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.URLEncoder


/**
 * 文章内容页
 */
class UcAdapter(val mList: ArrayList<ArtTypeMode>) : FootAdapter() {

    private val mSelectAnimation = ScaleInAnimation()

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        for (anim in mSelectAnimation.getAnimators(holder.itemView)) {
            anim.setDuration(100).start()
            anim.interpolator = LinearInterpolator()
        }
    }

    override fun creatdVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ArtEmu.ART.ordinal) when (viewType) {//文章
            1, 5, 0 -> CustomVhoder(
                inflater!!.inflate(
                    R.layout.ucnew_item_layout,
                    parent,
                    false
                )
            )//普通样式 多图
            3, 6 -> CustomVhoder(
                inflater!!.inflate(
                    R.layout.ucnew3_item_layout,
                    parent,
                    false
                )
            )//基本样式+大图 头条
            8 -> CustomVhoder(
                inflater!!.inflate(
                    R.layout.ucnew_item_layout,
                    parent,
                    false
                )
            )//多图
            else -> CustomVhoder(
                inflater!!.inflate(
                    R.layout.ucnew_item_layout,
                    parent,
                    false
                )
            )
        } else CustomVhoder(//广告
            inflater!!.inflate(
                R.layout.gdt_ad_item_layout,
                parent,
                false
            )
        )
    }

    override fun bindVH(holder: RecyclerView.ViewHolder, position: Int) {

        if (mList[position].type == ArtEmu.ART) {
            val mode = mList[position].mode as UcArticleItem
            when (mode.style_type) {
                5, 1, 0, 8 -> {//多图
                    holder.itemView.title.text = mode.title
                    if (mode.bottomLeftMark.mark != null) {
                        holder.itemView.news_type.text = mode.bottomLeftMark.mark
                        if (!mode.bottomLeftMark.mark_icon_url.isNull()) {
                            glide!!.load(mode.bottomLeftMark.mark_icon_url)
                                .into(holder.itemView.ad_img)
                            holder.itemView.ad_img.visibility = View.VISIBLE
                        } else {
                            holder.itemView.ad_img.visibility = View.GONE
                        }
                        holder.itemView.news_type.setBackgroundResource(R.drawable.shape_grayline)
                    } else {
                        holder.itemView.news_type.text = ""
                        holder.itemView.news_type.setBackgroundDrawable(null)
                        holder.itemView.ad_img.visibility = View.GONE
                    }
                    holder.itemView.desc.text = mode.source_name
                    holder.itemView.cmt_cnt.text = mode.cmt_cnt.toString()
                    holder.itemView.date.text =
                        DateUtils.formatDateTime(
                            context!!,
                            mode.publish_time,
                            DateUtils.FORMAT_ABBREV_TIME
                        )
                    if (mode.isRead) {
                        holder.itemView.title.setTextColor(context!!.resources.getColor(R.color.text_gray))
                    } else {
                        holder.itemView.title.setTextColor(context!!.resources.getColor(R.color.black))
                    }


                    if (mode.thumbnails != null) {
                        if (mode.thumbnails.size == 1) {
                            holder.itemView.imgView.visibility = View.VISIBLE
                            holder.itemView.recyclerView.visibility = View.GONE

                            glide!!.load(mode.thumbnails[0].url).into(holder.itemView.imgView)
                        } else if (mode.thumbnails.size > 1) {
                            holder.itemView.imgView.visibility = View.GONE
                            holder.itemView.recyclerView.visibility = View.VISIBLE

                            val imgAdapter = ImgAdapter(mode.thumbnails.toStringArray()).apply {
                                onItemClickListener = object : RecyerViewItemListener {
                                    override fun click(view: View, position: Int) {
                                        holder.itemView.performClick()
                                    }
                                }
                            }
                            imgAdapter.type = 3
                            holder.itemView.recyclerView.adapter = imgAdapter
                            holder.itemView.recyclerView.layoutManager =
                                CustomGridManager(context, 3).setScrollEnabled(false)
                            if (holder.itemView.recyclerView.itemDecorationCount < 1) holder.itemView.recyclerView.addItemDecoration(
                                RecyclerSpace(5)
                            )
                        }
                    } else {
                        holder.itemView.imgView.visibility = View.GONE
                        holder.itemView.recyclerView.visibility = View.GONE
                    }
                }
                3 -> {//基本样式+大图 头条
                    holder.itemView.title3.text = mode.title
                    if (mode.thumbnails != null) {
                        glide!!.load(mode.thumbnails[0].url).into(holder.itemView.thumbImage)
                        holder.itemView.visibility = View.VISIBLE
                    } else {
                        holder.itemView.visibility = View.GONE
                    }
                    if (mode.videos != null && mode.videos.isNotEmpty()) {
                        holder.itemView.has_video.visibility = View.VISIBLE
                    } else {
                        holder.itemView.has_video.visibility = View.GONE
                    }
                    if (mode.isRead) {
                        holder.itemView.title3.setTextColor(context!!.resources.getColor(R.color.text_gray))
                    } else {
                        holder.itemView.title3.setTextColor(context!!.resources.getColor(R.color.black))
                    }
                    holder.itemView.ad_desc.text = mode.source_name

                    if (mode.bottomLeftMark.mark != null) {
                        holder.itemView.type_desc.text = mode.bottomLeftMark.mark
                        holder.itemView.type_desc.setBackgroundResource(R.drawable.shape_grayline)
                        if (!mode.bottomLeftMark.mark_icon_url.isNull()) {
                            glide!!.load(mode.bottomLeftMark.mark_icon_url)
                                .into(holder.itemView.ad_img1)
                            holder.itemView.ad_img1.visibility = View.VISIBLE
                        } else {
                            holder.itemView.ad_img1.visibility = View.GONE
                        }
                    } else holder.itemView.ad_img1.visibility = View.GONE
                }
                6 -> {//纯图模式
                    holder.itemView.title3.visibility = View.GONE
                    holder.itemView.has_video.visibility = View.GONE
                    holder.itemView.type_desc.visibility = View.GONE
                    if (mode.thumbnails != null) {
                        glide!!.load(mode.thumbnails[0].url).into(holder.itemView.thumbImage)
                    }
                }
            }
            //展示的广告
            if (!mode.show_impression_url.isNull()) {
                UcRequests.ucAdShow(mode.show_impression_url, fun(_, _, _) {
                })
            }

            holder.itemView.setOnClickListener {
                when (mode.style_type) {
                    5, 1, 0 -> holder.itemView.title.setTextColor(context!!.resources.getColor(R.color.text_gray))
                    3 -> holder.itemView.title3.setTextColor(context!!.resources.getColor(R.color.text_gray))
                }
                context!!.startActivity<WebActivity>(
                    "url" to mode.url,
                    "uc" to Urls.uc_share + URLEncoder.encode(mode.url, "utf-8"), "ad" to true
                )
            }
        } else {
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
            GlobalScope.launch(Dispatchers.IO) {  adView.render() }
            // 调用render方法后sdk才会开始展示广告

        }

    }


    private fun List<Thumbnail>.toStringArray(): ArrayList<String> {
        val strs = ArrayList<String>()
        for (index in this) {
            strs.add(index.url)
        }
        return strs
    }


    override fun getItemCount(): Int {
        count = mList.size
        return super.getItemCount()
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < count) mList[position].type!!.ordinal else return LOADMORE
    }
}
