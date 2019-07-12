package com.lakehub.adherenceapp

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_clients.*
import kotlinx.android.synthetic.main.content_clients.*

class ClientsActivity : AppCompatActivity() {
    private var clients = arrayListOf<Client>()
    private lateinit var myAdapter: ClientAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clients)

        supportActionBar?.hide()
        supportActionBar?.setDisplayShowTitleEnabled(false)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorRedDark)


        add_fab.setColorFilter(Color.WHITE)

        val states = arrayOf(intArrayOf(android.R.attr.state_enabled), intArrayOf(android.R.attr.state_pressed))

        val colors = intArrayOf(
            ContextCompat.getColor(this, R.color.colorGreen),
            ContextCompat.getColor(this, R.color.colorPrimary)
        )
        val colorList = ColorStateList(states, colors)
        add_fab.backgroundTintList = colorList

        iv_back.setOnClickListener {
            finish()
        }

        add_fab.setOnClickListener {
            startActivity(Intent(this, AddClientActivity::class.java))
        }

        myAdapter = ClientAdapter(this, clients)
        val mLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)

        recycler_view.apply {
            layoutManager = mLayoutManager
            adapter = myAdapter
        }

        fetchData()
    }

    private fun fetchData() {
        showProgress()
        val phoneNo = FirebaseAuth.getInstance().currentUser?.phoneNumber

        val userRef = FirebaseFirestore.getInstance().collection("users")
        userRef.whereEqualTo("chvPhoneNumber", phoneNo!!)
            .get()
            .addOnCompleteListener {
                hideProgress()

            }
            .addOnFailureListener {
                hideProgress()
            }

        userRef.whereEqualTo("chvPhoneNumber", phoneNo)
            .addSnapshotListener { querySnapshot, _ ->
                hideProgress()
                if (querySnapshot != null && querySnapshot.documents.isNotEmpty()) {
                    showAlarms()
                    clients.clear()

                    for (document in querySnapshot.documents) {
                        val client = Client(
                            location = document.getString("location")!!,
                            name = document.getString("name")!!,
                            phoneNumber = document.getString("phoneNumber")!!
                        )
                        clients.add(client)
                    }

                    myAdapter.notifyDataSetChanged()

                } else {
                    hideAlarms()
                }
            }
    }

    private fun showProgress() {
        progress_bar.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        progress_bar.visibility = View.GONE
    }

    private fun showAlarms() {
        recycler_view.makeVisible()
        tv_no_clients.makeGone()
    }

    private fun hideAlarms() {
        recycler_view.makeGone()
        tv_no_clients.makeVisible()
    }
}
