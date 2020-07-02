package pl.pancordev.poolinterpreter

import android.app.Application
import org.opencv.android.OpenCVLoader
import timber.log.Timber
import timber.log.Timber.DebugTree

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        initLogger()
        initOpenCV()
    }

    private fun initLogger() {
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
    }

    // TODO: should initialize OpenCV in async way
    // OpenCVLoader.initAsync("4.3.0", this, managerCallback)
    private fun initOpenCV() {
        if (OpenCVLoader.initDebug()) {
            Timber.d("OpenCV initialized")
        } else {
            Timber.e("Could not initialize OpenCV")
        }
    }
}