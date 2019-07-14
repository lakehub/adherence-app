package com.lakehub.adherenceapp

import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_edit_client.*
import kotlinx.android.synthetic.main.content_edit_client.*

class EditClientActivity : AppCompatActivity() {
    private var inProgress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_client)

        supportActionBar?.hide()
        supportActionBar?.setDisplayShowTitleEnabled(false)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorRedDark)
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

        val phoneNo = intent.getStringExtra("phoneNo")
        val location = intent.getStringExtra("location")
        val name = intent.getStringExtra("name")

        et_phone_no.setText(getString(R.string.phone_no, phoneNo?.substring(4)))
        et_location.setText(titleCase(location!!))
        et_name.setText(titleCase(name!!))

        iv_cancel.setOnClickListener {
            finish()
        }

        fab.setOnClickListener {
            val clientName = et_name.text.toString()
            val clientLocation = et_location.text.toString()
            if (clientName.isNotEmpty() && clientLocation.isNotEmpty()) {
                showProgress()
                val db = FirebaseFirestore.getInstance()
                val userRef = db.collection("users").document(phoneNo!!)
                val data = mapOf(
                    "location" to clientLocation,
                    "name" to clientName
                )

                userRef.update(data)
                    .addOnCompleteListener {
                        if (it.isComplete) {
                            hideProgress()
                            val toast = Toast(this)
                            val view: View = layoutInflater.inflate(R.layout.normal_toast, null)
                            val textView: TextView = view.findViewById(R.id.message)
                            textView.text = getString(R.string.client_edit_success)
                            toast.view = view
                            toast.setGravity(Gravity.BOTTOM, 30, 30)
                            toast.duration = Toast.LENGTH_SHORT
                            toast.show()
                            finish()
                        }
                    }

            } else {
                val toast = Toast(this)
                val view: View = layoutInflater.inflate(R.layout.warning, null)
                val textView: TextView = view.findViewById(R.id.message)
                textView.text = getString(R.string.fill_fields)
                toast.view = view
                toast.setGravity(Gravity.BOTTOM, 30, 30)
                toast.duration = Toast.LENGTH_SHORT
                toast.show()
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
