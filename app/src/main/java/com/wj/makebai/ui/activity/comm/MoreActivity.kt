package com.wj.makebai.ui.activity.comm

import android.util.SparseIntArray
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import com.abase.view.weight.RecyclerSpace
import com.wj.commonlib.data.mode.HomeToolsMode
import com.wj.commonlib.http.HttpManager
import com.wj.makebai.R
import com.wj.makebai.ui.activity.base.MakeActivity
import com.wj.makebai.ui.adapter.home.HomeToolsAdapter
import com.wj.makebai.ui.weight.sticky.StickyItemDecoration
import kotlinx.android.synthetic.main.activity_more.*


/**
 * 更多功能
 * @author Administrator
 * @version 1.0
 * @date 2019/12/6
 */
class MoreActivity : MakeActivity() {
    override fun bindLayout(): Int {
        return R.layout.activity_more
    }

    override fun initData() {
        title_content.text = getString(
            R.string.feature_list
        )
        //获取所有的工具类
        HttpManager.homeToolAllList {
            it.sortBy { it.type }//先进行排序

            val indexHeader = SparseIntArray()
            for (index in 0 until it.size) {
                if (indexHeader.indexOfKey(it[index].type) < 0) {
                    if(index>0)indexHeader.put(it[index].type, index+1)
                    else indexHeader.put(it[index].type, index)
                }
            }
            var mode = HomeToolsMode("", "推荐工具", "", 0, 0, 0, "", -1)
            if (indexHeader.get(0) > -1)
                it.add(indexHeader.get(0), mode)

            mode = HomeToolsMode("", "其他", "", 0, 0, 0, "", -1)
            if (indexHeader.get(1) > -1)
                it.add(indexHeader.get(1), mode)


            recyclerView.addItemDecoration(StickyItemDecoration())
            val gridLayoutManager = GridLayoutManager(activity, 4)
            recyclerView.layoutManager = gridLayoutManager
            recyclerView.addItemDecoration(RecyclerSpace(20))

            recyclerView.adapter = HomeToolsAdapter(it)

            gridLayoutManager.spanSizeLookup = object : SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (indexHeader.indexOfValue(position) < 0) {
                        1
                    } else 4
                }
            }
        }
    }
}