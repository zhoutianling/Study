package com.zero.study.ui.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.zero.base.ext.toast
import com.zero.study.databinding.DialogFragmentBinding


/**
 * @author Admin
 * @date:2024/9/3 18:53
 * @path:com.zero.study.ui.dialog.DialogFragment
 */
class Dialog : DialogFragment() {

    private var _binding: DialogFragmentBinding? = null
    private val binding get() = _binding!!
    private var title: String = ""
    private var cancelText: String = ""
    private var confirmText: String = ""
    private var onClickListener: (String) -> Unit = {}
    private var cancelOnTouchOutSide: Boolean = true


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogFragmentBinding.inflate(inflater, container, false)
        requireDialog().window?.requestFeature(Window.FEATURE_NO_TITLE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvTitle.text = title
        binding.tvCancel.text = cancelText
        binding.tvConfirm.text = confirmText
        dialog?.setCanceledOnTouchOutside(cancelOnTouchOutSide)
        binding.tvCancel.setOnClickListener {
            dismiss()
        }
        binding.tvConfirm.setOnClickListener {
            val inputContent = binding.etInput.text.toString().trim()
            if (TextUtils.isEmpty(inputContent)) {
                view.context.toast("Input content is empty!")
                return@setOnClickListener
            }
            onClickListener.invoke(inputContent)
            dismiss()
        }

    }

    override fun onStart() {
        super.onStart()
        requireDialog().window?.let {
            val displayMetrics = resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            it.setLayout((screenWidth * 0.85).toInt(), LayoutParams.WRAP_CONTENT)
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    class Builder {
        private val dialogFragment = Dialog()

        fun setTitle(title: String): Builder {
            dialogFragment.title = title
            return this
        }

        fun setCancelText(cancelText: String): Builder {
            dialogFragment.cancelText = cancelText
            return this
        }

        fun setConfirmText(confirmText: String): Builder {
            dialogFragment.confirmText = confirmText
            return this
        }

        fun setOnClickListener(listener: (String) -> Unit): Builder {
            dialogFragment.onClickListener = listener
            return this
        }

        fun setCancelOnTouchOutSide(cancelOnTouchOutSide: Boolean): Builder {
            dialogFragment.cancelOnTouchOutSide = cancelOnTouchOutSide
            return this
        }

        fun build(): DialogFragment {
            return dialogFragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

