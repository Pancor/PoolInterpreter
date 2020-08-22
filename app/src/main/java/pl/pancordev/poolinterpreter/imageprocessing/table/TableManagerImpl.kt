package pl.pancordev.poolinterpreter.imageprocessing.table

import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import timber.log.Timber

class TableManagerImpl : TableContract.TableManager {

    private var hacked = Mat()

    private fun resizeImage(imageToResize: Mat, percentage: Double): Resized {
        val resizedImage = Mat()

        val imageSize = imageToResize.size()
        val newSize = Size(imageSize.width * percentage, imageSize.height * percentage)
        Imgproc.resize(imageToResize, resizedImage, newSize)

        val originalImageSizeToResizedRatio = imageSize.width / resizedImage.size().width
        return Resized(resizedImage, originalImageSizeToResizedRatio)
    }

    override fun getTable(mat: Mat): List<Point> {
        //resize image
        val resizedMat = resizeImage(mat, 0.1).resizedImage

        //convert to hsv with blur
        val hsvMat = Mat()
        Imgproc.cvtColor(resizedMat, hsvMat, Imgproc.COLOR_RGB2HSV)

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

    private fun getTableContours(table: Mat, tableColor: Double): List<Point> {
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
            Imgproc.approxPolyDP(MatOfPoint2f(*points), rectPoints, epsilon, true)

            val pts = rectPoints.toArray()
            pts.forEach { point ->
                run {
//                    point.x *= originalImageSizeToResizedRatio
//                    point.y *= originalImageSizeToResizedRatio    //TODO: obtain ratio
                }
            }
            return pts.toList()
        }
        return emptyList()
    }

    override fun hackView(): Mat {
        val resizedMat = Mat()
//        val newSize = Size(hacked.size().width * originalImageSizeToResizedRatio, hacked.size().height * originalImageSizeToResizedRatio) //TODO: obtain ratio
//        Imgproc.resize(hacked, resizedMat, newSize)

        return resizedMat
    }

    data class Resized(val resizedImage: Mat, val originalImageSizeToResizedRatio: Double)
}