package com.andriishchehel.bookreader4u.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Book(
    val bookId: String,
    val image: String,
    val title: String,
    val author: String,
    val description: String,
    val genres: List<String>,
    val rating: Float = 0f,
    val reviews: List<String>,
    val starRatings: Map<Int, Int>,
    val reviewCount: Long = 0L,
    val fileUrl: String,
): Parcelable
