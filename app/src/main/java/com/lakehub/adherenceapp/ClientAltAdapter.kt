package com.lakehub.adherenceapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder


class ClientAltAdapter(val context: Context, private val clients: ArrayList<Client>) :
    androidx.recyclerview.widget.RecyclerView.Adapter<ClientAltAdapter.MyViewHolder>() {

    override fun getItemCount(): Int {
        return clients.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.client_alt_row, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val client = clients[position]
        holder.nameTv.text = titleCase(client.name)
        holder.phoneTv.text = context.getString(R.string.phone_no, client.phoneNumber.substring(4))

        holder.itemView.setOnClickListener {

        }
    }

    inner class MyViewHolder(view: View) : ViewHolder(view) {
        var nameTv: TextView = view.findViewById(R.id.tv_name)
        var phoneTv: TextView = view.findViewById(R.id.tv_phone_no)
    }
}