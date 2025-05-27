package com.zero.study.ui.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zero.base.model.BaseViewModel
import com.zero.study.net.entity.CountryEntity
import com.zero.study.net.repository.CountryRepository
import kotlinx.coroutines.launch


/**
 * @date:2024/8/29 15:24
 * @path:com.zero.study.ui.model.SplashViewModel
 */
class SplashViewModel : BaseViewModel() {
    private val country = MutableLiveData<CountryEntity>()

    fun getCountry(): LiveData<CountryEntity> {
        return country
    }

    fun fetchCountry() {
        loading.value = true
        viewModelScope.launch {
            try {
                val countryEntity = CountryCase(CountryRepository()).invoke()
                country.postValue(countryEntity)
                loading.postValue(false)
            } catch (e: Exception) {
                loading.postValue(false)
                error.postValue(e.message)
            }
        }
    }

    inner class CountryCase(private val repository: CountryRepository) {
        suspend operator fun invoke(): CountryEntity {
            return repository.getCountry()
        }
    }
}