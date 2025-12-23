package com.zero.study.ui.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zero.base.model.BaseViewModel
import com.zero.study.net.entity.CountryEntity


/**
 * @date:2024/8/29 15:24
 * @path:com.zero.study.ui.model.SplashViewModel
 */
class SplashViewModel : BaseViewModel() {
    private val country = MutableLiveData<CountryEntity>()

    fun getCountry(): LiveData<CountryEntity> {
        return country
    }
}