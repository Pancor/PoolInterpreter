package pl.pancordev.poolinterpreter.imageprocessing.tools

import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Point
import pl.pancordev.poolinterpreter.imageprocessing.BypassHack

interface ImageProcessingService: BypassHack {

    fun resizeImage(imageToResize: Mat, percentage: Double): ImageProcessingServiceImpl.Resized

    fun getTableColor(image: Mat): Double

    fun obtainAllPossibleContoursBasedOnColor(image: Mat, tableColor: Double): List<MatOfPoint>

    fun getTheLargestContour(contours: List<MatOfPoint>): Array<Point>

    fun reduceContourPoints(contour: Array<Point>): List<Point>

    fun resizeCoordinates(coordinates: List<Point>, ratio: Double): List<Point>
}