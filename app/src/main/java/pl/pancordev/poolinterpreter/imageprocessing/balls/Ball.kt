package pl.pancordev.poolinterpreter.imageprocessing.balls

import org.opencv.core.Point

data class Ball(val id: Int, var circle: Circle) {
    var color: BallType = BallType.UNKNOWN
    var isStriped = false
    var whiteArea = 0.0
    var hsv: HSV = HSV(0.0, 0.0, 0.0)
}
