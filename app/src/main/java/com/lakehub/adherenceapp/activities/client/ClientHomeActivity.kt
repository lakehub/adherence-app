package com.lakehub.adherenceapp.activities.client

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
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
import com.lakehub.adherenceapp.adapters.AlarmAdapter
import com.lakehub.adherenceapp.adapters.MissedAlarmAdapter
import com.lakehub.adherenceapp.adapters.TakenAlarmAdapter
import com.lakehub.adherenceapp.app.AppPreferences
import com.lakehub.adherenceapp.app.MainApplication
import com.lakehub.adherenceapp.data.Alarm
import com.lakehub.adherenceapp.utils.*
import com.lakehub.adherenceapp.utils.setTextColorRes
import kotlinx.android.synthetic.main.activity_client_home.*
import kotlinx.android.synthetic.main.app_bar_client_home.*
import kotlinx.android.synthetic.main.app_bar_client_home.view.*
import kotlinx.android.synthetic.main.calendar_day.view.*
import kotlinx.android.synthetic.main.content_client_home.*
import kotlinx.android.synthetic.main.client_menu.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import org.threeten.bp.format.DateTimeFormatter
import java.io.File
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList


class ClientHomeActivity : AppCompatActivity() {
    private lateinit var alarmList: ArrayList<Alarm>
    private lateinit var missedAlarmList: ArrayList<Alarm>
    private lateinit var takenList: ArrayList<Alarm>
    private lateinit var myAdapter: AlarmAdapter
    private lateinit var missedAlarmAdapter: MissedAlarmAdapter
    private lateinit var takenAlarmAdapter: TakenAlarmAdapter

    private val selectedDates = mutableSetOf<LocalDate>()
    private val today = LocalDate.now()
    private val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM")
    private var selectedDate: LocalDate? = null
    private var selectedDateStr: String? = null
    private var currentYearMonth: YearMonth? = null
    private var selected = false

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

        registerForContextMenu(recycler_view_upcoming)
        recycler_view_upcoming.makeGone()
        recycler_view_missed.makeGone()
        tv_no_alarm.makeGone()
        tv_no_missed.makeGone()

        iv_menu.setOnClickListener {
            if (!drawer_layout.isDrawerOpen(GravityCompat.START)) {
                drawer_layout.openDrawer(GravityCompat.START, true)
            }
        }

        iv_close.setOnClickListener {
            drawer_layout.closeDrawer(GravityCompat.START, true)
        }

        add_fab.setOnClickListener {
            drawer_layout.closeDrawer(GravityCompat.START, true)
            AppPreferences.surfed = true
            startActivityForResult(Intent(this, AddAlarmActivity::class.java), 900)
        }

