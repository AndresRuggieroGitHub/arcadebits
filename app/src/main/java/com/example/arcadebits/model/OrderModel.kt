package com.example.arcadebits.model

data class OrderModel(
    val orderNumber: String,
    val date: String,
    val total: String,
    val status: String,
    val thumbnailResId: Int
)