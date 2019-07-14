package com.lakehub.adherenceapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_deactivate_client.*

class DeactivateClientActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deactivate_client)

        progress_bar.makeGone()

        val phoneNo = intent.extras?.getString("phoneNo")

        tv_cancel.setOnClickListener {
            finish()
        }

        tv_ok.setOnClickListener {
            progress_bar.makeVisible()
            FirebaseFirestore.getInstance().collection("users")
                .document(phoneNo!!)
                .update("active", false)
                .addOnCompleteListener {
                    if (it.isComplete) {
                        val toast = Toast(MainApplication.applicationContext())
                        val view: View = View.inflate(
                            MainApplication.applicationContext(),
                            R.layout.normal_toast, null
                        )
                        val textView: TextView = view.findViewById(R.id.message)
                        textView.text = this.getString(R.string.client_deactivate_success)
                        toast.view = view
                        toast.setGravity(Gravity.BOTTOM, 30, 30)
                        toast.duration = Toast.LENGTH_LONG
                        toast.show()
                        finish()
                    }
                }
        }
    }
}
