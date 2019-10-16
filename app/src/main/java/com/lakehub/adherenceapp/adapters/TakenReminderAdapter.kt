package com.lakehub.adherenceapp.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.lakehub.adherenceapp.R
import com.lakehub.adherenceapp.activities.chv.SingleMissedReminderActivity
import com.lakehub.adherenceapp.activities.chv.SingleReminderActivity
import com.lakehub.adherenceapp.data.ChvReminder
import com.lakehub.adherenceapp.utils.*
import org.joda.time.format.DateTimeFormat


class TakenReminderAdapter(val context: Context, private val alarms: ArrayList<ChvReminder>) :
    androidx.recyclerview.widget.RecyclerView.Adapter<TakenReminderAdapter.MyViewHolder>() {

    override fun getItemCount(): Int {
        return alarms.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.taken_reminder_row, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val alarm = alarms[position]

        holder.tvDescription.text =
            limitStringLength(alarm.description, 40)
        holder.tvCount.text = (position + 1).toString()

        if (alarm.appointment!!) {
            holder.clientTv.makeVisible()
            holder.clientTv.text = limitStringLength(
                alarm.clientAccessKey?.split(" ")?.get(0)!!, 6
            )
            holder.tvDescription.text =
                limitStringLength(alarm.description, 30)
        } else {
            holder.clientTv.makeGone()
        }

        if (alarm.recent) {
            holder.timeTv.text = displayDateTime(alarm.dateTime)
        } else {
            holder.timeTv.text = displayTime(alarm.dateTime)
        }

        if (alarm.snoozed > 0) {
            val format = "yyyy MM dd HH:mm"
            val myFormatter = DateTimeFormat.forPattern(format)
            val myDate = myFormatter.parseDateTime(alarm.dateTime)
            val newDate = myDate.plusMinutes(alarm.snoozed)
            holder.timeTv.text = displayTime(newDate)
        }

        holder.itemView.setOnClickListener {
            val myIntent = Intent(context, SingleReminderActivity::class.java)
            myIntent.putExtra("docId", alarm.docId)
            myIntent.putExtra("id", alarm.id)
            myIntent.putExtra("description", alarm.description)
            myIntent.putExtra("tonePath", alarm.alarmTonePath)
            myIntent.putExtra("date", alarm.dateTime)
            myIntent.putExtra("drug", alarm.drug)
            myIntent.putExtra("appointment", alarm.appointment)
            myIntent.putExtra("medType", alarm.medicationType)
            myIntent.putExtra("repeatMode", alarm.repeatMode)
            myIntent.putExtra("clientAccessKey", alarm.clientAccessKey)
            myIntent.putExtra("hospital", alarm.hospital)
            myIntent.putExtra("taken", true)
            myIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(myIntent)
        }

    }

    inner class MyViewHolder(view: View) : ViewHolder(view) {
        var tvCount: TextView = view.findViewById(R.id.tv_count)
        var tvDescription: TextView = view.findViewById(R.id.tv_dsc)
        var timeTv: TextView = view.findViewById(R.id.tv_time)
        var clientTv: TextView = view.findViewById(R.id.tv_client)
    }

}