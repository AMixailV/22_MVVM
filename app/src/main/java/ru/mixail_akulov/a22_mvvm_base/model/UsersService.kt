package ru.mixail_akulov.a22_mvvm_base.model

import com.github.javafaker.Faker
import ru.mixail_akulov.a22_mvvm_base.UserNotFoundException
import ru.mixail_akulov.a22_mvvm_base.tasks.SimpleTask
import ru.mixail_akulov.a22_mvvm_base.tasks.SuccessResult
import ru.mixail_akulov.a22_mvvm_base.tasks.Task
import java.util.*
import java.util.concurrent.Callable

typealias UsersListener = (users: List<User>) -> Unit

class UsersService {

    private var users = mutableListOf<User>()
    // 11.2.2 Переменная, которая говорит, когда список загружен, а когда нет.
    private var loaded = false

    private val listeners = mutableSetOf<UsersListener>()

    // 11.1.4 Теперь методы обрабатывающие логику, будут возвращать таски
    // <Unit> - значит по логике ничего метод не возвращает
    // И переименуем getUsers() в loadUsers()
    // 11.2.1 Возвращаем SimpleTask. В Callable передаемлюбой код,который выполняется асинхронно более, чем несколько милисекунд
    fun loadUsers(): Task<Unit> = SimpleTask<Unit>(Callable {
        // задержка
        Thread.sleep(2000)
        // переносим код из блока init
        val faker = Faker.instance()
        IMAGES.shuffle()
        users = (1..100).map { User(
            id = it.toLong(),
            name = faker.name().name(),
            company = faker.company().name(),
            photo = IMAGES[it % IMAGES.size]
        ) }.toMutableList()
        // сообщаем, что список загружен
        loaded = true
        notifyChanges()
    })

    // 8.5 Метод для получения пользователя по id и возврат класса с деталями о пользователе
    // 11.2.4 Оборачиваем в SimpleTask, так же, как и остальные методы действий ниже
    fun getById(id: Long): Task<UserDetails> = SimpleTask<UserDetails>(Callable{

        Thread.sleep(2000)
        // 8.6 Находим пользователя по id, и если пользователя не существует,то выбросиме исключение, предварительно
        // создав класс для исключения
        val user = users.firstOrNull { it.id == id } ?: throw UserNotFoundException()

        // 8.7 Если пользователя нашли то создаем UserDetails, куда включаем нашего пользователя,
        // а также сгенерированные случайные данные с помощью Faker. Будут сгенерированы 3 абзаца с текстом,
        // которые будут объединены с переводом строки.
        return@Callable UserDetails(
            user = user,
            details = Faker.instance().lorem().paragraphs(3).joinToString("\n\n")
        )
    })

    fun deleteUser(user: User): Task<Unit> = SimpleTask<Unit> (Callable {
        Thread.sleep(2000)
        val indexToDelete = users.indexOfFirst { it.id == user.id }
        if (indexToDelete != -1) {
            users.removeAt(indexToDelete)
            notifyChanges()
        }
    })

    fun moveUser(user: User, moveBy: Int): Task<Unit> = SimpleTask<Unit>(Callable {
        Thread.sleep(2000)
        val oldIndex = users.indexOfFirst { it.id == user.id }
        if (oldIndex == -1) return@Callable
        val newIndex = oldIndex + moveBy
        if (newIndex < 0 || newIndex >= users.size) return@Callable
        Collections.swap(users, oldIndex, newIndex)
        notifyChanges()
    })

    fun addListener(listener: UsersListener) {
        listeners.add(listener)
        // 12.2.3 проверяем есть ли список прежде чем вызывать слушателя
        if (loaded) {
            listener.invoke(users)
        }
    }

    fun removeListener(listener: UsersListener) {
        listeners.remove(listener)
    }

    private fun notifyChanges() {
        if (!loaded) return
        listeners.forEach { it.invoke(users) }
    }

    companion object {
        private val IMAGES = mutableListOf(
            "https://images.unsplash.com/photo-1600267185393-e158a98703de?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&ixid=MnwxfDB8MXxyYW5kb218fHx8fHx8fHwxNjI0MDE0NjQ0&ixlib=rb-1.2.1&q=80&utm_campaign=api-credit&utm_medium=referral&utm_source=unsplash_source&w=800",
            "https://images.unsplash.com/photo-1579710039144-85d6bdffddc9?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&ixid=MnwxfDB8MXxyYW5kb218fHx8fHx8fHwxNjI0MDE0Njk1&ixlib=rb-1.2.1&q=80&utm_campaign=api-credit&utm_medium=referral&utm_source=unsplash_source&w=800",
            "https://images.unsplash.com/photo-1488426862026-3ee34a7d66df?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&ixid=MnwxfDB8MXxyYW5kb218fHx8fHx8fHwxNjI0MDE0ODE0&ixlib=rb-1.2.1&q=80&utm_campaign=api-credit&utm_medium=referral&utm_source=unsplash_source&w=800",
            "https://images.unsplash.com/photo-1620252655460-080dbec533ca?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&ixid=MnwxfDB8MXxyYW5kb218fHx8fHx8fHwxNjI0MDE0NzQ1&ixlib=rb-1.2.1&q=80&utm_campaign=api-credit&utm_medium=referral&utm_source=unsplash_source&w=800",
            "https://images.unsplash.com/photo-1613679074971-91fc27180061?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&ixid=MnwxfDB8MXxyYW5kb218fHx8fHx8fHwxNjI0MDE0NzUz&ixlib=rb-1.2.1&q=80&utm_campaign=api-credit&utm_medium=referral&utm_source=unsplash_source&w=800",
            "https://images.unsplash.com/photo-1485795959911-ea5ebf41b6ae?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&ixid=MnwxfDB8MXxyYW5kb218fHx8fHx8fHwxNjI0MDE0NzU4&ixlib=rb-1.2.1&q=80&utm_campaign=api-credit&utm_medium=referral&utm_source=unsplash_source&w=800",
            "https://images.unsplash.com/photo-1545996124-0501ebae84d0?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&ixid=MnwxfDB8MXxyYW5kb218fHx8fHx8fHwxNjI0MDE0NzY1&ixlib=rb-1.2.1&q=80&utm_campaign=api-credit&utm_medium=referral&utm_source=unsplash_source&w=800",
            "https://images.unsplash.com/flagged/photo-1568225061049-70fb3006b5be?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&ixid=MnwxfDB8MXxyYW5kb218fHx8fHx8fHwxNjI0MDE0Nzcy&ixlib=rb-1.2.1&q=80&utm_campaign=api-credit&utm_medium=referral&utm_source=unsplash_source&w=800",
            "https://images.unsplash.com/photo-1567186937675-a5131c8a89ea?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&ixid=MnwxfDB8MXxyYW5kb218fHx8fHx8fHwxNjI0MDE0ODYx&ixlib=rb-1.2.1&q=80&utm_campaign=api-credit&utm_medium=referral&utm_source=unsplash_source&w=800",
            "https://images.unsplash.com/photo-1546456073-92b9f0a8d413?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&ixid=MnwxfDB8MXxyYW5kb218fHx8fHx8fHwxNjI0MDE0ODY1&ixlib=rb-1.2.1&q=80&utm_campaign=api-credit&utm_medium=referral&utm_source=unsplash_source&w=800"
        )
    }
}