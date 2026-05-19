package com.andriishchehel.bookreader4u.ui.library.shelfs.shelf

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.andriishchehel.bookreader4u.R
import com.andriishchehel.bookreader4u.databinding.FragmentShelfBinding
import com.andriishchehel.bookreader4u.ui.library.shelfs.ShelfCreateDialogFragment
import com.andriishchehel.bookreader4u.ui.library.shelfs.ShelfViewModel
import com.andriishchehel.bookreader4u.ui.library.shelfs.ShelfsAdapter
import com.andriishchehel.bookreader4u.ui.reader.ReaderActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ShelfFragment : Fragment() {

    private val viewModel: ShelfViewModel by viewModels()
    private lateinit var binding: FragmentShelfBinding
    private lateinit var shelfAdapter: ShelfAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbarShelf) { v, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
            v.updatePadding(top = topInset)
            insets
        }

        val shelfId = arguments?.getString("shelfId")
        val shelfName = arguments?.getString("shelfName")
        binding.toolbarShelf.title = shelfName ?: "Полиця"

        (requireActivity() as AppCompatActivity).apply {
            setSupportActionBar(binding.toolbarShelf)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
        }

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_shelf, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_rename -> {
                        val dialog = ShelfRenameDialogFragment().apply {
                            arguments = Bundle().apply {
                                putString("shelfId", shelfId)
                                putString("shelfName", shelfName)
                            }
                        }
                        dialog.show(parentFragmentManager, "ShelfRenameDialog")
                        true
                    }

                    R.id.action_delete -> {
                        lifecycleScope.launch {
                            if (shelfId != null) {
                                viewModel.deleteShelf(shelfId).fold(
                                    onSuccess = {
                                        requireActivity().onBackPressedDispatcher.onBackPressed()
                                    },
                                    onFailure = {
                                        Toast.makeText(
                                            context,
                                            it.localizedMessage,
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                )
                            }
                        }
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        binding.toolbarShelf.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        parentFragmentManager.setFragmentResultListener("shelfRenameResult", viewLifecycleOwner) { _, bundle ->
            val newName = bundle.getString("newShelfName")
            if (!newName.isNullOrBlank()) {
                binding.toolbarShelf.title = newName
            }
        }

        shelfAdapter = ShelfAdapter(emptyList())
        binding.rvShelf.adapter = shelfAdapter

        shelfAdapter.setOnItemClickListener(object : ShelfAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val book = shelfAdapter.getItem(position)
                val intent = Intent(context, ReaderActivity::class.java).apply {
                    putExtra("epubUrl", book.fileUrl)
                    putExtra("bookId", book.bookId)
                }
                startActivity(intent)
            }

            override fun onDeleteClick(position: Int) {
                val book = shelfAdapter.getItem(position)
                AlertDialog.Builder(requireContext())
                    .setTitle("Видалити книгу")
                    .setMessage("Ви дійсно хочете видалити книгу з полиці?")
                    .setPositiveButton("Так") { _, _ ->
                        lifecycleScope.launch {
                            viewModel.removeBookFromShelf(book.bookId, shelfId!!)
                        }
                    }
                    .setNegativeButton("Скасувати", null)
                    .show()
            }
        })
        lifecycleScope.launch {
            viewModel.fetchShelfBooks(shelfId!!)
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collectLatest { books ->
                    shelfAdapter.setBooks(books)
                }
        }

        binding.fabAddBook.setOnClickListener {
            val dialog = ShelfAddBookDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("shelfId", shelfId)
                }
            }
            dialog.show(parentFragmentManager, "ShelfAddBookDialog")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentShelfBinding.inflate(inflater, container, false)
        return binding.root
    }
}