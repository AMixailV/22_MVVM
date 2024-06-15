package ru.mixail_akulov.a22_mvvm_base.tasks

sealed class Result<T> {

    // 11.3.6 в методе map() объявляем функцию mapper, которая будет принимать тип T, а отдавать тип R.
    // если this is SuccessResult, то мы с помощью mapper преобразуем данные, которые есть в
    // SuccessResult типа Т, преобразуем в тип R.
    @Suppress("UNCHECKED_CAST")
    fun <R> map(mapper: (T) -> R): Result<R> {
        if (this is SuccessResult) return SuccessResult(mapper(data))
        return this as Result<R>
    }

}

// 11.1.9 Используем sealed class для того, чтобы определить разные типы результатов:
// - общий класс Result, который может нести в себе какие-то данные <T>.
// - SuccessResult на случай, если операция завершилась успешно, и она содержит какие-то данные. Наследуемся от общей задачи.
// - ErrorResult, который будет содержать ошибку.
// - PendingResult, который говорит, что задача еще выполняется
// - EmptyResult - задача еще не запускалась.
class SuccessResult<T>(
    val data: T
) : Result<T>()

class ErrorResult<T>(
    val error: Throwable
) : Result<T>()

class PendingResult<T> : Result<T>()

class EmptyResult<T> : Result<T>()