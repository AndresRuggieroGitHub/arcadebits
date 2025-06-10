package com.example.arcadebits.ui.view.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.arcadebits.R
import com.example.arcadebits.databinding.FragmentBirthdayBinding
import com.example.arcadebits.ui.view.SocialMediasActivity


class BirthdayFragment : Fragment() {

    private var _binding: FragmentBirthdayBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBirthdayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setListeners()


    }

    private fun setListeners() {

        binding.tvAreas.text = HtmlCompat.fromHtml(
            getString(R.string.birthday_available_areas_text),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        binding.tvSchedule.text = HtmlCompat.fromHtml(
            getString(R.string.birthday_schedule_text),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        binding.tvPhoneNumber.setOnClickListener {
            val phoneNumber = binding.tvPhoneNumber.text.toString()
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$phoneNumber")
            startActivity(intent)
        }
        binding.tvSocialMedias.setOnClickListener {
            val intent = Intent(requireContext(), SocialMediasActivity::class.java)
            startActivity(intent)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}