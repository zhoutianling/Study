package com.zero.study.net.repository

import com.zero.base.net.RetrofitManager
import com.zero.base.net.bean.ApiResponse
import com.zero.study.net.api.DomainApi
import com.zero.study.net.entity.Article
import com.zero.base.net.bean.PageResponse

/**
 * @date:2024/9/23 20:20
 * @path:com.zero.study.net.repository.DomainRepository
 */
object DomainRepository : DomainApi {
    private val domainApi by lazy { RetrofitManager.getService(DomainApi::class.java) }
    override suspend fun getAskPageList(pageNo: Int): ApiResponse<PageResponse<Article>> {
        return domainApi.getAskPageList(pageNo)
    }
}