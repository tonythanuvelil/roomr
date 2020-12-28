package com.tony.roomr

import android.util.Patterns
import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tony.roomr.data.Subscriber
import com.tony.roomr.data.SubscriberRepository
import kotlinx.coroutines.launch

class SubscriberViewModel(private val repository: SubscriberRepository) : ViewModel(), Observable {
    val subscribers = repository.subscribers
    private var isUpdateOrDelete = false
    private lateinit var subscriberToUpdateOrDelete: Subscriber

    @Bindable
    val inputName = MutableLiveData<String>()

    @Bindable
    val inputEmail = MutableLiveData<String>()

    @Bindable
    val saveButtonText = MutableLiveData<String>()

    @Bindable
    val clearButtonText = MutableLiveData<String>()

    private val statusMessage = MutableLiveData<Event<String>>()
    val message: LiveData<Event<String>>
        get() = statusMessage

    init {
        saveButtonText.value = "Save"
        clearButtonText.value = "Clear All"
    }

    fun saveOrUpdate() {
        if(inputName.value == null) {
            statusMessage.value = Event("Please enter name")
        } else if(inputEmail.value == null) {
            statusMessage.value = Event("Please enter email")
        } else if(!Patterns.EMAIL_ADDRESS.matcher(inputEmail.value).matches()){
            statusMessage.value = Event("Please enter a valid email")
        } else {
            if (isUpdateOrDelete) {
                subscriberToUpdateOrDelete.name = inputName.value!!
                subscriberToUpdateOrDelete.email = inputEmail.value!!
                update(subscriberToUpdateOrDelete)
            } else {
                val name = inputName.value!!
                val email = inputEmail.value!!
                insert(Subscriber(0, name, email))
                inputName.value = null
                inputEmail.value = null
            }
        }
    }

    fun clearAllOrDelete() {
        if (isUpdateOrDelete) {
            delete(subscriberToUpdateOrDelete)
        } else {
            clearAll()
        }
    }

    fun insert(subscriber: Subscriber) = viewModelScope.launch {
        val newRowId = repository.insert(subscriber)
        if(newRowId > -1) {
            statusMessage.value = Event("Subscriber inserted successfully $newRowId")
        } else {
            statusMessage.value = Event("Error occurred")
        }
    }

    fun update(subscriber: Subscriber) = viewModelScope.launch {
        val noOfRows = repository.update(subscriber)
        if(noOfRows > 0) {
            inputName.value = null
            inputEmail.value = null
            isUpdateOrDelete = false
            saveButtonText.value = "Save"
            clearButtonText.value = "Clear All"
            statusMessage.value = Event("$noOfRows rows updated successfully")
        } else {
            statusMessage.value = Event("Error occurred")
        }
    }

    fun delete(subscriber: Subscriber) = viewModelScope.launch {
        val noOfRowsDeleted = repository.delete(subscriber)
        if (noOfRowsDeleted > 0) {
            inputName.value = null
            inputEmail.value = null
            isUpdateOrDelete = false
            saveButtonText.value = "Save"
            clearButtonText.value = "Clear All"
            statusMessage.value = Event("$noOfRowsDeleted rows deleted successfully")
        } else {
            statusMessage.value = Event("Error occurred")
        }
    }

    fun clearAll() = viewModelScope.launch {
        val noOfRowsDeleted = repository.deleteAll()
        if(noOfRowsDeleted > 0) {
            statusMessage.value = Event("$noOfRowsDeleted rows deleted successfully")
        } else {
            statusMessage.value = Event("Error occurred")
        }
    }

    fun initUpdateAndDelete(subscriber: Subscriber) {
        inputName.value = subscriber.name
        inputEmail.value = subscriber.email
        isUpdateOrDelete = true
        subscriberToUpdateOrDelete = subscriber
        saveButtonText.value = "Update"
        clearButtonText.value = "Delete"
    }

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {

    }

    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {

    }
}