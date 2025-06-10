package com.example.arcadebits.ui.adapter

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.arcadebits.R
import com.example.arcadebits.model.ProductModel
import com.example.arcadebits.ui.view.MainActivity
import com.example.arcadebits.ui.view.ProductDetailActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ProductAdapter(
    private var productList: List<ProductModel>
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.iv_product_image)
        val heartIcon: ImageView   = itemView.findViewById(R.id.iv_heart_bottom)
        val title: TextView        = itemView.findViewById(R.id.tv_title)
        val originalPrice: TextView= itemView.findViewById(R.id.tv_original_price)
        val salePrice: TextView    = itemView.findViewById(R.id.tv_sale_price)
        val onSaleLabel: TextView  = itemView.findViewById(R.id.tv_on_sale)

        init {
            heartIcon.setOnClickListener {
                val pos = adapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener

                val product = productList[pos]
                toggleFavorite(product)
                heartIcon.setImageResource(
                    if (product.isFavorite)
                        R.drawable.product_liked_heart_icon
                    else
                        R.drawable.product_heart_add_to_wishlist_icon
                )
                Toast.makeText(
                    itemView.context,
                    if (product.isFavorite) "Añadido a favoritos"
                    else "Eliminado de favoritos",
                    Toast.LENGTH_SHORT
                ).show()
            }

            itemView.setOnClickListener {
                val pos = adapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
                val product = productList[pos]
                val ctx = itemView.context
                val intent = Intent(ctx, ProductDetailActivity::class.java)
                    .apply { putExtra("EXTRA_PRODUCT_ID", product.id) }

                (ctx as? androidx.appcompat.app.AppCompatActivity)
                    ?.startActivityForResult(intent, MainActivity.REQUEST_CODE_DETAIL)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.product_view, parent, false)
        return ProductViewHolder(view)
    }

    override fun getItemCount(): Int = productList.size

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]

        // Imagen
        if (product.image.isNotEmpty()) {
            val decodedBytes = Base64.decode(product.image, Base64.DEFAULT)
            val bmp = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            holder.productImage.setImageBitmap(bmp)
        } else {
            holder.productImage.setImageResource(R.drawable.empty_orders_illustration)
        }

        // Título
        holder.title.text = product.title

        // Precios
        holder.originalPrice.text = "€ %.2f".format(product.originalPrice)
        if (product.salePrice != null && product.salePrice != 0.0) {
            holder.salePrice.text = "€ %.2f".format(product.salePrice)
            holder.salePrice.visibility = View.VISIBLE
            holder.onSaleLabel.visibility = View.VISIBLE
            holder.originalPrice.paintFlags =
                holder.originalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.salePrice.visibility = View.GONE
            holder.onSaleLabel.visibility = View.GONE
            holder.originalPrice.paintFlags =
                holder.originalPrice.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

        // Corazón
        holder.heartIcon.setImageResource(
            if (product.isFavorite)
                R.drawable.product_liked_heart_icon
            else
                R.drawable.product_heart_add_to_wishlist_icon
        )
    }


    fun updateProducts(newList: List<ProductModel>) {
        productList = newList
        notifyDataSetChanged()
    }


    private fun toggleFavorite(product: ProductModel) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val favRef = FirebaseDatabase.getInstance()
            .reference.child("favorites").child(user.uid).child(product.id)

        if (product.isFavorite) {
            favRef.removeValue()
            product.isFavorite = false
        } else {
            favRef.setValue(true)
            product.isFavorite = true
        }
    }
}
