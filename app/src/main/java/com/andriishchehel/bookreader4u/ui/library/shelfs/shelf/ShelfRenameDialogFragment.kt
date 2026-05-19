package com.andriishchehel.bookreader4u.ui.library.shelfs.shelf

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.andriishchehel.bookreader4u.databinding.DialogFragmentShelfRenameBinding
import com.andriishchehel.bookreader4u.ui.library.shelfs.ShelfViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ShelfRenameDialogFragment : DialogFragment() {

    private val viewModel: ShelfViewModel by viewModels()
    private lateinit var binding: DialogFragmentShelfRenameBinding

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.8).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogFragmentShelfRenameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val shelfId = arguments?.getString("shelfId")
        val shelfName = arguments?.getString("shelfName")
        binding.inputShelfName.setText(shelfName)
        binding.btnRename.setOnClickListener {
            val name = binding.inputShelfName.text.toString().trim()
            lifecycleScope.launch {
                viewModel.updateShelfName(shelfId!!, name).fold(
                    onFailure = {
                        Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show()
                    },
                    onSuccess = {
                        val result = Bundle().apply {
                            putString("newShelfName", name)
                        }
                        parentFragmentManager.setFragmentResult("shelfRenameResult", result)
                        dismiss()
                    }
                )
            }
        }

        binding.btnBack.setOnClickListener { dismiss() }
    }
}