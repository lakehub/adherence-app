package com.lakehub.adherenceapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        supportActionBar?.hide()


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


                if (phoneNumber.length < 10 && phoneNumber.length > 13) {
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
                    val myIntent = Intent(this@RegisterActivity, VerifyActivity::class.java)
                    myIntent.putExtra("phoneNumber", phoneNumber)
                    startActivity(myIntent)
                    finish()
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

    }
}
