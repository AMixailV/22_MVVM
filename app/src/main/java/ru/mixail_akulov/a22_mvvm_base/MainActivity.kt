package ru.mixail_akulov.a22_mvvm_base

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import ru.mixail_akulov.a22_mvvm_base.databinding.ActivityMainBinding
import ru.mixail_akulov.a22_mvvm_base.model.User
import ru.mixail_akulov.a22_mvvm_base.model.UsersListener
import ru.mixail_akulov.a22_mvvm_base.model.UsersService
import ru.mixail_akulov.a22_mvvm_base.screens.UserDetailsFragment
import ru.mixail_akulov.a22_mvvm_base.screens.UsersListFragment

// 9.1 реализовываем Navigator
class MainActivity : AppCompatActivity(), Navigator {

    // 6.5 удаляем все, кроме биндинга и onCreate
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 6.6 В контейнер для фрагментов в майн_активити загружаем фрагмент UsersListFragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragmentContainer, UsersListFragment())
                .commit()
        }

    }

    // 9.2 запускаем фрагмент для показа деталей пользователя
    override fun showDetails(user: User) {
            supportFragmentManager.beginTransaction()
                .addToBackStack(null) // добавляем, чтобы можно было выйти назад по системной стрелке
                .replace(R.id.fragmentContainer, UserDetailsFragment.newInstance(user.id))
                .commit()
    }

    // 9.3 Системный метод возврата назад
    override fun goBack() {
        onBackPressedDispatcher.onBackPressed()
    }

    // 9.4 Показ коротких сообщений
    override fun toast(messageRes: Int) {
        Toast.makeText(this, messageRes, Toast.LENGTH_SHORT).show()
    }
}