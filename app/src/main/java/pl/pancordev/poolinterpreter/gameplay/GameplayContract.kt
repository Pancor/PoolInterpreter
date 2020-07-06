package pl.pancordev.poolinterpreter.gameplay

import org.opencv.android.CameraBridgeViewBase
import org.opencv.core.Mat


interface GameplayContract {

    interface View {

    }

    interface Presenter {

        fun onViewReady()

        fun processImage(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat
    }
}