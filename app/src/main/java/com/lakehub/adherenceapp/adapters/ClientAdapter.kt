package com.lakehub.adherenceapp.adapters

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.firebase.storage.FirebaseStorage
import com.lakehub.adherenceapp.*
import com.lakehub.adherenceapp.data.Client
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File


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
        holder.tvLocation.text = client.location
        holder.tvAccessKey.text = titleCase(client.accessKey)

        holder.menu.setOnClickListener {
            openOptionMenu(holder.menu, holder.adapterPosition)
        }

        holder.itemView.setOnClickListener {
            val myIntent = Intent(context, ClientAppointmentsActivity::class.java)
            myIntent.putExtra("clientAccessKey", client.accessKey)
            context.startActivity(myIntent)
        }

        if (client.image != null) {
            val contextWrapper = ContextWrapper(MainApplication.applicationContext())
            val directory: File = contextWrapper.getDir("client_images", Context.MODE_PRIVATE)
            var bitmap = loadImgFromInternalStorage(directory.absolutePath, client.image)
            if (bitmap == null) {
                val storageRef = FirebaseStorage.getInstance().reference
                val filename = client.image
                val imgRef = storageRef.child("client_images/$filename")
                val mContextWrapper = ContextWrapper(context)
                val mDirectory: File = mContextWrapper.getDir(
                    "client_images",
                    Context.MODE_PRIVATE
                )
                val file = File(mDirectory, filename)
                imgRef.getFile(file).addOnSuccessListener {
                    var myBitmap = loadImgFromInternalStorage(directory.absolutePath, client.image)
                    Glide.with(context)
                        .load(myBitmap)
                        .apply(
                            RequestOptions()
                                .placeholder(R.drawable.user)
                                .error(R.drawable.user)
                        )
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?, model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                return false

                            }

                            override fun onResourceReady(
                                resource: Drawable?, model: Any?,
                                target: Target<Drawable>?, dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                myBitmap!!.recycle()
                                myBitmap = null
                                return false
                            }

                        })
                        .into(holder.userIv)
                }
            } else {
                Glide.with(context)
                    .load(bitmap)
                    .apply(
                        RequestOptions()
                            .placeholder(R.drawable.user)
                            .error(R.drawable.user)
                    )
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?, model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false

                        }

                        override fun onResourceReady(
                            resource: Drawable?, model: Any?,
                            target: Target<Drawable>?, dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            bitmap!!.recycle()
                            bitmap = null
                            return false
                        }

                    })
                    .into(holder.userIv)
            }


        }
    }

    inner class MyViewHolder(view: View) : ViewHolder(view) {
        var tvAccessKey: TextView = view.findViewById(R.id.tvAccessKey)
        var tvLocation: TextView = view.findViewById(R.id.tvLocation)
        var menu: ImageView = view.findViewById(R.id.iv_options)
        var userIv: CircleImageView = view.findViewById(R.id.iv_user)
    }

    private fun openOptionMenu(v: View, position: Int) {
        val client = clients[position]
        val popup = PopupMenu(v.context, v)
        popup.menuInflater.inflate(R.menu.client_menu, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.edit -> {
                    val myIntent = Intent(context, EditClientActivity::class.java)
                    myIntent.putExtra("accessKey", client.accessKey)
                    myIntent.putExtra("location", client.location)
                    myIntent.putExtra("name", client.name)
                    myIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(myIntent)
                }
                R.id.delete -> {
                    val myIntent = Intent(context, DeactivateClientActivity::class.java)
                    myIntent.putExtra("accessKey", client.accessKey)
                    myIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(myIntent)
                }
            }
            true
        }
        popup.show()
    }
}