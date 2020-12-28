package com.tony.roomr

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.tony.roomr.data.Subscriber
import com.tony.roomr.data.SubscriberDatabase
import com.tony.roomr.data.SubscriberRepository
import com.tony.roomr.databinding.ActivityMainBinding
import com.tony.roomr.network.APIInterface
import com.tony.roomr.network.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding
    private lateinit var subscriberViewModel: SubscriberViewModel
    private lateinit var adapter: MyRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val dao = SubscriberDatabase.getInstance(application).subscriberDAO
        val repository = SubscriberRepository(dao)
        val factory = SubscriberViewModelFactory(repository)
        subscriberViewModel = ViewModelProvider(this, factory).get(SubscriberViewModel::class.java)
        binding.myViewModel = subscriberViewModel
        binding.lifecycleOwner = this
        initRecyclerView()

        subscriberViewModel.message.observe(this, Observer { it ->
            it.getContentIfNotHandled()?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        })

        // Initiate GET request
        getUserData()
        // Initiate POST request
        postUserData()
    }

    private fun initRecyclerView() {
        binding.rvSubscriber.layoutManager = LinearLayoutManager(this)
        adapter = MyRecyclerViewAdapter { selectedItem: Subscriber -> listItemClicked(selectedItem)}
        binding.rvSubscriber.adapter = adapter
        displaySubscribersList()
    }

    private fun displaySubscribersList() {
        subscriberViewModel.subscribers.observe(this, Observer {
            Log.i(TAG, "displaySubscribersList: $it")
            adapter.setList(it)
            adapter.notifyDataSetChanged()
        })
    }

    private fun listItemClicked(subscriber: Subscriber) {
        subscriberViewModel.initUpdateAndDelete(subscriber)
    }

    companion object {
        const val BASE_URL = "https://reqres.in/api/"
    }

    // Retrofit Network call

    private fun getUserData() {
        val userId = 2
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(APIInterface::class.java)
        val call = service.getUser(userId)

        call.enqueue(object : Callback<User> {
            override fun onResponse(
                call: Call<User>,
                response: Response<User>
            ) {
                if (response.code() == 200) {
                    val body = response.body()!!

                    Log.d(TAG, "onResponse: $body")
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t.message}")
            }
        })
    }

    private fun postUserData() {
        val user = User()
        user.name = "sam mendes"
        user.job = "director"

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(APIInterface::class.java)
        val call = service.createUser(user)

        call.enqueue(object : Callback<User> {
            override fun onResponse(
                call: Call<User>,
                response: Response<User>
            ) {
                if (response.code() == 201) {
                    val body = response.body()!!

                    Log.d(TAG, "In postUserData -> onResponse: $body")
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.d(TAG, "In postUserData -> onFailure: ${t.message}")
            }
        })
    }
}