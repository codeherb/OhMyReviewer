package io.yogiyo.ohmyreviewer.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI 상태를 나타내는 마커 인터페이스
 * 각 화면의 State data class가 이를 구현합니다.
 */
interface UiState

/**
 * 사용자 인텐트(액션)를 나타내는 마커 인터페이스
 * 각 화면의 Event sealed interface가 이를 구현합니다.
 */
interface UiEvent

/**
 * 일회성 사이드 이펙트를 나타내는 마커 인터페이스 (네비게이션, 스낵바, 토스트 등)
 * 각 화면의 Effect sealed interface가 이를 구현합니다.
 */
interface UiEffect

/**
 * MVI 패턴을 따르는 베이스 ViewModel
 *
 * @param S UI 상태 타입
 * @param E 사용자 이벤트 타입
 * @param F 사이드 이펙트 타입
 * @param initialState 초기 UI 상태
 */
abstract class MviViewModel<S : UiState, E : UiEvent, F : UiEffect>(
    initialState: S,
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effect = Channel<F>(Channel.BUFFERED)
    val effect: Flow<F> = _effect.receiveAsFlow()

    /**
     * UI에서 발생한 이벤트를 처리합니다.
     */
    fun onEvent(event: E) {
        handleEvent(event)
    }

    /**
     * 각 화면의 ViewModel에서 이벤트를 처리하는 로직을 구현합니다.
     */
    protected abstract fun handleEvent(event: E)

    /**
     * UI 상태를 업데이트합니다.
     * reducer 함수를 통해 현재 상태를 기반으로 새 상태를 생성합니다.
     */
    protected fun updateState(reducer: S.() -> S) {
        _state.update(reducer)
    }

    /**
     * 일회성 사이드 이펙트를 발생시킵니다.
     */
    protected fun sendEffect(effect: F) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}
