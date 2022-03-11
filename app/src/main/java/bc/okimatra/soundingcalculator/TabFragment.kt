package bc.okimatra.soundingcalculator

import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import bc.okimatra.soundingcalculator.databinding.*
import bc.okimatra.soundingcalculator.datasetup.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round
import android.app.TimePickerDialog





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
                var results: List<Double>
                dateSetListener = DatePickerDialog.OnDateSetListener {
                        _, year, month, dayOfMonth ->
                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.MONTH, month)
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    updateDateInView()
                }

                binding1.apply{
                    tanggal.setOnClickListener {
                        DatePickerDialog(
                            requireContext(),
                            dateSetListener,
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }

                    waktu.setOnClickListener { // TODO Auto-generated method stub
                        val mcurrentTime = Calendar.getInstance()
                        val hour = mcurrentTime[Calendar.HOUR_OF_DAY]
                        val minute = mcurrentTime[Calendar.MINUTE]
                        val mTimePicker = TimePickerDialog(
                            requireContext(),
                            { _, selectedHour, selectedMinute -> waktu.setText(String.format(getString(R.string.format_waktu, selectedHour.toString(), selectedMinute.toString()))) },
                            hour,
                            minute,
                            true
                        )
                        mTimePicker.setTitle("Select Time")
                        mTimePicker.show()
                    }

                    interpolasiTab.setOnClickListener {
                        interpolasiTab.background = ResourcesCompat.getDrawable(resources, R.drawable.switch_on,null)
                        interpolasiTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.white))
                        fraksiTab.background = null
                        fraksiTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.login))
                        interpolasiLayout.visibility = View.VISIBLE
                        fraksiLayout.visibility = View.GONE
                        tabelKalibrasi.text = null
                        tabelFraksi.text = null
                    }

                    fraksiTab.setOnClickListener {
                        fraksiTab.background = ResourcesCompat.getDrawable(resources, R.drawable.switch_on,null)
                        fraksiTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.white))
                        interpolasiTab.background = null
                        interpolasiTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.login))
                        fraksiLayout.visibility = View.VISIBLE
                        interpolasiLayout.visibility = View.GONE
                        tabelKalibrasi1.text = null
                        tabelKalibrasi2.text = null
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
                            tabelKalibrasi1.text = null
                            tabelKalibrasi2.text = null
                            Toast.makeText(requireActivity(), "Data Telah Tersimpan", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireActivity(), "Tidak Ada Data", Toast.LENGTH_SHORT).show()
                        }
                    }

                    tinggiCairan.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }

                        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }

                        override fun afterTextChanged(p0: Editable?) {
                            if (tinggiCek(binding1)) {
                                judulDataTabel(binding1)
                                results = soundingCalculator(binding1)
                            } else {
                                resetDataTabel(binding1)
                            }
                        }
                    })

                    tinggiMeja.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }

                        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }

                        override fun afterTextChanged(p0: Editable?) {
                            if (tinggiCek(binding1)) {
                                judulDataTabel(binding1)
                                results = soundingCalculator(binding1)
                            } else {
                                resetDataTabel(binding1)
                            }
                        }
                    })

                    suhuCairan.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }

                        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }

                        override fun afterTextChanged(p0: Editable?) {
                            results = soundingCalculator(binding1)
                        }
                    })

                    suhuTetap.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }

                        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }

                        override fun afterTextChanged(p0: Editable?) {
                            results = soundingCalculator(binding1)
                        }
                    })

                    muai.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }

                        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }

                        override fun afterTextChanged(p0: Editable?) {
                            results = soundingCalculator(binding1)
                        }
                    })

                    tabelFraksi.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }

                        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }

                        override fun afterTextChanged(p0: Editable?) {
                            results = soundingCalculator(binding1)
                        }
                    })

                    tabelKalibrasi.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }

                        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }

                        override fun afterTextChanged(p0: Editable?) {
                            results = soundingCalculator(binding1)
                        }
                    })

                    tabelKalibrasi1.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }

                        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }

                        override fun afterTextChanged(p0: Editable?) {
                            results = soundingCalculator(binding1)
                        }
                    })

                    tabelKalibrasi2.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }

                        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }

                        override fun afterTextChanged(p0: Editable?) {
                            results = soundingCalculator(binding1)
                        }
                    })

                    densityCairan.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }

                        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }

                        override fun afterTextChanged(p0: Editable?) {
                            results = soundingCalculator(binding1)
                        }
                    })
                }
            }
            title === "User" -> {
                val userDao = (requireActivity().application as UserApp).db.userDao()
                lifecycleScope.launch {
                    userDao.fetchAllUser().collect {
                        Log.d("exactemployee", "$it")
                        val list = ArrayList(it)
                        setupListOfUserDataIntoRecyclerView(list,userDao)
                    }
                }

                binding3.apply {
                    eksportirTab.setOnClickListener {
                        eksportirTab.background = ResourcesCompat.getDrawable(resources, R.drawable.switch_on,null)
                        eksportirTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.white))
                        pegawaiTab.background = null
                        pegawaiTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.login))
                        penggunajasaLayout.visibility = View.VISIBLE
                        btnAddUser.visibility = View.GONE
                        btnAddExporter.visibility = View.VISIBLE
                        pegawaiLayout.visibility = View.GONE
                        etNIPId.text = null
                        etName.hint = getText(R.string.enter_nama_eksportir)
                        lifecycleScope.launch {
                            userDao.fetchAllExporter().collect {
                                Log.d("exactemployee", "$it")
                                val list = ArrayList(it)
                                setupListOfExporterDataIntoRecyclerView(list, userDao)
                            }
                        }
                    }

                    pegawaiTab.setOnClickListener {
                        pegawaiTab.background = ResourcesCompat.getDrawable(resources, R.drawable.switch_on,null)
                        pegawaiTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.white))
                        eksportirTab.background = null
                        eksportirTab.setTextColor(ContextCompat.getColor(requireContext(),R.color.login))
                        btnAddUser.visibility = View.VISIBLE
                        btnAddExporter.visibility = View.GONE
                        pegawaiLayout.visibility = View.VISIBLE
                        penggunajasaLayout.visibility = View.GONE
                        etJabatanId.text = null
                        etName.hint = getText(R.string.enter_name)
                        svUserList.visibility = View.VISIBLE
                        svExporterList.visibility = View.GONE
                    }
                }

                _binding3?.btnAddUser?.setOnClickListener {
                    addRecordUser(userDao)
                }

                _binding3?.btnAddExporter?.setOnClickListener {
                    addRecordExporter(userDao)
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

                    etName.setOnFocusChangeListener { _, _ ->
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
                        }

                        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                            textOld = s.toString()
                        }

                        override fun afterTextChanged(s: Editable) {
                            number = s.toString().replace("_","").replace(".","").replace("-","")
                            cursorPosition = etNPWPId.selectionStart

                            if (cursorPosition > number.length) {
                                etNPWPId.setSelection(number.length)
                            }

//                            if (etNPWPId.hasFocus()) {
//                                if ((number.isEmpty() && textOld != holder) || etNPWPId.text.toString().isEmpty() || (number.isEmpty() && textOld == holder && s.toString() != holder) ) {
//                                    etNPWPId.setText(getString(R.string.before_edited))
//                                }
//                            }

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

    private fun updateDateInView() {
        val timeID = "dd-MM-yyyy"
        val sdf = SimpleDateFormat(timeID, Locale.getDefault())
        binding1.tanggal.setText(sdf.format(cal.time).toString())
    }

    private fun soundingCalculator(binding: FragmentOneBinding): List<Double> {
        var volumeApp = 0.0
        var volumeAbs = 0.0
        var volume = 0.0
        var nilaiHasilKalkulator= 0.0
        var delta: Double
        var volumeMid: Double
        binding.apply {
            if (calculatorCheck(binding)) {
                when {
                    tabelFraksi.text.toString().isNotEmpty() and tabelKalibrasi.text.toString().isNotEmpty() -> {
                        volumeApp = tabelFraksi.text.toString().toDouble() + tabelKalibrasi.text.toString().toDouble()
                        volumeAbs = volumeApp*(1.0+((suhuCairan.text.toString().toDouble()-suhuTetap.text.toString().toDouble())*muai.text.toString().toDouble()))
                        volume = volumeAbs/1000.0
                        nilaiHasilKalkulator = round(volume*densityCairan.text.toString().toDouble()*100000.0)/100000.0
                        nilaiHasilKalkulator = round(nilaiHasilKalkulator*10000.0)/10000.0
                        nilaiHasilKalkulator = round(nilaiHasilKalkulator*1000.0)/1000.0
                        hasilKalkulator.text = String.format(getString(R.string.hasil_akhir_edited), nilaiHasilKalkulator.toString())
                    }
                    tabelKalibrasi1.text.toString().isNotEmpty() and tabelKalibrasi2.text.toString().isNotEmpty() -> {
                        delta = ((tinggiCairan.text.toString().toDouble() + tinggiMeja.text.toString().toDouble())/1000 - judulTabelKalibrasi1.text.toString().subSequence(judulTabelKalibrasi1.text.indexOf("(")+1, judulTabelKalibrasi1.text.indexOf(")")-2).toString().toDouble())/0.01
                        volumeMid = tabelKalibrasi1.text.toString().toDouble()+delta*(tabelKalibrasi2.text.toString().toDouble()-tabelKalibrasi1.text.toString().toDouble())
                        volumeApp = volumeMid
                        volumeAbs = volumeApp*(1.0+((suhuCairan.text.toString().toDouble()-suhuTetap.text.toString().toDouble())*muai.text.toString().toDouble()))
                        volume = volumeAbs/1000.0
                        nilaiHasilKalkulator = round(volume*densityCairan.text.toString().toDouble()*100000.0)/100000.0
                        nilaiHasilKalkulator = round(nilaiHasilKalkulator*10000.0)/10000.0
                        nilaiHasilKalkulator = round(nilaiHasilKalkulator*1000.0)/1000.0
                        hasilKalkulator.text = String.format(getString(R.string.hasil_akhir_edited), nilaiHasilKalkulator.toString())
                    }
                    else -> {
                        hasilKalkulator.text = getText(R.string.hasil)
                    }
                }
            }
            else {
                hasilKalkulator.text = getText(R.string.hasil)
            }
        }
        return listOf(volumeApp, volumeAbs, volume, nilaiHasilKalkulator)
    }

    private fun resetDataTabel(binding: FragmentOneBinding) {
        binding.apply {
            judulTabelKalibrasi.text = getText(R.string.tabel_kalibrasi)
            judulTabelFraksi.text = getText(R.string.tabel_fraksi)
            judulTabelKalibrasi1.text = getText(R.string.tabel_kalibrasi1)
            judulTabelKalibrasi2.text = getText(R.string.tabel_kalibrasi2)
            dataTabel.text = getText(R.string.data_tabel)
        }
    }

    private fun judulDataTabel(binding: FragmentOneBinding) {
        var soundingCorrect: Double
        var satuanmm : String
        var soundingCorrected: String
        var soundingCorrectedFinal: String
        binding.apply {
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
            judulTabelKalibrasi1.text = judulTabelKalibrasi.text.toString()
            if (judulTabelKalibrasi.text.toString().subSequence(judulTabelKalibrasi.text.toString().length-5,judulTabelKalibrasi.text.toString().length-4).toString().toInt() != 9) {
                judulTabelKalibrasi2.text = String.format(getString(R.string.tabel_kalibrasi_edited), (round((judulTabelKalibrasi.text.toString().subSequence(17,judulTabelKalibrasi.text.toString().length-3).toString().toDouble() + 0.01)* 100.0) / 100.0).toString())
            }
            else {
                judulTabelKalibrasi2.text = String.format(getString(R.string.tabel_kalibrasi_edited1), (round((judulTabelKalibrasi.text.toString().subSequence(17,judulTabelKalibrasi.text.toString().length-3).toString().toDouble() + 0.01)* 10.0) / 10.0).toString())
            }
            satuanmm = (tinggiCairan.text.toString().toDouble() + tinggiMeja.text.toString().toDouble()).toString()
            satuanmm = satuanmm.subSequence(satuanmm.indexOf(".")-1,satuanmm.indexOf(".")).toString()
            judulTabelFraksi.text = String.format(getString(R.string.tabel_fraksi_edited), satuanmm)
        }
    }

    private fun tinggiCek (binding: FragmentOneBinding): Boolean {
        binding.apply {
            return tinggiCairan.text.toString().isNotEmpty() and tinggiMeja.text.toString().isNotEmpty()
        }
    }

    private fun calculatorCheck(binding: FragmentOneBinding): Boolean {
        binding.apply {
            return tinggiCairan.text.toString().isNotEmpty() and
                    tinggiMeja.text.toString().isNotEmpty() and
                    suhuCairan.text.toString().isNotEmpty() and
                    suhuTetap.text.toString().isNotEmpty() and
                    muai.text.toString().isNotEmpty() and
                    densityCairan.text.toString().isNotEmpty()
        }
    }

    private fun setupListOfUserDataIntoRecyclerView(employeesList:ArrayList<UserEntity>, userDao: UserDao) {

        if (employeesList.isNotEmpty()) {
            // Adapter class is initialized and list is passed in the param.
            val itemAdapter = UserAdapter(employeesList,{ updateId ->
                updateRecordDialogUser(updateId,userDao)
            }){deleteId->
                deleteRecordAlertDialogUser(deleteId,userDao)
            }
            // Set the LayoutManager that this RecyclerView will use.
            _binding3?.rvUserList?.layoutManager = LinearLayoutManager(context)
            // adapter instance is set to the recyclerview to inflate the items.
            _binding3?.rvUserList?.adapter = itemAdapter
            _binding3?.svUserList?.visibility = View.VISIBLE
            _binding3?.svExporterList?.visibility = View.GONE
            _binding3?.tvNoRecordsAvailable?.visibility = View.GONE
        } else {
            _binding3?.svUserList?.visibility = View.GONE
            _binding3?.svExporterList?.visibility = View.GONE
            _binding3?.tvNoRecordsAvailable?.visibility = View.VISIBLE
        }
    }

    private fun addRecordUser(userDao: UserDao) {
        val name = _binding3?.etName?.text.toString()
        val nip = _binding3?.etNIPId?.text.toString()
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
                    userDao.insertUser(UserEntity(nama = name, nip = nip.toLong()))
                    Toast.makeText(context, "Data Berhasil Disimpan", Toast.LENGTH_SHORT).show()
                    _binding3?.etName?.text?.clear()
                    _binding3?.etNIPId?.text?.clear()
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
                binding.etUpdateName.setText(it.nama)
                binding.etUpdateNIPId.setText(it.nip.toString())
            }
        }
        binding.tvUpdate.setOnClickListener {

            val name = binding.etUpdateName.text.toString()
            val nip = binding.etUpdateNIPId.text.toString()
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
                        userDao.updateUser(UserEntity(id, name, nip.toLong()))
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
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        //performing positive action
        builder.setPositiveButton("Yes") { dialogInterface, _ ->
            lifecycleScope.launch {
                //calling the deleteEmployee method of DatabaseHandler class to delete record
                userDao.deleteUser(UserEntity(id))
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

    private fun setupListOfExporterDataIntoRecyclerView(exporterList:ArrayList<ExporterEntity>, userDao: UserDao) {

        if (exporterList.isNotEmpty()) {
            // Adapter class is initialized and list is passed in the param.
            val exporterAdapter = ExporterAdapter(exporterList,{ updateId ->
                updateRecordDialogExporter(updateId,userDao)
            }){deleteId->
                deleteRecordAlertDialogExporter(deleteId,userDao)
            }
            // Set the LayoutManager that this RecyclerView will use.
            _binding3?.rvExporterList?.layoutManager = LinearLayoutManager(context)
            // adapter instance is set to the recyclerview to inflate the items.
            _binding3?.rvExporterList?.adapter = exporterAdapter
            _binding3?.svExporterList?.visibility = View.VISIBLE
            _binding3?.svUserList?.visibility = View.GONE
            _binding3?.tvNoRecordsAvailable?.visibility = View.GONE
        } else {
            _binding3?.svUserList?.visibility = View.GONE
            _binding3?.svExporterList?.visibility = View.GONE
            _binding3?.tvNoRecordsAvailable?.visibility = View.VISIBLE
        }
    }

    private fun addRecordExporter(userDao: UserDao) {
        val name = _binding3?.etName?.text.toString()
        val jabatan = _binding3?.etJabatanId?.text.toString()

        when {
            name.isEmpty() -> {
                Toast.makeText(context, "Mohon masukkan Nama Eksportir", Toast.LENGTH_SHORT).show()
            }
            jabatan.isEmpty() -> {
                Toast.makeText(context, "Mohon masukkan Jabatan Eksportir", Toast.LENGTH_SHORT).show()
            }
            else -> {
                lifecycleScope.launch {
                    userDao.insertExporter(ExporterEntity(nama = name, jabatan = jabatan))
                    Toast.makeText(context, "Data Berhasil Disimpan", Toast.LENGTH_SHORT).show()
                    _binding3?.etName?.text?.clear()
                    _binding3?.etJabatanId?.text?.clear()
                }
            }
        }
    }

    private fun updateRecordDialogExporter(id:Int, userDao: UserDao) {
        val updateDialog = Dialog(requireContext(), R.style.Theme_Dialog)
        updateDialog.setCancelable(false)
        /*Set the screen content from a layout resource.
         The resource will be inflated, adding all top-level views to the screen.*/
        val binding = DialogUpdateExporterBinding.inflate(layoutInflater)
        updateDialog.setContentView(binding.root)
        updateDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        lifecycleScope.launch {
            userDao.fetchExporterById(id).collect {
                binding.etUpdateName.setText(it.nama)
                binding.etUpdateJabatanId.setText(it.jabatan)
            }
        }

        binding.tvUpdate.setOnClickListener {

            val name = binding.etUpdateName.text.toString()
            val jabatan = binding.etUpdateJabatanId.text.toString()

            when {
                name.isEmpty() -> {
                    Toast.makeText(context, "Mohon Masukkan Nama Eksportir", Toast.LENGTH_SHORT).show()
                }
                jabatan.isEmpty() -> {
                    Toast.makeText(context, "Mohon Masukkan Jabatan Eksportir", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    lifecycleScope.launch {
                        userDao.updateExporter(ExporterEntity(id, name, jabatan))
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

    private fun deleteRecordAlertDialogExporter(id:Int,userDao: UserDao) {

        val builder = AlertDialog.Builder(requireContext())
        //set title for alert dialog
        //set message for alert dialog
        builder.setTitle("Hapus Data").setMessage("Apakah Anda yakin ingin menghapus data?")

        //performing positive action
        builder.setPositiveButton("Yes") { dialogInterface, _ ->
            lifecycleScope.launch {
                //calling the deleteEmployee method of DatabaseHandler class to delete record
                userDao.deleteExporter(ExporterEntity(id))
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

    private fun setupListOfDataIntoRecyclerViewCompany(companyList:ArrayList<CompanyEntity>, userDao: UserDao) {
        if (companyList.isNotEmpty()) {
            // Adapter class is initialized and list is passed in the param.
            val companyAdapter = CompanyAdapter(companyList,{updateId ->
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
        val name = _binding4?.etName?.text.toString()
        val npwp = _binding4?.etNPWPId?.text.toString()
        val alamat = _binding4?.etAlamatId?.text.toString()

        when {
            name.isEmpty() -> {
                Toast.makeText(context, "Mohon masukkan Nama Perusahaan", Toast.LENGTH_SHORT).show()
            }
            npwp.isEmpty() || npwp == getString(R.string.before_edited) -> {
                Toast.makeText(context, "Mohon masukkan NPWP Perusahaan", Toast.LENGTH_SHORT).show()
            }
            alamat.isEmpty() -> {
                Toast.makeText(context, "Mohon masukkan Alamat Perusahaan", Toast.LENGTH_SHORT).show()
            }
            else -> {
                lifecycleScope.launch {
                    userDao.insertCompany(CompanyEntity(nama = name, npwp = npwp, alamat = alamat))
                    Toast.makeText(context, "Data Berhasil Disimpan", Toast.LENGTH_SHORT).show()
                    _binding4?.etName?.text?.clear()
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
                binding.etUpdateName.setText(it.nama)
                binding.etUpdateNPWPId.setText(it.npwp)
                binding.etUpdateAlamatId.setText(it.alamat)
            }
        }
        binding.tvUpdate.setOnClickListener {

            val name = binding.etUpdateName.text.toString()
            val npwp = binding.etUpdateNPWPId.text.toString()
            val alamat = binding.etUpdateAlamatId.text.toString()

            when {
                name.isEmpty() -> {
                    Toast.makeText(context, "Mohon masukkan Nama Perusahaan", Toast.LENGTH_SHORT).show()
                }
                npwp.isEmpty() -> {
                    Toast.makeText(context, "Mohon masukkan NPWP Perusahaan", Toast.LENGTH_SHORT).show()
                }
                alamat.isEmpty() -> {
                    Toast.makeText(context, "Mohon masukkan Alamat Perusahaan", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    lifecycleScope.launch {
                        userDao.updateCompany(CompanyEntity(id, name, npwp, alamat))
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
                userDao.deleteCompany(CompanyEntity(id))
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