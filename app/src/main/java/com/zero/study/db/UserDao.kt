package com.zero.study.db

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.zero.study.db.entity.DogOwner
import com.zero.study.db.entity.User


/**
 * 数据访问对象
 */
@Dao
interface UserDao {
    /**
     * 查询
     */
    @Query("SELECT * FROM t_user")
    suspend fun queryAllUser(): MutableList<User>

    @Transaction
    @Query("SELECT * FROM t_user")
    suspend fun queryUserDogs(): MutableList<DogOwner>

    /**
     *配合LiveData实时查询
     */
    @Query("SELECT * FROM t_user")
    fun getAllUsers(): LiveData<MutableList<User>>

    /**
     *配合Paging分页查询
     */
    @Query("SELECT * FROM t_user")
    suspend fun getUserListByPage(): MutableList<User>

    /**
     * 按年龄查询
     */
    @Query("SELECT * FROM t_user WHERE age > :minAge")
    fun loadAllUsersOlderThan(minAge: Int): Array<User>

    /**
     * 根据姓名参数查询
     */
    @Query("SELECT * FROM t_user WHERE name = :name")
    suspend fun queryUserByName(name: String): User?

    /**
     * 根据姓名、年龄查询
     */
    @Query("SELECT * FROM t_user WHERE name = :name")
    suspend fun queryUserByParam(name: String): User?

    /**
     * 按名字模拟查询
     */
    @Query("SELECT * FROM t_user WHERE name LIKE :key")
    suspend fun queryFuzzyByName(key: String): MutableList<User>

    // 添加单条数据
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addUser(vararg user: User)

    // 添加批量数据 OnConflictStrategy :当冲突时: ABORT,取消;  REPLACE,替换;  IGNORE,忽略;
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserList(list: MutableList<User>)

    // 更新某一个数据
    @Update
    suspend fun updateUser(vararg user: User): Int

    //更新所有数据
    @Query("UPDATE t_user set age='50'")
    suspend fun updateAll(): Int

    //删除某一个数据
    @Delete
    suspend fun deleteSingle(vararg user: User)

    @Transaction
    suspend fun upGradeUser(update: User) {
        deleteSingle(update)
        addUser(update)
    }

    //删除表里所有数据
    @Query("DELETE FROM t_user")
    suspend fun deleteAllUser(): Int
}