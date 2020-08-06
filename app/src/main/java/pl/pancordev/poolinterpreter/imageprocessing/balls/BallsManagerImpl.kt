package pl.pancordev.poolinterpreter.imageprocessing.balls

import org.opencv.core.Mat

class BallsManagerImpl : BallsContract.BallsManager {

    override fun getBalls(mat: Mat) {

    }

    override fun hackView(): Mat {
        return Mat()
    }
}