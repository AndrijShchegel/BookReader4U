package com.andriishchehel.bookreader4u.ui.search

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.andriishchehel.bookreader4u.R
import com.andriishchehel.bookreader4u.data.model.Book
import com.andriishchehel.bookreader4u.databinding.FragmentSearchBinding
import com.andriishchehel.bookreader4u.ui.bookDetails.BookDetailsActivity
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var searchAdapter: SearchAdapter
    private var orderBy: String = ""
    private var descendingOrder = true
    private var minRating = 0.5f
    private var useAndLogic = true
    private var included: ArrayList<String> = arrayListOf()
    private var excluded: ArrayList<String> = arrayListOf()
    private var searchJob: Job? = null
    private val viewModel: SearchViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (orderBy.isEmpty()) {
            orderBy = resources.getStringArray(R.array.order_by)[0]
        }
        setupListeners()
        lifecycleScope.launch {
            search()
        }
    }

    private fun setupListeners() {
        binding.buttonFilter.setOnClickListener {
            val dialog = FilterDialogFragment(
                orderBy,
                descendingOrder,
                minRating,
                useAndLogic,
                included,
                excluded
            )
            dialog.show(childFragmentManager, FilterDialogFragment.TAG)
        }

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                searchJob?.cancel()
                searchJob = CoroutineScope(Dispatchers.Main).launch {
                    delay(1000)
                    search()
                }
            }

        })

        childFragmentManager.setFragmentResultListener(
            FilterDialogFragment.FILTER_REQUEST_KEY,
            this
        ) { _, bundle ->
            orderBy = bundle.getString(FilterDialogFragment.KEY_ORDER_BY) ?: ""
            descendingOrder = bundle.getBoolean(FilterDialogFragment.KEY_ORDER)
            minRating = bundle.getFloat(FilterDialogFragment.KEY_MIN_RATING)
            useAndLogic = bundle.getBoolean(FilterDialogFragment.KEY_AND_LOGIC)
            included =
                bundle.getStringArrayList(FilterDialogFragment.KEY_INCLUDED_GENRES) ?: arrayListOf()
            excluded =
                bundle.getStringArrayList(FilterDialogFragment.KEY_EXCLUDED_GENRES) ?: arrayListOf()
            lifecycleScope.launch {
                search()
            }
        }
    }

    private suspend fun search() {
        val translatedOrderBy = translateOrderBy(orderBy)
        val search = binding.searchEditText.text.toString()
        showList(
            viewModel.search(
                translatedOrderBy,
                descendingOrder,
                search,
                minRating,
                useAndLogic,
                included,
                excluded
            )
        )
    }

    private fun translateOrderBy(orderBy: String): String {
        val orderOptions = resources.getStringArray(R.array.order_by)
        return when (orderBy) {
            orderOptions[0] -> "createAt"
            orderOptions[1] -> "title"
            orderOptions[2] -> "rating"
            orderOptions[3] -> "reviewCount"
            else -> orderOptions[1]
        }
    }

    private fun showList(list: List<Book>) {
        if (list.isNotEmpty()) {
            binding.rvSearchDisplay.visibility = View.VISIBLE
            binding.tvSearchError.visibility = View.GONE
            searchAdapter = SearchAdapter(list)
            binding.rvSearchDisplay.adapter = searchAdapter
            searchAdapter.setOnItemClickListener(object : SearchAdapter.OnItemClickListener {
                override fun onItemClick(position: Int) {
                    val intent = Intent(context, BookDetailsActivity::class.java)
                    intent.putExtra("Book", list[position])
                    startActivity(intent)
                }
            })
        } else {
            binding.rvSearchDisplay.visibility = View.GONE
            binding.tvSearchError.visibility = View.VISIBLE
        }
    }
}