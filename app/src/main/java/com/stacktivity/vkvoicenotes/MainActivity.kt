package com.stacktivity.vkvoicenotes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import by.kirich1409.viewbindingdelegate.viewBinding
import com.stacktivity.core.utils.FragmentManagers.replaceFragment
import com.stacktivity.vkvoicenotes.databinding.ActivityMainBinding
import com.stacktivity.voicenotes.ui.voicenotes.VoiceNotesFragment

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val binding by viewBinding(ActivityMainBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initUI(savedInstanceState)
    }

    private fun initUI(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            replaceFragment(supportFragmentManager, VoiceNotesFragment(), binding.container.id)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

        val count = supportFragmentManager.backStackEntryCount

        if (count == 0) {
            finish()
        }
    }
}