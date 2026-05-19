package com.andriishchehel.bookreader4u.ui.library.shelfs.shelf

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.andriishchehel.bookreader4u.R
import com.andriishchehel.bookreader4u.data.model.ContinueBook
import com.andriishchehel.bookreader4u.data.model.Shelfs
import com.andriishchehel.bookreader4u.databinding.ItemSavedBinding
import com.andriishchehel.bookreader4u.databinding.ItemShelfBinding
import com.andriishchehel.bookreader4u.databinding.ItemShelfsBinding

class ShelfAdapter(private var bookList: List<ContinueBook>) :
    RecyclerView.Adapter<ShelfAdapter.MyViewHolder>() {
    private lateinit var clickListener: OnItemClickListener

    inner class MyViewHolder(val binding: ItemShelfBinding, listener: OnItemClickListener) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.shelfTouch.setOnClickListener {
                listener.onItemClick(bindingAdapterPosition)
            }
            binding.btnDeleteShelf.setOnClickListener {
                listener.onDeleteClick(bindingAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemShelfBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding, clickListener)
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
        }
    }

    override fun getItemCount() = bookList.size


    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onDeleteClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        clickListener = listener
    }

    fun getItem(position: Int): ContinueBook = bookList[position]

    fun setBooks(newBooks: List<ContinueBook>) {
        this.bookList = newBooks
        notifyDataSetChanged()
    }

}