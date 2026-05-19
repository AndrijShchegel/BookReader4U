package com.andriishchehel.bookreader4u.ui.library

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.andriishchehel.bookreader4u.R
import com.andriishchehel.bookreader4u.databinding.FragmentLibraryBinding
import com.andriishchehel.bookreader4u.ui.library.saved.LibrarySavedFragment
import com.andriishchehel.bookreader4u.ui.library.shelfs.ShelfsFragment
import com.andriishchehel.bookreader4u.ui.reader.ReaderActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LibraryFragment : Fragment() {
    private lateinit var binding: FragmentLibraryBinding
    private val viewModel: LibraryViewModel by viewModels()
    private lateinit var savedAdapter: ContinueBookAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        fetchSaved()
        fetchShelfs()

        savedAdapter = ContinueBookAdapter(emptyList())
        binding.rvContinue.adapter = savedAdapter

        savedAdapter.setOnItemClickListener(object : ContinueBookAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val book = savedAdapter.getItem(position)
                val intent = Intent(context, ReaderActivity::class.java).apply {
                    putExtra("epubUrl", book.fileUrl)
                    putExtra("bookId", book.bookId)
                }
                startActivity(intent)
            }
        })
        lifecycleScope.launch {
            viewModel.fetchReading()
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collectLatest { books ->
                    savedAdapter.setBooks(books)
                }
        }
    }

    private fun setupListeners() {
        binding.layoutSaved.setOnClickListener {
            activity?.supportFragmentManager?.commit {
                replace(R.id.frame_content, LibrarySavedFragment())
                addToBackStack(null)
            }
        }

        binding.layoutCollections.setOnClickListener {
            activity?.supportFragmentManager?.commit {
                replace(R.id.frame_content, ShelfsFragment())
                addToBackStack(null)
            }
        }
    }

    private fun fetchSaved() {
        lifecycleScope.launch {
            viewModel.fetchSavedBookSize()
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collectLatest {
                    changeSavedNumberDisplayed(it)
                }
        }
    }

    private fun changeSavedNumberDisplayed(savedSize: Int) {
        binding.tvSavedItems.text = when (savedSize) {
            1 -> getString(R.string.saved_single, savedSize)
            in 2..4 -> getString(R.string.saved_two_to_four, savedSize)
            else -> getString(R.string.saved_multiple, savedSize)
        }
    }

    private fun fetchShelfs() {
        lifecycleScope.launch {
            viewModel.fetchShelfsSize()
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collectLatest {
                    changeShelfsNumberDisplayed(it)
                }
        }
    }

    private fun changeShelfsNumberDisplayed(shelfsSize: Int) {
        binding.tvCollectionsItems.text = when (shelfsSize) {
            1 -> "1 полиця"
            in 2..4 -> "$shelfsSize полиці"
            else -> "$shelfsSize полиць"
        }
    }
}