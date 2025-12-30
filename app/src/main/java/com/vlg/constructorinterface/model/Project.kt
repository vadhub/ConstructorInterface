package com.vlg.constructorinterface.model

data class Project(
    val id: String = System.currentTimeMillis().toString(),
    val name: String,
    val path: String,
    val createdAt: Long = System.currentTimeMillis()
)