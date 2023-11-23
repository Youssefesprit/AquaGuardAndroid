package tn.aquaguard.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import retrofit2.Response
import tn.aquaguard.models.LoginRequest
import tn.aquaguard.models.LoginResponse
import tn.aquaguard.models.SignupRequest
import tn.aquaguard.models.ChangePassword
import tn.aquaguard.models.SendActivationCode
import tn.aquaguard.network.SessionManager

import tn.aquaguard.network.UserService

class UserViewModel : ViewModel(){

    var errorMessage: String by mutableStateOf("")
    var response: Response<LoginResponse?>? = null
    var responseDelete: Response<SendActivationCode?>? = null
    var responsePass: Response<ChangePassword?>? = null

    suspend fun login(user: LoginRequest, sessionManager: SessionManager){

        val userService = UserService.getInstance()

        try {
            response = userService.login(user)
            sessionManager.saveToken(
                response?.body()!!.token,
                response?.body()!!.username,
                response?.body()!!.userId,
                response?.body()!!.role,
                response?.body()!!.email,
                response?.body()!!.isActivated)

        } catch (e: Exception) {
            errorMessage = e.message.toString()
        }
    }

    suspend fun signup(user: SignupRequest){
        val userService = UserService.getInstance()
        try {
            response = userService.signup(user)

        } catch (e: Exception) {
            errorMessage = e.message.toString()
        }
    }

    suspend fun changePassword(user: ChangePassword){
        val userService = UserService.getInstance()
        try {
            responsePass = userService.changePassword(user)

        } catch (e: Exception) {
            errorMessage = e.message.toString()
        }
    }

    suspend fun deleteAccount(email: String?){

        val userService = UserService.getInstance()
        try {
            Log.e("before","fefe")
            userService.deleteAccount(email)
            Log.e("after","fefe")
        } catch (e: Exception) {
            errorMessage = e.message.toString()
        }
    }
}
