package com.example.sportsapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import android.widget.ImageView

class SplashScreen : AppCompatActivity() {
    private lateinit var logoIcon: ImageView
    private lateinit var logoName: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        //assign variables to view resources
        logoIcon = findViewById(R.id.logo_icon)
        logoName = findViewById(R.id.logo_name)

        //create variables to hold animations
        val iconAnimation = AnimationUtils.loadAnimation(this, R.anim.logo_icon_animation)
        val nameAnimation = AnimationUtils.loadAnimation(this, R.anim.logo_name_animation)

        //play the animations
        logoIcon.startAnimation(iconAnimation)
        logoName.startAnimation(nameAnimation)

        //set the duration of the splash screen itself and navigate user to main activity
        val splashScreenTime = 1500
        val intent = Intent(this@SplashScreen, MainActivity::class.java)
        Handler().postDelayed({
            startActivity(intent)
            finish()
        }, splashScreenTime.toLong())
    }
}