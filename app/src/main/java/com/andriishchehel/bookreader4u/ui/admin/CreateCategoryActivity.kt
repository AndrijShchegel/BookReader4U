package com.andriishchehel.bookreader4u.ui.admin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.andriishchehel.bookreader4u.data.model.CreateCategory
import com.andriishchehel.bookreader4u.databinding.ActivityCreateCategoryBinding
import com.google.firebase.firestore.FirebaseFirestore

class CreateCategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateCategoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.buttonCreateCategory.setOnClickListener { onCreateCategoryClicked() }
    }

    private fun onCreateCategoryClicked() {
        val name = binding.editTextCreateCategoryName.text.toString()
        val description = binding.editTextCreateCategoryDescription.text.toString()

        if (validation(name, description)) {
            val db = FirebaseFirestore.getInstance()

            // Check if a category with the same name already exists
            db.collection("categories")
                .whereEqualTo("name", name)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.isEmpty) {
                        // No category with this name exists, so create a new one
                        val categoryCollection = db.collection("categories").document()

                        val category = CreateCategory(
                            name = name,
                            description = description,
                        )

                        categoryCollection.set(category)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(
                                        this,
                                        "Category created successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    finish()
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Error: ${task.exception?.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    } else {
                        // Category with the same name already exists
                        Toast.makeText(
                            this,
                            "Category name already exists. Please choose a different name.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Error checking category name: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    private fun validation(name: String, description: String): Boolean {

        return when {
            name.isEmpty() -> {
                Toast.makeText(this, "Please provide category name", Toast.LENGTH_LONG).show()
                false
            }

            description.isEmpty() -> {
                Toast.makeText(this, "Please provide category description", Toast.LENGTH_LONG)
                    .show()
                false
            }

            else -> true
        }
    }
}