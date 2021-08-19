package com.wj.makebai.ui.activity.article

import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import com.wj.commonlib.app.BaseApplication
import com.wj.commonlib.data.mode.ArticleKeyMode
import com.wj.commonlib.statices.EventCodes
import com.wj.eventbus.WjEventBus
import com.wj.ktutils.db.delete
import com.wj.makebai.R
import com.wj.makebai.data.db.MySqlHelper
import com.wj.makebai.data.db.PdtDb
import com.wj.makebai.ui.activity.base.MakeActivity
import com.wj.makebai.ui.weight.channel.ChannelAdapter
import com.wj.makebai.ui.weight.channel.helper.ItemDragHelperCallback
import kotlinx.android.synthetic.main.channel_layout.*


/**
 * 频道 增删改查 排序
 * Created by YoKeyword on 15/12/29.
 */
class ChannelActivity : MakeActivity() {
    private var adapter: ChannelAdapter?=null
    override fun bindLayout(): Int {
        return R.layout.channel_layout
    }

    override fun initData() {
        title_content.text=getString(R.string.channel)

        val items = ArrayList<ArticleKeyMode>()
        PdtDb.select(0) {
            items.addAll(it)
        }

        val otherItems = ArrayList<ArticleKeyMode>()
        PdtDb.select(1) {
            otherItems.addAll(it)
        }


        val manager = GridLayoutManager(this, 4)
        recyclerview!!.layoutManager = manager

        val callback = ItemDragHelperCallback()
        val helper = ItemTouchHelper(callback)
        helper.attachToRecyclerView(recyclerview)

        adapter = ChannelAdapter(this, helper, items, otherItems)
        manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val viewType = adapter!!.getItemViewType(position)
                return if (viewType == ChannelAdapter.TYPE_MY || viewType == ChannelAdapter.TYPE_OTHER) 1 else 4
            }
        }
        recyclerview!!.adapter = adapter

        adapter!!.setOnMyChannelItemClickListener(object : ChannelAdapter.OnMyChannelItemClickListener{
            override fun onItemClick(v: View, position: Int) {
            }
        })
    }

    override fun onDestroy() {
        if(adapter!=null){
            //删除数据
            MySqlHelper.getInstance(BaseApplication.mApplication!!.get()!!).use {
                delete(MySqlHelper.PDCHANNEL)
            }
            //插入我的数据
            PdtDb.insert(adapter!!.mMyChannelItems as ArrayList<ArticleKeyMode>,0)
            //插入其他数据
            PdtDb.insert( adapter!!.mOtherChannelItems as ArrayList<ArticleKeyMode>,1)
            WjEventBus.getInit().post(EventCodes.XSPD,0)
        }
        super.onDestroy()

    }
}
