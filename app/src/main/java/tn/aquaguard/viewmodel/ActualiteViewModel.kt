package tn.aquaguard.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.Response
import tn.aquaguard.models.Actualites
import tn.aquaguard.models.Avis
import tn.aquaguard.models.SearchRequest
import tn.aquaguard.repository.ActualiteRepository

class ActualiteViewModel: ViewModel() {

    private val repository =ActualiteRepository()
    var errorMessage: String by mutableStateOf("")
    var response: Response<String?>? = null
lateinit var Actualitesy : List<Actualites>
    private val _avisByIds = MutableLiveData<Avis?>()
    val avisByIds: LiveData<Avis?> = _avisByIds


    private val _actualitesearched = MutableLiveData<List<Actualites?>>()
    val actualitesearched: MutableLiveData<List<Actualites?>> = _actualitesearched

    private val _isactualiteviewd = MutableLiveData<Boolean>()
    val isactualiteviewd: LiveData<Boolean> = _isactualiteviewd


    private val _addOrUpdateAvisResult = MutableLiveData<String?>()
    val addOrUpdateAvisResult: LiveData<String?> = _addOrUpdateAvisResult

    private val _addAvisResult = MutableLiveData<String?>()
    val addAvisResult: LiveData<String?> = _addAvisResult

    private val _addAvisStatues = MutableLiveData<Response<String>>()
    val addAvisStatues: LiveData<Response<String>> = _addAvisStatues


    val actualites = liveData {
        val ActualiteData = repository.getAll()
        emit(ActualiteData ?: emptyList())
    }



    fun getAvisByIds(idUser: String, idActualites: String) {
        viewModelScope.launch {
            try {
                val avis = repository.getAvisByIds(idUser, idActualites)
                _avisByIds.postValue(avis)
            } catch (e: Exception) {
                errorMessage = "Error getting avis by ids: ${e.message}"
            }
        }
    }
    fun addOrUpdateAvis(avis: Avis) {
        viewModelScope.launch {
            try {
                repository.addOrUpdateAvis(avis)
                _addOrUpdateAvisResult.postValue("ok")
            } catch (e: Exception) {
                _addOrUpdateAvisResult.postValue("error")
            }
        }
    }

    fun searchActualites(about: SearchRequest) {
        viewModelScope.launch {
            try {
                val actualites = repository.searchActualites(about)
                _actualitesearched.postValue(actualites ?: emptyList())
            } catch (e: Exception) {
                errorMessage = "Error searching actualites: ${e.message}"
            }
        }
    }
     fun addview(actualiteId:String){
         viewModelScope.launch {
             try {
                 val result = repository.addview(actualiteId)
                 _isactualiteviewd.value = true
             } catch (e: Exception) {
                 _isactualiteviewd.value = false

             }
         }
     }

    }

