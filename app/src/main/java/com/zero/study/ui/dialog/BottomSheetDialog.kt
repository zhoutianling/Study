package com.zero.study.ui.dialog

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zero.base.ext.toast
import com.zero.study.R
import com.zero.study.databinding.BottomDialogFragmentBinding


/**
 * @author Admin
 * @date:2024/9/3 18:53
 * @path:com.zero.study.ui.dialog.DialogFragment
 */
class BottomSheetDialog : BottomSheetDialogFragment() {

    private var _binding: BottomDialogFragmentBinding? = null
    private val binding get() = _binding!!
    private var title: String = ""
    private var cancelText: String = ""
    private var confirmText: String = ""
    private var onClickListener: (String) -> Unit = {}
    private var cancelOnTouchOutSide: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogStyle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomDialogFragmentBinding.inflate(inflater, container, false)
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
        val behavior = BottomSheetBehavior.from(view.parent as View)
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        Log.d("zzz", "onStateChanged: STATE_EXPANDED")
                    }

                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        Log.d("zzz", "onStateChanged: STATE_COLLAPSED")
                    }

                    BottomSheetBehavior.STATE_HIDDEN -> {
                        Log.d("zzz", "onStateChanged: STATE_HIDDEN")
                    }

                    BottomSheetBehavior.STATE_DRAGGING -> {
                    }

                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                    }

                    BottomSheetBehavior.STATE_SETTLING -> {
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // 滑动过程中（slideOffset：0-1，0是收起，1是完全展开）
            }
        })
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    class Builder {
        private val bottomDialog = BottomSheetDialog()

        fun setTitle(title: String): Builder {
            bottomDialog.title = title
            return this
        }

        fun setCancelText(cancelText: String): Builder {
            bottomDialog.cancelText = cancelText
            return this
        }

        fun setConfirmText(confirmText: String): Builder {
            bottomDialog.confirmText = confirmText
            return this
        }

        fun setOnClickListener(listener: (String) -> Unit): Builder {
            bottomDialog.onClickListener = listener
            return this
        }

        fun setCancelOnTouchOutSide(cancelOnTouchOutSide: Boolean): Builder {
            bottomDialog.cancelOnTouchOutSide = cancelOnTouchOutSide
            return this
        }

        fun build(): DialogFragment {
            return bottomDialog
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

