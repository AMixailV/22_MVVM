package ru.mixail_akulov.a22_mvvm_base.screens

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.mixail_akulov.a22_mvvm_base.App
import ru.mixail_akulov.a22_mvvm_base.Navigator

// 7.1 Создаем файл для работы с фрагментами: FragmentUtils, в котором и создадим фабрику, задачей которой
//        будет создание viewModels с параметрами, которые передаются в конструктор.
//        Т.к. фабрике передаются обычно классы из слоя model, то фабрике надо знать, где брать эти классы.
//        В нашем случае это UserService (синглтон) и находится он в App. И если мы будем создавать еще классы,
//        например AccountService, NoteService и пр, то мы их тоже будем добавлять в класс App, поэтому в конструктор фабрики
//        мы можем передать класс App.
// 7.2 Наследуемся от ViewModelProvider.Factory
class ViewModelFactory(
    private val app: App
) : ViewModelProvider.Factory {

    // 7.3 Реализуем создание фабрики. В конструктор приходит класс ViewModel и в результате надо отдать саму созданную ViewModel.
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        // 7.4 С помощью оператора when() внутри, которого передаем тот modelClass, который пришел.
        val viewModel = when (modelClass) {

            // 7.5 И далее сравниваем. Если modelClass == UsersListViewModel, то мы создаем
            // UsersListViewModel и передаем ей usersService, который находится в классе app.
            UsersListViewModel::class.java -> {
                UsersListViewModel(app.usersService)
            }

            // 8.14 Добавляем создание новой ViewModel
            UserDetailsViewModel::class.java -> {
                UserDetailsViewModel(app.usersService)
            }

            // 7.6 Если нам передали непонятно что, то выбрасываем исключение.
            else -> {
                throw IllegalStateException("Unknown view model class")
            }
        }
        // 7.7 Приводим к возвращаемому общему типу Т
        return viewModel as T
    }
}

// 7.7 Экстейшн метод для более короткой записи вызова создания фабрики
// Т.к. App зарегистрирован в манифесте как класс приложения, то App и есть applicationContext
fun Fragment.factory() = ViewModelFactory(requireContext().applicationContext as App)

// 9.5 Короткое обращение к Навигатору
fun Fragment.navigator() = requireActivity() as Navigator