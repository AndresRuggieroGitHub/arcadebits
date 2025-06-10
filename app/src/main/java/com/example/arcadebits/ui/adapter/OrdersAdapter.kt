package com.example.arcadebits.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.arcadebits.databinding.ItemOrderBinding
import com.example.arcadebits.model.OrderModel

class OrdersAdapter(
    private val orders: List<OrderModel>,
    private val onClick: (OrderModel) -> Unit
) : RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(private val binding: ItemOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(order: OrderModel) {
            binding.ivOrderThumbnail.setImageResource(order.thumbnailResId)
            binding.tvOrderDate.text = order.date
            binding.tvOrderNumber.text = "Order #${order.orderNumber}"
            binding.tvOrderTotal.text = "Total: ${order.total}"
            binding.tvOrderStatus.text = order.status


            binding.cardOrder.setOnClickListener {
                onClick(order)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    override fun getItemCount(): Int = orders.size
}