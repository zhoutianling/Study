package com.zero.study.net.repository

import com.zero.base.net.RetrofitManager
import com.zero.study.net.api.CountryApi
import com.zero.study.net.entity.CountryEntity

/**
 * @date:2024/8/29 15:15
 * @path:com.zero.study.net.repository.CountryRespository
 */
class CountryRepository : CountryApi {
    private val countryApi by lazy { RetrofitManager.getService(CountryApi::class.java) }
    override suspend fun getCountry(): CountryEntity {
        return countryApi.getCountry()
    }
}