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
        val usersRef = db.collection("users")

        cl_btn_submit.setOnClickListener {
            var phoneNumber: String = edit_text.text.toString().trim()

            if (phoneNumber.isNotEmpty()) {
                var list = phoneNumber.split("")
                if (list[0] == "")
                    list = list.subList(1, list.size)

                if (list[0] != "+") {
                    if (list[0] == "0") {
                        list = list.subList(1, list.size)
                        phoneNumber = "+254${list.joinToString("")}"
                    } else {
                        phoneNumber = "+254${list.joinToString("")}"
                    }
                }


                if (phoneNumber.length < 10 || phoneNumber.length > 13) {
                    val toast = Toast(this)
                    val view: View = layoutInflater.inflate(R.layout.warning, null)
                    val textView: TextView = view.findViewById(R.id.message)
                    textView.text = getString(R.string.invalid_phone)
                    toast.view = view
                    toast.setGravity(Gravity.BOTTOM, 30, 30)
                    toast.duration = Toast.LENGTH_SHORT
                    toast.show()
                    input_layout.requestFocus()
                } else {
                    showProgress()
                    usersRef.document(phoneNumber)
                    usersRef.whereEqualTo("phoneNumber", phoneNumber)
                        .get()
                        .addOnCompleteListener {
                            hideProgress()
                            if (it.result!!.isEmpty) {
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
                            }
                        }
                        .addOnFailureListener {
                            hideProgress()
                            Log.d("TAG", "exception: $it")
                        }
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
