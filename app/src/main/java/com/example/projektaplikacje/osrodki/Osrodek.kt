package com.example.projektaplikacje.osrodki

data class Osrodek(
    val Adres: String = "",
    val Ośrodek: String = "",
    val Schorzenie: String = "",  // z Firestore
    val Telefon: String = "",
    var schorzenia: List<String> = emptyList()  // używane lokalnie
)