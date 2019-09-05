package com.lakehub.adherenceapp

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.lakehub.adherenceapp.fragments.FirstTutorialFragment
import com.lakehub.adherenceapp.fragments.SecondTutorialFragment
import com.lakehub.adherenceapp.fragments.ThirdTutorialFragment

class TutorialsPagerAdapter internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm) {
    private val count = 3
    override fun getItem(position: Int): Fragment {
        var fragment: Fragment? = null
        when(position) {
            0 -> fragment = FirstTutorialFragment()
            1 -> fragment = SecondTutorialFragment()
            2 -> fragment = ThirdTutorialFragment()
        }
        return fragment!!
    }

    override fun getCount(): Int {
        return count
    }
}