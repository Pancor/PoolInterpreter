package pl.pancordev.poolinterpreter.imageprocessing.table

import org.opencv.core.Mat
import org.opencv.core.Point
import pl.pancordev.poolinterpreter.imageprocessing.BypassHack

interface TableContract {

    interface TableManager: BypassHack {

        fun getTable(mat: Mat): Array<Point>
    }
}