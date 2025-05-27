package com.zero.study.ui.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {
    private val mText = MutableLiveData<String>()

    init {
        mText.value = "Home"
    }

    val text: LiveData<String>
        get() = mText

}