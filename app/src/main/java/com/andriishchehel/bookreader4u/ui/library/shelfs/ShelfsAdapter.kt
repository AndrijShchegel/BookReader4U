package com.andriishchehel.bookreader4u.ui.library.shelfs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.andriishchehel.bookreader4u.R
import com.andriishchehel.bookreader4u.data.model.Shelfs
import com.andriishchehel.bookreader4u.databinding.ItemShelfsBinding

class ShelfsAdapter(private var bookList: List<Shelfs>) :
    RecyclerView.Adapter<ShelfsAdapter.MyViewHolder>() {
    private lateinit var clickListener: OnItemClickListener

    inner class MyViewHolder(val binding: ItemShelfsBinding, listener: OnItemClickListener) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                listener.onItemClick(bindingAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemShelfsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding, clickListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = bookList[position]
        with(holder.binding) {
            if (currentItem.firstBookImage.isNotEmpty()) {
            ivCollectionsIcon.load(currentItem.firstBookImage) {
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
            } else {
                ivCollectionsIcon.setImageResource(R.color.light_gray)
            }
            tvCollectionsText.text = currentItem.shelfName
            val context = holder.itemView.context
            val booksSize = currentItem.bookIds.size
            tvCollectionsItems.text = when (booksSize) {
                1 -> context.getString(R.string.saved_single, booksSize)
                in 2..4 -> context.getString(R.string.saved_two_to_four, booksSize)
                else -> context.getString(R.string.saved_multiple, booksSize)
            }
        }
    }

    override fun getItemCount() = bookList.size


    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        clickListener = listener
    }

    fun getItem(position: Int): Shelfs = bookList[position]

    fun setBooks(newBooks: List<Shelfs>) {
        this.bookList = newBooks
        notifyDataSetChanged()
    }

}