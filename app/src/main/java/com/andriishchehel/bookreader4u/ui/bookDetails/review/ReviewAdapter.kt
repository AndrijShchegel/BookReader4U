package com.andriishchehel.bookreader4u.ui.bookDetails.review

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.andriishchehel.bookreader4u.R
import com.andriishchehel.bookreader4u.data.model.Review
import com.andriishchehel.bookreader4u.databinding.ItemReviewBinding

class ReviewAdapter(private var reviews: List<Review>) :
    RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    inner class ReviewViewHolder(private val binding: ItemReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(review: Review) {
            with(binding) {
                if (review.reviewerAvatar.isNotEmpty()) {
                    ivReviewProfileRound.load(review.reviewerAvatar) {
                        crossfade(true)
                        listener(
                            onStart = {
                                progressBar.visibility = View.VISIBLE
                            },
                            onSuccess = { _, _ ->
                                progressBar.visibility = View.GONE
                            }
                        )
                        error(R.drawable.default_profile_image_40)
                    }
                }

                tvReviewUsername.text = review.reviewerName
                rbReviewIndicator.rating = review.rating
                tvReviewDate.text = review.date
                tvReviewText.text = review.text
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemReviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReviewViewHolder(binding)
    }

    override fun getItemCount() = reviews.size

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(reviews[position])
    }


    fun setReviews(newReviews: List<Review>) {
        this.reviews = newReviews
        notifyDataSetChanged()
    }
}