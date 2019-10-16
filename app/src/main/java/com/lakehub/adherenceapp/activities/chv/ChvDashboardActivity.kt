package com.lakehub.adherenceapp.activities.chv

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.model.InDateStyle
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.kizitonwose.calendarview.utils.yearMonth
import com.lakehub.adherenceapp.*
import com.lakehub.adherenceapp.adapters.ChvMissedReminderAdapter
import com.lakehub.adherenceapp.adapters.ChvReminderAdapter
import com.lakehub.adherenceapp.app.AppPreferences
import com.lakehub.adherenceapp.app.MainApplication
import com.lakehub.adherenceapp.data.ChvReminder
import com.lakehub.adherenceapp.utils.*
import com.lakehub.adherenceapp.utils.setTextColorRes
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
import java.io.File
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
    private var selected: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chv_dashboard)

        setSupportActionBar(toolbar)
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

        cl_follow_up_menu.setOnClickListener {
            startActivity(Intent(this, FollowUpActivity::class.java))
            drawer_layout.closeDrawer(GravityCompat.START)
        }

        cl_reports_menu.setOnClickListener {
            AppPreferences.surfed = true
            startActivity(Intent(this, ChvReportActivity::class.java))
            drawer_layout.closeDrawer(GravityCompat.START)
        }

        add_fab.setOnClickListener {
            AppPreferences.surfed = true
            startActivityForResult(Intent(this, AddChvReminderActivity::class.java), 900)
            drawer_layout.closeDrawer(GravityCompat.START, true)
        }

        cl_profile_menu.setOnClickListener {
            drawer_layout.closeDrawer(GravityCompat.START, true)
            startActivity(Intent(this, ChvProfileActivity::class.java))
            AppPreferences.surfed = true
        }

        iv_user.setOnClickListener {
            AppPreferences.surfed = true
            startActivity(Intent(this, ChvProfileActivity::class.java))
            drawer_layout.closeDrawer(GravityCompat.START, true)
        }

        cl_logout_menu.setOnClickListener {
            drawer_layout.closeDrawer(GravityCompat.START)
//            auth.signOut()
//            FirebaseFirestore.getInstance().clearPersistence()
            AppPreferences.loggedIn = false
            AppPreferences.accessKey = null
            AppPreferences.accountType = 0
            AppPreferences.chvAccessKey = null
            AppPreferences.profileImg = null
            emptyDirectory("user_images")
            emptyDirectory("client_images")
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

        when {
            date.hourOfDay in 0..12 -> tv_title.text  =getString(R.string.good_morning_chv)
            date.hourOfDay in 13..16 -> tv_title.text  =getString(R.string.good_afternoon_chv)
            date.hourOfDay in 17..24 -> tv_title.text  =getString(R.string.good_evening_chv)
        }

        val mySelectedDateFormat = "yyyy-MM-dd"
        val mySelectedDateFormatter = DateTimeFormat.forPattern(mySelectedDateFormat)
        selectedDateStr = mySelectedDateFormatter.print(date)

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
                            selected = true

                            fetchByDate()

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
                    if (selected) {
                        when {
                            day.date == selectedDate -> {
                                textView.setBackgroundResource(R.drawable.selected_date_bg)
                            }
                            day.date == today -> {
                                textView.setTextColorRes(R.color.colorPrimary)
                                textView.background = null
                            }
                            else -> {
                                textView.setTextColorRes(android.R.color.black)
                                textView.background = null
                            }
                        }
                    } else {
                        when {
                            day.date == selectedDate && day.date != today -> {
                                textView.setBackgroundResource(R.drawable.selected_date_bg)
                            }
                            day.date == today -> {
                                textView.setTextColorRes(R.color.colorPrimary)
                                textView.background = null
                            }
                            else -> {
                                textView.setTextColorRes(android.R.color.black)
                                textView.background = null
                            }
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

        fetchRecent()
    }

    override fun onResume() {
        super.onResume()
        if (AppPreferences.profileImg != null) {
            val contextWrapper = ContextWrapper(MainApplication.applicationContext())
            val directory: File = contextWrapper.getDir("user_images", Context.MODE_PRIVATE)
            var bitmap = loadImgFromInternalStorage(
                directory.absolutePath,
                AppPreferences.profileImg!!
            )
            if (bitmap == null) {
                val storageRef = FirebaseStorage.getInstance().reference
                val filename = AppPreferences.profileImg
                val imgRef = storageRef.child("chv_images/$filename")
                val mContextWrapper = ContextWrapper(this)
                val mDirectory: File = mContextWrapper.getDir(
                    "user_images",
                    Context.MODE_PRIVATE
                )
                val file = File(mDirectory, filename!!)
                imgRef.getFile(file).addOnSuccessListener {
                    var myBitmap = loadImgFromInternalStorage(
                        directory.absolutePath,
                        AppPreferences.profileImg!!
                    )
                    Glide.with(this)
                        .load(myBitmap)
                        .apply(
                            RequestOptions()
                                .placeholder(R.drawable.user)
                                .error(R.drawable.user)
                        )
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?, model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                return false

                            }

                            override fun onResourceReady(
                                resource: Drawable?, model: Any?,
                                target: Target<Drawable>?, dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                myBitmap!!.recycle()
                                myBitmap = null
                                return false
                            }

                        })
                        .into(iv_user)
                }
            } else {
                Glide.with(this)
                    .load(bitmap)
                    .apply(
                        RequestOptions()
                            .placeholder(R.drawable.user)
                            .error(R.drawable.user)
                    )
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?, model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false

                        }

                        override fun onResourceReady(
                            resource: Drawable?, model: Any?,
                            target: Target<Drawable>?, dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            bitmap!!.recycle()
                            bitmap = null
                            return false
                        }

                    })
                    .into(iv_user)
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
        fetchRecent()
        cl_upcoming_alarms.makeVisible()
        cl_missed_alarms.makeVisible()
    }

    private fun showUpcoming() {
        fetchRecent()
        cl_upcoming_alarms.makeVisible()
        cl_missed_alarms.makeGone()
    }

    private fun showMissed() {
        fetchRecent()
        cl_upcoming_alarms.makeGone()
        cl_missed_alarms.makeVisible()
    }


    private fun selectDate(date: LocalDate) {
        calendar_view.smoothScrollToDate(date)
        if (selectedDate != date) {
            val oldDate = selectedDate
            selectedDate = date
            oldDate?.let { calendar_view.notifyDateChanged(it) }
            calendar_view.notifyDateChanged(date)

        }
    }

    private fun fetchByDate() {
        if (selectedDateStr != null) {
            showProgress()
            val accessKey = AppPreferences.accessKey

            val alarmsRef = FirebaseFirestore.getInstance().collection("chv_reminders")
                .whereEqualTo("accessKey", accessKey!!)
                .whereEqualTo("date", selectedDateStr)
                .whereEqualTo("cancelled", false)

            alarmsRef.get()
                .addOnCompleteListener {
                    if (it.isComplete)
                        hideProgress()
                }

            alarmsRef.addSnapshotListener { querySnapshot, _ ->
                hideProgress()
                if (selected) {
                    if (querySnapshot != null && querySnapshot.documents.isNotEmpty()) {
                        alarmList.clear()
                        missedAlarmList.clear()

                        for (document in querySnapshot.documents) {
                            val alarm = document.toObject(ChvReminder::class.java)

                            if (!alarm?.rang!! && alarm.date == selectedDateStr) {
                                alarmList.add(alarm)
                            } else if (alarm.missed) {
                                missedAlarmList.add(alarm)
                            }
                        }

                        val sorted = alarmList.sortedWith(Comparator { o1, o2 ->
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

                        val missedSorted = missedAlarmList.sortedWith(Comparator { o1, o2 ->
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
    }

    private fun fetchRecent() {
        selected = false
        selectDate(LocalDate.now())
        val offset = TimeZone.getDefault().rawOffset
        val tz = DateTimeZone.forOffsetMillis(offset)
        val millis = DateTime.now(tz).millis
        showProgress()
        val accessKey = AppPreferences.accessKey

        val alarmsRef = FirebaseFirestore.getInstance().collection("chv_reminders")
            .whereEqualTo("accessKey", accessKey!!)
            .whereEqualTo("cancelled", false)
            .whereEqualTo("rang", false)
            .whereGreaterThanOrEqualTo("millis", millis)
            .orderBy("millis", Query.Direction.ASCENDING)
            .limit(5)

        val missedAlarmsRef = FirebaseFirestore.getInstance().collection("chv_reminders")
            .whereEqualTo("accessKey", accessKey)
            .whereEqualTo("missed", true)
            .orderBy("millis", Query.Direction.ASCENDING)
            .limit(5)
        /*alarmsRef.get()
            .addOnCompleteListener {
                if (it.isComplete) {
                    hideProgress()
                }
            }*/

        alarmsRef.addSnapshotListener { querySnapshot, _ ->
            hideProgress()
            if (!selected) {
                if (querySnapshot != null && !querySnapshot.isEmpty) {
                    alarmList.clear()

                    for (document in querySnapshot.documents) {
                        val alarm = document.toObject(ChvReminder::class.java)
                        alarmList.add(alarm!!)

                    }

                    val sorted = alarmList.sortedWith(Comparator { o1, o2 ->
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

                    alarmList.clear()
                    alarmList.addAll(sorted)

                    myAdapter.notifyDataSetChanged()

                    if (alarmList.isEmpty()) {
                        hideAlarms()
                    } else {
                        showAlarms()
                    }
                } else {
                    hideAlarms()
                }
            }
        }

        missedAlarmsRef.addSnapshotListener { querySnapshot, _ ->
            if (!selected) {
                if (querySnapshot != null && !querySnapshot.isEmpty) {
                    missedAlarmList.clear()

                    for (document in querySnapshot.documents) {
                        val alarm = document.toObject(ChvReminder::class.java)
                        missedAlarmList.add(alarm!!)

                    }

                    val missedSorted = missedAlarmList.sortedWith(Comparator { o1, o2 ->
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

                    missedAlarmList.clear()
                    missedAlarmList.addAll(missedSorted)

                    missedAlarmAdapter.notifyDataSetChanged()

                    if (missedAlarmList.isEmpty()) {
                        hideMissedAlarms()
                    } else {
                        showMissedAlarms()
                    }
                } else {
                    hideMissedAlarms()
                }
            }
        }
    }
}
