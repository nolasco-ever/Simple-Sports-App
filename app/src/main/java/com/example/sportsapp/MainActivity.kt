package com.example.sportsapp

import Model.Information
import RecyclerAdapter
import ViewModel.MainViewModel
import ViewModel.MainViewModelFactory
import android.annotation.SuppressLint
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
import android.util.Log

import android.graphics.BitmapFactory

import android.graphics.Bitmap
import android.widget.TextView

import java.io.InputStream

import java.net.HttpURLConnection

import java.net.URL




class MainActivity : AppCompatActivity() {
    private var viewManager = LinearLayoutManager(this)
    private lateinit var viewModel: MainViewModel
    private lateinit var mainRecycler: RecyclerView

    private val client = OkHttpClient()

    private lateinit var searchBar: EditText
    private lateinit var resultsTextView: TextView

    private lateinit var userQuery: String

    private var baseUrl: String = "https://www.thesportsdb.com/api/v1/json/2/searchteams.php?t="
    private var teamBaseUrl: String = "https://www.thesportsdb.com/team/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        searchBar = findViewById(R.id.searchBar)
        resultsTextView = findViewById(R.id.resultsTextView)

        resultsTextView.visibility = View.INVISIBLE

        mainRecycler = findViewById(R.id.recycler)
        val application = requireNotNull(this).application
        val factory = MainViewModelFactory()

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        initializeAdapter()

        searchBar.setOnKeyListener(View.OnKeyListener{ v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP){
                resultsTextView.visibility = View.VISIBLE
                userQuery = searchBar.text.toString()
                resultsTextView.text = getString(R.string.loading_results)
                addData()
                mainRecycler.adapter?.notifyDataSetChanged()
                return@OnKeyListener false
            }
            false
        })
    }

    private fun run(url: String) {
        viewModel.clear()

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call, response: Response){
                val thisResponse = response.body()?.string()
                val jsonObject = JSONTokener(thisResponse).nextValue() as JSONObject

                //check if the response is null
                if (jsonObject.get("teams").toString() == "null"){
                    resultsTextView.text = "${getString(R.string.no_results_found)} '$userQuery'"
                }
                else{
                    resultsTextView.text = "${getString(R.string.showing_results)} '$userQuery'"
                    
                    val jsonArray = jsonObject.getJSONArray("teams")

                    for (i in 0 until jsonArray.length()){
                        val teamId = jsonArray.getJSONObject(i).getString("idTeam")
                        val teamName = jsonArray.getJSONObject(i).getString("strTeam")
                        val year = jsonArray.getJSONObject(i).getString("intFormedYear")
                        val league = jsonArray.getJSONObject(i).getString("strLeague")
                        val country = jsonArray.getJSONObject(i).getString("strCountry")
                        val description = jsonArray.getJSONObject(i).getString("strDescriptionEN")
                        val imageLink = jsonArray.getJSONObject(i).getString("strTeamBadge")

                        //convert url to bitmap
                        val imageBitmap = getBitmapFromURL(imageLink) as Bitmap
                        val teamPageLink = teamBaseUrl + teamId

                        val info = Information(teamName, league, country, year, description, imageBitmap, teamPageLink)

                        viewModel.add(info)
                    }
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
//            println("DATA: $it")
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
            searchBar.text.clear()
        }
    }

    //Function adds an underscore between every word of a string
    //EXAMPLE:
    //  "Hey this is a string" converts to "Hey_this_is_a_string"
    fun createUrlTail(str: String): String{
        println("PROVIDED STRING: $str")
        //split the provided string at every space
        var delimiter = " "
        val parts = str.split(delimiter)

        println("PARTS: $parts")

        //variable will hold the edited string. Initialize it to the first element of the array
        var urlTail = parts[0] + "_"

        println("PARTS[0]: ${parts[0]}")

        //holds size of array
        val stringLength = parts.size

        println("STRING LENGTH: $stringLength")

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

        println("URL TAIL COMPLETE: $urlTail")

        return urlTail
    }

    fun getBitmapFromURL(src: String?): Bitmap? {
        return try {
            val url = URL(src)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            val myBitmap = BitmapFactory.decodeStream(input)
            myBitmap
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}