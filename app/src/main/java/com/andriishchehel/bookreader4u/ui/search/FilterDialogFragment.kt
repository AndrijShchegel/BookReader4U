package com.andriishchehel.bookreader4u.ui.search

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.andriishchehel.bookreader4u.R
import com.andriishchehel.bookreader4u.databinding.DialogFragmentFilterBinding
import com.andriishchehel.bookreader4u.util.GenresHelper
import com.google.android.material.chip.Chip

class FilterDialogFragment(
    private var orderBy: String,
    private var descendingOrder: Boolean,
    private var minRating: Float,
    private var useAndLogic: Boolean,
    private val includedGenres: ArrayList<String>,
    private val excludedGenres: ArrayList<String>,
) : DialogFragment() {
    private lateinit var binding: DialogFragmentFilterBinding
    private val genreStates = mutableMapOf<Chip, Int>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogFragmentFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val orderByArray = resources.getStringArray(R.array.order_by)
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, orderByArray)
        binding.autoCompleteSortBy.setAdapter(arrayAdapter)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.ThemeOverlay)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupInitialValues()
        setupListeners()
        setupGenreChips()

        (requireActivity() as AppCompatActivity).apply {
            setSupportActionBar(binding.toolbarFilter)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
        }

        binding.toolbarFilter.setNavigationOnClickListener {
            dismiss()
        }
    }

    private fun setupInitialValues() {
        binding.autoCompleteSortBy.setText(orderBy, false)

        binding.btnDescending.isChecked = descendingOrder
        binding.btnAscending.isChecked = !descendingOrder

        binding.sliderRating.value = minRating
        binding.tvFilterByRatingDisplay.text = when {
            minRating == 0.5f -> getString(R.string.filter_minimum_rating_any)
            else -> getString(R.string.filter_minimum_rating_number, minRating)
        }

        binding.btnAnd.isChecked = useAndLogic
        binding.btnOr.isChecked = !useAndLogic
    }

    private fun setupListeners() {
        binding.sliderRating.addOnChangeListener { _, value, _ ->
            val displayText = when {
                value == 0.5f -> getString(R.string.filter_minimum_rating_any)
                else -> getString(R.string.filter_minimum_rating_number, value)
            }
            binding.tvFilterByRatingDisplay.text = displayText
        }
        context?.getString(R.string.genre_action)

        binding.btnApplyFilters.setOnClickListener {
            applyFilters()
        }
        binding.btnResetFilters.setOnClickListener {
            resetFilters()
        }
    }

    private fun setupGenreChips() {
        val genres = GenresHelper.getGenres(requireContext())

        genres.forEach { (key, genreName) ->
            val chip = createFilterChip(key, genreName)
            binding.chipGroupGenres.addView(chip)

            when {
                includedGenres.contains(key) -> {
                    genreStates[chip] = 1
                    updateChipAppearance(chip, 1)
                }
                excludedGenres.contains(key) -> {
                    genreStates[chip] = 2
                    updateChipAppearance(chip, 2)
                }
            }
        }
    }

    private fun createFilterChip(key: String, genreName: String): Chip {
        return Chip(requireContext()).apply {
            text = genreName
            isCheckable = false
            updateChipAppearance(this, 0)

            setOnClickListener {
                val currentState = genreStates[this] ?: 0
                val nextState = (currentState + 1) % 3
                genreStates[this] = nextState
                updateChipAppearance(this, nextState)

                when (nextState) {
                    0 -> { // Neutral
                        includedGenres.remove(key)
                        excludedGenres.remove(key)
                    }

                    1 -> { // Include
                        includedGenres.add(key)
                        excludedGenres.remove(key)
                    }

                    2 -> { // Exclude
                        includedGenres.remove(key)
                        excludedGenres.add(key)
                    }
                }
            }
        }
    }

    private fun updateChipAppearance(chip: Chip, state: Int) {
        when (state) {
            0 -> { // neutral
                chip.setTextColor(Color.BLACK)
                chip.chipIcon =
                    ContextCompat.getDrawable(requireContext(), R.drawable.blank_check_box)
            }

            1 -> { // include
                chip.setTextColor(Color.GREEN)
                chip.chipIcon =
                    ContextCompat.getDrawable(requireContext(), R.drawable.greeen_check_box)
            }

            2 -> { // exclude
                chip.setTextColor(Color.RED)
                chip.chipIcon =
                    ContextCompat.getDrawable(requireContext(), R.drawable.red_check_box)
            }
        }
    }

    private fun applyFilters() {
        orderBy = binding.autoCompleteSortBy.text.toString()
        descendingOrder = binding.btnDescending.isChecked
        minRating = binding.sliderRating.value
        useAndLogic = binding.btnAnd.isChecked
        sendResultsBack(orderBy, descendingOrder, minRating, useAndLogic)
        dismiss()

    }

    private fun sendResultsBack(finalOrderBy: String, descendingOrder: Boolean, minRating: Float, shouldMatchAllGenres: Boolean) {
        val result = Bundle().apply {
            putString(KEY_ORDER_BY, finalOrderBy)
            putBoolean(KEY_ORDER, descendingOrder)
            putFloat(KEY_MIN_RATING, minRating)
            putBoolean(KEY_AND_LOGIC, shouldMatchAllGenres)
            putStringArrayList(KEY_INCLUDED_GENRES, ArrayList(includedGenres))
            putStringArrayList(KEY_EXCLUDED_GENRES, ArrayList(excludedGenres))
        }

        parentFragmentManager.setFragmentResult(FILTER_REQUEST_KEY, result)
    }

    private fun resetFilters() {
        //1
        val orderBy = resources.getStringArray(R.array.order_by)
        if (orderBy.isNotEmpty()) {
            binding.autoCompleteSortBy.setText(orderBy[0], false)
        }

        //2
        binding.btnDescending.isChecked = true
        binding.btnAscending.isChecked = false

        //3
        binding.sliderRating.value = 0.5f
        binding.tvFilterByRatingDisplay.text = getString(R.string.filter_minimum_rating_any)

        //4
        binding.btnAnd.isChecked = true
        binding.btnOr.isChecked = false

        //5
        includedGenres.clear()
        excludedGenres.clear()
        for (i in 0 until binding.chipGroupGenres.childCount) {
            val chip = binding.chipGroupGenres.getChildAt(i) as Chip
            genreStates[chip] = 0
            updateChipAppearance(chip, 0)
        }
    }

    companion object {
        const val KEY_ORDER_BY = "orderBy"
        const val KEY_ORDER = "descendingOrder"
        const val KEY_MIN_RATING = "minRating"
        const val KEY_AND_LOGIC = "useAndLogic"
        const val KEY_INCLUDED_GENRES = "includedGenres"
        const val KEY_EXCLUDED_GENRES = "excludedGenres"
        const val FILTER_REQUEST_KEY = "filter_result"
        const val TAG = "FilterDialogFragment"
    }
}