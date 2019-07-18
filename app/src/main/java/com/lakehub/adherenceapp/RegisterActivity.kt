package com.lakehub.adherenceapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        supportActionBar?.hide()

        hideProgress()

        val db = FirebaseFirestore.getInstance()
        db.enableNetwork()
        val usersRef = db.collection("users")

        cl_btn_submit.setOnClickListener {
            var phoneNumber: String = edit_text.text.toString().trim()

            if (phoneNumber.isNotEmpty()) {
                if (phoneNumber.length == 10 || phoneNumber.length == 13) {
                    if (phoneNumber.length == 10) {
                        phoneNumber = "+254${phoneNumber.substring(1)}"
                    }
                    showProgress()
                    usersRef.document(phoneNumber)
                        .get()
                        .addOnCompleteListener {
                            hideProgress()
                            if (it.isSuccessful) {
                                if (it.result!!.data == null) {
                                    val userRef = db.collection("users")
                                    val myUser = hashMapOf(
                                        "phoneNumber" to phoneNumber,
                                        "category" to 2,
                                        "points" to 0,
                                        "clients" to 0
                                    )
                                    userRef.document(phoneNumber)
                                        .set(myUser)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                AppPreferences.accountType = 2
                                                AppPreferences.loggedIn = true
                                                AppPreferences.phoneNo = phoneNumber
                                                startActivity(Intent(this, ChvDashboardActivity::class.java))
                                                finish()
                                            } else {
                                                hideProgress()
                                                val toast = Toast(this)
                                                val view: View = layoutInflater.inflate(R.layout.network_error, null)
                                                toast.view = view
                                                toast.setGravity(Gravity.BOTTOM, 30, 30)
                                                toast.duration = Toast.LENGTH_SHORT
                                                toast.show()
                                            }
                                        }
                                } else {
                                    val toast = Toast(this)
                                    val view: View = layoutInflater.inflate(R.layout.warning, null)
                                    val textView: TextView = view.findViewById(R.id.message)
                                    textView.text = getString(R.string.already_registered)
                                    toast.view = view
                                    toast.setGravity(Gravity.BOTTOM, 30, 30)
                                    toast.duration = Toast.LENGTH_SHORT
                                    toast.show()
                                }
                                /*if (!it.result?.exists()!!) {
                                    val myIntent = Intent(this@RegisterActivity, VerifyActivity::class.java)
                                    myIntent.putExtra("phoneNumber", phoneNumber)
                                    myIntent.putExtra("newUser", true)
                                    startActivity(myIntent)
                                    finish()
                                } else {
                                    val toast = Toast(this)
                                    val view: View = layoutInflater.inflate(R.layout.warning, null)
                                    val textView: TextView = view.findViewById(R.id.message)
                                    textView.text = getString(R.string.already_registered)
                                    toast.view = view
                                    toast.setGravity(Gravity.BOTTOM, 30, 30)
                                    toast.duration = Toast.LENGTH_SHORT
                                    toast.show()
                                    input_layout.requestFocus()
                                }*/
                            } else {
                                hideProgress()
                                val toast = Toast(this)
                                val view: View = layoutInflater.inflate(R.layout.network_error, null)
                                toast.view = view
                                toast.setGravity(Gravity.BOTTOM, 30, 30)
                                toast.duration = Toast.LENGTH_SHORT
                                toast.show()
                            }
                        }
                } else {
                    val toast = Toast(this)
                    val view: View = layoutInflater.inflate(R.layout.warning, null)
                    val textView: TextView = view.findViewById(R.id.message)
                    textView.text = getString(R.string.invalid_phone)
                    toast.view = view
                    toast.setGravity(Gravity.BOTTOM, 30, 30)
                    toast.duration = Toast.LENGTH_SHORT
                    toast.show()
                    input_layout.requestFocus()
                }
            } else {
                val toast = Toast(this)
                val view: View = layoutInflater.inflate(R.layout.warning, null)
                val textView: TextView = view.findViewById(R.id.message)
                textView.text = getString(R.string.enter_phone)
                toast.view = view
                toast.setGravity(Gravity.BOTTOM, 30, 30)
                toast.duration = Toast.LENGTH_SHORT
                toast.show()
                input_layout.requestFocus()
            }
        }

        tv_login.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

    }

    override fun onBackPressed() {

        this.finishAffinity()
    }

    private fun showProgress() {
        tv_btn_submit.text = getString(R.string.submitting)
        tv_btn_submit.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
        progress_bar_submit.visibility = View.VISIBLE
        cl_btn_submit.setBackgroundColor(ContextCompat.getColor(this, R.color.materialColorGray))
    }

    private fun hideProgress() {
        tv_btn_submit.text = getString(R.string.verify)
        tv_btn_submit.setTextColor(
            ContextCompat.getColor(applicationContext, android.R.color.white)
        )

        progress_bar_submit.visibility = View.GONE
        cl_btn_submit.setBackgroundColor(ContextCompat.getColor(this, R.color.colorYellow))
    }
}
