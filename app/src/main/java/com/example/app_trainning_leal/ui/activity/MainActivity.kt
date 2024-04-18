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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.app_trainning_leal.R
import com.example.app_trainning_leal.databinding.ActivityMainBinding
import com.example.app_trainning_leal.ui.model.TitleTrainning
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

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
        toolbar.setNavigationIcon(R.drawable.round_logout_24)
        toolbar.setTitle(getString(R.string.txt_new_trainnig_title))
        toolbar.setBackgroundColor(getColor(R.color.colorSecondaryBlack))
        setSupportActionBar(toolbar)



        toolbar.setNavigationOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            true
        }
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.saveData -> {
                    true
                }

                else -> false
            }
        }

        setupRecyclerView()
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

        db.collection("titleTrainning")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val titleTrainning = document.toObject(TitleTrainning::class.java)
                    titleTrainning.id = document.id
                    listTitleTrainning.add(titleTrainning)
                }
                listTitleTrainning = ArrayList(listTitleTrainning.sortedBy { it.nome })
                setupRecyclerView() // Configura a RecyclerView antes de atualizar os dados do adaptador
                adapter.updateData(listTitleTrainning) // Atualiza os dados do adaptador
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
                fetchTitleTrainning()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }

    private fun setupRecyclerView() {
        val trainningId = intent.getStringExtra("trainningId")
        adapter = ListTrainningAdapter(listTitleTrainning, trainningId)
        binding.rvMain.layoutManager = LinearLayoutManager(this)
        binding.rvMain.adapter = adapter

    }

    //Função para limpar os campos do formulário
    fun clearInputs() {
        val titleTraining = findViewById<TextInputEditText>(R.id.input_title_trainning) as TextView
        titleTraining.text = ""
    }

    private inner class ListTrainningAdapter(
        private var listTrainning: ArrayList<TitleTrainning>,
        var trainningId: String?

    ) : RecyclerView.Adapter<ListTrainningAdapter.ListTrainningViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListTrainningViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.title_trainning_item, parent, false)
            return ListTrainningViewHolder(view)
        }

        override fun onBindViewHolder(holder: ListTrainningViewHolder, position: Int) {

            holder.titleTraining.text = listTrainning[position].nome

            holder.addTrainning.setOnClickListener {

                val trainningId = listTrainning[position].id
                if (trainningId != null) {
                    val intent = Intent(this@MainActivity, ListExerciseActivity::class.java)
                    intent.putExtra("titleTrainningId", listTrainning[position].id)
                    intent.putExtra("trainningId", trainningId)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@MainActivity, "Erro ao adicionar treino", Toast.LENGTH_SHORT).show()
                }

            }
        }

        override fun getItemCount(): Int = listTrainning.size


        inner class ListTrainningViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview) {
            val titleTraining: TextView = itemview.findViewById(R.id.title_trainning_text)
            val addTrainning: ImageView = itemview.findViewById(R.id.ico_default_circle)
        }


        fun updateData(newList: ArrayList<TitleTrainning>) {
            listTrainning = newList
            notifyDataSetChanged()
        }
    }

}


