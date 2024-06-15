package ru.mixail_akulov.a22_mvvm_base.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import ru.mixail_akulov.a22_mvvm_base.R
import ru.mixail_akulov.a22_mvvm_base.UserActionListener
import ru.mixail_akulov.a22_mvvm_base.UsersAdapter
import ru.mixail_akulov.a22_mvvm_base.databinding.FragmentUsersListBinding
import ru.mixail_akulov.a22_mvvm_base.model.User
import ru.mixail_akulov.a22_mvvm_base.tasks.EmptyResult
import ru.mixail_akulov.a22_mvvm_base.tasks.ErrorResult
import ru.mixail_akulov.a22_mvvm_base.tasks.PendingResult
import ru.mixail_akulov.a22_mvvm_base.tasks.SuccessResult

// 3.4 наследуем Fragment()
class UsersListFragment : Fragment() {

    // 3.5 Добавляем биндинг и адаптер:
    private lateinit var binding: FragmentUsersListBinding
    private lateinit var adapter: UsersAdapter

    // 6.1 Поле доступа к ViewModel с помощью делегата viewModels().
    // Т.к. у ViewModel есть в конструкторе аргумент, то необходимо будет написать фабрику для ViewModel и добавить ее в делегата.
    private val viewModel: UsersListViewModel by viewModels { factory() }

    // 3.6 Переопределяем onCreateView:
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // 3.7 Инициализируем биндинг:
        binding = FragmentUsersListBinding.inflate(inflater, container, false)

        // 3.8 Задаем адаптер:
//        adapter = UsersAdapter(object : UserActionListener {
//            // 6.2 С помощью ViewModel перемещаем пользователя
//            override fun onUserMove(user: User, moveBy: Int) {
//                viewModel.moveUser(user, moveBy)
//            }
//
//            // 6.3 С помощью ViewModel удаляем пользователя,
//            override fun onUserDelete(user: User) {
//                viewModel.deleteUser(user)
//            }
//
//            // 9.6 Показываем детали при нажатии на пользователя
//            override fun onUserDetails(user: User) {
//                navigator().showDetails(user)
//            }
//
//        })
        // 11.7
        adapter = UsersAdapter(viewModel)


            // 6.4 Подписываемся на данные во ViewModel. Т.к. мы находимся в фрагменте, то первым параметром передаем viewLifecycleOwner,
        // который учитывает жизненный цикл интерфейса фрагмента и перестанет получать данные сразу после onDestroyView().
        // В активити можно передать this. В случае с фрагментом this будет жить и после смерти фрагмента, пока жива активити и можно получить краш.
//        viewModel.users.observe(viewLifecycleOwner, Observer {
//            // 6.5 Сюда мы получаем List<User> и передаем их в адаптер для преобразования в поля для отрисовки
//            adapter.users = it
//        })
        // 11.8 Раньше был просто список юзеров, а теперь UserListItem
        viewModel.users.observe(viewLifecycleOwner, Observer {
            hideAll()
            // проверяем результат
            when (it) {
                is SuccessResult -> {
                    binding.recyclerView.visibility = View.VISIBLE
                    adapter.users = it.data
                }
                is ErrorResult -> {
                    binding.tryAgainContainer.visibility = View.VISIBLE
                }
                is PendingResult -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is EmptyResult -> {
                    binding.noUsersTextView.visibility = View.VISIBLE
                }
            }
        })

        viewModel.actionShowDetails.observe(viewLifecycleOwner, Observer {
            it.getValue()?.let { user -> navigator().showDetails(user) }
        })
        viewModel.actionShowToast.observe(viewLifecycleOwner, Observer {
            it.getValue()?.let { messageRes -> navigator().toast(messageRes) }
        })

        // 4.1.6 Инициализируем recyclerView
        val layoutManager = LinearLayoutManager(requireContext())

        // 3.9 Назначаем layoutManager и adapter:
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapter

        // 3.10 Возвращаем:
        return binding.root
    }

    private fun hideAll() {
        binding.recyclerView.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
        binding.tryAgainContainer.visibility = View.GONE
        binding.noUsersTextView.visibility = View.GONE
    }
}
