package bc.okimatra.soundingcalculator

import android.Manifest
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import bc.okimatra.soundingcalculator.adapter.*
import bc.okimatra.soundingcalculator.databinding.*
import bc.okimatra.soundingcalculator.datasetup.*
import com.itextpdf.text.*
import com.itextpdf.text.pdf.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.round
import kotlin.math.roundToLong


class TabFragment(private val title: String) : Fragment() {
    private var _binding1: FragmentOneBinding? = null
    private val binding1 get() = _binding1!!
    private var _binding2: FragmentTwoBinding? = null
    private val binding2 get() = _binding2!!
    private var _binding3: FragmentThreeBinding? = null
    private val binding3 get() = _binding3!!

    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private val timeID = "EEEE, dd-MMMM-yyyy"
    private val sdf = SimpleDateFormat(timeID, Locale.getDefault())
    private val tanggalEng = sdf.format(cal.time).toString()
    private val tanggalID = dayConverter(monthConverter(tanggalEng))
    private val tz = TimeZone.getDefault()
    private val now = Date()
    private val timeZone: String = when ((tz.getOffset(now.time) / 3600000.0)) {
        in 6.9..7.8 -> {"WIB"}
        in 7.9..8.8 -> {"WITA"}
        in 8.9..9.8 -> {"WIT"}
        in -13.0..-0.1 -> {
            "GMT" + (tz.getOffset(now.time) / 3600000.0).toString().replace(".0","")
        }
        else -> {
            "GMT+" + (tz.getOffset(now.time) / 3600000.0).toString().replace(".0","")
        }
    }
    private val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
    private val currentyear = yearFormat.format(cal.time).toString()
//    private val timeFormat = SimpleDateFormat("EEEE, dd-MMMM-yyyy hh:mm", Locale.getDefault())
//    private val currentTime = dayConverter(monthConverter(timeFormat.format(cal.time).toString())) + " $timeZone"
//private val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
//    private val currentdate = dateFormat.format(cal.time).toString()

    private lateinit var ivCvMap: MutableMap<ImageView, CardView>
    private lateinit var ivTvMap: MutableMap<ImageView, TextView>
    private lateinit var ivSpAwalMap: MutableMap<ImageView, Spinner>
    private lateinit var ivSpAkhirMap: MutableMap<ImageView, Spinner>
    private lateinit var spAwalDbFinalMap: MutableMap<Spinner, Double>
    private lateinit var spAkhirDbFinalMap: MutableMap<Spinner, Double>
    private var soundingAwalTotal = 0.0
    private var soundingAkhirlTotal = 0.0
    private var counterSounding = 1
    private var fabOverSounding = true

    private var baseFontRegular  = BaseFont.createFont("res/font/nunito.ttf", "UTF-8", BaseFont.EMBEDDED)
    private var regularFont = Font(Font.FontFamily.HELVETICA, 11f, Font.NORMAL, BaseColor.BLACK)
    private var appFontTiny = Font(baseFontRegular, 2f)
    private var appFontMiddle = Font(baseFontRegular, 8f)
    private var appFontSemiBig = Font(Font.FontFamily.HELVETICA, 13f, Font.BOLD, BaseColor.BLACK)
    private var baseFontBig = BaseFont.createFont("res/font/nexa.otf", "UTF-8", BaseFont.EMBEDDED)
    private var appFontBig= Font(baseFontBig, 16f, Font.BOLD)

