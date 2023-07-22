package com.example.newsapp.Activities


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.Adapter.ArticleAdapter
import com.example.newsapp.Adapter.LAYOUT_CARD
import com.example.newsapp.Adapter.LAYOUT_LIST
import com.example.newsapp.Model.ArticleModel
import com.example.newsapp.Repos.APIResponses
import com.example.newsapp.Repos.MainRepository
import com.example.newsapp.Utilities.PrefUtils
import com.example.newsapp.Utilities.showMessage
import com.example.newsapp.ViewModels.MainViewModel
import com.example.newsapp.ViewModels.MainViewModelFactory
import com.example.newsapp.databinding.ActivityMainBinding
const val TAG="MAIN_ACTIVITY_"
class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    val activity=this
    val list:ArrayList<ArticleModel> =ArrayList()
    val adapter= ArticleAdapter(list,activity)
    private lateinit var viewModel: MainViewModel
    private val refreshLiveData=MutableLiveData<Boolean>()
    var layoutCurrent= LAYOUT_LIST
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
       PrefUtils.init(activity)
        binding.apply {
            setupRecyclerView()
            loadArticlesFromBackend()
            updateRefreshLayout()
            changeRecyclerViewLayout()
        }
    }
    private fun changeRecyclerViewLayout(){
        layoutCurrent=PrefUtils.getPrefInt("layout_type", LAYOUT_LIST)
       binding.mActionBar.mLayoutChanger.setOnClickListener {
           if(layoutCurrent== LAYOUT_CARD){
               layoutCurrent= LAYOUT_LIST
           }
           else{
               layoutCurrent= LAYOUT_CARD
           }
           updateUiBasedOnLayoutType()
       }
    }
    fun updateUiBasedOnLayoutType(){
        PrefUtils.putPrefInt("layout_type",layoutCurrent)
        if(layoutCurrent== LAYOUT_CARD){
            binding.mActionBar.mLayoutChanger.setImageResource(R.drawable.layout_card)
        }
        else{
            binding.mActionBar.mLayoutChanger.setImageResource(R.drawable.layout_list)
        }
        val tempList:ArrayList<ArticleModel> =ArrayList()
        if(list.isNotEmpty()){
            list.forEach {
                tempList.add(it)
            }
            list.clear()
            tempList.forEach {
                it.LAYOUT_TYPE=layoutCurrent
                list.add(it)
            }
            adapter.notifyDataSetChanged()
        }
    }
private fun updateRefreshLayout(){
    updateShimmerLayout(true)
    refreshLiveData.observe(this){
        binding.refreshLayout.isRefreshing=it
       updateShimmerLayout(it)
    }
    binding.refreshLayout.setOnRefreshListener {
        viewModel.currentPage=0
        viewModel.loadArticles()
    }
}

    private fun updateShimmerLayout(isloaded:Boolean) {
        binding.apply {
            if(!isloaded){
                binding.mCardShimmerHolder.visibility=View.GONE
                binding.mListShimmerHolder.visibility=View.GONE
                return
        }
            if(layoutCurrent== LAYOUT_CARD){
                mCardShimmerHolder.visibility=View.VISIBLE
                mListShimmerHolder.visibility=View.GONE
            }
            else{
                mCardShimmerHolder.visibility=View.GONE
                mListShimmerHolder.visibility=View.VISIBLE
            }

        }
    }

    private fun loadArticlesFromBackend(){
        val mainRepository=MainRepository(context=activity)
        viewModel=ViewModelProvider(activity,MainViewModelFactory(mainRepository=mainRepository)
        )[MainViewModel::class.java]
        viewModel.loadArticles()
        viewModel.articleliveData.observe(this){
             //logInfo(TAG,it.toString())
            when(it){
                is APIResponses.Error ->{
                    showMessage(activity,"Error ${it.errorMessage}")
                    refreshLiveData.value=false
                }
                is APIResponses.Loading -> {
                    showMessage(activity,"Loading")
                    refreshLiveData.value=true
                }
                is APIResponses.Success -> {
                    refreshLiveData.value=false
                    if(it.data!!.isNotEmpty()){
                        it.data?.forEach { model->
                            list.add(model)
                        }
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }

    }
    private fun setupRecyclerView() {
        binding.apply {
            mRecyclerHome.adapter = adapter
            mRecyclerHome.addOnScrollListener(object:RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if(!recyclerView.canScrollVertically(1))
                        viewModel.loadArticles()
                }
            })
            }
        }
    }

