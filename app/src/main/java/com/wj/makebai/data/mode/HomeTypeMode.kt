package com.wj.makebai.data.mode

import com.wj.makebai.data.emu.HomeEmu

/**
 * 首页
 * @author Administrator
 * @version 1.0
 * @date 2019/12/26
 */
class HomeTypeMode {
    constructor(mode: Any?, type: HomeEmu?) {
        this.mode = mode
        this.type = type
    }

    var mode:Any?=null
    var type: HomeEmu?=null
}