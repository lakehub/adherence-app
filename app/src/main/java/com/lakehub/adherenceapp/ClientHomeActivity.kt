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
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.model.InDateStyle
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.kizitonwose.calendarview.utils.yearMonth
import com.lakehub.adherenceapp.AppPreferences.tonePath
import kotlinx.android.synthetic.main.activity_client_home.*
import kotlinx.android.synthetic.main.app_bar_client_home.*
import kotlinx.android.synthetic.main.app_bar_client_home.view.*
import kotlinx.android.synthetic.main.calendar_day.view.*
import kotlinx.android.synthetic.main.content_client_home.*
import kotlinx.android.synthetic.main.my_menu.*
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import org.threeten.bp.format.DateTimeFormatter


@Suppress("UNCHECKED_CAST")
class ClientHomeActivity : AppCompatActivity() {
    private lateinit var alarmList: ArrayList<Alarm>
    private lateinit var myAdapter: AlarmAdapter

    private val selectedDates = mutableSetOf<LocalDate>()
    private val today = LocalDate.now()
    private val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM")
    private var selectedDate: LocalDate? = null
    private var selectedDateStr: String? = null
    private var currentYearMonth: YearMonth? = null

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

        registerForContextMenu(recycler_view)
        recycler_view.makeGone()
        tv_no_alarm.makeGone()

        iv_menu.setOnClickListener {
            if (!drawer_layout.isDrawerOpen(GravityCompat.START)) {
                drawer_layout.openDrawer(GravityCompat.START, true)
            }
        }

        iv_close.setOnClickListener {
            drawer_layout.closeDrawer(GravityCompat.START, true)
        }

        add_fab.setOnClickListener {
            startActivityForResult(Intent(this, AddAlarmActivity::class.java), 900)
        }

