package com.andriishchehel.bookreader4u.ui.library.shelfs.shelf

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.andriishchehel.bookreader4u.R
import com.andriishchehel.bookreader4u.data.model.ContinueBook
import com.andriishchehel.bookreader4u.data.model.Shelfs
import com.andriishchehel.bookreader4u.databinding.ItemAddToShelfBinding
import com.andriishchehel.bookreader4u.databinding.ItemSavedBinding
import com.andriishchehel.bookreader4u.databinding.ItemShelfBinding
import com.andriishchehel.bookreader4u.databinding.ItemShelfsBinding

class ShelfAddBookAdapter(private var bookList: List<ContinueBook>) :
    RecyclerView.Adapter<ShelfAddBookAdapter.MyViewHolder>() {
    private val selectedBooks = mutableSetOf<String>()

    inner class MyViewHolder(val binding: ItemAddToShelfBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemAddToShelfBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = bookList[position]
        with(holder.binding) {
            ivShelfBookCover.load(currentItem.image) {
                crossfade(true)
                listener(
                    onStart = {
                        progressBar.visibility = View.VISIBLE
                    },
                    onSuccess = { _, _ ->
                        progressBar.visibility = View.GONE
                    }
                )
            }
            tvShelfTitle.text = currentItem.title
            tvShelfAuthor.text = currentItem.author
            pbShelf.progress = currentItem.readingProgress
            tvShelfProgress.text = "${currentItem.readingProgress}%"

            root.setOnClickListener {
                if (cbAddBook.isChecked) {
                    selectedBooks.remove(currentItem.bookId)
                    cbAddBook.isChecked = false
                } else {
                    selectedBooks.add(currentItem.bookId)
                    cbAddBook.isChecked = true
                }
            }
        }
    }

    override fun getItemCount() = bookList.size

    fun getSelectedBooks(): List<String> = selectedBooks.toList()
}