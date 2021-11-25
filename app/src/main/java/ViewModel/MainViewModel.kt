package ViewModel

import Model.Information
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel() {
    //variable holds the array list
    var lst = MutableLiveData<ArrayList<Information>>()
    var newList = arrayListOf<Information>()

    //add data to the array list
    fun add(info: Information){
        newList.add(info)
        lst.postValue(newList)
    }

    //clear the array list
    fun clear(){
        newList.clear()
    }
}