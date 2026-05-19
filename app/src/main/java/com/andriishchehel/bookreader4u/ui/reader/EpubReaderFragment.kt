package com.andriishchehel.bookreader4u.ui.reader

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commitNow
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.andriishchehel.bookreader4u.R
import com.andriishchehel.bookreader4u.databinding.FragmentEpubReaderBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.readium.r2.navigator.epub.EpubNavigatorFactory
import org.readium.r2.navigator.epub.EpubNavigatorFragment
import org.readium.r2.navigator.epub.EpubPreferences
import org.readium.r2.shared.ExperimentalReadiumApi
import org.readium.r2.shared.util.AbsoluteUrl

class EpubReaderFragment : Fragment(), EpubNavigatorFragment.Listener {

    private val viewModel: ReaderViewModel by activityViewModels()
    private lateinit var navigator: EpubNavigatorFragment
    private lateinit var binding: FragmentEpubReaderBinding
    private lateinit var navigatorFactory: EpubNavigatorFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        childFragmentManager.fragmentFactory = navigatorFactory.createFragmentFactory(
            initialLocator = viewModel.getInitialLocator(),
            listener = this@EpubReaderFragment,
            initialPreferences = viewModel.preferences.value
        )

        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEpubReaderBinding.inflate(inflater, container, false)

        val tag = "EpubNavigatorFragment"
        if (savedInstanceState == null) {
            childFragmentManager.commitNow {
                add(R.id.navigator_container, EpubNavigatorFragment::class.java, Bundle(), tag)
            }
        }

        navigator = childFragmentManager.findFragmentByTag(tag) as EpubNavigatorFragment

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                navigator.currentLocator
                    .filterNotNull()
                    .collectLatest { locator ->
                        viewModel.saveProgression(locator)
                    }
            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.preferences.collectLatest { prefs ->
                    navigator.submitPreferences(prefs)
                }
            }
        }
    }

    @ExperimentalReadiumApi
    override fun onExternalLinkActivated(url: AbsoluteUrl) {
        TODO("Not yet implemented")
    }

    companion object {
        fun newInstance(factory: EpubNavigatorFactory): EpubReaderFragment {
            val fragment = EpubReaderFragment()
            fragment.navigatorFactory = factory
            return fragment
        }
    }
}