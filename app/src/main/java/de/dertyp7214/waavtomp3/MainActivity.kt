package de.dertyp7214.waavtomp3

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            1
        )

        btnConvert.setOnClickListener {
            val text = inputUrl.text.toString()
            val filename = "tmp_${System.currentTimeMillis()}.wav"
            val path = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)

            Log.d("UFF", "$text $filename ${path?.absolutePath}")

            PRDownloader.download(text, path?.absolutePath, filename)
                .build()
                .setOnStartOrResumeListener {
                    progressBar.visibility = VISIBLE
                }
                .setOnPauseListener { }
                .setOnCancelListener {
                    progressBar.visibility = INVISIBLE
                }
                .setOnProgressListener {
                    progressBar.progress =
                        ((it.currentBytes.toFloat() / it.totalBytes.toFloat()) * 100F).roundToInt()
                }
                .start(object : OnDownloadListener {
                    override fun onDownloadComplete() {
                        progressBar.visibility = INVISIBLE
                        Toast.makeText(this@MainActivity, "Downloaded", Toast.LENGTH_LONG).show()
                        val cmd = arrayOf(
                            "-y",
                            "-i",
                            File(path, filename).path,
                            File(path, filename.replace(".wav", ".mp3")).path
                        )
                        FFmpeg.getInstance(this@MainActivity)
                            .execute(cmd, object : FFmpegExecuteResponseHandler {
                                override fun onStart() {}
                                override fun onProgress(message: String) {
                                    try {
                                        size.text =
                                            message.split("size=")[1].split("time")[0].trim()
                                                .replace("\t", "")
                                        time.text =
                                            message.split("time=")[1].split("bitrate")[0].trim()
                                                .replace("\t", "")
                                        bitrate.text =
                                            message.split("bitrate=")[1].split("speed")[0].trim()
                                                .replace("\t", "")
                                        speed.text =
                                            message.split("speed=")[1].trim().replace("\t", "")
                                        if (stats.visibility == INVISIBLE) stats.visibility =
                                            VISIBLE
                                    } catch (e: Exception) {
                                    }
                                }

                                override fun onSuccess(message: String) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Converted",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    File(path, filename).apply {
                                        if (exists()) delete()
                                    }
                                    stats.visibility = INVISIBLE
                                }

                                override fun onFailure(message: String) {
                                    Log.d("ERROR", message)
                                    stats.visibility = INVISIBLE
                                }

                                override fun onFinish() {}
                            })
                    }

                    override fun onError(error: com.downloader.Error?) {
                        progressBar.visibility = INVISIBLE
                        Log.d("ERROR", error?.serverErrorMessage ?: error.toString())
                    }
                })
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    btnConvert.isEnabled = true
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Permission denied to read your External storage",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return
            }
        }
    }
}

data class Progress(val size: String, val time: String, val bitrate: String, val speed: String) {
    override fun toString(): String {
        return "size=$size\ttime=$time\tbitrate=$bitrate\tspeed=$speed"
    }
}
