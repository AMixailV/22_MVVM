package ru.mixail_akulov.a22_mvvm_base.screens

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.mixail_akulov.a22_mvvm_base.R
import ru.mixail_akulov.a22_mvvm_base.UserNotFoundException
import ru.mixail_akulov.a22_mvvm_base.model.UserDetails
import ru.mixail_akulov.a22_mvvm_base.model.UsersService
import ru.mixail_akulov.a22_mvvm_base.tasks.EmptyResult
import ru.mixail_akulov.a22_mvvm_base.tasks.Result
import ru.mixail_akulov.a22_mvvm_base.tasks.PendingResult
import ru.mixail_akulov.a22_mvvm_base.tasks.SuccessResult

// 8.1 в конструктор передаем UsersService
class UserDetailsViewModel(
    private val usersService: UsersService
    ) : BaseViewModel() {

    // 8.2 Определяем LiveData, которые будут содержать в себе пользователя с деталями
//    private val _userDetails = MutableLiveData<UserDetails>()
//    val userDetails: LiveData<UserDetails> = _userDetails
    //11.6
    private val _state = MutableLiveData<State>()
    val state: LiveData<State> = _state

    // 11.7 LiveData для показа тост сообщений
    private val _actionShowToast = MutableLiveData<Event<Int>>()
    val actionShowToast: LiveData<Event<Int>> = _actionShowToast

    // 11.7 LiveData для перехода на предыдущий экран
    private val _actionGoBack = MutableLiveData<Event<Unit>>()
    val actionGoBack: LiveData<Event<Unit>> = _actionGoBack

    private val currentState: State get() = state.value!!

    init {
        _state.value = State(
            userDetailsResult = EmptyResult(),
            deletingInProgress = false
        )
    }

    // 8.3 Загружаем пользователя по id, предварительно добавив в UserService метод для получения пользователя по id
    // и дата класс UserDetails для добавления поля деталей к пользователю и добавляем его в LiveData
//    fun loadUser(userId: Long) {
//        if (_state.value != null) return
//        try {
//            _state.value = usersService.getById(userId)
//        } catch (e: UserNotFoundException) {
//            e.printStackTrace()
//        }
//    }

    // 11.6
    fun loadUser(userId: Long) {
        if (currentState.userDetailsResult is SuccessResult) return

        _state.value = currentState.copy(userDetailsResult = PendingResult())

        usersService.getById(userId)
            .onSuccess {
                _state.value = currentState.copy(userDetailsResult = SuccessResult(it))
            }
            .onError {
                // 11.7
                _actionShowToast.value = Event(R.string.cant_load_user_details)
                _actionGoBack.value = Event(Unit)
            }
            .autoCancel()
    }

    // 8.4 Удаление пользователя. loadUser будет вызываться при старте и user появится в LiveData,
    // поэтому тут id уже не надо указывать. Проверяем наличие пользователя и удаляем его
//    fun deleteUser() {
//        val userDetails = this.state.value ?: return
//        usersService.deleteUser(userDetails.user)
//
//    }

    // 11.6
    fun deleteUser() {
        val userDetailsResult = currentState.userDetailsResult
        if (userDetailsResult !is SuccessResult) return
        _state.value = currentState.copy(deletingInProgress = true)
        usersService.deleteUser(userDetailsResult.data.user)
            .onSuccess {
                _actionShowToast.value = Event(R.string.user_has_been_deleted)
                _actionGoBack.value = Event(Unit)
            }
            .onError {
                _state.value = currentState.copy(deletingInProgress = false)
                _actionShowToast.value = Event(R.string.cant_delete_user)
            }
            .autoCancel()
    }

    // 11.6
    data class State(
        val userDetailsResult: Result<UserDetails>,
        private val deletingInProgress: Boolean
    ) {

        val showContent: Boolean get() = userDetailsResult is SuccessResult
        val showProgress: Boolean get() = userDetailsResult is PendingResult || deletingInProgress
        val enableDeleteButton: Boolean get() = !deletingInProgress

    }

}