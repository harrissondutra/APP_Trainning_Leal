package com.example.app_trainning_leal.ui.model

import com.google.firebase.firestore.DocumentReference

data class TitleTrainning(
    var id: String? = null,
    val nome: String? = null,
    val trainning: List<DocumentReference>? = null
)
