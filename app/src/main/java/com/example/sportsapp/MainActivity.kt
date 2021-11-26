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

        //assign view resources to variables
        searchBar = findViewById(R.id.searchBar)
        resultsTextView = findViewById(R.id.resultsTextView)
        mainRecycler = findViewById(R.id.recycler)

        //results text view is invisible by default
        resultsTextView.visibility = View.INVISIBLE

        //assign main view model variable
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        initializeAdapter()

        //When user hits enter on their keyboard, run the addData() function
        //and display the "loading..." string
        //Also, takes the user's search bar input and assigns it to userQuery variable
        //to use it in the run() function
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

    //run() function clears the view model to prevent showing previous query results
    //Requests data based on the passed url and converts the response body to a JSON Object
    //Then, checks if the JSON Object is null. If it is null that means there is no info to show,
    //so the resultsTextView's text is changed from "loading..." to "No results found for " and the
    //userQuery variable appended to it
    //If it is not null, we convert the JSON Object to a JSON Array and the resultsTextView's text
    //is changed from "loading..." to "Showing results for " and the userQuery variable appended to it
    //Then, we go through the entire JSON Array and extract the required data (team id, name, year formed,
    //league, country, description, and badge
    //We check to see if year = "0" and if so, change the year variable to "N/A" b/c there is no year to show
    //We convert imageLink to imageBitmap and append team Id to teamBaseUrl to create a link that
    //navigates the user to the team's page to get more info
    //Then we add the info to the view model
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
                        var year = jsonArray.getJSONObject(i).getString("intFormedYear")
                        val league = jsonArray.getJSONObject(i).getString("strLeague")
                        val country = jsonArray.getJSONObject(i).getString("strCountry")
                        var description = jsonArray.getJSONObject(i).getString("strDescriptionEN")
                        val imageLink = jsonArray.getJSONObject(i).getString("strTeamBadge")

                        if (year == "0"){
                            year = "N/A"
                        }
                        else if (description == "null"){
                            description = "No description found"
                        }

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

    //function assigns a view manager to the recycler view in our activity
    private fun initializeAdapter() {
        mainRecycler.layoutManager = viewManager
        observeData()
    }

    //function looks for any changes in the view model and then
    // sends those changes to our recycler adapter
    private fun observeData() {
        viewModel.lst.observe(this, Observer {
            mainRecycler.adapter = RecyclerAdapter(viewModel, it, this)
        })
    }

    //takes the user's input in the search bar and converts it to the proper format (see: createUrlTail function)
    //to append it to the base url
    //The new string is then passed to the run() function and the search bar is cleared
    private fun addData(){
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