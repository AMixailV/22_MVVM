package ru.mixail_akulov.a22_mvvm_base

import ru.mixail_akulov.a22_mvvm_base.model.User

interface Navigator {

    // 3.1 Список деталей для каждого пользователя
    fun showDetails(user: User)

    // 3.2 Выход на один экран назад
    fun goBack()

    // 3.3 Метод, чтобы показывать тост сообщения.
    fun toast(messageRes: Int)

}
