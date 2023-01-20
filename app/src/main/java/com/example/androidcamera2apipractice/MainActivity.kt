package com.example.androidcamera2apipractice

import android.Manifest.permission.*
import android.R
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Camera
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.SurfaceHolder
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.androidcamera2apipractice.databinding.ActivityMainBinding
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


class MainActivity : AppCompatActivity() , SurfaceHolder.Callback, Camera.PictureCallback{

    private val TAG = "MainActivity"

    private lateinit var binding : ActivityMainBinding

    private val neededPermissions = arrayOf(CAMERA)

    private var surfaceHolder: SurfaceHolder? = null
    private var camera: Camera? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val result = checkPermission()
        if (result) {
            setupSurfaceHolder()
        }

    }

    // Other code related to camera permissions.

    private fun setupSurfaceHolder() {
        binding.startBtn.visibility = View.VISIBLE
        binding.surfaceView.visibility = View.VISIBLE

        surfaceHolder = binding.surfaceView.holder
        binding.surfaceView.holder.addCallback(this)
        setBtnClick()
    }

    private fun setBtnClick() {
        binding.startBtn.setOnClickListener { captureImage() }
    }

    private fun captureImage() {
        if (camera != null) {
            camera!!.takePicture(null, null, this)
        }
    }



    private fun checkPermission(): Boolean {
        val currentAPIVersion = Build.VERSION.SDK_INT
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            val permissionsNotGranted = ArrayList<String>()
            for (permission in neededPermissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsNotGranted.add(permission)
                }
            }
            if (permissionsNotGranted.size > 0) {
                var shouldShowAlert = false
                for (permission in permissionsNotGranted) {
                    shouldShowAlert = ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
                }

                val arr = arrayOfNulls<String>(permissionsNotGranted.size)
                val permissions = permissionsNotGranted.toArray(arr)
                if (shouldShowAlert) {
                    showPermissionAlert(permissions)
                } else {
                    requestPermissions(permissions)
                }
                return false
            }
        }
        return true
    }

    private fun showPermissionAlert(permissions: Array<String?>) {
        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setCancelable(true)
        alertBuilder.setTitle("Permission Required")
        alertBuilder.setMessage("You must grant permission to access camera and external storage to run this application.")
        alertBuilder.setPositiveButton(R.string.yes) { _, _ -> requestPermissions(permissions) }
        val alert = alertBuilder.create()
        alert.show()
    }

    private fun requestPermissions(permissions: Array<String?>) {
        ActivityCompat.requestPermissions(this@MainActivity, permissions, REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE -> {
                for (result in grantResults) {
                    if (result == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(this, "Please grant permissions for proper App Functionality", Toast.LENGTH_SHORT).show()
                        // Not all permissions granted. Show some message and return.
                        return
                    }
                }

                Toast.makeText(this, "Permissions Granted", Toast.LENGTH_SHORT).show()
                // All permissions are granted. Do the work accordingly.
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    companion object {
        const val REQUEST_CODE = 100
    }

    override fun surfaceCreated(p0: SurfaceHolder) {
        startCamera()
    }

    private fun startCamera() {
        camera = Camera.open()
        camera!!.setDisplayOrientation(90)
        try {
            camera!!.setPreviewDisplay(surfaceHolder)
            camera!!.startPreview()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }


    override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
        resetCamera()
    }

    private fun resetCamera() {
        if (surfaceHolder!!.surface == null) {
            // Return if preview surface does not exist
            return
        }

        // Stop if preview surface is already running.
        camera!!.stopPreview()
        try {
            // Set preview display
            camera!!.setPreviewDisplay(surfaceHolder)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // Start the camera preview...
        camera!!.startPreview()
    }


    override fun surfaceDestroyed(p0: SurfaceHolder) {
        releaseCamera()
    }

    private fun releaseCamera() {
        camera!!.stopPreview()
        camera!!.release()
        camera = null
    }


    override fun onPictureTaken(bytes: ByteArray, camera: Camera) {
       // saveImage(bytes)
        displayImage(bytes)
        resetCamera()
    }

    private fun displayImage(bytes: ByteArray) {
        try{
            binding. apply {
              //  surfaceView.visibility = View.GONE
                image.visibility = View.VISIBLE
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                image.setImageBitmap(Bitmap.createScaledBitmap(bmp, 1000, 1000, false))
            }
        }catch (e : java.lang.Exception){
            Log.d(TAG, "displayImage: "+ e.message)
        }


    }

    private fun saveImage(bytes: ByteArray) {
        val outStream: FileOutputStream
        try {
            val fileName = "TUTORIALWING_" + System.currentTimeMillis() + ".jpg"
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                fileName
            )
            outStream = FileOutputStream(file)
            outStream.write(bytes)
            outStream.close()
            Toast.makeText(this@MainActivity, "Picture Saved: $fileName", Toast.LENGTH_LONG).show()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }



}