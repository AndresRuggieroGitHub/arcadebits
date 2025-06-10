package com.example.arcadebits.ui.view

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import com.example.arcadebits.R
import com.example.arcadebits.databinding.ActivityProductDetailBinding
import com.example.arcadebits.model.ProductModel
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailBinding
    private var productId: String = ""
    private var quantity = 1
    private var isFavorite = false

    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val sb = insets.getInsets(android.view.WindowInsets.Type.systemBars())
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom)
            insets
        }


        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener { finish() }


        productId = intent.getStringExtra("EXTRA_PRODUCT_ID") ?: run {
            Toast.makeText(this, "Producto no encontrado", Toast.LENGTH_SHORT).show()
            finish(); return
        }

        loadProductFromFirestore()


        setListeners()

        binding.tvQuantity.text = quantity.toString()
        binding.ivPlus.setOnClickListener {
            quantity++
            binding.tvQuantity.text = quantity.toString()
        }
        binding.ivMinus.setOnClickListener {
            if (quantity > 1) {
                quantity--
                binding.tvQuantity.text = quantity.toString()
            }
        }




    }

    private fun setListeners() {
        binding.btnAddToCart.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                Toast.makeText(this, "Inicia sesión para agregar al carrito", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val cartRef = FirebaseDatabase.getInstance()
                .reference
                .child("carts")
                .child(user.uid)
                .child(productId)


            cartRef.child("quantity").get().addOnSuccessListener { snap ->
                val prevQty = snap.getValue(Int::class.java) ?: 0
                val newQty = prevQty + quantity


                cartRef.child("quantity")
                    .setValue(newQty)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Añadidas $quantity unidad(es). Total en carrito: $newQty",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al actualizar carrito", Toast.LENGTH_SHORT).show()
                    }
            }.addOnFailureListener {
                Toast.makeText(this, "Error de red al leer carrito", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnGoToCart.setOnClickListener {
            val data = Intent().apply { putExtra("GO_TO_CART", true) }
            setResult(RESULT_OK, data)
            finish()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_product_detail, menu)

        val menuItem = menu?.findItem(R.id.menu_fav)
        val actionView = menuItem?.actionView as? ImageButton
        actionView?.setOnClickListener {
            toggleFavoriteInDb()
        }


        actionView?.let {
            it.setImageResource(
                if (isFavorite)
                    R.drawable.product_detail_liked_heart_icon
                else
                    R.drawable.product_detail_heart_add_to_wishlist_icon
            )
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_fav -> {
                toggleFavoriteInDb()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun loadProductFromFirestore() {

        showLoading()


        val appB = FirebaseApp.getInstance("appB")
        val dbB = FirebaseFirestore.getInstance(appB)

        dbB.collection("products").document(productId).get()
            .addOnSuccessListener { doc ->
                hideLoading()

                if (doc.exists()) {
                    val product = doc.toObject(ProductModel::class.java)
                    if (product != null) {
                        product.id = doc.id
                        fillProductDetails(product)
                    } else {
                        Toast.makeText(this, "Producto no encontrado (doc sin datos)", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    Toast.makeText(this, "Documento no existe", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                hideLoading()
                Toast.makeText(this, "Error al cargar producto: ${e.message}", Toast.LENGTH_LONG).show()
                finish()
            }

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            FirebaseDatabase.getInstance()
                .reference.child("favorites")
                .child(user.uid)
                .child(productId)
                .get()
                .addOnSuccessListener { snapshot ->
                    isFavorite = snapshot.exists()
                    updateFavIcon()
                }
        }
    }
    private fun fillProductDetails(product: ProductModel) {

        if (!product.image.isNullOrEmpty()) {
            try {
                val decoded = Base64.decode(product.image, Base64.DEFAULT)
                val bmp = BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
                binding.ivProduct.setImageBitmap(bmp)
            } catch (_: Exception) {
                binding.ivProduct.setImageResource(R.drawable.empty_orders_illustration)
            }
        } else {
            binding.ivProduct.setImageResource(R.drawable.empty_orders_illustration)
        }


        binding.tvTitle.text = product.title
        binding.tvDescription.text = product.description

        // Precios
        if (product.salePrice != null && product.salePrice != product.originalPrice) {
            binding.tvSalePrice.text = "€ %.2f".format(product.salePrice)
            binding.tvOriginalPrice.text = "€ %.2f".format(product.originalPrice)
            binding.tvOriginalPrice.paintFlags = binding.tvOriginalPrice.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            binding.tvOriginalPrice.visibility = android.view.View.VISIBLE
        } else {
            binding.tvSalePrice.text = "€ %.2f".format(product.originalPrice)
            binding.tvOriginalPrice.visibility = android.view.View.GONE
        }
    }

    private fun updateFavIcon() {
        val menuItem = binding.toolbar.menu.findItem(R.id.menu_fav)
        val actionView = menuItem?.actionView as? ImageButton
        actionView?.setImageResource(
            if (isFavorite)
                R.drawable.product_detail_liked_heart_icon
            else
                R.drawable.product_detail_heart_add_to_wishlist_icon
        )
    }

    private fun toggleFavoriteInDb() {
        val user = FirebaseAuth.getInstance().currentUser ?: run {
            Toast.makeText(this, "Inicia sesión primero", Toast.LENGTH_SHORT).show()
            return
        }

        val favRef = FirebaseDatabase.getInstance()
            .reference.child("favorites")
            .child(user.uid)
            .child(productId)

        if (isFavorite) {
            favRef.removeValue()
            isFavorite = false
        } else {
            favRef.setValue(true)
            isFavorite = true
        }
        updateFavIcon()
        Toast.makeText(
            this,
            if (isFavorite) "Añadido a favoritos" else "Eliminado de favoritos",
            Toast.LENGTH_SHORT
        ).show()
    }


    private fun showLoading() {
        binding.progressBar.visibility = android.view.View.VISIBLE
    }
    private fun hideLoading() {
        binding.progressBar.visibility = android.view.View.GONE
    }

}
