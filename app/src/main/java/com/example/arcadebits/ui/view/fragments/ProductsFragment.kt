package com.example.arcadebits.ui.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.arcadebits.R
import com.example.arcadebits.databinding.FragmentProductsBinding
import com.example.arcadebits.model.CategoryModel
import com.example.arcadebits.model.ProductModel
import com.example.arcadebits.ui.adapter.CategoryAdapter
import com.example.arcadebits.ui.adapter.ProductAdapter
import com.example.arcadebits.ui.view.Searchable
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class ProductsFragment : Fragment(), Searchable {

    private var _binding: FragmentProductsBinding? = null
    private val binding get() = _binding!!


    private val cats = listOf(
        CategoryModel(R.drawable.news_icon, "Novedades"),
        CategoryModel(R.drawable.funko_icon, "Funko POP"),
        CategoryModel(R.drawable.merchandasing_icon2, "Merchandasing"),
        CategoryModel(R.drawable.discount_icon, "Descuentos"),
        CategoryModel(R.drawable.figures_icon, "Figuras"),
        CategoryModel(R.drawable.pokemon_icon, "Pokémon TCG"),
        CategoryModel(R.drawable.videogames_icon, "Video juegos"),
        CategoryModel(R.drawable.boardgames_icon, "Juegos de mesa")
    )
    private lateinit var catAdapter: CategoryAdapter
    private lateinit var productAdapter: ProductAdapter

    private var currentCategory: String? = null
    private var allProducts: List<ProductModel> = emptyList()
    private var filteredProducts: List<ProductModel> = emptyList()

    private val pageSizeProducts = 20
    private var currentProductsPage = 1


    private var initialSearchQuery: String? = null


    private var hasDoneInitialFilter = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.tvNoResults.visibility = View.GONE

        binding.rvRecomendations.visibility = View.GONE
        binding.llProductsPagination.visibility = View.GONE


        initialSearchQuery = arguments?.getString("SEARCH_QUERY")?.takeIf { it.isNotBlank() }

        if (initialSearchQuery == null) {
            currentCategory = arguments?.getString("CATEGORY_KEY")?.takeIf { it.isNotBlank() }
                ?: "Novedades"
        }

        binding.rvCategories.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        catAdapter = CategoryAdapter(cats, { category ->

            filterProductsByCategory(category.title)
        }, currentCategory)
        binding.rvCategories.adapter = catAdapter


        currentCategory?.let { cat ->
            val idx = cats.indexOfFirst { it.title.equals(cat, ignoreCase = true) }
            if (idx != -1) {
                binding.rvCategories.scrollToPosition(idx)
            }
        }


        productAdapter = ProductAdapter(emptyList())
        binding.rvRecomendations.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvRecomendations.adapter = productAdapter


        fetchProductsFromFirestore()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fetchProductsFromFirestore() {
        binding.pbProducts.visibility = View.VISIBLE

        binding.tvNoResults.visibility = View.GONE
        binding.rvRecomendations.visibility = View.GONE
        binding.llProductsPagination.visibility = View.GONE

        val dbB = FirebaseFirestore.getInstance(FirebaseApp.getInstance("appB"))
        dbB.collection("products").get()
            .addOnSuccessListener { snapshot ->
                allProducts = snapshot.documents.mapNotNull {
                    it.toObject(ProductModel::class.java)?.apply { id = it.id }
                }
                markFavoritesThenDisplayProducts()
            }
            .addOnFailureListener { e ->
                binding.pbProducts.visibility = View.GONE
                Toast.makeText(requireContext(),
                    "Error al cargar productos: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun markFavoritesThenDisplayProducts() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            applyInitialFilter()
            return
        }
        val favRef = FirebaseDatabase.getInstance().reference.child("favorites").child(user.uid)
        favRef.get().addOnSuccessListener { snapshot ->
            val favIds = snapshot.children.mapNotNull { it.key }.toSet()
            allProducts.forEach { it.isFavorite = favIds.contains(it.id) }
            applyInitialFilter()
        }.addOnFailureListener {
            applyInitialFilter()
        }
    }

    private fun applyInitialFilter() {
        // Ocultamos progress
        binding.pbProducts.visibility = View.GONE

        if (initialSearchQuery != null) {

            clearCategorySelection()

            onSearchQuery(initialSearchQuery!!)
            initialSearchQuery = null
        } else {

            filterProductsByCategory(currentCategory ?: "Novedades")

            updateUIAfterFilter()
        }
        hasDoneInitialFilter = true
    }


    private fun filterProductsByCategory(categoryName: String) {

        binding.tvNoResults.visibility = View.GONE
        binding.rvRecomendations.visibility = View.VISIBLE
        binding.llProductsPagination.visibility = View.VISIBLE

        currentCategory = categoryName
        filteredProducts = when (categoryName.lowercase()) {
            "descuentos" -> allProducts.filter { it.salePrice != null && it.salePrice!! > 0.0 }
            "novedades"  -> allProducts.sortedByDescending { it.createdAt }
            else         -> allProducts.filter { it.categories.any { cat -> cat.equals(categoryName, true) } }
        }
        currentProductsPage = 1
        showProductsPage(1)
        setupProductsPaginationControls()


        if (hasDoneInitialFilter) {
            updateUIAfterFilter()
        }
    }


    override fun onSearchQuery(query: String) {

        binding.tvNoResults.visibility = View.GONE
        binding.rvRecomendations.visibility = View.VISIBLE
        binding.llProductsPagination.visibility = View.VISIBLE

        clearCategorySelection()
        filteredProducts = allProducts.filter {
            it.title.contains(query, true)
                    || it.description.contains(query, true)
                    || it.categories.any { cat -> cat.contains(query, true) }
        }
        currentProductsPage = 1
        showProductsPage(1)
        setupProductsPaginationControls()
        updateUIAfterFilter(query)
    }

    override fun onSearchCleared() {

        filterProductsByCategory(currentCategory ?: "Novedades")
    }


    private fun clearCategorySelection() {
        catAdapter.clearSelection()
    }


    private fun showProductsPage(page: Int) {
        val from = (page - 1) * pageSizeProducts
        val to = minOf(from + pageSizeProducts, filteredProducts.size)

        val sub = if (from < filteredProducts.size) filteredProducts.subList(from, to) else emptyList()
        productAdapter.updateProducts(sub)
        currentProductsPage = page
    }

    private fun setupProductsPaginationControls() {
        val container = binding.llProductsPagination ?: return
        container.removeAllViews()
        val total = (filteredProducts.size + pageSizeProducts - 1) / pageSizeProducts
        if (total <= 1) return

        for (i in 1..total) {
            val btn = TextView(requireContext()).apply {
                text = i.toString()
                setPadding(16, 8, 16, 8)
                textSize = 16f
                setTextColor(
                    if (i == currentProductsPage)
                        resources.getColor(R.color.cian_bg)
                    else
                        resources.getColor(R.color.black)
                )
                setOnClickListener {
                    showProductsPage(i)
                    setupProductsPaginationControls()
                }
            }
            container.addView(btn)
        }
    }


    private fun updateUIAfterFilter(query: String? = null) {
        val empty = filteredProducts.isEmpty()
        binding.tvNoResults.apply {
            text = if (query != null)
                "No hay resultados para “$query”"
            else
                "No hay productos en “$currentCategory”"
            visibility = if (empty) View.VISIBLE else View.GONE
        }
        binding.rvRecomendations.visibility = if (empty) View.GONE else View.VISIBLE
        binding.llProductsPagination.visibility = if (empty) View.GONE else View.VISIBLE
    }

    override fun onResume() {
        super.onResume()

        if (!hasDoneInitialFilter) {
            markFavoritesThenDisplayProducts()
        }
    }
}