        cl_profile_menu.setOnClickListener {
            drawer_layout.closeDrawer(GravityCompat.START, true)
            AppPreferences.surfed = true
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        alarmList = arrayListOf()
        missedAlarmList = arrayListOf()
        takenList = arrayListOf()
        myAdapter = AlarmAdapter(this, alarmList)
        missedAlarmAdapter = MissedAlarmAdapter(this, missedAlarmList)
        takenAlarmAdapter = TakenAlarmAdapter(this, takenList)

        val mLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        val dividerItemDecoration = androidx.recyclerview.widget
            .DividerItemDecoration(
                this,
                mLayoutManager.orientation
            )

        val missedLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        val missedDividerItemDecoration = androidx.recyclerview.widget
            .DividerItemDecoration(
                this,
                mLayoutManager.orientation
            )

        val takenLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        val takenDividerItemDecoration = androidx.recyclerview.widget
            .DividerItemDecoration(
                this,
                mLayoutManager.orientation
            )

        recycler_view_upcoming.apply {
            layoutManager = mLayoutManager
            addItemDecoration(dividerItemDecoration)
            adapter = myAdapter
        }

        recycler_view_missed.apply {
            layoutManager = missedLayoutManager
            addItemDecoration(missedDividerItemDecoration)
            adapter = missedAlarmAdapter
        }

        recyclerViewTaken.apply {
            layoutManager = takenLayoutManager
            addItemDecoration(takenDividerItemDecoration)
            adapter = takenAlarmAdapter
        }

        val mySelectedDateFormat = "yyyy-MM-dd"
        val mySelectedDateFormatter = DateTimeFormat.forPattern(mySelectedDateFormat)
        selectedDateStr = mySelectedDateFormatter.print(DateTime.now())

        cl_logout_menu.setOnClickListener {
            drawer_layout.closeDrawer(GravityCompat.START)
//            auth.signOut()
//            FirebaseFirestore.getInstance().clearPersistence()
            AppPreferences.loggedIn = false
            AppPreferences.accessKey = null
            AppPreferences.accountType = 0
            AppPreferences.chvAccessKey = null
            AppPreferences.profileImg = null
            AppPreferences.authenticated = false
            emptyDirectory("user_images")
            FirebaseAuth.getInstance().signOut()
            finish()
        }

        iv_alarm.setOnClickListener {
            showUpcoming()
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

        clTakenMenu.setOnClickListener {
            showTaken()
            drawer_layout.closeDrawer(GravityCompat.START)
        }

        appointmentsMenu.setOnClickListener {
            startActivity(Intent(this, AppointmentsActivity::class.java))
            drawer_layout.closeDrawer(GravityCompat.START)
        }

        /*auth.addAuthStateListener {
            val user = it.currentUser
            if (user == null) {
//                startActivity(Intent(this, LoginActivity::class.java))
                this.finish()
            }
        }*/

        val offset = TimeZone.getDefault().rawOffset
        val tz = DateTimeZone.forOffsetMillis(offset)
        val date = DateTime.now(tz)

        when {
            date.hourOfDay in 0..11 -> tv_title.text  =getString(R.string.good_morning)
            date.hourOfDay in 12..16 -> tv_title.text  =getString(R.string.good_afternoon)
            date.hourOfDay in 17..24 -> tv_title.text  =getString(R.string.good_evening)
        }

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
                            selected = true
                            fetchByDate()

                        }
//                        calendar_view.notifyDayChanged(day)
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

        calendar_view.scrollToDate(LocalDate.now())

        iv_next.setOnClickListener {
            calendar_view.findFirstVisibleDay()?.let {
                calendar_view.smoothScrollToDate(it.date.plusDays(7))
            }

        }

        iv_prev.setOnClickListener {
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

        fetchRecent()

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
        /*if (requestCode == 900 && resultCode == Activity.RESULT_OK) {
            val success = data?.getBooleanExtra("success", false)
            if (success != null && success)
                fetchByDate(selectedDateStr!!)
        }*/
    }

    private fun fetchByDate() {
        if (selectedDateStr != null) {
            showProgress()
            val accessKey = AppPreferences.accessKey

            val alarmsRef = FirebaseFirestore.getInstance().collection("alarms")
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
                        takenList.clear()

                        for (document in querySnapshot.documents) {
                            val alarm = document.toObject(Alarm::class.java)

                            if (!alarm?.cancelled!! && !alarm.rang && alarm.date == selectedDateStr) {
                                alarmList.add(alarm)
                            } else if (alarm.missed) {
                                if (!alarm.cleaned) {
                                    missedAlarmList.add(alarm)
                                }
                            } else if (alarm.rang && !alarm.cancelled) {
                                takenList.add(alarm)
                            }
                        }

                        val sorted = alarmList.sortedWith(Comparator { o1, o2 ->
                            when {
                                dateMillis(o1.fromDate) > dateMillis(
                                    o2.fromDate
                                ) -> 1
                                dateMillis(o1.fromDate) < dateMillis(
                                    o2.fromDate
                                ) -> -1
                                else -> 0
                            }
                        })

                        val missedSorted = missedAlarmList.sortedWith(Comparator { o1, o2 ->
                            when {
                                dateMillis(o1.fromDate) > dateMillis(
                                    o2.fromDate
                                ) -> 1
                                dateMillis(o1.fromDate) < dateMillis(
                                    o2.fromDate
                                ) -> -1
                                else -> 0
                            }
                        })

                        val takenSorted = takenList.sortedWith(Comparator { o1, o2 ->
                            when {
                                dateMillis(o1.fromDate) > dateMillis(
                                    o2.fromDate
                                ) -> 1
                                dateMillis(o1.fromDate) < dateMillis(
                                    o2.fromDate
                                ) -> -1
                                else -> 0
                            }
                        })

                        alarmList.clear()
                        missedAlarmList.clear()
                        takenList.clear()
                        alarmList.addAll(sorted)
                        missedAlarmList.addAll(missedSorted)
                        takenList.addAll(takenSorted)

                        myAdapter.notifyDataSetChanged()
                        missedAlarmAdapter.notifyDataSetChanged()
                        takenAlarmAdapter.notifyDataSetChanged()

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

                        if (takenList.isEmpty()) {
                            hideTakenAlarms()
                        } else {
                            showTakenAlarms()
                        }
                    } else {
                        hideAlarms()
                        hideMissedAlarms()
                        hideTakenAlarms()
                    }
                }
            }
        }
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
                val imgRef = storageRef.child("client_images/$filename")
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

    private fun fetchRecent() {
        selected = false
        selectDate(LocalDate.now())
        val offset = TimeZone.getDefault().rawOffset
        val tz = DateTimeZone.forOffsetMillis(offset)
        val millis = DateTime.now(tz).millis
        showProgress()
        val accessKey = AppPreferences.accessKey

        val alarmsRef = FirebaseFirestore.getInstance().collection("alarms")
            .whereEqualTo("accessKey", accessKey!!)
            .whereEqualTo("cancelled", false)
            .whereEqualTo("rang", false)
            .whereGreaterThanOrEqualTo("millis", millis)
            .orderBy("millis", Query.Direction.ASCENDING)
//            .limit(5)

        val missedAlarmsRef = FirebaseFirestore.getInstance().collection("alarms")
            .whereEqualTo("accessKey", accessKey)
            .whereEqualTo("missed", true)
            .orderBy("millis", Query.Direction.ASCENDING)
//            .limit(5)

        val takenAlarmsRef = FirebaseFirestore.getInstance().collection("alarms")
            .whereEqualTo("accessKey", accessKey)
            .whereEqualTo("missed", false)
            .whereEqualTo("cancelled", false)
            .whereEqualTo("rang", true)
            .orderBy("millis", Query.Direction.ASCENDING)
//            .limit(5)

        alarmsRef.addSnapshotListener { querySnapshot, _ ->
            hideProgress()
            if (!selected) {
                if (querySnapshot != null && !querySnapshot.isEmpty) {
                    alarmList.clear()

                    for (document in querySnapshot.documents) {
                        val alarm = document.toObject(Alarm::class.java)
                        alarmList.add(alarm!!)

                    }

                    val sorted = alarmList.sortedWith(Comparator { o1, o2 ->
                        when {
                            dateMillis(o1.fromDate) > dateMillis(
                                o2.fromDate
                            ) -> 1
                            dateMillis(o1.fromDate) < dateMillis(
                                o2.fromDate
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

        missedAlarmsRef.addSnapshotListener { querySnapshot, e ->
            if (!selected) {
                if (querySnapshot != null && !querySnapshot.isEmpty) {
                    missedAlarmList.clear()

                    for (document in querySnapshot.documents) {
                        val alarm = document.toObject(Alarm::class.java)
                        if (alarm?.cleaned == false) {
                            missedAlarmList.add(alarm)
                        }

                    }

                    val missedSorted = missedAlarmList.sortedWith(Comparator { o1, o2 ->
                        when {
                            dateMillis(o1.fromDate) > dateMillis(
                                o2.fromDate
                            ) -> 1
                            dateMillis(o1.fromDate) < dateMillis(
                                o2.fromDate
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

        takenAlarmsRef.addSnapshotListener { querySnapshot, e ->
            if (!selected) {
                if (querySnapshot != null && !querySnapshot.isEmpty) {
                    takenList.clear()

                    for (document in querySnapshot.documents) {
                        val alarm = document.toObject(Alarm::class.java)
                        takenList.add(alarm!!)

                    }

                    val takenSorted = takenList.sortedWith(Comparator { o1, o2 ->
                        when {
                            dateMillis(o1.fromDate) > dateMillis(
                                o2.fromDate
                            ) -> 1
                            dateMillis(o1.fromDate) < dateMillis(
                                o2.fromDate
                            ) -> -1
                            else -> 0
                        }
                    })

                    takenList.clear()
                    takenList.addAll(takenSorted)

                    takenAlarmAdapter.notifyDataSetChanged()

                    if (takenList.isEmpty()) {
                        hideTakenAlarms()
                    } else {
                        showTakenAlarms()
                    }
                } else {
                    hideTakenAlarms()
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

    private fun hideTakenAlarms() {
        recyclerViewTaken.makeGone()
        tvNoTaken.makeVisible()
    }

    private fun showTakenAlarms() {
        recyclerViewTaken.makeVisible()
        tvNoTaken.makeGone()
    }

    private fun showHome() {
        fetchRecent()
        cl_upcoming_alarms.makeVisible()
        cl_missed_alarms.makeVisible()
        clTakenAlarms.makeVisible()
    }

    private fun showUpcoming() {
        fetchRecent()
        cl_upcoming_alarms.makeVisible()
        cl_missed_alarms.makeGone()
        clTakenAlarms.makeGone()
    }

    private fun showMissed() {
        fetchRecent()
        cl_upcoming_alarms.makeGone()
        cl_missed_alarms.makeVisible()
        clTakenAlarms.makeGone()
    }

    private fun showTaken() {
        fetchRecent()
        cl_upcoming_alarms.makeGone()
        cl_missed_alarms.makeGone()
        clTakenAlarms.makeVisible()
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

    override fun onPause() {
        super.onPause()
    }
}
