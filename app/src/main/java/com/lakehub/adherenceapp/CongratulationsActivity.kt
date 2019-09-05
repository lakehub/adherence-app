package com.lakehub.adherenceapp

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import com.google.firebase.firestore.FirebaseFirestore
import com.lakehub.adherenceapp.data.User
import kotlinx.android.synthetic.main.activity_congratulations.*

class CongratulationsActivity : AppCompatActivity() {
    private var inProgress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_congratulations)
        hideProgress()

        val accessKey = AppPreferences.accessKey
        val db = FirebaseFirestore.getInstance()
        val usersDoc = db.collection("users").document(accessKey!!)
        usersDoc.get()
            .addOnCompleteListener {
                if (it.isComplete) {
                    val user = it.result?.toObject(User::class.java)
                    val points = user?.points!!.toInt().plus(1)
                    usersDoc.update("points", points)
                    tv_points.text = getString(R.string.point, 1)
                    if (points > 1) {
                        tv_total_points.text = getString(R.string.points, points)
                    } else {
                        tv_total_points.text = getString(R.string.point, points)
                    }
                }
            }
            .addOnFailureListener {

            }

        btn_close.setOnClickListener {
            if (!inProgress) {
                finish()
            }
        }
    }

    private fun hideProgress() {
        inProgress = false
        progress.makeGone()
        cl_main.makeVisible()
    }

    private fun showProgress() {
        inProgress = true
        progress.makeVisible()
        cl_main.makeGone()
    }

    override fun onAttachedToWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            this.window.addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }
}
