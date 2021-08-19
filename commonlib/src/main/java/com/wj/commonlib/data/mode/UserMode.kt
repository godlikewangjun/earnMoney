package com.wj.commonlib.data.mode

/**
 * 用户信息
 * @author Admin
 * @version 1.0
 * @date 2018/6/25
 */

data class UserMode(
    val token: String,
    var userbalance: Int,
    val usericon: String,
    val userid: Int,
    var usermoney: Double,
    var username: String,
    val usersex: Int,
    val userCode: String,
    val userPushId: String,
    val creatime: String,
    val sharePoints:Int,
    val shareCount:Int,
    val luckyCount:Int,
    var userspendingtotal: Double,
    val usertype: Int,
    val audiPoint:Int,
    val otherid:String,
    var user_phone:String,
    var isVip:Int,
    var vipToDate:String?
){
    var userPwd:String?=null
}