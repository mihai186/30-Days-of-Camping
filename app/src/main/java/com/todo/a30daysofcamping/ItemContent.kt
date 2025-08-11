package com.todo.a30daysofcamping

data class ItemContent(
    val day: Int,
    val fullDescription: Int,
    val image: Int? = null,
    val videoLink: String? = null
)