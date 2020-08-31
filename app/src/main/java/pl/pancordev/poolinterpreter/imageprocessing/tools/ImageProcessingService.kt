package pl.pancordev.poolinterpreter.imageprocessing.tools

import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Point
import pl.pancordev.poolinterpreter.imageprocessing.BypassHack
import pl.pancordev.poolinterpreter.imageprocessing.balls.Ball
import pl.pancordev.poolinterpreter.imageprocessing.balls.BallType
import pl.pancordev.poolinterpreter.imageprocessing.balls.Circle
import pl.pancordev.poolinterpreter.imageprocessing.balls.HSV

interface ImageProcessingService: BypassHack {

    fun resizeImage(imageToResize: Mat, percentage: Double): ImageProcessingServiceImpl.Resized

    fun getTableColor(image: Mat): Double

    fun obtainAllPossibleContoursBasedOnColor(image: Mat, tableColor: Double): List<MatOfPoint>

    fun getTheLargestContour(contours: List<MatOfPoint>): Array<Point>

    fun reduceContourPoints(contour: Array<Point>): List<Point>

    fun resizeCoordinates(coordinates: List<Point>, ratio: Double): List<Point>

    fun findCircles(image: Mat): Mat

    fun getCirclesWithProperCoordinates(circles: Mat, maxNumberOfBalls: Int, ratio: Double): MutableList<Circle>

    fun getRoiForCircle(circle: Circle, image: Mat): Mat

    fun getMaskBasedOnBallShape(roi: Mat): Mat

    fun getMaskForRemovingWhite(roi: Mat): Mat

    fun calculateWhiteAreaOfBall(maskRemovingWhite: Mat): Double

    fun calculateMaxValue(image: Mat, mask: Mat, channelNumber: Int): Double

    fun resolveBallColor(hsv: HSV): BallType

    fun resolveWhichBallIsStripped(balls: List<Ball>, ball: Ball): Boolean

    fun draw(balls: List<Ball>, image: Mat): Mat

}