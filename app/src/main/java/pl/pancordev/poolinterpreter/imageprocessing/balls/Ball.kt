package pl.pancordev.poolinterpreter.imageprocessing.balls

import org.opencv.core.Point

data class Ball(val id: Int, val center: Point, val radius: Int) {
    var color: BallType = BallType.UNKNOWN
    var isStriped = false
    var whiteArea = 0.0

    var ballColor1 = 0.0
    var ballColor2 = 0.0
    var ballColor3 = 0.0
}
