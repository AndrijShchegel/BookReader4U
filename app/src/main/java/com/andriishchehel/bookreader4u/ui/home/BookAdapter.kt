package com.andriishchehel.bookreader4u.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.andriishchehel.bookreader4u.data.model.Book
import com.andriishchehel.bookreader4u.databinding.ItemHomeBinding

class BookAdapter(private var bookList: List<Book>) :
    RecyclerView.Adapter<BookAdapter.MyViewHolder>() {
    private lateinit var clickListener: OnItemClickListener

    inner class MyViewHolder(val binding: ItemHomeBinding, listener: OnItemClickListener) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                listener.onItemClick(bindingAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemHomeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding, clickListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = bookList[position]
        with(holder.binding) {
            ivBookCover.load(currentItem.image) {
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
            tvBookTitle.text = currentItem.title
            tvBookAuthor.text = currentItem.author
            ratingBar.rating = currentItem.rating
            tvRatingCount.text = "(${currentItem.reviewCount})"
        }
    }

    override fun getItemCount() = bookList.size


    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        clickListener = listener
    }

    fun getItem(position: Int): Book = bookList[position]

    fun setBooks(newBooks: List<Book>) {
        this.bookList = newBooks
        notifyDataSetChanged()
    }

}