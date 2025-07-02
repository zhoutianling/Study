package com.zero.study.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.DialogFragment

/**
 * @author Admin
 */
class ProgressDialog : DialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val progressBar = ProgressBar(this.activity)
        progressBar.isIndeterminate = true
        return progressBar
    }

    override fun onStart() {
        super.onStart()
        if (dialog != null && dialog?.window != null) {
            dialog?.setCanceledOnTouchOutside(true)
            dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }
}
