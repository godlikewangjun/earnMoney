package com.wj.makebai.ui.adapter

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.qq.e.ads.nativ.NativeExpressADView
import com.wj.commonlib.data.mode.ArticleMode
import com.wj.commonlib.utils.CommTools
import com.wj.makebai.R
import com.wj.makebai.data.mode.ArticleDetailTypeMode
import com.wj.makebai.ui.control.CommControl
import com.wj.makebai.utils.MbTools
import com.wj.ui.base.BaseAdapter
import com.wj.ui.base.viewhoder.CustomVhoder
import kotlinx.android.synthetic.main.articlecontent_item_layout.view.*
import kotlinx.android.synthetic.main.articletitle_item_layout.view.*
import kotlinx.android.synthetic.main.gdt_ad_item_layout.view.*

/**
 * 文章详情的adapter
 * @author dchain
 * @version 1.0
 * @date 2019/9/3
 */
class ArticleDetailAdapter() : BaseAdapter() {
    var isAd=true
    var mList = ArrayList<ArticleDetailTypeMode>()
    /**
     * 是否加载完成
     */
    var isFinsh: Boolean = false
        set(value) {
            field = value
            if (itemCount > 1) {
                notifyItemChanged(itemCount - 1)
            }
        }

    constructor(list: ArrayList<ArticleDetailTypeMode>) : this() {
        this.mList = list
    }

    enum class ArticleDetailTypes {
        TITLE, AD, CONTENT, PUSH_TITLE, PUSHLIST, LOADMORE
    }


    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)


        if (position < itemCount - 1) {
            if (mList[position].type != ArticleDetailTypes.AD) {
                val mode = mList[position]
                when (mode.type) {
                    ArticleDetailTypes.TITLE -> {
                        holder.itemView.article_title.text = mode.mode as String
                    }
                    ArticleDetailTypes.CONTENT -> {
                        val data = mode.mode as ArticleMode
                        holder.itemView.like_status.text = String.format(
                            context!!.resources.getString(R.string.zan),
                            CommTools.long2Strng(data.like_count)
                        )
                        if (holder.itemView.webview.isSelected) return
                        holder.itemView.webview.mWebView.scrollBarSize = 0
                        holder.itemView.webview.isSelected = true
                        holder.itemView.webview.loadHtml(
                            MbTools.setHtml(
                                context!!,
                                data.articlecontent,
                                data.source,
                                data.creatime
                            )
                        )
                    }
                    ArticleDetailTypes.PUSHLIST -> {
                        val articleMode = mode.mode as ArticleMode
                        CommControl.bindArticleData(
                            context!!,
                            glide!!,
                            this,
                            holder,
                            articleMode,
                            position,
                            isAd
                        )
                    }
                    else -> {
                    }
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
//            adView.render() // 调用render方法后sdk才会开始展示广告
            }
        } else {
            if (isFinsh) {
                holder.itemView.findViewById<View>(R.id.foot_loading).visibility = View.GONE
                holder.itemView.findViewById<View>(R.id.foot_finsh).visibility = View.VISIBLE
            } else {
                holder.itemView.findViewById<View>(R.id.foot_loading).visibility = View.VISIBLE
                holder.itemView.findViewById<View>(R.id.foot_finsh).visibility = View.GONE
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        super.onCreateViewHolder(parent, viewType)
        return when (ArticleDetailTypes.values()[viewType]) {
            ArticleDetailTypes.TITLE -> CustomVhoder(//标题
                inflater!!.inflate(
                    R.layout.articletitle_item_layout,
                    parent,
                    false
                )
            )
            ArticleDetailTypes.CONTENT -> CustomVhoder(//网页内容
                inflater!!.inflate(
                    R.layout.articlecontent_item_layout,
                    parent,
                    false
                )
            )
            ArticleDetailTypes.PUSH_TITLE -> CustomVhoder(//推荐的标题
                inflater!!.inflate(
                    R.layout.articlepushtitle_item_layout,
                    parent,
                    false
                )
            )
            ArticleDetailTypes.PUSHLIST -> CustomVhoder(//列表
                inflater!!.inflate(
                    R.layout.article_item_layout,
                    parent,
                    false
                )
            )
            ArticleDetailTypes.LOADMORE -> {
                val footView = inflater!!.inflate(
                    R.layout.commlib_footview_layout,
                    parent,
                    false
                )
                if (isFinsh) {
                    footView.findViewById<View>(R.id.foot_loading).visibility = View.GONE
                    footView.findViewById<View>(R.id.foot_finsh).visibility = View.VISIBLE
                } else {
                    footView.findViewById<View>(R.id.foot_loading).visibility = View.VISIBLE
                    footView.findViewById<View>(R.id.foot_finsh).visibility = View.GONE
                }
                CustomVhoder(footView)
            }
            else -> {
                CustomVhoder(//广告
                    inflater!!.inflate(
                        R.layout.gdt_ad_item_layout,
                        parent,
                        false
                    )
                )
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < mList.size) mList[position].type!!.ordinal else return ArticleDetailTypes.LOADMORE.ordinal

    }

    override fun getItemCount(): Int {
        return if (mList.size > 0) mList.size + 1 else 0
    }
}