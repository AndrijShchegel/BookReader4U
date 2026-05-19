package com.andriishchehel.bookreader4u.ui.search

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.andriishchehel.bookreader4u.data.model.Book
import com.andriishchehel.bookreader4u.databinding.ActivitySearchGenresBinding
import com.andriishchehel.bookreader4u.ui.bookDetails.BookDetailsActivity
import com.andriishchehel.bookreader4u.util.GenresHelper.getGenreName
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot

class SearchGenresActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchGenresBinding
    private lateinit var bookList: ArrayList<Book>
    private val db = FirebaseFirestore.getInstance()
    private lateinit var searchAdapter: SearchAdapter

    private lateinit var genre: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchGenresBinding.inflate(layoutInflater)

        genre = intent.getStringExtra("Genre").toString()
        setContentView(binding.root)

        binding.toolbarGenre.title = getGenreName(this, genre)
        search()

        // Set the toolbar as the action bar
        setSupportActionBar(binding.toolbarGenre)

        // Enable the back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbarGenre.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun search() {
        bookList = ArrayList()

        db.collection("books").whereArrayContains("genres", genre).get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    getBookInfo(doc)
                }
                buildList()
            }
    }

    private fun getBookInfo(doc: QueryDocumentSnapshot) {
        val reviews = (doc.get("reviews") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
        val starRatings = when (val data = doc.get("starRatings")) {
            is Map<*, *> -> data.mapNotNull { (key, value) ->
                val star = (key as? String)?.toIntOrNull()
                val count = (value as? Number)?.toInt()
                if (star != null && count != null) star to count else null
            }.toMap()

            else -> emptyMap()
        }


        val book = Book(
            bookId = doc.id,
            image = doc.getString("image") ?: "",
            title = doc.getString("title") ?: "",
            author = doc.getString("author") ?: "",
            rating = doc.getDouble("rating")?.toFloat() ?: 0f,
            description = doc.getString("description") ?: "",
            genres = (doc.get("genres") as? List<*>)?.filterIsInstance<String>()
                ?: emptyList(),
            reviews = reviews,
            reviewCount = doc.getLong("reviewCount") ?: 0L,
            fileUrl = doc.getString("fileUrl") ?: "",
            starRatings = starRatings,
        )
        bookList.add(book)
    }


    private fun buildList() {
        searchAdapter = SearchAdapter(bookList)
        binding.rvSearchGenresDisplay.adapter = searchAdapter
        searchAdapter.setOnItemClickListener(object : SearchAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val intent = Intent(this@SearchGenresActivity, BookDetailsActivity::class.java)
                intent.putExtra("Book", bookList[position])
                startActivity(intent)
            }
        })
    }
}