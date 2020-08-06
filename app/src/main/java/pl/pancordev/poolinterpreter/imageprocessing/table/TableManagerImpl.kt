package pl.pancordev.poolinterpreter.imageprocessing.table

import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import timber.log.Timber

class TableManagerImpl : TableContract.TableManager {

    private var ratio = 0.0
    private var hacked = Mat()

    override fun getTable(mat: Mat): Array<Point> {
        //resize image
        val resizedMat = Mat()
        val newSize = Size(mat.size().width * 0.1, mat.size().height * 0.1)
        Imgproc.resize(mat, resizedMat, newSize)
        ratio = mat.size().width / resizedMat.size().width //TODO: store resizing ratio for future calculations of drawing proper scale of contours

        //convert to hsv with blur
        val hsvMat = Mat()
        Imgproc.cvtColor(resizedMat, hsvMat, Imgproc.COLOR_BGR2HSV)

        //TODO: Remove noise from hsv so contours of table don't 'jump'

        //find main color in region of interest
        val circle = Rect(hsvMat.width() / 2, hsvMat.height() / 2, hsvMat.width() / 4,hsvMat.height() / 4)
        val roi = Mat(hsvMat, circle)
        val hist = Mat()
        Imgproc.calcHist(listOf(roi), MatOfInt(0), Mat(), hist, MatOfInt(180), MatOfFloat(0f, 180f))
        val minMaxLocResult = Core.minMaxLoc(hist)
        val tableColor = minMaxLocResult.maxLoc.y

        Timber.d("Table color: $tableColor")

        return getTableContours(hsvMat, tableColor)
    }

    private fun getTableContours(table: Mat, tableColor: Double): Array<Point> {
        val thresh = Mat()
        Core.inRange(table, Scalar(tableColor - 5, 0.0, 0.0), Scalar(tableColor + 5, 255.0, 255.0), thresh)

        val allContours: List<MatOfPoint> = mutableListOf()
        val hierarchy = Mat()
        Imgproc.findContours(thresh, allContours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

        var max = 0.0
        var maxIndex = 0
        for (i in allContours.indices) {
            val area = Imgproc.contourArea(allContours[i])
            if (area > max){
                max = area
                maxIndex = i
            }
        }
        val rectPoints = MatOfPoint2f()
        if (allContours.isNotEmpty()) {
            val points = allContours[maxIndex].toArray()

            val epsilon = 0.05 * Imgproc.arcLength(MatOfPoint2f(*points), true)
            Timber.e("arcLength: ${Imgproc.arcLength(MatOfPoint2f(*points), true)} epsilon: $epsilon")
            Imgproc.approxPolyDP(MatOfPoint2f(*points), rectPoints, epsilon, true)

            val pts = rectPoints.toArray()
            pts.forEach { point ->
                run {
                    point.x *= ratio
                    point.y *= ratio
                }
            }
            return pts
        }
        return emptyArray()
    }

    override fun hackView(): Mat {
        val resizedMat = Mat()
        val newSize = Size(hacked.size().width * ratio, hacked.size().height * ratio)
        Imgproc.resize(hacked, resizedMat, newSize)

        return resizedMat
    }
}