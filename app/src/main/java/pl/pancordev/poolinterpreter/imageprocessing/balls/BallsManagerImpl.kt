package pl.pancordev.poolinterpreter.imageprocessing.balls

import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import pl.pancordev.poolinterpreter.imageprocessing.tools.ImageProcessingService

class BallsManagerImpl(private val imageProcessingService: ImageProcessingService) : BallsContract.BallsManager {

    private val ballTrackerImpl =  BallTrackerImpl()

    private var ratio = 0.0
    private var hacked = Mat()

    override fun getBalls(mat: Mat): List<Ball> {
        val resized = imageProcessingService.resizeImage(mat, 0.5)
        val resizedMat = resized.resizedImage
        ratio = resized.originalToResizedRatio

        val greyscale = Mat()
        Imgproc.cvtColor(resizedMat, greyscale, Imgproc.COLOR_BGR2GRAY)

        val blurred = Mat()
        Imgproc.blur(greyscale, blurred, Size(3.0,3.0))

        val circles = imageProcessingService.findCircles(blurred)
        val resizedCircles = imageProcessingService.getCirclesWithProperCoordinates(circles, 16, ratio)
        //return resizedCircles
        //this function should end here

        val trackedBalls = ballTrackerImpl.update(resizedCircles)

        val recognisedBalls = recogniseBall(trackedBalls, mat)

        hacked = imageProcessingService.draw(recognisedBalls, mat)
        return emptyList()
    }

    override fun recogniseBall(balls: List<Ball>, image: Mat): List<Ball> {
        for (ball in balls) {
            val roi = imageProcessingService.getRoiForCircle(ball.circle, image)
            val mask = imageProcessingService.getMaskBasedOnBallShape(roi)
            val roiRemoveWhite = imageProcessingService.getMaskForRemovingWhite(roi)

            val finalMask = Mat()
            Core.bitwise_and(mask, roiRemoveWhite, finalMask)

            val whiteArea = imageProcessingService.calculateWhiteAreaOfBall(roiRemoveWhite)

            val roiHsvMat = Mat()
            Imgproc.cvtColor(roi, roiHsvMat, Imgproc.COLOR_RGB2HSV)

            val hChannel = imageProcessingService.calculateMaxValue(roiHsvMat, finalMask, 0)
            val sChannel = imageProcessingService.calculateMaxValue(roiHsvMat, finalMask, 1)
            val vChannel = imageProcessingService.calculateMaxValue(roiHsvMat, finalMask, 2)
            val hsv = HSV(hChannel, sChannel, vChannel)

            val ballColor = imageProcessingService.resolveBallColor(hsv)

            ball.hsv = hsv
            ball.whiteArea = whiteArea
            ball.color = ballColor
            ball.isStriped = imageProcessingService.resolveWhichBallIsStripped(balls, ball)
        }
        return balls
    }

    override fun hackView(): Mat {
        val resizedMat = Mat()
        val newSize = Size(hacked.size().width * ratio, hacked.size().height * ratio)
        Imgproc.resize(hacked, resizedMat, newSize)

        return hacked
    }
}