package com.zero.study.db.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * @date:2024/10/30 18:13
 * @path:com.zero.study.db.entity.DogOwner
 */
data class DogOwner(
    @Embedded val user: User,
    @Relation(
        parentColumn = "id",
        entity = Dog::class,
        entityColumn = "ownId",
        projection = ["name"]
    )
    val dogs: List<String>
)
