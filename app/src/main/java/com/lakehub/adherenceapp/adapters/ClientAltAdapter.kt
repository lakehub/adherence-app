package com.lakehub.adherenceapp.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.lakehub.adherenceapp.data.Client
import com.lakehub.adherenceapp.R
import com.lakehub.adherenceapp.titleCase


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
            val returnIntent = Intent()
            returnIntent.putExtra("name", client.name)
            returnIntent.putExtra("phoneNo", client.phoneNumber)
            val activity = context as Activity
            activity.setResult(Activity.RESULT_OK, returnIntent)
            activity.finish()

        }
    }

    inner class MyViewHolder(view: View) : ViewHolder(view) {
        var nameTv: TextView = view.findViewById(R.id.tv_name)
        var phoneTv: TextView = view.findViewById(R.id.tv_phone_no)
    }
}