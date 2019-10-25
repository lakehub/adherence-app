package com.lakehub.adherenceapp.activities.startup

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.TaskExecutors
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.lakehub.adherenceapp.R
import com.lakehub.adherenceapp.app.AppPreferences
import com.lakehub.adherenceapp.utils.showWarning
import kotlinx.android.synthetic.main.activity_auth.*
import java.util.concurrent.TimeUnit

class AuthActivity : AppCompatActivity() {
    var canResend = false
    val handler = Handler()
    private lateinit var alertDialog: AlertDialog
    private lateinit var alertDialogBuilder: AlertDialog.Builder
    private lateinit var clBtnSubmit: CardView
    private lateinit var editTextCode: TextInputEditText
    private lateinit var inputLayoutCode: TextInputLayout
    private lateinit var tvBtnSubmit: TextView
    private lateinit var progressBarSubmit: ProgressBar
    private var verificationId: String? = null
    private var credential: PhoneAuthCredential? = null
    private val auth = FirebaseAuth.getInstance()
    private lateinit var tvCount: TextView
    private lateinit var tvResend: TextView
    private var phoneNumber = ""
    private var handlerRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setCancelable(false)
        val inflater: LayoutInflater = this.layoutInflater
        val dialogView: View = inflater.inflate(R.layout.code_verification_dialog, null)
        alertDialogBuilder.setView(dialogView)
        alertDialog = alertDialogBuilder.create()

        inputLayoutCode = dialogView.findViewById(R.id.input_layout_code)
        clBtnSubmit = dialogView.findViewById(R.id.cl_btn_submit)
        editTextCode = dialogView.findViewById(R.id.edit_text_code)
        tvBtnSubmit = dialogView.findViewById(R.id.tv_btn_submit)
        tvCount = dialogView.findViewById(R.id.tv_count)
        tvResend = dialogView.findViewById(R.id.tv_resend)
        progressBarSubmit = dialogView.findViewById(R.id.progress_bar_submit)

        tvCount.text = getString(R.string.sec_count, 60)

        hideVerificationProgress()

        hideProgress()

        cl_btn_submit.setOnClickListener {
            phoneNumber = edit_text.text.toString().trim()

            if (phoneNumber.isNotEmpty()) {
                if (phoneNumber.length < 10 || phoneNumber.length > 13) {
                    showWarning(getString(R.string.invalid_phone))
                } else {
                    showProgress()
                    if (phoneNumber.length == 10) {
                        phoneNumber = "+254${phoneNumber.substring(1)}"
                    }
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        phoneNumber,
                        60,
                        TimeUnit.SECONDS,
                        TaskExecutors.MAIN_THREAD,
                        callback()
                    )
                }
            } else {
                showWarning(getString(R.string.phone_required))
            }
        }

        editTextCode.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val text = p0.toString().trim()

                if (text.isBlank()) {
                    inputLayoutCode.error = getString(R.string.required)
                } else {
                    inputLayoutCode.error = null
                }
            }

        })

        clBtnSubmit.setOnClickListener {
            inputLayoutCode.error = null
            val code = editTextCode.text.toString().trim()

            if (code.isNotEmpty()) {
                showVerificationProgress()
                credential = PhoneAuthProvider.getCredential(verificationId!!, code)
                signInWithPhoneAuthCredential(credential!!)
            } else {
                inputLayoutCode.error = getString(R.string.required)
                inputLayoutCode.requestFocus()
            }
        }

        tvCount.setOnClickListener {
            if (canResend) {
                canResend = false
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phoneNumber,
                    60,
                    TimeUnit.SECONDS,
                    TaskExecutors.MAIN_THREAD,
                    callback()
                )
                tvCount.text = getString(R.string.sec_count, 60)
            }
        }
    }

    private fun startCountdown() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                try {
                    val currentSec = tvCount.text.toString().split(" ")[0].toInt()

                    if (currentSec > 1) {
                        val sec = currentSec - 1
                        tvCount.text = getString(R.string.sec_count, sec)
                    } else {
                        tvCount.text = getString(R.string.resend)
                        tvResend.visibility = View.GONE
                        canResend = true
                    }
                } catch (e: Throwable) {
                    tvCount.text = getString(R.string.resend)
                    tvResend.visibility = View.GONE
                    canResend = true
                }

                handler.postDelayed(this, 1000)
            }
        }, 1000)
    }

    private fun stopCountDown() {
        handler.removeCallbacksAndMessages(null)
    }

    private fun callback(): PhoneAuthProvider.OnVerificationStateChangedCallbacks =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                hideProgress()
                val code: String? = phoneAuthCredential.smsCode
                signInWithPhoneAuthCredential(phoneAuthCredential)
                editTextCode.setText(code)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                hideProgress()
                if (e.localizedMessage != null)
                    showWarning(e.localizedMessage!!)

                Log.d("TAG", "message: ${e.message}")
            }

            override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(p0, p1)
                hideProgress()
                verificationId = p0
                Log.e("TAG", "verification id: $verificationId")
                stopCountDown()
                startCountdown()
                alertDialog.show()
            }

            override fun onCodeAutoRetrievalTimeOut(p0: String) {
                super.onCodeAutoRetrievalTimeOut(p0)
                Log.d("TAG", "code retrieval fail")
            }
        }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                hideVerificationProgress()
                if (task.isSuccessful) {
                    alertDialog.dismiss()
                    AppPreferences.authenticated = true
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                    Log.e("TAG", "authenticated")

                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w("TAG", "signInWithCredential:failure", task.exception)

                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Toast.makeText(this, getString(R.string.invalid_code), Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
    }

    private fun showProgress() {
        tv_btn_submit.text = getString(R.string.sending)
        tv_btn_submit.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
        progress_bar_submit.visibility = View.VISIBLE
        edit_text.isEnabled = false
        cl_btn_submit.setBackgroundColor(ContextCompat.getColor(this, R.color.materialColorGray))
    }

    private fun hideProgress() {
        tv_btn_submit.text = getString(R.string.send_me_code)
        tv_btn_submit.setTextColor(
            ContextCompat.getColor(applicationContext, android.R.color.white)
        )

        progress_bar_submit.visibility = View.GONE
        cl_btn_submit.setBackgroundColor(ContextCompat.getColor(this, R.color.colorGreen))
        edit_text.isEnabled = true
    }

    private fun showVerificationProgress() {
        tvBtnSubmit.text = getString(R.string.submitting)
        tvBtnSubmit.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
        progressBarSubmit.visibility = View.VISIBLE
        editTextCode.isEnabled = false
        clBtnSubmit.setCardBackgroundColor(ContextCompat.getColor(this, R.color.materialColorGray))
    }

    private fun hideVerificationProgress() {
        tv_btn_submit.text = getString(R.string.submit)
        tv_btn_submit.setTextColor(
            ContextCompat.getColor(applicationContext, android.R.color.white)
        )

        progressBarSubmit.visibility = View.GONE
        clBtnSubmit.setCardBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
        editTextCode.isEnabled = true
    }
}
