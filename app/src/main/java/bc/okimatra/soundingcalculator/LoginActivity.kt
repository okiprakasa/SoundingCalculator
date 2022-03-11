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
import java.text.SimpleDateFormat
import java.util.*

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
                val nama = inputNama.text.toString()
                val nip = inputNip.text.toString()
                val sdfyear = SimpleDateFormat("yyyy", Locale.getDefault())
                val sdfdate = SimpleDateFormat("yyyyMM", Locale.getDefault())
                val year = sdfyear.format(Calendar.getInstance().time)
                val date = sdfdate.format(Calendar.getInstance().time)
                when {
                    nama.isEmpty() -> {
                        Toast.makeText(this@LoginActivity, "Mohon masukkan Nama Anda", Toast.LENGTH_SHORT).show()
                    }
                    nip.isEmpty() -> {
                        Toast.makeText(this@LoginActivity, "Mohon masukkan NIP Anda", Toast.LENGTH_SHORT).show()
                    }
                    nip.length != 18 -> {
                        Toast.makeText(this@LoginActivity, "Jumlah Digit NIP Anda Kurang", Toast.LENGTH_SHORT).show()
                    }
                    nip.substring(0,4).toInt() !in year.toInt()-90..year.toInt()-13 -> {
                        Toast.makeText(this@LoginActivity, "Mohon Periksa Tahun Lahir Anda", Toast.LENGTH_SHORT).show()
                    }
                    nip.substring(4,6).toInt() !in 1..12 -> {
                        Toast.makeText(this@LoginActivity, "Mohon Periksa Bulan Lahir Anda", Toast.LENGTH_SHORT).show()
                    }
                    nip.substring(6,8).toInt() !in 1..31 -> {
                        Toast.makeText(this@LoginActivity, "Mohon Periksa Hari Lahir Anda", Toast.LENGTH_SHORT).show()
                    }
                    nip.substring(8,12).toInt() !in nip.substring(0,4).toInt()+13..year.toInt() -> {
                        Toast.makeText(this@LoginActivity, "Mohon Periksa Tahun Penerimaan PNS Anda", Toast.LENGTH_SHORT).show()
                    }
                    nip.substring(8,12).toInt() - nip.substring(0,4).toInt() > 70 -> {
                        Toast.makeText(this@LoginActivity, "Mohon Periksa Tahun Lahir dan Tahun Penerimaan PNS Anda", Toast.LENGTH_SHORT).show()
                    }
                    nip.substring(12,14).toInt() !in 1..12 -> {
                        Toast.makeText(this@LoginActivity, "Mohon Periksa Bulan Penerimaan PNS Anda", Toast.LENGTH_SHORT).show()
                    }
                    nip.substring(8,14).toInt() > date.toInt() -> {
                        Toast.makeText(this@LoginActivity, "Anda Bulan Ini Belum Menjadi PNS", Toast.LENGTH_SHORT).show()
                    }
                    nip.substring(14,15).toInt() !in 1..2 -> {
                        Toast.makeText(this@LoginActivity, "Mohon Periksa Kode Terkait Jenis Kelamin Anda", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        val userDao = (application as UserApp).db.userDao()
                        lifecycleScope.launch {
                            userDao.insertUser(UserEntity(nama = nama, nip =  nip.toLong()))
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

@Suppress("unused")
fun nipcheck(str: String): Boolean {
    val sdfyear = SimpleDateFormat("yyyy", Locale.getDefault())
    val sdfdate = SimpleDateFormat("yyyyMM", Locale.getDefault())
    val year = sdfyear.format(Calendar.getInstance().time)
    val date = sdfdate.format(Calendar.getInstance().time)
    return if (str.length == 18) {
        ((str.substring(0,4).toInt() in year.toInt()-90..year.toInt()-13) &&
                (str.substring(4,6).toInt() in 1..12) &&
                (str.substring(6,8).toInt() in 1..31) &&
                (str.substring(8,12).toInt() in str.substring(0,4).toInt()+13..year.toInt()) &&
                (str.substring(8,12).toInt() - str.substring(0,4).toInt() <= 70) &&
                (str.substring(12,14).toInt() in 1..12) &&
                (str.substring(8,14).toInt() <= date.toInt()) &&
                (str.substring(14,15).toInt() in 1..2)
        )
    } else {
        false
    }
}