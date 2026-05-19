package com.andriishchehel.bookreader4u.util

import android.content.Context
import com.andriishchehel.bookreader4u.R

object GenresHelper {
    private fun getGenreKeys(context: Context): List<String> {
        return context.resources.getStringArray(R.array.genre_keys).toList()
    }

    fun getGenreName(context: Context, key: String): String {
        return when (key) {
            "action" -> context.getString(R.string.genre_action)
            "adventure" -> context.getString(R.string.genre_adventure)
            "comedy" -> context.getString(R.string.genre_comedy)
            "drama" -> context.getString(R.string.genre_drama)
            "fantasy" -> context.getString(R.string.genre_fantasy)
            "fiction" -> context.getString(R.string.genre_fiction)
            "horror" -> context.getString(R.string.genre_horror)
            "historical" -> context.getString(R.string.genre_historical)
            "mystery" -> context.getString(R.string.genre_mystery)
            "romance" -> context.getString(R.string.genre_romance)
            "sci-fi" -> context.getString(R.string.genre_sci_fi)
            "thriller" -> context.getString(R.string.genre_thriller)
            "tragedy" -> context.getString(R.string.genre_tragedy)
            else -> context.getString(R.string.genre_unknown)
        }
    }

    fun getGenres(context: Context): List<Pair<String, String>> {
        return getGenreKeys(context).map { key ->
            key to getGenreName(context, key)
        }
    }
}