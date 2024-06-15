package ru.mixail_akulov.a22_mvvm_base.screens

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.mixail_akulov.a22_mvvm_base.R
import ru.mixail_akulov.a22_mvvm_base.UserActionListener
import ru.mixail_akulov.a22_mvvm_base.model.User
import ru.mixail_akulov.a22_mvvm_base.model.UsersListener
import ru.mixail_akulov.a22_mvvm_base.model.UsersService
import ru.mixail_akulov.a22_mvvm_base.tasks.*

// 11.3.1 Т.к. теперь будет ожидание загрузки,то создаем data class для элемента, который будет содержать одновременно
// и информацию пользователя и буленовскую переменную, которая будет говорить - элемент в обработке или не в обработке.
data class UserListItem(
    val user: User,
    val isInProgress: Boolean
)

// 4.1 Наследуются от ViewModel():
class UsersListViewModel(

    // 4.2 В конструктор желательно передать все зависимости, от которых она зависит.
    // В нашем случае это UsersService, который определяет операции с пользователем.
    private val usersService: UsersService
) : BaseViewModel(), UserActionListener {

    // 4.3 Во ViewModel необходимо описать операции, которые разрешено делать из фрагмента
    // (загрузка пользователя loadUsers, перемещение onUserMove, удаление onUserDelete и запуск экрана с деталями onUserDetails)
    // А также данные, которые ViewModel будет отправлять во фрагмент.

    // 4.4 ViewModel не должна ничего знать ни о активити, ни о фрагментах, а поэтому данные будем передавать с помощью
    // специальной переменной. Эта переменная позволяет не только слушать данные, но также и изменять данные внутри себя.
    // Активити и фрагменты могут подписываться на эти переменные и слушают их изменения. При подписке они сразу получают текущее
    // значение и далее получают все изменения.
    // Т.к. поле приватное, то изменения слушать может делать только ViewModel.
    // При повороте экрана ViewModel выживает, поэтому данные сохраняются
    // private val _users = MutableLiveData<List<User>>()
    // 11.3.2 Т.к. добавляем индикатор загрузки прогресс бар, то меняем User на новый дата класс UserListItem
    // private val _users = MutableLiveData<List<UserListItem>>()
    // 11.3.5 Т.к. загрузка может быть и неудачной, то нам нужен не просто список, а результат загрузки списка
    private val _users = MutableLiveData<Result<List<UserListItem>>>()

    // 4.5 Обычная LiveData, доступ к которой есть у View, т.к. она публичная. Но через неё нельзя изменять список _users.
    // Она уже учитывает жизненный цикл активити и фрагментов. Поэтому отписываться от нее не надо, все отпишутся сами. Когда
    // они восстановятся, то подпишутся заново и получат обновленные значения.
    // val users: LiveData<List<User>> = _users
    // val users: LiveData<List<UserListItem>> = _users
    val users: LiveData<Result<List<UserListItem>>> = _users

    //11.7 LiveData для запуска экрана деталей
    private val _actionShowDetails = MutableLiveData<Event<User>>()
    val actionShowDetails: LiveData<Event<User>> = _actionShowDetails

    //11.7 LiveData для показа тост сообщений
    private val _actionShowToast = MutableLiveData<Event<Int>>()
    val actionShowToast: LiveData<Event<Int>> = _actionShowToast

    // 11.3.3 Информацию о прогрессе будем хранить во viewModel: userIdsInProgress - список id пользователей, которые в обработке
    private val userIdsInProgress = mutableListOf<Long>()

    // 11.3.4 Из-за того, что у нас из модели приходит тип список пользователей, а нам нужно будет его потом еще и
    // преобразовывать в список List<UserListItem>, то добавим отдельную переменную для хранения текущего результата.
    private var usersResult: Result<List<User>> = EmptyResult()
        set(value) {
            field = value
            notifyUpdates()
        }

    // 5.3 Создаем слушателя списка пользователей. В методе loadUsers() добавляем его в список слушателей в UsersService,
    // и получаемые значения при каких-либо изменениях назначаем приватной LiveData _users, которая передает их в публичную LiveData,
    // из которой их получают фрагменты для отрисовки.
//    private val listener: UsersListener = {
//        _users.value = it
//    }
    // 11.3.9
    private val listener: UsersListener = {
        usersResult = if (it.isEmpty()) {
            EmptyResult()
        } else {
            SuccessResult(it)
        }
    }

    // 5.5 вызываем при первом обращении к ViewModel для загрузки начального списка пользователей
    // 11.3.10 переносим сюда usersService.addListener(listener)
    init {
        usersService.addListener(listener)
        loadUsers()
    }

    // 5.4 отписываемся от прослушивания изменений, т.к. ViewModel умрет раньше, чем App класс UsersService, и надо остановить передачу данных,
    // чтобы не было утечек памяти.
    override fun onCleared() {
        super.onCleared()
        usersService.removeListener(listener)
    }

    // 4.6 Методы действий с пользователями
    // 5.1 передаем в метод класса UsersService добавление слушателя изменений в списке пользователей
//    private fun loadUsers() {
//        usersService.addListener(listener)
//    }

    //11.3.11 Сразу указываем на загрузку и далее обрабатываем только ошибку, т.к. успех обработан уже в инит
    private fun loadUsers() {
        usersResult = PendingResult()
        usersService.loadUsers()
            .onError {
                usersResult = ErrorResult(it)
            }
            .autoCancel()
    }

    // 5.2 передаем реализацию в класс UsersService, где прописана соответствующая логика по перемещению пользователя
    // 11.3.12 Мы будем разрешать выполнять операцию на текущем пользователе только в том случае, если с ним не выполняется никакой другой операции.
    override fun onUserMove(user: User, moveBy: Int) {
        if (isInProgress(user)) return
        addProgressTo(user)
        usersService.moveUser(user, moveBy)
            .onSuccess {
                removeProgressFrom(user)
            }
            .onError {
                removeProgressFrom(user)
                _actionShowToast.value = Event(R.string.cant_move_user)
            }
            .autoCancel()
    }

    //11.3.13
    override fun onUserDelete(user: User) {
        if (isInProgress(user)) return
        addProgressTo(user)
        usersService.deleteUser(user)
            .onSuccess {
                removeProgressFrom(user)
            }
            .onError {
                removeProgressFrom(user)
                _actionShowToast.value = Event(R.string.cant_delete_user)
            }
            .autoCancel()
    }

    override fun onUserDetails(user: User) {
        _actionShowDetails.value = Event(user)
    }

    // 11.3.8 Создадим несколько приватных методов, которые будут добавлять прогресс бар пользователю, удалять
    // прогресс барс пользователя и также метод, который будет возвращать является
    // ли данный пользователь сейчас в процессе обработки.
    private fun addProgressTo(user: User) {
        userIdsInProgress.add(user.id)
        notifyUpdates()
    }

    private fun removeProgressFrom(user: User) {
        userIdsInProgress.remove(user.id)
        notifyUpdates()
    }

    private fun isInProgress(user: User): Boolean {
        return userIdsInProgress.contains(user.id)
    }


    // 11.3.7 Таким образом теперь мы можем взять результат, который у нас пришел из модели userResult, и в случае
    // если он равен успеху, то мы преобразовываем данные внутри него.
    private fun notifyUpdates() {
//        _users.value = usersResult.map { users ->
//            // users.map { user -> UserListItem(user, userIdsInProgress.contains(user.id)) }
//            // 11.3.9 заменим на метод isInProgress
//            users.map { user -> UserListItem(user, isInProgress(user)) }
//        }
        // 11.4 Мы делаем .postValue, и .postValue уже у себя внутри эти данные
        // передаст дальше в LiveData во фрагменте уже в главном потоке не зависимо от того, где мы находимся.
        _users.postValue(usersResult.map { users ->
            // users.map { user -> UserListItem(user, userIdsInProgress.contains(user.id)) }
            // 11.3.9 заменим на метод isInProgress
            users.map { user -> UserListItem(user, isInProgress(user)) }
        })
    }

}
