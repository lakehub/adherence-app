package com.lakehub.adherenceapp.activities.startup

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.lakehub.adherenceapp.R
import com.lakehub.adherenceapp.activities.chv.ChvDashboardActivity
import com.lakehub.adherenceapp.activities.client.ClientHomeActivity
import com.lakehub.adherenceapp.app.AppPreferences
import com.lakehub.adherenceapp.data.User
import com.lakehub.adherenceapp.utils.USER_CLIENT
import com.lakehub.adherenceapp.utils.showNetworkError
import com.lakehub.adherenceapp.utils.showWarning
import kotlinx.android.synthetic.main.activity_login.*
import java.io.File

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        supportActionBar?.hide()

        hideProgress()

        val hadLaunched: Boolean = intent.getBooleanExtra("hadLaunched", false)

        val db = FirebaseFirestore.getInstance()

        val usersRef = db.collection("users")

        cl_btn_submit.setOnClickListener {
            val accessKey: String = edit_text.text.toString().trim()

            if (accessKey.isNotEmpty()) {
                if (AppPreferences.loggedIn) {
                    if (accessKey.equals(AppPreferences.accessKey, true)) {
                        AppPreferences.appInForeground = true
                        /*if (AppPreferences.accountType == USER_CLIENT) {
                           startActivity(Intent(this, ClientHomeActivity::class.java))
                        } else {
                            startActivity(Intent(this, ChvDashboardActivity::class.java))
                        }*/
                        if (hadLaunched) {
                            finish()
                        } else {
                            finish()
                            if (AppPreferences.accountType == USER_CLIENT) {
                                startActivity(Intent(this, ClientHomeActivity::class.java))
                            } else {
                                startActivity(Intent(this, ChvDashboardActivity::class.java))
                            }
                        }
                    } else {
                        showWarning(getString(R.string.invalid_access_key))
                    }
                } else {

                    showProgress()
                    usersRef.document(accessKey)
                        .get()
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                hideProgress()
                                if (it.result!!.data != null) {
                                    val user = it.result!!.toObject(User::class.java)
                                    AppPreferences.accountType = user?.category!!
                                    AppPreferences.loggedIn = true
                                    AppPreferences.accessKey = user.accessKey
                                    AppPreferences.profileImg = user.image
                                    if (user.image != null) {
                                        val storageRef = FirebaseStorage.getInstance().reference
                                        val filename = user.image
                                        val imgRef = storageRef.child("client_images/$filename")
                                        val mContextWrapper = ContextWrapper(this)
                                        val mDirectory: File = mContextWrapper.getDir(
                                            "user_images",
                                            Context.MODE_PRIVATE
                                        )
                                        val file = File(mDirectory, filename!!)
                                        imgRef.getFile(file)
                                    }
                                    if (user.category == USER_CLIENT) {
                                        finish()
                                        AppPreferences.chvAccessKey = user.chvAccessKey
                                        startActivity(Intent(this, ClientHomeActivity::class.java))
                                    } else {
                                        finish()
                                        startActivity(Intent(this, ChvDashboardActivity::class.java))
                                    }
                                } else {
                                    showWarning(getString(R.string.invalid_access_key))
                                }
                            } else {
                                hideProgress()
                                showNetworkError()
                            }
                        }
                }

            } else {
                showWarning(getString(R.string.empty_access_key))
                input_layout.requestFocus()
            }
        }

    }

    private fun showProgress() {
        tv_btn_submit.text = getString(R.string.submitting)
        tv_btn_submit.setTextColor(ContextCompat.getColor(applicationContext,
            R.color.colorPrimary
        ))
        progress_bar_submit.visibility = View.VISIBLE
//        edit_text.isEnabled = false
        cl_btn_submit.setBackgroundColor(ContextCompat.getColor(this,
            R.color.materialColorGray
        ))
    }

    private fun hideProgress() {
        tv_btn_submit.text = getString(R.string.sign_in)
        tv_btn_submit.setTextColor(
            ContextCompat.getColor(applicationContext, android.R.color.black)
        )

        progress_bar_submit.visibility = View.GONE
        cl_btn_submit.setBackgroundColor(ContextCompat.getColor(this,
            R.color.colorYellow
        ))
//        edit_text.isEnabled = true
    }

    override fun onBackPressed() {
        AppPreferences.exit = true
        finish()
    }
}
