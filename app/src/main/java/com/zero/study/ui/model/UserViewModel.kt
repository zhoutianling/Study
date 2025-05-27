package com.zero.study.ui.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.zero.study.data.UserPagingSource
import com.zero.study.db.entity.User
import kotlinx.coroutines.flow.Flow

class UserViewModel : ViewModel() {

    val userFlow: Flow<PagingData<User>> = Pager(config = PagingConfig(pageSize = 3, prefetchDistance = 1, initialLoadSize = 1, maxSize = 500)) {
        UserPagingSource()
    }.flow.cachedIn(viewModelScope)
}