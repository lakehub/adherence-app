package com.lakehub.adherenceapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_deactivate_client.*

class DeactivateClientActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deactivate_client)

        progress_bar.makeGone()

        val accessKey = intent.extras?.getString("accessKey")

        tv_cancel.setOnClickListener {
            finish()
        }

        tv_ok.setOnClickListener {
            progress_bar.makeVisible()
            FirebaseFirestore.getInstance().collection("users")
                .document(accessKey!!)
                .update("active", false)
            showSuccess(getString(R.string.client_deactivate_success))
            finish()
        }
    }
}
