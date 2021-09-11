package hu.bme.aut.conicon.ui

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import hu.bme.aut.conicon.R

/**
 * This Activity is responsible for the splash screen of the application
 */
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo = findViewById<ImageView>(R.id.ivLogo)

        // Making a simple animation for the logo on the splash screen
        logo.animate().setDuration(2000).rotationYBy(360f).scaleX(2.5f).scaleY(2.5f)
                .setListener(object: Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {
                // It can be empty
            }

            override fun onAnimationEnd(animation: Animator?) {
                // When the animation ends, the NavigationActivity will start
                startActivity(
                        Intent(
                                applicationContext,
                                NavigationActivity::class.java
                        ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
            }

            override fun onAnimationCancel(animation: Animator?) {
                // It can be empty
            }

            override fun onAnimationRepeat(animation: Animator?) {
                // It can be empty
            }
        })
    }
}