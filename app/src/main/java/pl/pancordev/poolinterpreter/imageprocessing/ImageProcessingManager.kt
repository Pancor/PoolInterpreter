package pl.pancordev.poolinterpreter.imageprocessing

import org.opencv.core.Mat
import org.opencv.core.Point
import pl.pancordev.poolinterpreter.imageprocessing.table.TableManagerImpl

class ImageProcessingManager(private val tableManagerImpl: TableManagerImpl) {

    fun obtainTableCoordinates(image: Mat): List<Point> {
        tableManagerImpl.getTable(image)
        return emptyList()
    }
}