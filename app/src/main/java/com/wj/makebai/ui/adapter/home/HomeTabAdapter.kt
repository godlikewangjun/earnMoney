package com.wj.makebai.ui.adapter.home

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * 首页的viewpager
 * @author admin
 * @version 1.0
 * @date 2020/8/24
 */
class HomeTabAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    var fragments = ArrayList<Fragment>()

    constructor(fragmentActivity: FragmentActivity, fragments: ArrayList<Fragment>) : this(
        fragmentActivity
    ) {
        this.fragments = fragments;
    }

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}