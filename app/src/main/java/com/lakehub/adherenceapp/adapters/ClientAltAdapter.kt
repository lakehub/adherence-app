package com.lakehub.adherenceapp.adapters

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.firebase.storage.FirebaseStorage
import com.lakehub.adherenceapp.MainApplication
import com.lakehub.adherenceapp.data.Client
import com.lakehub.adherenceapp.R
import com.lakehub.adherenceapp.loadImgFromInternalStorage
import com.lakehub.adherenceapp.titleCase
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File


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
        holder.tvAccessKey.text = titleCase(client.accessKey)
        holder.tvLocation.text = titleCase(client.location)

        holder.itemView.setOnClickListener {
            val returnIntent = Intent()
            returnIntent.putExtra("accessKey", client.accessKey)
            val activity = context as Activity
            activity.setResult(Activity.RESULT_OK, returnIntent)
            activity.finish()

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
        var userIv: CircleImageView = view.findViewById(R.id.iv_user)
    }
}