package com.wj.makebai.data.mode

import com.wj.makebai.ui.adapter.ArticleDetailAdapter

/**
 * 文章推荐的mode适配
 * @author dchain
 * @version 1.0
 * @date 2019/9/3
 */
class ArticleDetailTypeMode {
    constructor(type: ArticleDetailAdapter.ArticleDetailTypes?, mode: Any?) {
        this.type = type
        this.mode = mode
    }

    //数据类型
    var type: ArticleDetailAdapter.ArticleDetailTypes?=null
    //mode
    var mode:Any?=null
}