package com.lakehub.adherenceapp.adapters

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.view.*
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.firebase.firestore.FirebaseFirestore
import com.lakehub.adherenceapp.*
import com.lakehub.adherenceapp.data.ChvReminder
import com.lakehub.adherenceapp.receivers.ChvReminderReceiver
import kotlinx.android.synthetic.main.delete_success_toast.view.*
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

        holder.tvDescription.text = limitStringLength(alarm.description, 40)
        holder.tvCount.text = (position + 1).toString()

        when {
            alarm.isAppointment!! -> holder.clientTv.text = limitStringLength(alarm.clientName?.split(" ")?.get(0)!!, 8)
            alarm.isDrug!! -> holder.clientTv.text = if (alarm.medType == 1) {
                context.getString(R.string.treatment)
            } else {
                context.getString(R.string.arv)
            }
            else -> holder.clientTv.text = titleCase(limitStringLength(alarm.hospital!!, 8))
        }

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
        var clientTv: TextView = view.findViewById(R.id.tv_client)
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
                    val myIntent = Intent(context, EditChvReminderActivity::class.java)
                    myIntent.putExtra("docId", alarm.docId)
                    myIntent.putExtra("id", alarm.id)
                    myIntent.putExtra("description", alarm.description)
                    myIntent.putExtra("tonePath", alarm.alarmTone)
                    myIntent.putExtra("date", alarm.fromDate)
                    myIntent.putExtra("isDrug", alarm.isDrug)
                    myIntent.putExtra("isAppointment", alarm.isAppointment)
                    myIntent.putExtra("medType", alarm.medType)
                    myIntent.putExtra("repeatMode", alarm.repeatMode)
                    myIntent.putExtra("clientName", alarm.clientName)
                    myIntent.putExtra("clientPhoneNo", alarm.clientPhoneNo)
                    myIntent.putExtra("hospital", alarm.hospital)
                    context.startActivity(myIntent)
                }
                R.id.cancel -> {
                    val alarmsRef = FirebaseFirestore.getInstance()
                        .collection("chv_reminders")
                        .document(alarm.docId!!)

                    alarmsRef.update("cancelled", true)
                        .addOnCompleteListener {
                            if (it.isComplete) {
                                val toast = Toast(MainApplication.applicationContext())
                                val view: View = View.inflate(
                                    MainApplication.applicationContext(),
                                    R.layout.delete_success_toast, null
                                )
                                val textView: TextView = view.findViewById(R.id.message)
                                textView.text = context.getString(R.string.alarm_cancel_success)
                                toast.view = view
                                toast.setGravity(Gravity.BOTTOM, 30, 30)
                                toast.duration = Toast.LENGTH_LONG
                                toast.show()
                                view.tv_undo.setOnClickListener {

                                }


                                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                                val myIntent =
                                    Intent(MainApplication.applicationContext(), ChvReminderReceiver::class.java)
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
                }
            }
            true
        }
        popup.show()
    }
}