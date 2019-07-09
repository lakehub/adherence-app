package com.lakehub.adherenceapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.TaskExecutors
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_verify.*
import java.util.concurrent.TimeUnit

class VerifyActivity : AppCompatActivity() {
    private var verificationId: String? = null
    private var phoneNumber: String? = null
    private val auth = FirebaseAuth.getInstance()
    private var newUser: Boolean? = null
    private lateinit var userRef: CollectionReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify)

        supportActionBar?.hide()

        hideProgress()

        val db = FirebaseFirestore.getInstance()
        userRef = db.collection("users")

        val extra = intent.extras
        phoneNumber = extra?.getString("phoneNumber")
        newUser = extra?.getBoolean("newUser")

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
                /*val toast = Toast(applicationContext)
                val view: View = layoutInflater.inflate(R.layout.warning, null)
                val textView: TextView = view.findViewById(R.id.message)
                textView.text = getString(R.string.invalid_code)
                toast.view = view
                toast.setGravity(Gravity.BOTTOM, 30, 30)
                toast.duration = Toast.LENGTH_LONG
                toast.show()*/
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
                    edit_text.setText(code)
                    if (verificationId != null) {
                        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
                        signInWithPhoneAuthCredential(credential)
                    }
                }
            }

            override fun onVerificationFailed(e: FirebaseException?) {
                hideProgress()

                Log.d("TAG", "message: ${e?.message}")
            }

            override fun onCodeSent(myMerificationId: String?, p1: PhoneAuthProvider.ForceResendingToken?) {
                super.onCodeSent(myMerificationId, p1)
                verificationId = myMerificationId
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
                    if (newUser != null && newUser == true) {
                        Toast.makeText(this, "registered as new user", Toast.LENGTH_LONG).show()
                        val myUser = hashMapOf(
                            "phoneNumber" to user!!.phoneNumber,
                            "category" to 0
                        )
                        userRef.document(phoneNumber!!)
                            .set(myUser)
                            .addOnSuccessListener {
                                Toast.makeText(this, "user added", Toast.LENGTH_LONG).show()

                                startActivity(Intent(this, SelectAccountTypeActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "cannot add user: $it", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        val db = FirebaseFirestore.getInstance()
                        val usersRef = db.collection("users")

                        usersRef.document(phoneNumber!!)
                        usersRef.whereEqualTo("phoneNumber", phoneNumber)
                            .get()
                            .addOnCompleteListener {
                                hideProgress()
                                val category = it.result!!.documents[0].get("category").toString().toInt()
                                if (category == 1) {
                                    AppPreferences.accountType = 1
                                    startActivity(Intent(this@VerifyActivity, ClientHomeActivity::class.java))
                                } else {
                                    AppPreferences.accountType = 2
                                    startActivity(Intent(this@VerifyActivity, ChvDashboardActivity::class.java))
                                }
                                finish()

                            }
                            .addOnFailureListener {
                                hideProgress()
                                Log.d("TAG", "exception: $it")
                            }
                    }
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
