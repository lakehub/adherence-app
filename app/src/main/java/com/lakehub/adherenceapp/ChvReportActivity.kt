package com.lakehub.adherenceapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_chv_report.*
import kotlinx.android.synthetic.main.content_chv_report.*
import kotlinx.android.synthetic.main.month_tab.view.*
import lecho.lib.hellocharts.model.PieChartData
import lecho.lib.hellocharts.model.SliceValue
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import java.util.*
import kotlin.math.roundToInt


class ChvReportActivity : AppCompatActivity() {
    private var year: Int? = null
    private var month: Int? = null
    private var selectedDateStr: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chv_report)
        val months = arrayListOf<String>()
        months.add(getString(R.string.jan))
        months.add(getString(R.string.feb))
        months.add(getString(R.string.mar))
        months.add(getString(R.string.apr))
        months.add(getString(R.string.may))
        months.add(getString(R.string.jun))
        months.add(getString(R.string.jul))
        months.add(getString(R.string.aug))
        months.add(getString(R.string.sep))
        months.add(getString(R.string.oct))
        months.add(getString(R.string.nov))
        months.add(getString(R.string.dec))

        supportActionBar?.setDisplayShowTitleEnabled(false)

        pie_chart.makeGone()
        cl_details.makeGone()
        tv_no_report.makeGone()
        hideProgress()

        val offset = TimeZone.getDefault().rawOffset
        val tz = DateTimeZone.forOffsetMillis(offset)
        val date = DateTime.now(tz)
        year = date.year
        month = date.monthOfYear

        val reportDateFormat = "yyyy-MM"
        val reportDateFormatter = DateTimeFormat.forPattern(reportDateFormat)
        selectedDateStr = reportDateFormatter.print(date)

        for (i in 0 until month!!) {
            tabs.addTab(tabs.newTab())
        }

        for (i in 0 until tabs.tabCount) {
            val tab = tabs.getTabAt(i)
            val view = layoutInflater.inflate(R.layout.month_tab, null)
            view.tv_month.text = months[i]
            tab?.customView = view
        }


        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                val view = tab!!.customView
                view!!.tv_month.text = months[tab.position]
                view.tv_month?.setTextColor(ContextCompat.getColor(applicationContext, android.R.color.white))
                view.tv_month.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.colorRedLight))
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                val view = tab!!.customView
                val position = tab.position
                view!!.tv_month.text = months[position]
                view.tv_month?.setTextColor(ContextCompat.getColor(applicationContext, android.R.color.black))
                view.tv_month.setBackgroundColor(ContextCompat.getColor(applicationContext, android.R.color.white))
                selectedDateStr = if (position > 8) {
                    "$year-${position.plus(1)}"
                } else {
                    "$year-0${position.plus(1)}"
                }
                fetchData()
            }

        })

        Handler().postDelayed(
            {
                tabs.getTabAt(month!!.minus(1))!!.select()
            }, 100
        )

        iv_back.setOnClickListener {
            finish()
        }

        pie_chart.isChartRotationEnabled = false
        pie_chart.circleFillRatio = 0.9f
    }

    private fun showReport() {
        pie_chart.makeVisible()
        cl_details.makeVisible()
        tv_no_report.makeGone()
    }

    private fun hideReport() {
        pie_chart.makeGone()
        cl_details.makeGone()
        tv_no_report.makeVisible()
    }

    @SuppressLint("SetTextI18n")
    private fun fetchData() {
        showProgress()
        var clients = 0

        val docRef = FirebaseFirestore.getInstance().collection("reports")
        docRef.whereEqualTo("chvPhoneNo", AppPreferences.phoneNo)
            .whereEqualTo("date", selectedDateStr)
            .addSnapshotListener { querySnapshot, _ ->
                if (querySnapshot!!.documents.isNotEmpty()) {
                    val doc = querySnapshot.documents[0]
                    val taken = doc.getLong("taken")
                    val snoozed = doc.getLong("snoozed")
                    val missed = doc.getLong("missed")

                    tv_taken_count.text = taken.toString()
                    tv_snooze_count.text = snoozed.toString()
                    tv_missed_count.text = missed.toString()

                    val total = taken!!.plus(snoozed!!).plus(missed!!)
                    tv_taken_percent.text = "${taken.toFloat().div(total).times(100).roundToInt()}%"
                    tv_snooze_percent.text = "${snoozed.toFloat().div(total).times(100).roundToInt()}%"
                    tv_missed_percent.text = "${missed.toFloat().div(total).times(100).roundToInt()}%"

                    FirebaseFirestore.getInstance().collection("users").document(AppPreferences.phoneNo!!)
                        .get()
                        .addOnCompleteListener {
                            if (it.isComplete) {
                                hideProgress()
                                showReport()
                                clients = it.result!!.getLong("clients")?.toInt()!!
                                val dataSet = arrayListOf<SliceValue>()
                                dataSet.add(
                                    SliceValue(
                                        missed.toFloat(),
                                        ContextCompat.getColor(this, R.color.colorRedLight)
                                    )
                                )
                                dataSet.add(
                                    SliceValue(
                                        snoozed.toFloat(),
                                        ContextCompat.getColor(this, R.color.colorGreen)
                                    )
                                )
                                dataSet.add(
                                    SliceValue(
                                        taken.toFloat(),
                                        ContextCompat.getColor(this, R.color.colorPrimary)
                                    )
                                )
                                val pieChatData = PieChartData(dataSet)
                                pieChatData.setHasCenterCircle(true)
                                    .setCenterText1(clients.toString())
                                    .setCenterText2(getString(R.string.clients).toUpperCase())
                                    .setCenterText1FontSize(60)
                                    .setCenterText2FontSize(15)
                                    .setCenterText1Color(ContextCompat.getColor(this, android.R.color.white))
                                    .setCenterText2Color(
                                        ContextCompat.getColor(
                                            this,
                                            R.color.colorWhiteSemitransparent
                                        )
                                    )
                                    .setSlicesSpacing(0)
                                    .setCenterCircleScale(0.93f)

                                pie_chart.pieChartData = pieChatData
                            }
                        }

                } else {
                    hideProgress()
                    hideReport()
                }
            }
    }

    private fun showProgress() {
        progress_bar.makeVisible()
    }

    private fun hideProgress() {
        progress_bar.makeGone()
    }

}
