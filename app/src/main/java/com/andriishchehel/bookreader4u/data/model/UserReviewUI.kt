package com.andriishchehel.bookreader4u.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserReviewUI(
    val reviewId: String,
    val username: String,
    val profileImage: String,
    val timestamp: Timestamp,
    val rating: Float,
    val text: String
): Parcelable