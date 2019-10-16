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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.lakehub.adherenceapp.*
import com.lakehub.adherenceapp.activities.chv.MakeAppointmentActivity
import com.lakehub.adherenceapp.app.MainApplication
import com.lakehub.adherenceapp.data.FollowUp
import com.lakehub.adherenceapp.utils.loadImgFromInternalStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File


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

        holder.tv.text = followUp.clientAccessKey
        val clientRef = FirebaseFirestore.getInstance().collection("users").document(followUp.clientAccessKey)

        clientRef.get()
            .addOnCompleteListener {
                if (it.isComplete) {
                    val image = it.result?.getString("image")
                    if (image != null) {
                        val contextWrapper = ContextWrapper(MainApplication.applicationContext())
                        val directory: File = contextWrapper.getDir("client_images", Context.MODE_PRIVATE)
                        var bitmap = loadImgFromInternalStorage(
                            directory.absolutePath,
                            image
                        )
                        if (bitmap == null) {
                            val storageRef = FirebaseStorage.getInstance().reference
                            val filename = image
                            val imgRef = storageRef.child("client_images/$filename")
                            val mContextWrapper = ContextWrapper(context)
                            val mDirectory: File = mContextWrapper.getDir(
                                "client_images",
                                Context.MODE_PRIVATE
                            )
                            val file = File(mDirectory, filename)
                            imgRef.getFile(file).addOnSuccessListener {
                                var myBitmap =
                                    loadImgFromInternalStorage(
                                        directory.absolutePath,
                                        image
                                    )
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
                                    .into(holder.imgView)
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
                                .into(holder.imgView)
                        }


                    }
                }
            }

        holder.itemView.setOnClickListener {
            val activity = context as Activity
            val intent = Intent(context, MakeAppointmentActivity::class.java)
            intent.putExtra("clientAccessKey", followUp.clientAccessKey)
            intent.putExtra("date", followUp.dateTime)
            activity.startActivity(intent)
        }
    }

    inner class MyViewHolder(view: View) : ViewHolder(view) {
        var tv: TextView = view.findViewById(R.id.text_view)
        var imgView: CircleImageView = view.findViewById(R.id.image_view)
    }

}