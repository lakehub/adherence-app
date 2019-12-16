package com.lakehub.adherenceapp.adapters

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.view.*
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.firebase.firestore.FirebaseFirestore
import com.lakehub.adherenceapp.*
import com.lakehub.adherenceapp.activities.chv.EditChvReminderActivity
import com.lakehub.adherenceapp.activities.chv.SingleReminderActivity
import com.lakehub.adherenceapp.app.MainApplication
import com.lakehub.adherenceapp.data.ChvReminder
import com.lakehub.adherenceapp.receivers.ChvReminderReceiver
import com.lakehub.adherenceapp.utils.*
import org.joda.time.format.DateTimeFormat


class ChvReminderAdapter(val context: Context, private val alarms: ArrayList<ChvReminder>) :
    androidx.recyclerview.widget.RecyclerView.Adapter<ChvReminderAdapter.MyViewHolder>() {
    private var position: Int = 0

    var currentPosition: Int
        get() = position
        set(value) {
            position = value
        }

    override fun getItemCount(): Int {
        return alarms.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.chv_reminder_row, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val alarm = alarms[position]

        holder.tvDescription.text =
            limitStringLength(alarm.description, 15)
        holder.tvCount.text = (position + 1).toString()

        when {
            alarm.appointment!! -> holder.clientTv.text = alarm.clientUserId
            alarm.drug!! -> holder.clientTv.text = if (alarm.medicationType == 1) {
                context.getString(R.string.treatment)
            } else {
                context.getString(R.string.arv)
            }
            else -> holder.clientTv.text = if (alarm.hospital == null) {
                ""
            } else {
                titleCase(
                    limitStringLength(
                        alarm.hospital,
                        8
                    )
                )
            }
        }

        if (alarm.recent) {
            holder.timeTv.text = displayDateTime(alarm.dateTime)
        } else {
            holder.timeTv.text = displayTime(alarm.dateTime)
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
            val myDate = myFormatter.parseDateTime(alarm.dateTime)
            val newDate = myDate.plusMinutes(alarm.snoozed)
            holder.timeTv.text = displayTime(newDate)
        }

        if (alarm.repeatMode?.size == 1) {
            when (alarm.repeatMode[0]) {
                8 -> {
                    holder.tvMode.text = context.getString(R.string.once)
                }
                9 -> {
                    holder.tvMode.text = context.getString(R.string.daily)
                }
                10 -> {
                    holder.tvMode.text = context.getString(R.string.weekday)
                }
            }
        } else {
            holder.tvMode.text = context.getString(R.string.custom)
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
            myIntent.putExtra("clientUserId", alarm.clientUserId)
            myIntent.putExtra("hospital", alarm.hospital)
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
        override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?,
            menuinfo: ContextMenu.ContextMenuInfo?
        ) {
            menu?.add(Menu.NONE, v?.id!!, 0, "Cancel")
            menu?.add(Menu.NONE, v?.id!!, 0, "Edit")
        }

        var tvCount: TextView = view.findViewById(R.id.tv_count)
        var tvDescription: TextView = view.findViewById(R.id.tv_dsc)
        var timeTv: TextView = view.findViewById(R.id.tv_time)
        var clientTv: TextView = view.findViewById(R.id.tv_client)
        var activeView: View = view.findViewById(R.id.active_view)
        var menu: ImageView = view.findViewById(R.id.iv_menu)
        var tvMode: TextView = view.findViewById(R.id.tvMode)
    }

    private fun openOptionMenu(v: View, position: Int) {
        val alarm = alarms[position]
        val popup = PopupMenu(v.context, v)
        popup.menuInflater.inflate(R.menu.upcoming_menu, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.edit -> {
                    val myIntent = Intent(context, EditChvReminderActivity::class.java)
                    myIntent.putExtra("docId", alarm.docId)
                    myIntent.putExtra("id", alarm.id)
                    myIntent.putExtra("description", alarm.description)
                    myIntent.putExtra("tonePath", alarm.alarmTonePath)
                    myIntent.putExtra("date", alarm.dateTime)
                    myIntent.putExtra("drug", alarm.drug)
                    myIntent.putExtra("appointment", alarm.appointment)
                    myIntent.putExtra("medType", alarm.medicationType)
                    myIntent.putExtra("repeatMode", alarm.repeatMode)
                    myIntent.putExtra("clientUserId", alarm.clientUserId)
                    myIntent.putExtra("hospital", alarm.hospital)
                    myIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(myIntent)
                }
                R.id.cancel -> {
                    val alarmsRef = FirebaseFirestore.getInstance()
                        .collection("chv_reminders")
                        .document(alarm.docId!!)

                    alarmsRef.update("cancelled", true)
                    val activity = context as AppCompatActivity?
                    activity?.showWarning(context.getString(R.string.alarm_cancel_success))
                    val alarmManager =
                        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                    val myIntent =
                        Intent(
                            MainApplication.applicationContext(),
                            ChvReminderReceiver::class.java
                        )
                    val pendingIntent =
                        PendingIntent.getBroadcast(
                            context,
                            alarm.id,
                            myIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    alarmManager.cancel(pendingIntent)
                }
            }
            true
        }
        popup.show()
    }
}