package com.example.arcadebits.ui.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.arcadebits.databinding.FragmentFavoritesListBinding
import com.example.arcadebits.model.ProductModel
import com.example.arcadebits.ui.adapter.ProductAdapter
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore

class FavoritesListFragment : Fragment() {
    private var _binding: FragmentFavoritesListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ProductAdapter
    private val favoriteProducts = mutableListOf<ProductModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ProductAdapter(favoriteProducts)
        binding.rvFavorites.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvFavorites.adapter = adapter

        loadFavoriteProducts()
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.rvFavorites.visibility = if (loading) View.GONE else View.VISIBLE
        binding.llEmptyFavorites.visibility = View.GONE
    }

    private fun loadFavoriteProducts() {
        setLoading(true)

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            showEmptyState()
            return
        }

        FirebaseDatabase.getInstance().reference
            .child("favorites").child(user.uid)
            .get().addOnSuccessListener { snapshot ->
                val ids = snapshot.children.mapNotNull { it.key }

                if (ids.isEmpty()) {
                    showEmptyState()
                    return@addOnSuccessListener
                }

                val appB = FirebaseApp.getInstance("appB")
                FirebaseFirestore.getInstance(appB)
                    .collection("products")
                    .whereIn(FieldPath.documentId(), ids)
                    .get().addOnSuccessListener { docs ->
                        favoriteProducts.clear()
                        docs.documents.mapNotNull { doc ->
                            doc.toObject(ProductModel::class.java)?.apply {
                                id = doc.id
                                isFavorite = true
                            }
                        }.also { favoriteProducts.addAll(it) }

                        if (favoriteProducts.isEmpty()) showEmptyState()
                        else showFavoriteList()
                    }.addOnFailureListener {
                        Toast.makeText(requireContext(), "Error cargando favoritos", Toast.LENGTH_SHORT).show()
                        showEmptyState()
                    }
            }.addOnFailureListener {
                showEmptyState()
            }
    }

    private fun showFavoriteList() {
        binding.llEmptyFavorites.visibility = View.GONE
        binding.rvFavorites.visibility = View.VISIBLE
        adapter.notifyDataSetChanged()
        binding.progressBar.visibility = View.GONE
    }

    private fun showEmptyState() {
        binding.progressBar.visibility = View.GONE
        binding.rvFavorites.visibility = View.GONE
        binding.llEmptyFavorites.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
