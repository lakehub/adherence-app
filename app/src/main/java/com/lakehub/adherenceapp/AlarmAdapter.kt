package com.lakehub.adherenceapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView.ViewHolder


class AlarmAdapter(val context: Context, private val alarms: ArrayList<Alarm>) :
    androidx.recyclerview.widget.RecyclerView.Adapter<AlarmAdapter.MyViewHolder>() {

    override fun getItemCount(): Int {
        return alarms.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.upcomig_list_row, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val alarm = alarms[position]

        holder.tvDescription.text = limitStringLength(alarm.description, 50)
        holder.timeTv.text = displayTime(alarm.fromDate)

        if (holder.adapterPosition == 0) {
            holder.tvCount.visibility = View.VISIBLE
            holder.activeView.visibility = View.VISIBLE
        } else {
            holder.tvCount.visibility = View.GONE
            holder.activeView.visibility = View.GONE
        }

        holder.container.setOnClickListener {

        }

    }

    inner class MyViewHolder(view: View) : ViewHolder(view) {
        var tvCount: TextView = view.findViewById(R.id.tv_count)
        var tvDescription: TextView = view.findViewById(R.id.tv_dsc)
        var timeTv: TextView = view.findViewById(R.id.tv_time)
        var container: ConstraintLayout = view.findViewById(R.id.container)
        var activeView: View = view.findViewById(R.id.active_view)
    }
}