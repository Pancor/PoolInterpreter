package pl.pancordev.poolinterpreter.imageprocessing.balls

import org.opencv.core.Mat
import pl.pancordev.poolinterpreter.imageprocessing.BypassHack

interface BallsContract {

    interface BallsManager: BypassHack {

        fun getBalls(mat: Mat): List<Ball>
    }
}