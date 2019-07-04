package com.lakehub.adherenceapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

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
                        .addOnSuccessListener { user ->
                            hideProgress()
                            Log.d("TAG", "doc: ${user.documents}")
                            if (!user.isEmpty) {
                                val myIntent = Intent(this@LoginActivity, VerifyActivity::class.java)
                                myIntent.putExtra("phoneNumber", phoneNumber)
                                startActivity(myIntent)
                                finish()
                            } else {
                                val toast = Toast(this)
                                val view: View = layoutInflater.inflate(R.layout.warning, null)
                                val textView: TextView = view.findViewById(R.id.message)
                                textView.text = getString(R.string.dnt_register)
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

        tv_register.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun showProgress() {
        tv_btn_submit.text = getString(R.string.submitting)
        tv_btn_submit.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
        progress_bar_submit.visibility = View.VISIBLE
//        edit_text.isEnabled = false
        cl_btn_submit.setBackgroundColor(ContextCompat.getColor(this, R.color.materialColorGray))
    }

    private fun hideProgress() {
        tv_btn_submit.text = getString(R.string.verify)
        tv_btn_submit.setTextColor(
            ContextCompat.getColor(applicationContext, android.R.color.white)
        )

        progress_bar_submit.visibility = View.GONE
        cl_btn_submit.setBackgroundColor(ContextCompat.getColor(this, R.color.colorYellow))
//        edit_text.isEnabled = true
    }
}
