package com.andriishchehel.bookreader4u.ui.bookDetails.review

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.andriishchehel.bookreader4u.data.model.UserReviewUI
import com.andriishchehel.bookreader4u.databinding.DialogFragmentReviewEditBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ReviewSubmitDialogFragment(
    private var reviewScore: Float,
    private val bookId: String,
    private var reviewText: String,
    private var existingReviewId: String,
) : DialogFragment() {

    private lateinit var binding: DialogFragmentReviewEditBinding
    private val viewModel: ReviewSubmitViewModel by viewModels()

    private val startingScore = reviewScore

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ratingBarReview.rating = reviewScore
        binding.editTextReview.setText(reviewText)

        binding.toolbarReview.setNavigationOnClickListener { dismiss() }
        binding.buttonConfirmReview.setOnClickListener { submitReview() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.ThemeOverlay)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogFragmentReviewEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun submitReview() {
        reviewScore = binding.ratingBarReview.rating
        reviewText = binding.editTextReview.text.toString()

        if (reviewScore == 0f) {
            showSnackbar("Please rate this book from 1 to 5")
        }
        lifecycleScope.launch {
            val review = viewModel.submitUserReview(
                reviewScore,
                reviewText,
                bookId,
                existingReviewId,
                startingScore
            ).getOrElse {
                showSnackbar(it.localizedMessage ?: "Unknown message")
                return@launch
            }
            sendResultBack(review)
        }
    }

    private fun sendResultBack(review: UserReviewUI) {
        val bundle = Bundle().apply {
            putParcelable("review", review)
        }
        parentFragmentManager.setFragmentResult("review_result", bundle)
        dismiss()
    }

    private fun showSnackbar(message: String) {
        view?.let { Snackbar.make(it, message, Snackbar.LENGTH_LONG).show() }
    }

    companion object {
        const val TAG = "ReviewEditDialog"
    }
}