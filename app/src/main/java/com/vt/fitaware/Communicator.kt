package com.vt.fitaware

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel

class Communicator : ViewModel(){

    val message =MutableLiveData<Any>()

    fun setMsgCommunicator(msg:String){
        message.value = msg
    }
}