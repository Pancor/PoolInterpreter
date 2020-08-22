package pl.pancordev.poolinterpreter.imageprocessing.balls

import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import timber.log.Timber

class BallsManagerImpl : BallsContract.BallsManager {

    private val ballTrackerImpl =  BallTrackerImpl()

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
            8, 13)
        Timber.d("Found ${circles.cols()} balls")

        val balls = mutableListOf<Ball>()
        //val size = if (circles.cols() > 16) { 16 } else { circles.cols() }
        val size = if (circles.cols() > 9) { 9 } else { circles.cols() }
        for (x in 0 until size) {
            val c = circles.get(0, x)
            val center = Point(Math.round(c[0]*ratio).toDouble(), Math.round(c[1]*ratio).toDouble())
            val radius = Math.round(c[2]*ratio).toInt()
            balls.add(Ball(x, center, radius))
        }

        for (ball in balls) {
            val ballSize = ball.radius * 2
            val contours = Rect((ball.center.x - ball.radius).toInt(), (ball.center.y - ball.radius).toInt(), ballSize, ballSize)
            val roi = Mat(mat, contours)

            val roiHsvMat = Mat()
            Imgproc.cvtColor(roi, roiHsvMat, Imgproc.COLOR_RGB2HSV)

            val mask = Mat.zeros(roi.size(), CvType.CV_8UC1)
            Imgproc.circle(mask, Point(ball.radius.toDouble(), ball.radius.toDouble()), ball.radius, Scalar(255.0, 255.0, 255.0), -1)

            val roiGray = Mat()
            Imgproc.cvtColor(roi, roiGray, Imgproc.COLOR_RGB2GRAY)
            val roiRemoveWhite = Mat()
            Imgproc.threshold(roiGray, roiRemoveWhite, 200.0, 255.0, Imgproc.THRESH_BINARY_INV)
            val finalMask = Mat()
            Core.bitwise_and(mask, roiRemoveWhite, finalMask)

            val whiteCnts: List<MatOfPoint> = mutableListOf()
            Core.bitwise_not(roiRemoveWhite, roiRemoveWhite)
            Imgproc.findContours(roiRemoveWhite, whiteCnts, Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE)
            for (cnt in whiteCnts) {
                ball.whiteArea += Imgproc.contourArea(cnt)
            }

            val hist1 = Mat()
            Imgproc.calcHist(listOf(roiHsvMat), MatOfInt(0), finalMask, hist1, MatOfInt(180), MatOfFloat(0f, 180f))
            val minMaxLocResult = Core.minMaxLoc(hist1)
            ball.ballColor1 = minMaxLocResult.maxLoc.y

            val hist2 = Mat()
            Imgproc.calcHist(listOf(roiHsvMat), MatOfInt(1), finalMask, hist2, MatOfInt(255), MatOfFloat(0f, 255f))
            val minMaxLocResult2 = Core.minMaxLoc(hist2)
            ball.ballColor2 = minMaxLocResult2.maxLoc.y

            val hist3 = Mat()
            Imgproc.calcHist(listOf(roiHsvMat), MatOfInt(2), finalMask, hist3, MatOfInt(255), MatOfFloat(0f, 255f))
            val minMaxLocResult3 = Core.minMaxLoc(hist3)
            ball.ballColor3 = minMaxLocResult3.maxLoc.y

            if (ball.ballColor1 > 20 && ball.ballColor1 < 35) {
                ball.color = BallType.YELLOW
            }
            if (ball.ballColor1 > -1 && ball.ballColor1 < 9 || ball.ballColor1 > 177) {
                ball.color = BallType.RED
            }
            if (ball.ballColor1 > 6 && ball.ballColor1 < 20) {
                ball.color = BallType.ORANGE
            }
            if (ball.ballColor1 > 165 && ball.ballColor1 < 178) {
                ball.color = BallType.BROWN
            }
            if (ball.ballColor1 > 70 && ball.ballColor1 < 95) {
                ball.color = BallType.GREEN
            }
            if (ball.ballColor1 > 100 && ball.ballColor1 < 115) {
                ball.color = BallType.BLUE
            }
            if (ball.ballColor1 > 115 && ball.ballColor1 < 140) {
                ball.color = BallType.PURPLE
            }
            if (ball.ballColor1 > 50 && ball.ballColor1 < 95 && ball.ballColor3 < 40) {
                ball.color = BallType.BLACK
            }
            if (ball.ballColor2 < 50) {
                ball.color = BallType.WHITE
            }

            for (it in balls) {
                if (it.color == ball.color && it.id != ball.id ) {
                    if (ball.whiteArea > it.whiteArea) {
                        ball.isStriped = true
                    } else if (ball.whiteArea < it.whiteArea) {
                        it.isStriped = true
                    }
                }
            }
        }
        val trackedBalls = ballTrackerImpl.update(balls)

        for ((key, ball) in trackedBalls) {
            Imgproc.circle(mat, ball.center, ball.radius, Scalar(255.0, 0.0, 255.0),
                3, 8, 0)

            val textPoint = Point(ball.center.x + 20, ball.center.y + 10)
            if (ball.color != BallType.UNKNOWN) {
                Imgproc.putText(mat, "#${key} (${ball.ballColor1}, ${ball.ballColor2}, ${ball.ballColor3}) ${ball.color.name} ${ball.whiteArea}, ${ball.isStriped}", textPoint, Imgproc.FONT_HERSHEY_SIMPLEX, 1.0, Scalar(255.0, 0.0, 255.0), 2)
            } else {
                Imgproc.putText(mat, "(${ball.ballColor1}, ${ball.ballColor2}, ${ball.ballColor3})", textPoint, Imgproc.FONT_HERSHEY_SIMPLEX, 1.0, Scalar(255.0, 0.0, 255.0), 2)
            }
        }
        hacked = mat
        return balls //do not trust this balls
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