package com.lakehub.adherenceapp.activities.startup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.lakehub.adherenceapp.R
import com.lakehub.adherenceapp.activities.chv.ChvDashboardActivity
import com.lakehub.adherenceapp.activities.client.ClientHomeActivity
import com.lakehub.adherenceapp.app.AppPreferences
import com.lakehub.adherenceapp.data.Role
import kotlinx.android.synthetic.main.activity_select_account_type.*

class SelectAccountTypeActivity : AppCompatActivity() {

    private var phoneNumber: String? = null
    private lateinit var userRef: CollectionReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_account_type)

        val db = FirebaseFirestore.getInstance()
        userRef = db.collection("users")

        supportActionBar?.hide()

        hideProgress()

        FirebaseAuth.getInstance().addAuthStateListener {
            phoneNumber = it.currentUser?.phoneNumber
        }

        cl_client.setOnClickListener {
            showProgress()
            userRef.document(phoneNumber!!)
                .update("category", 1)
                .addOnSuccessListener {
                    hideProgress()
                    AppPreferences.role = Role.CLIENT
                    startActivity(Intent(this@SelectAccountTypeActivity, ClientHomeActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    hideProgress()
                    val toast = Toast(this)
                    val view: View = layoutInflater.inflate(R.layout.network_error, null)
                    toast.view = view
                    toast.setGravity(Gravity.BOTTOM, 30, 30)
                    toast.duration = Toast.LENGTH_SHORT
                    toast.show()
                }
        }

        cl_chv.setOnClickListener {
            showProgress()
            userRef.document(phoneNumber!!)
                .update("category", 2)
                .addOnSuccessListener {
                    hideProgress()
                    AppPreferences.role = Role.CHV
                    startActivity(Intent(this@SelectAccountTypeActivity, ChvDashboardActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    hideProgress()
                    val toast = Toast(this)
                    val view: View = layoutInflater.inflate(R.layout.network_error, null)
                    toast.view = view
                    toast.setGravity(Gravity.BOTTOM, 30, 30)
                    toast.duration = Toast.LENGTH_SHORT
                    toast.show()
                }
        }
    }

    private fun hideProgress() {
        progress_bar.visibility = View.GONE
    }

    private fun showProgress() {
        progress_bar.visibility = View.VISIBLE
    }
}
