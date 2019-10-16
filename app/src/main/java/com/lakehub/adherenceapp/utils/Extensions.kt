package com.lakehub.adherenceapp.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.lakehub.adherenceapp.R

internal fun TextView.setTextColorRes(@ColorRes color: Int) = setTextColor(ContextCompat.getColor(context, color))

fun View.makeVisible() {
    visibility = View.VISIBLE
}

fun View.makeInVisible() {
    visibility = View.INVISIBLE
}

fun View.makeGone() {
    visibility = View.GONE
}

fun dpToPx(dp: Int, context: Context): Int =
    TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(),
        context.resources.displayMetrics
    ).toInt()

fun AppCompatActivity.showToast(view: View) {
    val toast = Toast(this)
    toast.view = view
    toast.setGravity(Gravity.BOTTOM, 0, 30)
    toast.duration = Toast.LENGTH_SHORT
    toast.show()
}

fun AppCompatActivity.showWarning(message: String) {
    val view = layoutInflater.inflate(R.layout.warning, null)
    val textView: TextView = view.findViewById(R.id.message)
    textView.text = message
    this.showToast(view)
}

fun AppCompatActivity.showNetworkError() {
    val view = layoutInflater.inflate(R.layout.network_error, null)
    showToast(view)
}

fun AppCompatActivity.showSuccess(message: String) {
    val view = layoutInflater.inflate(R.layout.normal_toast, null)
    val textView: TextView = view.findViewById(R.id.message)
    textView.text = message
    this.showToast(view)
}

fun AppCompatActivity.isIntentCallable (intent: Intent): Boolean {
    val list = packageManager.queryIntentActivities(
        intent,
        PackageManager.MATCH_DEFAULT_ONLY
    )
    return list.size > 0
}