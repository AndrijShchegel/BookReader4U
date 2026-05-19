package com.andriishchehel.bookreader4u.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.andriishchehel.bookreader4u.databinding.FragmentHomeBinding
import com.andriishchehel.bookreader4u.ui.bookDetails.BookDetailsActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var newBooksAdapter: BookAdapter
    private lateinit var mysteryBooksAdapter: BookAdapter
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buildNewBooks()
        buildMysteryBooks()
    }

    private fun buildNewBooks() {
        newBooksAdapter = BookAdapter(emptyList())
        binding.rvNewBooks.adapter = newBooksAdapter
        binding.rvNewBooks.addItemDecoration(DefaultItemDecorator(24))

        newBooksAdapter.setOnItemClickListener(object : BookAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val book = newBooksAdapter.getItem(position)
                val intent = Intent(context, BookDetailsActivity::class.java)
                intent.putExtra("Book", book)
                startActivity(intent)
            }
        })

        lifecycleScope.launch {
            viewModel.getLatestBooks()
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collectLatest { books ->
                    newBooksAdapter.setBooks(books)
                }
        }
    }

    private fun buildMysteryBooks() {
        mysteryBooksAdapter = BookAdapter(emptyList())
        binding.rvMysteryBooks.adapter = mysteryBooksAdapter
        binding.rvMysteryBooks.addItemDecoration(DefaultItemDecorator(24))

        mysteryBooksAdapter.setOnItemClickListener(object : BookAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val book = mysteryBooksAdapter.getItem(position)
                val intent = Intent(context, BookDetailsActivity::class.java)
                intent.putExtra("Book", book)
                startActivity(intent)
            }
        })

        lifecycleScope.launch {
            viewModel.getMysteryBooks().collectLatest { books ->
                mysteryBooksAdapter.setBooks(books)
            }
        }
    }
}