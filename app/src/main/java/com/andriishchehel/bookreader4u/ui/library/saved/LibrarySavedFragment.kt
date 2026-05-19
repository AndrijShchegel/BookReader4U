package com.andriishchehel.bookreader4u.ui.library.saved

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.andriishchehel.bookreader4u.databinding.FragmentLibrarySavedBinding
import com.andriishchehel.bookreader4u.ui.library.ContinueBookAdapter
import com.andriishchehel.bookreader4u.ui.reader.ReaderActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LibrarySavedFragment : Fragment() {
    private val viewModel: LibrarySavedViewModel by viewModels()

    private lateinit var savedAdapter: ContinueBookAdapter
    private lateinit var binding: FragmentLibrarySavedBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLibrarySavedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbarSavedBooks) { v, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
            v.updatePadding(top = topInset)
            insets
        }

        (requireActivity() as AppCompatActivity).apply {
            setSupportActionBar(binding.toolbarSavedBooks)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
        }

        binding.toolbarSavedBooks.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        savedAdapter = ContinueBookAdapter(emptyList())
        binding.rvSaved.adapter = savedAdapter

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
            viewModel.fetchSavedBooks()
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collectLatest { books ->
                    savedAdapter.setBooks(books)
                }
        }

    }
}