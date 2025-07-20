/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.weather.ui.mymodel

import android.weather.data.remote.model.WeatherResponse
import android.weather.data.remote.repository.WeatherRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject


@HiltViewModel
class MyModelViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MyModelUiState>(MyModelUiState.NoLoading)
    val uiState: StateFlow<MyModelUiState> = _uiState

    fun getWeather(cityName: String) {
        viewModelScope.launch {
            _uiState.value = MyModelUiState.Loading
            try {
                val response = weatherRepository.fetchWeather(cityName)
                _uiState.value = MyModelUiState.Success(response)
            } catch (e: HttpException) { when (e.code()) {
                    404 -> _uiState.value = MyModelUiState.Error("Invalid city")
                    else -> _uiState.value = MyModelUiState.Error("Server error: ${e.code()}")
                }
            } catch (e: IOException) {
                _uiState.value = MyModelUiState.Error("Network error: ${e.message}")
            } catch (e: Exception) {
                _uiState.value = MyModelUiState.Error("Unexpected error: ${e.message}")
            }
        }
    }
}

sealed interface MyModelUiState {
    object Loading : MyModelUiState
    object NoLoading : MyModelUiState
    data class Error(val data: String) : MyModelUiState
    data class Success(val data: WeatherResponse) : MyModelUiState
}
