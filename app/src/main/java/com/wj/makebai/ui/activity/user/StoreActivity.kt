package com.wj.makebai.ui.activity.user

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.wj.makebai.R
import com.wj.makebai.data.db.LikeDb
import com.wj.makebai.data.db.TypesEnum
import com.wj.makebai.ui.activity.base.MakeActivity
import com.wj.makebai.ui.adapter.TabAdapter
import com.wj.makebai.ui.fragment.ContentFragment
import kotlinx.android.synthetic.main.activity_store.*

/**
 * 收藏
 * @author dchain
 * @version 1.0
 * @date 2019/10/12
 */
class StoreActivity : MakeActivity(), View.OnClickListener {
    private lateinit var fragments: ArrayList<Fragment>
    override fun bindLayout(): Int {
        return R.layout.activity_store
    }

    override fun initData() {
        title_content.text = getString(R.string.store)

        tabLayout.tabMode = TabLayout.MODE_FIXED

        val fragment = ContentFragment()
        var bundle = Bundle()
        bundle.putBoolean("refresh", true)
        fragment.arguments = bundle


        fragments = arrayListOf(fragment)
        val titles = arrayListOf("资讯", "短视频")
        val adapter = TabAdapter(
            supportFragmentManager,
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
            fragments.toTypedArray(),
            titles
        )
        viewpager.offscreenPageLimit = fragments.size
        viewpager.adapter = adapter
        tabLayout.setupWithViewPager(viewpager)

        other.text = getString(R.string.remove_all)
        other_down.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.other_down -> {//清空
                AlertDialog.Builder(activity).setTitle("提示").setMessage("确定清空吗？")
                    .setPositiveButton("确定"
                    ) { _, _ ->
                        if (viewpager.currentItem == 0) {
                            LikeDb.clear(activity, TypesEnum.ARTICLE)
                        } else {
                            LikeDb.clear(activity, TypesEnum.VIDEO)
                        }
                    }
                    .setNegativeButton("取消"
                    ) { _, _ -> }.show()

            }
        }
    }

}