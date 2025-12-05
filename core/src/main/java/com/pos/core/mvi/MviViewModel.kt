package com.pos.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * MVI ViewModel基类
 * 处理Intent、State和Effect的流程
 */
abstract class MviViewModel<I : MviIntent, S : MviState, E : MviEffect>(
    initialState: S
) : ViewModel() {

    // State管理
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    // Effect管理（单次发送）
    private val _effects = Channel<E>(capacity = Channel.BUFFERED)
    val effects: Flow<E> = _effects.receiveAsFlow()

    // 当前State
    protected val currentState: S
        get() = _state.value

    /**
     * 处理Intent
     */
    fun handleIntent(intent: I) {
        viewModelScope.launch {
            processIntent(intent)
        }
    }

    /**
     * 子类实现的Intent处理逻辑
     */
    protected abstract suspend fun processIntent(intent: I)

    /**
     * 更新State
     */
    protected fun setState(newState: S) {
        _state.value = newState
    }

    /**
     * 发送一次性Effect
     */
    protected suspend fun sendEffect(effect: E) {
        _effects.send(effect)
    }

    /**
     * 以函数式方式更新State
     */
    protected fun updateState(reducer: (S) -> S) {
        _state.value = reducer(currentState)
    }
}
