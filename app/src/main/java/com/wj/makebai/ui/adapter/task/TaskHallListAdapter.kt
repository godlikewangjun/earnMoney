package com.wj.makebai.ui.adapter.task

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abase.util.GsonUtil
import com.abase.view.weight.MyDialog
import com.bumptech.glide.request.RequestOptions
import com.wj.commonlib.data.mode.AppTaskMode
import com.wj.commonlib.statices.Info
import com.wj.commonlib.ui.adapter.base.FootAdapter
import com.wj.makebai.R
import com.wj.makebai.ui.activity.appTask.TaskDetailActivity
import com.wj.ui.base.viewhoder.CustomVhoder
import kotlinx.android.synthetic.main.taskhall_item_layout.view.*
import kotlin.math.roundToInt

/**
 * 任务列表
 * @author Administrator
 * @version 1.0
 * @date 2020/8/9
 */
class TaskHallListAdapter() : FootAdapter() {
    var list = ArrayList<AppTaskMode>()
    private var dialog: MyDialog? = null

    constructor(list: ArrayList<AppTaskMode>) : this() {
        this.list = list
    }

    @SuppressLint("SetTextI18n")
    override fun bindVH(holder: RecyclerView.ViewHolder, position: Int) {
        val mode = list[position]
        glide!!.load(mode.task_icon).apply(RequestOptions().circleCrop())
            .into(holder.itemView.image)
        holder.itemView.task_desc.text = mode.task_name
        holder.itemView.task_price.text =
            "+ ${(mode.task_point * Info.money2Points).toInt()}金币"
        holder.itemView.task_count.text = "剩余${mode.task_count - mode.task_dcount}"
        holder.itemView.success_count.text = "${mode.countSuccess}人已赚"
        holder.itemView.state.visibility = View.VISIBLE
        if(mode.state!=null)
            when(mode.state.toDouble().roundToInt()){
                0->holder.itemView.state.text = "进行中"
                4->holder.itemView.state.text = "已超时"
                5->holder.itemView.state.text = "已取消"
                else-> holder.itemView.state.visibility = View.GONE
            }

        else holder.itemView.state.visibility = View.GONE

        holder.itemView.setOnClickListener {
            val intent= Intent(context!!, TaskDetailActivity::class.java)
            intent.putExtra("data", GsonUtil.gson2String(
                list[position]
            ))
            context!!.startActivity(
                intent,
                ActivityOptions.makeSceneTransitionAnimation(
                    context!! as Activity,
                    holder.itemView.image,
                    "taskImg"
                ).toBundle()
            )
            onItemClickListener?.click(it, position)
        }
    }

    override fun creatdVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
      return  CustomVhoder(
          inflater!!.inflate(
              R.layout.taskhall_item_layout,
              parent,
              false
          )
      )
    }

    override fun getItemCount(): Int {
        count=list.size
        return super.getItemCount()
    }
}