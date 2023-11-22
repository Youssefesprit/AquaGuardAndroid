package tn.aquaguard.ui


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri

import android.os.Bundle
import android.text.Editable
import android.util.Log

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.view.Window
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import tn.aquaguard.R
import tn.aquaguard.databinding.ActivityMainBinding
import tn.aquaguard.fragments.EventFragment
import tn.aquaguard.fragments.ForumFragment
import tn.aquaguard.fragments.HomeFragment
import tn.aquaguard.fragments.MyCalenderFragment
import tn.aquaguard.fragments.MyEventFragment
import tn.aquaguard.fragments.MyPostFrament
import tn.aquaguard.fragments.StoreFragment
import tn.aquaguard.models.AddEventRequest
import tn.aquaguard.models.Event
import tn.aquaguard.network.PostService
import tn.aquaguard.network.RetrofitClient
import tn.aquaguard.network.SessionManager
import tn.aquaguard.utils.ImagePickerCallback
import tn.aquaguard.utils.ImagePickerUtils
import tn.aquaguard.viewmodel.EventViewModel
import tn.aquaguard.viewmodel.PostViewModel
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var postViewModel: PostViewModel

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var fab: FloatingActionButton
    private lateinit var bottomAppBar: BottomAppBar
    private lateinit var binding: ActivityMainBinding

    private val eventViewModel: EventViewModel by viewModels()

    private lateinit var selectedImage: ImageView
    private var selectedImageUri: Uri? = null
    companion object {
        val IMAGE_REQUEST_CODE = 100;
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        // Initialize ViewModel
        postViewModel = ViewModelProvider(this).get(PostViewModel::class.java)

        setContentView(binding.root)

        val toolbar: Toolbar = findViewById(R.id.toolbar) //Ignore red line errors
        setSupportActionBar(toolbar)
        drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)

        bottomAppBar = findViewById(R.id.bottomAppBar)
        fab = findViewById(R.id.fab)


        // Set the BottomAppBar to act as the ActionBar
        setSupportActionBar(bottomAppBar)

        // Handle the FAB click event
        fab.setOnClickListener {
            // Add your FAB click action here
        }

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, tn.aquaguard.R.string.open_nav,
            tn.aquaguard.R.string.close_nav
        )

        // Set the color of the menu icon
        toggle.drawerArrowDrawable.color = ContextCompat.getColor(this, R.color.white)

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navView.setNavigationItemSelectedListener(this)


        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    println("home")
                    replaceFragment(HomeFragment())
                    binding.include3.nameofcurentFragment.text = "Home"
                }

                R.id.events -> {
                    println("event")
                    replaceFragment(EventFragment())
                    binding.include3.nameofcurentFragment.text = "Event"
                }

                R.id.forum -> {
//                    Toast.makeText(applicationContext, username, Toast.LENGTH_SHORT)
//                        .show()
                    println("forum")
                    replaceFragment(ForumFragment())
                    binding.include3.nameofcurentFragment.text = "Forum"
                }

                R.id.store -> {
                    println("store")
                    replaceFragment(StoreFragment())
                    binding.include3.nameofcurentFragment.text = "Store"
                }
            }
            true
        }
        binding.fab.setOnClickListener {
            showBottomDialog()
        }

    }

    private fun showBottomDialog() {

        val isEvent = binding.include3.nameofcurentFragment.text == "Event"

        val isPost =
            binding.include3.nameofcurentFragment.text == "Forum" || binding.include3.nameofcurentFragment.text == "My Posts"
        if (isEvent) {
            val onBtnAddImageClick: () -> Unit = {
            }

            val onBtnSubmitClick: (AddEventRequest) -> Unit = { event ->
                lifecycleScope.launch {
                    println("-----------" + event);

                    eventViewModel.addEvent(event)

                    val response = eventViewModel.response

                    // Check the response code after the coroutine completes
                    if (response?.isSuccessful == true) {
                        // Check for the expected 201 status code
                        if (response.code() == 201) {
                            // Successful creation of an event
                            Toast.makeText(
                                this@MainActivity,
                                "Event added successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            // Handle unexpected status codes
                            Toast.makeText(
                                this@MainActivity,
                                "Unexpected status code: ${response.code()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        // Handle unsuccessful response
                        Toast.makeText(
                            this@MainActivity,
                            "Error: ${response?.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            }

            DialogAddEvent(this, onBtnAddImageClick, onBtnSubmitClick)
        }
        if (isPost) {

            val inflater = LayoutInflater.from(this)
            val dialogView = inflater.inflate(R.layout.custom_add_post_dialog, null)
            val closeButton = dialogView.findViewById<ImageView>(R.id.closeButton)

            selectedImage = dialogView.findViewById<ImageView>(R.id.imageSelected)
            selectedImage.setImageDrawable(null)

            val descriptionEditText = dialogView.findViewById<TextInputEditText>(R.id.description)


            val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
            val addEventImage = dialogView.findViewById<Button>(R.id.addEventImage)
            val okButton = dialogView.findViewById<Button>(R.id.submit)

            val dialogBuilder = AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)

            val dialog = dialogBuilder.create()

            fun pickImageFromGallery() {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, IMAGE_REQUEST_CODE)
            }


            if (closeButton != null) {
                closeButton.setOnClickListener {
                    dialog.dismiss()
                }
            }
            if (cancelButton != null) {
                cancelButton.setOnClickListener {
                    descriptionEditText.text = Editable.Factory.getInstance().newEditable("")
                    selectedImage.setImageDrawable(null)
                }
            }

            if (addEventImage != null) {
                addEventImage.setOnClickListener {
                    pickImageFromGallery()
                }
            }
            if (okButton != null) {
                okButton.setOnClickListener {
                    println("Button clicked")
                    val descriptionText = descriptionEditText.text.toString()
                    val description = RequestBody.create(MediaType.parse("text/plain"), descriptionText)

                    selectedImageUri?.let { uri ->
                        contentResolver.openInputStream(uri)?.use { inputStream ->
                            val mimeType = contentResolver.getType(uri)
                            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"

                            Log.d("MainActivity", "MIME Type: $mimeType, Extension: $extension")

                            val tempFile = File.createTempFile("prefix_", "_image.$extension", cacheDir)
                            FileOutputStream(tempFile).use { outputStream ->
                                inputStream.copyTo(outputStream)
                            }

                            val requestFile = RequestBody.create(MediaType.parse("image/*"), tempFile)

                            println("walah la na3rech kjsdf :::::::::::"+tempFile.name)

                            val imageBody = MultipartBody.Part.createFormData("image", "image_.$extension", requestFile)

                            println("----------------------------"+imageBody.body().toString())
                            postViewModel.addPost(description, imageBody)
                            println("Request sent")
                        } ?: run {
                            Log.e("MainActivity", "Failed to open InputStream from URI")
                        }
                    }
                }
            }



            dialog.show()
        }
    }


    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager: FragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_calendar -> {

                replaceFragment(MyCalenderFragment())
                binding.include3.nameofcurentFragment.text = "My calender"
            }

            R.id.nav_my_posts -> {
                replaceFragment(MyPostFrament())
                binding.include3.nameofcurentFragment.text = "My Posts"
            }

            R.id.nav_my_events -> {
                replaceFragment(MyEventFragment())
                binding.include3.nameofcurentFragment.text = "My Events"
            }

            R.id.nav_notification -> Toast.makeText(this, "notification!", Toast.LENGTH_SHORT)
                .show()

            R.id.nav_reclamation -> Toast.makeText(this, "reclmation!", Toast.LENGTH_SHORT).show()

            R.id.nav_command -> Toast.makeText(this, "command!", Toast.LENGTH_SHORT).show()

            R.id.nav_logout -> Toast.makeText(this, "Logout!", Toast.LENGTH_SHORT).show()
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
       override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            selectedImageUri = data?.data // Store the selected image URI
            selectedImage.setImageURI(selectedImageUri)
        }
    }

}


