package pl.pancordev.poolinterpreter.imageprocessing.tools

import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import kotlin.math.min

class ImageProcessingServiceImpl: ImageProcessingService {

    private var hacked = Mat()

    override fun resizeImage(imageToResize: Mat, percentage: Double): Resized {
        val resizedImage = Mat()

        val imageSize = imageToResize.size()
        val newSize = Size(imageSize.width * percentage, imageSize.height * percentage)
        Imgproc.resize(imageToResize, resizedImage, newSize)

        val originalToResizedRatio = imageSize.width / resizedImage.size().width
        return Resized(resizedImage, originalToResizedRatio)
    }

    override fun getTableColor(image: Mat): Double {
        val circularMask = Mat.zeros(image.size(), CvType.CV_8UC1)
        Imgproc.circle(circularMask, Point((image.width() / 2).toDouble(), (image.height() / 2).toDouble()),
            min(image.height(), image.width()) / 4, Scalar(255.0, 255.0, 255.0), -1)

        val hist = Mat()
        Imgproc.calcHist(listOf(image), MatOfInt(0), circularMask, hist, MatOfInt(180), MatOfFloat(0f, 180f))

        val minMaxLocResult = Core.minMaxLoc(hist)
        return minMaxLocResult.maxLoc.y
    }

    override fun obtainAllPossibleContoursBasedOnColor(image: Mat, tableColor: Double): List<MatOfPoint> {
        val threshedImage = Mat()
        Core.inRange(image, Scalar(tableColor - 5, 0.0, 0.0), Scalar(tableColor + 5, 255.0, 255.0), threshedImage)

        val allContours: List<MatOfPoint> = mutableListOf()
        Imgproc.findContours(threshedImage, allContours, Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

        return allContours
    }

    override fun getTheLargestContour(contours: List<MatOfPoint>): Array<Point> {
        var maxArea = 0.0
        var tableContour = MatOfPoint()
        for (contour in contours) {
            val contourArea = Imgproc.contourArea(contour)
            if (maxArea < contourArea) {
                maxArea = contourArea
                tableContour = contour
            }
        }
        return tableContour.toArray()
    }

    override fun reduceContourPoints(contour: Array<Point>): List<Point> {
        val reducedNumberOfPoints = MatOfPoint2f()

        val epsilon = 0.05 * Imgproc.arcLength(MatOfPoint2f(*contour), true)
        Imgproc.approxPolyDP(MatOfPoint2f(*contour), reducedNumberOfPoints, epsilon, true)

        return reducedNumberOfPoints.toArray().asList()
    }

    override fun resizeCoordinates(coordinates: List<Point>, ratio: Double): List<Point> {
        val resizedPoints = mutableListOf<Point>()
        for (point in coordinates) {
            point.x *= ratio
            point.y *= ratio
            resizedPoints.add(point)
        }
        return resizedPoints
    }

    override fun hackView() = hacked

    data class Resized(val resizedImage: Mat, val originalToResizedRatio: Double)
}