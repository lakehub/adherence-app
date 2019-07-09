package com.lakehub.adherenceapp

import android.view.View
import com.kizitonwose.calendarview.ui.ViewContainer
import kotlinx.android.synthetic.main.calendar_day.view.*

class DayViewContainer(view: View) : ViewContainer(view) {
    val textView = view.calendarDayText
    
    // Without the kotlin android extensions plugin
    // val textView = view.findViewById<TextView>(R.id.calendarDayText)
}