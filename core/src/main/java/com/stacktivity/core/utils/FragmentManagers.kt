package com.stacktivity.core.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

object FragmentManagers {

    fun addFragment(fragmentManager: FragmentManager, fragment: Fragment, frameId: Int) {
        fragmentManager.beginTransaction()
            .add(frameId, fragment)
            .commit()
    }

    fun replaceFragment(fragmentManager: FragmentManager, fragment: Fragment, frameId: Int) {
        fragmentManager.beginTransaction()
            .replace(frameId, fragment)
            .addToBackStack(null)
            .commit()
    }
}