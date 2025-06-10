package com.example.arcadebits.ui.view

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.arcadebits.R
import com.example.arcadebits.databinding.ActivityMianBinding
import com.example.arcadebits.ui.view.fragments.FavoritesListFragment
import com.example.arcadebits.ui.view.fragments.BirthdayFragment
import com.example.arcadebits.ui.view.fragments.CartFragment
import com.example.arcadebits.ui.view.fragments.EventsFragment
import com.example.arcadebits.ui.view.fragments.HomeFragment
import com.example.arcadebits.ui.view.fragments.ProductsFragment
import com.example.arcadebits.ui.view.fragments.ProfileFragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMianBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private var isSearchActive = false


    companion object {
        const val REQUEST_CODE_DETAIL = 1001
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        binding=ActivityMianBinding.inflate(layoutInflater)
        setContentView(binding.root)
        drawerLayout = binding.drawerLayout
        navView = binding.navView

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->

            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)


            val isKeyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            binding.bottomBar.visibility = if (isKeyboardVisible) View.GONE else View.VISIBLE

            insets
        }
        initComponents()
        setListeners()


        supportFragmentManager.addOnBackStackChangedListener {
            updateActionBarAfterBackStackChange()
        }


        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isSearchActive) {
                    deactivateSearchBar()
                    return
                }

                val fm = supportFragmentManager
                if (fm.backStackEntryCount > 0) {
                    fm.popBackStack()
                    return
                }


                regresarOSalir()
            }
        })





    }


    override fun onResume() {
        super.onResume()
        val current = supportFragmentManager.findFragmentById(R.id.flContent)
        if (current is HomeFragment || current is ProductsFragment) {
            binding.ivSearch.visibility = View.VISIBLE
        } else {
            binding.ivSearch.visibility = View.GONE
        }
    }


    private fun regresarOSalir() {
        val current = supportFragmentManager.findFragmentById(R.id.flContent)
        if (current !is HomeFragment) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.flContent, HomeFragment())
                .commit()
            binding.tvTitle.text = getString(R.string.fragment_home_title)
            navView.setCheckedItem(R.id.nav_home)

            binding.ivSearch.visibility = View.VISIBLE
        } else {
            exitApp()
        }
    }

    private fun setListeners() {
        binding.hamburgerIcon.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        binding.viewHomeIcon.setOnClickListener {
            if (!isFragmentVisible(HomeFragment::class.java)) {
                replaceFragment(HomeFragment(), R.string.fragment_home_title, R.id.nav_home)
            }
        }
        binding.viewWishlistIcon.setOnClickListener {
            if (!isFragmentVisible(FavoritesListFragment::class.java)) {
                replaceFragment(FavoritesListFragment(), R.string.fragment_wishlist_title, null)
            }
        }
        binding.viewCartIcon.setOnClickListener {
            if (!isFragmentVisible(CartFragment::class.java)) {
                replaceFragment(CartFragment(), R.string.fragment_cart_title, null)
            }
        }

        binding.viewProfileIcon.setOnClickListener {
            if (!isFragmentVisible(ProfileFragment::class.java)) {
                replaceFragment(ProfileFragment(), R.string.fragment_profile_title, R.id.nav_profile)
            }
        }
        binding.ivSearch.setOnClickListener {
            activateSearchBar()
        }
        binding.ivThreeDots.setOnClickListener { view ->
            showOverflowMenu(view)
        }



        initMenuListeners()

    }

    private fun initMenuListeners() {

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    if (!isFragmentVisible(HomeFragment::class.java)) {
                        replaceFragment(HomeFragment(), R.string.fragment_home_title, R.id.nav_home)
                    }
                    true
                }
                R.id.nav_profile -> {
                    if (!isFragmentVisible(ProfileFragment::class.java)) {
                        replaceFragment(ProfileFragment(), R.string.fragment_profile_title, R.id.nav_profile)
                    }
                    true
                }
                R.id.nav_products -> {
                    if (!isFragmentVisible(ProductsFragment::class.java)) {
                        replaceFragment(ProductsFragment(), R.string.fragment_products_title, R.id.nav_products)
                    }
                    true
                }
                R.id.nav_events -> {
                    if (!isFragmentVisible(EventsFragment::class.java)) {
                        replaceFragment(EventsFragment(), R.string.nav_events_title, R.id.nav_events)
                    }
                    true
                }
                R.id.nav_birthday -> {
                    if (!isFragmentVisible(BirthdayFragment::class.java)) {
                        replaceFragment(BirthdayFragment(), R.string.fragment_birthday_title, null)
                    }
                    true
                }
                R.id.nav_social_media -> {
                    val intent = Intent(this, SocialMediasActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_support -> {
                    val intent = Intent(this, SupportActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_log_out -> {
                    FirebaseAuth.getInstance().signOut()
                    finish()
                    startActivity(Intent(this, WelcomeActivity::class.java))
                    true
                }
                else -> false
            }.also {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
        }



    }

    private fun initComponents() {
        initFragment()
    }

    private fun initFragment() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.flContent, HomeFragment())
            .commit()

        binding.ivSearch.visibility = View.VISIBLE
    }



    private fun isFragmentVisible(fragmentClass: Class<out Fragment>): Boolean {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.flContent)
        return currentFragment?.javaClass == fragmentClass
    }


    fun replaceFragment(
        fragment: Fragment,
        titleResId: Int,
        menuItemId: Int? = null
    ) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.flContent, fragment)
            .addToBackStack(null)
            .commit()


        binding.tvTitle.text = getString(titleResId)


        menuItemId?.let { navView.setCheckedItem(it) }


        binding.ivSearch.visibility = if (fragment is HomeFragment || fragment is ProductsFragment) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }





    private fun activateSearchBar() {
        isSearchActive = true
        binding.headerNormal.visibility = View.GONE
        binding.etSearchFull.visibility = View.VISIBLE
        binding.etSearchFull.requestFocus()


        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.etSearchFull, InputMethodManager.SHOW_IMPLICIT)

        binding.etSearchFull.imeOptions = EditorInfo.IME_ACTION_SEARCH
        binding.etSearchFull.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(v.text.toString().trim())
                true
            } else false
        }
    }


    fun performSearch(query: String) {

        val current = supportFragmentManager.findFragmentById(R.id.flContent)
        if (current is ProductsFragment) {

            current.onSearchQuery(query)
        } else {

            val frag = ProductsFragment().apply {
                arguments = Bundle().apply {
                    putString("SEARCH_QUERY", query)
                }
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.flContent, frag)
                .addToBackStack(null)
                .commit()
            binding.tvTitle.text = getString(R.string.fragment_products_title)
            navView.setCheckedItem(R.id.nav_products)
            binding.ivSearch.visibility = View.GONE
        }
    }



    private fun deactivateSearchBar() {
        isSearchActive = false
        binding.etSearchFull.setText("")
        binding.etSearchFull.visibility = View.GONE
        binding.headerNormal.visibility = View.VISIBLE

        // Ocultar teclado
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etSearchFull.windowToken, 0)

        // Forzar que la barra inferior vuelva a aparecer
        binding.bottomBar.visibility = View.VISIBLE
    }




    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            currentFocus?.let { focusedView ->
                if (focusedView is EditText) {
                    val outRect = Rect()
                    focusedView.getGlobalVisibleRect(outRect)
                    if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                        focusedView.clearFocus()
                        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(focusedView.windowToken, 0)

                        if (isSearchActive) {
                            deactivateSearchBar()
                        }
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }



    private fun updateActionBarAfterBackStackChange() {
        val current = supportFragmentManager.findFragmentById(R.id.flContent)
        when (current) {
            is HomeFragment -> {
                binding.tvTitle.text = getString(R.string.fragment_home_title)
                binding.ivSearch.visibility = View.VISIBLE
                navView.setCheckedItem(R.id.nav_home)
            }
            is ProductsFragment -> {
                binding.tvTitle.text = getString(R.string.fragment_products_title)
                binding.ivSearch.visibility = View.VISIBLE
                navView.setCheckedItem(R.id.nav_products)
            }
            is EventsFragment -> {
                binding.tvTitle.text = getString(R.string.nav_events_title)
                binding.ivSearch.visibility = View.GONE
                navView.setCheckedItem(R.id.nav_events)
            }
            is ProfileFragment -> {
                binding.tvTitle.text = getString(R.string.fragment_profile_title)
                binding.ivSearch.visibility = View.GONE
                navView.setCheckedItem(R.id.nav_profile)
            }
            is BirthdayFragment -> {
                binding.tvTitle.text = getString(R.string.fragment_birthday_title)
                binding.ivSearch.visibility = View.GONE

            }
            else -> {

                binding.ivSearch.visibility = View.GONE
            }
        }
    }




    /**
     * Permite al Fragment pedir que se cambie el texto del título en la Activity.
     */
    fun setActionBarTitle(title: String) {
        binding.tvTitle.text = title
    }

    private fun showOverflowMenu(anchor: View) {
        val popup = androidx.appcompat.widget.PopupMenu(this, anchor)
        popup.menuInflater.inflate(R.menu.menu_overflow, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_settings -> {
                    AlertDialog.Builder(this)
                        .setTitle(getString(R.string.main_three_dots_settings_title))
                        .setMessage(getString(R.string.main_three_dots_settings_message))
                        .setPositiveButton(getString(R.string.main_three_dots_settings_positive_button_text), null)
                        .show()
                    true
                }
                R.id.action_help -> {
                    AlertDialog.Builder(this)
                        .setTitle(getString(R.string.main_three_dots_help_title))
                        .setMessage(getString(R.string.main_three_dots_help_message))
                        .setPositiveButton(getString(R.string.main_three_dots_help_positive_button_text), null)
                        .show()
                    true
                }
                R.id.action_about -> {
                    AlertDialog.Builder(this)
                        .setTitle(getString(R.string.main_three_dots_about_title))
                        .setMessage(getString(R.string.main_three_dots_about_message))
                        .setPositiveButton(getString(R.string.main_three_dots_about_positive_button_text), null)
                        .show()
                    true
                }

                else -> false
            }
        }
        popup.show()
    }





    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_DETAIL && resultCode == RESULT_OK) {
            val goToCart = data?.getBooleanExtra("GO_TO_CART", false) ?: false
            if (goToCart) {
                // Si ya no está, o cargamos, simplemente reemplazamos
                supportFragmentManager.beginTransaction()
                    .replace(R.id.flContent, CartFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }
    }


    private fun exitApp() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.main_exit_title))
            .setMessage(getString(R.string.main_exit_message))
            .setPositiveButton(getString(R.string.main_exit_close_app_option)) { dialog, _ ->
                finishAffinity()
            }
            .setNegativeButton(getString(R.string.main_exit_log_out_option)) { dialog, _ ->
                FirebaseAuth.getInstance().signOut()
                finish()
                startActivity(Intent(this, WelcomeActivity::class.java))

            }
            .setNeutralButton(getString(R.string.main_exit_cancel_option)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

}
