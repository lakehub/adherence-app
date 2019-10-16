package com.lakehub.adherenceapp.activities.client

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.lakehub.adherenceapp.R
import com.lakehub.adherenceapp.app.AppPreferences
import com.lakehub.adherenceapp.app.MainApplication
import com.lakehub.adherenceapp.data.User
import com.lakehub.adherenceapp.utils.*
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.app_bar_client_home.toolbar
import kotlinx.android.synthetic.main.app_bar_settings.view.iv_back
import kotlinx.android.synthetic.main.content_settings.*
import kotlinx.android.synthetic.main.normal_toast.view.*
import java.io.File
import java.io.FileInputStream

class SettingsActivity : AppCompatActivity() {
    private val gallery = 1002
    private var filePath: String? = null
    private lateinit var destinationUri: Uri
    private var inProgress = false
    private val userRef = FirebaseFirestore.getInstance().collection("users")
        .document(AppPreferences.accessKey!!)
    private var myForResult = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

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

        cl_logout.setOnClickListener {
            AppPreferences.loggedIn = false
            AppPreferences.accessKey = null
            AppPreferences.accountType = 0
            AppPreferences.chvAccessKey = null
            AppPreferences.profileImg = null
            AppPreferences.authenticated = false
            emptyDirectory("user_images")

            this.finishAndRemoveTask()
        }

        userRef.get()
            .addOnCompleteListener {
                if (it.isComplete) {
                    val user = it.result!!.toObject(User::class.java)
                    tvPoints.text = user?.points.toString()
                }
            }

        if (AppPreferences.profileImg != null) {
            val contextWrapper = ContextWrapper(MainApplication.applicationContext())
            val directory: File = contextWrapper.getDir("user_images", Context.MODE_PRIVATE)
            var bitmap =
                loadImgFromInternalStorage(
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

        toolbar_username.text = titleCase(AppPreferences.accessKey!!)
//        edit_text.setText(titleCase(AppPreferences.myName!!))

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
                    forResult = true
                    Dexter.withActivity(this)
                        .withPermissions(
                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission
                                .WRITE_EXTERNAL_STORAGE
                        )
                        .withListener(object : MultiplePermissionsListener {
                            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                                forResult = false
                                if (report?.areAllPermissionsGranted() == true) {
                                    choosePhotoFromGallery()
                                } else {
                                    showWarning(getString(R.string.grant_perm_file))
                                }
                            }

                            override fun onPermissionRationaleShouldBeShown(
                                permissions: MutableList<PermissionRequest>?,
                                token: PermissionToken?
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
                /* userRef.update("name", username)
                     .addOnCompleteListener {
                         AppPreferences.myName = username
                     }*/
            }

        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        forResult = false

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                gallery -> {
                    forResult = true
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
        forResult = true
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
        val imgRef = storageRef.child("client_images/$filename")
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

    var forResult: Boolean
        get() = myForResult
        set(value) {
            myForResult = value
        }
}
