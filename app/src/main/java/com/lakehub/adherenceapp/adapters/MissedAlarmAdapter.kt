package com.lakehub.adherenceapp.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.firebase.firestore.FirebaseFirestore
import com.lakehub.adherenceapp.R
import com.lakehub.adherenceapp.activities.client.SingleMissedAlarmActivity
import com.lakehub.adherenceapp.data.Alarm
import com.lakehub.adherenceapp.utils.displayDateTime
import com.lakehub.adherenceapp.utils.displayTime
import com.lakehub.adherenceapp.utils.limitStringLength
import com.lakehub.adherenceapp.utils.showSuccess
import org.joda.time.format.DateTimeFormat


class MissedAlarmAdapter(val context: Context, private val alarms: ArrayList<Alarm>) :
    androidx.recyclerview.widget.RecyclerView.Adapter<MissedAlarmAdapter.MyViewHolder>() {

    override fun getItemCount(): Int {
        return alarms.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.missed_alarm_row, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val alarm = alarms[position]

        holder.tvDescription.text =
            limitStringLength(alarm.description, 40)
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

        holder.itemView.setOnClickListener {
            val myIntent = Intent(context, SingleMissedAlarmActivity::class.java)
            myIntent.putExtra("docId", alarm.docId)
            myIntent.putExtra("id", alarm.id)
            myIntent.putExtra("description", alarm.description)
            myIntent.putExtra("tonePath", alarm.alarmTonePath)
            myIntent.putExtra("fromDate", alarm.fromDate)
            myIntent.putExtra("toDate", alarm.toDate)
            myIntent.putExtra("place", alarm.place)
            myIntent.putExtra("medType", alarm.medicationType)
            myIntent.putExtra("repeatMode", alarm.repeatMode)
            myIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(myIntent)
        }

        holder.icMenu.setOnClickListener {
            openOptionMenu(it, alarm)
        }

    }

    inner class MyViewHolder(view: View) : ViewHolder(view) {
        var tvCount: TextView = view.findViewById(R.id.tv_count)
        var tvDescription: TextView = view.findViewById(R.id.tv_dsc)
        var timeTv: TextView = view.findViewById(R.id.tv_time)
        var icMenu: View = view.findViewById(R.id.icMenu)
    }

    private fun openOptionMenu(v: View, alarm: Alarm) {
        val popup = PopupMenu(v.context, v)
        popup.menuInflater.inflate(R.menu.missed_menu, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.clean -> {
                    val alarmsRef = FirebaseFirestore.getInstance()
                        .collection("alarms")
                        .document(alarm.docId!!)

                    alarmsRef.update("cleaned", true)
                    (context as AppCompatActivity).showSuccess(context.getString(R.string.alarm_cleaned))
                }
            }
            true
        }
        popup.show()
    }

}