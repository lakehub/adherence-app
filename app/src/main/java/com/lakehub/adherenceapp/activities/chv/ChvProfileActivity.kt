package com.lakehub.adherenceapp.activities.chv

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.lakehub.adherenceapp.*
import com.lakehub.adherenceapp.app.AppPreferences
import com.lakehub.adherenceapp.app.MainApplication
import com.lakehub.adherenceapp.data.User
import com.lakehub.adherenceapp.repositories.UserRepository
import com.lakehub.adherenceapp.utils.*
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_chv_profile.*
import kotlinx.android.synthetic.main.activity_chv_profile.view.*
import kotlinx.android.synthetic.main.content_chv_profile.cl_logout
import kotlinx.android.synthetic.main.content_chv_profile.edit_text
import kotlinx.android.synthetic.main.content_chv_profile.tv_browse
import kotlinx.android.synthetic.main.content_chv_profile.use_access_key_switch
import kotlinx.android.synthetic.main.normal_toast.view.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream

class ChvProfileActivity : AppCompatActivity() {
    val userRef = FirebaseFirestore.getInstance().collection("users")
        .document(UserRepository().userId)
    private val gallery = 1002
    private var filePath: String? = null
    private lateinit var destinationUri: Uri
    private var inProgress = false
    private var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chv_profile)

        supportActionBar?.hide()

        toolbar.iv_back.setOnClickListener {
            onBackPressed()
        }

        tv_browse.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        cl_progress.makeGone()
        cl_refresh.makeGone()

        cl_refresh.setOnClickListener {
            uploadImage()
        }

        val switchStatesThumb =
            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.state_checked))


        val switchColorsThumb = intArrayOf(
            ContextCompat.getColor(this, R.color.colorRed),
            ContextCompat.getColor(this, R.color.colorGreen)
        )
        val switchColorListThumb = ColorStateList(switchStatesThumb, switchColorsThumb)

        val switchStatesTrack =
            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.state_checked))


        val switchColorsTrack = intArrayOf(
            ContextCompat.getColor(this, R.color.colorGreen),
            ContextCompat.getColor(this, R.color.colorRed)
        )
        val switchColorListTrack = ColorStateList(switchStatesTrack, switchColorsTrack)

        use_access_key_switch.thumbTintList = switchColorListThumb
        use_access_key_switch.trackTintList = switchColorListTrack

        use_access_key_switch.setOnCheckedChangeListener { _, checked ->
            if(user?.hasAccessKey == checked) return@setOnCheckedChangeListener

            if(checked)
                showEditAccesssKeyDialog()
            else
                disableAccessKey()
        }

        cl_logout.setOnClickListener {
            AppPreferences.profileImg = null
            emptyDirectory("user_images")
            emptyDirectory("client_images")
            FirebaseAuth.getInstance().signOut()
            this.finishAffinity()
        }

        if (AppPreferences.profileImg != null) {
            val contextWrapper = ContextWrapper(MainApplication.applicationContext())
            val directory: File = contextWrapper.getDir("user_images", Context.MODE_PRIVATE)
            var bitmap = loadImgFromInternalStorage(
                directory.absolutePath,
                AppPreferences.profileImg!!
            )
            Glide.with(this)
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
                .into(iv_toolbar)

        }

        toolbar_username.text =
            titleCase(UserRepository().userId)

        val mContextWrapper = ContextWrapper(this)
        val mDirectory: File = mContextWrapper.getDir(
            "Temp",
            Context.MODE_PRIVATE
        )
        val path = File(mDirectory, "temp.png")
        destinationUri = Uri.parse(path.path)

        tv_browse.setOnClickListener {
            if (!inProgress) {
                if (Build.VERSION.SDK_INT >= 23) {
                    Dexter.withActivity(this)
                        .withPermissions(
                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission
                                .WRITE_EXTERNAL_STORAGE
                        )
                        .withListener(object : MultiplePermissionsListener {
                            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                                choosePhotoFromGallery()
                            }

                            override fun onPermissionRationaleShouldBeShown(
                                permissions: MutableList<PermissionRequest>?, token: PermissionToken?
                            ) {
                                token?.continuePermissionRequest()
                            }

                        }).check()
                } else {
                    choosePhotoFromGallery()
                }
            }
        }

        edit_text.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(str: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val username = str.toString().trim()
                toolbar_username.text = titleCase(username)
                userRef.update("name", username)
                    .addOnCompleteListener {
//                        AppPreferences.myName = username
                    }
            }

        })

        lifecycleScope.launch {
            try {
                user = UserRepository().getCurrentUser()
                use_access_key_switch.isChecked = user!!.hasAccessKey
                use_access_key_switch.visibility = View.VISIBLE
            }catch (e: Exception) {
                Log.e("SettingsActivity", "Failed to load user: ${e.message}")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                gallery -> {
                    cl_refresh.makeGone()
                    val uri: Uri = data?.data!!
                    filePath = getRealPathFromURIPath(uri, this)
                    UCrop.of(uri, destinationUri)
                        .withAspectRatio(5f, 5f)
                        .start(this)
                }
                UCrop.REQUEST_CROP -> {
                    val uri: Uri = UCrop.getOutput(data!!)!!
                    filePath = getRealPathFromURIPath(uri, this)
                    uploadImage()
                    try {
                        //getting image from gallery
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            val source = ImageDecoder.createSource(contentResolver, uri)
                            val bitmap = ImageDecoder.decodeBitmap(source)
                            iv_toolbar.setImageBitmap(bitmap)
                        } else {
                            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                            iv_toolbar.setImageBitmap(bitmap)
                        }

                        //val bg = BitmapDrawable(bitmap)
                        //Setting image to ImageView
                        //                    profile_parent_img.setImageBitmap(bitmap)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } else if (requestCode == UCrop.RESULT_ERROR) {
            val cropError: Throwable? = UCrop.getError(data!!)
            Log.d("TAG", "crop error: $cropError")
        }
    }

    private fun choosePhotoFromGallery() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )

        startActivityForResult(galleryIntent, gallery)
    }

    private fun uploadImage() {
        inProgress = true
        cl_refresh.makeGone()
        cl_progress.makeVisible()
        val storageRef = FirebaseStorage.getInstance().reference
        val metadata = StorageMetadata.Builder()
            .setContentType("image/jpeg")
            .build()
        val filename = "${getRandomString(26)}.jpeg"
        val imgRef = storageRef.child("chv_images/$filename")
        val stream = FileInputStream(File(filePath!!))
        val uploadTask = imgRef.putStream(stream, metadata)

        uploadTask.addOnSuccessListener {
            inProgress = false
            cl_progress.makeGone()
            val toast = Toast(this)
            val view = View.inflate(this, R.layout.normal_toast, null)
            view.message.text = getString(R.string.img_update_success)
            toast.view = view
            toast.setGravity(Gravity.BOTTOM, 30, 30)
            toast.duration = Toast.LENGTH_SHORT
            toast.show()
            emptyDirectory("Temp")

            userRef.update("image", filename)
            AppPreferences.profileImg = filename
            val mContextWrapper = ContextWrapper(this)
            val mDirectory: File = mContextWrapper.getDir(
                "user_images",
                Context.MODE_PRIVATE
            )
            val file = File(mDirectory, filename)
            imgRef.getFile(file)
        }.addOnFailureListener {
            inProgress = false
            cl_progress.makeGone()
            cl_refresh.makeVisible()
            val toast = Toast(this)
            val view = View.inflate(this, R.layout.warning, null)
            val textView = view.findViewById<TextView>(R.id.message)
            textView.text = getString(R.string.upload_failure_message)
            toast.view = view
            toast.setGravity(Gravity.BOTTOM, 30, 30)
            toast.duration = Toast.LENGTH_SHORT
            toast.show()
        }.addOnProgressListener {
            Log.d("TAG", "progress: ${100 * it.bytesTransferred}")
        }

    }

    private fun showEditAccesssKeyDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setCancelable(true)
        val inflater: LayoutInflater = this.layoutInflater
        val dialogView: View = inflater.inflate(R.layout.content_edit_access_key, null)
        alertDialogBuilder.setView(dialogView)
        val alertDialog = alertDialogBuilder.create()

        val accessKeyEditText = dialogView.findViewById<TextInputEditText>(R.id.input_access_key)
        val cancelView = dialogView.findViewById<TextView>(R.id.tv_cancel)
        val okView = dialogView.findViewById<TextView>(R.id.tv_ok)

        accessKeyEditText.setText(user?.accessKey ?: "")

        cancelView.setOnClickListener {
            alertDialog.dismiss()
        }

        accessKeyEditText.doAfterTextChanged {
            okView.isEnabled = it?.isNotEmpty() ?: false
        }

        okView.setOnClickListener {
            alertDialog.dismiss()
            user!!.hasAccessKey = true
            user!!.accessKey = accessKeyEditText.text.toString()
            UserRepository().updateAccessKey(true, user!!.accessKey)
        }

        alertDialog.show()
    }

    private fun disableAccessKey() {
        lifecycleScope.launch {
            UserRepository().updateAccessKey(false)
        }
    }
}
