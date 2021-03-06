package pl.pancordev.poolinterpreter.gameplay

import android.content.pm.PackageManager
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.OpenCVLoader
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import pl.pancordev.poolinterpreter.imageprocessing.balls.Ball
import pl.pancordev.poolinterpreter.imageprocessing.balls.BallsContract
import pl.pancordev.poolinterpreter.imageprocessing.table.TableContract
import timber.log.Timber

class GameplayPresenter constructor(private val gameplayView: GameplayContract.View,
                                    private val tableManager: TableContract.TableManager,
                                    private val ballsManager: BallsContract.BallsManager) :
    GameplayContract.Presenter, CameraBridgeViewBase.CvCameraViewListener2 {

    companion object {
        const val CAMERA_PERMISSION_CODE = 1
    }

    override fun onViewReady() {
        Timber.d("On view ready")

        if (OpenCVLoader.initDebug()) {
            gameplayView.onImageProcessingReady()
        } else {
            Timber.e("Could not init OpenCV")
        }
    }

    override fun processImage(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        return inputFrame.gray()
    }

    override fun setResult(requestCode: Int, permissions: Array<out String>,
                           grantResults: IntArray) {
        when (requestCode) {
            CAMERA_PERMISSION_CODE ->
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Timber.d("Camera permissions granted")
                    gameplayView.onCameraPermissionsGranted()
                    gameplayView.setCameraListener(this)
                } else {
                    Timber.e("Permissions not granted for camera")
                }
        }
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        Timber.d("CameraView started")
    }

    override fun onCameraViewStopped() {
        Timber.d("CameraView stopped")
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        val table = table(inputFrame.rgba())
        val balls = ballsManager.getBalls(table)
        val drawnBalls = balls(table, balls)

        //return table
        //return drawnBalls
        return ballsManager.hackView()
    }

    private fun table(mat: Mat): Mat {
        val points = tableManager.getTable(mat)

        val tablePoints = MatOfPoint(*points)
        Timber.d("Found table points: ${points.size}")
        val mask = Mat.zeros(mat.size(), CvType.CV_8UC1)
        Imgproc.fillPoly(mask, listOf(tablePoints), Scalar(255.0, 255.0, 255.0))

        val cropped = Mat(mat.size(), CvType.CV_8UC1)
        mat.copyTo(cropped, mask)

        return cropped
    }

    private fun balls(mat: Mat, balls: List<Ball>): Mat {
        balls.forEach { ball ->
            Imgproc.circle(mat, ball.center, ball.radius, Scalar(255.0, 0.0, 255.0),
                3, 8, 0)
        }
        return mat
    }
}