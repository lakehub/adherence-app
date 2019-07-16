package com.lakehub.adherenceapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.lakehub.adherenceapp.R
import com.lakehub.adherenceapp.data.FollowUp
import com.lakehub.adherenceapp.limitStringLength
import com.lakehub.adherenceapp.titleCase


class FollowUpAdapter(val context: Context, private val followUps: ArrayList<FollowUp>) :
    androidx.recyclerview.widget.RecyclerView.Adapter<FollowUpAdapter.MyViewHolder>() {

    override fun getItemCount(): Int {
        return followUps.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.follow_up_row, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val followUp = followUps[position]

        holder.tv.text = titleCase(limitStringLength(followUp.clientName.split(" ")[0], 10))

    }

    inner class MyViewHolder(view: View) : ViewHolder(view) {
        var tv: TextView = view.findViewById(R.id.text_view)
    }

}