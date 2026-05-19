package com.andriishchehel.bookreader4u.ui.admin

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.andriishchehel.bookreader4u.data.model.CreateBook
import com.andriishchehel.bookreader4u.databinding.ActivityCreateBookBinding
import com.andriishchehel.bookreader4u.util.GenresHelper
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class CreateBookActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var binding: ActivityCreateBookBinding
    private var selectedGenresGlobal = emptyList<String>()
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBookBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.ivCreateBookImage.setOnClickListener { imagePickLauncher.launch("image/*") }
        binding.twCreateBookCategory.setOnClickListener { showGenreSelection() }
        binding.buttonCreateBook.setOnClickListener { onCreateBookClicked() }
    }

    private val imagePickLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                binding.ivCreateBookImage.setImageURI(it)
            }
        }

    private fun showGenreSelection() {
        val genres = GenresHelper.getGenres(this)
        val categoryNamesArray = genres.map { it.second }.toTypedArray()
        val selectedCategories = BooleanArray(categoryNamesArray.size)

        // Create a multi-choice dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Categories")
            .setMultiChoiceItems(
                categoryNamesArray,
                selectedCategories
            ) { _, which, isChecked ->
                selectedCategories[which] = isChecked
            }
            .setPositiveButton("OK") { _, _ ->
                val selectedCategoryIds = mutableListOf<String>()
                val selectedCategoryNames = mutableListOf<String>()
                for (i in selectedCategories.indices) {
                    if (selectedCategories[i]) {
                        selectedCategoryIds.add(genres[i].first)
                        selectedCategoryNames.add(genres[i].second)
                    }
                }
                selectedGenresGlobal = selectedCategoryIds
                binding.twCreateBookCategory.text = selectedCategoryNames.joinToString(", ")
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }


    private fun onCreateBookClicked() {
        val title = binding.editTextCreateBookTitle.text.toString()
        val author = binding.editTextCreateBookAuthor.text.toString()
        val description = binding.editTextCreateBookDescription.text.toString()

        if (validation(title, author, description)) {
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("book_covers/${UUID.randomUUID()}.jpg")

            imageRef.putFile(selectedImageUri!!).continueWithTask { task ->
                if (!task.isSuccessful) {
                    throw task.exception ?: Exception("upload failed")
                }
                imageRef.downloadUrl
            }.addOnSuccessListener { downloadUri ->
                saveBookToFirestore(
                    imageUri = downloadUri.toString(),
                    title = title,
                    author = author,
                    description = description,
                )
            }.addOnFailureListener {
                Toast.makeText(this, "Image upload failed: ${it.message}", Toast.LENGTH_LONG).show()
            }

        }
    }

    private fun saveBookToFirestore(
        imageUri: String,
        title: String,
        author: String,
        description: String
    ) {
        val createdAt = Timestamp.now()

        val starRatings = mapOf(
            "1" to 0L,
            "2" to 0L,
            "3" to 0L,
            "4" to 0L,
            "5" to 0L
        )

        val book = CreateBook(
            image = imageUri,
            title = title,
            author = author,
            description = description,
            genres = selectedGenresGlobal,
            createAt = createdAt,
            starRatings = starRatings,
        )

        val newBookRef = db.collection("books").document()

        newBookRef.set(book)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Book created successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG)
                        .show()
                }
            }
    }

    private fun validation(
        title: String, author: String, description: String
    ): Boolean {
        return when {
            selectedImageUri == null -> {
                Toast.makeText(this, "Please select book cover image", Toast.LENGTH_LONG).show()
                false
            }

            title.isEmpty() -> {
                Toast.makeText(this, "Please provide book title", Toast.LENGTH_LONG).show()
                false
            }

            author.isEmpty() -> {
                Toast.makeText(this, "Please provide book author", Toast.LENGTH_LONG).show()
                false
            }

            description.isEmpty() -> {
                Toast.makeText(this, "Please provide book description", Toast.LENGTH_LONG).show()
                false
            }

            else -> true
        }

    }
}