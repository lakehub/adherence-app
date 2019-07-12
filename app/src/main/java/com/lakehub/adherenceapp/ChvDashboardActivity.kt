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

        add_fab.setOnClickListener {
            startActivityForResult(Intent(this, AddChvReminderActivity::class.java), 900)
        }

        cl_settings_menu.setOnClickListener {
            drawer_layout.closeDrawer(GravityCompat.START, true)
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        cl_logout_menu.setOnClickListener {
            drawer_layout.closeDrawer(GravityCompat.START)
            auth.signOut()
        }

        cl_clients_menu.setOnClickListener {
            drawer_layout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, ClientsActivity::class.java))
        }

        val offset = TimeZone.getDefault().rawOffset
        val tz = DateTimeZone.forOffsetMillis(offset)
        val date = DateTime.now(tz)

        val mySelectedDateFormat = "yyyy-MM-dd"
        val mySelectedDateFormatter = DateTimeFormat.forPattern(mySelectedDateFormat)
        selectedDateStr = mySelectedDateFormatter.print(date)

//        fetchData()

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
                            toolbar.calendar_view.notifyDateChanged(day.date)
                            oldDate?.let { toolbar.calendar_view.notifyDateChanged(oldDate) }

                            selectedDateStr = selectedDate.toString()

//                            fetchData()

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
}
