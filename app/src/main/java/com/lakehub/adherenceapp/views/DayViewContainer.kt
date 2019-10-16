package com.lakehub.adherenceapp.views

import android.view.View
import android.widget.TextView
import com.kizitonwose.calendarview.ui.ViewContainer
import kotlinx.android.synthetic.main.calendar_day.view.*

class DayViewContainer(view: View) : ViewContainer(view) {
    val textView: TextView = view.calendarDayText
    
    // Without the kotlin android extensions plugin
    // val textView = view.findViewById<TextView>(R.id.calendarDayText)
}