package bc.okimatra.soundingcalculator

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import bc.okimatra.soundingcalculator.databinding.LoginPageBinding
import bc.okimatra.soundingcalculator.datasetup.UserApp
import bc.okimatra.soundingcalculator.datasetup.UserEntity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: LoginPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding){
            var i = 1
            val thread: Thread = object : Thread() {
                override fun run() {
                    try {
                        while (!this.isInterrupted) {
                            sleep(2000)
                            runOnUiThread {
                                // update TextView here!
                                when {
                                    i%3 == 1 -> {
                                        surabaya.visibility = View.GONE
                                        kotabaru.visibility = View.VISIBLE
                                        pantoloan.visibility = View.GONE
                                    }
                                    i%3 == 2 -> {
                                        surabaya.visibility = View.GONE
                                        kotabaru.visibility = View.GONE
                                        pantoloan.visibility = View.VISIBLE
                                    }
                                    else -> {
                                        surabaya.visibility = View.VISIBLE
                                        kotabaru.visibility = View.GONE
                                        pantoloan.visibility = View.GONE
                                    }
                                }
                                i += 1
                            }
                        }
                    } catch (e: InterruptedException) {
                    }
                }
            }

            thread.start()

            btnStart.setOnClickListener {
                when {
                    inputNama.text.toString().isEmpty() -> {
                        Toast.makeText(this@LoginActivity, "Mohon masukkan Nama Anda", Toast.LENGTH_SHORT).show()
                    }
                    inputNip.text.toString().isEmpty() -> {
                        Toast.makeText(this@LoginActivity, "Mohon masukkan NIP Anda", Toast.LENGTH_SHORT).show()
                    }
                    digitcheck(inputNip.text.toString()) -> {
                        Toast.makeText(this@LoginActivity, "Mohon cek kembali NIP Anda", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        val userDao = (application as UserApp).db.userDao()
                        lifecycleScope.launch {
                            userDao.insertUser(UserEntity(nama = inputNama.text.toString(), nip =  inputNip.text.toString().toLong()))
                        }
                        val intent = Intent(this@LoginActivity,MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }
}

fun digitcheck(str: String): Boolean {
    val num = str.toLong()
    return (num/1000000000000000 < 194 || num/10000000000000000 > 21)
}