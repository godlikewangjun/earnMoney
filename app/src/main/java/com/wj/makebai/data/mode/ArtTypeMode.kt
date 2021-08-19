package com.wj.makebai.data.mode

import com.wj.makebai.data.emu.ArtEmu

/**
 * 文章信息流的类型
 * @author Administrator
 * @version 1.0
 * @date 2019/12/19
 */
class ArtTypeMode {
    constructor(mode: Any?, type: ArtEmu?) {
        this.mode = mode
        this.type = type
    }

    var mode:Any?=null
    var type:ArtEmu?=null
}