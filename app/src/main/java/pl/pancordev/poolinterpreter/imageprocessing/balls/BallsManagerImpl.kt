package pl.pancordev.poolinterpreter.imageprocessing.balls

import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import timber.log.Timber

class BallsManagerImpl : BallsContract.BallsManager {

    private var ratio = 0.0
    private var hacked = Mat()

    override fun getBalls(mat: Mat) {
        val resizedMat = Mat()
        val newSize = Size(mat.size().width * 0.5, mat.size().height * 0.5)
        Imgproc.resize(mat, resizedMat, newSize)
        ratio = mat.size().width / resizedMat.size().width //TODO: store resizing ratio for future calculations of drawing proper scale of contours

        val greyscale = Mat()
        Imgproc.cvtColor(resizedMat, greyscale, Imgproc.COLOR_BGR2GRAY)

        val blurred = Mat()
        Imgproc.blur(greyscale, blurred, Size(6.0,6.0))

        val circles = Mat()
        Imgproc.HoughCircles(blurred, circles, Imgproc.HOUGH_GRADIENT, 1.0,
            20.0, 50.0, 17.0,
            5, 13)
        Timber.d("Found ${circles.cols()} balls")

        val size = if (circles.cols() > 17) { 17 } else { circles.cols() }
        for (x in 0 until size) { //16 because there is only 16 balls
            val c = circles.get(0, x)
            val center = Point(Math.round(c[0]*ratio).toDouble(), Math.round(c[1]*ratio).toDouble())
            val radius = Math.round(c[2]*ratio).toInt()
            Imgproc.circle(mat, center, radius, Scalar(255.0, 0.0, 255.0),
                3, 8, 0)
        }

        hacked = mat
    }

    override fun hackView(): Mat {
        val resizedMat = Mat()
        val newSize = Size(hacked.size().width * ratio, hacked.size().height * ratio)
        Imgproc.resize(hacked, resizedMat, newSize)

        return hacked
    }
}