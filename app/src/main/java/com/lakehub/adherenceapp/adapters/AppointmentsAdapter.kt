package com.lakehub.adherenceapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.lakehub.adherenceapp.*
import com.lakehub.adherenceapp.data.ChvReminder
import org.joda.time.format.DateTimeFormat


class AppointmentsAdapter(val context: Context, private val alarms: ArrayList<ChvReminder>) :
    androidx.recyclerview.widget.RecyclerView.Adapter<AppointmentsAdapter.MyViewHolder>() {

    override fun getItemCount(): Int {
        return alarms.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.appointment_row, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val alarm = alarms[position]

        holder.tvDescription.text = limitStringLength(alarm.description, 50)
        holder.tvCount.text = (position + 1).toString()

        if (alarm.recent) {
            holder.timeTv.text = displayDateTime(alarm.fromDate)
        } else {
            holder.timeTv.text = displayTime(alarm.fromDate)
        }

        if (alarm.snoozed > 0) {
            val format = "yyyy MM dd HH:mm"
            val myFormatter = DateTimeFormat.forPattern(format)
            val myDate = myFormatter.parseDateTime(alarm.fromDate)
            val newDate = myDate.plusMinutes(alarm.snoozed)
            holder.timeTv.text = displayTime(newDate)
        }

        if (holder.adapterPosition == 0) {
            holder.activeView.visibility = View.VISIBLE
        } else {
            holder.activeView.visibility = View.GONE
        }

    }

    inner class MyViewHolder(view: View) : ViewHolder(view) {
        var tvCount: TextView = view.findViewById(R.id.tv_count)
        var tvDescription: TextView = view.findViewById(R.id.tv_dsc)
        var timeTv: TextView = view.findViewById(R.id.tv_time)
        var activeView: View = view.findViewById(R.id.active_view)
    }

}