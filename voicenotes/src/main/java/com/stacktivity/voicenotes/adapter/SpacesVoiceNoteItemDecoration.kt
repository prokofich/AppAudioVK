package com.stacktivity.voicenotes.adapter

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration


class SpacesVoiceNoteItemDecoration(private val horizontalSpace: Int, private val verticalSpace: Int) : ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.set(horizontalSpace, verticalSpace, horizontalSpace, verticalSpace)
    }
}