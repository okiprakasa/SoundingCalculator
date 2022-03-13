package bc.okimatra.soundingcalculator

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import bc.okimatra.soundingcalculator.databinding.*
import bc.okimatra.soundingcalculator.datasetup.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.DecimalFormat
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

    private var _binding4: FragmentFourBinding? = null
    private val binding4 get() = _binding4!!

    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        retainInstance = true
        return when {
            title === "Calculator" -> {
                _binding1 = FragmentOneBinding.inflate(inflater, container, false)
                binding1.root
            }
            title === "Data" -> {
                _binding2 = FragmentTwoBinding.inflate(inflater, container, false)
                binding2.root
            }
            title === "User" -> {
                _binding3 = FragmentThreeBinding.inflate(inflater, container, false)
                binding3.root
            }
            else -> {
                _binding4 = FragmentFourBinding.inflate(inflater, container, false)
                binding4.root
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when {
            title === "Calculator" -> {
                @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
                var results: List<Double>
                val userDao = (requireActivity().application as UserApp).db.userDao()
                dateSetListener = DatePickerDialog.OnDateSetListener {
                        _, year, month, dayOfMonth ->
                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.MONTH, month)
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    val timeID = "EE, dd-MMM-yyyy"
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
                        { _, selectedHour, selectedMinute -> binding1.waktu.setText(String.format(getString(R.string.format_waktu, tanggalID, selectedHour, selectedMinute, timeZone))) },
                        hour,
                        minute,
                        true
                    )
//                    mTimePicker.setTitle("Pilih Waktu")
                    mTimePicker.show()
                }

                binding1.apply{
                    waktu.setOnClickListener {
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
                        if (calculatorCheck()) {
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
                            Toast.makeText(context, "Mohon Cek Data Kembali, Nilai Hasil Masih 0", Toast.LENGTH_SHORT).show()
                        }
                    }

                    back.setOnClickListener {
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

                    simpanHasil.setOnClickListener {
                        if (hasilKalkulator.text.toString() != "Hasil : 0.000 MT") {
                            suhuCairan.text = null
                            suhuTetap.text = null
                            tinggiMeja.text = null
                            muai.text = null
                            densityCairan.text = null
                            tinggiCairan.text = null
                            tabelFraksi.text = null
                            tabelKalibrasi.text = null
                            tabelKalibrasi2.text = null
                            noTangki.text = null
                            waktu.text = null
                            noDokumen.text = null
                            produk.text = null
                            bentuk.text = null
                            Toast.makeText(requireActivity(), "Data Telah Tersimpan", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireActivity(), "Tidak Ada Data", Toast.LENGTH_SHORT).show()
                        }
                    }

                    val listETTinggi = listOf(tinggiCairan, tinggiMeja)
                    results = calculatorTinggiListener(listETTinggi)

                    val listET = listOf(suhuCairan, suhuTetap, muai, tabelFraksi, tabelKalibrasi, tabelKalibrasi2, densityCairan)
                    results = calculatorListener(listET)
                }
            }
            title === "User" -> {
                val userDao = (requireActivity().application as UserApp).db.userDao()
                lifecycleScope.launch {
                    userDao.fetchAllUser().collect {
                        val list = ArrayList(it)
                        setupListOfUserDataIntoRecyclerView(list,userDao)
                    }
                }

                binding3.apply {

                    penggunaJasaTab.setOnClickListener {
                        lifecycleScope.launch {
                            userDao.countAllCompany().collect { it1 ->
                                if (it1>0) {
                                    penggunaJasaTab.background = ResourcesCompat.getDrawable(resources, R.drawable.switch_on,null)
                                    penggunaJasaTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.white))
                                    pegawaiTab.background = null
                                    pegawaiTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.login))
                                    penggunajasaLayout.visibility = View.VISIBLE
                                    btnAddUser.visibility = View.GONE
                                    btnAddPenggunaJasa.visibility = View.VISIBLE
                                    pegawaiLayout.visibility = View.GONE
                                    perusahaanLayout.visibility = View.VISIBLE
                                    nip.text = null
                                    nama.hint = getText(R.string.hint_pengguna_jasa)
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
                                    Toast.makeText(context, "Mohon Tambahkan Data Perusahaan Terlebih Dahulu Pada Tab Companies", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                    }

                    pegawaiTab.setOnClickListener {
                        pegawaiTab.background = ResourcesCompat.getDrawable(resources, R.drawable.switch_on,null)
                        pegawaiTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.white))
                        penggunaJasaTab.background = null
                        penggunaJasaTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.login))
                        btnAddUser.visibility = View.VISIBLE
                        btnAddPenggunaJasa.visibility = View.GONE
                        pegawaiLayout.visibility = View.VISIBLE
                        penggunajasaLayout.visibility = View.GONE
                        jabatan.text = null
                        nama.hint = getText(R.string.hint_nama)
                        svUserList.visibility = View.VISIBLE
                        svServiceUserList.visibility = View.GONE
                        perusahaanLayout.visibility = View.GONE
                    }
                }

                _binding3?.btnAddUser?.setOnClickListener {
                    addRecordUser(userDao)
                }

                _binding3?.btnAddPenggunaJasa?.setOnClickListener {
                    addRecordServiceUser(userDao)
                }

            }
            title === "Company" -> {
                val userDao = (requireActivity().application as UserApp).db.userDao()
                
                binding4.apply {

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
                }

                _binding4?.btnAdd?.setOnClickListener {
                    addRecordCompany(userDao)
                }

                lifecycleScope.launch {
                    userDao.fetchAllCompany().collect {
                        Log.d("exactcompanies", "$it")
                        val list = ArrayList(it)
                        setupListOfDataIntoRecyclerViewCompany(list,userDao)
                    }
                }
            }
            else -> {
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding1 = null
        _binding2 = null
        _binding3 = null
        _binding4 = null
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

    private fun dayConverter(date: String): String {
        return date.replace("Mon","Senin").replace("Tue","Selasa").replace("Wed","Rabu").replace("Thu","Kamis").replace("Fri","Jumat").replace("Sat","Sabtu").replace("Sun","Minggu")
    }

    private fun monthConverter(date: String): String {
        return date.replace("Jan","Januari").replace("Feb","Februari").replace("Mar","Maret").replace("Apr","April").replace("May","Mei").replace("Jun","Juni").replace("Jul","Juli").replace("Aug","Agustus").replace("Sep","September").replace("Oct","Oktober").replace("Nov","November").replace("Dec","December")
    }

    private fun soundingCalculator(): List<Double> {
        var volumeKalibrasi2 = 0.0
        var volumeFraksi = 0.0
        var volumeMid = 0.0
        var volumeApp = 0.0
        var volumeAbs = 0.0
        var volume = 0.0
        var nilaiHasilKalkulator = 0.0
        var delta: Double
        var tinggiTerkoreksi: Double
        binding1.apply {
            if (calculatorCheck()) {
                when {
                    tabelFraksi.text.toString().isNotEmpty() and tabelKalibrasi.text.toString().isNotEmpty() -> {
                        volumeFraksi = tabelFraksi.text.toString().toDouble()
                        volumeKalibrasi2 = tabelKalibrasi.text.toString().toDouble()
                        volumeMid = volumeKalibrasi2
                        tinggiTerkoreksi = roundDigits((tinggiCairan.text.toString().toDouble() + tinggiMeja.text.toString().toDouble())/1000)
                        volumeApp = roundDigits(tabelFraksi.text.toString().toDouble() + tabelKalibrasi.text.toString().toDouble())
                        volumeAbs = roundDigits(volumeApp*(1.0+((suhuCairan.text.toString().toDouble()-suhuTetap.text.toString().toDouble())*muai.text.toString().toDouble())))
                        volume = roundDigits(volumeAbs/1000.0)
                        nilaiHasilKalkulator = roundDigits(volume*densityCairan.text.toString().toDouble())
                        hasilKalkulator.text = String.format(getString(R.string.hasil_akhir_edited), nilaiHasilKalkulator.toString().replace(".",","))
                        hasilVolume.text = String.format(getString(R.string.volume_edited), volume.toString().replace(".",","))
                        hasilVolumeApp.text = String.format(getString(R.string.volume_app_edited), volumeApp.toString().replace(".",","))
                        hasilVolumeObs.text = String.format(getString(R.string.volume_obs_edited), volumeAbs.toString().replace(".",","))
                        hasilTinggiTerkoreksi.text = String.format(getString(R.string.tinggi_terkoreksi_edited), tinggiTerkoreksi.toString().replace(".",","))
                    }
                    tabelKalibrasi.text.toString().isNotEmpty() and tabelKalibrasi2.text.toString().isNotEmpty() -> {
                        volumeKalibrasi2 = tabelKalibrasi2.text.toString().toDouble()
                        tinggiTerkoreksi = roundDigits(tinggiCairan.text.toString().toDouble() + tinggiMeja.text.toString().toDouble())
                        delta = ((tinggiCairan.text.toString().toDouble() + tinggiMeja.text.toString().toDouble())/1000 - judulTabelKalibrasi.text.toString().subSequence(judulTabelKalibrasi.text.indexOf("(")+1, judulTabelKalibrasi.text.indexOf(")")-2).toString().toDouble())/0.01
                        volumeMid = tabelKalibrasi.text.toString().toDouble()+delta*(tabelKalibrasi2.text.toString().toDouble()-tabelKalibrasi.text.toString().toDouble())
                        volumeApp = roundDigits(volumeMid)
                        volumeAbs = roundDigits(volumeApp*(1.0+((suhuCairan.text.toString().toDouble()-suhuTetap.text.toString().toDouble())*muai.text.toString().toDouble())))
                        volume = roundDigits(volumeAbs/1000.0)
                        nilaiHasilKalkulator = roundDigits(volume*densityCairan.text.toString().toDouble())
                        hasilKalkulator.text = String.format(getString(R.string.hasil_akhir_edited), nilaiHasilKalkulator.toString().replace(".",","))
                        hasilVolume.text = String.format(getString(R.string.volume_edited), volume.toString().replace(".",","))
                        hasilVolumeApp.text = String.format(getString(R.string.volume_app_edited), volumeApp.toString().replace(".",","))
                        hasilVolumeObs.text = String.format(getString(R.string.volume_obs_edited), volumeAbs.toString().replace(".",","))
                        hasilTinggiTerkoreksi.text = String.format(getString(R.string.tinggi_terkoreksi_edited), tinggiTerkoreksi.toString().replace(".",","))
                    }
                    else -> {
                        resetResult()
                    }
                }
            }
            else {
                resetResult()
            }
        }
        return listOf(volumeFraksi, volumeKalibrasi2, volumeMid, volumeApp, volumeAbs, volume, nilaiHasilKalkulator)
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
//            val tingggiTerkoreksi = roundDigits(tinggiCairan.text.toString().toDouble() + tinggiMeja.text.toString().toDouble())
//            hasilTinggiTerkoreksi.text = String.format(getString(R.string.tinggi_terkoreksi_edited), tingggiTerkoreksi.toString().replace(".",","))
        }
    }

    private fun tinggiCek (): Boolean {
        binding1.apply {
            return tinggiCairan.text.toString().isNotEmpty() and tinggiMeja.text.toString().isNotEmpty()
        }
    }

    private fun roundDigits(number: Double): Double {
        val number6digits = (number * 1000000).roundToLong()/1000000.toDouble()
        val number5digits = (number6digits * 100000).roundToLong()/100000.toDouble()
        val number4digits = (number5digits * 10000).roundToLong()/10000.toDouble()
        return (number4digits * 1000).roundToLong()/1000.toDouble()
    }

    private fun calculatorCheck(): Boolean {
        binding1.apply {
            return tinggiCairan.text.toString().isNotEmpty() and
                    tinggiMeja.text.toString().isNotEmpty() and
                    suhuCairan.text.toString().isNotEmpty() and
                    suhuTetap.text.toString().isNotEmpty() and
                    muai.text.toString().isNotEmpty() and
                    densityCairan.text.toString().isNotEmpty()
        }
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
            // Adapter class is initialized and list is passed in the param.
            val itemAdapter = PegawaiAdapter(employeesList,{ updateId ->
                updateRecordDialogUser(updateId,userDao)
            }){deleteId->deleteRecordAlertDialogUser(deleteId,userDao)
            }
            // Set the LayoutManager that this RecyclerView will use.
            _binding3?.rvUserList?.layoutManager = LinearLayoutManager(context)
            // adapter instance is set to the recyclerview to inflate the items.
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
                    userDao.insertUser(PegawaiEntity(nama_pegawai = name, nip = nip.toLong()))
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
        /*Set the screen content from a layout resource.
         The resource will be inflated, adding all top-level views to the screen.*/
        val binding = DialogUpdateBinding.inflate(layoutInflater)
        updateDialog.setContentView(binding.root)
        updateDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        lifecycleScope.launch {
            userDao.fetchUserById(id).collect {
                binding.updateNama.setText(it.nama_pegawai)
                binding.updateNip.setText(it.nip.toString())
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
                        userDao.updateUser(PegawaiEntity(id, name, nip.toLong()))
                        Toast.makeText(context, "Data Berhasil Diupdate", Toast.LENGTH_SHORT)
                            .show()
                        updateDialog.dismiss() // Dialog will be dismissed
                    }
                }
            }
        }
        binding.tvCancel.setOnClickListener{
            updateDialog.dismiss()
        }
        //Start the dialog and display it on screen.
        updateDialog.show()
    }

    private fun deleteRecordAlertDialogUser(id:Int,userDao: UserDao) {
        val builder = AlertDialog.Builder(requireContext())
        //set title for alert dialog
        //set message for alert dialog
        builder.setTitle("Hapus Data").setMessage("Apakah Anda yakin ingin menghapus data?")

        //performing positive action
        builder.setPositiveButton("Yes") { dialogInterface, _ ->
            lifecycleScope.launch {
                //calling the deleteEmployee method of DatabaseHandler class to delete record
                userDao.deleteUser(PegawaiEntity(id))
                Toast.makeText(
                    context,
                    "Data Berhasil Dihapus",
                    Toast.LENGTH_SHORT
                ).show()

                dialogInterface.dismiss() // Dialog will be dismissed
            }

        }


        //performing negative action
        builder.setNegativeButton("No") { dialogInterface, _ ->
            dialogInterface.dismiss() // Dialog will be dismissed
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false) // Will not allow user to cancel after clicking on remaining screen area.
        alertDialog.show()  // show the dialog to UI
    }

    private fun setupListOfServiceUserDataIntoRecyclerView(penggunaJasaList:ArrayList<PenggunaJasaEntity>, userDao: UserDao) {

        if (penggunaJasaList.isNotEmpty()) {
            // Adapter class is initialized and list is passed in the param.
            val penggunaJasaAdapter = PenggunaJasaAdapter(penggunaJasaList,{ updateId ->
                updateRecordDialogServiceUser(updateId,userDao)
            }){deleteId->
                deleteRecordAlertDialogServiceUser(deleteId,userDao)
            }
            // Set the LayoutManager that this RecyclerView will use.
            _binding3?.rvServiceUserList?.layoutManager = LinearLayoutManager(context)
            // adapter instance is set to the recyclerview to inflate the items.
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
            }
        }
    }

    private fun updateRecordDialogServiceUser(id:Int, userDao: UserDao) {
        val updateDialog = Dialog(requireContext(), R.style.Theme_Dialog)
        updateDialog.setCancelable(false)
        /*Set the screen content from a layout resource.
         The resource will be inflated, adding all top-level views to the screen.*/
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
                            binding.updateNama.setText(it.nama_pengguna_jasa)
                            binding.updateJabatan.setText(it.jabatan)
                            val spinnerPosition = adapter?.getPosition(it.perusahaan_pengguna_jasa)
                            if (spinnerPosition != null) {
                                binding.updatePerusahaan.setSelection(spinnerPosition)
                            }
                        }
                    }
                } else {
                    lifecycleScope.launch {
                        userDao.fetchServiceUserById(id).collect {
                            binding.updateNama.setText(it.nama_pengguna_jasa)
                            binding.updateJabatan.setText(it.jabatan)
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
                                updateDialog.dismiss() // Dialog will be dismissed
                            }
                        }
                    }
                }
            }
        }
        binding.tvCancel.setOnClickListener{
            updateDialog.dismiss()
        }
        //Start the dialog and display it on screen.
        updateDialog.show()
    }

    private fun deleteRecordAlertDialogServiceUser(id:Int,userDao: UserDao) {

        val builder = AlertDialog.Builder(requireContext())
        //set title for alert dialog
        //set message for alert dialog
        builder.setTitle("Hapus Data").setMessage("Apakah Anda yakin ingin menghapus data?")

        //performing positive action
        builder.setPositiveButton("Yes") { dialogInterface, _ ->
            lifecycleScope.launch {
                //calling the deleteEmployee method of DatabaseHandler class to delete record
                userDao.deleteServiceUser(PenggunaJasaEntity(id))
                Toast.makeText(
                    context,
                    "Data Berhasil Dihapus",
                    Toast.LENGTH_SHORT
                ).show()
                dialogInterface.dismiss() // Dialog will be dismissed
            }
        }

        //performing negative action
        builder.setNegativeButton("No") { dialogInterface, _ ->
            dialogInterface.dismiss() // Dialog will be dismissed
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false) // Will not allow user to cancel after clicking on remaining screen area.
        alertDialog.show()  // show the dialog to UI
    }

    private fun setupListOfDataIntoRecyclerViewCompany(perusahaanList:ArrayList<PerusahaanEntity>, userDao: UserDao) {
        if (perusahaanList.isNotEmpty()) {
            // Adapter class is initialized and list is passed in the param.
            val companyAdapter = PerusahaanAdapter(perusahaanList,{ updateId ->
                updateRecordDialogCompany(updateId,userDao)
            }){deleteId->
                deleteRecordAlertDialogCompany(deleteId,userDao)
            }
            // Set the LayoutManager that this RecyclerView will use.
            _binding4?.rvItemsList?.layoutManager = LinearLayoutManager(context)
            // adapter instance is set to the recyclerview to inflate the items.
            _binding4?.rvItemsList?.adapter = companyAdapter
            _binding4?.svItemList?.visibility = View.VISIBLE
            _binding4?.tvNoRecordsAvailable?.visibility = View.GONE
        } else {

            _binding4?.svItemList?.visibility = View.GONE
            _binding4?.tvNoRecordsAvailable?.visibility = View.VISIBLE
        }
    }

    private fun addRecordCompany(userDao: UserDao) {
        val name = endSpaceRemover(_binding4?.nama?.text.toString())
        val npwp = _binding4?.etNPWPId?.text.toString()
        val alamat = endSpaceRemover(_binding4?.etAlamatId?.text.toString())

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
                    _binding4?.nama?.text?.clear()
                    _binding4?.etNPWPId?.text?.clear()
                    _binding4?.etAlamatId?.text?.clear()
                }
            }
        }
    }

    private fun updateRecordDialogCompany(id:Int, userDao: UserDao) {
        val updateDialog = Dialog(requireContext(), R.style.Theme_Dialog)
        updateDialog.setCancelable(false)
        /*Set the screen content from a layout resource.
         The resource will be inflated, adding all top-level views to the screen.*/
        val binding = DialogUpdateCompanyBinding.inflate(layoutInflater)
        updateDialog.setContentView(binding.root)
        updateDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        lifecycleScope.launch {
            userDao.fetchCompanyById(id).collect {
                binding.updateNama.setText(it.nama_perusahaan)
                binding.updateNpwp.setText(it.npwp)
                binding.etUpdateAlamatId.setText(it.alamat)
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
                        updateDialog.dismiss() // Dialog will be dismissed
                    }
                }
            }
        }
        binding.tvCancel.setOnClickListener{
            updateDialog.dismiss()
        }
        //Start the dialog and display it on screen.
        updateDialog.show()
    }

    private fun deleteRecordAlertDialogCompany(id:Int,userDao: UserDao) {
        val builder = AlertDialog.Builder(requireContext())
        //set title for alert dialog
        //set message for alert dialog
        builder.setTitle("Hapus Data").setMessage("Apakah Anda yakin ingin menghapus data?")

        //performing positive action
        builder.setPositiveButton("Yes") { dialogInterface, _ ->
            lifecycleScope.launch {
                //calling the deleteEmployee method of DatabaseHandler class to delete record
                userDao.deleteCompany(PerusahaanEntity(id))
                Toast.makeText(
                    context,
                    "Data Berhasil Dihapus",
                    Toast.LENGTH_SHORT
                ).show()

                dialogInterface.dismiss() // Dialog will be dismissed
            }
        }

        //performing negative action
        builder.setNegativeButton("No") { dialogInterface, _ ->
            dialogInterface.dismiss() // Dialog will be dismissed
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false) // Will not allow user to cancel after clicking on remaining screen area.
        alertDialog.show()  // show the dialog to UI
    }
}