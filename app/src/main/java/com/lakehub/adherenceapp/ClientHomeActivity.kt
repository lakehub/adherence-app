package com.lakehub.adherenceapp

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_client_home.*
import kotlinx.android.synthetic.main.app_bar_client_home.*
import kotlinx.android.synthetic.main.content_client_home.*
import kotlinx.android.synthetic.main.menu.*
import org.joda.time.DateTime

class ClientHomeActivity : AppCompatActivity() {
    private lateinit var alarmList: ArrayList<Alarm>
    private lateinit var myAdapter: AlarmAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_home)

        setSupportActionBar(toolbar)

        add_fab.setColorFilter(Color.WHITE)
        val states = arrayOf(intArrayOf(android.R.attr.state_enabled), intArrayOf(android.R.attr.state_pressed))

        val auth = FirebaseAuth.getInstance()

        val colors = intArrayOf(
            ContextCompat.getColor(this, R.color.colorGreen),
            ContextCompat.getColor(this, R.color.colorPrimary)
        )
        val colorList = ColorStateList(states, colors)
        add_fab.backgroundTintList = colorList

        toolbar.setCollapsible(true)

        iv_menu.setOnClickListener {
            if (!drawer_layout.isDrawerOpen(GravityCompat.START)) {
                drawer_layout.openDrawer(GravityCompat.START, true)
            }
        }

        iv_close.setOnClickListener {
            drawer_layout.closeDrawer(GravityCompat.START, true)
        }

        add_fab.setOnClickListener {
            startActivityForResult(Intent(this, AddAlarmActivity::class.java), 101)
        }

        alarmList = arrayListOf()
        myAdapter = AlarmAdapter(this, alarmList)
        val mLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        val dividerItemDecoration = androidx.recyclerview.widget
            .DividerItemDecoration(
                this,
                mLayoutManager.orientation
            )

        recycler_view.apply {
            layoutManager = mLayoutManager
            addItemDecoration(dividerItemDecoration)
            adapter = myAdapter
        }

        fetchData()

        cl_logout.setOnClickListener {
            auth.signOut()
        }

        auth.addAuthStateListener {
            val user = it.currentUser
            if (user == null) {
                this.finishAffinity()
            }
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 900 && resultCode == Activity.RESULT_OK) {
            val success = data?.getBooleanExtra("success", false)
//            if (success != null && success)
//                fetchData()
        }
    }

    private fun fetchData() {
        showProgress()
        val phoneNumber = FirebaseAuth.getInstance().currentUser?.phoneNumber

        val alarmsRef = FirebaseFirestore.getInstance().collection("alarms")
        alarmsRef.whereEqualTo("phoneNumber", phoneNumber!!)
            .get()
            .addOnCompleteListener {
                hideProgress()
                val documents = it.result?.documents
                alarmList.clear()
                for (document in documents!!) {
                    val alarm = Alarm(
                        description = document.getString("description")!!,
                        fromDate = document.getString("fromDate")!!
                    )

                    alarmList.add(alarm)
                }

                val sorted = alarmList.sortedWith(Comparator { o1, o2 ->
                    when {
                        dateMillis(o1.fromDate) < dateMillis(o2.fromDate) -> 1
                        dateMillis(o1.fromDate) > dateMillis(o2.fromDate) -> -1
                        else -> 0
                    }
                })

                alarmList.clear()
                alarmList.addAll(sorted)

                myAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                hideProgress()
            }
    }

    private fun showProgress() {
        progress_bar.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        progress_bar.visibility = View.GONE
    }
}
