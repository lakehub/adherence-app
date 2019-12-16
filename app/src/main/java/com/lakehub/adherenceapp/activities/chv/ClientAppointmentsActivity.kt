package com.lakehub.adherenceapp.activities.chv

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.lakehub.adherenceapp.*
import com.lakehub.adherenceapp.adapters.AppointmentsAdapter
import com.lakehub.adherenceapp.app.AppPreferences
import com.lakehub.adherenceapp.data.ChvReminder
import com.lakehub.adherenceapp.repositories.UserRepository
import com.lakehub.adherenceapp.utils.dateMillis
import com.lakehub.adherenceapp.utils.makeGone
import com.lakehub.adherenceapp.utils.makeVisible
import com.lakehub.adherenceapp.utils.titleCase
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
    private var clientAccessKey: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_appointments)

        supportActionBar?.hide()
        supportActionBar?.setDisplayShowTitleEnabled(false)
        window.statusBarColor = ContextCompat.getColor(this,
            R.color.colorRedDark
        )

        clientAccessKey = intent.extras?.getString("clientAccessKey")
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

        tv_client_name.text = titleCase(clientAccessKey!!)
        fetchAppointments()
    }

    private fun fetchAppointments() {
        showProgress()
        val offset = TimeZone.getDefault().rawOffset
        val tz = DateTimeZone.forOffsetMillis(offset)
        val millis = DateTime.now(tz).millis
        val accessKey = UserRepository().userId

        val alarmsRef = db.collection("chv_reminders")
            .whereEqualTo("appointment", true)
            .whereEqualTo("clientAccessKey", clientAccessKey)
            .whereEqualTo("accessKey", accessKey)
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
                                val alarm = document.toObject(ChvReminder::class.java)
                                appointments.add(alarm!!)

                            }

                            val sorted = appointments.sortedWith(Comparator { o1, o2 ->
                                when {
                                    dateMillis(o1.dateTime) > dateMillis(
                                        o2.dateTime
                                    ) -> 1
                                    dateMillis(o1.dateTime) < dateMillis(
                                        o2.dateTime
                                    ) -> -1
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
