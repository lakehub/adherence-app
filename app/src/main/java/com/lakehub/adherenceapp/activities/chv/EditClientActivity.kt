package com.lakehub.adherenceapp.activities.chv

import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.lakehub.adherenceapp.R
import com.lakehub.adherenceapp.utils.showSuccess
import com.lakehub.adherenceapp.utils.showWarning
import com.lakehub.adherenceapp.utils.titleCase
import kotlinx.android.synthetic.main.activity_edit_client.*
import kotlinx.android.synthetic.main.content_edit_client.*

class EditClientActivity : AppCompatActivity() {
    private var inProgress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_client)

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

        val userId = intent.getStringExtra("userId")
        val location = intent.getStringExtra("location")

//        et_phone_no.setText(getString(R.string.phone_no, accessKey?.substring(4)))
        et_phone_no.setText(userId)
        et_location.setText(titleCase(location!!))

        iv_cancel.setOnClickListener {
            finish()
        }

        fab.setOnClickListener {
            val clientLocation = et_location.text.toString()
            if (clientLocation.isNotEmpty()) {
                val db = FirebaseFirestore.getInstance()
                val userRef = db.collection("users").document(userId!!)
                val data = mapOf(
                    "location" to clientLocation
                )

                userRef.update(data)
                showSuccess(getString(R.string.client_edit_success))
                finish()
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
