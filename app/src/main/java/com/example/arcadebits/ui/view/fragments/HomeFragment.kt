package com.example.arcadebits.ui.view.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.arcadebits.R
import com.example.arcadebits.databinding.FragmentHomeBinding
import com.example.arcadebits.model.CategoryModel
import com.example.arcadebits.model.ProductModel
import com.example.arcadebits.ui.adapter.CategoryAdapter
import com.example.arcadebits.ui.adapter.ProductAdapter
import com.example.arcadebits.ui.adapter.SliderAdapter
import com.example.arcadebits.ui.view.MainActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val sliderHandler = Handler(Looper.getMainLooper())
    private val sliderRunnable = object : Runnable {
        override fun run() {
            val vp = binding.viewPager2
            vp.currentItem = (vp.currentItem + 1) % (vp.adapter?.itemCount ?: 1)
            sliderHandler.postDelayed(this, 5000)
        }
    }

    private lateinit var productAdapter: ProductAdapter

    // 👇 NUEVO: guardamos el adapter de categorías
    private lateinit var categoryAdapter: CategoryAdapter

    private var fullProducts: List<ProductModel> = emptyList()
    private var filteredProducts: List<ProductModel> = emptyList()

    private var currentHomePage = 1
    private val pageSizeHome = 12

    private var currentSelectedCategory: String? = "Novedades"

    private lateinit var pbRecs: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initBanner()
        initStaticBanner()
        initCategories()

        productAdapter = ProductAdapter(emptyList())
        binding.rvRecomendations.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvRecomendations.adapter = productAdapter

        pbRecs = binding.pbRecomendations

        fetchProductsFromFirestore()
        setListeners()
    }

    override fun onResume() {
        super.onResume()


        markFavoritesThenDisplay()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sliderHandler.removeCallbacks(sliderRunnable)
        _binding = null
    }

    private fun setListeners() {
        binding.ivCommingEvent.setOnClickListener {

            (activity as? MainActivity)?.replaceFragment(
                EventsFragment(),
                R.string.nav_events_title,
                R.id.nav_events
            )
        }

        binding.tvSeeAll.setOnClickListener {
            val selectedCategory = currentSelectedCategory ?: "Novedades"
            val fragment = ProductsFragment().apply {
                arguments = Bundle().apply {
                    putString("CATEGORY_KEY", selectedCategory)
                }
            }
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.flContent, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun initBanner() {
        val imageList = listOf(
            R.drawable.kimetsu_banner,
            R.drawable.dragonball_banner,
            R.drawable.funko_banner,
            R.drawable.cumple_banner
        )
        binding.viewPager2.adapter = SliderAdapter(imageList)
        binding.viewPager2.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        (binding.diEvents as DotsIndicator).setViewPager2(binding.viewPager2)

        binding.viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                sliderHandler.removeCallbacks(sliderRunnable)
                sliderHandler.postDelayed(sliderRunnable, 5000)
            }
        })
        sliderHandler.postDelayed(sliderRunnable, 5000)
    }

    private fun initStaticBanner() {
        binding.ivCommingEvent.setImageResource(R.drawable.comming_event)
    }

    private fun initCategories() {
        val cats = listOf(
            CategoryModel(R.drawable.news_icon, "Novedades"),
            CategoryModel(R.drawable.funko_icon, "Funko POP"),
            CategoryModel(R.drawable.merchandasing_icon2, "Merchandasing"),
            CategoryModel(R.drawable.discount_icon, "Descuentos"),
            CategoryModel(R.drawable.figures_icon, "Figuras"),
            CategoryModel(R.drawable.pokemon_icon, "Pokémon TCG"),
            CategoryModel(R.drawable.videogames_icon, "Video juegos"),
            CategoryModel(R.drawable.boardgames_icon, "Juegos de mesa")
        )

        binding.rvCategories.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)


        categoryAdapter = CategoryAdapter(cats, { category ->
            filterProductsByCategory(category.title)
        }, currentSelectedCategory)
        binding.rvCategories.adapter = categoryAdapter


        val initialIndex = cats.indexOfFirst {
            it.title.equals(currentSelectedCategory, ignoreCase = true)
        }
        if (initialIndex != -1) {
            binding.rvCategories.scrollToPosition(initialIndex)
        }
    }

    private fun filterProductsByCategory(categoryName: String) {
        currentSelectedCategory = categoryName

        filteredProducts = when (categoryName.lowercase()) {
            "descuentos" -> fullProducts.filter { it.salePrice != null && it.salePrice != 0.0 }
            "novedades" -> fullProducts.sortedByDescending { it.createdAt }
            else -> fullProducts.filter { product ->
                product.categories.any { it.equals(categoryName, ignoreCase = true) }
            }
        }

        currentHomePage = 1
        showHomePage(currentHomePage)
        setupHomePaginationControls()
    }

    private fun fetchProductsFromFirestore() {
        // 1) Mostrar spinner
        pbRecs.visibility = View.VISIBLE
        binding.rvRecomendations.visibility = View.GONE

        val appB = FirebaseApp.getInstance("appB")
        val dbB = FirebaseFirestore.getInstance(appB)
        dbB.collection("products")
            .get()
            .addOnSuccessListener { snapshot ->
                fullProducts = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(ProductModel::class.java)?.apply { id = doc.id }
                }
                // Leer y marcar favoritos
                markFavoritesThenDisplay()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(),
                    "Error al cargar productos: ${e.message}", Toast.LENGTH_LONG).show()
                // ocultar spinner aunque falle
                pbRecs.visibility = View.GONE
            }
    }

    /**
     * 1) Consulta Realtime DB por /favorites/{uid}
     * 2) Marca fullProducts[i].isFavorite = true para cada product.id que aparezca
     * 3) Finalmente asigna la lista al adapter
     */
    private fun markFavoritesThenDisplay() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            // Si no hay usuario autenticado, nadie tiene favoritos → todo queda en false
            setupAdapterWithProducts(fullProducts)
            return
        }

        val dbRef = FirebaseDatabase.getInstance().reference
        val favRef = dbRef.child("favorites").child(user.uid)

        // Leemos solo una vez
        favRef.get().addOnSuccessListener { snapshot ->
            // snapshot.value es un Map<{productId}: true> o null
            val favIds = mutableSetOf<String>()
            if (snapshot.exists()) {
                snapshot.children.forEach { child ->
                    child.key?.let { favIds.add(it) }
                }
            }

            // Marcamos isFavorite para cada producto
            fullProducts.forEach { product ->
                product.isFavorite = favIds.contains(product.id)
            }

            // Filtramos/mostramos “Novedades” (o la categoría que toque) sobre esta lista ya marcada
            filterProductsByCategory(currentSelectedCategory ?: "Novedades")
        }
            .addOnFailureListener {
                // En caso de fallo, simplemente mostramos sin marcar favoritos
                filterProductsByCategory(currentSelectedCategory ?: "Novedades")
            }
    }

    /** Usa filteredProducts ya marcado con isFavorite y arranca el adapter */
    private fun setupAdapterWithProducts(list: List<ProductModel>) {
        productAdapter = ProductAdapter(list)
        binding.rvRecomendations.adapter = productAdapter
        // (Si tienes paginación, sólo pasas la página 1 aquí)
    }


    private fun resetToInitialState() {
        currentSelectedCategory = "Novedades"

        // Actualizar adapter y forzar redraw
        categoryAdapter.updateSelectedCategory("Novedades")
        categoryAdapter.notifyDataSetChanged() // <- fuerza que se refresque todo

        // Filtrar productos y refrescar adapter
        filterProductsByCategory("Novedades")

        // Asegurarse de que el scroll al top ocurre después de setear los productos
        binding.rvRecomendations.post {
            binding.rvRecomendations.scrollToPosition(0)
        }

        // También puede ayudarte forzar que el layout manager lo haga
        (binding.rvRecomendations.layoutManager as? GridLayoutManager)?.scrollToPositionWithOffset(0, 0)

        // Rescrollear horizontal de categorías (opcional)
        binding.rvCategories.post {
            binding.rvCategories.scrollToPosition(0)
        }
    }

    private fun showHomePage(page: Int) {
        if (filteredProducts.isEmpty()) return

        // Calcula el sub‐rango a mostrar
        val fromIndex = (page - 1) * pageSizeHome
        val toIndex = minOf(page * pageSizeHome, filteredProducts.size)
        if (fromIndex >= filteredProducts.size) return
        val slice = filteredProducts.subList(fromIndex, toIndex)

        // Actualiza el adapter con la página correspondiente
        productAdapter.updateProducts(slice)

        // Oculta el ProgressBar y muestra el RecyclerView
        pbRecs.visibility = View.GONE
        binding.rvRecomendations.visibility = View.VISIBLE

        // Asegúrate de que la lista se desplace al principio
        binding.rvRecomendations.scrollToPosition(0)
    }

    private fun setupHomePaginationControls() {
        val container = binding.llHomePagination
        container.removeAllViews()

        val totalPages = (filteredProducts.size + pageSizeHome - 1) / pageSizeHome
        if (totalPages <= 1) return

        for (i in 1..totalPages) {
            val btn = TextView(requireContext()).apply {
                text = i.toString()
                setPadding(24, 12, 24, 12)
                textSize = 16f
                setTextColor(
                    if (i == currentHomePage)
                        resources.getColor(R.color.cian_bg, null)
                    else
                        resources.getColor(R.color.black, null)
                )
                setOnClickListener {
                    currentHomePage = i
                    showHomePage(i)
                    setupHomePaginationControls()
                }
            }
            container.addView(btn)
        }
    }
}