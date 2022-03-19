package bc.okimatra.soundingcalculator

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import bc.okimatra.soundingcalculator.databinding.*
import bc.okimatra.soundingcalculator.datasetup.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round
import kotlin.math.roundToLong
import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.itextpdf.text.*
import com.itextpdf.text.pdf.*
import com.itextpdf.text.pdf.draw.LineSeparator
import com.itextpdf.text.pdf.draw.VerticalPositionMark
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import java.io.ByteArrayOutputStream
import java.lang.Exception

class TabFragment(private val title: String) : Fragment() {

    private var _binding1: FragmentOneBinding? = null
    private val binding1 get() = _binding1!!

    private var _binding2: FragmentTwoBinding? = null
    private val binding2 get() = _binding2!!

    private var _binding3: FragmentThreeBinding? = null
    private val binding3 get() = _binding3!!

    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener

    private val colorPrimary = BaseColor(40, 116, 240)
    private val fontSizeDefault = 12f
    private val fontSizeSmall = 8f
    private var basfontLight: BaseFont = BaseFont.createFont("res/font/helvetica.ttf", "UTF-8", BaseFont.EMBEDDED)
    private var appFontLight = Font(basfontLight, fontSizeSmall)
    private var basfontRegular: BaseFont = BaseFont.createFont("res/font/helvetica.ttf", "UTF-8", BaseFont.EMBEDDED)
    private var appFontRegular = Font(basfontRegular, fontSizeDefault)
    private var basfontSemiBold: BaseFont = BaseFont.createFont("res/font/helvetica.ttf", "UTF-8", BaseFont.EMBEDDED)
    private var appFontSemiBold = Font(basfontSemiBold, 24f)
    private var basfontBold: BaseFont = BaseFont.createFont("res/font/helvetica.ttf", "UTF-8", BaseFont.EMBEDDED)
    private var appFontBold = Font(basfontBold, fontSizeDefault)
    private val paddingEdge = 40f
    private val textTopPadding = 3f
    private val tableTopPadding = 10f
    private val textTopPaddingExtra = 30f
    private val billDetailsTopPadding = 80f
    private val data = ArrayList<ModelItems>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

