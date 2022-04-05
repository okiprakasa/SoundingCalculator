package bc.okimatra.soundingcalculator

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import bc.okimatra.soundingcalculator.databinding.LoginPageBinding
import bc.okimatra.soundingcalculator.datasetup.KantorEntity
import bc.okimatra.soundingcalculator.datasetup.PegawaiEntity
import bc.okimatra.soundingcalculator.datasetup.UserApp
import bc.okimatra.soundingcalculator.datasetup.UserDao
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: LoginPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val userDao = (application as UserApp).db.userDao()

        val sdfyear = SimpleDateFormat("yyyy", Locale.getDefault())
        val sdfdate = SimpleDateFormat("yyyyMM", Locale.getDefault())
        val year = sdfyear.format(Calendar.getInstance().time)
        val date = sdfdate.format(Calendar.getInstance().time)

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
            var checker = 0
            lifecycleScope.launch {
                userDao.countAllKantor().collect { it1 ->
                    if (it1!=12) {
                        if (checker == 0) {
                            checker = 1
                            insertKantorDB(userDao,
                                "Balikpapan",
                                "Kantor Pengawasan Dan Pelayanan Bea Dan Cukai Tipe Madya Pabean B Balikpapan".uppercase(),
                                "Kantor Wilayah Direktorat Jenderal Bea Dan Cukai Kalimantan Bagian Timur".uppercase(),
                                "Balikpapan",
                                "BAPFP-X/KBC.160107/",
                                "DP100300"
                            )
                            insertKantorDB(userDao,
                                "Banjarmasin",
                                "Kantor Pengawasan Dan Pelayanan Bea Dan Cukai Tipe Madya Pabean B Banjarmasin".uppercase(),
                                "Kantor Wilayah Direktorat Jenderal Bea Dan Cukai Kalimantan Bagian Selatan".uppercase(),
                                "Banjarmasin",
                                "BAPFP-X/KBC.150105/",
                                "DP100100"
                            )
                            insertKantorDB(userDao,
                                "Bitung",
                                "Kantor Pengawasan Dan Pelayanan Bea Dan Cukai Tipe Madya Pabean C Bitung".uppercase(),
                                "Kantor Wilayah Direktorat Jenderal Bea Dan Cukai Sulawesi Bagian Utara".uppercase(),
                                "Bitung",
                                "BAPFP-X/KBC.180404/",
                                "DP111100"
                            )
                            insertKantorDB(userDao,
                                "Bontang",
                                "Kantor Pengawasan Dan Pelayanan Bea Dan Cukai Tipe Madya Pabean C Bontang".uppercase(),
                                "Kantor Wilayah Direktorat Jenderal Bea Dan Cukai Kalimantan Bagian Timur".uppercase(),
                                "Bontang",
                                "BAPFP-X/KBC.1603/",
                                "DP100600"
                            )
                            insertKantorDB(userDao,
                                "Gresik",
                                "Kantor Pengawasan Dan Pelayanan Bea Dan Cukai Tipe Madya Pabean B Gresik".uppercase(),
                                "Kantor Wilayah Direktorat Jenderal Bea Dan Cukai Jawa Timur I".uppercase(),
                                "Gresik",
                                "BAPFP-X/KBC.110408/",
                                "DP070300"
                            )
                            insertKantorDB(userDao,
                                "Kotabaru",
                                "Kantor Pengawasan Dan Pelayanan Bea Dan Cukai Tipe Madya Pabean C Kotabaru".uppercase(),
                                "Kantor Wilayah Direktorat Jenderal Bea Dan Cukai Kalimantan Bagian Selatan".uppercase(),
                                "Kotabaru",
                                "BAPFP-X/KBC.150504/",
                                "DP100200"
                            )
                            insertKantorDB(userDao,
                                "Pangkalan Bun",
                                "Kantor Pengawasan Dan Pelayanan Bea Dan Cukai Tipe Madya Pabean C Pangkalan Bun".uppercase(),
                                "Kantor Wilayah Direktorat Jenderal Bea Dan Cukai Kalimantan Bagian Selatan".uppercase(),
                                "Pangkalan Bun",
                                "BAPFP-X/KBC.1503/",
                                "DP090800"
                            )
                            insertKantorDB(userDao,
                                "Pantoloan",
                                "Kantor Pengawasan Dan Pelayanan Bea Dan Cukai Tipe Madya Pabean C Pantoloan".uppercase(),
                                "Kantor Wilayah Direktorat Jenderal Bea Dan Cukai Sulawesi Bagian Utara".uppercase(),
                                "Pasangkayu",
                                "BAPFP-X/KBC.180104/",
                                "DP110800"
                            )
                            insertKantorDB(userDao,
                                "Samarinda",
                                "Kantor Pengawasan Dan Pelayanan Bea Dan Cukai Tipe Madya Pabean B Samarinda".uppercase(),
                                "Kantor Wilayah Direktorat Jenderal Bea Dan Cukai Kalimantan Bagian Timur".uppercase(),
                                "Samarinda",
                                "BAPFP-X/KBC.1602/",
                                "DP100500"
                            )
                            insertKantorDB(userDao,
                                "Sampit",
                                "Kantor Pengawasan Dan Pelayanan Bea Dan Cukai Tipe Madya Pabean C Sampit".uppercase(),
                                "Kantor Wilayah Direktorat Jenderal Bea Dan Cukai Kalimantan Bagian Selatan".uppercase(),
                                "Sampit, Kotawaringin Timur",
                                "BAPFP-X/KBC.1502/",
                                "DP090700"
                            )
                            insertKantorDB(userDao,
                                "Tanjung Emas",
                                "Kantor Pengawasan Dan Pelayanan Bea Dan Cukai Tipe Madya Pabean Tanjung Emas".uppercase(),
                                "Kantor Wilayah Direktorat Jenderal Bea Dan Cukai Jawa Tengah Dan DI Yogyakarta".uppercase(),
                                "Semarang",
                                "BAPFP-X/KBC.100106/",
                                "DP060100"
                            )
                            insertKantorDB(userDao,
                                "Tanjung Perak",
                                "Kantor Pengawasan Dan Pelayanan Bea Dan Cukai Tipe Madya Pabean Tanjung Perak".uppercase(),
                                "Kantor Wilayah Direktorat Jenderal Bea Dan Cukai Jawa Timur I".uppercase(),
                                "Surabaya",
                                "BAPFP-X/KBC.110102/",
                                "DP070100"
                            )
                        }
                    }
                    lifecycleScope.launch {
                        userDao.fetchAllKantor().collect {
                            val items = arrayListOf<String>()
                            for (j in it.indices) {
                                items.add(it[j].kota)
                            }
                            val adapter = ArrayAdapter(
                                applicationContext,
                                R.layout.dropdown_layout,
                                items
                            )
                            kantorPegawai.adapter = adapter
                        }
                    }
                }
            }


            btnStart.setOnClickListener {
                val nama = endSpaceRemover(inputNama.text.toString())
                val nip = inputNip.text.toString()
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
                        lifecycleScope.launch {
                            userDao.fetchKantorByKota(kantorPegawai.selectedItem.toString()).collect {
                                lifecycleScope.launch {
                                    userDao.insertUser(PegawaiEntity(nama_pegawai = nama,
                                        nip = nip,
                                        kota_pegawai = it.kota,
                                        kantor_pegawai = it.kantor,
                                        kanwil_pegawai = it.kanwil,
                                        lokasi_ba_pegawai = it.lokasi_ba,
                                        format_ba_pegawai = it.format_ba,
                                        format_3d_pegawai = it.format_3d
                                    ))
                                }
                            }
                        }
                        val intent = Intent(this@LoginActivity,MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }

    private fun insertKantorDB(userDao: UserDao, kota: String, kantor: String, kanwil: String, lokasiBa: String, formatBa: String, format3d: String) {
        lifecycleScope.launch {
            userDao.insertKantor(KantorEntity(
                kota = kota,
                kantor = kantor,
                kanwil = kanwil,
                lokasi_ba = lokasiBa,
                format_ba = formatBa,
                format_3d = format3d
            ))
        }
    }
}

fun endSpaceRemover(text:String): String {
    var newtext = text
    if (text.isNotEmpty()) {
        while (newtext.subSequence(newtext.length-1,newtext.length) == " ") {
            newtext = newtext.subSequence(0, newtext.length-1).toString()
        }
    }
    return newtext
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