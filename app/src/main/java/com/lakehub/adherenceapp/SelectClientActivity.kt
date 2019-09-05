package com.lakehub.adherenceapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.lakehub.adherenceapp.adapters.ClientAltAdapter
import com.lakehub.adherenceapp.data.Client
import kotlinx.android.synthetic.main.app_bar_chv_dashboard.*
import kotlinx.android.synthetic.main.content_select_client.*

class SelectClientActivity : AppCompatActivity() {
    private var clients = arrayListOf<Client>()
    private lateinit var myAdapter: ClientAltAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_client)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.select_client)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorRedDark)

        myAdapter = ClientAltAdapter(this, clients)
        val mLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)

        recycler_view.apply {
            layoutManager = mLayoutManager
            adapter = myAdapter
        }

        fetchData()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }

            else -> super.onOptionsItemSelected(item!!)
        }
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
