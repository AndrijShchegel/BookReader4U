package com.andriishchehel.bookreader4u.ui.library

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.andriishchehel.bookreader4u.data.model.ContinueBook
import com.andriishchehel.bookreader4u.databinding.ItemSavedBinding

class ContinueBookAdapter(private var bookList: List<ContinueBook>) :
    RecyclerView.Adapter<ContinueBookAdapter.MyViewHolder>() {
    private lateinit var clickListener: OnItemClickListener

    inner class MyViewHolder(val binding: ItemSavedBinding, listener: OnItemClickListener) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                listener.onItemClick(bindingAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemSavedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding, clickListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = bookList[position]
        with(holder.binding) {
            ivSavedBookCover.load(currentItem.image) {
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
            tvSavedTitle.text = currentItem.title
            tvSavedAuthor.text = currentItem.author
            rbSavedRating.progress = currentItem.readingProgress
            tvSavedRatingCount.text = "${currentItem.readingProgress}%"
        }
    }

    override fun getItemCount() = bookList.size


    interface OnItemClickListener {
        fun onItemClick(position: Int)
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