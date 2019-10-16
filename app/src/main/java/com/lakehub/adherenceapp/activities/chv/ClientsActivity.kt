package com.lakehub.adherenceapp.activities.chv

import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.lakehub.adherenceapp.app.AppPreferences
import com.lakehub.adherenceapp.R
import com.lakehub.adherenceapp.adapters.ClientAdapter
import com.lakehub.adherenceapp.data.Client
import com.lakehub.adherenceapp.utils.makeGone
import com.lakehub.adherenceapp.utils.makeVisible
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
        window.statusBarColor = ContextCompat.getColor(this,
            R.color.colorRedDark
        )


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
//            startActivity(Intent(this, AddClientActivity::class.java))
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
        val phoneNo = AppPreferences.accessKey

        val userRef = FirebaseFirestore.getInstance().collection("users")
        userRef.whereEqualTo("chvAccessKey", phoneNo!!)
            .get()
            .addOnCompleteListener {
                hideProgress()

            }
            .addOnFailureListener {
                hideProgress()
            }

        userRef.whereEqualTo("chvAccessKey", phoneNo)
            .whereEqualTo("active", true)
            .addSnapshotListener { querySnapshot, _ ->
                hideProgress()
                if (querySnapshot != null && querySnapshot.documents.isNotEmpty()) {
                    showClients()
                    clients.clear()

                    for (document in querySnapshot.documents) {
                        val client = document.toObject(Client::class.java)
                        clients.add(client!!)
                    }

                    myAdapter.notifyDataSetChanged()

                } else {
                    hideClients()
                }
            }
    }

    private fun showProgress() {
        progress_bar.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        progress_bar.visibility = View.GONE
    }

    private fun showClients() {
        recycler_view.makeVisible()
        tv_no_clients.makeGone()
    }

    private fun hideClients() {
        recycler_view.makeGone()
        tv_no_clients.makeVisible()
    }
}
