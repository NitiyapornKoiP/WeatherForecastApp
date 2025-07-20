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

import android.weather.data.remote.model.Clouds
import android.weather.data.remote.model.Coord
import android.weather.data.remote.model.Main
import android.weather.data.remote.model.Sys
import android.weather.data.remote.model.Weather
import android.weather.data.remote.model.WeatherResponse
import android.weather.data.remote.model.Wind
import android.weather.ui.theme.MyApplicationTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun MyModelScreen(modifier: Modifier = Modifier, viewModel: MyModelViewModel = hiltViewModel()) {
    val items by viewModel.uiState.collectAsStateWithLifecycle()
    MyModelScreen(
        uiState = items,
        onSave = viewModel::getWeather,
        modifier = modifier
    )
}

@Composable
internal fun MyModelScreen(
    uiState: MyModelUiState,
    onSave: (name: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(24.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        var nameMyModel by remember { mutableStateOf("") }

        Text(
            text = "Search Weather by City",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp),
            color = MaterialTheme.colorScheme.primary
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .weight(1f),
                value = nameMyModel,
                label = { Text("City Name") },
                onValueChange = { nameMyModel = it },
                singleLine = true
            )

            Button(
                modifier = Modifier
                    .height(56.dp)
                    .align(Alignment.CenterVertically),
                onClick = { onSave(nameMyModel) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                Text("Search")
            }
        }

        when (uiState) {
            is MyModelUiState.Success -> {
                MyDataScreen(
                    items = uiState.data,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            is MyModelUiState.Error -> {
                Text(
                    text = "⚠️ ${uiState.data}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            is MyModelUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 24.dp)
                )
            }

            MyModelUiState.NoLoading -> {}
        }
    }
}

@Composable
fun MyDataScreen(
    items: WeatherResponse,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE0F7FA)
        ),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = items.name,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = items.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Divider()

            Spacer(modifier = Modifier.height(12.dp))

            InfoRow(label = "Timezone", value = "${items.timezone}")
            InfoRow(label = "Temp", value = "${items.main.temp.toCelsius()} °C")
            InfoRow(
                label = "Temp Range",
                value = "${items.main.temp_min.toCelsius()} - ${items.main.temp_max.toCelsius()} °C"
            )
            InfoRow(label = "Humidity", value = "${items.main.humidity}%")
            InfoRow(label = "Pressure", value = "${items.main.pressure} hPa")

            Spacer(modifier = Modifier.height(12.dp))
            Divider()
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Wind",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            InfoRow(label = "Speed", value = "${items.wind.speed} m/s")
            InfoRow(label = "Direction", value = "${items.wind.deg}°")
            items.wind.gust?.let {
                InfoRow(label = "Gust", value = "$it m/s")
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider()
            Spacer(modifier = Modifier.height(12.dp))

            InfoRow(label = "Clouds", value = "${items.clouds.all}%")
            InfoRow(
                label = "Geo coords",
                value = "[ ${items.coord.lat}, ${items.coord.lon} ]"
            )
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

fun Double.toCelsius(): String {
    val celsius = this - 273.15
    return String.format("%.1f", celsius)
}

// Previews
@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    MyApplicationTheme {
        MyModelScreen(
            uiState = MyModelUiState.Success(
                data = WeatherResponse(
                    name = "Bangkok",
                    cod = 200,
                    id = 1,
                    timezone = 25200,
                    coord = Coord(100.5, 13.75),
                    weather = listOf(Weather(800, "Clear", "clear sky", "01d")),
                    base = "stations",
                    main = Main(303.18, 307.1, 303.18, 303.18, 1002, 65, 1002, 1001),
                    visibility = 10000,
                    wind = Wind(5.7, 209, 9.86),
                    clouds = Clouds(100),
                    dt = 1753007154,
                    sys = Sys("TH", 1752965953, 1753012137),
                )
            ),
            onSave = {}
        )
    }
}


