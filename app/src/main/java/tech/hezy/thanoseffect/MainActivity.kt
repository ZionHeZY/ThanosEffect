package tech.hezy.thanoseffect

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var thanosContainer: ThanosDisintegrationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        thanosContainer = findViewById(R.id.thanosContainer)
        val startButton = findViewById<Button>(R.id.startButton)
        val resetButton = findViewById<Button>(R.id.resetButton)

        startButton.setOnClickListener {
            thanosContainer.startDisintegration()
        }

        resetButton.setOnClickListener {
            thanosContainer.reset()
        }
    }
}