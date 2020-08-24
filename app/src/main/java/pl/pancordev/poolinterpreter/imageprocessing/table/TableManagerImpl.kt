package pl.pancordev.poolinterpreter.imageprocessing.table

import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import pl.pancordev.poolinterpreter.imageprocessing.tools.ImageProcessingService
import timber.log.Timber

class TableManagerImpl(private val imageProcessingService: ImageProcessingService) : TableContract.TableManager {

    var ratio = 1.0

    override fun getTable(mat: Mat): List<Point> {
        val resized = imageProcessingService.resizeImage(mat, 0.1)
        ratio = resized.originalToResizedRatio
        val hsvMat = Mat()
        Imgproc.cvtColor(resized.resizedImage, hsvMat, Imgproc.COLOR_RGB2HSV)

        //TODO: Remove noise from hsv so contours of table don't 'jump'

        val tableColor = imageProcessingService.getTableColor(hsvMat)

        val contours = imageProcessingService.obtainAllPossibleContoursBasedOnColor(hsvMat, tableColor)

        if (contours.isNotEmpty()) {
            val tableContour = imageProcessingService.getTheLargestContour(contours)
            val reducedTablePoints = imageProcessingService.reduceContourPoints(tableContour)

            return imageProcessingService.resizeCoordinates(reducedTablePoints, resized.originalToResizedRatio)
        }
        return emptyList()
    }

    override fun hackView(): Mat {
        val hacked = imageProcessingService.hackView()
        val resizedMat = Mat()
        val newSize = Size(hacked.size().width * ratio, hacked.size().height * ratio)
        Imgproc.resize(hacked, resizedMat, newSize)

        return resizedMat
    }
}