                    interpolasiTab.setOnClickListener {
                        interpolasiTab.background = ResourcesCompat.getDrawable(resources, R.drawable.switch_on,null)
                        interpolasiTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.white))
                        fraksiTab.background = null
                        fraksiTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.login))
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
                        interpolasiTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.login))
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
                        if (hasilKalkulator.text.toString() != "Hasil: 0.000 MT") {
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
                                                    Toast.makeText(context, "Mohon Tambahkan Data Pengguna Jasa Terlebih Dahulu Pada Tab User", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    }
                                    else {
                                        Toast.makeText(context, "Mohon Tambahkan Data Pegawai Terlebih Dahulu Pada Tab User", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                        else {
                            Toast.makeText(context, "Mohon Cek Data, Nilai Hasil Masih 0", Toast.LENGTH_SHORT).show()
                        }
                    }

                    back.setOnClickListener {
                        backFunction()
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

                    val listETTinggi = listOf(tinggiCairan, tinggiMeja)
                    results = calculatorTinggiListener(listETTinggi)

                    val listET = listOf(suhuCairan, suhuTetap, muai, tabelFraksi, tabelKalibrasi, tabelKalibrasi2, densityCairan)
                    results = calculatorListener(listET)

                    simpanHasil.setOnClickListener {
                        val nomorTangkiText = endSpaceRemover(_binding1?.noTangki?.text.toString())
                        val lokasiSoundingText =  endSpaceRemover(_binding1?.lokasiSounding?.text.toString())
                        val waktuText = _binding1?.waktu?.text.toString()
                        val bentukText = endSpaceRemover(_binding1?.bentuk?.text.toString())
                        when {
                            nomorTangkiText.isEmpty() -> {
                                Toast.makeText(requireActivity(), "Nomor Tangki Belum Diisi", Toast.LENGTH_SHORT).show()
                            }
                            lokasiSoundingText.isEmpty() -> {
                                Toast.makeText(requireActivity(), "Lokasi Sounding Belum Diisi", Toast.LENGTH_SHORT).show()
                            }
                            waktuText.isEmpty() -> {
                                Toast.makeText(requireActivity(), "Waktu Sounding Belum Diisi", Toast.LENGTH_SHORT).show()
                            }
                            bentukText.isEmpty() -> {
                                Toast.makeText(requireActivity(), "Bentuk Fisik/Warna/Bau Belum Diisi", Toast.LENGTH_SHORT).show()
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
                                val nomorDokumen = endSpaceRemover(_binding1?.noDokumen?.text.toString())
                                val produk = endSpaceRemover(_binding1?.produk?.text.toString())
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
                                                                nomor_dokumen = nomorDokumen,
                                                                produk = produk,
                                                                bentuk = bentukText,
                                                                waktu_date = Date().time
                                                            ))
                                                            Toast.makeText(requireActivity(), "Data Telah Tersimpan", Toast.LENGTH_SHORT).show()
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
                                                            _binding1?.waktu?.text?.clear()
                                                            _binding1?.produk?.text?.clear()
                                                            _binding1?.bentuk?.text?.clear()
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
            title === "User" -> {
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
                                    pegawaiTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.login))
                                    perusahaanTab.background = null
                                    perusahaanTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.login))
                                    pegawaiLayout.visibility = View.GONE //NIP
                                    btnAddUser.visibility = View.GONE
                                    npwpLayout.visibility = View.GONE
                                    alamatLayout.visibility = View.GONE
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
                                    Toast.makeText(context, "Mohon Tambahkan Data Perusahaan Terlebih Dahulu Pada Tab Company", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }

                    pegawaiTab.setOnClickListener {
                        pegawaiTab.background = ResourcesCompat.getDrawable(resources, R.drawable.switch_on,null)
                        pegawaiTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.white))
                        penggunaJasaTab.background = null
                        penggunaJasaTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.login))
                        perusahaanTab.background = null
                        perusahaanTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.login))
                        npwpLayout.visibility = View.GONE
                        alamatLayout.visibility = View.GONE
                        btnAddCompany.visibility = View.GONE
                        penggunajasaLayout.visibility = View.GONE
                        btnAddPenggunaJasa.visibility = View.GONE
                        btnAddUser.visibility = View.VISIBLE
                        pegawaiLayout.visibility = View.VISIBLE
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
                        penggunaJasaTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.login))
                        pegawaiTab.background = null
                        pegawaiTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.login))
                        npwpLayout.visibility = View.VISIBLE
                        alamatLayout.visibility = View.VISIBLE
                        btnAddCompany.visibility = View.VISIBLE
                        penggunajasaLayout.visibility = View.GONE
                        btnAddPenggunaJasa.visibility = View.GONE
                        btnAddUser.visibility = View.GONE
                        pegawaiLayout.visibility = View.GONE
                        svServiceUserList.visibility = View.GONE
                        perusahaanLayout.visibility = View.GONE
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
                }

                _binding3?.btnAddCompany?.setOnClickListener {
                    addRecordCompany(userDao)
                }

                _binding3?.btnAddUser?.setOnClickListener {
                    addRecordUser(userDao)
                }

                _binding3?.btnAddPenggunaJasa?.setOnClickListener {
                    addRecordServiceUser(userDao)
                }
            }
            else -> {
                lifecycleScope.launch {
                    userDao.fetchAllSounding().collect {
                        Log.d("okimatra", "$it")
                        val list = ArrayList(it)
                        setupListOfDataIntoRecyclerViewSounding(list,userDao)
                    }
                }
                binding2.apply{
                    rawDataTab.setOnClickListener {
                        rawDataTab.background = ResourcesCompat.getDrawable(resources, R.drawable.switch_on,null)
                        rawDataTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.white))
                        finalTab.background = null
                        finalTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.login))
                        fabRawData.visibility = View.GONE
                        svSoundingList.visibility = View.VISIBLE
                        tvNoRecordsAvailable.visibility = View.GONE
                        svFinalList.visibility = View.GONE
                        Handler(Looper.getMainLooper()).postDelayed({
                            lifecycleScope.launch {
                                userDao.fetchAllSounding().collect {
                                    Log.d("exactcompanies", "$it")
                                    val list = ArrayList(it)
                                    setupListOfDataIntoRecyclerViewSounding(list,userDao)
                                }
                            }
                        }, 10)
                    }

                    finalTab.setOnClickListener {
                        finalTab.background = ResourcesCompat.getDrawable(resources, R.drawable.switch_on,null)
                        finalTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.white))
                        rawDataTab.background = null
                        rawDataTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.login))
                        fabRawData.visibility = View.VISIBLE
                        tvNoRecordsAvailable.visibility = View.VISIBLE
                        svSoundingList.visibility = View.GONE
                    }

                    initData()
                    generatePdf.setOnClickListener {
                        Dexter.withContext(requireActivity())
                            .withPermissions(
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ).withListener(object : MultiplePermissionsListener {
                                override fun onPermissionsChecked(report: MultiplePermissionsReport) {

                                    if (report.areAllPermissionsGranted()) {

                                        appFontRegular.color = BaseColor.WHITE
                                        appFontRegular.size = 10f
                                        val doc = Document(PageSize.A4, 0f, 0f, 0f, 0f)
                                        val outPath = requireActivity().getExternalFilesDir(null).toString() + "/my_invoice.pdf" //location where the pdf will store
                                        Log.d("loc", outPath)
                                        val writer = PdfWriter.getInstance(doc, FileOutputStream(outPath))
                                        doc.open()
                                        //Header Column Init with width nad no. of columns
                                        initInvoiceHeader(doc)
                                        doc.setMargins(0f, 0f, paddingEdge, paddingEdge)
                                        initBillDetails(doc)
                                        addLine(writer)
                                        initTableHeader(doc)
                                        initItemsTable(doc)
                                        initPriceDetails(doc)
                                        initFooter(doc)
                                        doc.close()

                                        val file = File(outPath)
                                        val path: Uri =FileProvider.getUriForFile(Objects.requireNonNull(activity!!.applicationContext),BuildConfig.APPLICATION_ID + ".provider", file)
//                                        val path: Uri = FileProvider.getUriForFile(requireActivity(),BuildConfig.APPLICATION_ID + ".provider",file)
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW)
                                            intent.setDataAndType(path, "application/pdf")
                                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            startActivity(intent)
                                        } catch (e: ActivityNotFoundException) {
                                            Toast.makeText(requireActivity(), "No PDF Viewer", Toast.LENGTH_SHORT).show()
                                        }


                                    } else {
                                        Toast.makeText(requireActivity(), "Permission Not Granted", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                override fun onPermissionRationaleShouldBeShown(
                                    permissions: List<PermissionRequest>,
                                    token: PermissionToken
                                ) {
                                    token.continuePermissionRequest()
                                }
                            }).check()
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

    private fun initFooter(doc: Document) {
        appFontRegular.color = colorPrimary
        val footerTable = PdfPTable(1)
        footerTable.totalWidth = PageSize.A4.width
        footerTable.isLockedWidth = true
        val thankYouCell =
            PdfPCell(Phrase("THANK YOU FOR YOUR BUSINESS", appFontRegular))
        thankYouCell.border = Rectangle.NO_BORDER
        thankYouCell.paddingLeft = paddingEdge
        thankYouCell.paddingTop = paddingEdge
        thankYouCell.horizontalAlignment = Rectangle.ALIGN_CENTER
        footerTable.addCell(thankYouCell)
        doc.add(footerTable)

    }

    private fun initData() {
        for (i in 1..15) {
            data.add(
                ModelItems(
                    "Item $i",
                    "Description $i",
                    (1..1000).random(),
                    (1234..123456).random(),
                    (1..99).random(),
                    (1234..132456).random()
                )
            )
        }
    }

    private fun initPriceDetails(doc: Document) {
        val priceDetailsTable = PdfPTable(2)
        priceDetailsTable.totalWidth = PageSize.A4.width
        priceDetailsTable.setWidths(floatArrayOf(5f, 2f))
        priceDetailsTable.isLockedWidth = true

        appFontRegular.color = colorPrimary
        val txtSubTotalCell = PdfPCell(Phrase("Sub Total : ", appFontRegular))
        txtSubTotalCell.border = Rectangle.NO_BORDER
        txtSubTotalCell.horizontalAlignment = Rectangle.ALIGN_RIGHT
        txtSubTotalCell.paddingTop = textTopPaddingExtra
        priceDetailsTable.addCell(txtSubTotalCell)
        appFontBold.color = BaseColor.BLACK
        val totalPriceCell = PdfPCell(Phrase("AED 12000", appFontBold))
        totalPriceCell.border = Rectangle.NO_BORDER
        totalPriceCell.horizontalAlignment = Rectangle.ALIGN_RIGHT
        totalPriceCell.paddingTop = textTopPaddingExtra
        totalPriceCell.paddingRight = paddingEdge
        priceDetailsTable.addCell(totalPriceCell)


        val txtTaxCell = PdfPCell(Phrase("Tax Total : ", appFontRegular))
        txtTaxCell.border = Rectangle.NO_BORDER
        txtTaxCell.horizontalAlignment = Rectangle.ALIGN_RIGHT
        txtTaxCell.paddingTop = textTopPadding
        priceDetailsTable.addCell(txtTaxCell)

        val totalTaxCell = PdfPCell(Phrase("AED 100", appFontBold))
        totalTaxCell.border = Rectangle.NO_BORDER
        totalTaxCell.horizontalAlignment = Rectangle.ALIGN_RIGHT
        totalTaxCell.paddingTop = textTopPadding
        totalTaxCell.paddingRight = paddingEdge
        priceDetailsTable.addCell(totalTaxCell)

        val txtTotalCell = PdfPCell(Phrase("TOTAL : ", appFontRegular))
        txtTotalCell.border = Rectangle.NO_BORDER
        txtTotalCell.horizontalAlignment = Rectangle.ALIGN_RIGHT
        txtTotalCell.paddingTop = textTopPadding
        txtTotalCell.paddingBottom = textTopPadding
        txtTotalCell.paddingLeft = paddingEdge
        priceDetailsTable.addCell(txtTotalCell)
        appFontBold.color = colorPrimary
        val totalCell = PdfPCell(Phrase("AED 12100", appFontBold))
        totalCell.border = Rectangle.NO_BORDER
        totalCell.horizontalAlignment = Rectangle.ALIGN_RIGHT
        totalCell.paddingTop = textTopPadding
        totalCell.paddingBottom = textTopPadding
        totalCell.paddingRight = paddingEdge
        priceDetailsTable.addCell(totalCell)

        doc.add(priceDetailsTable)
    }

    private fun initItemsTable(doc: Document) {
        val itemsTable = PdfPTable(5)
        itemsTable.isLockedWidth = true
        itemsTable.totalWidth = PageSize.A4.width
        itemsTable.setWidths(floatArrayOf(1.5f, 1f, 1f, .6f, 1.1f))

        for (item in data) {
            itemsTable.deleteBodyRows()

            val itemdetails = PdfPTable(1)
            val itemName = PdfPCell(Phrase(item.itemName, appFontRegular))
            itemName.border = Rectangle.NO_BORDER
            val itemDesc = PdfPCell(Phrase(item.itemDesc, appFontLight))
            itemDesc.border = Rectangle.NO_BORDER
            itemdetails.addCell(itemName)
            itemdetails.addCell(itemDesc)
            val itemCell = PdfPCell(itemdetails)
            itemCell.border = Rectangle.NO_BORDER
            itemCell.paddingTop = tableTopPadding
            itemCell.paddingLeft = paddingEdge
            itemsTable.addCell(itemCell)


            val quantityCell = PdfPCell(Phrase(item.quantity.toString(), appFontRegular))
            quantityCell.border = Rectangle.NO_BORDER
            quantityCell.horizontalAlignment = Rectangle.ALIGN_RIGHT
            quantityCell.paddingTop = tableTopPadding
            itemsTable.addCell(quantityCell)

            val disAmount = PdfPCell(Phrase("AED ${item.disAmount}", appFontRegular))
            disAmount.border = Rectangle.NO_BORDER
            disAmount.horizontalAlignment = Rectangle.ALIGN_RIGHT
            disAmount.paddingTop = tableTopPadding
            itemsTable.addCell(disAmount)

            val vat = PdfPCell(Phrase(item.vat.toString(), appFontRegular))
            vat.border = Rectangle.NO_BORDER
            vat.horizontalAlignment = Rectangle.ALIGN_RIGHT
            vat.paddingTop = tableTopPadding
            itemsTable.addCell(vat)

            val netAmount = PdfPCell(Phrase("AED ${item.netAmount}", appFontRegular))
            netAmount.horizontalAlignment = Rectangle.ALIGN_RIGHT
            netAmount.border = Rectangle.NO_BORDER
            netAmount.paddingTop = tableTopPadding
            netAmount.paddingRight = paddingEdge
            itemsTable.addCell(netAmount)
            doc.add(itemsTable)
        }
    }

    private fun initTableHeader(doc: Document) {

        doc.add(Paragraph("\n\n\n\n\n")) //adds blank line to place table header below the line

        val titleTable = PdfPTable(5)
        titleTable.isLockedWidth = true
        titleTable.totalWidth = PageSize.A4.width
        titleTable.setWidths(floatArrayOf(1.5f, 1f, 1f, .6f, 1.1f))
        appFontBold.color = colorPrimary

        val itemCell = PdfPCell(Phrase("Description", appFontBold))
        itemCell.border = Rectangle.NO_BORDER
        itemCell.paddingTop = tableTopPadding
        itemCell.paddingBottom = tableTopPadding
        itemCell.paddingLeft = paddingEdge
        titleTable.addCell(itemCell)


        val quantityCell = PdfPCell(Phrase("Quantity", appFontBold))
        quantityCell.border = Rectangle.NO_BORDER
        quantityCell.horizontalAlignment = Rectangle.ALIGN_RIGHT
        quantityCell.paddingBottom = tableTopPadding
        quantityCell.paddingTop = tableTopPadding
        titleTable.addCell(quantityCell)

        val disAmount = PdfPCell(Phrase("DIS. Amount", appFontBold))
        disAmount.border = Rectangle.NO_BORDER
        disAmount.horizontalAlignment = Rectangle.ALIGN_RIGHT
        disAmount.paddingBottom = tableTopPadding
        disAmount.paddingTop = tableTopPadding
        titleTable.addCell(disAmount)

        val vat = PdfPCell(Phrase("VAT %", appFontBold))
        vat.border = Rectangle.NO_BORDER
        vat.horizontalAlignment = Rectangle.ALIGN_RIGHT
        vat.paddingBottom = tableTopPadding
        vat.paddingTop = tableTopPadding
        titleTable.addCell(vat)

        val netAmount = PdfPCell(Phrase("Net Amount", appFontBold))
        netAmount.horizontalAlignment = Rectangle.ALIGN_RIGHT
        netAmount.border = Rectangle.NO_BORDER
        netAmount.paddingTop = tableTopPadding
        netAmount.paddingBottom = tableTopPadding
        netAmount.paddingRight = paddingEdge
        titleTable.addCell(netAmount)
        doc.add(titleTable)
    }

    private fun addLine(writer: PdfWriter) {
        val canvas: PdfContentByte = writer.directContent
        canvas.setColorStroke(colorPrimary)
        canvas.moveTo(40.0, 480.0)

        // Drawing the line
        canvas.lineTo(560.0, 480.0)
        canvas.setLineWidth(3f)

        // Closing the path stroke
        canvas.closePathStroke()
    }

    private fun initBillDetails(doc: Document) {
        val billDetailsTable =
            PdfPTable(3)  // table to show customer address, invoice, date and total amount
        billDetailsTable.setWidths(
            floatArrayOf(
                2f,
                1.82f,
                2f
            )
        )
        billDetailsTable.isLockedWidth = true
        billDetailsTable.paddingTop = 30f

        billDetailsTable.totalWidth =
            PageSize.A4.width // set content width to fill document
        val customerAddressTable = PdfPTable(1)
        appFontRegular.color = BaseColor.GRAY
        appFontRegular.size = 8f
        val txtBilledToCell = PdfPCell(
            Phrase(
                "Billed To",
                appFontLight
            )
        )
        txtBilledToCell.border = Rectangle.NO_BORDER
        customerAddressTable.addCell(
            txtBilledToCell
        )
        appFontRegular.size = fontSizeDefault
        appFontRegular.color = BaseColor.BLACK
        val clientAddressCell1 = PdfPCell(
            Paragraph(
                "Sreehari K",
                appFontRegular
            )
        )
        clientAddressCell1.border = Rectangle.NO_BORDER
        clientAddressCell1.paddingTop = textTopPadding
        customerAddressTable.addCell(clientAddressCell1)

        val clientAddressCell2 = PdfPCell(
            Paragraph(
                "Address Line 1",
                appFontRegular
            )
        )
        clientAddressCell2.border = Rectangle.NO_BORDER
        clientAddressCell2.paddingTop = textTopPadding
        customerAddressTable.addCell(clientAddressCell2)


        val clientAddressCell3 = PdfPCell(
            Paragraph(
                "Address Line 2",
                appFontRegular
            )
        )
        clientAddressCell3.border = Rectangle.NO_BORDER
        clientAddressCell3.paddingTop = textTopPadding
        customerAddressTable.addCell(clientAddressCell3)


        val clientAddressCell4 = PdfPCell(
            Paragraph(
                "Address Line 3",
                appFontRegular
            )
        )
        clientAddressCell4.border = Rectangle.NO_BORDER
        clientAddressCell4.paddingTop = textTopPadding
        customerAddressTable.addCell(clientAddressCell4)

        val billDetailsCell1 = PdfPCell(customerAddressTable)
        billDetailsCell1.border = Rectangle.NO_BORDER

        billDetailsCell1.paddingTop = billDetailsTopPadding

        billDetailsCell1.paddingLeft = paddingEdge

        billDetailsTable.addCell(billDetailsCell1)


        val invoiceNumAndData = PdfPTable(1)
        appFontRegular.color = BaseColor.LIGHT_GRAY
        appFontRegular.size = 8f
        val txtInvoiceNumber = PdfPCell(Phrase("Invoice Number", appFontLight))
        txtInvoiceNumber.paddingTop = billDetailsTopPadding
        txtInvoiceNumber.border = Rectangle.NO_BORDER
        invoiceNumAndData.addCell(txtInvoiceNumber)
        appFontRegular.color = BaseColor.BLACK
        appFontRegular.size = 12f
        val invoiceNumber = PdfPCell(Phrase("BMC00${(1234..9879).random()}", appFontRegular))
        invoiceNumber.border = Rectangle.NO_BORDER
        invoiceNumber.paddingTop = textTopPadding
        invoiceNumAndData.addCell(invoiceNumber)

        appFontRegular.color = BaseColor.LIGHT_GRAY
        appFontRegular.size = 5f
        val txtDate = PdfPCell(Phrase("Date Of Issue", appFontLight))
        txtDate.paddingTop = textTopPaddingExtra
        txtDate.border = Rectangle.NO_BORDER
        invoiceNumAndData.addCell(txtDate)

        appFontRegular.color = BaseColor.BLACK
        appFontRegular.size = fontSizeDefault
        val dateCell = PdfPCell(Phrase("04/11/2019", appFontRegular))
        dateCell.border = Rectangle.NO_BORDER
        invoiceNumAndData.addCell(dateCell)

        val dataInvoiceNumAndData = PdfPCell(invoiceNumAndData)
        dataInvoiceNumAndData.border = Rectangle.NO_BORDER
        billDetailsTable.addCell(dataInvoiceNumAndData)

        val totalPriceTable = PdfPTable(1)
        val txtInvoiceTotal = PdfPCell(Phrase("Invoice Total", appFontLight))
        txtInvoiceTotal.paddingTop = billDetailsTopPadding
        txtInvoiceTotal.horizontalAlignment = Rectangle.ALIGN_RIGHT
        txtInvoiceTotal.border = Rectangle.NO_BORDER
        totalPriceTable.addCell(txtInvoiceTotal)

        appFontSemiBold.color = colorPrimary
        val totalAomountCell = PdfPCell(Phrase("AED ${(111..21398).random()}", appFontSemiBold))
        totalAomountCell.border = Rectangle.NO_BORDER
        totalAomountCell.horizontalAlignment = Rectangle.ALIGN_RIGHT
        totalPriceTable.addCell(totalAomountCell)
        val dataTotalAmount = PdfPCell(totalPriceTable)
        dataTotalAmount.border = Rectangle.NO_BORDER
        dataTotalAmount.paddingRight = paddingEdge
        dataTotalAmount.verticalAlignment = Rectangle.ALIGN_BOTTOM

        billDetailsTable.addCell(dataTotalAmount)
        doc.add(billDetailsTable)
    }

    private fun initInvoiceHeader(doc: Document) {
        val d = ContextCompat.getDrawable(requireContext(),R.drawable.gear)
        val bitDw = d as BitmapDrawable
        val bmp = bitDw.bitmap
        val stream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val image = Image.getInstance(stream.toByteArray())
        val headerTable = PdfPTable(3)
        headerTable.setWidths(
            floatArrayOf(
                1.3f,
                1f,
                1f
            )
        ) // adds 3 colomn horizontally
        headerTable.isLockedWidth = true
        headerTable.totalWidth = PageSize.A4.width // set content width to fill document
        val cell = PdfPCell(Image.getInstance(image)) // Logo Cell
        cell.border = Rectangle.NO_BORDER // Removes border
        cell.paddingTop = textTopPaddingExtra // sets padding
        cell.paddingRight = tableTopPadding
        cell.paddingLeft = paddingEdge
        cell.horizontalAlignment = Rectangle.ALIGN_LEFT
        cell.paddingBottom = textTopPaddingExtra

        cell.backgroundColor = colorPrimary // sets background color
        cell.horizontalAlignment = Element.ALIGN_CENTER
        headerTable.addCell(cell) // Adds first cell with logo

        val contactTable =
            PdfPTable(1) // new vertical table for contact details
        val phoneCell =
            PdfPCell(
                Paragraph(
                    "+91 8547984369",
                    appFontRegular
                )
            )
        phoneCell.border = Rectangle.NO_BORDER
        phoneCell.horizontalAlignment = Element.ALIGN_RIGHT
        phoneCell.paddingTop = textTopPadding

        contactTable.addCell(phoneCell)

        val emailCellCell = PdfPCell(Phrase("sreeharikariot@gmail.com", appFontRegular))
        emailCellCell.border = Rectangle.NO_BORDER
        emailCellCell.horizontalAlignment = Element.ALIGN_RIGHT
        emailCellCell.paddingTop = textTopPadding

        contactTable.addCell(emailCellCell)

        val webCell = PdfPCell(Phrase("www.kariot.me", appFontRegular))
        webCell.border = Rectangle.NO_BORDER
        webCell.paddingTop = textTopPadding
        webCell.horizontalAlignment = Element.ALIGN_RIGHT

        contactTable.addCell(webCell)


        val headCell = PdfPCell(contactTable)
        headCell.border = Rectangle.NO_BORDER
        headCell.horizontalAlignment = Element.ALIGN_RIGHT
        headCell.verticalAlignment = Element.ALIGN_MIDDLE
        headCell.backgroundColor = colorPrimary
        headerTable.addCell(headCell)

        val address = PdfPTable(1)
        val line1 = PdfPCell(
            Paragraph(
                "Address Line 1",
                appFontRegular
            )
        )
        line1.border = Rectangle.NO_BORDER
        line1.paddingTop = textTopPadding
        line1.horizontalAlignment = Element.ALIGN_RIGHT

        address.addCell(line1)

        val line2 = PdfPCell(Paragraph("Address Line 2", appFontRegular))
        line2.border = Rectangle.NO_BORDER
        line2.paddingTop = textTopPadding
        line2.horizontalAlignment = Element.ALIGN_RIGHT

        address.addCell(line2)

        val line3 = PdfPCell(Paragraph("Address Line 3", appFontRegular))
        line3.border = Rectangle.NO_BORDER
        line3.paddingTop = textTopPadding
        line3.horizontalAlignment = Element.ALIGN_RIGHT

        address.addCell(line3)


        val addressHeadCell = PdfPCell(address)
        addressHeadCell.border = Rectangle.NO_BORDER
        addressHeadCell.setLeading(22f, 25f)
        addressHeadCell.horizontalAlignment = Element.ALIGN_RIGHT
        addressHeadCell.verticalAlignment = Element.ALIGN_MIDDLE
        addressHeadCell.backgroundColor = colorPrimary
        addressHeadCell.paddingRight = paddingEdge
        headerTable.addCell(addressHeadCell)

        doc.add(headerTable)
    }

    @Throws(DocumentException::class)
    private fun addNewItem(document: Document, text: String, align: Int, font: Font) {
        val chunk = Chunk(text, font)
        val paragraph = Paragraph(chunk)
        paragraph.alignment = align
        document.add(paragraph)
    }

    @Throws(DocumentException::class)
    private fun addNewItemWithLeftAndRight(document: Document, textLeft: String, textRight: String, textLeftFont: Font, textRightFont: Font) {
        val chunkTextLeft = Chunk(textLeft, textLeftFont)
        val chunkTextRight = Chunk(textRight, textRightFont)
        val p = Paragraph(chunkTextLeft)
        p.add(Chunk(VerticalPositionMark()))
        p.add(chunkTextRight)
        document.add(p)
    }

    @Throws(DocumentException::class)
    private fun addLineSeparator(document: Document) {
        val lineSeparator = LineSeparator()
        lineSeparator.lineColor = BaseColor(0, 0, 0, 68)
        addLineSpace(document)
        document.add(Chunk(lineSeparator))
    }

    @Throws(DocumentException::class)
    private fun addLineSpace(document: Document) {
        document.add(Paragraph(""))
    }

    private fun printPDF() {
        val printManager = requireActivity().getSystemService(AppCompatActivity.PRINT_SERVICE) as PrintManager
        try {
            val printDocumentAdapter: PrintDocumentAdapter = PdfDocumentAdapter(Common.getAppPath(requireActivity()) + "test_pdf.pdf")
            printManager.print("Document", printDocumentAdapter, PrintAttributes.Builder().build())
        } catch (e: Exception) {
            Log.e("okimatra", "" + e.message)
            Toast.makeText(requireActivity(), "Can't read pdf file", Toast.LENGTH_SHORT).show()
        }
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
                        delta =
                            ((tinggiCairan.text.toString().toDouble() + tinggiMeja.text.toString()
                                .toDouble()) / 1000 - judulTabelKalibrasi.text.toString()
                                .subSequence(
                                    judulTabelKalibrasi.text.indexOf("(") + 1,
                                    judulTabelKalibrasi.text.indexOf(")") - 2
                                ).toString().toDouble()) / 0.01
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

    private fun resetResulUpdate(binding: DialogUpdateSoundingBinding) {
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
                        resetResulUpdate(binding)
                    }
                }
            } else {
                resetResulUpdate(binding)
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

    private fun dayConverter(date: String): String {
        return date.replace("Monday","Senin").replace("Tuesday","Selasa").replace("Wednesday","Rabu").replace("Thursday","Kamis").replace("Friday","Jumat").replace("Saturday","Sabtu").replace("Sunday","Minggu")
    }

    private fun monthConverter(date: String): String {
        return date.replace("January","Januari").replace("February","Februari").replace("March","Maret").replace("May","Mei").replace("June","Juni").replace("July","Juli").replace("August","Agustus").replace("October","Oktober").replace("December","December")
    }

    private fun roundDigits(number: Double): Double {
        val number6digits = (number * 1000000).roundToLong()/1000000.toDouble()
        val number5digits = (number6digits * 100000).roundToLong()/100000.toDouble()
        val number4digits = (number5digits * 10000).roundToLong()/10000.toDouble()
        return (number4digits * 1000).roundToLong()/1000.toDouble()
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

    private fun setupListOfUserDataIntoRecyclerView(employeesList:ArrayList<PegawaiEntity>, userDao: UserDao) {
        if (employeesList.isNotEmpty()) {
            val itemAdapter = PegawaiAdapter(employeesList,{ updateId ->
                updateRecordDialogUser(updateId,userDao)
            }){deleteId->deleteRecordAlertDialogUser(deleteId,userDao)
            }
            _binding3?.rvUserList?.layoutManager = LinearLayoutManager(requireActivity())
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
                Toast.makeText(context, "Mohon Periksa Tahun Lahir dan Tahun Penerimaan PNS Anda", Toast.LENGTH_SHORT).show()
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
                    userDao.insertUser(PegawaiEntity(nama_pegawai = name, nip = nip))
                    Toast.makeText(context, "Data Berhasil Disimpan", Toast.LENGTH_SHORT).show()
                    _binding3?.nama?.text?.clear()
                    _binding3?.nip?.text?.clear()
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
                    Toast.makeText(context, "Mohon Periksa Tahun Lahir dan Tahun Penerimaan PNS Anda", Toast.LENGTH_SHORT).show()
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
                        userDao.updateUser(PegawaiEntity(id, name, nip))
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
                fraksiTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.login))
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
                interpolasiTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.login))
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
                        noDokumen.setText(it1.nomor_dokumen)
                        produk.setText(it1.produk)
                        bentuk.setText(it1.bentuk)
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
                    visibilityVisible(listOf(judulNoTangki,judulNamaPegawai,judulNamaPenggunaJasa,judulLokasiSounding,tvNext2,tvBack1), listOf(noTangki,lokasiSounding))
                    namaPegawai.visibility = View.VISIBLE
                    namaPenggunaJasa.visibility = View.VISIBLE
                } else {
                    Toast.makeText(context, "Mohon Cek Kelengkapan Data", Toast.LENGTH_SHORT).show()
                }
            }

            tvBack1.setOnClickListener {
                visibilityGone(listOf(judulNoTangki,judulNamaPegawai,judulNamaPenggunaJasa,judulLokasiSounding,tvNext2,tvBack1), listOf(noTangki,lokasiSounding))
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

            tvNext2.setOnClickListener {
                if (emptyCheck(listOf(noTangki,lokasiSounding))) {
                    visibilityGone(listOf(judulNoTangki,judulNamaPegawai,judulNamaPenggunaJasa,judulLokasiSounding,tvNext2,tvBack1), listOf(noTangki,lokasiSounding))
                    namaPegawai.visibility = View.GONE
                    namaPenggunaJasa.visibility = View.GONE
                    visibilityVisible(listOf(tvUpdate,tvBack2, judulWaktu, judulNoDokumen, judulProduk,judulBentuk), listOf(waktu,noDokumen,produk,bentuk))
                } else {
                    Toast.makeText(context, "Mohon Cek Data Kembali", Toast.LENGTH_SHORT).show()
                }
            }

            tvBack2.setOnClickListener {
                visibilityGone(listOf(tvUpdate,tvBack2, judulWaktu, judulNoDokumen, judulProduk,judulBentuk), listOf(waktu,noDokumen,produk,bentuk))
                visibilityVisible(listOf(judulNoTangki,judulNamaPegawai,judulNamaPenggunaJasa,judulLokasiSounding,tvNext2,tvBack1), listOf(noTangki,lokasiSounding))
                namaPegawai.visibility = View.VISIBLE
                namaPenggunaJasa.visibility = View.VISIBLE
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
                val nomorTangkiText = endSpaceRemover(binding.noTangki.text.toString())
                val lokasiSoundingText =  endSpaceRemover(binding.lokasiSounding.text.toString())
                val waktuText = binding.waktu.text.toString()
                val bentukText = endSpaceRemover(binding.bentuk.text.toString())
                val tinggiCairanAngka = binding.tinggiCairan.text.toString().toDouble()
                val suhuCairanAngka = binding.suhuCairan.text.toString().toDouble()
                val suhuKalibrasiAngka = binding.suhuTetap.text.toString().toDouble()
                val tinggiMejaAngka = binding.tinggiMeja.text.toString().toDouble()
                val koefisienMuai = binding.faktorMuai.text.toString().toDouble()
                val volumeKalibrasi = binding.tabelKalibrasi.text.toString().toDouble()
                val densityAngka = binding.densityCairan.text.toString().toDouble()
                val petugasSounding = binding.namaPegawai.selectedItem.toString()
                val penggunaJasa = binding.namaPenggunaJasa.selectedItem.toString()
                val nomorDokumen = endSpaceRemover(binding.noDokumen.text.toString())
                val produk = endSpaceRemover(binding.produk.text.toString())
                results = soundingCalculatorUpdate(binding)
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
                                                nomor_dokumen = nomorDokumen,
                                                produk = produk,
                                                bentuk = bentukText,
                                                waktu_date = waktuDate
                                            ))
                                            Toast.makeText(requireActivity(), "Data Telah Tersimpan", Toast.LENGTH_SHORT).show()
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
            val companyAdapter = PerusahaanAdapter(perusahaanList, { updateId ->updateRecordDialogCompany(updateId,userDao)}) {deleteId->deleteRecordAlertDialogCompany(deleteId,userDao)}
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

    private fun setupListOfDataIntoRecyclerViewSounding(soundingList:ArrayList<SoundingEntity>, userDao: UserDao) {
        if (soundingList.isNotEmpty()) {
            val soundingAdapter = SoundingAdapter(soundingList,{updateId->updateRecordDialogSounding(updateId,userDao)},{deleteId->deleteRecordAlertDialogSounding(deleteId,userDao)},{pdfId->pdfRecordAlertDialogSounding(pdfId,userDao)})
            _binding2?.rvSoundingList?.layoutManager = LinearLayoutManager(context)
            _binding2?.rvSoundingList?.adapter = soundingAdapter
            _binding2?.svSoundingList?.visibility = View.VISIBLE
            _binding2?.tvNoRecordsAvailable?.visibility = View.GONE
        } else {
            _binding2?.svSoundingList?.visibility = View.GONE
            _binding2?.tvNoRecordsAvailable?.visibility = View.VISIBLE
        }
    }

    private fun pdfRecordAlertDialogSounding(id: Int, userDao: UserDao) {
        lifecycleScope.launch {
            userDao.fetchSoundingById(id).collect {
                try {
                    Dexter.withContext(requireActivity())
                        .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .withListener(object : PermissionListener {
                            override fun onPermissionGranted(permissionGrantedResponse: PermissionGrantedResponse) {
                                val path =Common.getAppPath(requireActivity()) + "test_pdf.pdf"
                                val date = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
                                if (File(path).exists()) File(path).delete()
                                try {
                                    val document = Document()
                                    //Save
                                    PdfWriter.getInstance(document, FileOutputStream(path))
                                    //open to write
                                    document.open()
                                    //Settings
                                    document.pageSize = PageSize.A4.rotate()
                                    document.addCreationDate()
                                    document.addAuthor("okimatra")
                                    document.addCreator("okimatra")

                                    //Font Settings
                                    val colorAccent = BaseColor(0, 153, 204, 255)
                                    val fontSizeHeader = 20.0f
                                    val valueFontSize = 26.0f

                                    //Custom font
                                    val fontName = BaseFont.createFont("res/font/helvetica.ttf", "UTF-8", BaseFont.EMBEDDED)

                                    //create title of document
                                    val titleFont = Font(fontName, 20.0f, Font.NORMAL, BaseColor.BLACK)
                                    addNewItem(document, "Laporan Hitung Barang Curah Bea Cukai", Element.ALIGN_CENTER, titleFont)

                                    // Add more
                                    val orderNumberFont = Font(fontName, fontSizeHeader, Font.NORMAL, colorAccent)
                                    addNewItem(document, "order number", Element.ALIGN_LEFT, orderNumberFont)
                                    val orderNumberValueFont = Font(fontName, valueFontSize, Font.NORMAL, BaseColor.BLACK)
                                    addNewItem(document, "#525263", Element.ALIGN_LEFT, orderNumberValueFont)
                                    addLineSeparator(document)
                                    addNewItem(document, "Order Date", Element.ALIGN_LEFT, orderNumberFont)
                                    addNewItem(document, date, Element.ALIGN_LEFT, orderNumberValueFont)
                                    addLineSeparator(document)
                                    addNewItem(document, "Account name", Element.ALIGN_LEFT, orderNumberFont)
                                    addNewItem(document, it.pengguna_jasa_sounding, Element.ALIGN_LEFT, orderNumberValueFont)
                                    addLineSeparator(document)

                                    //Add product order detail
                                    addLineSpace(document)
                                    addNewItem(document, "Product details", Element.ALIGN_CENTER, titleFont)
                                    addLineSeparator(document)

                                    //item 1
                                    addNewItemWithLeftAndRight(
                                        document,
                                        "Burger",
                                        "(1.0%)",
                                        titleFont,
                                        orderNumberValueFont
                                    )
                                    addNewItemWithLeftAndRight(document, "20", "1200.0", titleFont, orderNumberValueFont)
                                    addLineSeparator(document)

                                    //item 2
                                    addNewItemWithLeftAndRight(document, "Pizza", "(0.0%)", titleFont, orderNumberValueFont)
                                    addNewItemWithLeftAndRight(document, "12", "1520.0", titleFont, orderNumberValueFont)
                                    addLineSeparator(document)

                                    //item 3
                                    addNewItemWithLeftAndRight(
                                        document,
                                        "Sandwich",
                                        "(0.0%)",
                                        titleFont,
                                        orderNumberValueFont
                                    )
                                    addNewItemWithLeftAndRight(document, "10", "1000.0", titleFont, orderNumberValueFont)
                                    addLineSeparator(document)

                                    //Total
                                    addLineSpace(document)
                                    addLineSpace(document)
                                    addNewItemWithLeftAndRight(document, "total", "8500", titleFont, orderNumberValueFont)
                                    document.close()
                                    printPDF()
                                } catch (e: FileNotFoundException) {
                                    e.printStackTrace()
                                }
                            }

                            override fun onPermissionDenied(permissionDeniedResponse: PermissionDeniedResponse) {}
                            override fun onPermissionRationaleShouldBeShown(
                                permissionRequest: PermissionRequest,
                                permissionToken: PermissionToken
                            ) {
                            }
                        })
                        .check()
                } catch (e: Exception) {
                    Log.d("okimara", "" + e.message)
                }
            }
        }
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

    private fun emptyCheck(listEditText: List<AppCompatEditText>): Boolean{
        var checkResult = true
        for (editText in listEditText) {
            checkResult = checkResult && editText.text.toString().isNotEmpty()
        }
        return checkResult
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
}