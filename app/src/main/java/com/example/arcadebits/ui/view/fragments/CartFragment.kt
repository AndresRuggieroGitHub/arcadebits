package com.example.arcadebits.ui.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.arcadebits.databinding.FragmentCartBinding
import com.example.arcadebits.model.CartDisplayItem
import com.example.arcadebits.model.CartItem
import com.example.arcadebits.model.ProductModel
import com.example.arcadebits.ui.adapter.CartAdapter
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore

class CartFragment : Fragment() {
    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private val rawCartItems = mutableListOf<CartItem>()
    private val productsById = mutableMapOf<String, ProductModel>()
    private lateinit var adapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CartAdapter(
            items = emptyList(),
            onQuantityChanged = { displayItem, newQty ->
                val user = FirebaseAuth.getInstance().currentUser ?: return@CartAdapter
                val qtyRef = FirebaseDatabase.getInstance()
                    .reference.child("carts").child(user.uid)
                    .child(displayItem.product.id)
                    .child("quantity")
                qtyRef.setValue(newQty)
                    .addOnSuccessListener {
                        displayItem.quantity = newQty
                        val idx = adapter.currentItems.indexOf(displayItem)
                        adapter.notifyItemChanged(idx)
                        updateTotal(adapter.currentItems)
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Error actualizando cantidad", Toast.LENGTH_SHORT).show()
                    }
            },
            onDelete = { displayItem ->
                val user = FirebaseAuth.getInstance().currentUser ?: return@CartAdapter
                val itemRef = FirebaseDatabase.getInstance()
                    .reference.child("carts").child(user.uid)
                    .child(displayItem.product.id)
                itemRef.removeValue()
                    .addOnSuccessListener {
                        val updated = adapter.currentItems.filter { it.product.id != displayItem.product.id }
                        adapter.updateItems(updated)
                        updateTotal(updated)
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Error eliminando del carrito", Toast.LENGTH_SHORT).show()
                    }
            }
        )

        binding.rvCartItems.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCartItems.adapter = adapter
        binding.btnCheckout.setOnClickListener {
            Toast.makeText(requireContext(), "Funcionalidad de pago próximamente", Toast.LENGTH_SHORT).show()
        }
        loadCartItemsFromFirebase()
    }

    private fun loadCartItemsFromFirebase() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        showLoading(true)

        FirebaseDatabase.getInstance()
            .reference.child("carts").child(user.uid)
            .get().addOnSuccessListener { snap ->
                rawCartItems.clear()
                snap.children.forEach { c ->
                    val pid = c.key ?: return@forEach
                    val qty = c.child("quantity").getValue(Long::class.java)?.toInt() ?: 0
                    rawCartItems.add(CartItem(pid, qty))
                }
                val ids = rawCartItems.map { it.productId }
                if (ids.isEmpty()) {
                    adapter.updateItems(emptyList())
                    updateTotal(emptyList())
                    showLoading(false)
                    return@addOnSuccessListener
                }

                val appB = FirebaseApp.getInstance("appB")
                FirebaseFirestore.getInstance(appB)
                    .collection("products")
                    .whereIn(FieldPath.documentId(), ids)
                    .get().addOnSuccessListener { snap2 ->
                        productsById.clear()
                        snap2.documents.forEach { doc ->
                            doc.toObject(ProductModel::class.java)
                                ?.apply { id = doc.id }
                                ?.also { productsById[it.id] = it }
                        }
                        val displayItems = rawCartItems.mapNotNull { ci ->
                            productsById[ci.productId]?.let { prod ->
                                CartDisplayItem(prod, ci.quantity)
                            }
                        }
                        adapter.updateItems(displayItems)
                        updateTotal(displayItems)
                        showLoading(false)
                    }.addOnFailureListener {
                        Toast.makeText(requireContext(), "Error cargando productos", Toast.LENGTH_SHORT).show()
                        showLoading(false)
                    }

            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Error cargando carrito", Toast.LENGTH_SHORT).show()
                showLoading(false)
            }
    }

    private fun updateTotal(list: List<CartDisplayItem>) {
        val total = list.sumOf { it.quantity * (it.product.salePrice ?: it.product.originalPrice) }
        binding.tvTotal.text = "Total: €%.2f".format(total)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onResume() {
        super.onResume()

        loadCartItemsFromFirebase()
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
}
