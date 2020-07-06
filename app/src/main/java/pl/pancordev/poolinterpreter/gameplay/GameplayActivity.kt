package pl.pancordev.poolinterpreter.gameplay

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.act_gameplay.*
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat
import pl.pancordev.poolinterpreter.R
import timber.log.Timber

class GameplayActivity : AppCompatActivity(), GameplayContract.View,
    CameraBridgeViewBase.CvCameraViewListener2 {

    private lateinit var gameplayPresenter: GameplayContract.Presenter
    private lateinit var managerCallback: BaseLoaderCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_gameplay)

        gameplayPresenter = GameplayPresenter(this)
        gameplayPresenter.onViewReady()

        val self = this
        managerCallback = object : BaseLoaderCallback(this) {
            override fun onManagerConnected(status: Int) {
                Timber.d("OpenCV manager connection status: $status")
                when (status) {
                    LoaderCallbackInterface.SUCCESS -> ActivityCompat.requestPermissions(self,
                        arrayOf(Manifest.permission.CAMERA), 1)
                    else ->  super.onManagerConnected(status)
                }
            }
        }
        cameraView.setCvCameraViewListener(this)
        if (OpenCVLoader.initDebug()) {
            managerCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        } else {
            Timber.e("Could not init OpenCV")
        }
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        Timber.d("CameraView started")
    }

    override fun onCameraViewStopped() {
        Timber.d("CameraView stopped")
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        return gameplayPresenter.processImage(inputFrame)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            1 ->
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Timber.d("Camera permissions granted")
                    cameraView.setCameraPermissionGranted()
                    cameraView.enableView()
                } else {
                    Timber.e("Permissions not granted for camera")
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