package com.lakehub.adherenceapp

import android.content.Context
import android.view.*
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView.ViewHolder


class AlarmAdapter(val context: Context, private val alarms: ArrayList<Alarm>) :
    androidx.recyclerview.widget.RecyclerView.Adapter<AlarmAdapter.MyViewHolder>() {
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
                .inflate(R.layout.upcomig_list_row, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val alarm = alarms[position]

        holder.tvDescription.text = limitStringLength(alarm.description, 50)
        holder.timeTv.text = displayTime(alarm.fromDate)
        holder.tvCount.text = (position + 1).toString()

        if (holder.adapterPosition == 0) {
            holder.activeView.visibility = View.VISIBLE
        } else {
            holder.activeView.visibility = View.GONE
        }

        holder.menu.setOnClickListener {
            openOptionMenu(holder.menu, holder.adapterPosition)
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
        val popup = PopupMenu(v.context, v)
        popup.menuInflater.inflate(R.menu.upcoming_menu, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            Toast.makeText(context, "Feature coming soon", Toast.LENGTH_SHORT).show()
            true
        }
        popup.show()
    }
}