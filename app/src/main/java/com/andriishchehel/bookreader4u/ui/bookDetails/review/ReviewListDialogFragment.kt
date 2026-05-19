package com.andriishchehel.bookreader4u.ui.bookDetails.review

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.andriishchehel.bookreader4u.data.model.Review
import com.andriishchehel.bookreader4u.databinding.DialogFragmentReviewsListBinding
import kotlinx.coroutines.launch

class ReviewListDialogFragment(private val bookId: String) : DialogFragment() {

    private val viewModel: ReviewListViewModel by viewModels()
    private lateinit var binding: DialogFragmentReviewsListBinding
    private lateinit var adapter: ReviewAdapter
    private val reviews = emptyList<Review>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.ThemeOverlay)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogFragmentReviewsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        getReviews()
    }

    private fun setupToolbar() {
        binding.toolbarReview.setNavigationOnClickListener { dismiss() }
    }

    private fun setupRecyclerView() {
        adapter = ReviewAdapter(reviews)
        binding.recyclerViewHome.adapter = adapter
    }

    private fun getReviews() {
        lifecycleScope.launch {
            viewModel.getReviews(bookId).fold(
                onSuccess = { reviews ->
                    adapter.setReviews(reviews)
                },
                onFailure = { exception ->
                    Toast.makeText(
                        context,
                        "Error fetching reviews: ${exception.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    adapter.setReviews(emptyList())
                }
            )
        }
    }


    companion object {
        const val TAG = "ReviewListDialog"
    }
}