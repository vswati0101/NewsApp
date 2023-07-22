package com.example.newsapp.ViewModels

import androidx.lifecycle.ViewModel
import com.example.newsapp.Repos.MainRepository

class MainViewModel(val mainRepository: MainRepository):ViewModel() {
    var currentPage=1
    val articleliveData get()=mainRepository.liveData
    fun loadArticles(){
        currentPage++
        mainRepository.getArticles(currentPage.toString())
    }

}