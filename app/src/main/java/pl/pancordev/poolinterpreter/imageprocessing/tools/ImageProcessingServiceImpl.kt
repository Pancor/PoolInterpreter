package pl.pancordev.poolinterpreter.imageprocessing.tools

import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import pl.pancordev.poolinterpreter.imageprocessing.balls.Ball
import pl.pancordev.poolinterpreter.imageprocessing.balls.BallType
import pl.pancordev.poolinterpreter.imageprocessing.balls.Circle
import pl.pancordev.poolinterpreter.imageprocessing.balls.HSV
import java.lang.IllegalArgumentException
import kotlin.math.min
import kotlin.math.round
import kotlin.math.roundToInt

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

    override fun findCircles(image: Mat): Mat {
        val circles = Mat()
        Imgproc.HoughCircles(image, circles, Imgproc.HOUGH_GRADIENT, 1.0, 20.0, 50.0, 17.0, 8, 13)
        return circles
    }

    override fun getCirclesWithProperCoordinates(circles: Mat, maxNumberOfBalls: Int, ratio: Double): MutableList<Circle> { //TODO: change to immutable
        val resizedCircles = mutableListOf<Circle>()
        val size = min(maxNumberOfBalls, circles.cols())
        for (x in 0 until size) {
            val c = circles.get(0, x)
            val center = Point(round(c[0] * ratio), round(c[1] * ratio))
            val radius = (c[2] * ratio).roundToInt()
            resizedCircles.add(Circle(center, radius))
        }
        return resizedCircles
    }

    override fun getRoiForCircle(circle: Circle, image: Mat): Mat {
        val rectSize = circle.radius * 2
        val roiContours = Rect((circle.center.x - circle.radius).toInt(), (circle.center.y - circle.radius).toInt(), rectSize, rectSize)
        return Mat(image, roiContours)
    }

    override fun getMaskBasedOnBallShape(roi: Mat): Mat {
        val mask = Mat.zeros(roi.size(), CvType.CV_8UC1)
        val ballRadius = roi.width() / 2.0
        Imgproc.circle(mask, Point(ballRadius, ballRadius), ballRadius.toInt(), Scalar(255.0, 255.0, 255.0), -1)
        return mask
    }

    override fun getMaskForRemovingWhite(roi: Mat): Mat {
        val roiGray = Mat()
        Imgproc.cvtColor(roi, roiGray, Imgproc.COLOR_RGB2GRAY)
        val maskRemovingWhite = Mat()
        Imgproc.threshold(roiGray, maskRemovingWhite, 200.0, 255.0, Imgproc.THRESH_BINARY_INV)
        return maskRemovingWhite
    }

//    private fun scaleTwoMasks(firstMask: Mat, secondMask: Mat) : Mat {
//        val scaledMask = Mat()
//        Core.bitwise_and(firstMask, secondMask, scaledMask)
//        return scaledMask
//    }

    override fun calculateWhiteAreaOfBall(maskRemovingWhite: Mat): Double {
        val whiteAreaContours: List<MatOfPoint> = mutableListOf()
        Core.bitwise_not(maskRemovingWhite, maskRemovingWhite)
        Imgproc.findContours(maskRemovingWhite, whiteAreaContours, Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE)
        var whiteArea = 0.0
        for (cnt in whiteAreaContours) {
            whiteArea += Imgproc.contourArea(cnt)
        }
        return whiteArea
    }

    override fun calculateMaxValue(image: Mat, mask: Mat, channelNumber: Int): Double {
        val histSize: MatOfInt
        val range: MatOfFloat
        when (channelNumber) {
            0 -> {
                histSize = MatOfInt(180)
                range = MatOfFloat(0f, 180f)
            }
            1, 2 -> {
                histSize = MatOfInt(255)
                range = MatOfFloat(0f, 255f)
            }
            else -> throw IllegalArgumentException("Provided channel number: $channelNumber is not supported")
        }

        val histogram = Mat()
        Imgproc.calcHist(listOf(image), MatOfInt(channelNumber), mask, histogram, histSize, range)
        val minMaxLocResult = Core.minMaxLoc(histogram)
        return minMaxLocResult.maxLoc.y
    }

    override fun resolveBallColor(hsv: HSV): BallType {
        var ballType = BallType.UNKNOWN
        if (hsv.hChannel > 20 && hsv.hChannel < 35) {
            ballType = BallType.YELLOW
        }
        if (hsv.hChannel > -1 && hsv.hChannel < 9 || hsv.hChannel > 177) {
            ballType = BallType.RED
        }
        if (hsv.hChannel > 6 && hsv.hChannel < 20) {
            ballType = BallType.ORANGE
        }
        if (hsv.hChannel > 165 && hsv.hChannel < 178) {
            ballType = BallType.BROWN
        }
        if (hsv.hChannel > 70 && hsv.hChannel < 95) {
            ballType = BallType.GREEN
        }
        if (hsv.hChannel > 100 && hsv.hChannel < 115) {
            ballType = BallType.BLUE
        }
        if (hsv.hChannel > 115 && hsv.hChannel < 140) {
            ballType = BallType.PURPLE
        }
        if (hsv.hChannel > 50 && hsv.hChannel < 95 && hsv.vChannel < 40) {
            ballType = BallType.BLACK
        }
        if (hsv.sChannel < 50) {
            ballType = BallType.WHITE
        }
        return ballType
    }

    override fun resolveWhichBallIsStripped(balls: List<Ball>, ball: Ball): Boolean {
        for (it in balls) {
            if (it.color == ball.color && it.id != ball.id && ball.whiteArea > it.whiteArea) {
                return true
            }
        }
        return false
    }

    override fun draw(balls: List<Ball>, image: Mat): Mat {
        for (ball in balls) {
            Imgproc.circle(image, ball.circle.center, ball.circle.radius, Scalar(255.0, 0.0, 255.0),
                3, 8, 0)

            val textPoint = Point(ball.circle.center.x + 20, ball.circle.center.y + 10)
            if (ball.color != BallType.UNKNOWN) {
                Imgproc.putText(image, "#${ball.id} (${ball.hsv.hChannel}, ${ball.hsv.sChannel}, ${ball.hsv.vChannel}) " +
                        "${ball.color.name} ${ball.whiteArea}, ${ball.isStriped}", textPoint, Imgproc.FONT_HERSHEY_SIMPLEX,
                    1.0, Scalar(255.0, 0.0, 255.0), 2)
            } else {
                Imgproc.putText(image, "(${ball.hsv.hChannel}, ${ball.hsv.sChannel}, ${ball.hsv.vChannel})",
                    textPoint, Imgproc.FONT_HERSHEY_SIMPLEX, 1.0, Scalar(255.0, 0.0, 255.0), 2)
            }
        }
        return image
    }

    override fun hackView() = hacked

    data class Resized(val resizedImage: Mat, val originalToResizedRatio: Double)
}