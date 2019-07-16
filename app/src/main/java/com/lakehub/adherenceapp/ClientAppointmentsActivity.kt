package com.lakehub.adherenceapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.lakehub.adherenceapp.adapters.AppointmentsAdapter
import com.lakehub.adherenceapp.data.ChvReminder
import kotlinx.android.synthetic.main.activity_client_appointments.*
import kotlinx.android.synthetic.main.activity_clients.iv_back
import kotlinx.android.synthetic.main.content_client_appointments.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.util.*
import kotlin.Comparator

class ClientAppointmentsActivity : AppCompatActivity() {
    private val appointments = arrayListOf<ChvReminder>()
    private lateinit var myAdapter: AppointmentsAdapter
    private lateinit var db: FirebaseFirestore
    private var clientPhoneNo: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_appointments)

        supportActionBar?.hide()
        supportActionBar?.setDisplayShowTitleEnabled(false)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorRedDark)

        clientPhoneNo = intent.extras?.getString("clientPhoneNo")
        val clientName = intent.extras?.getString("clientName")
        db = FirebaseFirestore.getInstance()

        myAdapter = AppointmentsAdapter(this, appointments)
        val mLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)

        recycler_view.apply {
            adapter = myAdapter
            layoutManager = mLayoutManager
        }

        recycler_view.makeGone()
        tv_no_appointments.makeGone()

        iv_back.setOnClickListener {
            finish()
        }

        tv_client_name.text = titleCase(clientName!!)
        fetchAppointments()
    }

    private fun fetchAppointments() {
        showProgress()
        val offset = TimeZone.getDefault().rawOffset
        val tz = DateTimeZone.forOffsetMillis(offset)
        val millis = DateTime.now(tz).millis
        val phoneNumber = AppPreferences.phoneNo

        val alarmsRef = db.collection("chv_reminders")
            .whereEqualTo("isAppointment", true)
            .whereEqualTo("clientPhoneNo", clientPhoneNo)
            .whereEqualTo("phoneNumber", phoneNumber)
            .whereEqualTo("cancelled", false)
            .whereEqualTo("rang", false)
            .whereEqualTo("missed", false)
            .whereGreaterThanOrEqualTo("millis", millis)
            .orderBy("millis", Query.Direction.ASCENDING)

        alarmsRef.get()
            .addOnCompleteListener {
                if (it.isComplete) {
                    hideProgress()
                    if (!it.result?.isEmpty!!) {
                        if (it.result!!.documents.isNotEmpty()) {
                            for (document in it.result!!.documents) {
                                val alarm = ChvReminder(
                                    description = document.getString("description")!!,
                                    fromDate = document.getString("dateTime")!!,
                                    docId = document.id,
                                    alarmTone = document.getString("alarmTonePath"),
                                    isDrug = document.getBoolean("isDrug"),
                                    isAppointment = document.getBoolean("isAppointment"),
                                    cancelled = document.getBoolean("cancelled")!!,
                                    medType = document.getDouble("medicationType")?.toInt(),
                                    repeatMode = document.get("repeatMode") as ArrayList<Int>,
                                    id = document.getLong("id")?.toInt()!!,
                                    snoozed = document.getLong("snoozed")?.toInt()!!,
                                    rang = document.getBoolean("rang")!!,
                                    missed = document.getBoolean("missed")!!,
                                    clientPhoneNo = document.getString("clientPhoneNo"),
                                    clientName = document.getString("clientName")
                                )
                                appointments.add(alarm)

                            }

                            val sorted = appointments.sortedWith(Comparator { o1, o2 ->
                                when {
                                    dateMillis(o1.fromDate) > dateMillis(o2.fromDate) -> 1
                                    dateMillis(o1.fromDate) < dateMillis(o2.fromDate) -> -1
                                    else -> 0
                                }
                            })

                            appointments.clear()
                            appointments.addAll(sorted)

                            myAdapter.notifyDataSetChanged()

                            if (appointments.isEmpty()) {
                                hideAlarms()
                            } else {
                                showAlarms()
                            }
                        } else {
                            hideAlarms()
                        }
                    } else {
                        hideAlarms()
                    }
                }
            }
    }

    private fun hideAlarms() {
        recycler_view.makeGone()
        tv_no_appointments.makeVisible()
    }

    private fun showAlarms() {
        recycler_view.makeVisible()
        tv_no_appointments.makeGone()
    }

    private fun showProgress() {
        progress_bar.makeVisible()
    }

    private fun hideProgress() {
        progress_bar.makeGone()
    }
}
