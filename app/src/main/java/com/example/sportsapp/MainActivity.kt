package com.example.sportsapp

import Model.Information
import RecyclerAdapter
import ViewModel.MainViewModel
import ViewModel.MainViewModelFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import org.json.JSONObject
import org.json.JSONTokener
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private var viewManager = LinearLayoutManager(this)
    private lateinit var viewModel: MainViewModel
    private lateinit var mainRecycler: RecyclerView

    private val client = OkHttpClient()

    private lateinit var searchBar: EditText

    private var baseUrl: String = "https://www.thesportsdb.com/api/v1/json/2/searchteams.php?t="

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        searchBar = findViewById(R.id.searchBar)

        mainRecycler = findViewById(R.id.recycler)
        val application = requireNotNull(this).application
        val factory = MainViewModelFactory()

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        initializeAdapter()

        searchBar.setOnKeyListener(View.OnKeyListener{ v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP){
                addData()
                return@OnKeyListener true
            }
            false
        })
    }

    private fun run(url: String) {
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response){
                val thisResponse = response.body()?.string()
                val jsonObject = JSONTokener(thisResponse).nextValue() as JSONObject
                val jsonArray = jsonObject.getJSONArray("teams")

                for (i in 0 until jsonArray.length()){
                    val teamName = jsonArray.getJSONObject(i).getString("strTeam")
                    val year = jsonArray.getJSONObject(i).getString("intFormedYear")
                    val league = jsonArray.getJSONObject(i).getString("strLeague")
                    val country = jsonArray.getJSONObject(i).getString("strCountry")
                    val description = jsonArray.getJSONObject(i).getString("strDescriptionEN")

                    var info = Information(teamName, league, country, year, description)

                    viewModel.add(info)

                    searchBar.text.clear()

                    mainRecycler.adapter?.notifyDataSetChanged()

                    println("TEAM NAME: $teamName")
                    println("YEAR FORMED: $year")
                    println("TEAM LEAGUE: $league")
                    println("TEAM COUNTRY: $country")
                    println("----------------------")
                }
            }
        })
    }

    private fun initializeAdapter() {
        mainRecycler.layoutManager = viewManager
        observeData()
    }

    fun observeData() {
        viewModel.lst.observe(this, Observer {
            println("DATA: $it")
            mainRecycler.adapter = RecyclerAdapter(viewModel, it, this)
        })
    }

    fun addData(){
        var searchQuery = searchBar.text.toString()

        if (searchQuery.isNullOrBlank()){
            Toast.makeText(this,"You must enter a search query!",Toast.LENGTH_LONG).show()
        }
        else{
            //convert search query into format to concatenate to the base url
            val urlTail = createUrlTail(searchQuery)

            run(baseUrl+urlTail)
        }
    }

    //Function adds an underscore between every word of a string
    //EXAMPLE:
    //  "Hey this is a string" converts to "Hey_this_is_a_string"
    fun createUrlTail(str: String): String{
        //split the provided string at every space
        var delimiter = " "
        val parts = str.split(delimiter)

        //variable will hold the edited string. Initialize it to the first element of the array
        var urlTail = parts[0]

        //holds size of array
        val stringLength = parts.size

        //If the string contained more than one word, we need to run this loop to concatenate all words
        //if not, then there is no point in running this loop
        if (stringLength > 1){
            for (i in 1 until stringLength){
                //we are concatenating and adding an underscore after every word
                //EXCEPT for the last word
                if (i != stringLength-1){
                    urlTail = urlTail + parts[i] + "_"
                }
                else{
                    urlTail = urlTail + parts[i]
                }
            }
        }

        return urlTail
    }
}