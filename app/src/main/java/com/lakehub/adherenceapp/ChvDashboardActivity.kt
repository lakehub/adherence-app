package com.lakehub.adherenceapp

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.model.InDateStyle
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.kizitonwose.calendarview.utils.yearMonth
import kotlinx.android.synthetic.main.activity_chv_dashboard.*
import kotlinx.android.synthetic.main.app_bar_chv_dashboard.*
import kotlinx.android.synthetic.main.app_bar_chv_dashboard.view.*
import kotlinx.android.synthetic.main.calendar_day.view.*
import kotlinx.android.synthetic.main.chv_menu.*
import kotlinx.android.synthetic.main.content_chv_dashboard.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

class ChvDashboardActivity : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()
    private val today = LocalDate.now()
    private val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM")
    private var selectedDate: LocalDate? = null
    private var selectedDateStr: String? = null
    private var currentYearMonth: YearMonth? = null
    private lateinit var alarmList: ArrayList<ChvReminder>
    private lateinit var missedAlarmList: ArrayList<ChvReminder>
    private lateinit var myAdapter: ChvReminderAdapter
    private lateinit var missedAlarmAdapter: ChvMissedReminderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chv_dashboard)

        setSupportActionBar(toolbar)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorRedDark)

        add_fab.setColorFilter(Color.WHITE)
        val states = arrayOf(intArrayOf(android.R.attr.state_enabled), intArrayOf(android.R.attr.state_pressed))

        val colors = intArrayOf(
            ContextCompat.getColor(this, R.color.colorGreen),
            ContextCompat.getColor(this, R.color.colorPrimary)
        )
        val colorList = ColorStateList(states, colors)
        add_fab.backgroundTintList = colorList

        iv_menu.setOnClickListener {
            if (!drawer_layout.isDrawerOpen(GravityCompat.START)) {
                drawer_layout.openDrawer(GravityCompat.START, true)
            }
        }

        iv_close.setOnClickListener {
            drawer_layout.closeDrawer(GravityCompat.START, true)
        }

        cl_home_menu.setOnClickListener {
            showHome()
            drawer_layout.closeDrawer(GravityCompat.START)
        }

        cl_upcoming_menu.setOnClickListener {
            showUpcoming()
            drawer_layout.closeDrawer(GravityCompat.START)
        }

        cl_missed_menu.setOnClickListener {
            showMissed()
            drawer_layout.closeDrawer(GravityCompat.START)
        }

        add_fab.setOnClickListener {
            startActivityForResult(Intent(this, AddChvReminderActivity::class.java), 900)
        }

        cl_settings_menu.setOnClickListener {
            drawer_layout.closeDrawer(GravityCompat.START, true)
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        cl_logout_menu.setOnClickListener {
            drawer_layout.closeDrawer(GravityCompat.START)
//            auth.signOut()
//            FirebaseFirestore.getInstance().clearPersistence()
            AppPreferences.loggedIn = false
            AppPreferences.phoneNo = null
            AppPreferences.accountType = 0
            finish()
        }

        cl_clients_menu.setOnClickListener {
            drawer_layout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, ClientsActivity::class.java))
        }

        /*auth.addAuthStateListener {
            val user = it.currentUser
            if (user == null) {
//                startActivity(Intent(this, LoginActivity::class.java))
                this.finish()
            }
        }*/

        alarmList = arrayListOf()
        missedAlarmList = arrayListOf()
        myAdapter = ChvReminderAdapter(this, alarmList)
        missedAlarmAdapter = ChvMissedReminderAdapter(this, missedAlarmList)
        val mLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)

        val missedLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)

        recycler_view_upcoming.apply {
            layoutManager = mLayoutManager
            adapter = myAdapter
        }

        recycler_view_missed.apply {
            layoutManager = missedLayoutManager
            adapter = missedAlarmAdapter
        }

        val offset = TimeZone.getDefault().rawOffset
        val tz = DateTimeZone.forOffsetMillis(offset)
        val date = DateTime.now(tz)

        val mySelectedDateFormat = "yyyy-MM-dd"
        val mySelectedDateFormatter = DateTimeFormat.forPattern(mySelectedDateFormat)
        selectedDateStr = mySelectedDateFormatter.print(date)

        fetchData()

        val currentMonth = YearMonth.of(date.year, date.monthOfYear)
        val firstMonth = currentMonth.minusMonths(0)
        val lastMonth = currentMonth.plusMonths(120)
        val firstDayOfWeek = DayOfWeek.MONDAY
        toolbar.calendar_view.setup(firstMonth, lastMonth, firstDayOfWeek)

        toolbar.calendar_view.inDateStyle = InDateStyle.FIRST_MONTH
        toolbar.calendar_view.maxRowCount = 1
        toolbar.calendar_view.hasBoundaries = false

        class DayViewContainer(view: View) : ViewContainer(view) {
            // Will be set when this container is bound. See the dayBinder.
            lateinit var day: CalendarDay
            val textView = view.calendarDayText

            init {
                view.setOnClickListener {

                    if (day.owner == DayOwner.THIS_MONTH) {
                        if (selectedDate != day.date) {
                            val oldDate = selectedDate
                            selectedDate = day.date
                            if (toolbar.calendar_view != null) {
                                toolbar.calendar_view.notifyDateChanged(day.date)
                                oldDate?.let { toolbar.calendar_view.notifyDateChanged(oldDate) }
                            }

                            selectedDateStr = selectedDate.toString()

                            fetchData()

                        }
                        toolbar.calendar_view.notifyDayChanged(day)
                    }
                }
            }
        }

        toolbar.calendar_view.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                val textView = container.textView
                textView.text = day.date.dayOfMonth.toString()
                if (day.owner == DayOwner.THIS_MONTH) {
                    when (day.date) {
                        selectedDate -> {
                            textView.setBackgroundResource(R.drawable.circular_bg_gray)
                        }
                        today -> {
                            textView.setTextColorRes(R.color.colorPrimary)
                            textView.background = null
                        }
                        else -> {
                            textView.setTextColorRes(android.R.color.black)
                            textView.background = null
                        }
                    }
                } else {
                    textView.setTextColorRes(android.R.color.white)
                    textView.background = null
                }
            }
        }

        toolbar.calendar_view.scrollToDate(LocalDate.now())

        toolbar.iv_next.setOnClickListener {
            toolbar.calendar_view.findFirstVisibleDay()?.let {
                toolbar.calendar_view.smoothScrollToDate(it.date.plusDays(7))
            }

        }

        toolbar.iv_prev.setOnClickListener {
            toolbar.calendar_view.findFirstVisibleDay()?.let {
                toolbar.calendar_view.smoothScrollToDate(it.date.minusDays(7))
            }
        }

        toolbar.calendar_view.monthScrollListener = {
            // In week mode, we show the header a bit differently.
            // We show indices with dates from different months since
            // dates overflow and cells in one index can belong to different
            // months/years.
            currentYearMonth = it.yearMonth
            val firstDate = it.weekDays.first().first().date
            val lastDate = it.weekDays.last().last().date
            if (firstDate.yearMonth == lastDate.yearMonth) {
                toolbar.tv_yr.text = firstDate.yearMonth.year.toString()
                toolbar.tv_month.text = monthTitleFormatter.format(firstDate)
            } else {
                toolbar.tv_month.text =
                    "${monthTitleFormatter.format(firstDate)} - ${monthTitleFormatter.format(lastDate)}"
                if (firstDate.year == lastDate.year) {
                    toolbar.tv_yr.text = firstDate.yearMonth.year.toString()
                } else {
                    toolbar.tv_yr.text = "${firstDate.yearMonth.year} - ${lastDate.yearMonth.year}"
                }
            }

        }
    }

    private fun fetchData() {
        if (selectedDateStr != null) {
            showProgress()
            val phoneNumber = AppPreferences.phoneNo

            val alarmsRef = FirebaseFirestore.getInstance().collection("chv_reminders")
            alarmsRef.whereEqualTo("phoneNumber", phoneNumber!!)
                .whereEqualTo("date", selectedDateStr)
                .get()
                .addOnCompleteListener {
                    hideProgress()
                }
                .addOnFailureListener {
                    hideProgress()
                }

            alarmsRef.whereEqualTo("phoneNumber", phoneNumber)
                .whereEqualTo("date", selectedDateStr)
                .addSnapshotListener { querySnapshot, _ ->
                    hideProgress()
                    if (querySnapshot != null && querySnapshot.documents.isNotEmpty()) {
                        alarmList.clear()
                        missedAlarmList.clear()

                        for (document in querySnapshot.documents) {
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
                                missed = document.getBoolean("missed")!!
                            )

                            if (!alarm.cancelled && !alarm.rang && document.getString("date") == selectedDateStr) {
                                alarmList.add(alarm)
                            } else if (alarm.missed && document.getString("date") == selectedDateStr) {
                                missedAlarmList.add(alarm)
                            }
                        }

                        val sorted = alarmList.sortedWith(Comparator { o1, o2 ->
                            when {
                                dateMillis(o1.fromDate) > dateMillis(o2.fromDate) -> 1
                                dateMillis(o1.fromDate) < dateMillis(o2.fromDate) -> -1
                                else -> 0
                            }
                        })

                        val missedSorted = missedAlarmList.sortedWith(Comparator { o1, o2 ->
                            when {
                                dateMillis(o1.fromDate) > dateMillis(o2.fromDate) -> 1
                                dateMillis(o1.fromDate) < dateMillis(o2.fromDate) -> -1
                                else -> 0
                            }
                        })

                        alarmList.clear()
                        missedAlarmList.clear()
                        alarmList.addAll(sorted)
                        missedAlarmList.addAll(missedSorted)

                        myAdapter.notifyDataSetChanged()
                        missedAlarmAdapter.notifyDataSetChanged()

                        if (alarmList.isEmpty()) {
                            hideAlarms()
                        } else {
                            showAlarms()
                        }

                        if (missedAlarmList.isEmpty()) {
                            hideMissedAlarms()
                        } else {
                            showMissedAlarms()
                        }
                    } else {
                        hideAlarms()
                        hideMissedAlarms()
                    }
                }
        }
    }

    private fun showProgress() {
        progress_bar.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        progress_bar.visibility = View.GONE
    }

    private fun hideAlarms() {
        recycler_view_upcoming.makeGone()
        tv_no_alarm.makeVisible()
    }

    private fun showAlarms() {
        recycler_view_upcoming.makeVisible()
        tv_no_alarm.makeGone()
    }

    private fun hideMissedAlarms() {
        recycler_view_missed.makeGone()
        tv_no_missed.makeVisible()
    }

    private fun showMissedAlarms() {
        recycler_view_missed.makeVisible()
        tv_no_missed.makeGone()
    }

    private fun showHome() {
        cl_upcoming_alarms.makeVisible()
        cl_missed_alarms.makeVisible()
    }

    private fun showUpcoming() {
        cl_upcoming_alarms.makeVisible()
        cl_missed_alarms.makeGone()
    }

    private fun showMissed() {
        cl_upcoming_alarms.makeGone()
        cl_missed_alarms.makeVisible()
    }
}
