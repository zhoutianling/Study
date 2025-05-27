package com.zero.study.ipc

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "t_book")
data class Book(@PrimaryKey(autoGenerate = true) var id: Int = 0, @ColumnInfo(name = "name") var name: String, @ColumnInfo(name = "author") var author: String = "") : Parcelable
