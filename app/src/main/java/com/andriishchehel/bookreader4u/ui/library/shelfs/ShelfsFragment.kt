package com.andriishchehel.bookreader4u.ui.library.shelfs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
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
import com.andriishchehel.bookreader4u.databinding.FragmentShelfsBinding
import com.andriishchehel.bookreader4u.ui.library.shelfs.shelf.ShelfFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ShelfsFragment : Fragment() {

    private val viewModel: ShelfViewModel by viewModels()
    private lateinit var shelfsAdapter: ShelfsAdapter
    private lateinit var binding: FragmentShelfsBinding


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbarBookDescription) { v, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
            v.updatePadding(top = topInset)
            insets
        }
        binding.root.updatePadding(top = 0)

        (requireActivity() as AppCompatActivity).apply {
            setSupportActionBar(binding.toolbarBookDescription)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
        }

        binding.toolbarBookDescription.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.fabCreateShelf.setOnClickListener {
            val dialog = ShelfCreateDialogFragment()
            dialog.show(parentFragmentManager, "ShelfsDialog")
        }

        shelfsAdapter = ShelfsAdapter(emptyList())
        binding.rvShelfs.adapter = shelfsAdapter

        shelfsAdapter.setOnItemClickListener(object : ShelfsAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val shelf = shelfsAdapter.getItem(position)

                val fragment = ShelfFragment().apply {
                    arguments = Bundle().apply {
                        putString("shelfId", shelf.shelfId)
                        putString("shelfName", shelf.shelfName)
                    }
                }

                parentFragmentManager.commit {
                    replace(R.id.frame_content, fragment)
                    addToBackStack(null)
                }
            }
        })
        lifecycleScope.launch {
            viewModel.fetchShelfs()
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collectLatest { books ->
                    shelfsAdapter.setBooks(books)
                }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentShelfsBinding.inflate(inflater, container, false)
        return binding.root
    }
}