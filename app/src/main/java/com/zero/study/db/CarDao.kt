package com.zero.study.db

import androidx.room.Dao
import androidx.room.Query
import com.zero.study.db.entity.Car


/**
 * 数据访问对象
 */
@Dao
interface CarDao {
    // 查询
    @Query("SELECT * FROM t_car")
    fun queryAllBooks(): MutableList<Car>

}