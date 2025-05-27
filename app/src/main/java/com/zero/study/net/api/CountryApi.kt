package com.zero.study.net.api

import com.zero.study.net.entity.CountryEntity
import retrofit2.http.GET

/**
 * @date:2024/8/29 15:10
 * @path:com.zero.study.net.api.CountryApiService
 */
interface CountryApi {
    @GET(" ")
    suspend fun getCountry(): CountryEntity
}
