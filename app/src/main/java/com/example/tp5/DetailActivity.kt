package com.example.tp5

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import java.io.File
import java.io.FileOutputStream

class DetailActivity : AppCompatActivity() {

    private lateinit var imgDetailPhoto: ShapeableImageView
    private lateinit var etNom: EditText
    private lateinit var etPrenom: EditText
    private lateinit var etTelephone: EditText
    private lateinit var etEmail: EditText

    private var currentContact: Contact? = null
    private var contactPosition: Int = -1
    private var currentPhotoUriString: String? = null
    private var cameraTempUri: Uri? = null

    // Choose photo from gallery
    private val selectPhotoLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        if (uri != null) {
            val savedUri = copyUriToInternalStorage(uri)
            if (savedUri != null) {
                currentPhotoUriString = savedUri.toString()
                ImageUtils.loadPhoto(this, currentPhotoUriString, imgDetailPhoto)
            } else {
                Toast.makeText(this, "Impossible d'enregistrer la photo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Take photo with camera
    private val takePhotoLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { success: Boolean ->
        if (success && cameraTempUri != null) {
            currentPhotoUriString = cameraTempUri.toString()
            ImageUtils.loadPhoto(this, currentPhotoUriString, imgDetailPhoto)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detailMain)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbarDetail)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            cancelAndFinish()
        }

        imgDetailPhoto = findViewById(R.id.imgDetailPhoto)
        etNom = findViewById(R.id.etNom)
        etPrenom = findViewById(R.id.etPrenom)
        etTelephone = findViewById(R.id.etTelephone)
        etEmail = findViewById(R.id.etEmail)

        val btnCall = findViewById<MaterialButton>(R.id.btnCall)
        val btnSendEmail = findViewById<MaterialButton>(R.id.btnSendEmail)
        val btnChoosePhoto = findViewById<MaterialButton>(R.id.btnChoosePhoto)
        val btnTakePhoto = findViewById<MaterialButton>(R.id.btnTakePhoto)
        val btnSave = findViewById<MaterialButton>(R.id.btnSave)
        val btnCancel = findViewById<MaterialButton>(R.id.btnCancel)

        extractIntentData()
        populateContactData()

        btnCall.setOnClickListener { makePhoneCall() }
        btnSendEmail.setOnClickListener { sendEmail() }
        btnChoosePhoto.setOnClickListener { selectPhotoLauncher.launch("image/*") }
        btnTakePhoto.setOnClickListener { dispatchTakePictureIntent() }
        btnSave.setOnClickListener { saveContactData() }
        btnCancel.setOnClickListener { cancelAndFinish() }
    }

    private fun extractIntentData() {
        intent?.let {
            currentContact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getParcelableExtra(MainActivity.EXTRA_CONTACT, Contact::class.java)
            } else {
                @Suppress("DEPRECATION")
                it.getParcelableExtra(MainActivity.EXTRA_CONTACT)
            }
            contactPosition = it.getIntExtra(MainActivity.EXTRA_POSITION, -1)
        }
    }

    private fun populateContactData() {
        currentContact?.let { contact ->
            etNom.setText(contact.nom)
            etPrenom.setText(contact.prenom)
            etTelephone.setText(contact.telephone)
            etEmail.setText(contact.email)
            currentPhotoUriString = contact.photoUri

            ImageUtils.loadPhoto(this, currentPhotoUriString, imgDetailPhoto)
        }
    }

    private fun makePhoneCall() {
        val phone = etTelephone.text.toString().trim()
        if (phone.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_required_fields), Toast.LENGTH_SHORT).show()
            return
        }

        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
        try {
            startActivity(dialIntent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, getString(R.string.error_no_dialer), Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendEmail() {
        val email = etEmail.text.toString().trim()
        if (email.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_required_fields), Toast.LENGTH_SHORT).show()
            return
        }

        val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
        try {
            startActivity(emailIntent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, getString(R.string.error_no_email_app), Toast.LENGTH_SHORT).show()
        }
    }

    private fun dispatchTakePictureIntent() {
        try {
            val photoFile = File.createTempFile("camera_photo_", ".jpg", cacheDir)
            cameraTempUri = FileProvider.getUriForFile(
                this,
                "$packageName.fileprovider",
                photoFile,
            )
            cameraTempUri?.let { uri ->
                takePhotoLauncher.launch(uri)
            }
        } catch (_: Exception) {
            Toast.makeText(this, getString(R.string.error_camera_failed), Toast.LENGTH_SHORT).show()
        }
    }

    private fun copyUriToInternalStorage(uri: Uri): Uri? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val file = File(filesDir, "contact_photo_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(file)
        } catch (_: Exception) {
            null
        }
    }

    private fun saveContactData() {
        val nom = etNom.text.toString().trim()
        val prenom = etPrenom.text.toString().trim()
        val telephone = etTelephone.text.toString().trim()
        val email = etEmail.text.toString().trim()

        if (nom.isEmpty() || prenom.isEmpty() || telephone.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_required_fields), Toast.LENGTH_LONG).show()
            if (nom.isEmpty()) etNom.error = getString(R.string.label_nom)
            if (prenom.isEmpty()) etPrenom.error = getString(R.string.label_prenom)
            if (telephone.isEmpty()) etTelephone.error = getString(R.string.label_telephone)
            return
        }

        val contactToSave = currentContact?.copy(
            nom = nom,
            prenom = prenom,
            telephone = telephone,
            email = email,
            photoUri = currentPhotoUriString,
        ) ?: Contact(
            id = System.currentTimeMillis().toString(),
            nom = nom,
            prenom = prenom,
            telephone = telephone,
            email = email,
            photoUri = currentPhotoUriString,
        )

        val resultIntent = Intent().apply {
            putExtra(MainActivity.EXTRA_CONTACT, contactToSave)
            putExtra(MainActivity.EXTRA_POSITION, contactPosition)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun cancelAndFinish() {
        Toast.makeText(this, getString(R.string.msg_cancelled), Toast.LENGTH_SHORT).show()
        setResult(RESULT_CANCELED)
        finish()
    }
}
