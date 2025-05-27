package com.zero.study.data

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.zero.study.db.DbManager
import com.zero.study.db.UserDao
import com.zero.study.db.entity.User
import com.zero.study.ui.holder.LoadStateViewHolder

/**
 * @date:2024/10/28 11:39
 * @path:com.zero.study.data.UserPagingSource
 */
class UserPagingSource : PagingSource<Int, User>() {
    private var maxPage = 2
    private val userDao: UserDao by lazy { DbManager.db.userDao() }

    /**
     *  初始加载的页码;  暂且返回 1 或 null
     */
    override fun getRefreshKey(state: PagingState<Int, User>): Int {
        return 1
    }

    /**
     * 加载数据的方法
     */
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, User> {
        val nextPage = params.key ?: 1
        if (nextPage>=maxPage){
            return LoadResult.Error(LoadStateViewHolder.NoMoreException())
        }
        val size = params.loadSize
        Log.d("zzz", "size: $size,nextPage:$nextPage")
        val response = userDao.getUserListByPage()
        return LoadResult.Page(
            data = response,
            prevKey = null,// Only paging forward.  只向后加载就给 null
            nextKey = nextPage + 1,//nextKey 下一页页码;  尾页给 null;  否则当前页码加1
        )
    }
}