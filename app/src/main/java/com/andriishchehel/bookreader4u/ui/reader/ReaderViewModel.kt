package com.andriishchehel.bookreader4u.ui.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andriishchehel.bookreader4u.data.repository.ReaderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.readium.r2.navigator.epub.EpubPreferences
import org.readium.r2.navigator.preferences.Theme
import org.readium.r2.shared.publication.Locator
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val repository: ReaderRepository
) : ViewModel() {
    private lateinit var sharedBookId: String
    private var initialLocator: Locator? = null
    private val _preferences = MutableStateFlow(EpubPreferences())
    val preferences: StateFlow<EpubPreferences> = _preferences.asStateFlow()

    suspend fun downloadBook(url: String, file: File): Result<Unit> {
        if (!file.exists()) {
            repository.downloadBook(url, file).getOrElse {
                return Result.failure(it)
            }
        }
        return Result.success(Unit)
    }

    suspend fun saveProgression(locator: Locator): Result<Unit> {
        return repository.saveProgression(sharedBookId, locator)
    }

    fun shareBookId(bookId: String) {
        sharedBookId = bookId
    }

    suspend fun setInitialLocator() {
        initialLocator = repository.fetchProgression(sharedBookId)
    }

    fun getInitialLocator(): Locator? {
        return initialLocator
    }

    suspend fun setPreferences(key: String, value: Any) {
        val current = preferences.value
        val updatedPreferences = mutableMapOf<String, Any>(
            "theme" to (current.theme ?: Theme.LIGHT).name.lowercase(),
            "fontSize" to (current.fontSize ?: 1.0),
            "lineHeight" to (current.lineHeight ?: 1.0)
        )
        updatedPreferences[key] = value
        repository.setPreferences(updatedPreferences)
    }

    init {
        viewModelScope.launch {
            repository.getPreferencesFlow().collectLatest { prefsMap ->
                val theme = when (prefsMap["theme"] as? String) {
                    "sepia" -> Theme.SEPIA
                    "dark" -> Theme.DARK
                    else -> Theme.LIGHT
                }
                val fontSize = (prefsMap["fontSize"] as? Number)?.toDouble() ?: 1.0
                val lineHeight = (prefsMap["lineHeight"] as? Number)?.toDouble() ?: 1.0

                _preferences.value = EpubPreferences(
                    publisherStyles = false,
                    theme = theme,
                    fontSize = fontSize,
                    lineHeight = lineHeight,
                )
            }
        }
    }
}