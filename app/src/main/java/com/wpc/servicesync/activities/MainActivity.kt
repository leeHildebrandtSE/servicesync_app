package com.wpc.servicesync.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.wpc.servicesync.R
import com.wpc.servicesync.utils.DebugUtils

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Show debug button in debug mode
        if (DebugUtils.isDebugMode) {
            showDebugOption()
        }

        // Navigate to Login Activity after a short delay
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            if (!isFinishing) {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }, 2000) // 2 second delay to show splash screen
    }

    private fun showDebugOption() {
        // Add debug button dynamically
        val debugButton = Button(this).apply {
            text = "🛠️ Debug Panel"
            setTextColor(getColor(R.color.white))
            background = getDrawable(R.drawable.button_secondary)
            alpha = 0.7f

            setOnClickListener {
                startActivity(Intent(this@MainActivity, DebugActivity::class.java))
            }
        }

        // Find the main layout and add the debug button
        val mainLayout = findViewById<View>(android.R.id.content)
        if (mainLayout is android.view.ViewGroup) {
            val layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(32, 32, 32, 32)
            }

            // Create a container for the debug button
            val debugContainer = android.widget.LinearLayout(this).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                gravity = android.view.Gravity.BOTTOM or android.view.Gravity.END
                addView(debugButton, layoutParams)
            }

            val containerParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
                android.view.Gravity.BOTTOM or android.view.Gravity.END
            )

            (mainLayout as android.widget.FrameLayout).addView(debugContainer, containerParams)
        }
    }
}