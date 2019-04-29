package fr.delcey.mvvm_clean_archi.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.delcey.mvvm_clean_archi.usecases.WeatherUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel(private val useCase: WeatherUseCase) : ViewModel() {

    // We expose a LiveData but manipulate a MutableLiveData internaly
    private val _weatherLiveData = MutableLiveData<String>()
    val weatherLiveData: LiveData<String> = _weatherLiveData

    // We keep reference to previous job to cancel it if necessary
    private var currentJob: Job? = null

    fun getWeather(city: String?) {

        // Cancel a previous job if a new key has been pressed by user during the 500 ms waiting phase
        currentJob?.let {
            if (it.isActive) {
                it.cancel()
            }
        }

        // Coroutine stuff, look this up !!
        currentJob = viewModelScope.launch(Dispatchers.IO) {

            // Let user finish typing before querying server (don't congest it)
            delay(500)

            // Query server
            val weatherResponse = useCase.getWeather(city)
            if (weatherResponse != null) {
                val weatherText = "Dans la ville ${weatherResponse.cityName}, il fait ${weatherResponse.weatherValues.temperature}°C"

                _weatherLiveData.postValue(weatherText)
            } else {
                _weatherLiveData.postValue(
                    "$city est une ville inconnue au bataillon, entrez une vraie ville svp. " +
                            "Ou connectez-vous aux internets. "
                )
            }
        }
    }
}