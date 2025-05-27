package com.zero.study.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "t_dog")
data class Dog(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    @ColumnInfo(name = "ownId")
    var ownId: Int,
    @ColumnInfo(name = "age")
    var age: Int?,
    @ColumnInfo(name = "name")
    var name: String
)
