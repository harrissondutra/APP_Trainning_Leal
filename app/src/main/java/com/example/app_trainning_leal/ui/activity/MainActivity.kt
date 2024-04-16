package com.example.app_trainning_leal.ui.activity

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.app_trainning_leal.R
import com.example.app_trainning_leal.databinding.ActivityMainBinding
import com.example.app_trainning_leal.ui.model.TitleTrainning
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var db = FirebaseFirestore.getInstance()
    private var listTitleTrainning: ArrayList<TitleTrainning> = arrayListOf()
    private lateinit var adapter: ListTrainningAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //Personalização da AppBar
        window.statusBarColor = getColor(R.color.colorSecondaryBlack)
        val toolbar: MaterialToolbar = findViewById(R.id.main_toolbar)
        toolbar.setTitle(getString(R.string.txt_new_trainnig_title))
        toolbar.setNavigationIcon(R.drawable.round_logout_24)
        toolbar.setBackgroundColor(getColor(R.color.colorSecondaryBlack))
        setSupportActionBar(toolbar)

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.saveData -> {
                    true
                }

                else -> false
            }
        }

        fetchTitleTrainning()

        // Criação de novo treino

        val icoNewTraining: ImageView = findViewById(R.id.create_new)
        val titleTraining = findViewById<TextInputEditText>(R.id.input_title_trainning) as TextView
        //Clique para criar novo treino
        icoNewTraining.setOnClickListener {
            if (titleTraining.text.isNotEmpty()) {
                val titleTrainning = hashMapOf(
                    "nome" to titleTraining.text.toString(),
                )
                addTitleTrainning(titleTrainning)
            }else{
                Toast.makeText(this, "Insira o nome do Plano de Treino", Toast.LENGTH_SHORT).show()
            }

            val service = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            service.hideSoftInputFromWindow(currentFocus?.windowToken, 0)


        }


    }

    private fun fetchTitleTrainning() {

        listTitleTrainning.clear()

        db.collection("titleTrainning") // Corrigindo o nome da coleção
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val titleTrainning = document.toObject(TitleTrainning::class.java)
                    listTitleTrainning.add(titleTrainning)
                    Log.d(TAG, "Document data: ${document.data}") // Adicionando log para verificar os dados
                }
                setupRecyclerView()
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }

    private fun addTitleTrainning(titleTrainning: HashMap<String, String>) {
        db.collection("titleTrainning")
            .add(titleTrainning)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "Treino adicionado", Toast.LENGTH_SHORT).show()
                clearInputs()
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                fetchTitleTrainning() // Atualiza a lista e a RecyclerView
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }

    private fun setupRecyclerView() {
        adapter = ListTrainningAdapter(listTitleTrainning)
        binding.rvMain.layoutManager = LinearLayoutManager(this)
        binding.rvMain.adapter = adapter

    }

    //Função para limpar os campos do formulário
    fun clearInputs() {
        val titleTraining = findViewById<TextInputEditText>(R.id.input_title_trainning) as TextView
        titleTraining.text = ""
    }

    private inner class ListTrainningAdapter(
        private val listTrainning: ArrayList<TitleTrainning>
    ) : RecyclerView.Adapter<ListTrainningAdapter.ListTrainningViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListTrainningViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.title_trainning_item, parent, false)
            return ListTrainningViewHolder(view)
        }

        override fun onBindViewHolder(holder: ListTrainningViewHolder, position: Int) {
            holder.titleTraining.text = listTrainning[position].nome
        }

        override fun getItemCount(): Int = listTrainning.size


        inner class ListTrainningViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview) {
            val titleTraining: TextView = itemview.findViewById(R.id.title_trainning_text)
        }
    }

}


