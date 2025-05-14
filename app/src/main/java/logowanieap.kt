val email = binding.emailEditText.text.toString()
val password = binding.passwordEditText.text.toString()

FirebaseAuth.getInstance()
.signInWithEmailAndPassword(email, password)
.addOnCompleteListener { task ->
    if (task.isSuccessful) {
        // Przejdź do głównego ekranu
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    } else {
        Toast.makeText(this, "Błąd logowania: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
    }
}
