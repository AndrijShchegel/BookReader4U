package com.andriishchehel.bookreader4u.ui.library.shelfs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.andriishchehel.bookreader4u.databinding.DialogFragmentShelfCreateBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ShelfCreateDialogFragment : DialogFragment() {

    private val viewModel: ShelfViewModel by viewModels()
    private lateinit var binding: DialogFragmentShelfCreateBinding

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
        binding = DialogFragmentShelfCreateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnCreate.setOnClickListener {
            val name = binding.inputShelfName.text.toString()
            lifecycleScope.launch {
                viewModel.createShelf(name).fold(
                    onFailure = {
                        Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show()
                    },
                    onSuccess = { dismiss() }
                )
            }
        }

        binding.btnBack.setOnClickListener { dismiss() }
    }
}