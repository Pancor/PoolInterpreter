package pl.pancordev.poolinterpreter.imageprocessing.table

import org.opencv.core.Mat
import org.opencv.core.Point

interface TableContract {

    interface TableManager {

        fun getTable(mat: Mat): Array<Point>
    }
}