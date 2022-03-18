package bc.okimatra.soundingcalculator

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import bc.okimatra.soundingcalculator.databinding.ActivitySplashBinding
import bc.okimatra.soundingcalculator.datasetup.UserApp
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val topSplash = AnimationUtils.loadAnimation(this,R.anim.topsplash)
        val botSplash = AnimationUtils.loadAnimation(this,R.anim.botsplash)

        binding.apply{
            topTextSplash.startAnimation(topSplash)
            botTextSplash.startAnimation(botSplash)
        }
        val splashScreenTimeOut = 650
        val userDao = (application as UserApp).db.userDao()
        var loginIntent = Intent(this@SplashActivity,LoginActivity::class.java)
        lifecycleScope.launch {
            userDao.countAllUser().collect {
                if(it > 0) {
                    loginIntent = Intent(this@SplashActivity,MainActivity::class.java)
                }
            }
        }

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(loginIntent)
            finish()
        }, splashScreenTimeOut.toLong())
    }
}