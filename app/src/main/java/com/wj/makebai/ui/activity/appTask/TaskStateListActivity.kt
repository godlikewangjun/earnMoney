package com.wj.makebai.ui.activity.appTask

import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.wj.makebai.R
import com.wj.makebai.ui.activity.base.MakeActivity
import com.wj.makebai.ui.adapter.home.HomeTabAdapter
import com.wj.makebai.ui.fragment.TaskStateListFragment
import kotlinx.android.synthetic.main.activity_taskstatelist.*

/**
 * 任务状态列表
 * @author admin
 * @version 1.0
 * @date 2020/9/27
 */
class TaskStateListActivity : MakeActivity() {
    override fun bindLayout(): Int {
        return R.layout.activity_taskstatelist
    }

    override fun initData() {
        title_content.text = resources.getString(R.string.my_task)
        //设置list
        val fragments = arrayListOf<Fragment>(
            TaskStateListFragment(0),
            TaskStateListFragment(1),
            TaskStateListFragment(3),
            TaskStateListFragment(6),
            TaskStateListFragment(4),
        )
        viewpager.adapter = HomeTabAdapter(this, fragments)
        viewpager.offscreenPageLimit = fragments.size

        setTab(tab)
        val type = intent.getIntExtra("type", -1)
        if (type != -1) viewpager.currentItem = type
    }

    private fun setTab(tabLayout: TabLayout) {
        val titles = arrayOf("待提交", "审核中", "已完成", "不通过", "超时")
        TabLayoutMediator(
            tabLayout, viewpager
        ) { tab, position ->
            tab.text = titles[position]
        }.attach()
    }
}