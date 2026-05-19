package com.andriishchehel.bookreader4u.ui.reader

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.PopupWindow
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.andriishchehel.bookreader4u.R
import com.andriishchehel.bookreader4u.databinding.ActivityReadiumReaderBinding
import com.andriishchehel.bookreader4u.databinding.PopupReaderDisplaySettingsBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.readium.adapter.pdfium.document.PdfiumDocumentFactory
import org.readium.r2.navigator.epub.EpubNavigatorFactory
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.asset.AssetRetriever
import org.readium.r2.shared.util.getOrElse
import org.readium.r2.shared.util.http.DefaultHttpClient
import org.readium.r2.shared.util.toUrl
import org.readium.r2.streamer.PublicationOpener
import org.readium.r2.streamer.parser.DefaultPublicationParser
import java.io.File

@AndroidEntryPoint
class ReaderActivity : AppCompatActivity() {

    private val viewModel: ReaderViewModel by viewModels()
    private lateinit var binding: ActivityReadiumReaderBinding
    private lateinit var epubFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadiumReaderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbarReadBook) { v, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
            v.updatePadding(top = topInset)
            insets
        }

        setSupportActionBar(binding.toolbarReadBook)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = ""
        binding.toolbarReadBook.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val epubUrl = intent.getStringExtra("epubUrl") ?: run {
            showErrorAndFinish("Не вказано шлях до EPUB-файлу")
            return
        }

        val bookId = intent.getStringExtra("bookId") ?: run {
            showErrorAndFinish("Не вказано book Id")
            return
        }
        viewModel.shareBookId(bookId)

        epubFile = File(cacheDir, "book_${bookId}.epub")

        lifecycleScope.launch {
            viewModel.setInitialLocator()

            viewModel.downloadBook(epubUrl, epubFile).fold(
                onSuccess = {
                    loadEpub(epubFile)
                },
                onFailure = {
                    showErrorAndFinish(it.localizedMessage ?: "Невідома помилка")
                }
            )
        }

        lifecycleScope.launch {
            viewModel.preferences.collectLatest { pref ->
                pref.theme?.backgroundColor?.let { binding.readerLayout.setBackgroundColor(it) }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_read_book, menu)

        val displayItem = menu.findItem(R.id.menu_display_settings)
        val otherItem = menu.findItem(R.id.menu_other_options)

        val whiteColor = ContextCompat.getColor(this, android.R.color.white)
        displayItem.icon?.setTint(whiteColor)
        otherItem.icon?.setTint(whiteColor)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_display_settings -> {
                showDisplaySettingsPopup()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDisplaySettingsPopup() {
        val popupView = layoutInflater.inflate(R.layout.popup_reader_display_settings, null)

        val popupWindow = PopupWindow(popupView, WRAP_CONTENT, WRAP_CONTENT, true).apply {
            elevation = 10f
            isOutsideTouchable = true
        }

        popupWindow.showAsDropDown(binding.toolbarReadBook)

        val popupBinding = PopupReaderDisplaySettingsBinding.bind(popupView)
        popupBinding.themeLight.setOnClickListener {
            lifecycleScope.launch {
                viewModel.setPreferences("theme", "light")
            }
        }

        popupBinding.themeSepia.setOnClickListener {
            lifecycleScope.launch {
                viewModel.setPreferences("theme", "sepia")
            }
        }

        popupBinding.themeDark.setOnClickListener {
            lifecycleScope.launch {
                viewModel.setPreferences("theme", "dark")
            }
        }

        var fontSize = viewModel.preferences.value.fontSize ?: 1.0
        var sizePercent = (fontSize * 100).toInt()
        popupBinding.tvDisplayTextSize.text = "$sizePercent%"
        popupBinding.moreSize.setOnClickListener {
            lifecycleScope.launch {
                fontSize += 0.1
                viewModel.setPreferences("fontSize", fontSize)
                sizePercent = (fontSize * 100).toInt()
                popupBinding.tvDisplayTextSize.text = "$sizePercent%"
            }
        }
        popupBinding.lessSize.setOnClickListener {
            lifecycleScope.launch {
                fontSize -= 0.1
                viewModel.setPreferences("fontSize", fontSize)
                sizePercent = (fontSize * 100).toInt()
                popupBinding.tvDisplayTextSize.text = "$sizePercent%"
            }
        }
        var lineHeight = viewModel.preferences.value.lineHeight ?: 1.0
        var heightPercent = (lineHeight * 100).toInt()
        popupBinding.tvDisplayLineHeight.text = "$heightPercent%"
        popupBinding.moreLineHeight.setOnClickListener {
            lifecycleScope.launch {
                lineHeight += 0.1
                viewModel.setPreferences("lineHeight", lineHeight)
                heightPercent = (lineHeight * 100).toInt()
                popupBinding.tvDisplayLineHeight.text = "$heightPercent%"
            }
        }
        popupBinding.lessLineHeight.setOnClickListener {
            lifecycleScope.launch {
                lineHeight -= 0.1
                viewModel.setPreferences("lineHeight", lineHeight)
                heightPercent = (lineHeight * 100).toInt()
                popupBinding.tvDisplayLineHeight.text = "$heightPercent%"
            }
        }
    }

    private suspend fun loadEpub(file: File) {
        try {
            val httpClient = DefaultHttpClient()
            val assetRetriever = AssetRetriever(
                contentResolver = contentResolver,
                httpClient = httpClient
            )
            val url = file.toUrl()
            val asset = assetRetriever.retrieve(url)
                .getOrElse {
                    showErrorAndFinish("Не вдалося завантажити ресурс")
                    return
                }
            val publicationOpener = PublicationOpener(
                publicationParser = DefaultPublicationParser(
                    context = this@ReaderActivity,
                    httpClient = httpClient,
                    assetRetriever = assetRetriever,
                    pdfFactory = PdfiumDocumentFactory(this@ReaderActivity),
                )
            )

            val publication = publicationOpener.open(asset, allowUserInteraction = true)
                .getOrElse {
                    showErrorAndFinish("Не вдалося відкрити публікацію")
                    return
                }

            if (publication.conformsTo(Publication.Profile.EPUB)) {
                val navigatorFactory = EpubNavigatorFactory(
                    publication = publication
                )
                val fragment = EpubReaderFragment.newInstance(navigatorFactory)

                supportFragmentManager.beginTransaction()
                    .replace(R.id.navigator_container, fragment)
                    .commit()
            }

        } catch (e: Exception) {
            showErrorAndFinish("Помилка читання EPUB: ${e.localizedMessage}")
        }
    }


    private fun showErrorAndFinish(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
        Log.e("EPUBViewer", message)
    }
}