package com.wj.makebai.ui.adapter

import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.wj.makebai.R
import com.wj.ui.base.BaseAdapter
import com.wj.ui.base.viewhoder.CustomVhoder
import com.yanbo.lib_screen.entity.ClingDevice
import com.yanbo.lib_screen.manager.DeviceManager
import kotlinx.android.synthetic.main.tv_list_item.view.*


/**
 * 搜索到的可以投屏的TV
 * @author admin
 * @version 1.0
 * @date 2020/5/28
 */
class TvAdapter() : BaseAdapter() {
    private var clingDevices: List<ClingDevice>? = null
    private var choose = -1

    init {
        clingDevices = DeviceManager.getInstance().clingDeviceList
    }


    override fun getItemCount(): Int {
        return clingDevices!!.size
    }

    fun refresh() {
        clingDevices = DeviceManager.getInstance().clingDeviceList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val mode = clingDevices!![position]
        holder.itemView.tv_check.text = mode.device.details.friendlyName
        holder.itemView.tv_check.isChecked = choose == position
        holder.itemView.tv_check.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                choose = position
                notifyDataSetChanged()
                DeviceManager.getInstance().currClingDevice = mode
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        super.onCreateViewHolder(parent, viewType)
        return CustomVhoder(inflater!!.inflate(R.layout.tv_list_item, parent, false))
    }
}