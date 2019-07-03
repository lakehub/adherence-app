package com.lakehub.adherenceapp

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class TutorialsPagerAdapter internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm) {
    private val count = 3
    override fun getItem(position: Int): Fragment {
        var fragment: Fragment? = null
        when(position) {
            0 -> fragment = FirstTutorialFragment()
            1 -> fragment = FirstTutorialFragment()
            2 -> fragment = FirstTutorialFragment()
        }
        return fragment!!
    }

    override fun getCount(): Int {
        return count
    }

    override fun getPageTitle(position: Int): CharSequence? {
        var pageTitle: String? = null
        when(position) {
            0 -> pageTitle = "chats"
            1 -> pageTitle = "forum"
        }
        return pageTitle?.toUpperCase()
    }
}