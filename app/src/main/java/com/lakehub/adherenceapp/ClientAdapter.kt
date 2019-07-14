package com.lakehub.adherenceapp

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.firebase.firestore.FirebaseFirestore


class ClientAdapter(val context: Context, private val clients: ArrayList<Client>) :
    androidx.recyclerview.widget.RecyclerView.Adapter<ClientAdapter.MyViewHolder>() {

    override fun getItemCount(): Int {
        return clients.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.client_row, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val client = clients[position]
        holder.locationTv.text = client.location
        holder.nameTv.text = titleCase(client.name)
        holder.locationTv.text = titleCase(client.location)
        holder.phoneTv.text = context.getString(R.string.phone_no, client.phoneNumber.substring(4))

        holder.menu.setOnClickListener {
            openOptionMenu(holder.menu, holder.adapterPosition)
        }
    }

    inner class MyViewHolder(view: View) : ViewHolder(view) {
        var nameTv: TextView = view.findViewById(R.id.tv_name)
        var phoneTv: TextView = view.findViewById(R.id.tv_phone_no)
        var locationTv: TextView = view.findViewById(R.id.tv_location)
        var menu: ImageView = view.findViewById(R.id.iv_options)
    }

    private fun openOptionMenu(v: View, position: Int) {
        val client = clients[position]
        val popup = PopupMenu(v.context, v)
        popup.menuInflater.inflate(R.menu.client_menu, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.edit -> {
                    val myIntent = Intent(context, EditClientActivity::class.java)
                    myIntent.putExtra("phoneNo", client.phoneNumber)
                    myIntent.putExtra("location", client.location)
                    myIntent.putExtra("name", client.name)
                    context.startActivity(myIntent)
                }
                R.id.delete -> {
                    val myIntent = Intent(context, DeactivateClientActivity::class.java)
                    myIntent.putExtra("phoneNo", client.phoneNumber)
                    context.startActivity(myIntent)
                }
            }
            true
        }
        popup.show()
    }
}