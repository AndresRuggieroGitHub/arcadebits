package com.example.arcadebits.ui.adapter

import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.arcadebits.R
import com.example.arcadebits.model.CartDisplayItem
import com.example.arcadebits.ui.view.ProductDetailActivity

class CartAdapter(
    private var items: List<CartDisplayItem>,
    private val onQuantityChanged: (CartDisplayItem, Int) -> Unit,
    private val onDelete: (CartDisplayItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {


    val currentItems: List<CartDisplayItem>
        get() = items

    inner class CartViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val ivImage   : ImageView   = v.findViewById(R.id.iv_item_image)
        val tvTitle   : TextView    = v.findViewById(R.id.tv_item_title)
        val tvPrice   : TextView    = v.findViewById(R.id.tv_item_price)
        val tvQty     : TextView    = v.findViewById(R.id.tv_item_quantity)
        val btnPlus   : View        = v.findViewById(R.id.iv_plus)
        val btnMinus  : View        = v.findViewById(R.id.iv_minus)
        val btnDelete : ImageButton = v.findViewById(R.id.iv_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cart_item_view, parent, false)
        return CartViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = items[position]
        // Image
        if (item.product.image.isNotEmpty()) {
            val decoded = Base64.decode(item.product.image, Base64.DEFAULT)
            val bmp = BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
            holder.ivImage.setImageBitmap(bmp)
        } else {
            holder.ivImage.setImageResource(R.drawable.empty_orders_illustration)
        }

        holder.tvTitle.text = item.product.title
        holder.tvPrice.text = "€ %.2f".format(item.product.salePrice ?: item.product.originalPrice)
        holder.tvQty.text   = item.quantity.toString()

        holder.btnPlus.setOnClickListener    { onQuantityChanged(item, item.quantity + 1) }
        holder.btnMinus.setOnClickListener   { if (item.quantity > 1) onQuantityChanged(item, item.quantity - 1) }
        holder.btnDelete.setOnClickListener  { onDelete(item) }

        holder.itemView.setOnClickListener {
            val ctx = holder.itemView.context
            val intent = Intent(ctx, ProductDetailActivity::class.java).apply {
                putExtra("EXTRA_PRODUCT_ID", item.product.id)
            }
            ctx.startActivity(intent)
        }
    }

    /** Update full list */
    fun updateItems(newItems: List<CartDisplayItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}