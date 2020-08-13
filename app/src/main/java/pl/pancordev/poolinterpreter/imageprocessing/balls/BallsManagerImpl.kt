package pl.pancordev.poolinterpreter.imageprocessing.balls

import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import timber.log.Timber

class BallsManagerImpl : BallsContract.BallsManager {

    private var ratio = 0.0
    private var hacked = Mat()

    override fun getBalls(mat: Mat): List<Ball> {
        val resizedMat = Mat()
        val newSize = Size(mat.size().width * 0.5, mat.size().height * 0.5)
        Imgproc.resize(mat, resizedMat, newSize)
        ratio = mat.size().width / resizedMat.size().width //TODO: store resizing ratio for future calculations of drawing proper scale of contours

        val greyscale = Mat()
        Imgproc.cvtColor(resizedMat, greyscale, Imgproc.COLOR_BGR2GRAY)

        val blurred = Mat()
        Imgproc.blur(greyscale, blurred, Size(3.0,3.0))

        val circles = Mat()
        Imgproc.HoughCircles(blurred, circles, Imgproc.HOUGH_GRADIENT, 1.0,
            20.0, 50.0, 17.0,
            5, 13)
        Timber.d("Found ${circles.cols()} balls")

        val balls = mutableListOf<Ball>()
        //val size = if (circles.cols() > 16) { 16 } else { circles.cols() }
        val size = if (circles.cols() > 9) { 9 } else { circles.cols() }
        for (x in 0 until size) { //16 because there is only 16 balls
            val c = circles.get(0, x)
            val center = Point(Math.round(c[0]*ratio).toDouble(), Math.round(c[1]*ratio).toDouble())
            val radius = Math.round(c[2]*ratio).toInt()
            balls.add(Ball(center, radius))
        }

        for (ball in balls) {
            val ballSize = ball.radius * 2
            val contours = Rect((ball.center.x - ball.radius).toInt(), (ball.center.y - ball.radius).toInt(), ballSize, ballSize)
            Timber.d("Ball: ${ball.radius} (${ball.center.x}, ${ball.center.y}), mat: (${mat.size().width}, ${mat.size().height})")
            val roi = Mat(mat, contours)

            val roiHsvMat = Mat()
            Imgproc.cvtColor(roi, roiHsvMat, Imgproc.COLOR_RGB2HSV)

            val mask = Mat.zeros(roi.size(), CvType.CV_8UC1)
            Imgproc.circle(mask, Point(ball.radius.toDouble(), ball.radius.toDouble()), ball.radius, Scalar(255.0, 255.0, 255.0), -1)
            roi.copyTo(roi, mask)

            val hist1 = Mat()
            Imgproc.calcHist(listOf(roiHsvMat), MatOfInt(0), mask, hist1, MatOfInt(180), MatOfFloat(0f, 180f))
            val minMaxLocResult = Core.minMaxLoc(hist1)
            val ballColor1 = minMaxLocResult.maxLoc.y

            val hist2 = Mat()
            Imgproc.calcHist(listOf(roiHsvMat), MatOfInt(1), mask, hist2, MatOfInt(255), MatOfFloat(0f, 255f))
            val minMaxLocResult2 = Core.minMaxLoc(hist2)
            val ballColor2 = minMaxLocResult2.maxLoc.y

            val hist3 = Mat()
            Imgproc.calcHist(listOf(roiHsvMat), MatOfInt(2), mask, hist3, MatOfInt(255), MatOfFloat(0f, 255f))
            val minMaxLocResult3 = Core.minMaxLoc(hist3)
            val ballColor3 = minMaxLocResult3.maxLoc.y


            Timber.d("Ball color: ($ballColor1, $ballColor2, $ballColor3)")


            var properType = BallType.UNKNOWN
            if (ballColor1 > 25 && ballColor1 < 35) {
                properType = BallType.YELLOW
            }
            if (ballColor1 > -1 && ballColor1 < 9) {
                properType = BallType.RED
            }
            if (ballColor1 > 10 && ballColor1 < 24) {
                properType = BallType.ORANGE
            }
            if (ballColor1 > 165 && ballColor1 < 180) {
                properType = BallType.BROWN
            }
            if (ballColor1 > 70 && ballColor1 < 95) {
                properType = BallType.GREEN
            }
            if (ballColor1 > 100 && ballColor1 < 115) {
                properType = BallType.BLUE
            }
            if (ballColor1 > 115 && ballColor1 < 140) {
                properType = BallType.PURPLE
            }
            if (ballColor3 < 40) {
                properType = BallType.BLACK
            }
            if (ballColor2 < 50) {
                properType = BallType.WHITE
            }

            Imgproc.circle(mat, ball.center, ball.radius, Scalar(255.0, 0.0, 255.0),
                3, 8, 0)

            val textPoint = Point(ball.center.x + 20, ball.center.y + 10)
            if (properType != BallType.UNKNOWN) {
                Imgproc.putText(mat, "($ballColor1, $ballColor2, $ballColor3) ${properType.name}", textPoint, Imgproc.FONT_HERSHEY_SIMPLEX, 1.0, Scalar(255.0, 0.0, 255.0), 2)
            } else {
                Imgproc.putText(mat, "($ballColor1, $ballColor2, $ballColor3)", textPoint, Imgproc.FONT_HERSHEY_SIMPLEX, 1.0, Scalar(255.0, 0.0, 255.0), 2)
            }
        }

        hacked = mat
        return balls
    }

    override fun recogniseBall(ball: Mat) {

    }

    override fun hackView(): Mat {
        val resizedMat = Mat()
        val newSize = Size(hacked.size().width * ratio, hacked.size().height * ratio)
        Imgproc.resize(hacked, resizedMat, newSize)

        return hacked
    }
}