        cl_settings.setOnClickListener {
            drawer_layout.closeDrawer(GravityCompat.START, true)
            startActivity(Intent(this, SettingsActivity::class.java))
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

        val mySelectedDateFormat = "yyyy-MM-dd"
        val mySelectedDateFormatter = DateTimeFormat.forPattern(mySelectedDateFormat)
        selectedDateStr = mySelectedDateFormatter.print(DateTime.now())

        fetchData(selectedDateStr!!)

        cl_logout.setOnClickListener {
            auth.signOut()
        }

        auth.addAuthStateListener {
            val user = it.currentUser
            if (user == null) {
                this.finishAffinity()
            }
        }

        val date = DateTime.now()

        val currentMonth = YearMonth.of(date.year, date.monthOfYear)
        val firstMonth = currentMonth.minusMonths(0)
        val lastMonth = currentMonth.plusMonths(120)
        val firstDayOfWeek = DayOfWeek.MONDAY
        calendar_view.setup(firstMonth, lastMonth, firstDayOfWeek)

        calendar_view.inDateStyle = InDateStyle.FIRST_MONTH
        calendar_view.maxRowCount = 1
        calendar_view.hasBoundaries = false

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
                            calendar_view.notifyDateChanged(day.date)
                            oldDate?.let { calendar_view.notifyDateChanged(oldDate) }

                            selectedDateStr = selectedDate.toString()

                            fetchData(selectedDateStr!!)

                        }
                        calendar_view.notifyDayChanged(day)
                    }
                }
            }
        }

        /*class DayViewContainer(view: View) : ViewContainer(view) {
            // Will be set when this container is bound. See the dayBinder.
            lateinit var day: CalendarDay

            val textView = with(view) {
                setOnClickListener {
                    if (day.owner == DayOwner.THIS_MONTH) {
                        if (selectedDate == day.date) {
                            selectedDate = null
                            calendar_view.notifyDayChanged(day)
                        } else {
                            val oldDate = selectedDate
                            selectedDate = day.date
                            calendar_view.notifyDateChanged(day.date)
                            oldDate?.let { calendar_view.notifyDateChanged(oldDate) }
                        }
                    }
                }
                return@with this as TextView

                val textView = view.exOneDayText
            }

        }*/

        calendar_view.dayBinder = object : DayBinder<DayViewContainer> {
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

        calendar_view.scrollToDate(LocalDate.now())

        toolbar.iv_next.setOnClickListener {
            calendar_view.findFirstVisibleDay()?.let {
                calendar_view.smoothScrollToDate(it.date.plusDays(7))
            }

        }

        toolbar.iv_prev.setOnClickListener {
            calendar_view.findFirstVisibleDay()?.let {
                calendar_view.smoothScrollToDate(it.date.minusDays(7))
            }
        }

        calendar_view.monthScrollListener = {
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
            if (success != null && success)
                fetchData(selectedDateStr!!)
        }
    }

    private fun fetchData(dateStr: String) {
        showProgress()
        val phoneNumber = FirebaseAuth.getInstance().currentUser?.phoneNumber

        val alarmsRef = FirebaseFirestore.getInstance().collection("alarms")
        alarmsRef.whereEqualTo("phoneNumber", phoneNumber!!)
            .whereEqualTo("date", dateStr)
            .get()
            .addOnCompleteListener {
                hideProgress()
                val documents = it.result?.documents
                if (documents!!.isNotEmpty()) {
                    showAlarms()
                    alarmList.clear()

                    for (document in documents) {
                        val alarm = Alarm(
                            description = document.getString("description")!!,
                            fromDate = document.getString("fromDate")!!,
                            toDate = document.getString("toDate"),
                            docId = document.id,
                            alarmTone = document.getString("alarmTonePath"),
                            location = document.getString("location"),
                            isPlace = document.getBoolean("isPlace"),
                            medType = document.getDouble("medicationType")?.toInt(),
                            repeatMode = document.get("repeatMode") as ArrayList<Int>,
                            id = document.getLong("id")?.toInt()!!,
                            cancelled = document.getBoolean("cancelled")!!
                        )

                        if (!alarm.cancelled) {
                            alarmList.add(alarm)
                        }
                    }

                    val sorted = alarmList.sortedWith(Comparator { o1, o2 ->
                        when {
                            dateMillis(o1.fromDate) > dateMillis(o2.fromDate) -> 1
                            dateMillis(o1.fromDate) < dateMillis(o2.fromDate) -> -1
                            else -> 0
                        }
                    })

                    alarmList.clear()
                    alarmList.addAll(sorted)

                    myAdapter.notifyDataSetChanged()
                } else {
                    hideAlarms()
                }
            }
            .addOnFailureListener {
                hideProgress()
            }

        alarmsRef.whereEqualTo("phoneNumber", phoneNumber)
            .whereEqualTo("date", dateStr)
            .addSnapshotListener { querySnapshot, _ ->
                if (querySnapshot != null && querySnapshot.documents.isNotEmpty()) {
                    alarmList.clear()

                    for (document in querySnapshot.documents) {
                        val alarm = Alarm(
                            description = document.getString("description")!!,
                            fromDate = document.getString("fromDate")!!,
                            toDate = document.getString("toDate"),
                            docId = document.id,
                            alarmTone = document.getString("alarmTonePath"),
                            location = document.getString("location"),
                            isPlace = document.getBoolean("isPlace"),
                            cancelled = document.getBoolean("cancelled")!!,
                            medType = document.getDouble("medicationType")?.toInt(),
                            repeatMode = document.get("repeatMode") as ArrayList<Int>,
                            id = document.getLong("id")?.toInt()!!
                        )

                        if (!alarm.cancelled) {
                            alarmList.add(alarm)
                        }
                    }

                    val sorted = alarmList.sortedWith(Comparator { o1, o2 ->
                        when {
                            dateMillis(o1.fromDate) > dateMillis(o2.fromDate) -> 1
                            dateMillis(o1.fromDate) < dateMillis(o2.fromDate) -> -1
                            else -> 0
                        }
                    })

                    alarmList.clear()
                    alarmList.addAll(sorted)

                    myAdapter.notifyDataSetChanged()
                }
            }

        /*recycler_view.addOnItemTouchListener(object : RecyclerItemClickListener(this,
    object : OnItemClickListener {
        override fun onItemClick(view: View, position: Int) {
            val myView = view.findViewById<TextView>(R.id.tv_count)
//                    openOptionMenu(myView, position)
            Log.d("TAG", "item touched pos: $position")
        }

    }) {

})*/
    }

    /*fun openOptionMenu(v: View, position: Int) {
        val popup = PopupMenu(v.context, v)
        popup.menuInflater.inflate(R.menu.upcoming_menu, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            Toast.makeText(
                baseContext,
                "You selected the action : " + item.title + " position " + position,
                Toast.LENGTH_SHORT
            ).show()
            true
        }
        popup.show()
    }*/

    private fun showProgress() {
        progress_bar.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        progress_bar.visibility = View.GONE
    }

    private fun hideAlarms() {
        recycler_view.makeGone()
        tv_no_alarm.makeVisible()
    }

    private fun showAlarms() {
        recycler_view.makeVisible()
        tv_no_alarm.makeGone()
    }
}
