package de.dertyp7214.waavtomp3

import android.app.Application
import android.widget.Toast
import cafe.adriel.androidaudioconverter.AndroidAudioConverter
import cafe.adriel.androidaudioconverter.callback.ILoadCallback
import com.downloader.PRDownloader

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        PRDownloader.initialize(applicationContext)
        AndroidAudioConverter.load(this, object : ILoadCallback {
            override fun onSuccess() {
            }

            override fun onFailure(error: Exception) {
                Toast.makeText(
                    applicationContext,
                    "FFMPEG is not supported on your phone!",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
}