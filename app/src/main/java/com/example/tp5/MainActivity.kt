package com.example.tp5

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.ListView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CONTACT = "extra_contact"
        const val EXTRA_POSITION = "extra_position"
    }

    private val contactsList = ArrayList<Contact>()
    private lateinit var adapter: ContactAdapter
    private lateinit var listViewContacts: ListView

    private val detailActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val data = result.data!!
            val updatedContact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                data.getParcelableExtra(EXTRA_CONTACT, Contact::class.java)
            } else {
                @Suppress("DEPRECATION")
                data.getParcelableExtra(EXTRA_CONTACT)
            }

            val position = data.getIntExtra(EXTRA_POSITION, -1)

            if (updatedContact != null && position in contactsList.indices) {
                contactsList[position] = updatedContact
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        initSampleContacts()

        listViewContacts = findViewById(R.id.listViewContacts)
        adapter = ContactAdapter(this, contactsList)
        listViewContacts.adapter = adapter

        listViewContacts.setOnItemClickListener { _, _, position, _ ->
            val selectedContact = contactsList[position]
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra(EXTRA_CONTACT, selectedContact)
                putExtra(EXTRA_POSITION, position)
            }
            detailActivityResultLauncher.launch(intent)
        }
    }

    private fun initSampleContacts() {
        if (contactsList.isEmpty()) {
            contactsList.add(
                Contact(
                    id = "1",
                    nom = "Dupont",
                    prenom = "Claire",
                    telephone = "06 12 34 56 78",
                    email = "claire.dupont@example.com",
                ),
            )
            contactsList.add(
                Contact(
                    id = "2",
                    nom = "Martin",
                    prenom = "Thomas",
                    telephone = "07 23 45 67 89",
                    email = "thomas.martin@example.com",
                ),
            )
            contactsList.add(
                Contact(
                    id = "3",
                    nom = "Bernard",
                    prenom = "Sophie",
                    telephone = "06 98 76 54 32",
                    email = "sophie.bernard@example.com",
                ),
            )
            contactsList.add(
                Contact(
                    id = "4",
                    nom = "Petit",
                    prenom = "Lucas",
                    telephone = "07 11 22 33 44",
                    email = "lucas.petit@example.com",
                ),
            )
            contactsList.add(
                Contact(
                    id = "5",
                    nom = "Moreau",
                    prenom = "Julien",
                    telephone = "06 55 44 33 22",
                    email = "julien.moreau@example.com",
                ),
            )
        }
    }
}
