package com.andriishchehel.bookreader4u.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.andriishchehel.bookreader4u.data.model.Book
import com.andriishchehel.bookreader4u.databinding.ItemSearchBookBinding

class SearchAdapter(
    private val bookList: List<Book>,
) :
    RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {
    private lateinit var clickListener: OnItemClickListener


    inner class SearchViewHolder(
        val binding: ItemSearchBookBinding,
        listener: OnItemClickListener
    ) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                listener.onItemClick(bindingAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val binding =
            ItemSearchBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchViewHolder(binding, clickListener)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val book = bookList[position]

        with(holder.binding) {
            ivSearchBookCover.load(book.image) {
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

            tvSearchBookTitle.text = book.title
            tvSearchAuthor.text = book.author
            rbSearchRating.rating = book.rating
            tvSearchDescription.text = book.description
            tvSearchRatingCount.text = "(${book.reviewCount})"
        }
    }

    override fun getItemCount() = bookList.size

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        clickListener = listener
    }
}
