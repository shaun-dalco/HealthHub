package com.hotmail.shaundalco.healthhub

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class HealthConnectRationaleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Open your privacy policy (or a screen explaining why you need steps)
        // Replace with your own URL.
        val url = "https://yourdomain.com/privacy"
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))

        // Close immediately so user goes back to Health Connect permission screen.
        finish()
    }
}