    private var baseFontArial = BaseFont.createFont("res/font/arial.ttf", "UTF-8", BaseFont.EMBEDDED)
    private var fontArialBigBold = Font(baseFontArial, 13f, Font.BOLD, BaseColor.BLACK)
    private var fontArialRegular = Font(baseFontArial, 11f, Font.NORMAL, BaseColor.BLACK)
    private var fontArialRegularBold = Font(baseFontArial, 11f, Font.BOLD, BaseColor.BLACK)
    private var fontArialSmall = Font(baseFontArial, 8f, Font.NORMAL, BaseColor.BLACK)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View {
        return when {
            title === "Calculator" -> {
                _binding1 = FragmentOneBinding.inflate(inflater, container, false)
                binding1.root
            }
            title === "Data" -> {
                _binding2 = FragmentTwoBinding.inflate(inflater, container, false)
                binding2.root
            }
            else -> {
                _binding3 = FragmentThreeBinding.inflate(inflater, container, false)
                binding3.root
            }
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userDao = (requireActivity().application as UserApp).db.userDao()

        when {
            title === "Calculator" -> {
                var results: List<Double>
                binding1.apply{

                    waktu.setOnClickListener {
                        dateSetListener = DatePickerDialog.OnDateSetListener {
                                _, year, month, dayOfMonth ->
                            cal.set(Calendar.YEAR, year)
                            cal.set(Calendar.MONTH, month)
                            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                            val mcurrentTime = Calendar.getInstance()
                            val hour = mcurrentTime[Calendar.HOUR_OF_DAY]
                            val minute = mcurrentTime[Calendar.MINUTE]
                            val mTimePicker = TimePickerDialog(
                                requireContext(),
                                R.style.TimePickerTheme,
                                { _, selectedHour, selectedMinute -> waktu.setText(String.format(getString(R.string.format_waktu, tanggalID, selectedHour, selectedMinute, timeZone))) },
                                hour,
                                minute,
                                true
                            )
                            mTimePicker.show()
                        }
                        DatePickerDialog(
                            requireContext(),
                            R.style.TimePickerTheme,
                            dateSetListener,
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }

                    interpolasiTab.setOnClickListener {
                        interpolasiTab.background = ResourcesCompat.getDrawable(resources, R.drawable.switch_on,null)
                        interpolasiTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.white))
                        fraksiTab.background = null
                        fraksiTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.appTheme))
                        judulTabelFraksi.visibility = View.GONE
                        fraksi.visibility = View.GONE
                        if ("m" !in judulTabelKalibrasi.text.toString()) {
                            judulTabelKalibrasi.text = getText(R.string.tabel_kalibrasi1)
                        }
                        tabelKalibrasi.hint = getText(R.string.vol_kalibrasi1)
                        judulTabelKalibrasi2.visibility = View.VISIBLE
                        kalibrasi2.visibility = View.VISIBLE
                        tabelFraksi.text = null
                    }

                    fraksiTab.setOnClickListener {
                        fraksiTab.background = ResourcesCompat.getDrawable(resources, R.drawable.switch_on,null)
                        fraksiTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.white))
                        interpolasiTab.background = null
                        interpolasiTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.appTheme))
                        judulTabelKalibrasi2.visibility = View.GONE
                        kalibrasi2.visibility = View.GONE
                        if ("m" !in judulTabelKalibrasi.text.toString()) {
                            judulTabelKalibrasi.text = getText(R.string.tabel_kalibrasi)
                        }
                        tabelKalibrasi.hint = getText(R.string.vol_kalibrasi)
                        judulTabelFraksi.visibility = View.VISIBLE
                        fraksi.visibility = View.VISIBLE
                        tabelKalibrasi2.text = null
                    }

                    down.setOnClickListener {
                        hasilVolume.visibility = View.VISIBLE
                        hasilVolumeApp.visibility = View.VISIBLE
                        hasilVolumeObs.visibility = View.VISIBLE
                        hasilTinggiTerkoreksi.visibility = View.VISIBLE
                        up.visibility = View.VISIBLE
                        down.visibility = View.GONE
                    }

                    up.setOnClickListener {
                        hasilVolume.visibility = View.GONE
                        hasilVolumeApp.visibility = View.GONE
                        hasilVolumeObs.visibility = View.GONE
                        hasilTinggiTerkoreksi.visibility = View.GONE
                        up.visibility = View.GONE
                        down.visibility = View.VISIBLE
                    }

                    next.setOnClickListener {
                        val hasil = hasilKalkulator.text.toString()
                        when {
                            hasil == "Hasil: 0.000 MT" -> {
                                Toast.makeText(context, "Mohon Cek Data\nNilai Hasil Masih 0", Toast.LENGTH_SHORT).show()
                            }
                            hasil.subSequence(hasil.indexOf(":")+1, hasil.length-3).toString().replace(",",".").replace(" ","").toDouble()<0 -> {
                                Toast.makeText(context, "Mohon Cek Data\nHasil Bernilai Negatif", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                lifecycleScope.launch {
                                    userDao.countAllUser().collect { it1 ->
                                        if (it1>0) {
                                            lifecycleScope.launch {
                                                userDao.countAllServiceUser().collect { it2 ->
                                                    if (it2>0) {
                                                        dataLapanganLayout.visibility = View.GONE
                                                        dataTangkiLayout.visibility = View.GONE
                                                        dataTabelLayout.visibility = View.GONE
                                                        hasilLayout.visibility = View.GONE
                                                        next.visibility = View.GONE
                                                        dataAdministrasiLayout.visibility = View.VISIBLE
                                                        back.visibility = View.VISIBLE
                                                        simpanHasil.visibility = View.VISIBLE
                                                        Handler(Looper.getMainLooper()).postDelayed({
                                                            scrollView.fullScroll(ScrollView.FOCUS_UP)
                                                        }, 1)
                                                        lifecycleScope.launch {
                                                            userDao.fetchAllUser().collect {
                                                                populateDropdownUser(ArrayList(it), namaPegawai)
                                                            }
                                                        }
                                                        lifecycleScope.launch {
                                                            userDao.fetchAllServiceUser().collect {
                                                                populateDropdownServiceUser(ArrayList(it), namaPenggunaJasa)
                                                            }
                                                        }
                                                    }
                                                    else {
                                                        Toast.makeText(context, "Mohon Tambahkan Data\nPengguna Jasa Terlebih Dahulu\nPada Tab User", Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                            }
                                        }
                                        else {
                                            Toast.makeText(context, "Mohon Tambahkan Data Pegawai\nTerlebih Dahulu Pada Tab User", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            }
                        }
                    }

                    back.setOnClickListener {
                        backFunction()
                    }

                    namaPenggunaJasa.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                        override fun onNothingSelected(parent: AdapterView<*>?) {
                        }

                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            lifecycleScope.launch {
                                userDao.fetchServiceUserByName(namaPenggunaJasa.selectedItem.toString()).collect {
                                    try {
                                        lokasiSounding.setText(String.format(getString(R.string.lokasi_edited),it.perusahaan_pengguna_jasa))
                                    } catch (e: Exception) {
                                        Log.d("okimatra", "" + e.message)
                                    }
                                }
                            }
                        }
                    }

                    val listETTinggi = listOf(tinggiCairan, tinggiMeja)
                    results = calculatorTinggiListener(listETTinggi)

                    val listET = listOf(suhuCairan, suhuTetap, muai, tabelFraksi, tabelKalibrasi, tabelKalibrasi2, densityCairan)
                    results = calculatorListener(listET)

                    simpanHasil.setOnClickListener {
                        val nomorTangkiText = endSpaceRemover(_binding1?.noTangki?.text.toString().uppercase())
                        val lokasiSoundingText =  endSpaceRemover(_binding1?.lokasiSounding?.text.toString())
                        val waktuText = _binding1?.waktu?.text.toString()
                        when {
                            nomorTangkiText.isEmpty() -> {
                                Toast.makeText(requireContext(), "Nomor Tangki Belum Diisi", Toast.LENGTH_SHORT).show()
                            }
                            lokasiSoundingText.isEmpty() -> {
                                Toast.makeText(requireContext(), "Lokasi Sounding Belum Diisi", Toast.LENGTH_SHORT).show()
                            }
                            waktuText.isEmpty() -> {
                                Toast.makeText(requireContext(), "Waktu Sounding Belum Diisi", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                val tinggiCairanAngka = _binding1?.tinggiCairan?.text.toString().toDouble()
                                val suhuCairanAngka = _binding1?.suhuCairan?.text.toString().toDouble()
                                val suhuKalibrasiAngka = _binding1?.suhuTetap?.text.toString().toDouble()
                                val tinggiMejaAngka = _binding1?.tinggiMeja?.text.toString().toDouble()
                                val koefisienMuai = _binding1?.muai?.text.toString().toDouble()
                                val volumeKalibrasi = _binding1?.tabelKalibrasi?.text.toString().toDouble()
                                val densityAngka = _binding1?.densityCairan?.text.toString().toDouble()
                                val petugasSounding = _binding1?.namaPegawai?.selectedItem.toString()
                                val penggunaJasa = _binding1?.namaPenggunaJasa?.selectedItem.toString()
                                val judulKalibrasi1 = _binding1?.judulTabelKalibrasi?.text.toString().replace(".",",")
                                val judulKalibrasi2 = _binding1?.judulTabelKalibrasi2?.text.toString().replace(".",",")
                                val judulFraksi = _binding1?.judulTabelFraksi?.text.toString()
                                val dataTabel = _binding1?.dataTabel?.text.toString().replace("Data Tabel (","").replace(")","").replace(".",",")
                                results = soundingCalculator()
                                lifecycleScope.launch {
                                    userDao.fetchServiceUserByName(penggunaJasa).collect { it3 ->
                                        try {
                                            val npwp = it3.npwp_perusahaan
                                            val alamat = it3.alamat_perusahaan
                                            val perusahaan = it3.perusahaan_pengguna_jasa
                                            val jabatan = it3.jabatan
                                            lifecycleScope.launch {
                                                userDao.fetchUserByName(petugasSounding).collect {
                                                    try {
                                                        val nip = it.nip
                                                        lifecycleScope.launch {
                                                            userDao.insertSounding(SoundingEntity(
                                                                tinggi_cairan = tinggiCairanAngka,
                                                                suhu_cairan = suhuCairanAngka,
                                                                suhu_kalibrasi_tangki = suhuKalibrasiAngka,
                                                                tinggi_meja = tinggiMejaAngka,
                                                                faktor_muai = koefisienMuai,
                                                                volume_kalibrasi1 = volumeKalibrasi,
                                                                density_cairan = densityAngka,
                                                                tinggi_cairan_terkoreksi = results[0],
                                                                volume_fraksi = results[1],
                                                                volume_kalibrasi2 = results[2],
                                                                volume_mid = results[3],
                                                                volume_app = results[4],
                                                                volume_obs = results[5],
                                                                volume = results[6],
                                                                hasil_sounding = results[7],
                                                                no_tangki = nomorTangkiText,
                                                                pegawai_sounding = petugasSounding,
                                                                nip_pegawai = nip,
                                                                pengguna_jasa_sounding = penggunaJasa,
                                                                jabatan_pengguna_jasa = jabatan,
                                                                perusahaan_sounding = perusahaan,
                                                                npwp_perusahaan_sounding = npwp,
                                                                alamat_perusahaan_sounding = alamat,
                                                                lokasi_sounding = lokasiSoundingText,
                                                                waktu = waktuText,
                                                                waktu_date = Date().time,
                                                                judulKalibrasi1 = judulKalibrasi1,
                                                                judulKalibrasi2 = judulKalibrasi2,
                                                                judulFraksi = judulFraksi,
                                                                judulDataTabel = dataTabel
                                                            ))
                                                            Toast.makeText(requireContext(), "Data Telah Tersimpan", Toast.LENGTH_SHORT).show()
                                                            _binding1?.tinggiCairan?.text?.clear()
                                                            _binding1?.suhuCairan?.text?.clear()
                                                            _binding1?.suhuTetap?.text?.clear()
                                                            _binding1?.tinggiMeja?.text?.clear()
                                                            _binding1?.muai?.text?.clear()
                                                            _binding1?.tabelKalibrasi?.text?.clear()
                                                            _binding1?.densityCairan?.text?.clear()
                                                            _binding1?.tabelFraksi?.text?.clear()
                                                            _binding1?.tabelKalibrasi2?.text?.clear()
                                                            _binding1?.noTangki?.text?.clear()
                                                            _binding1?.lokasiSounding?.text?.clear()
                                                            backFunction()
                                                        }
                                                    } catch (e: Exception) {
                                                        Log.d("okimatra", "" + e.message)
                                                    }
                                                }
                                            }
                                        } catch (e: Exception) {
                                            Log.d("okimatra", "" + e.message)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            title === "Data" -> {
                binding2.apply {
                    lifecycleScope.launch {
                        userDao.fetchAllUser().collect {
                            if (it.isNotEmpty()) {
                                noBa.setText(it[0].format_ba_pegawai)
                                noBa.hint = it[0].format_ba_pegawai
                                lokasiBa.setText(it[0].lokasi_ba_pegawai)
                                lokasiBa.hint = String.format(getString(R.string.hint_lokasi_ba), it[0].lokasi_ba_pegawai)
                            }
                        }
                    }
                    noDokumen.setText(String.format(getString(R.string.no_dokumen_edited),currentyear))

                    rawDataTab.background = ResourcesCompat.getDrawable(resources, R.drawable.switch_on,null)
                    rawDataTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.white))
                    finalTab.background = null
                    finalTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.appTheme))
                    fabFinalReport.visibility = View.GONE
                    svSoundingList.visibility = View.VISIBLE
                    svFinalList.visibility = View.GONE
                    tvNoFinalDataAvailable.visibility = View.GONE

                    Handler(Looper.getMainLooper()).postDelayed({
                        lifecycleScope.launch {
                            userDao.fetchAllSounding().collect {
                                Log.d("okimatra", "$it")
                                val list = ArrayList(it)
                                setupListOfDataIntoRecyclerViewSounding(list,userDao)
                            }
                        }
                    }, 10)

                    rawDataTab.setOnClickListener {
                        rawDataTab.background = ResourcesCompat.getDrawable(resources, R.drawable.switch_on,null)
                        rawDataTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.white))
                        finalTab.background = null
                        finalTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.appTheme))
                        fabFinalReport.visibility = View.GONE
                        fabCancelReport.visibility = View.GONE
                        svSoundingList.visibility = View.VISIBLE
                        svFinalList.visibility = View.GONE
                        tvNoFinalDataAvailable.visibility = View.GONE
                        Handler(Looper.getMainLooper()).postDelayed({
                            lifecycleScope.launch {
                                userDao.fetchAllSounding().collect {
                                    Log.d("okimatra", "$it")
                                    val list = ArrayList(it)
                                    setupListOfDataIntoRecyclerViewSounding(list,userDao)
                                }
                            }
                        }, 10)
                    }
                    finalTab.setOnClickListener {
                        lifecycleScope.launch {
                            userDao.countAllSounding().collect { itTotalSounding ->
                                if (itTotalSounding > 0) {
                                    svSoundingList.visibility = View.GONE
                                    tvNoRawDataAvailable.visibility = View.GONE
                                    svFinalList.visibility = View.VISIBLE
                                    finalTab.background = ResourcesCompat.getDrawable(resources, R.drawable.switch_on,null)
                                    finalTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.white))
                                    rawDataTab.background = null
                                    rawDataTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.appTheme))
                                    if (fabOverSounding) {
                                        soundingContainer.visibility = View.GONE
                                        btnAddSounding.visibility = View.GONE
                                        btnSave.visibility = View.GONE
                                        lifecycleScope.launch {
                                            userDao.fetchAllReport().collect {
                                                val list = ArrayList(it)
                                                setupListOfDataIntoRecyclerViewReport(list, userDao)
                                            }
                                        }
                                        fabFinalReport.visibility = View.VISIBLE
                                    } else {
                                        fabFinalReport.visibility = View.GONE
                                        rvFinalList.visibility = View.GONE
                                        tvNoFinalDataAvailable.visibility = View.GONE
                                        fabCancelReport.visibility = View.VISIBLE
                                        soundingContainer.visibility = View.VISIBLE
                                        btnAddSounding.visibility = View.VISIBLE
                                        btnSave.visibility = View.VISIBLE
                                    }
                                    lifecycleScope.launch {
                                        userDao.fetchAllSounding().collect {
                                            populateDropdownSoundingwithEmpty(ArrayList(it), awal1, false)
                                            populateDropdownEmptyOut(akhir1)
                                        }
                                    }
                                    spinnerListener(binding2, addOrClose1, userDao)
                                } else {
                                    Toast.makeText(requireContext(), "Mohon Tambahkan Raw Data\n Terlebih Dahulu Melalui Simpan Data\n Pada Tab Calculator", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                    fabFinalReport.setOnClickListener {
                        fabOverSounding = false
                        fabFinalReport.visibility = View.GONE
                        rvFinalList.visibility = View.GONE
                        tvNoFinalDataAvailable.visibility = View.GONE
                        dataHasilLayout.visibility = View.VISIBLE
                        fabCancelReport.visibility = View.VISIBLE
                        soundingContainer.visibility = View.VISIBLE
                        btnAddSounding.visibility = View.VISIBLE
                        btnSave.visibility = View.VISIBLE
                    }
                    fabCancelReport.setOnClickListener {
                        val builder = AlertDialog.Builder(requireContext())
                        builder.setTitle("Keluar").setMessage("Apakah Anda yakin ingin membatalkan penambahan data?")
                        builder.setPositiveButton("Yes") { dialogInterface, _ ->
                            fabOverSounding = true
                            fabCancelReport.visibility = View.GONE
                            soundingContainer.visibility = View.GONE
                            btnAddSounding.visibility = View.GONE
                            btnSave.visibility = View.GONE
                            dataHasilLayout.visibility = View.GONE
                            fabFinalReport.visibility = View.VISIBLE
                            rvFinalList.visibility = View.VISIBLE
                            tvNoFinalDataAvailable.visibility = View.VISIBLE
                            dialogInterface.dismiss()
                        }
                        builder.setNegativeButton("No") { dialogInterface, _ ->
                            dialogInterface.dismiss()
                        }
                        val alertDialog: AlertDialog = builder.create()
                        alertDialog.setCancelable(false)
                        alertDialog.show()
                    }
                    tanggalBa.setOnClickListener {
                        dateSetListener = DatePickerDialog.OnDateSetListener {
                                _, year, month, dayOfMonth ->
                            cal.set(Calendar.YEAR, year)
                            cal.set(Calendar.MONTH, month)
                            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                            tanggalBa.setText(tanggalID)
                        }
                        DatePickerDialog(
                            requireContext(),
                            R.style.TimePickerTheme,
                            dateSetListener,
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
                    waktuBarcon.setOnClickListener {
                        dateSetListener = DatePickerDialog.OnDateSetListener {
                                _, year, month, dayOfMonth ->
                            cal.set(Calendar.YEAR, year)
                            cal.set(Calendar.MONTH, month)
                            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                            val mcurrentTime = Calendar.getInstance()
                            val hour = mcurrentTime[Calendar.HOUR_OF_DAY]
                            val minute = mcurrentTime[Calendar.MINUTE]
                            val mTimePicker = TimePickerDialog(
                                requireContext(),
                                R.style.TimePickerTheme,
                                { _, selectedHour, selectedMinute -> waktuBarcon.setText(String.format(getString(R.string.format_waktu, tanggalID, selectedHour, selectedMinute, timeZone))) },
                                hour,
                                minute,
                                true
                            )
                            mTimePicker.show()
                        }
                        DatePickerDialog(
                            requireContext(),
                            R.style.TimePickerTheme,
                            dateSetListener,
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
                    btnSave.setOnClickListener {
                        val produkValue = endSpaceRemover(produk.text.toString())
                        val bentukValue = endSpaceRemover(bentuk.text.toString())
                        val namaSarkutValue = endSpaceRemover(namaSarkut.text.toString())
                        val jumlahContohValue = endSpaceRemover(jumlahBarcon.text.toString())
                        val waktuAjuValue = waktuBarcon.text.toString()
                        val noBaValue = endSpaceRemover(noBa.text.toString())
                        val tanggalBaValue = tanggalBa.text.toString()
                        val lokasiBaValue = endSpaceRemover(lokasiBa.text.toString())
                        val noDokumenValue = endSpaceRemover(noDokumen.text.toString().uppercase())
                        val hasilPerhitunganValue = hasilPerhitungan.text.toString()
                        val hasilPembulatanValue = endSpaceRemover(hasilPembulatan.text.toString())
                        when {
                            produkValue.isEmpty() -> {
                                Toast.makeText(requireContext(), "Produk/Jenis Barang Masih Kosong", Toast.LENGTH_SHORT).show()
                            }
                            bentukValue.isEmpty() -> {
                                Toast.makeText(requireContext(), "Bentuk Fisik/Warna/Bau Masih Kosong", Toast.LENGTH_SHORT).show()
                            }
                            namaSarkutValue.isEmpty() -> {
                                Toast.makeText(requireContext(), "Nama Sarkut Masih Kosong", Toast.LENGTH_SHORT).show()
                            }
                            jumlahContohValue.isEmpty() -> {
                                Toast.makeText(requireContext(), "Jumlah Contoh Masih Kosong", Toast.LENGTH_SHORT).show()
                            }
                            waktuAjuValue.isEmpty() -> {
                                Toast.makeText(requireContext(), "Waktu Aju Contoh Masih Kosong", Toast.LENGTH_SHORT).show()
                            }
                            noBaValue.isEmpty() -> {
                                Toast.makeText(requireContext(), "Nomor BA Masih Kosong", Toast.LENGTH_SHORT).show()
                            }
                            noBaValue.contains("BAPFP-X") -> {
                                Toast.makeText(requireContext(), "Nomor BA Masih X", Toast.LENGTH_SHORT).show()
                            }
                            tanggalBaValue.isEmpty() -> {
                                Toast.makeText(requireContext(), "Tanggal BA Masih Kosong", Toast.LENGTH_SHORT).show()
                            }
                            lokasiBaValue.isEmpty() -> {
                                Toast.makeText(requireContext(), "Lokasi BA Masih Kosong", Toast.LENGTH_SHORT).show()
                            }
                            noDokumenValue.isEmpty() -> {
                                Toast.makeText(requireContext(), "Nomor Form 3D Masih Kosong", Toast.LENGTH_SHORT).show()
                            }
                            noDokumenValue.length < 24 -> {
                                Toast.makeText(requireContext(), "Nomor Form 3D Belum Lengkap", Toast.LENGTH_SHORT).show()
                            }
                            hasilPembulatanValue.isEmpty() -> {
                                Toast.makeText(requireContext(), "Hasil Pembulatan Masih Kosong", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                val listSounding = ArrayList<String>()
                                ivSpAwalMap.keys.forEach {
                                    listSounding += arrayListOf(ivSpAwalMap[it]!!.selectedItem.toString())
                                    listSounding += arrayListOf(ivSpAkhirMap[it]!!.selectedItem.toString()) //ID from ivSpAwal = ivSpAkhir
                                }
                                Log.d("okimatra", listSounding.toString())
                                val tinggiCairanList = MutableList(listSounding.size) {0.0}
                                val suhuCairanList = MutableList(listSounding.size) {0.0}
                                val suhuKalibrasiTangkiList = MutableList(listSounding.size) {0.0}
                                val tinggiMejaList = MutableList(listSounding.size) {0.0}
                                val faktorMuaiList = MutableList(listSounding.size) {0.0}
                                val tinggiCairanTerkoreksiList = MutableList(listSounding.size) {0.0}
                                val volumeKalibrasi1List = MutableList(listSounding.size) {0.0}
                                val densityCairanList = MutableList(listSounding.size) {0.0}
                                val volumeFraksiList = MutableList(listSounding.size) {0.0}
                                val volumeKalibrasi2List = MutableList(listSounding.size) {0.0}
                                val volumeMidList = MutableList(listSounding.size) {0.0}
                                val volumeAppList = MutableList(listSounding.size) {0.0}
                                val volumeObsList = MutableList(listSounding.size) {0.0}
                                val volumeList = MutableList(listSounding.size) {0.0}
                                val hasilSoundingList = MutableList(listSounding.size) {0.0}
                                val noTangkiList = MutableList(listSounding.size) {""}
                                val pegawaiSoundingList = MutableList(listSounding.size) {""}
                                val nipPegawaiList = MutableList(listSounding.size) {""}
                                val penggunaJasaSoundingList = MutableList(listSounding.size) {""}
                                val jabatanPenggunaJasaList = MutableList(listSounding.size) {""}
                                val perusahaanSoundingList = MutableList(listSounding.size) {""}
                                val npwpPerusahaanSoundingList = MutableList(listSounding.size) {""}
                                val alamatPerusahaanSoundingList = MutableList(listSounding.size) {""}
                                val lokasiSoundingList = MutableList(listSounding.size) {""}
                                val waktuList = MutableList(listSounding.size) {""}
                                val judulKalibrasi1List = MutableList(listSounding.size) {""}
                                val judulKalibrasi2List = MutableList(listSounding.size) {""}
                                val judulFraksiList = MutableList(listSounding.size) {""}
                                val judulDataTabelList = MutableList(listSounding.size) {""}
                                for (i in listSounding.indices) {
                                    if (listSounding[i] != "Empty In; 0" && listSounding[i] != "Empty Out; 0") {
                                        lifecycleScope.launch {
                                            userDao.fetchSoundingByNoTangkiAndWaktu(
                                                listSounding[i].subSequence(0, listSounding[i].indexOf(";")).toString(),
                                                monthExtract(
                                                    listSounding[i].subSequence(
                                                        listSounding[i].indexOf(";") + 2,
                                                        listSounding[i].length
                                                    ).toString()
                                                )
                                            ).collect {
                                                try {
                                                    Log.d("okimatra1", tinggiCairanList.toString())
                                                    tinggiCairanList[i] = it.tinggi_cairan
                                                    Log.d("okimatra2", tinggiCairanList.toString())
                                                    suhuCairanList[i] = it.suhu_cairan
                                                    suhuKalibrasiTangkiList[i] =
                                                        it.suhu_kalibrasi_tangki
                                                    tinggiMejaList[i] = it.tinggi_meja
                                                    faktorMuaiList[i] = it.faktor_muai
                                                    tinggiCairanTerkoreksiList[i] =
                                                        it.tinggi_cairan_terkoreksi
                                                    volumeKalibrasi1List[i] = it.volume_kalibrasi1
                                                    densityCairanList[i] = it.density_cairan
                                                    volumeFraksiList[i] = it.volume_fraksi
                                                    volumeKalibrasi2List[i] = it.volume_kalibrasi2
                                                    volumeMidList[i] = it.volume_mid
                                                    volumeAppList[i] = it.volume_app
                                                    volumeObsList[i] = it.volume_obs
                                                    volumeList[i] = it.volume
                                                    hasilSoundingList[i] = it.hasil_sounding
                                                    noTangkiList[i] = it.no_tangki
                                                    pegawaiSoundingList[i] = it.pegawai_sounding
                                                    nipPegawaiList[i] = it.nip_pegawai
                                                    penggunaJasaSoundingList[i] =
                                                        it.pengguna_jasa_sounding
                                                    jabatanPenggunaJasaList[i] =
                                                        it.jabatan_pengguna_jasa
                                                    perusahaanSoundingList[i] =
                                                        it.perusahaan_sounding
                                                    npwpPerusahaanSoundingList[i] =
                                                        it.npwp_perusahaan_sounding
                                                    alamatPerusahaanSoundingList[i] =
                                                        it.alamat_perusahaan_sounding
                                                    lokasiSoundingList[i] = it.lokasi_sounding
                                                    waktuList[i] = it.waktu
                                                    judulKalibrasi1List[i] = it.judulKalibrasi1
                                                    judulKalibrasi2List[i] = it.judulKalibrasi2
                                                    judulFraksiList[i] = it.judulFraksi
                                                    judulDataTabelList[i] = it.judulDataTabel
                                                } catch (e: Exception) {
                                                    Log.d("okimatra", e.message.toString())
                                                }
                                            }
                                        }
                                    }
                                    else { //To add listSounding[i] sequentially because of lazy userDao
                                        Log.d("okimatra3",tinggiCairanList.toString())
                                        tinggiCairanList[i] = 0.0
                                        Log.d("okimatra4",tinggiCairanList.toString())
                                        suhuCairanList[i] = 0.0
                                        suhuKalibrasiTangkiList[i] = 0.0
                                        tinggiMejaList[i] = 0.0
                                        faktorMuaiList[i] = 0.0
                                        tinggiCairanTerkoreksiList[i] = 0.0
                                        volumeKalibrasi1List[i] = 0.0
                                        densityCairanList[i] = 0.0
                                        volumeFraksiList[i] = 0.0
                                        volumeKalibrasi2List[i] = 0.0
                                        volumeMidList[i] = 0.0
                                        volumeAppList[i] = 0.0
                                        volumeObsList[i] = 0.0
                                        volumeList[i] = 0.0
                                        hasilSoundingList[i] = 0.0
                                        noTangkiList[i] = ""
                                        pegawaiSoundingList[i] = ""
                                        nipPegawaiList[i] = ""
                                        penggunaJasaSoundingList[i] = ""
                                        jabatanPenggunaJasaList[i] = ""
                                        perusahaanSoundingList[i] = ""
                                        npwpPerusahaanSoundingList[i] = ""
                                        alamatPerusahaanSoundingList[i] = ""
                                        lokasiSoundingList[i] = ""
                                        waktuList[i] = ""
                                        judulKalibrasi1List[i] = ""
                                        judulKalibrasi2List[i] = ""
                                        judulFraksiList[i] = ""
                                        judulDataTabelList[i] = ""
                                    }
                                }
                                Handler(Looper.getMainLooper()).postDelayed({ //Give time to load all database data
                                    Log.d("okimatra4", tinggiCairanList.toString())
                                    lifecycleScope.launch {
                                        userDao.insertReport(ReportEntity(
                                            tinggi_cairan = tinggiCairanList as ArrayList<Double>,
                                            suhu_cairan = suhuCairanList as ArrayList<Double>,
                                            suhu_kalibrasi_tangki = suhuKalibrasiTangkiList as ArrayList<Double>,
                                            tinggi_meja = tinggiMejaList as ArrayList<Double>,
                                            faktor_muai = faktorMuaiList as ArrayList<Double>,
                                            tinggi_cairan_terkoreksi = tinggiCairanTerkoreksiList as ArrayList<Double>,
                                            volume_kalibrasi1 = volumeKalibrasi1List as ArrayList<Double>,
                                            density_cairan = densityCairanList as ArrayList<Double>,
                                            volume_fraksi = volumeFraksiList as ArrayList<Double>,
                                            volume_kalibrasi2 = volumeKalibrasi2List as ArrayList<Double>,
                                            volume_mid = volumeMidList as ArrayList<Double>,
                                            volume_app = volumeAppList as ArrayList<Double>,
                                            volume_obs = volumeObsList as ArrayList<Double>,
                                            volume = volumeList as ArrayList<Double>,
                                            hasil_sounding = hasilSoundingList as ArrayList<Double>,
                                            no_tangki = noTangkiList as ArrayList<String>,
                                            pegawai_sounding = pegawaiSoundingList as ArrayList<String>,
                                            nip_pegawai = nipPegawaiList as ArrayList<String>,
                                            pengguna_jasa_sounding = penggunaJasaSoundingList as ArrayList<String>,
                                            jabatan_pengguna_jasa = jabatanPenggunaJasaList as ArrayList<String>,
                                            perusahaan_sounding = perusahaanSoundingList as ArrayList<String>,
                                            npwp_perusahaan_sounding = npwpPerusahaanSoundingList as ArrayList<String>,
                                            alamat_perusahaan_sounding = alamatPerusahaanSoundingList as ArrayList<String>,
                                            lokasi_sounding = lokasiSoundingList as ArrayList<String>,
                                            waktu = waktuList as ArrayList<String>,
                                            nomor_dokumen = noDokumenValue,
                                            produk = produkValue,
                                            bentuk = bentukValue,
                                            waktu_date = Date().time,
                                            judulKalibrasi1 = judulKalibrasi1List as ArrayList<String>,
                                            judulKalibrasi2 = judulKalibrasi2List as ArrayList<String>,
                                            judulFraksi = judulFraksiList as ArrayList<String>,
                                            judulDataTabel = judulDataTabelList as ArrayList<String>,
                                            nama_sarkut = namaSarkutValue,
                                            tanggal_ba = tanggalBaValue,
                                            lokasi_ba = lokasiBaValue,
                                            jumlah_contoh = jumlahContohValue,
                                            waktu_aju = waktuAjuValue,
                                            hasil_pembulatan = hasilPembulatanValue,
                                            hasil_perhitungan = hasilPerhitunganValue,
                                            nomor_ba = noBaValue
                                        ))

                                        fabOverSounding = true
                                        soundingContainer.visibility = View.GONE
                                        btnAddSounding.visibility = View.GONE
                                        btnSave.visibility = View.GONE
                                        dataHasilLayout.visibility = View.GONE
                                        fabCancelReport.visibility = View.GONE
                                        fabFinalReport.visibility = View.VISIBLE
                                        lifecycleScope.launch {
                                            userDao.fetchAllReport().collect {
                                                val list = ArrayList(it)
                                                setupListOfDataIntoRecyclerViewReport(list, userDao)
                                            }
                                        }
                                        _binding2?.produk?.text?.clear()
                                        _binding2?.bentuk?.text?.clear()
                                        _binding2?.namaSarkut?.text?.clear()
                                        _binding2?.hasilPembulatan?.text?.clear()
                                        noDokumen.setText(String.format(getString(R.string.no_dokumen_edited),currentyear))
                                        noBa.setText(String.format(getString(R.string.no_ba_edited),currentyear))
                                        //Remove added input sounding
                                        ivTvMap.keys.forEach {
                                            if (it != addOrClose1) {
                                                (ivCvMap[it]?.parent as ViewGroup).removeView(ivCvMap[it])
                                                ivCvMap.remove(it)
                                                ivSpAwalMap.remove(it)
                                                ivSpAkhirMap.remove(it)
                                                ivTvMap.remove(it)
                                            }
                                        }
                                    }
                                }, 150) //wait on loading
                            }
                        }
                    }

                    titleSounding1.text = String.format(getString(R.string.data_sounding), 1)
                    addClickListener(binding2, btnAddSounding, userDao)
                    spAwalDbFinalMap = mutableMapOf(awal1 to 0.0)
                    spAkhirDbFinalMap = mutableMapOf(akhir1 to 0.0)
                    hasilPerhitungan.setText(zeroRemover("${roundDigits(soundingAwalTotal - soundingAkhirlTotal)}"))
                    ivCvMap = mutableMapOf(addOrClose1 to soundingCardView1)
                    ivTvMap = mutableMapOf(addOrClose1 to titleSounding1)
                    ivSpAwalMap = mutableMapOf(addOrClose1 to awal1)
                    ivSpAkhirMap = mutableMapOf(addOrClose1 to akhir1)
                }
            }
            else -> {
                lifecycleScope.launch {
                    userDao.fetchAllUser().collect {
                        val list = ArrayList(it)
                        setupListOfUserDataIntoRecyclerView(list,userDao)
                    }
                }

                binding3.apply {

                    var number = ""
                    var numberOld = ""
                    var textOld = ""
                    val holder = "__.___.___._-___.___"
                    var cursorPosition: Int
                    var cursor: Int

                    lifecycleScope.launch {
                        userDao.fetchAllKantor().collect {
                            populateDropdownKantor(ArrayList(it), kantorPegawai)
                        }
                    }

                    etNPWPId.setOnFocusChangeListener { _, _ ->
                        if (etNPWPId.text.toString().isEmpty()) {
                            etNPWPId.setText(getString(R.string.before_edited))
                        }
                        if (etNPWPId.selectionStart > number.length) {
                            etNPWPId.setSelection(number.length)
                        }
                    }

                    nama.setOnFocusChangeListener { _, _ ->
                        if (etNPWPId.text.toString().isEmpty() || etNPWPId.text.toString() == getString(R.string.before_edited)) {
                            etNPWPId.setText("")
                        }
                    }

                    etAlamatId.setOnFocusChangeListener { _, _ ->
                        if (etNPWPId.text.toString().isEmpty() || etNPWPId.text.toString() == getString(R.string.before_edited)) {
                            etNPWPId.setText("")
                        }
                    }

                    etNPWPId.addTextChangedListener(object : TextWatcher {
                        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                            number = s.toString().replace("_","").replace(".","").replace("-","")
                            cursorPosition = etNPWPId.selectionStart

                            if (cursorPosition > number.length) {
                                etNPWPId.setSelection(number.length)
                            }

                            if ((numberOld != number || etNPWPId.text.toString().length != 20) && etNPWPId.hasFocus()) {
                                numberOld = number
                                cursor = numberOld.length
                                when (numberOld.length) {
                                    15 -> {
                                        etNPWPId.setText(String.format(getString(R.string.number_15),numberOld.substring(0,2),numberOld.substring(2,5),numberOld.substring(5,8),numberOld.substring(8,9),numberOld.substring(9,12),numberOld.substring(12)))
                                        cursor += 5
                                    }
                                    in 13..14 -> {
                                        etNPWPId.setText(String.format(getString(R.string.number_12),numberOld.substring(0,2),numberOld.substring(2,5),numberOld.substring(5,8),numberOld.substring(8,9),numberOld.substring(9,12),numberOld.substring(12),holder.substring(numberOld.length+5)))
                                        cursor += 5
                                    }
                                    in 10..12 -> {
                                        etNPWPId.setText(String.format(getString(R.string.number_9),numberOld.substring(0,2),numberOld.substring(2,5),numberOld.substring(5,8),numberOld.substring(8,9),numberOld.substring(9),holder.substring(numberOld.length+4)))
                                        cursor += 4
                                    }
                                    9 -> {
                                        etNPWPId.setText(String.format(getString(R.string.number_9_exact),numberOld.substring(0,2),numberOld.substring(2,5),numberOld.substring(5,8),numberOld.substring(8,9),holder.substring(numberOld.length+3)))
                                        cursor += 3
                                    }
                                    in 6..8 -> {
                                        etNPWPId.setText(String.format(getString(R.string.number_5),numberOld.substring(0,2),numberOld.substring(2,5),numberOld.substring(5),holder.substring(numberOld.length+2)))
                                        cursor += 2
                                    }
                                    in 3..5 -> {
                                        etNPWPId.setText(String.format(getString(R.string.number_2),numberOld.substring(0,2),numberOld.substring(2),holder.substring(numberOld.length+1)))
                                        cursor += 1
                                    }
                                    in 1..2 -> {
                                        etNPWPId.setText(String.format(getString(R.string.number_0),numberOld.substring(0),holder.substring(numberOld.length)))
                                    }
                                    0 -> {
                                        etNPWPId.setText(holder)
                                    }
                                    else -> {
                                        etNPWPId.setText(textOld)
                                    }
                                }
                                etNPWPId.post {
                                    etNPWPId.setSelection(cursor)
                                }
                            }
                        }

                        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                            textOld = s.toString()
                        }

                        override fun afterTextChanged(s: Editable) {

                        }
                    })

                    etNPWPId.setOnClickListener {
                        if (etNPWPId.selectionStart > number.length) {
                            etNPWPId.setSelection(number.length)
                        }
                    }

                    penggunaJasaTab.setOnClickListener {
                        lifecycleScope.launch {
                            userDao.countAllCompany().collect { it1 ->
                                if (it1>0) {
                                    penggunaJasaTab.background = ResourcesCompat.getDrawable(resources, R.drawable.switch_on,null)
                                    penggunaJasaTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.white))
                                    pegawaiTab.background = null
                                    pegawaiTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.appTheme))
                                    perusahaanTab.background = null
                                    perusahaanTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.appTheme))
                                    pegawaiLayout.visibility = View.GONE //NIP
                                    btnAddUser.visibility = View.GONE
                                    npwpLayout.visibility = View.GONE
                                    alamatLayout.visibility = View.GONE
                                    kantorLayout.visibility = View.GONE
                                    btnAddCompany.visibility = View.GONE
                                    penggunajasaLayout.visibility = View.VISIBLE //Jabatan
                                    btnAddPenggunaJasa.visibility = View.VISIBLE
                                    perusahaanLayout.visibility = View.VISIBLE //Perusahaan
                                    nama.hint = getText(R.string.hint_pengguna_jasa)
                                    svCompanyList.visibility = View.GONE
                                    lifecycleScope.launch {
                                        userDao.fetchAllServiceUser().collect {
                                            val list = ArrayList(it)
                                            setupListOfServiceUserDataIntoRecyclerView(list, userDao)
                                        }
                                    }
                                    lifecycleScope.launch {
                                        userDao.fetchAllCompany().collect {
                                            populateDropdownCompany(ArrayList(it), perusahaan)
                                        }
                                    }
                                }
                                else {
                                    Toast.makeText(context, "Mohon Tambahkan Data\nPerusahaanTerlebih Dahulu\nPada Tab Company", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }

                    pegawaiTab.setOnClickListener {
                        pegawaiTab.background = ResourcesCompat.getDrawable(resources, R.drawable.switch_on,null)
                        pegawaiTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.white))
                        penggunaJasaTab.background = null
                        penggunaJasaTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.appTheme))
                        perusahaanTab.background = null
                        perusahaanTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.appTheme))
                        npwpLayout.visibility = View.GONE
                        alamatLayout.visibility = View.GONE
                        btnAddCompany.visibility = View.GONE
                        penggunajasaLayout.visibility = View.GONE
                        btnAddPenggunaJasa.visibility = View.GONE
                        btnAddUser.visibility = View.VISIBLE
                        pegawaiLayout.visibility = View.VISIBLE
                        kantorLayout.visibility = View.VISIBLE
                        nama.hint = getText(R.string.hint_nama)
                        svUserList.visibility = View.VISIBLE
                        svServiceUserList.visibility = View.GONE
                        perusahaanLayout.visibility = View.GONE
                        svCompanyList.visibility = View.GONE
                    }

                    perusahaanTab.setOnClickListener {
                        perusahaanTab.background = ResourcesCompat.getDrawable(resources, R.drawable.switch_on,null)
                        perusahaanTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.white))
                        penggunaJasaTab.background = null
                        penggunaJasaTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.appTheme))
                        pegawaiTab.background = null
                        pegawaiTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.appTheme))
                        npwpLayout.visibility = View.VISIBLE
                        alamatLayout.visibility = View.VISIBLE
                        btnAddCompany.visibility = View.VISIBLE
                        penggunajasaLayout.visibility = View.GONE
                        btnAddPenggunaJasa.visibility = View.GONE
                        btnAddUser.visibility = View.GONE
                        pegawaiLayout.visibility = View.GONE
                        svServiceUserList.visibility = View.GONE
                        perusahaanLayout.visibility = View.GONE
                        kantorLayout.visibility = View.GONE
                        nama.hint = getText(R.string.hint_perusahaan)
                        svUserList.visibility = View.GONE
                        lifecycleScope.launch {
                            userDao.fetchAllCompany().collect {
                                val list = ArrayList(it)
                                setupListOfDataIntoRecyclerViewCompany(list, userDao)
                            }
                        }
                        svCompanyList.visibility = View.VISIBLE
                    }

                    btnAddCompany.setOnClickListener {
                        addRecordCompany(userDao)
                    }

                    btnAddUser.setOnClickListener {
                        addRecordUser(userDao)
                    }

                    btnAddPenggunaJasa.setOnClickListener {
                        addRecordServiceUser(userDao)
                    }
                }
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding1 = null
        _binding2 = null
        _binding3 = null
    }

    private fun pdfReportSounding(id: Int, userDao: UserDao) {
        lifecycleScope.launch {
            userDao.fetchReportById(id).collect {
                try {
                    Dexter.withContext(requireContext())
                        .withPermissions(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ).withListener(object : MultiplePermissionsListener {
                            override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                                if (report.areAllPermissionsGranted()) {
                                    val sdf = SimpleDateFormat(" ddMMyy hhmmss", Locale.getDefault())
                                    val currentDate = sdf.format(Calendar.getInstance().time)
                                    val doc = Document(PageSize.A4, 0f, 0f, 0f, 0f)
                                    val outPath = requireContext().getExternalFilesDir(null).toString() + "/Report " + it.no_tangki + currentDate + ".pdf"  //location where the pdf will store
                                    Log.d("loc", outPath)
                                    val writer = PdfWriter.getInstance(doc, FileOutputStream(outPath))
                                    doc.open()
                                    doc.setMargins(0f, 0f, 40f, 40f)
                                    headerFinalReport(doc, writer)
//                                    bodyFinalReport(doc, it)
                                    doc.close()

                                    val file = File(outPath)
                                    file.listFiles()
                                    val path: Uri =FileProvider.getUriForFile(Objects.requireNonNull(activity!!.applicationContext),BuildConfig.APPLICATION_ID + ".provider", file)
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW)
                                        intent.setDataAndType(path, "application/pdf")
                                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        startActivity(intent)
                                    } catch (e: ActivityNotFoundException) {
                                        Toast.makeText(requireContext(), "Tidak Ditemukan Aplikasi PDF Viewer", Toast.LENGTH_SHORT).show()
                                    }


                                } else {
                                    Toast.makeText(requireContext(), "Akses Tidak Diberikan\n(Permission Not Granted)", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onPermissionRationaleShouldBeShown(
                                permissions: List<PermissionRequest>,
                                token: PermissionToken
                            ) {
                                token.continuePermissionRequest()
                            }
                        }).check()
                } catch (e: Exception) {
                    Log.d("okimatra", "" + e.message)
                }
            }
        }
    }
    private fun headerFinalReport(doc: Document, writer: PdfWriter) {
        doc.add(Paragraph("\n\n", fontArialRegular))
        val table1 = PdfPTable(1)
        table1.setWidths(floatArrayOf(1f))
        table1.isLockedWidth = true
        table1.totalWidth = PageSize.A4.width-80f

        val header = Paragraph("KEMENTERIAN KEUANGAN REPUBLIK INDONESIA\n", fontArialRegular)
        header.add(Paragraph("DIREKTORAT JENDERAL BEA DAN CUKAI", fontArialRegular))
        header.alignment = Element.ALIGN_LEFT
        val tittleCell = PdfPCell(header)
        tittleCell.horizontalAlignment = Element.ALIGN_LEFT
        tittleCell.verticalAlignment = Element.ALIGN_CENTER
        tittleCell.paddingBottom = 10f
        tittleCell.paddingTop = 10f
        tittleCell.paddingLeft = 10f
        tittleCell.paddingRight = 10f
        table1.addCell(tittleCell)
        doc.add(table1)
    }
    private fun headerRawReport(doc: Document, writer: PdfWriter) {
        val tableBotPadding = 10f
        val tableHorizontalPadding = 20f
        val tableTopPadding = 20f

        val headerTable = PdfPTable(2)
        headerTable.setWidths(floatArrayOf(1f, 3.5f))
        headerTable.isLockedWidth = true
        headerTable.totalWidth = PageSize.A4.width

        val logoBC = ResourcesCompat.getDrawable(resources, R.drawable.logo_bc, null)
        val bitDwLogoBC = logoBC as BitmapDrawable
        val bmpLogoBC = bitDwLogoBC.bitmap
        val streamLogoBC = ByteArrayOutputStream()
        bmpLogoBC.compress(Bitmap.CompressFormat.PNG, 100, streamLogoBC)
        val imageLogoBC = Image.getInstance(streamLogoBC.toByteArray())
        val scalerLogoBC: Float = (doc.pageSize.width - doc.leftMargin() - doc.rightMargin()) / imageLogoBC.width * 10
        imageLogoBC.scalePercent(scalerLogoBC)

        val cellLogoBC = PdfPCell(Image.getInstance(imageLogoBC))
        cellLogoBC.border = Rectangle.NO_BORDER
        cellLogoBC.horizontalAlignment = Rectangle.ALIGN_RIGHT
        cellLogoBC.verticalAlignment = Rectangle.ALIGN_CENTER
        cellLogoBC.paddingTop = tableTopPadding
        cellLogoBC.paddingBottom = tableBotPadding
        cellLogoBC.paddingRight = tableHorizontalPadding
        headerTable.addCell(cellLogoBC)

        val para = Paragraph("LAPORAN HITUNG BARANG CURAH BEA CUKAI", appFontBig)
        para.alignment = Element.ALIGN_MIDDLE
        val tittleCell = PdfPCell(para)
        tittleCell.border = Rectangle.NO_BORDER
        tittleCell.horizontalAlignment = Element.ALIGN_LEFT
        tittleCell.verticalAlignment = Element.ALIGN_MIDDLE
        tittleCell.paddingTop = tableTopPadding-3f
        tittleCell.paddingBottom = tableBotPadding
        tittleCell.paddingRight = tableHorizontalPadding
        headerTable.addCell(tittleCell)

        doc.add(headerTable)

        val colorPrimary = BaseColor(0, 0, 0)
        val canvas: PdfContentByte = writer.directContent
        canvas.setColorStroke(colorPrimary)
        canvas.moveTo(0.0, 755.0)
        // Drawing the line
        canvas.lineTo(PageSize.A4.width.toDouble(), 755.0)
        canvas.setLineWidth(1.5f)
        canvas.closePathStroke()
    }
    private fun bodyFinalReport(doc: Document, it: SoundingEntity) {
//        idTable.widthPercentage = 100f
//        idTable.tableEvent = BorderEvent()
//        idTable.deleteBodyRows()
//        val pID = Paragraph()
//        pID.add(idTable)
//        pID.indentationLeft = horizontalPadding
//        doc.add(pID)
        doc.add(Paragraph("\n\n\n\n\n", appFontTiny))
        val metodeFraksi = it.volume_fraksi > 0
        writeDataTitle("Data Umum", doc)
        val judulUmum = listOf("Nama Perusahaan", "Alamat Perusahaan", "Nomor Tangki", "Waktu Sounding", "Lokasi Sounding")
        val nilaiUmum = listOf(
            it.perusahaan_sounding,
            it.alamat_perusahaan_sounding,
            it.no_tangki,
            (it.waktu.subSequence(0, it.waktu.indexOf(":")-3).toString()+" Pukul${it.waktu.subSequence(it.waktu.indexOf(":")-3, it.waktu.length)}").replace("-"," "),
//            it.waktu.subSequence(0,it.waktu.indexOf(":")-3).toString().replace("-"," "),
//            it.waktu.subSequence(it.waktu.indexOf(":")-2, it.waktu.length).toString(),
            it.lokasi_sounding,
        )
        writeDatawithSemicolomn(judulUmum, nilaiUmum, doc)

        writeDataTitle("Data Lapangan", doc)
        val judulLapangan = listOf("Tinggi Cairan", "Suhu Cairan")
        val nilaiLapangan = listOf(
            "${zeroRemover((it.tinggi_cairan/1000).toBigDecimal().toPlainString()).replace(".",",")} m",
            "${zeroRemover(it.suhu_cairan.toBigDecimal().toPlainString()).replace(".", ",")} °C"
        )
        writeDatawithSemicolomn(judulLapangan, nilaiLapangan, doc)

        writeDataTitle("Data Tangki", doc)
        val judulTangki = listOf("Suhu Kalibrasi Tangki", "Tinggi Meja", "Koefisien Muai Tangki")
        val nilaiTangki = listOf(
            "${zeroRemover(it.suhu_kalibrasi_tangki.toBigDecimal().toPlainString()).replace(".",",")} °C",
            "${zeroRemover(it.tinggi_meja.toBigDecimal().toPlainString()).replace(".",",")} mm",
            zeroRemover(it.faktor_muai.toBigDecimal().toPlainString()).replace(".",",")
        )
        writeDatawithSemicolomn(judulTangki, nilaiTangki, doc)

        writeDataTitle("Data Tabel", doc)
        val judulTabel: List<String>
        val nilaiTabel: List<String>
        if (metodeFraksi) {
            judulTabel = listOf(it.judulKalibrasi1, it.judulFraksi, "Massa Jenis Cairan")
            nilaiTabel = listOf(
                "${zeroRemover(it.volume_kalibrasi1.toBigDecimal().toPlainString()).replace(".",",")} L",
                "${zeroRemover(it.volume_fraksi.toBigDecimal().toPlainString()).replace(".", ",")} L",
                "${zeroRemover(it.density_cairan.toBigDecimal().toPlainString()).replace(".",",")} MT/KL"
            )
        } else {
            judulTabel = listOf(it.judulKalibrasi1,"Tabel Kalibrasi (${it.judulDataTabel})", it.judulKalibrasi2, "Massa Jenis Produk")
            nilaiTabel = listOf(
                "${zeroRemover(it.volume_kalibrasi1.toBigDecimal().toPlainString()).replace(".",",")} L",
                "${zeroRemover(it.volume_mid.toBigDecimal().toPlainString()).replace(".",",")} L",
                "${zeroRemover(it.volume_kalibrasi2.toBigDecimal().toPlainString()).replace(".", ",")} L",
                "${zeroRemover(it.density_cairan.toBigDecimal().toPlainString()).replace(".",",")} MT/KL"
            )
        }
        writeDatawithSemicolomn(judulTabel, nilaiTabel, doc)

        val metode = if (metodeFraksi) "Metode Fraksi" else "Metode Interpolasi"
        writeDataTitle("Hasil Perhitungan $metode", doc)
        val calcData = listOf("Tinggi Terkoreksi", "Volume App", "Volume Obs", "Volume", "Hasil Akhir Muatan")
        val calcValue = listOf(
            "${zeroRemover(it.tinggi_cairan_terkoreksi.toBigDecimal().toPlainString()).replace(".",",")} m",
            "${zeroRemover(it.volume_app.toBigDecimal().toPlainString()).replace(".", ",")} L",
            "${zeroRemover(it.volume_obs.toBigDecimal().toPlainString()).replace(".",",")} L",
            "${zeroRemover(it.volume.toBigDecimal().toPlainString()).replace(".",",")} KL",
            "${zeroRemover(it.hasil_sounding.toBigDecimal().toPlainString()).replace(".",",")} MT"
        )
        writeDatawithSemicolomn(calcData, calcValue, doc)
        doc.add(Paragraph("\n", appFontMiddle))

//        val ttdValue = listOf("Disusun oleh,", "Pemeriksa Bea Cukai", "\n\n")
//        writeAuthentication(ttdValue, doc)
//        val nama = Chunk(it.pegawai_sounding, regularFont)
//        nama.setUnderline(0.5f, -2f)
//        val para = Paragraph(nama)
//        para.indentationLeft = 380f
//        doc.add(para)

        val ttdPenggunaJasa = listOf("Mengetahui,", "Eksportir", "\n\n\n")
        val ttdPegawai = listOf("Disusun oleh,", "Pemeriksa Bea Cukai", "\n\n\n")
        writeAuthenticationwithCustomer(ttdPenggunaJasa, ttdPegawai, doc)

        val table = PdfPTable(2)
        table.setWidths(floatArrayOf(1f, 1f))
        table.isLockedWidth = true
        table.totalWidth = PageSize.A4.width
        val namaPJ = Chunk(it.pengguna_jasa_sounding, regularFont)
        namaPJ.setUnderline(0.5f, -2f)
        val pjCell = PdfPCell(Phrase(namaPJ))
        pjCell.border = Rectangle.NO_BORDER
        pjCell.horizontalAlignment = Element.ALIGN_LEFT
        pjCell.verticalAlignment = Element.ALIGN_TOP
        pjCell.paddingLeft = 57f
        table.addCell(pjCell)
        val namaPeg = Chunk(it.pegawai_sounding, regularFont)
        namaPeg.setUnderline(0.5f, -2f)
        val pegCell = PdfPCell(Phrase(namaPeg))
        pegCell.border = Rectangle.NO_BORDER
        pegCell.horizontalAlignment = Element.ALIGN_LEFT
        pegCell.verticalAlignment = Element.ALIGN_TOP
        pegCell.paddingLeft = 72f
        table.addCell(pegCell)
        doc.add(table)
        table.deleteBodyRows()

        val nipSpace = it.nip_pegawai.subSequence(0,8).toString() +
                " " + it.nip_pegawai.subSequence(8,14).toString() +
                " " + it.nip_pegawai.subSequence(14,15).toString() +
                " " + it.nip_pegawai.subSequence(15,it.nip_pegawai.length).toString()
        val nip = listOf(nipSpace)
        val jabatan = listOf(it.jabatan_pengguna_jasa)
        writeAuthenticationwithCustomer(jabatan, nip, doc)
    }
    private fun setupListOfDataIntoRecyclerViewSounding(soundingList:ArrayList<SoundingEntity>, userDao: UserDao) {
        if (soundingList.isNotEmpty()) {
            val soundingAdapter = SoundingAdapter(soundingList,{updateId->updateRecordDialogSounding(updateId,userDao)},{deleteId->deleteRecordAlertDialogSounding(deleteId,userDao)},{pdfId->pdfSounding(pdfId,userDao)})
            _binding2?.rvSoundingList?.layoutManager = LinearLayoutManager(context)
            _binding2?.rvSoundingList?.adapter = soundingAdapter
            _binding2?.svSoundingList?.visibility = View.VISIBLE
            _binding2?.tvNoRawDataAvailable?.visibility = View.GONE
        } else {
            _binding2?.svSoundingList?.visibility = View.GONE
            _binding2?.tvNoRawDataAvailable?.visibility = View.VISIBLE
        }
    }

    private fun addClickListener(binding: FragmentTwoBinding, btn: Button, userDao: UserDao) {
        val radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20f, requireContext().resources.displayMetrics)
        val elevation = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5f, requireContext().resources.displayMetrics)
        binding.apply {
            btn.setOnClickListener {
                counterSounding += 1
                val cv = CardView(requireContext())
                cv.id = View.generateViewId()
                cv.radius = radius
                cv.elevation = elevation
                cv.layoutParams = soundingCardView1.layoutParams

                val ll = LinearLayout(requireContext())
                ll.layoutParams = soundingLL.layoutParams
                ll.orientation = soundingLL.orientation

                val llTitle = LinearLayout(requireContext())
                llTitle.layoutParams = soundingLLTitle.layoutParams
                llTitle.orientation = soundingLLTitle.orientation

                val tvTitleLeft = TextView(requireContext())
                tvTitleLeft.layoutParams = titleLeft.layoutParams

                val tvTitle = TextView(requireContext())
                tvTitle.layoutParams = titleSounding1.layoutParams
                tvTitle.gravity = titleSounding1.gravity
                tvTitle.text = String.format(getString(R.string.data_sounding), counterSounding)
                tvTitle.textSize = 19f
                tvTitle.typeface = titleSounding1.typeface

                val ivTitle = ImageView(requireContext())
                ivTitle.id = View.generateViewId()
                ivTitle.layoutParams = addOrClose1.layoutParams
                ivTitle.contentDescription = addOrClose1.contentDescription
                ivTitle.elevation = addOrClose1.elevation
                ivTitle.setImageResource(R.drawable.ic_close)

                val tvAwal = TextView(requireContext())
                tvAwal.text = getString(R.string.awal)
                tvAwal.textSize = 14f
                tvAwal.layoutParams = soundingAwal.layoutParams
                tvAwal.typeface = ResourcesCompat.getFont(requireContext(), R.font.helvetica)

                val rlETAwal = RelativeLayout(requireContext())
                rlETAwal.layoutParams = soundingRL.layoutParams
                rlETAwal.background = ResourcesCompat.getDrawable(resources, R.drawable.rounded_input, null)

                val spAwal = Spinner(requireContext())
                spAwal.layoutParams = awal1.layoutParams
                spAwal.background = ResourcesCompat.getDrawable(resources, R.color.Transparent, null)

                val tvAkhir = TextView(requireContext())
                tvAkhir.text = getString(R.string.akhir)
                tvAkhir.textSize = 14f
                tvAkhir.layoutParams = soundingAwal.layoutParams
                tvAkhir.typeface = ResourcesCompat.getFont(requireContext(), R.font.helvetica)

                val rlETAkhir = RelativeLayout(requireContext())
                rlETAkhir.layoutParams = soundingRL.layoutParams
                rlETAkhir.background = ResourcesCompat.getDrawable(resources, R.drawable.rounded_input, null)

                val spAkhir = Spinner(requireContext())
                spAkhir.layoutParams = akhir1.layoutParams
                spAkhir.background = ResourcesCompat.getDrawable(resources, R.color.Transparent, null)

                llTitle.addView(tvTitleLeft)
                llTitle.addView(tvTitle)
                llTitle.addView(ivTitle)
                ll.addView(llTitle)
                ll.addView(tvAwal)
                rlETAwal.addView(spAwal)
                ll.addView(rlETAwal)
                ll.addView(tvAkhir)
                rlETAkhir.addView(spAkhir)
                ll.addView(rlETAkhir)
                cv.addView(ll)
                soundingContainer.addView(cv)
//
//                try {
//                    val f: Field = Spinner::class.java.getDeclaredField("mCursorDrawableRes")
//                    f.isAccessible = true
//                    f.set(spAwal, R.drawable.cursor_color)
//                    f.set(spAkhir, R.drawable.cursor_color)
//                } catch (ignored: java.lang.Exception) {
//                }

                lifecycleScope.launch {
                    userDao.fetchAllSounding().collect {
                        populateDropdownSoundingwithEmpty(ArrayList(it), spAwal, false)
                        populateDropdownEmptyOut(spAkhir)
                    }
                }
                ivCvMap += mutableMapOf(ivTitle to cv)
                removeListener(binding, ivTitle)
                ivTvMap += mutableMapOf(ivTitle to tvTitle)
                ivSpAwalMap += mutableMapOf(ivTitle to spAwal)
                ivSpAkhirMap += mutableMapOf(ivTitle to spAkhir)
                spinnerListener(binding, ivTitle, userDao) //When clicked will populate spAkhir
            }
        }
    }
    private fun removeListener(binding: FragmentTwoBinding, iv: ImageView) {
        var i = 1
        binding.apply {
            iv.setOnClickListener {
                counterSounding -= 1
                (ivCvMap[iv]?.parent as ViewGroup).removeView(ivCvMap[iv]) //Only ViewGroup can have removeView, ViewParent can't
                ivCvMap.remove(iv)
                ivTvMap.remove(iv)
                ivSpAwalMap.remove(iv)
                ivSpAkhirMap.remove(iv)
                ivTvMap.keys.forEach {
                    ivTvMap[it]!!.text = String.format(getString(R.string.data_sounding), i)
                    i++
                }
                Toast.makeText(requireContext(), "Tab Deleted", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun spinnerListener(binding: FragmentTwoBinding, iv: ImageView, userDao: UserDao) {
        val spAwal = ivSpAwalMap[iv]!!
        val spAkhir = ivSpAkhirMap[iv]!!
        binding.apply {
            spAwal.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val soundingAwal = spAwal.selectedItem.toString()
                    lifecycleScope.launch {
                        userDao.fetchSoundingByNoTangkiNotWaktu(
                            soundingAwal.subSequence(0, soundingAwal.indexOf(";")).toString(),
                            monthExtract(
                                soundingAwal.subSequence(
                                    soundingAwal.indexOf(";") + 2,
                                    soundingAwal.length
                                ).toString()
                            )
                        ).collect {
                            try {
                                populateDropdownSoundingwithEmpty(ArrayList(it), spAkhir, true)
                            } catch (e: Exception) {
                                Log.d("okimatra", e.message.toString())
                            }
                        }
                    }
                    if (soundingAwal != "Empty In; 0") {
                        lifecycleScope.launch {
                            userDao.fetchSoundingByNoTangkiAndWaktu(
                                soundingAwal.subSequence(0, soundingAwal.indexOf(";")).toString(),
                                monthExtract(
                                    soundingAwal.subSequence(
                                        soundingAwal.indexOf(";") + 2,
                                        soundingAwal.length
                                    ).toString()
                                )
                            ).collect { it1 ->
                                try {
                                    spAwalDbFinalMap[spAwal] = it1.hasil_sounding
                                    soundingAwalTotal = 0.0
                                    spAwalDbFinalMap.keys.forEach { it2 ->
                                        soundingAwalTotal += spAwalDbFinalMap[it2]!!
                                    }
                                    hasilPerhitungan.setText(
                                        zeroRemover(
                                            "${
                                                roundDigits(
                                                    soundingAwalTotal - soundingAkhirlTotal
                                                )
                                            }"
                                        )
                                    )
                                } catch (e: Exception) {
                                    Log.d("okimatra", e.message.toString())
                                }
                            }
                        }
                    } else {
                        spAwalDbFinalMap[spAwal] = 0.0
                        soundingAwalTotal = 0.0
                        spAwalDbFinalMap.keys.forEach {it2 ->
                            soundingAwalTotal += spAwalDbFinalMap[it2]!!
                        }
                        hasilPerhitungan.setText(zeroRemover("${roundDigits(soundingAwalTotal - soundingAkhirlTotal)}"))
                    }
                }
            }

            spAkhir.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val soundingAkhir = spAkhir.selectedItem.toString()
                    if (soundingAkhir != "Empty Out; 0") {
                        lifecycleScope.launch {
                            userDao.fetchSoundingByNoTangkiAndWaktu(
                                soundingAkhir.subSequence(0, soundingAkhir.indexOf(";")).toString(),
                                monthExtract(
                                    soundingAkhir.subSequence(
                                        soundingAkhir.indexOf(";") + 2,
                                        soundingAkhir.length
                                    ).toString()
                                )
                            ).collect { it1 ->
                                try {
                                    spAkhirDbFinalMap[spAkhir] = it1.hasil_sounding
                                    soundingAkhirlTotal = 0.0
                                    spAkhirDbFinalMap.keys.forEach { it2 ->
                                        soundingAkhirlTotal += spAkhirDbFinalMap[it2]!!
                                    }
                                    hasilPerhitungan.setText(
                                        zeroRemover(
                                            "${
                                                roundDigits(
                                                    soundingAwalTotal - soundingAkhirlTotal
                                                )
                                            }"
                                        )
                                    )
                                } catch (e: Exception) {
                                    Log.d("okimatra", e.message.toString())
                                }
                            }
                        }
                    } else {
                        spAkhirDbFinalMap[spAkhir] = 0.0
                        soundingAkhirlTotal = 0.0
                        spAkhirDbFinalMap.keys.forEach { it2 ->
                            soundingAkhirlTotal += spAkhirDbFinalMap[it2]!!
                        }
                        hasilPerhitungan.setText(zeroRemover("${roundDigits(soundingAwalTotal - soundingAkhirlTotal)}"))
                    }
                }
            }
        }
    }

    private fun pdfSounding(id: Int, userDao: UserDao) {
        lifecycleScope.launch {
            userDao.fetchSoundingById(id).collect {
                try {
                    Dexter.withContext(requireContext())
                        .withPermissions(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ).withListener(object : MultiplePermissionsListener {
                            override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                                if (report.areAllPermissionsGranted()) {
                                    val sdf = SimpleDateFormat(" ddMMyy hhmmss", Locale.getDefault())
                                    val currentDate = sdf.format(Calendar.getInstance().time)
                                    val doc = Document(PageSize.A4, 0f, 0f, 0f, 0f)
                                    val outPath = requireContext().getExternalFilesDir(null).toString() + "/Report " + it.no_tangki + currentDate + ".pdf"  //location where the pdf will store
                                    Log.d("loc", outPath)
                                    val writer = PdfWriter.getInstance(doc, FileOutputStream(outPath))
                                    doc.open()
                                    doc.setMargins(0f, 0f, 40f, 40f)
                                    headerRawReport(doc, writer)
                                    bodyRawReport(doc, it)
                                    doc.close()

                                    val file = File(outPath)
                                    file.listFiles()
                                    val path: Uri =FileProvider.getUriForFile(Objects.requireNonNull(activity!!.applicationContext),BuildConfig.APPLICATION_ID + ".provider", file)
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW)
                                        intent.setDataAndType(path, "application/pdf")
                                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        startActivity(intent)
                                    } catch (e: ActivityNotFoundException) {
                                        Toast.makeText(requireContext(), "Tidak Ditemukan Aplikasi PDF Viewer", Toast.LENGTH_SHORT).show()
                                    }


                                } else {
                                    Toast.makeText(requireContext(), "Akses Tidak Diberikan\n(Permission Not Granted)", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onPermissionRationaleShouldBeShown(
                                permissions: List<PermissionRequest>,
                                token: PermissionToken
                            ) {
                                token.continuePermissionRequest()
                            }
                        }).check()
                } catch (e: Exception) {
                    Log.d("okimatra", "" + e.message)
                }
            }
        }
    }
    private fun bodyRawReport(doc: Document, it: SoundingEntity) {
//        idTable.widthPercentage = 100f
//        idTable.tableEvent = BorderEvent()
//        idTable.deleteBodyRows()
//        val pID = Paragraph()
//        pID.add(idTable)
//        pID.indentationLeft = horizontalPadding
//        doc.add(pID)
        doc.add(Paragraph("\n\n\n\n\n", appFontTiny))
        val metodeFraksi = it.volume_fraksi > 0
        writeDataTitle("Data Umum", doc)
        val judulUmum = listOf("Nama Perusahaan", "Alamat Perusahaan", "Nomor Tangki", "Waktu Sounding", "Lokasi Sounding")
        val nilaiUmum = listOf(
            it.perusahaan_sounding,
            it.alamat_perusahaan_sounding,
            it.no_tangki,
            (it.waktu.subSequence(0, it.waktu.indexOf(":")-3).toString()+" Pukul${it.waktu.subSequence(it.waktu.indexOf(":")-3, it.waktu.length)}").replace("-"," "),
//            it.waktu.subSequence(0,it.waktu.indexOf(":")-3).toString().replace("-"," "),
//            it.waktu.subSequence(it.waktu.indexOf(":")-2, it.waktu.length).toString(),
            it.lokasi_sounding,
        )
        writeDatawithSemicolomn(judulUmum, nilaiUmum, doc)

        writeDataTitle("Data Lapangan", doc)
        val judulLapangan = listOf("Tinggi Cairan", "Suhu Cairan")
        val nilaiLapangan = listOf(
            "${zeroRemover((it.tinggi_cairan/1000).toBigDecimal().toPlainString()).replace(".",",")} m",
            "${zeroRemover(it.suhu_cairan.toBigDecimal().toPlainString()).replace(".", ",")} °C"
        )
        writeDatawithSemicolomn(judulLapangan, nilaiLapangan, doc)

        writeDataTitle("Data Tangki", doc)
        val judulTangki = listOf("Suhu Kalibrasi Tangki", "Tinggi Meja", "Koefisien Muai Tangki")
        val nilaiTangki = listOf(
            "${zeroRemover(it.suhu_kalibrasi_tangki.toBigDecimal().toPlainString()).replace(".",",")} °C",
            "${zeroRemover(it.tinggi_meja.toBigDecimal().toPlainString()).replace(".",",")} mm",
            zeroRemover(it.faktor_muai.toBigDecimal().toPlainString()).replace(".",",")
        )
        writeDatawithSemicolomn(judulTangki, nilaiTangki, doc)

        writeDataTitle("Data Tabel", doc)
        val judulTabel: List<String>
        val nilaiTabel: List<String>
        if (metodeFraksi) {
            judulTabel = listOf(it.judulKalibrasi1, it.judulFraksi, "Massa Jenis Cairan")
            nilaiTabel = listOf(
                "${zeroRemover(it.volume_kalibrasi1.toBigDecimal().toPlainString()).replace(".",",")} L",
                "${zeroRemover(it.volume_fraksi.toBigDecimal().toPlainString()).replace(".", ",")} L",
                "${zeroRemover(it.density_cairan.toBigDecimal().toPlainString()).replace(".",",")} MT/KL"
            )
        } else {
            judulTabel = listOf(it.judulKalibrasi1,"Tabel Kalibrasi (${it.judulDataTabel})", it.judulKalibrasi2, "Massa Jenis Produk")
            nilaiTabel = listOf(
                "${zeroRemover(it.volume_kalibrasi1.toBigDecimal().toPlainString()).replace(".",",")} L",
                "${zeroRemover(it.volume_mid.toBigDecimal().toPlainString()).replace(".",",")} L",
                "${zeroRemover(it.volume_kalibrasi2.toBigDecimal().toPlainString()).replace(".", ",")} L",
                "${zeroRemover(it.density_cairan.toBigDecimal().toPlainString()).replace(".",",")} MT/KL"
            )
        }
        writeDatawithSemicolomn(judulTabel, nilaiTabel, doc)

        val metode = if (metodeFraksi) "Metode Fraksi" else "Metode Interpolasi"
        writeDataTitle("Hasil Perhitungan $metode", doc)
        val calcData = listOf("Tinggi Terkoreksi", "Volume App", "Volume Obs", "Volume", "Hasil Akhir Muatan")
        val calcValue = listOf(
            "${zeroRemover(it.tinggi_cairan_terkoreksi.toBigDecimal().toPlainString()).replace(".",",")} m",
            "${zeroRemover(it.volume_app.toBigDecimal().toPlainString()).replace(".", ",")} L",
            "${zeroRemover(it.volume_obs.toBigDecimal().toPlainString()).replace(".",",")} L",
            "${zeroRemover(it.volume.toBigDecimal().toPlainString()).replace(".",",")} KL",
            "${zeroRemover(it.hasil_sounding.toBigDecimal().toPlainString()).replace(".",",")} MT"
        )
        writeDatawithSemicolomn(calcData, calcValue, doc)
        doc.add(Paragraph("\n", appFontMiddle))

//        val ttdValue = listOf("Disusun oleh,", "Pemeriksa Bea Cukai", "\n\n")
//        writeAuthentication(ttdValue, doc)
//        val nama = Chunk(it.pegawai_sounding, regularFont)
//        nama.setUnderline(0.5f, -2f)
//        val para = Paragraph(nama)
//        para.indentationLeft = 380f
//        doc.add(para)

        val ttdPenggunaJasa = listOf("Mengetahui,", "Eksportir", "\n\n\n")
        val ttdPegawai = listOf("Disusun oleh,", "Pemeriksa Bea Cukai", "\n\n\n")
        writeAuthenticationwithCustomer(ttdPenggunaJasa, ttdPegawai, doc)

        val table = PdfPTable(2)
        table.setWidths(floatArrayOf(1f, 1f))
        table.isLockedWidth = true
        table.totalWidth = PageSize.A4.width
        val namaPJ = Chunk(it.pengguna_jasa_sounding, regularFont)
        namaPJ.setUnderline(0.5f, -2f)
        val pjCell = PdfPCell(Phrase(namaPJ))
        pjCell.border = Rectangle.NO_BORDER
        pjCell.horizontalAlignment = Element.ALIGN_LEFT
        pjCell.verticalAlignment = Element.ALIGN_TOP
        pjCell.paddingLeft = 57f
        table.addCell(pjCell)
        val namaPeg = Chunk(it.pegawai_sounding, regularFont)
        namaPeg.setUnderline(0.5f, -2f)
        val pegCell = PdfPCell(Phrase(namaPeg))
        pegCell.border = Rectangle.NO_BORDER
        pegCell.horizontalAlignment = Element.ALIGN_LEFT
        pegCell.verticalAlignment = Element.ALIGN_TOP
        pegCell.paddingLeft = 72f
        table.addCell(pegCell)
        doc.add(table)
        table.deleteBodyRows()

        val nipSpace = it.nip_pegawai.subSequence(0,8).toString() +
                " " + it.nip_pegawai.subSequence(8,14).toString() +
                " " + it.nip_pegawai.subSequence(14,15).toString() +
                " " + it.nip_pegawai.subSequence(15,it.nip_pegawai.length).toString()
        val nip = listOf(nipSpace)
        val jabatan = listOf(it.jabatan_pengguna_jasa)
        writeAuthenticationwithCustomer(jabatan, nip, doc)
    }
    private fun writeDataTitle(text: String, doc: Document) {
        val padding = 3f
        val judul= Chunk(text, appFontSemiBig)
        judul.setUnderline(0.5f, -2f)
        val paraJudul = Paragraph(judul)
        paraJudul.indentationLeft = 19*padding-1f
        doc.add(paraJudul)
        doc.add(Paragraph("\n", appFontTiny))
    }
    private fun writeDatawithSemicolomn(listJudul: List<String>, listNilai: List<String>, doc: Document) {
        val padding = 3f
        val table = PdfPTable(3)
        table.setWidths(floatArrayOf(1.7f, 0.05f, 3.5f))
        table.isLockedWidth = true
        table.totalWidth = PageSize.A4.width
        for (i in listJudul.indices) {
            val idCell = PdfPCell(Phrase(listJudul[i], regularFont))
            idCell.border = Rectangle.NO_BORDER
            idCell.horizontalAlignment = Element.ALIGN_LEFT
            idCell.verticalAlignment = Element.ALIGN_TOP
            idCell.paddingTop = padding
            idCell.paddingBottom = padding
            idCell.paddingLeft = 19*padding
            table.addCell(idCell)

            val separatorCell = PdfPCell(Phrase(":", regularFont))
            separatorCell.border = Rectangle.NO_BORDER
            separatorCell.horizontalAlignment = Element.ALIGN_CENTER
            separatorCell.verticalAlignment = Element.ALIGN_TOP
            separatorCell.paddingTop = padding
            separatorCell.paddingBottom = padding
            table.addCell(separatorCell)

            val valueCell = PdfPCell(Phrase(listNilai[i], regularFont))
            valueCell.border = Rectangle.NO_BORDER
            valueCell.horizontalAlignment = Element.ALIGN_LEFT
            valueCell.verticalAlignment = Element.ALIGN_TOP
            valueCell.paddingTop = padding
            valueCell.paddingBottom = padding
            valueCell.paddingRight = 18*padding
            table.addCell(valueCell)
        }
        doc.add(table)
        table.deleteBodyRows()
        doc.add(Paragraph("\n", appFontMiddle))
    }
    private fun writeAuthenticationwithCustomer(listPenggunaJasa: List<String>, listPegawai: List<String>, doc: Document) {
        val table = PdfPTable(2)
        table.setWidths(floatArrayOf(1f, 1f))
        table.isLockedWidth = true
        table.totalWidth = PageSize.A4.width
        for (i in listPegawai.indices) {
            val pjCell = PdfPCell(Phrase(listPenggunaJasa[i], regularFont))
            pjCell.border = Rectangle.NO_BORDER
            pjCell.horizontalAlignment = Element.ALIGN_LEFT
            pjCell.verticalAlignment = Element.ALIGN_TOP
            pjCell.paddingLeft = 57f
            table.addCell(pjCell)

            val pegCell = PdfPCell(Phrase(listPegawai[i], regularFont))
            pegCell.border = Rectangle.NO_BORDER
            pegCell.horizontalAlignment = Element.ALIGN_LEFT
            pegCell.verticalAlignment = Element.ALIGN_TOP
            pegCell.paddingLeft = 72f
            table.addCell(pegCell)
        }
        doc.add(table)
        table.deleteBodyRows()
    }
    @Suppress("unused")
    private fun writeAuthentication(listPegawai: List<String>, doc: Document) {
        for (i in listPegawai.indices) {
            val para = Paragraph(listPegawai[i], regularFont)
            para.indentationLeft = 380f
            doc.add(para)
        }
    }

    private fun resetResult() {
        binding1.apply {
            hasilKalkulator.text = getText(R.string.hasil)
            hasilVolume.text = getText(R.string.volume)
            hasilVolumeApp.text = getText(R.string.volume_app)
            hasilVolumeObs.text = getText(R.string.volume_obs)
            hasilTinggiTerkoreksi.text = getText(R.string.tinggi_terkoreksi)
        }
    }
    private fun resetDataTabel() {
        binding1.apply {
            judulTabelKalibrasi.text = getText(R.string.tabel_kalibrasi)
            judulTabelFraksi.text = getText(R.string.tabel_fraksi)
            judulTabelKalibrasi2.text = getText(R.string.tabel_kalibrasi2)
            dataTabel.text = getText(R.string.data_tabel)
        }
    }
    private fun calculatorCheck(): Boolean {
        binding1.apply {
            val checkResult: Boolean = try {
                tinggiCairan.text.toString().toDouble() > 0.0 &&
                        tinggiMeja.text.toString().toDouble() > 0.0 &&
                        suhuCairan.text.toString().toDouble() > 0.0 &&
                        suhuTetap.text.toString().toDouble() > 0.0 &&
                        muai.text.toString().toDouble() > 0.0 &&
                        densityCairan.text.toString().toDouble() > 0.0
            } catch (e: Exception) {
                false
            }
            return checkResult
        }
    }
    private fun judulDataTabel() {
        var soundingCorrect: Double
        var satuanmm : String
        var soundingCorrected: String
        var soundingCorrectedFinal: String
        binding1.apply {
            soundingCorrected = (tinggiCairan.text.toString().toDouble() + tinggiMeja.text.toString().toDouble()).toString()
            soundingCorrectedFinal = ((tinggiCairan.text.toString().toDouble() + tinggiMeja.text.toString().toDouble())/1000).toString()
            soundingCorrect = (round(soundingCorrected.toDouble())/1000.0)
            dataTabel.text = String.format(getString(R.string.data_tabel_edited), soundingCorrect.toString())
            try {
                judulTabelKalibrasi.text = String.format(getString(R.string.tabel_kalibrasi_edited), soundingCorrectedFinal.subSequence(0,soundingCorrectedFinal.indexOf(".") + 3))
            }
            catch (e:Exception) {
                try {
                    judulTabelKalibrasi.text = String.format(getString(R.string.tabel_kalibrasi_edited1), soundingCorrectedFinal.subSequence(0,soundingCorrectedFinal.indexOf(".") + 2))
                }
                catch (e:Exception) {
                    try {
                        judulTabelKalibrasi.text = String.format(getString(R.string.tabel_kalibrasi_edited2), soundingCorrectedFinal.subSequence(0,soundingCorrectedFinal.indexOf(".") + 1))
                    }
                    catch (e:Exception) {
                        judulTabelKalibrasi.text = String.format(getString(R.string.tabel_kalibrasi_edited2), soundingCorrectedFinal)
                    }
                }
            }
            if (judulTabelKalibrasi.text.toString().subSequence(judulTabelKalibrasi.text.toString().length-5,judulTabelKalibrasi.text.toString().length-4).toString().toInt() != 9) {
                judulTabelKalibrasi2.text = String.format(getString(R.string.tabel_kalibrasi_edited), (round((judulTabelKalibrasi.text.toString().subSequence(17,judulTabelKalibrasi.text.toString().length-3).toString().toDouble() + 0.01)* 100.0) / 100.0).toString())
            }
            else {
                judulTabelKalibrasi2.text = String.format(getString(R.string.tabel_kalibrasi_edited1), (round((judulTabelKalibrasi.text.toString().subSequence(17,judulTabelKalibrasi.text.toString().length-3).toString().toDouble() + 0.01)* 10.0) / 10.0).toString())
            }
            satuanmm = (tinggiCairan.text.toString().toDouble() + tinggiMeja.text.toString().toDouble()).toString()
            satuanmm = satuanmm.subSequence(satuanmm.indexOf(".")-1,satuanmm.indexOf(".")).toString()
            judulTabelFraksi.text = String.format(getString(R.string.tabel_fraksi_edited), satuanmm)
            if ("0 mm" in judulTabelFraksi.text.toString()) {
                tabelFraksi.setText("0")
            } else if ("0 mm" !in judulTabelFraksi.text.toString() && tabelFraksi.text.toString() == "0") {
                tabelFraksi.text = null
            }
        }
    }
    private fun tinggiCek (): Boolean {
        binding1.apply {
            return tinggiCairan.text.toString().isNotEmpty() and tinggiMeja.text.toString().isNotEmpty()
        }
    }
    private fun soundingCalculator(): List<Double> {
        var volumeKalibrasi2 = 0.0
        var volumeFraksi = 0.0
        var volumeMid = 0.0
        var volumeApp = 0.0
        var volumeObs = 0.0
        var volume = 0.0
        var nilaiHasilKalkulator = 0.0
        var delta: Double
        var tinggiTerkoreksi = 0.0
        binding1.apply {
            if (calculatorCheck()) {
                when {
                    tabelFraksi.text.toString().isNotEmpty() and tabelKalibrasi.text.toString()
                        .isNotEmpty() -> {
                        volumeFraksi = tabelFraksi.text.toString().toDouble()
                        volumeKalibrasi2 = tabelKalibrasi.text.toString().toDouble()
                        volumeMid = volumeKalibrasi2
                        tinggiTerkoreksi = roundDigits(
                            (tinggiCairan.text.toString().toDouble() + tinggiMeja.text.toString()
                                .toDouble()) / 1000
                        )
                        volumeApp = roundDigits(
                            tabelFraksi.text.toString().toDouble() + tabelKalibrasi.text.toString()
                                .toDouble()
                        )
                        volumeObs = roundDigits(
                            volumeApp * (1.0 + ((suhuCairan.text.toString()
                                .toDouble() - suhuTetap.text.toString()
                                .toDouble()) * muai.text.toString().toDouble()))
                        )
                        volume = roundDigits(volumeObs / 1000.0)
                        nilaiHasilKalkulator =
                            roundDigits(volume * densityCairan.text.toString().toDouble())
                        hasilKalkulator.text = String.format(
                            getString(R.string.hasil_akhir_edited),
                            nilaiHasilKalkulator.toString().replace(".", ",")
                        )
                        hasilVolume.text = String.format(
                            getString(R.string.volume_edited),
                            volume.toString().replace(".", ",")
                        )
                        hasilVolumeApp.text = String.format(
                            getString(R.string.volume_app_edited),
                            volumeApp.toString().replace(".", ",")
                        )
                        hasilVolumeObs.text = String.format(
                            getString(R.string.volume_obs_edited),
                            volumeObs.toString().replace(".", ",")
                        )
                        hasilTinggiTerkoreksi.text = String.format(
                            getString(R.string.tinggi_terkoreksi_edited),
                            tinggiTerkoreksi.toString().replace(".", ",")
                        )
                    }
                    tabelKalibrasi.text.toString().isNotEmpty() and tabelKalibrasi2.text.toString()
                        .isNotEmpty() -> {
                        volumeKalibrasi2 = tabelKalibrasi2.text.toString().toDouble()
                        tinggiTerkoreksi = roundDigits(
                            (tinggiCairan.text.toString().toDouble() + tinggiMeja.text.toString()
                                .toDouble()) / 1000
                        )
                        delta =((tinggiCairan.text.toString().toDouble() + tinggiMeja.text.toString().toDouble()) / 1000 -
                                judulTabelKalibrasi.text.toString().subSequence(
                                    judulTabelKalibrasi.text.indexOf("(") + 1,
                                    judulTabelKalibrasi.text.indexOf(")") - 2
                                ).toString().toDouble()) / 0.01
                        //Handle Double Rounding Error
                        delta = (delta * 1000000).roundToLong()/1000000.toDouble()
                        delta = (delta * 100000).roundToLong()/100000.toDouble()
                        volumeMid = tabelKalibrasi.text.toString()
                            .toDouble() + delta * (tabelKalibrasi2.text.toString()
                            .toDouble() - tabelKalibrasi.text.toString().toDouble())
                        volumeApp = roundDigits(volumeMid)
                        volumeObs = roundDigits(
                            volumeApp * (1.0 + ((suhuCairan.text.toString()
                                .toDouble() - suhuTetap.text.toString()
                                .toDouble()) * muai.text.toString().toDouble()))
                        )
                        volume = roundDigits(volumeObs / 1000.0)
                        nilaiHasilKalkulator =
                            roundDigits(volume * densityCairan.text.toString().toDouble())
                        hasilKalkulator.text = String.format(
                            getString(R.string.hasil_akhir_edited),
                            nilaiHasilKalkulator.toString().replace(".", ",")
                        )
                        hasilVolume.text = String.format(
                            getString(R.string.volume_edited),
                            volume.toString().replace(".", ",")
                        )
                        hasilVolumeApp.text = String.format(
                            getString(R.string.volume_app_edited),
                            volumeApp.toString().replace(".", ",")
                        )
                        hasilVolumeObs.text = String.format(
                            getString(R.string.volume_obs_edited),
                            volumeObs.toString().replace(".", ",")
                        )
                        hasilTinggiTerkoreksi.text = String.format(
                            getString(R.string.tinggi_terkoreksi_edited),
                            tinggiTerkoreksi.toString().replace(".", ",")
                        )
                    }
                    else -> {
                        resetResult()
                    }
                }
            } else {
                resetResult()
            }
        }
        return listOf(
            tinggiTerkoreksi,
            volumeFraksi,
            volumeKalibrasi2,
            volumeMid,
            volumeApp,
            volumeObs,
            volume,
            nilaiHasilKalkulator
        )
    }
    private fun calculatorListener(listEditText: List<AppCompatEditText>): List<Double> {
        var results1 = listOf<Double>()
        for (element in listEditText) {
            element.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun afterTextChanged(p0: Editable?) {
                    results1 = soundingCalculator()
                }
            })
        }
        return results1
    }
    private fun calculatorTinggiListener(listEditText: List<AppCompatEditText>): List<Double> {
        var results1 = listOf<Double>()
        for (element in listEditText) {
            element.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
                override fun afterTextChanged(p0: Editable?) {
                    if (tinggiCek()) {
                        judulDataTabel()
                        results1 = soundingCalculator()
                    }
                    else {
                        resetDataTabel()
                    }
                }
            })
        }
        return results1
    }

    private fun resetResultUpdate(binding: DialogUpdateSoundingBinding) {
        binding.apply {
            hasilKalkulator.text = getText(R.string.hasil)
            hasilVolume.text = getText(R.string.volume)
            hasilVolumeApp.text = getText(R.string.volume_app)
            hasilVolumeObs.text = getText(R.string.volume_obs)
            hasilTinggiTerkoreksi.text = getText(R.string.tinggi_terkoreksi)
        }
    }
    private fun resetDataTabelUdpate(binding: DialogUpdateSoundingBinding) {
        binding.apply {
            judulTabelKalibrasi.text = getText(R.string.tabel_kalibrasi)
            judulTabelFraksi.text = getText(R.string.tabel_fraksi)
            judulTabelKalibrasi2.text = getText(R.string.tabel_kalibrasi2)
        }
    }
    private fun calculatorCheckUpdate(binding: DialogUpdateSoundingBinding): Boolean {
        binding.apply {
            val checkResult: Boolean = try {
                tinggiCairan.text.toString().toDouble() > 0.0 &&
                        tinggiMeja.text.toString().toDouble() > 0.0 &&
                        suhuCairan.text.toString().toDouble() > 0.0 &&
                        suhuTetap.text.toString().toDouble() > 0.0 &&
                        faktorMuai.text.toString().toDouble() > 0.0 &&
                        densityCairan.text.toString().toDouble() > 0.0
            } catch (e: Exception) {
                false
            }
            return checkResult
        }
    }
    private fun judulDataTabelUpdate(binding: DialogUpdateSoundingBinding) {
        var satuanmm : String
        var soundingCorrectedFinal: String
        binding.apply {
            soundingCorrectedFinal = ((tinggiCairan.text.toString().toDouble() + tinggiMeja.text.toString().toDouble())/1000).toString()
            try {
                judulTabelKalibrasi.text = String.format(getString(R.string.tabel_kalibrasi_edited), soundingCorrectedFinal.subSequence(0,soundingCorrectedFinal.indexOf(".") + 3))
            }
            catch (e:Exception) {
                try {
                    judulTabelKalibrasi.text = String.format(getString(R.string.tabel_kalibrasi_edited1), soundingCorrectedFinal.subSequence(0,soundingCorrectedFinal.indexOf(".") + 2))
                }
                catch (e:Exception) {
                    try {
                        judulTabelKalibrasi.text = String.format(getString(R.string.tabel_kalibrasi_edited2), soundingCorrectedFinal.subSequence(0,soundingCorrectedFinal.indexOf(".") + 1))
                    }
                    catch (e:Exception) {
                        judulTabelKalibrasi.text = String.format(getString(R.string.tabel_kalibrasi_edited2), soundingCorrectedFinal)
                    }
                }
            }
            if (judulTabelKalibrasi.text.toString().subSequence(judulTabelKalibrasi.text.toString().length-5,judulTabelKalibrasi.text.toString().length-4).toString().toInt() != 9) {
                judulTabelKalibrasi2.text = String.format(getString(R.string.tabel_kalibrasi_edited), (round((judulTabelKalibrasi.text.toString().subSequence(17,judulTabelKalibrasi.text.toString().length-3).toString().toDouble() + 0.01)* 100.0) / 100.0).toString())
            }
            else {
                judulTabelKalibrasi2.text = String.format(getString(R.string.tabel_kalibrasi_edited1), (round((judulTabelKalibrasi.text.toString().subSequence(17,judulTabelKalibrasi.text.toString().length-3).toString().toDouble() + 0.01)* 10.0) / 10.0).toString())
            }
            satuanmm = (tinggiCairan.text.toString().toDouble() + tinggiMeja.text.toString().toDouble()).toString()
            satuanmm = satuanmm.subSequence(satuanmm.indexOf(".")-1,satuanmm.indexOf(".")).toString()
            judulTabelFraksi.text = String.format(getString(R.string.tabel_fraksi_edited), satuanmm)
            if ("0 mm" in judulTabelFraksi.text.toString()) {
                tabelFraksi.setText("0")
            } else if ("0 mm" !in judulTabelFraksi.text.toString() && tabelFraksi.text.toString() == "0") {
                tabelFraksi.text = null
            }
        }
    }
    private fun tinggiCekUpdate(binding: DialogUpdateSoundingBinding): Boolean {
        binding.apply {
            return tinggiCairan.text.toString().isNotEmpty() and tinggiMeja.text.toString().isNotEmpty()
        }
    }
    private fun soundingCalculatorUpdate(binding: DialogUpdateSoundingBinding): List<Double> {
        var volumeKalibrasi2 = 0.0
        var volumeFraksi = 0.0
        var volumeMid = 0.0
        var volumeApp = 0.0
        var volumeObs = 0.0
        var volume = 0.0
        var nilaiHasilKalkulator = 0.0
        var delta: Double
        var tinggiTerkoreksi = 0.0
        binding.apply {
            if (calculatorCheckUpdate(binding)) {
                when {
                    tabelFraksi.text.toString().isNotEmpty() and tabelKalibrasi.text.toString().isNotEmpty() -> {
                        volumeFraksi = tabelFraksi.text.toString().toDouble()
                        volumeKalibrasi2 = tabelKalibrasi.text.toString().toDouble()
                        volumeMid = volumeKalibrasi2
                        tinggiTerkoreksi = roundDigits((tinggiCairan.text.toString().toDouble() + tinggiMeja.text.toString().toDouble())/1000)
                        volumeApp = roundDigits(tabelFraksi.text.toString().toDouble() + tabelKalibrasi.text.toString().toDouble())
                        volumeObs = roundDigits(volumeApp * (1.0 + ((suhuCairan.text.toString().toDouble() - suhuTetap.text.toString().toDouble()) * faktorMuai.text.toString().toDouble())))
                        volume = roundDigits(volumeObs / 1000.0)
                        nilaiHasilKalkulator = roundDigits(volume * densityCairan.text.toString().toDouble())
                        hasilKalkulator.text = String.format(getString(R.string.hasil_akhir_edited),nilaiHasilKalkulator.toString().replace(".", ","))
                        hasilVolume.text = String.format(getString(R.string.volume_edited),volume.toString().replace(".", ","))
                        hasilVolumeApp.text = String.format(getString(R.string.volume_app_edited),volumeApp.toString().replace(".", ","))
                        hasilVolumeObs.text = String.format(getString(R.string.volume_obs_edited),volumeObs.toString().replace(".", ","))
                        hasilTinggiTerkoreksi.text = String.format(getString(R.string.tinggi_terkoreksi_edited),tinggiTerkoreksi.toString().replace(".", ","))
                    }
                    tabelKalibrasi.text.toString().isNotEmpty() and tabelKalibrasi2.text.toString().isNotEmpty() -> {
                        volumeKalibrasi2 = tabelKalibrasi2.text.toString().toDouble()
                        tinggiTerkoreksi = roundDigits((tinggiCairan.text.toString().toDouble() + tinggiMeja.text.toString().toDouble())/1000)
                        delta =((tinggiCairan.text.toString().toDouble() + tinggiMeja.text.toString().toDouble())/1000 - judulTabelKalibrasi.text.toString().subSequence(judulTabelKalibrasi.text.indexOf("(") + 1,judulTabelKalibrasi.text.indexOf(")") - 2).toString().toDouble()) / 0.01
                        delta = (delta * 1000000).roundToLong()/1000000.toDouble()
                        delta = (delta * 100000).roundToLong()/100000.toDouble()
                        volumeMid = tabelKalibrasi.text.toString().toDouble() + delta * (tabelKalibrasi2.text.toString().toDouble() - tabelKalibrasi.text.toString().toDouble())
                        volumeApp = roundDigits(volumeMid)
                        volumeObs = roundDigits(volumeApp * (1.0 + ((suhuCairan.text.toString().toDouble() - suhuTetap.text.toString().toDouble()) * faktorMuai.text.toString().toDouble())))
                        volume = roundDigits(volumeObs / 1000.0)
                        nilaiHasilKalkulator =roundDigits(volume * densityCairan.text.toString().toDouble())
                        hasilKalkulator.text = String.format(getString(R.string.hasil_akhir_edited),nilaiHasilKalkulator.toString().replace(".", ","))
                        hasilVolume.text = String.format(getString(R.string.volume_edited),volume.toString().replace(".", ","))
                        hasilVolumeApp.text = String.format(getString(R.string.volume_app_edited),volumeApp.toString().replace(".", ","))
                        hasilVolumeObs.text = String.format(getString(R.string.volume_obs_edited),volumeObs.toString().replace(".", ","))
                        hasilTinggiTerkoreksi.text = String.format(getString(R.string.tinggi_terkoreksi_edited),tinggiTerkoreksi.toString().replace(".", ","))
                    }
                    else -> {
                        resetResultUpdate(binding)
                    }
                }
            } else {
                resetResultUpdate(binding)
            }
        }
        return listOf(
            tinggiTerkoreksi,
            volumeFraksi,
            volumeKalibrasi2,
            volumeMid,
            volumeApp,
            volumeObs,
            volume,
            nilaiHasilKalkulator
        )
    }
    private fun calculatorListenerUpdate(listEditText: List<AppCompatEditText>, binding: DialogUpdateSoundingBinding): List<Double> {
        var results1 = listOf<Double>()
        for (element in listEditText) {
            element.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun afterTextChanged(p0: Editable?) {
                    results1 = soundingCalculatorUpdate(binding)
                }
            })
        }
        return results1
    }
    private fun calculatorTinggiListenerUpdate(listEditText: List<AppCompatEditText>, binding: DialogUpdateSoundingBinding): List<Double> {
        var results1 = listOf<Double>()
        for (element in listEditText) {
            element.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
                override fun afterTextChanged(p0: Editable?) {
                    if (tinggiCekUpdate(binding)) {
                        judulDataTabelUpdate(binding)
                        results1 = soundingCalculatorUpdate(binding)
                    }
                    else {
                        resetDataTabelUdpate(binding)
                    }
                }
            })
        }
        return results1
    }

    private fun setupListOfUserDataIntoRecyclerView(employeesList:ArrayList<PegawaiEntity>, userDao: UserDao) {
        if (employeesList.isNotEmpty()) {
            val itemAdapter = PegawaiAdapter(employeesList,{ updateId ->
                updateRecordDialogUser(updateId,userDao)
            }){deleteId->deleteRecordAlertDialogUser(deleteId,userDao)
            }
            _binding3?.rvUserList?.layoutManager = LinearLayoutManager(requireContext())
            _binding3?.rvUserList?.adapter = itemAdapter
            _binding3?.svUserList?.visibility = View.VISIBLE
            _binding3?.svServiceUserList?.visibility = View.GONE
            _binding3?.tvNoRecordsAvailable?.visibility = View.GONE
        } else {
            _binding3?.svUserList?.visibility = View.GONE
            _binding3?.svServiceUserList?.visibility = View.GONE
            _binding3?.tvNoRecordsAvailable?.visibility = View.VISIBLE
        }
    }
    private fun addRecordUser(userDao: UserDao) {
        val name = endSpaceRemover(_binding3?.nama?.text.toString())
        val nip = _binding3?.nip?.text.toString()
        val sdfyear = SimpleDateFormat("yyyy", Locale.getDefault())
        val sdfdate = SimpleDateFormat("yyyyMM", Locale.getDefault())
        val year = sdfyear.format(Calendar.getInstance().time)
        val date = sdfdate.format(Calendar.getInstance().time)
        when {
            name.isEmpty() -> {
                Toast.makeText(context, "Mohon Masukkan Nama Anda", Toast.LENGTH_SHORT).show()
            }
            nip.isEmpty() -> {
                Toast.makeText(context, "Mohon Masukkan NIP Anda", Toast.LENGTH_SHORT).show()
            }
            nip.length != 18 -> {
                Toast.makeText(context, "Jumlah Digit NIP Anda Kurang", Toast.LENGTH_SHORT).show()
            }
            nip.substring(0,4).toInt() !in year.toInt()-90..year.toInt()-13 -> {
                Toast.makeText(context, "Mohon Periksa Tahun Lahir Anda", Toast.LENGTH_SHORT).show()
            }
            nip.substring(4,6).toInt() !in 1..12 -> {
                Toast.makeText(context, "Mohon Periksa Bulan Lahir Anda", Toast.LENGTH_SHORT).show()
            }
            nip.substring(6,8).toInt() !in 1..31 -> {
                Toast.makeText(context, "Mohon Periksa Hari Lahir Anda", Toast.LENGTH_SHORT).show()
            }
            nip.substring(8,12).toInt() !in nip.substring(0,4).toInt()+13..year.toInt() -> {
                Toast.makeText(context, "Mohon Periksa Tahun Penerimaan PNS Anda", Toast.LENGTH_SHORT).show()
            }
            nip.substring(8,12).toInt() - nip.substring(0,4).toInt() > 70 -> {
                Toast.makeText(context, "Mohon Periksa Tahun Lahir dan\nTahun Penerimaan PNS Anda", Toast.LENGTH_SHORT).show()
            }
            nip.substring(12,14).toInt() !in 1..12 -> {
                Toast.makeText(context, "Mohon Periksa Bulan Penerimaan PNS Anda", Toast.LENGTH_SHORT).show()
            }
            nip.substring(8,14).toInt() > date.toInt() -> {
                Toast.makeText(context, "Anda Bulan Ini Belum Menjadi PNS", Toast.LENGTH_SHORT).show()
            }
            nip.substring(14,15).toInt() !in 1..2 -> {
                Toast.makeText(context, "Mohon Periksa Kode Terkait Jenis Kelamin Anda", Toast.LENGTH_SHORT).show()
            }
            else -> {
                lifecycleScope.launch {
                    userDao.fetchKantorByKota(binding3.kantorPegawai.selectedItem.toString()).collect {
                        lifecycleScope.launch {
                            userDao.insertUser(PegawaiEntity(nama_pegawai = name,
                                nip = nip,
                                kota_pegawai = it.kota,
                                kantor_pegawai = it.kantor,
                                kanwil_pegawai = it.kanwil,
                                lokasi_ba_pegawai = it.lokasi_ba,
                                format_ba_pegawai = it.format_ba,
                                format_3d_pegawai = it.format_3d
                            ))
                            Toast.makeText(context, "Data Berhasil Disimpan", Toast.LENGTH_SHORT).show()
                            _binding3?.nama?.text?.clear()
                            _binding3?.nip?.text?.clear()
                        }
                    }
                }
            }
        }
    }
    private fun updateRecordDialogUser(id:Int, userDao: UserDao) {
        val updateDialog = Dialog(requireContext(), R.style.Theme_Dialog)
        updateDialog.setCancelable(false)
        val binding = DialogUpdateBinding.inflate(layoutInflater)
        updateDialog.setContentView(binding.root)
        updateDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        lifecycleScope.launch {
            userDao.fetchAllKantor().collect { it ->
                populateDropdownKantor(ArrayList(it), binding.updateKantor)
                val items = arrayListOf<String>()
                for (i in 0 until ArrayList(it).size) {
                    items.add(ArrayList(it)[i].kota)
                }
                val adapter = ArrayAdapter(
                    requireContext(),
                    R.layout.dropdown_layout,
                    items
                )
                lifecycleScope.launch {
                    userDao.fetchUserById(id).collect {
                        try {
                            binding.updateNama.setText(it.nama_pegawai)
                            binding.updateNip.setText(it.nip)
                            val spinnerPosition = adapter.getPosition(it.kota_pegawai)
                            binding.updateKantor.setSelection(spinnerPosition)
                        } catch (e: Exception) {
                            Log.e("okimatra", "" + e.message)
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            userDao.fetchUserById(id).collect {
                try {
                    binding.updateNama.setText(it.nama_pegawai)
                    binding.updateNip.setText(it.nip)
                } catch (e: Exception) {
                    Log.e("okimatra", "" + e.message)
                }
            }
        }
        binding.tvUpdate.setOnClickListener {
            val name = endSpaceRemover(binding.updateNama.text.toString())
            val nip = binding.updateNip.text.toString()
            val sdfyear = SimpleDateFormat("yyyy", Locale.getDefault())
            val sdfdate = SimpleDateFormat("yyyyMM", Locale.getDefault())
            val year = sdfyear.format(Calendar.getInstance().time)
            val date = sdfdate.format(Calendar.getInstance().time)
            when {
                name.isEmpty() -> {
                    Toast.makeText(context, "Mohon masukkan Nama Anda", Toast.LENGTH_SHORT).show()
                }
                nip.isEmpty() -> {
                    Toast.makeText(context, "Mohon masukkan NIP Anda", Toast.LENGTH_SHORT).show()
                }
                nip.length != 18 -> {
                    Toast.makeText(context, "Jumlah Digit NIP Anda Kurang", Toast.LENGTH_SHORT).show()
                }
                nip.substring(0,4).toInt() !in year.toInt()-90..year.toInt()-13 -> {
                    Toast.makeText(context, "Mohon Periksa Tahun Lahir Anda", Toast.LENGTH_SHORT).show()
                }
                nip.substring(4,6).toInt() !in 1..12 -> {
                    Toast.makeText(context, "Mohon Periksa Bulan Lahir Anda", Toast.LENGTH_SHORT).show()
                }
                nip.substring(6,8).toInt() !in 1..31 -> {
                    Toast.makeText(context, "Mohon Periksa Hari Lahir Anda", Toast.LENGTH_SHORT).show()
                }
                nip.substring(8,12).toInt() !in nip.substring(0,4).toInt()+13..year.toInt() -> {
                    Toast.makeText(context, "Mohon Periksa Tahun Penerimaan PNS Anda", Toast.LENGTH_SHORT).show()
                }
                nip.substring(8,12).toInt() - nip.substring(0,4).toInt() > 70 -> {
                    Toast.makeText(context, "Mohon Periksa Tahun Lahir dan\nTahun Penerimaan PNS Anda", Toast.LENGTH_SHORT).show()
                }
                nip.substring(12,14).toInt() !in 1..12 -> {
                    Toast.makeText(context, "Mohon Periksa Bulan Penerimaan PNS Anda", Toast.LENGTH_SHORT).show()
                }
                nip.substring(8,14).toInt() > date.toInt() -> {
                    Toast.makeText(context, "Anda Bulan Ini Belum Menjadi PNS", Toast.LENGTH_SHORT).show()
                }
                nip.substring(14,15).toInt() !in 1..2 -> {
                    Toast.makeText(context, "Mohon Periksa Kode Terkait Jenis Kelamin Anda", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    lifecycleScope.launch {
                        userDao.fetchKantorByKota(binding.updateKantor.selectedItem.toString()).collect {
                            lifecycleScope.launch {
                                userDao.updateUser(PegawaiEntity(id,
                                    name,
                                    nip,
                                    kota_pegawai = it.kota,
                                    kantor_pegawai = it.kantor,
                                    kanwil_pegawai = it.kanwil,
                                    lokasi_ba_pegawai = it.lokasi_ba,
                                    format_ba_pegawai = it.format_ba,
                                    format_3d_pegawai = it.format_3d
                                ))
                                Toast.makeText(context, "Data Berhasil Diupdate", Toast.LENGTH_SHORT)
                                    .show()
                                updateDialog.dismiss()
                            }
                        }
                    }
                }
            }
        }
        binding.tvCancel.setOnClickListener{
            updateDialog.dismiss()
        }
        updateDialog.show()
    }
    private fun deleteRecordAlertDialogUser(id:Int,userDao: UserDao) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Hapus Data").setMessage("Apakah Anda yakin ingin menghapus data?")
        builder.setPositiveButton("Yes") { dialogInterface, _ ->
            lifecycleScope.launch {
                userDao.deleteUser(PegawaiEntity(id))
                Toast.makeText(context,"Data Berhasil Dihapus",Toast.LENGTH_SHORT).show()
                dialogInterface.dismiss()
            }
        }
        builder.setNegativeButton("No") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun setupListOfServiceUserDataIntoRecyclerView(penggunaJasaList:ArrayList<PenggunaJasaEntity>, userDao: UserDao) {
        if (penggunaJasaList.isNotEmpty()) {
            val penggunaJasaAdapter = PenggunaJasaAdapter(penggunaJasaList,{updateId -> updateRecordDialogServiceUser(updateId,userDao)},{deleteId->deleteRecordAlertDialogServiceUser(deleteId,userDao)})
            _binding3?.rvServiceUserList?.layoutManager = LinearLayoutManager(context)
            _binding3?.rvServiceUserList?.adapter = penggunaJasaAdapter
            _binding3?.svServiceUserList?.visibility = View.VISIBLE
            _binding3?.svUserList?.visibility = View.GONE
            _binding3?.tvNoRecordsAvailable?.visibility = View.GONE
        } else {
            _binding3?.svUserList?.visibility = View.GONE
            _binding3?.svServiceUserList?.visibility = View.GONE
            _binding3?.tvNoRecordsAvailable?.visibility = View.VISIBLE
        }
    }
    private fun addRecordServiceUser(userDao: UserDao) {
        val name = endSpaceRemover(_binding3?.nama?.text.toString())
        val jabatan = endSpaceRemover(_binding3?.jabatan?.text.toString())
        val perusahaan = _binding3?.perusahaan?.selectedItem.toString()
        var npwpPerusahaan: String
        var alamatPerusahaan: String
        lifecycleScope.launch {
            userDao.fetchCompanyByName(perusahaan).collect {
                try {
                    npwpPerusahaan = it.npwp
                    alamatPerusahaan = it.alamat
                    when {
                        name.isEmpty() -> {
                            Toast.makeText(context, "Mohon Masukkan Nama Pengguna Jasa", Toast.LENGTH_SHORT).show()
                        }
                        jabatan.isEmpty() -> {
                            Toast.makeText(context, "Mohon Masukkan Jabatan Pengguna Jasa", Toast.LENGTH_SHORT).show()
                        }
                        perusahaan.isEmpty() -> {
                            Toast.makeText(context, "Mohon Masukkan Nama Perusahaan", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            lifecycleScope.launch {
                                userDao.insertServiceUser(PenggunaJasaEntity(nama_pengguna_jasa = name, jabatan = jabatan, perusahaan_pengguna_jasa = perusahaan, npwp_perusahaan = npwpPerusahaan, alamat_perusahaan = alamatPerusahaan))
                                Toast.makeText(context, "Data Berhasil Disimpan", Toast.LENGTH_SHORT).show()
                                _binding3?.nama?.text?.clear()
                                _binding3?.jabatan?.text?.clear()
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.d("okimatra", "" + e.message)
                }
            }
        }
    }
    private fun updateRecordDialogServiceUser(id:Int, userDao: UserDao) {
        val updateDialog = Dialog(requireContext(), R.style.Theme_Dialog)
        updateDialog.setCancelable(false)
        val binding = DialogUpdateServiceUserBinding.inflate(layoutInflater)
        updateDialog.setContentView(binding.root)
        updateDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        lifecycleScope.launch {
            userDao.fetchAllCompany().collect { it ->
                populateDropdownCompany(ArrayList(it), binding.updatePerusahaan)
                val items = arrayListOf<String>()
                if (ArrayList(it).isNotEmpty()) {
                    for (i in 0 until ArrayList(it).size) {
                        items.add(ArrayList(it)[i].nama_perusahaan)
                    }
                    val adapter = activity?.let { it2 ->
                        ArrayAdapter(
                            it2,
                            R.layout.dropdown_layout,
                            items
                        )
                    }
                    lifecycleScope.launch {
                        userDao.fetchServiceUserById(id).collect {
                            try {
                                binding.updateNama.setText(it.nama_pengguna_jasa)
                                binding.updateJabatan.setText(it.jabatan)
                                val spinnerPosition = adapter?.getPosition(it.perusahaan_pengguna_jasa)
                                if (spinnerPosition != null) {
                                    binding.updatePerusahaan.setSelection(spinnerPosition)
                                }
                            } catch (e: Exception) {
                                Log.e("okimatra", "" + e.message)
                            }
                        }
                    }
                } else {
                    lifecycleScope.launch {
                        userDao.fetchServiceUserById(id).collect {
                            try {
                                binding.updateNama.setText(it.nama_pengguna_jasa)
                                binding.updateJabatan.setText(it.jabatan)
                            } catch (e: Exception) {
                                Log.d("okimatra", "" + e.message)
                            }
                        }
                    }
                }
            }
        }

        binding.tvUpdate.setOnClickListener {
            val name = endSpaceRemover(binding.updateNama.text.toString())
            val jabatan = endSpaceRemover(binding.updateJabatan.text.toString())
            val perusahaan = binding.updatePerusahaan.selectedItem.toString()
            var npwpPerusahaan: String
            var alamatPerusahaan: String

            lifecycleScope.launch {
                userDao.fetchCompanyByName(perusahaan).collect {
                    try {
                        npwpPerusahaan = it.npwp
                        alamatPerusahaan = it.alamat
                        when {
                            name.isEmpty() -> {
                                Toast.makeText(context, "Mohon Masukkan Nama Eksportir", Toast.LENGTH_SHORT).show()
                            }
                            jabatan.isEmpty() -> {
                                Toast.makeText(context, "Mohon Masukkan Jabatan Eksportir", Toast.LENGTH_SHORT).show()
                            }
                            perusahaan.isEmpty() -> {
                                Toast.makeText(context, "Mohon Pilih Perusahaan", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                lifecycleScope.launch {
                                    userDao.updateServiceUser(PenggunaJasaEntity(id, name, jabatan, perusahaan, npwpPerusahaan, alamatPerusahaan))
                                    Toast.makeText(context, "Data Berhasil Diupdate", Toast.LENGTH_SHORT)
                                        .show()
                                    updateDialog.dismiss()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.d("okimatra", "" + e.message)
                    }
                }
            }
        }

        binding.tvCancel.setOnClickListener{
            updateDialog.dismiss()
        }
        updateDialog.show()
    }
    private fun deleteRecordAlertDialogServiceUser(id:Int,userDao: UserDao) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Hapus Data").setMessage("Apakah Anda yakin ingin menghapus data?")
        builder.setPositiveButton("Yes") { dialogInterface, _ ->
            lifecycleScope.launch {
                //calling the deleteEmployee method of DatabaseHandler class to delete record
                userDao.deleteServiceUser(PenggunaJasaEntity(id))
                Toast.makeText(
                    context,
                    "Data Berhasil Dihapus",
                    Toast.LENGTH_SHORT
                ).show()
                dialogInterface.dismiss()
            }
        }
        builder.setNegativeButton("No") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun setupListOfDataIntoRecyclerViewCompany(perusahaanList:ArrayList<PerusahaanEntity>, userDao: UserDao) {
        if (perusahaanList.isNotEmpty()) {
            val companyAdapter = PerusahaanAdapter(perusahaanList, {updateId->updateRecordDialogCompany(updateId,userDao)}) {deleteId->deleteRecordAlertDialogCompany(deleteId,userDao)}
            _binding3?.rvCompanyList?.layoutManager = LinearLayoutManager(context)
            _binding3?.rvCompanyList?.adapter = companyAdapter
            _binding3?.svCompanyList?.visibility = View.VISIBLE
            _binding3?.tvNoRecordsAvailable?.visibility = View.GONE
        } else {
            _binding3?.svCompanyList?.visibility = View.GONE
            _binding3?.tvNoRecordsAvailable?.visibility = View.VISIBLE
        }
    }
    private fun addRecordCompany(userDao: UserDao) {
        val name = endSpaceRemover(_binding3?.nama?.text.toString())
        val npwp = _binding3?.etNPWPId?.text.toString()
        val alamat = endSpaceRemover(_binding3?.etAlamatId?.text.toString())
        when {
            name.isEmpty() -> {
                Toast.makeText(context, "Mohon Masukkan Nama Perusahaan", Toast.LENGTH_SHORT).show()
            }
            npwp.isEmpty() || "_" in npwp -> {
                Toast.makeText(context, "Mohon Masukkan NPWP Perusahaan", Toast.LENGTH_SHORT).show()
            }
            alamat.isEmpty() -> {
                Toast.makeText(context, "Mohon Masukkan Alamat Perusahaan", Toast.LENGTH_SHORT).show()
            }
            else -> {
                lifecycleScope.launch {
                    userDao.insertCompany(PerusahaanEntity(nama_perusahaan = name, npwp = npwp, alamat = alamat))
                    Toast.makeText(context, "Data Berhasil Disimpan", Toast.LENGTH_SHORT).show()
                    _binding3?.nama?.text?.clear()
                    _binding3?.etNPWPId?.text?.clear()
                    _binding3?.etAlamatId?.text?.clear()
                }
            }
        }
    }
    private fun updateRecordDialogCompany(id:Int, userDao: UserDao) {
        val updateDialog = Dialog(requireContext(), R.style.Theme_Dialog)
        updateDialog.setCancelable(false)
        val binding = DialogUpdateCompanyBinding.inflate(layoutInflater)
        updateDialog.setContentView(binding.root)
        updateDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        lifecycleScope.launch {
            userDao.fetchCompanyById(id).collect {
                try {
                    binding.updateNama.setText(it.nama_perusahaan)
                    binding.updateNpwp.setText(it.npwp)
                    binding.etUpdateAlamatId.setText(it.alamat)
                } catch (e: Exception) {
                    Log.e("okimatra", "" + e.message)
                }
            }
        }

        binding.apply {
            var number = ""
            var numberOld = ""
            var textOld = ""
            val holder = "__.___.___._-___.___"
            var cursorPosition: Int
            var cursor: Int

            updateNpwp.setOnFocusChangeListener { _, _ ->
                if (updateNpwp.text.toString().isEmpty()) {
                    updateNpwp.setText(getString(R.string.before_edited))
                }
                if (updateNpwp.selectionStart > number.length) {
                    updateNpwp.setSelection(number.length)
                }
            }

            updateNama.setOnFocusChangeListener { _, _ ->
                if (updateNpwp.text.toString().isEmpty() || updateNpwp.text.toString() == getString(R.string.before_edited)) {
                    updateNpwp.setText("")
                }
            }

            etUpdateAlamatId.setOnFocusChangeListener { _, _ ->
                if (updateNpwp.text.toString().isEmpty() || updateNpwp.text.toString() == getString(R.string.before_edited)) {
                    updateNpwp.setText("")
                }
            }

            updateNpwp.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    number = s.toString().replace("_","").replace(".","").replace("-","")
                    cursorPosition = updateNpwp.selectionStart
                    if (cursorPosition > number.length) {
                        updateNpwp.setSelection(number.length)
                    }
                    if ((numberOld != number || updateNpwp.text.toString().length != 20) && updateNpwp.hasFocus()) {
                        numberOld = number
                        cursor = numberOld.length
                        when (numberOld.length) {
                            15 -> {
                                updateNpwp.setText(String.format(getString(R.string.number_15),numberOld.substring(0,2),numberOld.substring(2,5),numberOld.substring(5,8),numberOld.substring(8,9),numberOld.substring(9,12),numberOld.substring(12)))
                                cursor += 5
                            }
                            in 13..14 -> {
                                updateNpwp.setText(String.format(getString(R.string.number_12),numberOld.substring(0,2),numberOld.substring(2,5),numberOld.substring(5,8),numberOld.substring(8,9),numberOld.substring(9,12),numberOld.substring(12),holder.substring(numberOld.length+5)))
                                cursor += 5
                            }
                            in 10..12 -> {
                                updateNpwp.setText(String.format(getString(R.string.number_9),numberOld.substring(0,2),numberOld.substring(2,5),numberOld.substring(5,8),numberOld.substring(8,9),numberOld.substring(9),holder.substring(numberOld.length+4)))
                                cursor += 4
                            }
                            9 -> {
                                updateNpwp.setText(String.format(getString(R.string.number_9_exact),numberOld.substring(0,2),numberOld.substring(2,5),numberOld.substring(5,8),numberOld.substring(8,9),holder.substring(numberOld.length+3)))
                                cursor += 3
                            }
                            in 6..8 -> {
                                updateNpwp.setText(String.format(getString(R.string.number_5),numberOld.substring(0,2),numberOld.substring(2,5),numberOld.substring(5),holder.substring(numberOld.length+2)))
                                cursor += 2
                            }
                            in 3..5 -> {
                                updateNpwp.setText(String.format(getString(R.string.number_2),numberOld.substring(0,2),numberOld.substring(2),holder.substring(numberOld.length+1)))
                                cursor += 1
                            }
                            in 1..2 -> {
                                updateNpwp.setText(String.format(getString(R.string.number_0),numberOld.substring(0),holder.substring(numberOld.length)))
                            }
                            0 -> {
                                updateNpwp.setText(holder)
                            }
                            else -> {
                                updateNpwp.setText(textOld)
                            }
                        }
                        updateNpwp.post {
                            updateNpwp.setSelection(cursor)
                        }
                    }
                }

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                    textOld = s.toString()
                }

                override fun afterTextChanged(s: Editable) {
                }
            })

            updateNpwp.setOnClickListener {
                if (updateNpwp.selectionStart > number.length) {
                    updateNpwp.setSelection(number.length)
                }
            }
        }

        binding.tvUpdate.setOnClickListener {
            val name = endSpaceRemover(binding.updateNama.text.toString())
            val npwp = binding.updateNpwp.text.toString()
            val alamat = endSpaceRemover(binding.etUpdateAlamatId.text.toString())
            when {
                name.isEmpty() -> {
                    Toast.makeText(context, "Mohon masukkan Nama Perusahaan", Toast.LENGTH_SHORT).show()
                }
                npwp.isEmpty() || "_" in npwp-> {
                    Toast.makeText(context, "Mohon masukkan NPWP Perusahaan", Toast.LENGTH_SHORT).show()
                }
                alamat.isEmpty() -> {
                    Toast.makeText(context, "Mohon masukkan Alamat Perusahaan", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    lifecycleScope.launch {
                        userDao.updateCompany(PerusahaanEntity(id, name, npwp, alamat))
                        Toast.makeText(context, "Data Berhasil Diupdate", Toast.LENGTH_SHORT)
                            .show()
                        updateDialog.dismiss()
                    }
                }
            }
        }
        binding.tvCancel.setOnClickListener{
            updateDialog.dismiss()
        }
        updateDialog.show()
    }
    private fun deleteRecordAlertDialogCompany(id:Int,userDao: UserDao) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Hapus Data").setMessage("Apakah Anda yakin ingin menghapus data?")
        builder.setPositiveButton("Yes") { dialogInterface, _ ->
            lifecycleScope.launch {
                userDao.deleteCompany(PerusahaanEntity(id))
                Toast.makeText(
                    context,
                    "Data Berhasil Dihapus",
                    Toast.LENGTH_SHORT
                ).show()
                dialogInterface.dismiss()
            }
        }
        builder.setNegativeButton("No") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun updateRecordDialogSounding(id:Int, userDao: UserDao) {
        val updateDialog = Dialog(requireContext(), R.style.Theme_Dialog)
        updateDialog.setCancelable(false)
        val binding = DialogUpdateSoundingBinding.inflate(layoutInflater)
        updateDialog.setContentView(binding.root)
        updateDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.apply {
            var results: List<Double>
            var waktuDate = Date().time
            val listETTinggi = listOf(tinggiCairan, tinggiMeja)
            val listET = listOf(suhuCairan, suhuTetap, faktorMuai, tabelFraksi, tabelKalibrasi, tabelKalibrasi2, densityCairan)

            results = calculatorTinggiListenerUpdate(listETTinggi, binding)
            results = calculatorListenerUpdate(listET, binding)

            tvNext.setOnClickListener {
                if (emptyCheck(listOf(tinggiCairan,suhuCairan,suhuTetap,tinggiMeja,faktorMuai))) {
                    if (tabelKalibrasi.text.toString() == tabelKalibrasi2.text.toString()) {
                        tabelKalibrasi2.setText("")
                    }
                    if (tabelFraksi.text.toString() == "0") {
                        tabelFraksi.setText("")
                    }
                    visibilityGone(listOf(judulTinggiCairan,judulSuhuCairan,judulSuhuTetap,judulTinggiMeja,judulFaktorMuai,tvNext,tvCancel), listOf(tinggiCairan,suhuCairan, suhuTetap, tinggiMeja, faktorMuai))
                    visibilityVisible(listOf(fraksiTab,interpolasiTab,tvNext1,tvBack,judulTabelKalibrasi,judulTabelFraksi,judulDensityCairan), listOf(tabelKalibrasi,tabelFraksi,densityCairan))
                    tabLayout.visibility = View.VISIBLE
                } else {
                    Toast.makeText(context, "Mohon Cek Kelengkapan Data", Toast.LENGTH_SHORT).show()
                }
            }

            tvBack.setOnClickListener {
                visibilityGone(listOf(fraksiTab,interpolasiTab,tvNext1,tvBack,judulTabelKalibrasi,judulTabelKalibrasi2,judulTabelFraksi,judulDensityCairan), listOf(tabelKalibrasi,tabelKalibrasi2,tabelFraksi,densityCairan))
                visibilityVisible(listOf(judulTinggiCairan,judulSuhuCairan,judulSuhuTetap,judulTinggiMeja,judulFaktorMuai,tvNext,tvCancel), listOf(tinggiCairan,suhuCairan, suhuTetap, tinggiMeja, faktorMuai))
                tabLayout.visibility = View.GONE
            }

            tvCancel.setOnClickListener{
                updateDialog.dismiss()
            }

            interpolasiTab.setOnClickListener {
                interpolasiTab.background = ResourcesCompat.getDrawable(resources, R.drawable.switch_on,null)
                interpolasiTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.white))
                fraksiTab.background = null
                fraksiTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.appTheme))
                judulTabelFraksi.visibility = View.GONE
                tabelFraksi.visibility = View.GONE
                if ("m" !in judulTabelKalibrasi.text.toString()) {
                    judulTabelKalibrasi.text = getText(R.string.tabel_kalibrasi1)
                }
                tabelKalibrasi.hint = getText(R.string.vol_kalibrasi1)
                judulTabelKalibrasi2.visibility = View.VISIBLE
                tabelKalibrasi2.visibility = View.VISIBLE
                tabelFraksi.text = null
            }

            fraksiTab.setOnClickListener {
                fraksiTab.background = ResourcesCompat.getDrawable(resources, R.drawable.switch_on,null)
                fraksiTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.white))
                interpolasiTab.background = null
                interpolasiTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.appTheme))
                judulTabelKalibrasi2.visibility = View.GONE
                tabelKalibrasi2.visibility = View.GONE
                if ("m" !in judulTabelKalibrasi.text.toString()) {
                    judulTabelKalibrasi.text = getText(R.string.tabel_kalibrasi)
                }
                tabelKalibrasi.hint = getText(R.string.vol_kalibrasi)
                judulTabelFraksi.visibility = View.VISIBLE
                tabelFraksi.visibility = View.VISIBLE
                tabelKalibrasi2.text = null
            }

            up.setOnClickListener {
                hasilVolume.visibility = View.GONE
                hasilVolumeApp.visibility = View.GONE
                hasilVolumeObs.visibility = View.GONE
                hasilTinggiTerkoreksi.visibility = View.GONE
                up.visibility = View.GONE
                down.visibility = View.VISIBLE
            }

            down.setOnClickListener {
                hasilVolume.visibility = View.VISIBLE
                hasilVolumeApp.visibility = View.VISIBLE
                hasilVolumeObs.visibility = View.VISIBLE
                hasilTinggiTerkoreksi.visibility = View.VISIBLE
                up.visibility = View.VISIBLE
                down.visibility = View.GONE
            }

            namaPenggunaJasa.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    lifecycleScope.launch {
                        userDao.fetchServiceUserByName(namaPenggunaJasa.selectedItem.toString()).collect {
                            try {
                                lokasiSounding.setText(String.format(getString(R.string.lokasi_edited),it.perusahaan_pengguna_jasa))
                            } catch (e: Exception) {
                                Log.d("okimatra", "" + e.message)
                            }
                        }
                    }
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    lifecycleScope.launch {
                        userDao.fetchServiceUserByName(namaPenggunaJasa.selectedItem.toString()).collect {
                            try {
                                lokasiSounding.setText(String.format(getString(R.string.lokasi_edited),it.perusahaan_pengguna_jasa))
                            } catch (e: Exception) {
                                Log.d("okimatra", "" + e.message)
                            }
                        }
                    }
                }
            }

            lifecycleScope.launch {
                userDao.fetchSoundingById(id).collect { it1 ->
                    try {
                        tinggiCairan.setText(zeroRemover(it1.tinggi_cairan.toBigDecimal().toPlainString()))
                        suhuCairan.setText(zeroRemover(it1.suhu_cairan.toBigDecimal().toPlainString()))
                        suhuTetap.setText(zeroRemover(it1.suhu_kalibrasi_tangki.toBigDecimal().toPlainString()))
                        tinggiMeja.setText(zeroRemover(it1.tinggi_meja.toBigDecimal().toPlainString()))
                        faktorMuai.setText(zeroRemover(it1.faktor_muai.toBigDecimal().toPlainString()))
                        tabelKalibrasi.setText(zeroRemover(it1.volume_kalibrasi1.toBigDecimal().toPlainString()))
                        tabelKalibrasi2.setText(zeroRemover(it1.volume_kalibrasi2.toBigDecimal().toPlainString()))
                        tabelFraksi.setText(zeroRemover(it1.volume_fraksi.toBigDecimal().toPlainString()))
                        densityCairan.setText(zeroRemover(it1.density_cairan.toBigDecimal().toPlainString()))
                        noTangki.setText(it1.no_tangki)
                        lokasiSounding.setText(it1.lokasi_sounding)
                        waktu.setText(it1.waktu)
                        waktuDate = it1.waktu_date
                        lifecycleScope.launch {
                            userDao.fetchAllUser().collect { it2 ->
                                populateDropdownUser(ArrayList(it2), namaPegawai)
                                val items = arrayListOf<String>()
                                if (ArrayList(it2).isNotEmpty()) {
                                    for (i in 0 until ArrayList(it2).size) {
                                        items.add(ArrayList(it2)[i].nama_pegawai)
                                    }
                                    val adapter = activity?.let { it ->
                                        ArrayAdapter(
                                            it,
                                            R.layout.dropdown_layout,
                                            items
                                        )
                                    }
                                    val spinnerPosition = adapter?.getPosition(it1.pegawai_sounding)
                                    if (spinnerPosition != null) {
                                        namaPegawai.setSelection(spinnerPosition)
                                    }
                                }
                            }
                        }

                        lifecycleScope.launch {
                            userDao.fetchAllServiceUser().collect { it3 ->
                                populateDropdownServiceUser(ArrayList(it3), namaPenggunaJasa)
                                val items = arrayListOf<String>()
                                if (ArrayList(it3).isNotEmpty()) {
                                    for (i in 0 until ArrayList(it3).size) {
                                        items.add(ArrayList(it3)[i].nama_pengguna_jasa)
                                    }
                                    val adapter = activity?.let { it ->
                                        ArrayAdapter(
                                            it,
                                            R.layout.dropdown_layout,
                                            items
                                        )
                                    }
                                    val spinnerPosition = adapter?.getPosition(it1.pengguna_jasa_sounding)
                                    if (spinnerPosition != null) {
                                        namaPenggunaJasa.setSelection(spinnerPosition)
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("okimatra", "" + e.message)
                    }
                }
            }

            tvNext1.setOnClickListener {
                if (hasilKalkulator.text.toString() != "Hasil: 0.000 MT") {
                    visibilityGone(listOf(fraksiTab,interpolasiTab,tvNext1,tvBack,judulTabelKalibrasi,judulTabelFraksi,judulDensityCairan,judulTabelKalibrasi2), listOf(tabelKalibrasi,tabelKalibrasi2,tabelFraksi,densityCairan))
                    tabLayout.visibility = View.GONE
                    hasilLayout.visibility = View.GONE
                    visibilityVisible(listOf(judulNoTangki,judulNamaPegawai,judulNamaPenggunaJasa,judulLokasiSounding,judulWaktu,tvUpdate,tvBack1), listOf(noTangki,lokasiSounding,waktu))
                    namaPegawai.visibility = View.VISIBLE
                    namaPenggunaJasa.visibility = View.VISIBLE
                } else {
                    Toast.makeText(context, "Mohon Cek Kelengkapan Data", Toast.LENGTH_SHORT).show()
                }
            }

            tvBack1.setOnClickListener {
                visibilityGone(listOf(judulNoTangki,judulNamaPegawai,judulNamaPenggunaJasa,judulLokasiSounding,judulWaktu,tvUpdate,tvBack1), listOf(noTangki,waktu,lokasiSounding))
                namaPegawai.visibility = View.GONE
                namaPenggunaJasa.visibility = View.GONE
                visibilityVisible(listOf(fraksiTab,interpolasiTab,tvNext1,tvBack,judulTabelKalibrasi,judulDensityCairan), listOf(tabelKalibrasi,densityCairan))
                tabLayout.visibility = View.VISIBLE
                hasilLayout.visibility = View.VISIBLE
                if (tabelFraksi.text.toString().isNotEmpty()) {
                    tabelFraksi.visibility = View.VISIBLE
                    judulTabelFraksi.visibility = View.VISIBLE
                }
                if (tabelKalibrasi2.text.toString().isNotEmpty()) {
                    tabelKalibrasi2.visibility = View.VISIBLE
                    judulTabelKalibrasi2.visibility = View.VISIBLE
                }
            }

            waktu.setOnClickListener {
                dateSetListener = DatePickerDialog.OnDateSetListener {
                        _, year, month, dayOfMonth ->
                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.MONTH, month)
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    val timeID = "EEEE, dd-MMMM-yyyy"
                    val sdf = SimpleDateFormat(timeID, Locale.getDefault())
                    val tanggalEng = sdf.format(cal.time).toString()
                    val tanggalID = dayConverter(monthConverter(tanggalEng))

                    val tz = TimeZone.getDefault()
                    val now = Date()
                    val timeZone: String = when ((tz.getOffset(now.time) / 3600000.0)) {
                        in 6.9..7.8 -> {
                            "WIB"
                        }
                        in 7.9..8.8 -> {
                            "WITA"
                        }
                        in 8.9..9.8 -> {
                            "WIT"
                        }
                        in -13.0..-0.1 -> {
                            "GMT" + (tz.getOffset(now.time) / 3600000.0).toString().replace(".0","")
                        }
                        else -> {
                            "GMT+" + (tz.getOffset(now.time) / 3600000.0).toString().replace(".0","")
                        }
                    }

                    val mcurrentTime = Calendar.getInstance()
                    val hour = mcurrentTime[Calendar.HOUR_OF_DAY]
                    val minute = mcurrentTime[Calendar.MINUTE]
                    val mTimePicker = TimePickerDialog(
                        requireContext(),
                        R.style.TimePickerTheme,
                        { _, selectedHour, selectedMinute -> waktu.setText(String.format(getString(R.string.format_waktu, tanggalID, selectedHour, selectedMinute, timeZone))) },
                        hour,
                        minute,
                        true
                    )
                    mTimePicker.show()
                }
                DatePickerDialog(
                    requireContext(),
                    R.style.TimePickerTheme,
                    dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }

            tvUpdate.setOnClickListener {
                val nomorTangkiText = endSpaceRemover(binding.noTangki.text.toString().uppercase())
                val lokasiSoundingText =  endSpaceRemover(binding.lokasiSounding.text.toString())
                val waktuText = waktu.text.toString()
                val tinggiCairanAngka = tinggiCairan.text.toString().toDouble()
                val suhuCairanAngka = suhuCairan.text.toString().toDouble()
                val suhuKalibrasiAngka = suhuTetap.text.toString().toDouble()
                val tinggiMejaAngka = tinggiMeja.text.toString().toDouble()
                val koefisienMuai = faktorMuai.text.toString().toDouble()
                val volumeKalibrasi = tabelKalibrasi.text.toString().toDouble()
                val densityAngka = densityCairan.text.toString().toDouble()
                val petugasSounding = namaPegawai.selectedItem.toString()
                val penggunaJasa = namaPenggunaJasa.selectedItem.toString()
                results = soundingCalculatorUpdate(binding)
                lifecycleScope.launch {
                    userDao.fetchServiceUserByName(penggunaJasa).collect { it3 ->
                        try {
                            val npwp = it3.npwp_perusahaan
                            val alamat = it3.alamat_perusahaan
                            val perusahaan = it3.perusahaan_pengguna_jasa
                            val jabatan = it3.jabatan
                            val judulKalibrasi1 = judulTabelKalibrasi.text.toString()
                            val judulKalibrasi2 =judulTabelKalibrasi2.text.toString()
                            val judulFraksi = judulTabelFraksi.text.toString()
                            val soundingCorrected = (tinggiCairan.text.toString().toDouble() + tinggiMeja.text.toString().toDouble()).toString()
                            val soundingCorrect = (round(soundingCorrected.toDouble())/1000.0)
                            val dataTabel = String.format(getString(R.string.data_tabel_edited), soundingCorrect.toString()).replace("Data Tabel (","").replace(")","").replace(".",",")
                            lifecycleScope.launch {
                                userDao.fetchUserByName(petugasSounding).collect {
                                    try {
                                        val nip = it.nip
                                        lifecycleScope.launch {
                                            userDao.updateSounding(SoundingEntity(id,
                                                tinggi_cairan = tinggiCairanAngka,
                                                suhu_cairan = suhuCairanAngka,
                                                suhu_kalibrasi_tangki = suhuKalibrasiAngka,
                                                tinggi_meja = tinggiMejaAngka,
                                                faktor_muai = koefisienMuai,
                                                volume_kalibrasi1 = volumeKalibrasi,
                                                density_cairan = densityAngka,
                                                tinggi_cairan_terkoreksi = results[0],
                                                volume_fraksi = results[1],
                                                volume_kalibrasi2 = results[2],
                                                volume_mid = results[3],
                                                volume_app = results[4],
                                                volume_obs = results[5],
                                                volume = results[6],
                                                hasil_sounding = results[7],
                                                no_tangki = nomorTangkiText,
                                                pegawai_sounding = petugasSounding,
                                                nip_pegawai = nip,
                                                pengguna_jasa_sounding = penggunaJasa,
                                                jabatan_pengguna_jasa = jabatan,
                                                perusahaan_sounding = perusahaan,
                                                npwp_perusahaan_sounding = npwp,
                                                alamat_perusahaan_sounding = alamat,
                                                lokasi_sounding = lokasiSoundingText,
                                                waktu = waktuText,
                                                waktu_date = waktuDate,
                                                judulKalibrasi1 = judulKalibrasi1,
                                                judulKalibrasi2 = judulKalibrasi2,
                                                judulFraksi = judulFraksi,
                                                judulDataTabel = dataTabel
                                            ))
                                            Toast.makeText(requireContext(), "Data Telah Tersimpan", Toast.LENGTH_SHORT).show()
                                            updateDialog.dismiss()
                                        }
                                    }  catch (e: Exception) {
                                        Log.d("okimatra", "" + e.message)
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.d("okimatra", "" + e.message)
                        }
                    }
                }
            }
        }
        updateDialog.show()
    }
    private fun deleteRecordAlertDialogSounding(id:Int,userDao: UserDao) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Hapus Data").setMessage("Apakah Anda yakin ingin menghapus data?")
        builder.setPositiveButton("Yes") { dialogInterface, _ ->
            lifecycleScope.launch {
                userDao.deleteSounding(SoundingEntity(id))
                Toast.makeText(context,"Data Berhasil Dihapus",Toast.LENGTH_SHORT).show()
                dialogInterface.dismiss()
            }
        }
        builder.setNegativeButton("No") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun setupListOfDataIntoRecyclerViewReport(reportList:ArrayList<ReportEntity>, userDao: UserDao) {
        _binding2?.soundingParent?.visibility = View.VISIBLE
        if (reportList.isNotEmpty()) {
            val reportAdapter = ReportAdapter(reportList,{deleteId->deleteRecordAlertDialogReport(deleteId,userDao)},{pdfId->pdfReportSounding(pdfId,userDao)})
            _binding2?.rvFinalList?.layoutManager = LinearLayoutManager(context)
            _binding2?.rvFinalList?.adapter = reportAdapter
            _binding2?.rvFinalList?.visibility = View.VISIBLE
            _binding2?.tvNoRawDataAvailable?.visibility = View.GONE
        } else {
            _binding2?.rvFinalList?.visibility = View.GONE
            _binding2?.tvNoRawDataAvailable?.visibility = View.VISIBLE
        }
    }
    private fun deleteRecordAlertDialogReport(id:Int,userDao: UserDao) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Hapus Data").setMessage("Apakah Anda yakin ingin menghapus data?")
        builder.setPositiveButton("Yes") { dialogInterface, _ ->
            lifecycleScope.launch {
                userDao.deleteReport(ReportEntity(id))
                Toast.makeText(context,"Data Berhasil Dihapus",Toast.LENGTH_SHORT).show()
                dialogInterface.dismiss()
            }
        }
        builder.setNegativeButton("No") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun populateDropdownUser(list:ArrayList<PegawaiEntity>, spinner: Spinner) {
        val items = arrayListOf<String>()
        if (list.isNotEmpty()) {
            for (i in 0 until list.size) {
                items.add(list[i].nama_pegawai)
            }
            val adapter = activity?.let { it ->
                ArrayAdapter(
                    it,
                    R.layout.dropdown_layout,
                    items
                )
            }
            spinner.adapter = adapter
        }
    }
    private fun populateDropdownKantor(list:ArrayList<KantorEntity>, spinner: Spinner) {
        val items = arrayListOf<String>()
        if (list.isNotEmpty()) {
            for (i in 0 until list.size) {
                items.add(list[i].kota)
            }
            val adapter = activity?.let { it ->
                ArrayAdapter(
                    it,
                    R.layout.dropdown_layout,
                    items
                )
            }
            spinner.adapter = adapter
        }
    }
    private fun populateDropdownCompany(list:ArrayList<PerusahaanEntity>, spinner: Spinner) {
        val items = arrayListOf<String>()
        if (list.isNotEmpty()) {
            for (i in 0 until list.size) {
                items.add(list[i].nama_perusahaan)
            }
            val adapter = activity?.let { it ->
                ArrayAdapter(
                    it,
                    R.layout.dropdown_layout,
                    items
                )
            }
            spinner.adapter = adapter
        }
    }
    private fun populateDropdownServiceUser(list:ArrayList<PenggunaJasaEntity>, spinner: Spinner) {
        val items = arrayListOf<String>()
        if (list.isNotEmpty()) {
            for (i in 0 until list.size) {
                items.add(list[i].nama_pengguna_jasa)
            }
            val adapter = ArrayAdapter(
                    requireContext(),
                    R.layout.dropdown_layout,
                    items
                )
            spinner.adapter = adapter
        }
    }
    private fun populateDropdownSoundingwithEmpty(list:ArrayList<SoundingEntity>, spinner: Spinner, out: Boolean) {
        val items = arrayListOf<String>()
        if (out) items.add("Empty Out; 0") else items.add("Empty In; 0")
        if (list.isNotEmpty()) {
            for (i in 0 until list.size) {
                items.add(list[i].no_tangki + "; ${monthCompress(list[i].waktu)}")
            }
        }
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.dropdown_layout,
            items
        )
        spinner.adapter = adapter
    }
    private fun populateDropdownEmptyOut(spinner: Spinner) {
        val items = arrayListOf("Empty Out; 0")
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.dropdown_layout,
            items
        )
        spinner.adapter = adapter
    }

    private fun emptyCheck(listEditText: List<AppCompatEditText>): Boolean{
        var checkResult = true
        for (editText in listEditText) {
            checkResult = checkResult && editText.text.toString().isNotEmpty()
        }
        return checkResult
    }
    private fun zeroRemover(text: String): String {
        return if (text.length > 1) {
            if (text.subSequence(text.length-2,text.length) == ".0") {
                (text.subSequence(0, text.length-2).toString())
            } else {
                text
            }
        } else {
            text
        }
    }
    private fun roundDigits(number: Double): Double {
        val number6digits = (number * 1000000).roundToLong()/1000000.toDouble()
        val number5digits = (number6digits * 100000).roundToLong()/100000.toDouble()
        val number4digits = (number5digits * 10000).roundToLong()/10000.toDouble()
        return (number4digits * 1000).roundToLong()/1000.toDouble()
    }

    private fun visibilityGone(listTextView: List<TextView>, listEditText: List<AppCompatEditText>) {
        for (editText in listEditText) {
            editText.visibility = View.GONE
        }
        for (textView in listTextView) {
            textView.visibility = View.GONE
        }
    }
    private fun visibilityVisible(listTextView: List<TextView>, listEditText: List<AppCompatEditText>) {
        for (editText in listEditText) {
            editText.visibility = View.VISIBLE
        }
        for (textView in listTextView) {
            textView.visibility = View.VISIBLE
        }
    }

    private fun dayConverter(date: String): String {
        return date.replace("Monday","Senin").replace("Tuesday","Selasa").replace("Wednesday","Rabu").replace("Thursday","Kamis").replace("Friday","Jumat").replace("Saturday","Sabtu").replace("Sunday","Minggu")
    }
    private fun monthConverter(date: String): String {
        return date.replace("January","Januari").replace("February","Februari").replace("March","Maret").replace("May","Mei").replace("June","Juni").replace("July","Juli").replace("August","Agustus").replace("October","Oktober").replace("December","December")
    }
    private fun monthCompress(date:String): String {
        return date.replace("Januari","01")
            .replace("Februari","02")
            .replace("Maret","03")
            .replace("April","04")
            .replace("Mei","05")
            .replace("Juni","06")
            .replace("Juli","07")
            .replace("Agustus","08")
            .replace("September","09")
            .replace("Oktober","10")
            .replace("November","11")
            .replace("Desember","12")
    }
    private fun monthExtract(date:String): String {
        return date.replace("-01-", "-Januari-")
            .replace("-02-","-Februari-")
            .replace("-03-","-Maret-")
            .replace("-04-","-April-")
            .replace("-05-","-Mei-")
            .replace("-06-","-Juni-")
            .replace("-07-","-Juli-")
            .replace("-08-","-Agustus-")
            .replace("-09-","-September-")
            .replace("-10-","-Oktober-")
            .replace("-11-","-November-")
            .replace("-12-","-Desember-")
    }

    private fun backFunction() {
        binding1.apply {
            dataAdministrasiLayout.visibility = View.GONE
            back.visibility = View.GONE
            simpanHasil.visibility = View.GONE
            dataLapanganLayout.visibility = View.VISIBLE
            dataTangkiLayout.visibility = View.VISIBLE
            dataTabelLayout.visibility = View.VISIBLE
            hasilLayout.visibility = View.VISIBLE
            next.visibility = View.VISIBLE
            Handler(Looper.getMainLooper()).postDelayed({
                scrollView.fullScroll(ScrollView.FOCUS_UP)
            }, 1)
        }
    }
}