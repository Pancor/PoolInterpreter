package pl.pancordev.poolinterpreter.gameplay

import android.content.pm.PackageManager
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat
import timber.log.Timber

class GameplayPresenter constructor(private val gameplayView: GameplayContract.View) :
    GameplayContract.Presenter, CameraBridgeViewBase.CvCameraViewListener2 {

    companion object {
        const val CAMERA_PERMISSION_CODE = 1
    }

    override fun onViewReady() {
        Timber.d("On view ready")

        if (OpenCVLoader.initDebug()) {
            gameplayView.onImageProcessingReady()
        } else {
            Timber.e("Could not init OpenCV")
        }
    }

    override fun processImage(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        return inputFrame.gray()
    }

    override fun setResult(requestCode: Int, permissions: Array<out String>,
                           grantResults: IntArray) {
        when (requestCode) {
            CAMERA_PERMISSION_CODE ->
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Timber.d("Camera permissions granted")
                    gameplayView.onCameraPermissionsGranted()
                } else {
                    Timber.e("Permissions not granted for camera")
                }
        }
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        Timber.d("CameraView started")
    }

    override fun onCameraViewStopped() {
        Timber.d("CameraView stopped")
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        return inputFrame.rgba()
    }
}