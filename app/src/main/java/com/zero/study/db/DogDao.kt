package com.zero.study.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.zero.study.db.entity.Dog


/**
 * 数据访问对象
 */
@Dao
interface DogDao {
    // 查询
//    @Query("SELECT * FROM t_dog")
//    suspend fun queryDogs(): MutableList<Dog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDog(vararg dog: Dog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDogList(list: MutableList<Dog>)
}