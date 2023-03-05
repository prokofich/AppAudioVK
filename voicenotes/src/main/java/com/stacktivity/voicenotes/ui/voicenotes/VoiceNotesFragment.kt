package com.stacktivity.voicenotes.ui.voicenotes

import android.Manifest.permission.RECORD_AUDIO
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import by.kirich1409.viewbindingdelegate.viewBinding
import com.stacktivity.core.utils.FragmentManagers.replaceFragment
import com.stacktivity.voicenotes.R
import com.stacktivity.voicenotes.R.string
import com.stacktivity.voicenotes.R.layout.voice_notes_screen
import com.stacktivity.voicenotes.R.dimen.VoiceNoteItem_horizontal_space
import com.stacktivity.voicenotes.R.dimen.VoiceNoteItem_vertical_space
import com.stacktivity.voicenotes.adapter.SpacesVoiceNoteItemDecoration
import com.stacktivity.voicenotes.adapter.VoiceNoteListAdapter
import com.stacktivity.voicenotes.databinding.VoiceNotesScreenBinding
import com.stacktivity.voicenotes.ui.file_rename.UserFileRenameDialog
import com.stacktivity.voicenotes.ui.login.LoginFragment
import com.stacktivity.voicenotes.ui.voicenotes.viewmodel.VoiceNotesViewModel
import com.stacktivity.voicenotes.utils.launchWhenStarted
import com.vk.api.sdk.VK
import kotlinx.coroutines.flow.onEach


class VoiceNotesFragment : Fragment(voice_notes_screen) {

    private val binding by viewBinding(VoiceNotesScreenBinding::bind)

    private val viewModel by viewModels<VoiceNotesViewModel> {
        VoiceNotesViewModelFactory(requireContext())
    }

    private val adapter by lazy { VoiceNoteListAdapter { voiceNoteItem ->
        viewModel.onMediaItemClicked(voiceNoteItem)
    }}

    private var testMode = false

    private var recordingFileName: String? = null
    private val recorder = registerForActivityResult(RequestPermission()) { granted ->
        when {
            granted -> {
                recordingFileName = viewModel.recordAudio()
            }
            shouldShowRequestPermissionRationale(RECORD_AUDIO).not() -> {
                // пользователь поставил галочку Don't ask again
            }
        }
    }

    companion object {
        const val KEY_TEST_MODE = "keyTest"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().title = getString(string.voice_notes_title)

        testMode = arguments?.getBoolean(KEY_TEST_MODE, false) ?: false

        val preferences = requireContext()
            .getSharedPreferences(requireActivity().packageName, Context.MODE_PRIVATE)

        val tokenExpired = preferences.getBoolean("TokenExpired", false)

        if ((VK.isLoggedIn().not() || tokenExpired) && testMode.not()) {
            showLoginScreen()
            return null
        }

        preferences.registerOnSharedPreferenceChangeListener { prefs, key ->
            if (key ==  "TokenExpired" && prefs.getBoolean(key, false)) {
                showLoginScreen()
            }
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initUI()
        setupObservers()
        viewModel.fetchItems()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (view == null) return

        if (viewModel.audioRecording.value) {
            viewModel.stopRecord()
        }
    }

    private fun initUI() {
        initNotesView()
    }

    private fun showLoginScreen() {
        requireActivity().supportFragmentManager.popBackStack()
        replaceFragment(requireActivity().supportFragmentManager, LoginFragment(), R.id.container)
    }

    private fun initNotesView() {
        val horizontalSpace = resources.getDimension(VoiceNoteItem_horizontal_space).toInt()
        val verticalSpace = resources.getDimension(VoiceNoteItem_vertical_space).toInt()

        binding.voiceNoteListRv.apply {
            adapter = this@VoiceNotesFragment.adapter

            addItemDecoration(
                SpacesVoiceNoteItemDecoration(
                    horizontalSpace = horizontalSpace,
                    verticalSpace = verticalSpace
                )
            )
        }
    }

    private fun setupObservers() {
        setupButtonsListeners()
        setupAdapterObservers()

        viewModel.audioRecording.onEach { audioRecording ->
            binding.btnAddVoiceNote.isChecked = audioRecording
        }.launchWhenStarted(lifecycleScope)

        viewModel.voiceNotesFlow.onEach { voiceNotes ->
            adapter.submitList(voiceNotes)
        }.launchWhenStarted(lifecycleScope)
    }

    private fun setupButtonsListeners() {
        binding.addVoiceNoteOverlay.apply {
            setOnClickListener {
                it.isEnabled = false
                if (viewModel.audioRecording.value) {
                    viewModel.stopRecord()
                    showFileRenameDialog()
                } else {
                    if (shouldShowRequestPermissionRationale(RECORD_AUDIO)) {
                        openSettingsScreen()
                    } else {
                        recorder.launch(RECORD_AUDIO)
                    }
                }
                it.isEnabled = true
            }
        }
    }

    private fun setupAdapterObservers() {
        viewModel.playbackState
            .onEach(adapter::onPlaybackStateChanged)
            .launchWhenStarted(lifecycleScope)

        adapter.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                binding.voiceNoteListRv.layoutManager?.scrollToPosition(positionStart)
            }
        })
    }

    private fun showFileRenameDialog() {
        val requestKey = "fileName"
        val dialog = UserFileRenameDialog.newInstance(recordingFileName!!)

        dialog.show(childFragmentManager, requestKey)
        childFragmentManager.setFragmentResultListener(requestKey, this) { _, result ->
            val newName = result.getString(requestKey) ?: recordingFileName!!
            viewModel.applyRecordedAudioName(recordingFileName!!, newName)
        }
    }

    private fun openSettingsScreen() {
        startActivity(Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.parse("package:${requireActivity().packageName}")
        })
    }
}