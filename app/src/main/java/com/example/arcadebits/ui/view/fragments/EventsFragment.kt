package com.example.arcadebits.ui.view.fragments

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.arcadebits.databinding.FragmentEventsBinding


class EventsFragment : Fragment() {

    private var _binding: FragmentEventsBinding? = null
    private val binding get() = _binding!!

    private val urlToLoad = "https://arcadejam.github.io/#home"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.webView.apply {
            settings.javaScriptEnabled = true

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(
                    view: WebView?, url: String?, favicon: Bitmap?
                ) {
                    showLoading()
                }

                override fun onPageFinished(
                    view: WebView?, url: String?
                ) {
                    hideLoading()
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?, request: WebResourceRequest?
                ): Boolean {
                    val external = request?.url.toString()
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(external)))
                    return true
                }
            }

            loadUrl(urlToLoad)
        }
    }


    private fun fadeIn(view: View) {
        view.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate().alpha(1f).setDuration(200).start()
        }
    }

    private fun fadeOut(view: View) {
        view.animate()
            .alpha(0f)
            .setDuration(200)
            .withEndAction { view.visibility = View.GONE }
            .start()
    }

    private fun showLoading() {
        fadeIn(binding.overlay)
        fadeIn(binding.progressBar)
    }

    private fun hideLoading() {
        fadeOut(binding.progressBar)
        fadeOut(binding.overlay)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}