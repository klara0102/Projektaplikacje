package com.example.projektaplikacje.firebasee


data class User(
    val email: String = "",
    val name: String? = null,
    val id: String = "",
    val phoneNumber: String = "",
    val dateOfBirth: String = "",
    val address: Map<String, String> = emptyMap(),
    val profilePictureUrl: String = "",
    val interests: List<String> = emptyList()
) {
    companion object {
        fun fromMap(data: Map<String, Any>): User {
            val addressMap = data["address"] as? Map<String, Any> ?: emptyMap()

            return User(
                email = data["email"] as? String ?: "",
                name = data["name"] as? String,
                id = data["id"] as? String ?: "",
                phoneNumber = data["phoneNumber"] as? String ?: "",
                dateOfBirth = data["dateOfBirth"] as? String ?: "",
                profilePictureUrl = data["profilePictureUrl"] as? String ?: "",
                interests = (data["interests"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                address = mapOf(
                    "city" to (addressMap["city"] as? String ?: ""),
                    "street" to (addressMap["street"] as? String ?: ""),
                    "postcode" to (addressMap["postcode"] as? String ?: "")
                )
            )
        }

        fun toMap(user: User): Map<String, Any> {
            return mapOf(
                "email" to user.email,
                "name" to (user.name ?: ""),
                "id" to user.id,
                "phoneNumber" to user.phoneNumber,
                "dateOfBirth" to user.dateOfBirth,
                "profilePictureUrl" to user.profilePictureUrl,
                "interests" to user.interests,
                "address" to user.address
            )
        }
    }
}
