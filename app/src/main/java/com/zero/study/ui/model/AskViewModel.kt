package com.zero.study.ui.model

import androidx.lifecycle.MutableLiveData
import com.zero.base.ext.handleRequest
import com.zero.base.ext.launch
import com.zero.base.model.BaseViewModel
import com.zero.study.net.entity.Article
import com.zero.base.net.bean.PageResponse
import com.zero.study.net.repository.DomainRepository

class AskViewModel : BaseViewModel() {

    /** 每日一问分页列表LiveData */
    val articlePageListLiveData = MutableLiveData<PageResponse<Article>?>()


    /** 请求每日一问分页列表 */
    fun fetchAskPageList(pageNo: Int = 1) {
        launch({
            handleRequest(DomainRepository.getAskPageList(pageNo), { articlePageListLiveData.value = it.data })
        })

    }

}