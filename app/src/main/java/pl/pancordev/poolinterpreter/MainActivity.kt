package pl.pancordev.poolinterpreter

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.act_main.*
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat

class MainActivity : AppCompatActivity(), CameraBridgeViewBase.CvCameraViewListener2 {

    private lateinit var managerCallback: BaseLoaderCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_main)

        val self = this
        managerCallback = object : BaseLoaderCallback(this) {
            override fun onManagerConnected(status: Int) {
                Log.d("TAG", "OpenCV manager connection status: $status")
                when (status) {
                    LoaderCallbackInterface.SUCCESS ->  ActivityCompat.requestPermissions(self, arrayOf(Manifest.permission.CAMERA), 1)
                    else ->  super.onManagerConnected(status)
                }
            }
        }
        cameraView.setCvCameraViewListener(this)

        if (OpenCVLoader.initDebug()) {
            managerCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        } else {
            Log.e("TAG", "Failed to static initialize OpenCV")
            OpenCVLoader.initAsync("4.3.0", this, managerCallback)
        }
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        Log.d("TAG", "CameraView started")
    }

    override fun onCameraViewStopped() {
        Log.d("TAG", "CameraView stopped")
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        return inputFrame.rgba()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            1 ->
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("TAG", "Camera permissions granted")
                    cameraView.setCameraPermissionGranted()
                    cameraView.enableView()
                } else {
                    Log.e("TAG", "Permissions not granted for camera")
                }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onPause() {
        super.onPause()
        cameraView.disableView()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraView.disableView()
    }
}