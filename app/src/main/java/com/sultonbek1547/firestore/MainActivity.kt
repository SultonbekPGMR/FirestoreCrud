package com.sultonbek1547.firestore

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.sultonbek1547.firestore.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val personCollectionRef = Firebase.firestore.collection("people")
    private lateinit var rvAdapter: RvAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.btnUpdate.isEnabled = false
        rvAdapter = RvAdapter(ArrayList(), object : RvClick {
            override fun itemLongClicked(person: Person) {
                deletePerson(person)
            }

            override fun itemClicked(person: Person) {
                binding.edtFirstName.setText(person.firstName)
                binding.edtLastName.setText(person.lastName)
                binding.age.setText(person.age.toString())
                binding.btnUpdate.isEnabled = true
                updatePerson(person)

            }
        })
        binding.myRv.adapter = rvAdapter

        subscribeToRealTimeUpdates()
        binding.apply {
            btnSave.setOnClickListener {
                savePerson(
                    getOldPerson()
                )
            }

        }

    }

    private fun getOldPerson(): Person {
        return Person(
            binding.edtFirstName.text.toString().trim(),
            binding.edtLastName.text.toString().trim(),
            binding.age.text.toString().trim().toInt(),
        )
    }

    private fun updatePerson(person: Person) = binding.btnUpdate.setOnClickListener {
        CoroutineScope(Dispatchers.IO).launch {
            val personQuery = personCollectionRef
                .whereEqualTo("firstName", person.firstName)
                .whereEqualTo("lastName", person.lastName)
                .whereEqualTo("age", person.age)
                .get()
                .await()

            if (personQuery.documents.isNotEmpty()) {
                for (document in personQuery) {
                    try {
                        personCollectionRef.document(document.id).set(
                            getNewPersonMap(),
                            SetOptions.merge()
                        ).await()
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, "Saved", Toast.LENGTH_SHORT).show()
                            binding.btnUpdate.isEnabled = false
                            binding.edtFirstName.setText("")
                            binding.edtLastName.setText("")
                            binding.age.setText("")

                        }
                    } catch (e: java.lang.Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@MainActivity,
                                "${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "No person matched the query",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }


    }

    private fun deletePerson(person: Person) = CoroutineScope(Dispatchers.IO).launch {
        val personQuery = personCollectionRef
            .whereEqualTo("firstName", person.firstName)
            .whereEqualTo("lastName", person.lastName)
            .whereEqualTo("age", person.age)
            .get()
            .await()

        if (personQuery.documents.isNotEmpty()) {
            for (document in personQuery) {
                try {
                    personCollectionRef.document(document.id).delete().await()

                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "deleted", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: java.lang.Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity,
                            "${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

        } else {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@MainActivity,
                    "No person matched the query",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


    }

    private fun getNewPersonMap(): Map<String, Any> {
        val firstName = binding.edtFirstName.text.toString()
        val lastName = binding.edtLastName.text.toString()
        val age = binding.age.text.toString()
        val map = mutableMapOf<String, Any>()
        if (firstName.isNotEmpty()) {
            map["firstName"] = firstName
        }
        if (firstName.isNotEmpty()) {
            map["lastName"] = lastName
        }
        if (firstName.isNotEmpty()) {
            map["age"] = age.toInt()
        }

        return map
    }

    private fun subscribeToRealTimeUpdates() {
        personCollectionRef.addSnapshotListener { value, error ->
            if (error != null) {
                Toast.makeText(this, "${error.message}", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            value?.let {
                val list = ArrayList<Person>()
                for (document in value.documents) {
                    document.toObject<Person>()?.let { list.add(it) }
                }
                rvAdapter.list.clear()
                rvAdapter.list.addAll(list)
                rvAdapter.notifyDataSetChanged()

            }

        }
    }

    private fun getPeople() = CoroutineScope(Dispatchers.IO).launch {
        try {
            val list = ArrayList<Person>()
            val querySnapshot = personCollectionRef.get().await()
            for (document in querySnapshot.documents) {
                document.toObject<Person>()?.let { list.add(it) }
            }
            withContext(Dispatchers.Main) {
                rvAdapter.list.clear()
                rvAdapter.list.addAll(list)
                rvAdapter.notifyDataSetChanged()
            }

        } catch (e: java.lang.Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun savePerson(person: Person) = CoroutineScope(Dispatchers.IO).launch {
        try {
            personCollectionRef.add(person).await()

            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Successfully saved data", Toast.LENGTH_LONG)
                    .show()
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

}