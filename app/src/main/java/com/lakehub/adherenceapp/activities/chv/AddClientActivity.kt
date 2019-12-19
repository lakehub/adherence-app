package com.lakehub.adherenceapp.activities.chv

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.lakehub.adherenceapp.app.AppPreferences
import com.lakehub.adherenceapp.R
import com.lakehub.adherenceapp.data.Role
import com.lakehub.adherenceapp.data.User
import com.lakehub.adherenceapp.repositories.UserRepository
import com.lakehub.adherenceapp.utils.showNetworkError
import com.lakehub.adherenceapp.utils.showSuccess
import com.lakehub.adherenceapp.utils.showWarning
import kotlinx.android.synthetic.main.activity_add_client.*
import kotlinx.android.synthetic.main.content_add_client.*

class AddClientActivity : AppCompatActivity() {
    private var inProgress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_client)

        supportActionBar?.hide()
        supportActionBar?.setDisplayShowTitleEnabled(false)
        window.statusBarColor = ContextCompat.getColor(this,
            R.color.colorRedDark
        )
        hideProgress()

        fab.setColorFilter(Color.WHITE)

        val states = arrayOf(
            intArrayOf(-android.R.attr.state_enabled),
            intArrayOf(android.R.attr.state_enabled),
            intArrayOf(android.R.attr.state_pressed)
        )

        val colors = intArrayOf(
            ContextCompat.getColor(this, R.color.materialColorGray),
            ContextCompat.getColor(this, R.color.colorRedDark),
            ContextCompat.getColor(this, R.color.colorMaroon)
        )
        val colorList = ColorStateList(states, colors)
        fab.backgroundTintList = colorList

        iv_cancel.setOnClickListener {
            finish()
        }

        fab.setOnClickListener {
            var phoneNumber = et_phone_no.text.toString()
            val name = et_name.text.toString()
            val location = et_location.text.toString()
            if (phoneNumber.isNotEmpty() && name.isNotEmpty() && location.isNotEmpty()) {
                if (phoneNumber.length < 10 || phoneNumber.length > 13) {
                    val toast = Toast(this)
                    val view: View = layoutInflater.inflate(R.layout.warning, null)
                    val textView: TextView = view.findViewById(R.id.message)
                    textView.text = getString(R.string.invalid_phone)
                    toast.view = view
                    toast.setGravity(Gravity.BOTTOM, 30, 30)
                    toast.duration = Toast.LENGTH_SHORT
                    toast.show()
                } else {
                    if (phoneNumber.length == 10 || phoneNumber.length == 13) {
                        showProgress()
                        if (phoneNumber.length == 10) {
                            phoneNumber = "+254${phoneNumber.substring(1)}"
                        }
                        val db = FirebaseFirestore.getInstance()
                        val userRef = db.collection("users").document(phoneNumber)
                        val chvUserId = UserRepository().userId

                        userRef.get()
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    if (it.result?.data == null) {
                                        val user = User(
                                            chvUserId = chvUserId,
                                            location = location,
                                            name = name,
                                            role = Role.CLIENT,
                                            active = true
                                        )

                                        userRef.set(user)
                                            .addOnCompleteListener { task ->
                                                if (task.isComplete) {
                                                    hideProgress()
                                                    /*val chvDoc = db.collection("users")
                                                        .document(UserRepository().userId)

                                                    chvDoc.get()
                                                        .addOnCompleteListener { docSnap ->
                                                            if (docSnap.isComplete) {
                                                                chvDoc.update(
                                                                    "clients",
                                                                    docSnap.result?.getLong("clients")!!
                                                                        .toInt().plus(1)
                                                                )
                                                            }
                                                        }*/
                                                    showSuccess(getString(R.string.client_add_success))
                                                    finish()
                                                }
                                            }
                                    } else {
                                        hideProgress()
                                        showWarning(getString(R.string.client_exist))
                                    }
                                } else {
                                    hideProgress()
                                    showNetworkError()
                                }
                            }

                    } else {
                        showWarning(getString(R.string.invalid_phone))
                    }
                }
            } else {
                showWarning(getString(R.string.fill_fields))
            }
        }
    }

    private fun showProgress() {
        inProgress = true
        progress_bar.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        inProgress = false
        progress_bar.visibility = View.GONE
    }
}
