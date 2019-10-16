package com.lakehub.adherenceapp.adapters

import android.content.Context
import android.content.Intent
import android.view.*
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.lakehub.adherenceapp.*
import com.lakehub.adherenceapp.activities.client.CancelAlarmActivity
import com.lakehub.adherenceapp.activities.client.EditAlarmActivity
import com.lakehub.adherenceapp.activities.client.SingleAlarmActivity
import com.lakehub.adherenceapp.data.Alarm
import com.lakehub.adherenceapp.utils.displayDateTime
import com.lakehub.adherenceapp.utils.displayTime
import com.lakehub.adherenceapp.utils.limitStringLength
import org.joda.time.format.DateTimeFormat


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

        holder.tvDescription.text =
            limitStringLength(alarm.description, 30)
        holder.tvCount.text = (position + 1).toString()

        if (alarm.recent) {
            holder.timeTv.text = displayDateTime(alarm.fromDate)
        } else {
            holder.timeTv.text = displayTime(alarm.fromDate)
        }

        if (holder.adapterPosition == 0) {
            holder.activeView.visibility = View.VISIBLE
        } else {
            holder.activeView.visibility = View.GONE
        }

        holder.menu.setOnClickListener {
            openOptionMenu(holder.menu, holder.adapterPosition)
        }

        if (alarm.snoozed > 0) {
            val format = "yyyy MM dd HH:mm"
            val myFormatter = DateTimeFormat.forPattern(format)
            val myDate = myFormatter.parseDateTime(alarm.fromDate)
            val newDate = myDate.plusMinutes(alarm.snoozed)
            holder.timeTv.text = displayTime(newDate)
        }

        holder.itemView.setOnClickListener {
            val myIntent = Intent(context, SingleAlarmActivity::class.java)
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

        /*holder.tvCount.setOnLongClickListener {
            Log.d("TAG", "long pressed")
            currentPosition = holder.adapterPosition
            openOptionMenu(holder.timeTv, position)
            false
        }*/


    }

    inner class MyViewHolder(view: View) : ViewHolder(view), View.OnCreateContextMenuListener {
        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuinfo: ContextMenu.ContextMenuInfo?) {
            menu?.add(Menu.NONE, v?.id!!, 0, "Cancel")
            menu?.add(Menu.NONE, v?.id!!, 0, "Edit")
        }

        var tvCount: TextView = view.findViewById(R.id.tv_count)
        var tvDescription: TextView = view.findViewById(R.id.tv_dsc)
        var timeTv: TextView = view.findViewById(R.id.tv_time)
        var activeView: View = view.findViewById(R.id.active_view)
        var menu: ImageView = view.findViewById(R.id.iv_menu)
    }

    private fun openOptionMenu(v: View, position: Int) {
        val alarm = alarms[position]
        val popup = PopupMenu(v.context, v)
        popup.menuInflater.inflate(R.menu.upcoming_menu, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.edit -> {
                    val myIntent = Intent(context, EditAlarmActivity::class.java)
                    myIntent.putExtra("docId", alarm.docId)
                    myIntent.putExtra("id", alarm.id)
                    myIntent.putExtra("description", alarm.description)
                    myIntent.putExtra("tonePath", alarm.alarmTonePath)
                    myIntent.putExtra("fromDate", alarm.fromDate)
                    myIntent.putExtra("toDate", alarm.toDate)
                    myIntent.putExtra("place", alarm.place)
                    myIntent.putExtra("medType", alarm.medicationType)
                    myIntent.putExtra("repeatMode", alarm.repeatMode)
//                    (context as Activity).startActivityForResult(myIntent, 900)
                    myIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(myIntent)
                }
                R.id.cancel -> {
                    val myIntent = Intent(context, CancelAlarmActivity::class.java)
                    myIntent.putExtra("docId", alarm.docId)
                    myIntent.putExtra("alarmId", alarm.id)
                    myIntent.putExtra("place", alarm.place)
                    context.startActivity(myIntent)
                }
            }
            true
        }
        popup.show()
    }
}