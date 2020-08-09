package pl.pancordev.poolinterpreter.imageprocessing

import org.opencv.core.Mat

/**
 * It is only a hack to change output of processed image, to see what is under the hood. Do not use
 * it in production code.
 */
interface BypassHack {

    fun hackView(): Mat
}