package ViewModel

import Model.Information
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel() {
    var lst = MutableLiveData<ArrayList<Information>>()
    var newList = arrayListOf<Information>()

    fun add(info: Information){
        newList.add(info)
        lst.postValue(newList)
    }
}