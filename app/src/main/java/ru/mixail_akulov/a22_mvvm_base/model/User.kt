package ru.mixail_akulov.a22_mvvm_base.model

data class User(
    val id: Long,
    val photo: String,
    val name: String,
    val company: String
)

// 8.6 Создаем расширенный дата класс с деталями о пользователе
data class UserDetails(
    val user: User,
    val details: String
)