package com.example.arcadebits.ui.view.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.arcadebits.R
import com.example.arcadebits.databinding.FragmentOrdersBinding
import com.example.arcadebits.model.OrderModel
import com.example.arcadebits.ui.adapter.OrdersAdapter


class OrdersFragment : Fragment() {


    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!


    private val sampleOrders = listOf(
        OrderModel(
            orderNumber = "12345",
            date = "12 Jun 2025",
            total = "€42.50",
            status = "Delivered",
            thumbnailResId = R.drawable.sample_product
        ),
        OrderModel(
            orderNumber = "12346",
            date = "28 May 2025",
            total = "€15.99",
            status = "Shipped",
            thumbnailResId = R.drawable.sample_product
        )

    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        updateEmptyState()
    }

    private fun setupRecyclerView() {
        binding.rvOrders.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = OrdersAdapter(sampleOrders) { order ->


            }
        }
    }

    private fun updateEmptyState() {
        if (sampleOrders.isEmpty()) {
            binding.rvOrders.visibility = View.GONE
            binding.layoutEmpty.visibility = View.VISIBLE
        } else {
            binding.rvOrders.visibility = View.VISIBLE
            binding.layoutEmpty.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}