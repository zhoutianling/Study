package com.zero.study.net.api

import com.zero.base.net.bean.ApiResponse
import com.zero.study.net.entity.Article
import com.zero.base.net.bean.PageResponse
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Http接口，Retrofit的请求Service
 *
 * @author LTP  2022/3/21
 */
interface DomainApi {

    /** 获取每日一问列表分页数据 */
    @GET("wenda/list/{pageNo}/json")
    suspend fun getAskPageList(@Path("pageNo") pageNo: Int): ApiResponse<PageResponse<Article>>

}