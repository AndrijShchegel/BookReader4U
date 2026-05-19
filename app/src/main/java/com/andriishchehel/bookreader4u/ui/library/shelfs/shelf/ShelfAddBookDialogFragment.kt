package com.andriishchehel.bookreader4u.ui.library.shelfs.shelf

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.andriishchehel.bookreader4u.databinding.DialogFragmentShelfAddBookBinding
import com.andriishchehel.bookreader4u.databinding.DialogFragmentShelfRenameBinding
import com.andriishchehel.bookreader4u.ui.library.shelfs.ShelfViewModel
import com.andriishchehel.bookreader4u.ui.reader.ReaderActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ShelfAddBookDialogFragment : DialogFragment() {

    private val viewModel: ShelfViewModel by viewModels()
    private lateinit var binding: DialogFragmentShelfAddBookBinding
    private lateinit var shelfAddBookAdapter: ShelfAddBookAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.ThemeOverlay)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogFragmentShelfAddBookBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val shelfId = arguments?.getString("shelfId")
        binding.btnConfirmSelected.setOnClickListener {
            lifecycleScope.launch {
                val bookList = shelfAddBookAdapter.getSelectedBooks()
                viewModel.addBooksToShelf(bookList, shelfId!!).fold(
                    onFailure = {
                        Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show()
                    },
                    onSuccess = {
                        dismiss()
                    }
                )
            }
        }

        binding.toolbarReview.setNavigationOnClickListener { dismiss() }

        lifecycleScope.launch {
            val bookList = viewModel.fetchReadingBooks(shelfId!!).getOrElse {
                Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show()
                return@launch
            }

            shelfAddBookAdapter = ShelfAddBookAdapter(bookList)
            binding.rvAddBooks.adapter = shelfAddBookAdapter
        }
    }
}