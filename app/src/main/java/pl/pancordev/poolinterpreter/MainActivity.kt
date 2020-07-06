package pl.pancordev.poolinterpreter

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.act_main.*
import pl.pancordev.poolinterpreter.gameplay.GameplayActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_main)

        gameplayBtn.setOnClickListener {
            startActivity(Intent(this, GameplayActivity::class.java))
        }
    }
}