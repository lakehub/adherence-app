package com.lakehub.adherenceapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.TaskExecutors
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.activity_verify.*
import java.lang.NumberFormatException
import java.util.concurrent.TimeUnit

class VerifyActivity : AppCompatActivity() {
    private var verificationId: String? = null
    private var phoneNumber: String? = null
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify)

        supportActionBar?.hide()

        hideProgress()

        val extra = intent.extras
        phoneNumber = extra?.getString("phoneNumber")

        retrieveCode()

        tv_count.text = getString(R.string.sec_count, 60)

        var canResend = false

        val handler = Handler()

        handler.postDelayed(object : Runnable {
            override fun run() {
                try {
                    val currentSec = tv_count.text.toString().split(" ")[0].toInt()

                    if (currentSec > 1) {
                        val sec = currentSec - 1
                        tv_count.text = getString(R.string.sec_count, sec)
                    } else {
                        tv_count.text = getString(R.string.resend)
                        tv_resend.visibility = View.GONE
                        canResend = true
                    }
                } catch (e: Throwable) {
                    tv_count.text = getString(R.string.resend)
                    tv_resend.visibility = View.GONE
                    canResend = true
                }

                handler.postDelayed(this, 1000)
            }
        }, 1000)

        cl_btn_submit.setOnClickListener {
            val code: String? = edit_text.text.toString().trim()
            Log.d("TAG", "verification id")

            if (verificationId != null) {
                if (code != null && verificationId != null) {
                    if (code.isNotEmpty() && code.isNotBlank() && code != "") {
                        showProgress()
                        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
                        signInWithPhoneAuthCredential(credential!!)
                    } else {
                        hideProgress()
                        input_layout.requestFocus()
                        val toast = Toast(this)
                        val view: View = layoutInflater.inflate(R.layout.warning, null)
                        val textView: TextView = view.findViewById(R.id.message)
                        textView.text = getString(R.string.enter_code)
                        toast.view = view
                        toast.setGravity(Gravity.BOTTOM, 30, 30)
                        toast.duration = Toast.LENGTH_SHORT
                        toast.show()
                        input_layout.requestFocus()
                    }
                } else {
                    input_layout.requestFocus()
                    val toast = Toast(this)
                    val view: View = layoutInflater.inflate(R.layout.warning, null)
                    val textView: TextView = view.findViewById(R.id.message)
                    textView.text = getString(R.string.enter_code)
                    toast.view = view
                    toast.setGravity(Gravity.BOTTOM, 30, 30)
                    toast.duration = Toast.LENGTH_SHORT
                    toast.show()
                    input_layout.requestFocus()
                }
            } else {
                val toast = Toast(applicationContext)
                val view: View = layoutInflater.inflate(R.layout.warning, null)
                val textView: TextView = view.findViewById(R.id.message)
                textView.text = getString(R.string.invalid_code)
                toast.view = view
                toast.setGravity(Gravity.BOTTOM, 30, 30)
                toast.duration = Toast.LENGTH_LONG
                toast.show()
            }
        }

        tv_count.setOnClickListener {
            if (canResend) {
                canResend = false
                retrieveCode()
                tv_count.text = getString(R.string.sec_count, 60)
            }
        }
    }

    private fun callback(): PhoneAuthProvider.OnVerificationStateChangedCallbacks =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(p0: PhoneAuthCredential?) {
                hideProgress()
                val code: String? = p0?.smsCode
                if (code != null) {
                    showProgress()
                    PhoneAuthProvider.getCredential(verificationId!!, code)
                    edit_text.setText(code)
                }
            }

            override fun onVerificationFailed(e: FirebaseException?) {
                hideProgress()

                Log.d("TAG", "message: ${e?.message}")
            }

            override fun onCodeSent(myMerificationId: String?, p1: PhoneAuthProvider.ForceResendingToken?) {
                super.onCodeSent(myMerificationId, p1)
                verificationId = myMerificationId
                Log.d("TAG", "ver id: $verificationId")
            }

            override fun onCodeAutoRetrievalTimeOut(p0: String?) {
                super.onCodeAutoRetrievalTimeOut(p0)
                Log.d("TAG", "retrieve code fail")
            }
        }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                hideProgress()
                if (task.isSuccessful) {
                    val user = task.result?.user
                    /*val myIntent = Intent(this, HomeActivity::class.java)
                    myIntent.putExtra("phoneNumber", user?.phoneNumber)
                    startActivity(myIntent)
                    finish()*/
                    Toast.makeText(this, "signed in", Toast.LENGTH_LONG).show()
                } else {
                    Log.w("TAG", "signInWithCredential:failure", task.exception)

                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        val toast = Toast(applicationContext)
                        val view: View = layoutInflater.inflate(R.layout.warning, null)
                        val textView: TextView = view.findViewById(R.id.message)
                        textView.text = getString(R.string.invalid_code)
                        toast.view = view
                        toast.setGravity(Gravity.BOTTOM, 30, 30)
                        toast.duration = Toast.LENGTH_LONG
                        toast.show()
                    }
                }
            }
    }

    private fun showProgress() {
        tv_btn_submit.text = getString(R.string.submitting)
        tv_btn_submit.setTextColor(ContextCompat.getColor(applicationContext, android.R.color.holo_green_light))
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
        cl_btn_submit.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_light))
//        edit_text.isEnabled = true
    }

    private fun retrieveCode() {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber!!,
            60,
            TimeUnit.SECONDS,
            TaskExecutors.MAIN_THREAD,
            callback()
        )
    }
}
