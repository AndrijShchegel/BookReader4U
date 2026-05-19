package com.andriishchehel.bookreader4u.ui.bookDetails

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import coil.load
import com.andriishchehel.bookreader4u.R
import com.andriishchehel.bookreader4u.data.model.Book
import com.andriishchehel.bookreader4u.data.model.UserReviewUI
import com.andriishchehel.bookreader4u.databinding.ActivityBookDetailsBinding
import com.andriishchehel.bookreader4u.ui.bookDetails.review.ReviewListDialogFragment
import com.andriishchehel.bookreader4u.ui.bookDetails.review.ReviewSubmitDialogFragment
import com.andriishchehel.bookreader4u.ui.reader.ReaderActivity
import com.andriishchehel.bookreader4u.ui.search.SearchGenresActivity
import com.andriishchehel.bookreader4u.util.GenresHelper
import com.andriishchehel.bookreader4u.util.extentions.makeExpandable
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class BookDetailsActivity : AppCompatActivity() {

    private val viewModel: BookDetailsViewModel by viewModels()
    private lateinit var binding: ActivityBookDetailsBinding
    private lateinit var book: Book

    private var reviewText = ""
    private var existingReviewId = ""
    private var reviewScore = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbarBookDescription) { v, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
            v.updatePadding(top = topInset)
            insets
        }
        extractBookFromIntent()
        setupUI()
        setupListeners()
        setupFragmentManagers()
        displayReviews()

        lifecycleScope.launch {
            viewModel.getUserReview(book.bookId).fold(
                onSuccess = {
                    showUserReview(it)
                },
                onFailure = {
                    if (it !is NoSuchElementException) {
                        showToast(it.localizedMessage ?: "Unknown message")
                    }
                }
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_book_details, menu)

        val saveItem = menu.findItem(R.id.button_book_details_saved)
        val moreItem = menu.findItem(R.id.menu_other_options)

        val whiteColor = ContextCompat.getColor(this, android.R.color.white)
        saveItem.icon?.setTint(whiteColor)
        moreItem.icon?.setTint(whiteColor)
        lifecycleScope.launch {
            viewModel.isSaved(book.bookId).onSuccess {
                val iconRes = if (it) R.drawable.bookmark_saved else R.drawable.icon_bookmark_border
                val icon = ContextCompat.getDrawable(this@BookDetailsActivity, iconRes)
                icon?.setTint(whiteColor)
                saveItem.setIcon(icon)
            }.onFailure {
                showToast(it.localizedMessage ?: "Unknown error")
            }
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.button_book_details_saved -> {
                lifecycleScope.launch {
                    viewModel.changeState(book.bookId).onSuccess {
                        val whiteColor = ContextCompat.getColor(this@BookDetailsActivity, android.R.color.white)
                        val iconRes = if (it == "saved") R.drawable.bookmark_saved else R.drawable.icon_bookmark_border
                        val icon = ContextCompat.getDrawable(this@BookDetailsActivity, iconRes)
                        icon?.setTint(whiteColor)
                        item.setIcon(icon)
                    }.onFailure {
                        showToast(it.localizedMessage ?: "Failed to update saved state")
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun extractBookFromIntent() {
        book = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("Book", Book::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("Book") as? Book
        } ?: run {
            showToast("Error: book not found")
            finish()
            return
        }
        viewModel.computeRatingProgress(book)
    }

    private fun setupUI() {
        with(binding) {
            ivBookDescriptionCover.load(book.image)
            tvBookDescriptionTitle.text = book.title
            tvBookDescriptionAuthor.text = book.author
            rbBookDescriptionRating.rating = book.rating
            tvRatingCount.text = "(${book.reviewCount})"
            tvBookDescriptionDescription.text = book.description

            if (book.genres.isNotEmpty()) {
                setupCategoryChips(book.genres)
            } else {
                tvBookDescriptionGenres.visibility = View.GONE
                chipGroupBookDescriptionGenres.visibility = View.GONE
            }
        }

        // Set the toolbar as the action bar
        setSupportActionBar(binding.toolbarBookDescription)
        // Enable the back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbarBookDescription.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupCategoryChips(genres: List<String>) {
        genres.forEach { genre ->
            val genreName = GenresHelper.getGenreName(this, genre)
            binding.chipGroupBookDescriptionGenres.addView(
                Chip(this).apply {
                    text = genreName
                    isCheckable = false
                    setOnClickListener {
                        val intent = Intent(context, SearchGenresActivity::class.java)
                        intent.putExtra("Genre", genre)
                        startActivity(intent)
                    }
                }
            )
        }
    }

    private fun setupListeners() {
        binding.buttonBookDescriptionRead.setOnClickListener {
            if (book.fileUrl.isEmpty()) {
                showToast("Not specified file path")
                return@setOnClickListener
            }
            lifecycleScope.launch {
                viewModel.markAsStarted(book.bookId)
            }

            val intent = Intent(this, ReaderActivity::class.java).apply {
                putExtra("epubUrl", book.fileUrl)
                putExtra("bookId", book.bookId)
            }
            startActivity(intent)
        }

        binding.rbBookDescriptionRatingInteract.setOnRatingBarChangeListener { _, rating, fromUser ->
            if (fromUser) {
                val dialog =
                    ReviewSubmitDialogFragment(rating, book.bookId, reviewText, existingReviewId)
                dialog.show(supportFragmentManager, ReviewSubmitDialogFragment.TAG)
                binding.rbBookDescriptionRatingInteract.rating = 0f
            }
        }

        binding.rbBookDescriptionRatingInteract.setOnRatingBarChangeListener { _, rating, fromUser ->
            if (fromUser) {
                val dialog =
                    ReviewSubmitDialogFragment(rating, book.bookId, reviewText, existingReviewId)
                dialog.show(supportFragmentManager, ReviewSubmitDialogFragment.TAG)
                binding.rbBookDescriptionRatingInteract.rating = 0f
            }
        }

        binding.buttonBookDescriptionReview.setOnClickListener {
            val dialog =
                ReviewSubmitDialogFragment(reviewScore, book.bookId, reviewText, existingReviewId)
            dialog.show(supportFragmentManager, ReviewSubmitDialogFragment.TAG)
        }

        binding.ibBookDescriptionMenuDeleteReview.setOnClickListener { view ->
            PopupMenu(this@BookDetailsActivity, view).apply {
                menuInflater.inflate(R.menu.menu_review_delete, menu)

                setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.action_delete_review -> {
                            lifecycleScope.launch {
                                viewModel.deleteUserReview(existingReviewId).getOrElse {
                                    showToast(it.message ?: "Unknown message")
                                    return@launch
                                }
                                hideUserReviewSection()
                            }
                            true
                        }

                        else -> false
                    }
                }
                show()
            }
        }

        binding.tvBookDescriptionMoreReviews.setOnClickListener {
            val dialog = ReviewListDialogFragment(book.bookId)
            dialog.show(supportFragmentManager, ReviewListDialogFragment.TAG)
        }
    }

    private fun hideUserReviewSection() {
        binding.layoutBookDescriptionUserReview.visibility = View.GONE
        binding.rbBookDescriptionRatingInteract.visibility = View.VISIBLE
        binding.tvBookDescriptionRating.text = getString(R.string.rate_this_book)
        binding.buttonBookDescriptionReview.text = getString(R.string.button_create_review)
        reviewText = ""
        existingReviewId = ""
        reviewScore = 0f
    }

    private fun showUserReview(review: UserReviewUI) {
        with(binding) {
            // Make review section visible
            layoutBookDescriptionUserReview.visibility = View.VISIBLE
            rbBookDescriptionRatingInteract.visibility = View.GONE

            // Display proper text for button and title
            tvBookDescriptionRating.text = getString(R.string.your_review)
            buttonBookDescriptionReview.text = getString(R.string.button_edit_review)

            // Display name of the user
            tvBookDescriptionUsername.text = review.username

            // Display review score
            rbBookDescriptionUserReviewIndicator.rating = review.rating

            // Get and format timestamp of the review
            tvBookDescriptionUserReviewDate.text = SimpleDateFormat(
                "dd MMM yyyy",
                Locale.getDefault()
            ).format(review.timestamp.toDate())

            // Load profile image
            ivBookDescriptionProfileRound.load(review.profileImage) {
                crossfade(true)
                placeholder(R.drawable.default_profile_image_40)
                error(R.drawable.default_profile_image_40)
            }

            // Display review text
            tvBookDescriptionUserReviewText.text = review.text
            tvBookDescriptionUserReviewText.makeExpandable()
        }

        // Store review text and ID for editing
        reviewText = review.text
        existingReviewId = review.reviewId
        reviewScore = review.rating
    }

    private fun setupFragmentManagers() {
        supportFragmentManager.setFragmentResultListener("review_result", this) { _, bundle ->
            val review = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getParcelable("review", UserReviewUI::class.java)
            } else {
                @Suppress("DEPRECATION")
                bundle.getParcelable("review")
            } ?: return@setFragmentResultListener


            lifecycleScope.launch {
                val userData = viewModel.getUserData().getOrElse {
                    showToast(it.message ?: "Unknown message")
                    return@launch
                }
                showUserReview(
                    UserReviewUI(
                        username = userData.name,
                        profileImage = userData.profileImage,
                        reviewId = review.reviewId,
                        timestamp = review.timestamp,
                        rating = review.rating,
                        text = review.text,
                    )
                )
            }

        }
    }


    private fun displayReviews() {
        with(binding) {
            if (book.reviewCount != 0L) {
                llBookDescriptionRatingDetails.visibility = View.VISIBLE
                llBookDescriptionReviewContainer.visibility = View.VISIBLE
                loadRatingDetails()
                loadLatestReviews()

                tvBookDescriptionAverageRating.text = when {
                    book.reviewCount == 1L -> getString(
                        R.string.rating_single,
                        book.rating,
                        book.reviewCount
                    )

                    book.reviewCount in 2..4 -> getString(
                        R.string.rating_two_to_four,
                        book.rating,
                        book.reviewCount
                    )

                    else -> getString(R.string.rating_multiple, book.rating, book.reviewCount)
                }
                tvBookDescriptionMoreReviews.visibility =
                    if (book.reviewCount > 3) View.VISIBLE else View.GONE
            } else {
                llBookDescriptionRatingDetails.visibility = View.GONE
                llBookDescriptionReviewContainer.visibility = View.GONE
            }
        }
    }

    private fun loadRatingDetails() {
        viewModel.ratingProgress.observe(this) { progresses ->
            val progressBars = listOf(
                binding.pbBookDescription5StarProgress,
                binding.pbBookDescription4StarProgress,
                binding.pbBookDescription3StarProgress,
                binding.pbBookDescription2StarProgress,
                binding.pbBookDescription1StarProgress
            )

            progresses.forEachIndexed { index, value ->
                progressBars[index].progress = value
            }
        }
    }

    private fun loadLatestReviews() {
        val reviewIds = book.reviews.takeLast(3).reversed()

        val container = binding.llBookDescriptionReviewContainer
        val reviewViews = listOf(
            container.getChildAt(0),
            container.getChildAt(1),
            container.getChildAt(2)
        )

        reviewViews.forEach { it.visibility = View.GONE }

        reviewIds.forEachIndexed { index, reviewId ->
            if (index < reviewViews.size) {
                val reviewView = reviewViews[index]
                reviewView.visibility = View.VISIBLE

                val usernameText = reviewView.findViewById<TextView>(R.id.tv_review_username)
                val dateText = reviewView.findViewById<TextView>(R.id.tv_review_date)
                val ratingBar = reviewView.findViewById<RatingBar>(R.id.rb_review_indicator)
                val reviewContent = reviewView.findViewById<TextView>(R.id.tv_review_text)
                val profileImage = reviewView.findViewById<ImageView>(R.id.iv_review_profile_round)
                lifecycleScope.launch {
                    val review = viewModel.getReview(reviewId).getOrElse {
                        showToast(it.message ?: "Unknown message")
                        return@launch
                    }
                    usernameText.text = review.username
                    ratingBar.rating = review.rating
                    dateText.text = SimpleDateFormat(
                        "dd MMM yyyy",
                        Locale.getDefault()
                    ).format(review.timestamp.toDate())
                    if (review.text.isNotEmpty()) {
                        reviewContent.text = review.text
                    } else {
                        reviewContent.visibility = View.GONE
                    }

                    profileImage.load(review.profileImage) {
                        crossfade(true)
                        placeholder(R.drawable.default_profile_image_40)
                        error(R.drawable.default_profile_image_40)
                    }
                }

            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}