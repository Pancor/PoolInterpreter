package pl.pancordev.poolinterpreter.gameplay

import org.opencv.android.CameraBridgeViewBase
import org.opencv.core.Mat

interface GameplayContract {

    val cameraPermissionCode: Int
        get() = 1

    interface View {

        fun onImageProcessingReady()

        fun setCameraListener(listener: CameraBridgeViewBase.CvCameraViewListener2)

        fun onCameraPermissionsGranted()
    }

    interface Presenter {

        fun onViewReady()

        fun processImage(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat

        fun setResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    }
}