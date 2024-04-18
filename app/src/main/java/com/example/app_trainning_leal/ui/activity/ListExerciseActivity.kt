package com.example.app_trainning_leal.ui.activity

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.app_trainning_leal.R
import com.example.app_trainning_leal.databinding.ActivityListExerciseBinding
import com.example.app_trainning_leal.ui.model.Trainning
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class ListExerciseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListExerciseBinding
    private var db = FirebaseFirestore.getInstance()
    private var listTrainning: ArrayList<Trainning> = arrayListOf()
    private lateinit var adapter: ListExerciseActivity.ListExerciseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityListExerciseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = getColor(R.color.colorPrimaryBlack)
        val toolbar: MaterialToolbar = findViewById(R.id.main_toolbar)
        toolbar.setTitle(getString(R.string.txt_new_exercises_title))
        toolbar.setBackgroundColor(getColor(R.color.colorPrimaryBlack))
        setSupportActionBar(toolbar)


        val textTrainning: TextView = findViewById(R.id.txt_title_exercises)
        textTrainning.text = intent.getStringExtra("titleTrainning")

        val trainningId = intent.getStringExtra("trainningId") // Recupere isso

        setupRecyclerView()
        fetchTrainning(trainningId)


    }

    private fun fetchTrainning(trainningId: String?) {

        listTrainning.clear()

        db.collection("trainning")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val trainning = document.toObject(Trainning::class.java)
                    trainning.id = document.id
                    listTrainning.add(trainning)
                }
                setupRecyclerView()
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
            }
    }

    private fun addTrainning(titleTrainning: HashMap<String, Int>, position: Int, trainningReference: String, trainningId: String?) {
        val titleTrainningId = listTrainning[position].id
        val titleTrainningReference = db.collection("titleTrainning").document(titleTrainningId!!)
        titleTrainningReference.update("trainning", FieldValue.arrayUnion(trainningReference))
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully updated!")
                fetchTrainning(trainningId) // Atualiza a lista e a RecyclerView
                Toast.makeText(
                    this@ListExerciseActivity,
                    "Exercício adicionado",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error updating document", e)
            }
    }


    private fun setupRecyclerView() {
        val titleTrainningId = intent.getStringExtra("titleTrainningId")
        adapter = ListExerciseAdapter(listTrainning, titleTrainningId ?: "")
        binding.exerciseRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.exerciseRecyclerView.adapter = adapter

    }



    private inner class ListExerciseAdapter(
        private val listTrainning: ArrayList<Trainning>,
        private val titleTrainningId: String
    ) : RecyclerView.Adapter<ListExerciseAdapter.ListExerciseViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListExerciseViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.exercise_item, parent, false)
            return ListExerciseViewHolder(view)
        }

        override fun onBindViewHolder(holder: ListExerciseViewHolder, position: Int) {
            holder.titleTraining.text = listTrainning[position].nome
            holder.repetitions.text = "${listTrainning[position].repeticoes.toString()}x"
            val trainningId = listTrainning[position].id

            holder.addTrainning.setOnClickListener {
                val titleTrainning = HashMap<String, Int>()
                val trainningId = listTrainning[position].id
                val trainningReference = db.document("trainning/$trainningId").path
                addTrainning(titleTrainning, position, trainningReference, trainningId)

                /*Toast.makeText(
                    this@ListExerciseActivity,
                    "Exercício adicionado",
                    Toast.LENGTH_SHORT
                ).show()*/
                /* val intent = Intent(this@ListExerciseActivity, ListExerciseActivity::class.java)
                 intent.putExtra("titleTrainning", listTrainning[position].nome)
                 startActivity(intent)*/
            }
        }

        override fun getItemCount(): Int = listTrainning.size


        inner class ListExerciseViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview) {
            val titleTraining: TextView = itemview.findViewById(R.id.title_trainning_text)
            val addTrainning: ImageView = itemview.findViewById(R.id.ico_default_circle)
            val repetitions: TextView = itemview.findViewById(R.id.repetitions_trainning_text)
        }
    }


}
