package com.lakehub.adherenceapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.model.InDateStyle
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.kizitonwose.calendarview.utils.yearMonth
import com.lakehub.adherenceapp.adapters.*
import com.lakehub.adherenceapp.data.Alarm
import com.lakehub.adherenceapp.data.FollowUp
import kotlinx.android.synthetic.main.activity_follow_up.*
import kotlinx.android.synthetic.main.activity_follow_up.view.*
import kotlinx.android.synthetic.main.calendar_day.view.*
import kotlinx.android.synthetic.main.content_follow_up.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

class FollowUpActivity : AppCompatActivity() {
    private val today = LocalDate.now()
    private val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM")
    private var selectedDate: LocalDate? = null
    private var selectedDateStr: String? = null
    private var currentYearMonth: YearMonth? = null
    private val alarmList = arrayListOf<Alarm>()
    private val followUps = arrayListOf<FollowUp>()
    private lateinit var myAdapter: MissedMedicationAdapter
    private lateinit var followUpAdapter: FollowUpAdapter
    private var selected: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follow_up)

        window.statusBarColor = ContextCompat.getColor(this, R.color.colorRedDark)

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

        hideProgress()

        myAdapter = MissedMedicationAdapter(this, alarmList)
        followUpAdapter = FollowUpAdapter(this, followUps)
        val mLayoutManager = LinearLayoutManager(this)
        val myLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        recycler_view_missed.apply {
            layoutManager = mLayoutManager
            adapter = myAdapter
        }
        recycler_view_follow_up.apply {
            layoutManager = myLayoutManager
            adapter = followUpAdapter
        }

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
//                        toolbar.calendar_view.notifyDayChanged(day)
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

        iv_back.setOnClickListener {
            finish()
        }
    }

    private fun showProgress() {
        progress_bar.makeVisible()
    }

    private fun hideProgress() {
        progress_bar.makeGone()
    }

    private fun hideMissed() {
        recycler_view_missed.makeGone()
        tv_no_missed.makeVisible()
    }

    private fun showMissed() {
        recycler_view_missed.makeVisible()
        tv_no_missed.makeGone()
    }

    private fun hideFollowUps() {
        recycler_view_follow_up.makeGone()
        tv_no_follow_up.makeVisible()
    }

    private fun showFollowUps() {
        recycler_view_follow_up.makeVisible()
        tv_no_follow_up.makeGone()
    }

    private fun fetchData() {
        showProgress()
        val alarmRef = FirebaseFirestore.getInstance().collection("alarms")
        val followUpRef = FirebaseFirestore.getInstance().collection("follow_ups")

        alarmRef.whereEqualTo("chvAccessKey", AppPreferences.accessKey)
            .whereEqualTo("date", selectedDateStr)
            .whereEqualTo("missed", true)
            .whereEqualTo("place", false)
            .whereEqualTo("marked", false)
            .addSnapshotListener { querySnapshot, _ ->
                hideProgress()
                if (querySnapshot != null && querySnapshot.documents.isNotEmpty()) {
                    alarmList.clear()

                    for (document in querySnapshot.documents) {
                        val alarm = document.toObject(Alarm::class.java)

                        alarmList.add(alarm!!)
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

                    if (alarmList.isEmpty()) {
                        hideMissed()
                    } else {
                        showMissed()
                    }
                } else {
                    hideMissed()
                }
            }

        followUpRef.whereEqualTo("date", selectedDateStr)
            .whereEqualTo("marked", false)
            .addSnapshotListener { querySnapshot, _ ->
                hideProgress()
                if (querySnapshot != null && querySnapshot.documents.isNotEmpty()) {
                    followUps.clear()

                    for (document in querySnapshot.documents) {
                        val followUp = document.toObject(FollowUp::class.java)

                        followUps.add(followUp!!)
                    }

                    val sorted = followUps.sortedWith(Comparator { o1, o2 ->
                        when {
                            dateMillis(o1.dateTime) > dateMillis(o2.dateTime) -> 1
                            dateMillis(o1.dateTime) < dateMillis(o2.dateTime) -> -1
                            else -> 0
                        }
                    })

                    followUps.clear()
                    followUps.addAll(sorted)

                    followUpAdapter.notifyDataSetChanged()

                    if (followUps.isEmpty()) {
                        hideFollowUps()
                    } else {
                        showFollowUps()
                    }
                } else {
                    hideFollowUps()
                }
            }
    }
}
