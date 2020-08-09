package pl.pancordev.poolinterpreter.gameplay

import android.Manifest
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.act_gameplay.*
import org.opencv.android.CameraBridgeViewBase
import pl.pancordev.poolinterpreter.R
import pl.pancordev.poolinterpreter.imageprocessing.balls.BallsManagerImpl
import pl.pancordev.poolinterpreter.imageprocessing.table.TableManagerImpl

class GameplayActivity : AppCompatActivity(), GameplayContract.View {

    private lateinit var gameplayPresenter: GameplayContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_gameplay)

        gameplayPresenter = GameplayPresenter(this, TableManagerImpl(),
            BallsManagerImpl())
        gameplayPresenter.onViewReady()
    }

    override fun onImageProcessingReady() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),
            GameplayPresenter.CAMERA_PERMISSION_CODE)
    }

    override fun setCameraListener(listener: CameraBridgeViewBase.CvCameraViewListener2) {
        cameraView.setCvCameraViewListener(listener)
    }

    override fun onCameraPermissionsGranted() {
        cameraView.setCameraPermissionGranted()
        cameraView.enableView()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        gameplayPresenter.setResult(requestCode, permissions, grantResults)
    }

    override fun onPause() {
        super.onPause()
        cameraView.disableView()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraView.disableView()
    }
}