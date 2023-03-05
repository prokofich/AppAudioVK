package com.stacktivity.voicenotes.ui.file_rename

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.stacktivity.voicenotes.R.layout.voice_note_rename_screen
import com.stacktivity.voicenotes.databinding.VoiceNoteRenameScreenBinding
import com.tunjid.androidx.core.delegates.fragmentArgs

class UserFileRenameDialog private constructor(): BottomSheetDialogFragment() {

    private val binding by viewBinding(VoiceNoteRenameScreenBinding::bind)

    private var defaultName by fragmentArgs<String>()


    companion object {
        fun newInstance(defaultName: String) = UserFileRenameDialog().apply {
            this.defaultName = defaultName
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return layoutInflater.inflate(voice_note_rename_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initUI()
        setupObservers()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        onCancel()
    }


    private fun initUI() {
        binding.fileName.setText(defaultName)
    }

    private fun setupObservers() {
        binding.btnCancel.setOnClickListener {
            onCancel()
            dismiss()
        }

        binding.btnOk.setOnClickListener {
            setResult(binding.fileName.text.toString())
        }
    }

    private fun setResult(result: String) {
        val bundle = Bundle(1)
        bundle.putString(tag, result)
        parentFragmentManager.setFragmentResult(tag!!, bundle)
        dismiss()
    }

    private fun onCancel() {
        setResult(defaultName)
    }
}