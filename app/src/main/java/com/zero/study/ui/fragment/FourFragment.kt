package com.zero.study.ui.fragment

import android.annotation.SuppressLint
import android.transition.TransitionManager
import android.util.Log
import androidx.constraintlayout.widget.ConstraintSet
import com.zero.base.bus.RxBus
import com.zero.base.ext.log
import com.zero.base.fragment.BaseFragment
import com.zero.study.R
import com.zero.study.databinding.FragmentFourBinding
import com.zero.study.event.MsgEvent
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

class FourFragment : BaseFragment<FragmentFourBinding>(FragmentFourBinding::inflate) {
    private var mNorSet = ConstraintSet()
    private var mBigSet = ConstraintSet()
    private var isNormal = false

    override fun initView() {
        val mBaseLayout = binding.constraintSet
        mNorSet.clone(mBaseLayout)
        mBigSet.load(context, R.layout.fragment_anim_large)
        binding.imageView.setOnClickListener {
            TransitionManager.beginDelayedTransition(mBaseLayout)
            if (isNormal) {
                mNorSet.applyTo(mBaseLayout)
            } else {
                mBigSet.applyTo(mBaseLayout)
            }
            isNormal = !isNormal
        }
    }

    @SuppressLint("CheckResult")
    override fun initData() {
        RxBus.getInstance().toObservable(MsgEvent::class.java).subscribe { event ->
            event.msg.log("zzz")
        }
    }

    override fun setListener() {
    }

    companion object {
        fun newInstance() = FourFragment()
    }
}