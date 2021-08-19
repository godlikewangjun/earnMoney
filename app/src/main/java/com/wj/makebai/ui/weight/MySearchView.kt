package com.wj.makebai.ui.weight

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import com.abase.util.AbAppUtil
import com.abase.util.AbViewUtil
import com.wj.makebai.R
import kotlinx.android.synthetic.main.activity_search_novel_layout.*
import kotlinx.android.synthetic.main.include_search.view.*

/**
 *
 * @author admin
 * @version 1.0
 * @date 2021/1/29
 */
class MySearchView:FrameLayout {
    private lateinit var  searchView:View
    constructor(context: Context) : super(context){
        init()
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){
        init()
    }
    var setOnClearListener={

    }
    var setOnSearchListener={

    }
    private fun init(){
        searchView=LayoutInflater.from(context).inflate(R.layout.include_search,null)
        addView(searchView)

        searchView.search_clear.setOnClickListener{
            searchView.search_text.setText("")
            setOnClearListener.invoke()
        }
        searchView.search_text.setOnEditorActionListener { v, actionId, event ->
            if (actionId === EditorInfo.IME_ACTION_SEARCH) {
                if (searchView.search_text.text?.trim()!!.isNotEmpty()){
                    setOnSearchListener.invoke()
                    AbAppUtil.closeSoftInput(context as Activity,searchView.search_text)
                }
                return@setOnEditorActionListener true
            }
            false
        }
    }
    fun setText(text:String?){
        if (text==null) return
        searchView.search_text.setText(text)
    }
    fun setHint(text:String?){
        if (text==null) return
        searchView.search_text.hint = text
    }
    fun getHint():String{
        return searchView.search_text.hint.toString()
    }
    fun getText():String{
        return searchView.search_text.text.toString()
    }
}