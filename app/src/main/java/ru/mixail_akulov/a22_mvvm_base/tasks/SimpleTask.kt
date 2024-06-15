package ru.mixail_akulov.a22_mvvm_base.tasks

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future

// 11.1.5 Выполняет наши задачи асинхронно
// 11.1.7 ExecutorsService возвращает feature, с помощью которого мы может отменить задачи или получить результаты.
private val executorService = Executors.newCachedThreadPool()

// 11.1.6 Создаем handler, чтобы наши слушатели всегда вызывались в главном потоке.
private val handler = Handler(Looper.getMainLooper())

/**
 * Выполнение кода [callable] в отдельном потоке.
 * Вызываемые результаты доставляются в обратные вызовы, назначенные через [onSuccess] и [onError]
 */
class SimpleTask<T>(
    private val callable: Callable<T>
) : Task<T> {

    private val future: Future<*>

    // 11.1.10 Добавляем переменную result. Т.к. задачу сразу стартуем в init{}, то сразу в переменную передаем PendingResult и заполняем try_catch.
    private var result: Result<T> = PendingResult()

    // 11.1.8 В блоке init{} запустим задачу на выполнение в блоке try_catch.
    //                - в случае успеха надо запомнить результат успеха
    //                - в случае ошибки надо запомнить результат ошибки
    //                - после чего оповестить слушателей
    // Для этого создадим специальный тип, который будет содержать результат.
    init {
        future = executorService.submit {
            result = try {
                SuccessResult(callable.call()) // 11.1.11 передаем callable.call() для успешного выполнения
            } catch (e: Throwable) {
                ErrorResult(e)   // 11.1.12 если не выполнится успешно, то перехватится ошибка
            }
            notifyListeners()
        }
    }

    // 11.1.13 добавляем поля для слушателей, и обнуляем их, т.к. вначале слушателей нет
    private var valueCallback: Callback<T>? = null
    private var errorCallback: Callback<Throwable>? = null

    // 11.1.14 Регистрируем слушателя для успеха
    override fun onSuccess(callback: Callback<T>): Task<T> {
        this.valueCallback = callback
        notifyListeners()
        return this
    }

    // 11.1.15 Регистрируем слушателя для ошибки
    override fun onError(callback: Callback<Throwable>): Task<T> {
        this.errorCallback = callback
        notifyListeners()
        return this
    }

    override fun cancel() {
        // 11.1.18 Обнуляем слушателей
        clear()
        // и закрываем future
        future.cancel(true)
    }

    override fun await(): T {
        // 11.1.19 .get() заставляет ждать пока асинхронно не выполнится блок init
        future.get()
        val result = this.result
        if (result is SuccessResult) return result.data
        else throw (result as ErrorResult).error
    }

    // 11.1.16 метод уведомления слушателей. Т.к. задача может быть выполнена в блоке init еще до того, как назначили слушателей,
    // то необходимо данный метод вызывать и в блоке init
    private fun notifyListeners() {
        // вызываем в главном потоке
        handler.post {
            val result = this.result
            val callback = this.valueCallback
            val errorCallback = this.errorCallback

            if (result is SuccessResult && callback != null) {
                callback(result.data)
                clear()
            } else if (result is ErrorResult && errorCallback != null) {
                errorCallback.invoke(result.error)
                clear()
            }
        }
    }

    // 11.1.17 Очищаем колбеки
    private fun clear() {
        valueCallback = null
        errorCallback = null
    }
}