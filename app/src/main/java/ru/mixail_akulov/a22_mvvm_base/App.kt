package ru.mixail_akulov.a22_mvvm_base

import android.app.Application
import ru.mixail_akulov.a22_mvvm_base.model.UsersService

class App : Application() {

    val usersService = UsersService()
}