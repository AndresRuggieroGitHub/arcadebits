package com.example.arcadebits.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date


data class ProductModel(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var originalPrice: Double = 0.0,
    var salePrice: Double? = null,
    var categories: List<String> = emptyList(),
    var image: String = "",
    var inStock: Boolean = true,
    @ServerTimestamp var createdAt: Date? = null,


    @Transient var isFavorite: Boolean = false

)

