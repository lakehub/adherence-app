package com.lakehub.adherenceapp.adapters

import android.content.Context
import android.view.*
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.firebase.firestore.FirebaseFirestore
import com.lakehub.adherenceapp.*
import com.lakehub.adherenceapp.data.Alarm
import com.lakehub.adherenceapp.data.FollowUp
import com.lakehub.adherenceapp.utils.displayTime
import com.lakehub.adherenceapp.utils.limitStringLength
import com.lakehub.adherenceapp.utils.titleCase
import kotlinx.android.synthetic.main.normal_toast.view.*
import org.joda.time.format.DateTimeFormat


class MissedMedicationAdapter(val context: Context, private val alarms: ArrayList<Alarm>) :
    androidx.recyclerview.widget.RecyclerView.Adapter<MissedMedicationAdapter.MyViewHolder>() {

    override fun getItemCount(): Int {
        return alarms.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.missed_medication_row, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val alarm = alarms[position]

        holder.tvDescription.text =
            limitStringLength(alarm.description, 25)
        holder.tvCount.text = (position + 1).toString()
        holder.timeTv.text = displayTime(alarm.fromDate)
        val medType = if (alarm.medicationType == 1) {
            context.getString(R.string.treatment)
        } else {
            context.getString(R.string.arv)
        }
        holder.medTv.text = titleCase(medType)

        holder.menu.setOnClickListener {
            openOptionMenu(holder.menu, holder.adapterPosition)
        }
        holder.accessKeyTv.text = alarm.accessKey

        if (alarm.snoozed > 0) {
            val format = "yyyy MM dd HH:mm"
            val myFormatter = DateTimeFormat.forPattern(format)
            val myDate = myFormatter.parseDateTime(alarm.fromDate)
            val newDate = myDate.plusMinutes(alarm.snoozed)
            holder.timeTv.text = displayTime(newDate)
        }


    }

    inner class MyViewHolder(view: View) : ViewHolder(view) {

        var tvCount: TextView = view.findViewById(R.id.tv_count)
        var tvDescription: TextView = view.findViewById(R.id.tv_dsc)
        var timeTv: TextView = view.findViewById(R.id.tv_time)
        var medTv: TextView = view.findViewById(R.id.tv_medication)
        var accessKeyTv: TextView = view.findViewById(R.id.tvAccessKey)
        var menu: ImageView = view.findViewById(R.id.iv_menu)
    }

    private fun openOptionMenu(v: View, position: Int) {
        val alarm = alarms[position]
        val popup = PopupMenu(v.context, v)
        popup.menuInflater.inflate(R.menu.missed_med_menu, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.mark -> {
                    val ref = FirebaseFirestore.getInstance().collection("follow_ups").document()
                    val alarmRef = FirebaseFirestore.getInstance().collection("alarms")
                        .document(alarm.docId!!)
                    val data = FollowUp(
                        clientAccessKey = alarm.accessKey!!,
                        date = alarm.date!!,
                        dateTime = alarm.fromDate
                    )
                    alarmRef.update("marked", true)
                        .addOnCompleteListener {
                            if (it.isComplete) {
                                val followUpRef = FirebaseFirestore.getInstance().collection("follow_ups")
                                followUpRef.whereEqualTo("date", alarm.date)
                                    .whereEqualTo("clientAccessKey", alarm.accessKey)
                                    .whereEqualTo("marked", false)
                                    .get()
                                    .addOnCompleteListener {qs ->
                                        if (qs.isComplete) {
                                                if (qs.result?.isEmpty!!) {
                                                    ref.set(data)
                                                        .addOnCompleteListener { task ->
                                                            if (task.isComplete) {
                                                                val toast = Toast(context)
                                                                val view = View.inflate(context, R.layout.normal_toast, null)
                                                                view.message.text = context.getString(R.string.mark_follow_up_success)
                                                                toast.view = view
                                                                toast.setGravity(Gravity.BOTTOM, 30, 30)
                                                                toast.duration = Toast.LENGTH_SHORT
                                                                toast.show()
                                                            }
                                                        }
                                                } else {

                                                }

                                        }
                                    }

                            }
                        }
                }
            }
            true
        }
        popup.show()
    }
}