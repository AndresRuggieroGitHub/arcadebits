package com.example.arcadebits.ui.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.arcadebits.R
import com.example.arcadebits.databinding.ActivitySignUpBinding
import com.example.arcadebits.databinding.ActivitySocialMediasBinding
import com.example.arcadebits.databinding.ActivityWelcomeBinding

class SocialMediasActivity : AppCompatActivity() {


    private lateinit var binding: ActivitySocialMediasBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding =ActivitySocialMediasBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setListeners()
    }

    private fun setListeners() {
        binding.ivToGoBack.setOnClickListener{
            finish()
        }

        //facebook------------------------------------------
        binding.ivFacebookIcon.setOnClickListener {
            goToFacebook()
        }
        binding.tvFacebook.setOnClickListener {
            goToFacebook()
        }
        //facebook------------------------------------------

        //   X   -------------------------------------------
        binding.ivXIcon.setOnClickListener {
            goToX()
        }
        binding.tvX.setOnClickListener {
            goToX()
        }
        //   X   -------------------------------------------

        //instagram-----------------------------------------
        binding.ivInstagramIcon.setOnClickListener {
            goToInstagram()
        }
        binding.tvInstagram.setOnClickListener {
            goToInstagram()
        }
        //instagram-----------------------------------------

        //youtube-------------------------------------------
        binding.ivYoutubeIcon.setOnClickListener {
            goToYoutube()
        }
        binding.tvYoutube.setOnClickListener {
            goToYoutube()
        }
        //youtube-------------------------------------------

        //threads-------------------------------------------
        binding.ivThreadsIcon.setOnClickListener {
            goToThreads()
        }
        binding.tvThreads.setOnClickListener {
            goToThreads()
        }
        //threads-------------------------------------------

    }

    private fun goToFacebook() {
        val url = getString(R.string.social_media_facebook_url_link)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun goToX() {
        val url = getString(R.string.social_media_x_url_link)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun goToInstagram() {
        val url = getString(R.string.social_media_instagram_url_link)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun goToYoutube() {
        val url = getString(R.string.social_media_youtube_url_link)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun goToThreads() {
        val url = getString(R.string.social_media_threads_url_link)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }





}