package pl.pancordev.poolinterpreter.gameplay

import org.opencv.android.CameraBridgeViewBase
import org.opencv.core.Mat
import timber.log.Timber


class GameplayPresenter constructor(val gameplayView: GameplayContract.View) :
    GameplayContract.Presenter {

    override fun onViewReady() {
        Timber.d("On view ready")
    }

    override fun processImage(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        return inputFrame.gray()
    }

}