package bc.okimatra.soundingcalculator

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
import android.widget.EditText
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
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.round


class TabFragment(private val title: String) : Fragment() {

    private var _binding1: FragmentOneBinding? = null
    private val binding1 get() = _binding1!!

    private var _binding2: FragmentTwoBinding? = null
    private val binding2 get() = _binding2!!

    private var _binding3: FragmentThreeBinding? = null
    private val binding3 get() = _binding3!!

    private var _binding4: FragmentFourBinding? = null
    private val binding4 get() = _binding4!!

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
                var soundingCorrect: Double
                var volumeApp:Double
                var volumeAbs : Double
                var volume : Double
                var nilaiHasilKalkulator : Double
                var delta : String
                var volumeMid : Double

                binding1.apply{
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
                    tinggiCairan.addTextChangedListener(object : TextWatcher
                    {
                        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }

                        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }

                        override fun afterTextChanged(p0: Editable?) {
                            if (tinggiCairan.text.toString().isNotEmpty() and tinggiMeja.text.toString().isNotEmpty()) {
                                val soundingCorrected = (tinggiCairan.text.toString().toDouble() + tinggiMeja.text.toString().toDouble()).toString()
                                val soundingCorrectedFinal = ((tinggiCairan.text.toString().toDouble() + tinggiMeja.text.toString().toDouble())/1000).toString()
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
                                delta = dataTabel.text.toString().subSequence(dataTabel.text.toString().length-4,dataTabel.text.toString().length-3).toString()
                                judulTabelFraksi.text = String.format(getString(R.string.tabel_fraksi_edited), delta)
                                if (tinggiCairan.text.toString().isNotEmpty() and
                                    tinggiMeja.text.toString().isNotEmpty() and
                                    suhuCairan.text.toString().isNotEmpty() and
                                    suhuTetap.text.toString().isNotEmpty() and
                                    muai.text.toString().isNotEmpty() and
                                    densityCairan.text.toString().isNotEmpty()) {
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
                                            volumeMid = tabelKalibrasi1.text.toString().toDouble()+(delta.toDouble()/1000/0.01)*(tabelKalibrasi2.text.toString().toDouble()-tabelKalibrasi1.text.toString().toDouble())
                                            volumeApp = volumeMid
                                            volumeAbs = volumeApp*(1.0+((suhuCairan.text.toString().toDouble()-suhuTetap.text.toString().toDouble())*muai.text.toString().toDouble()))
                                            volume = volumeAbs/1000.0
                                            nilaiHasilKalkulator = round(volume*densityCairan.text.toString().toDouble()*100000.0)/100000.0
                                            nilaiHasilKalkulator = round(nilaiHasilKalkulator*10000.0)/10000.0
                                            nilaiHasilKalkulator = round(nilaiHasilKalkulator*1000.0)/1000.0
                                            print(volumeMid)
                                            print(volumeAbs)
                                            print(volume)
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
                            } else {
                                judulTabelKalibrasi.text = getText(R.string.tabel_kalibrasi)
                                judulTabelFraksi.text = getText(R.string.tabel_fraksi)
                                judulTabelKalibrasi1.text = getText(R.string.tabel_kalibrasi1)
                                judulTabelKalibrasi2.text = getText(R.string.tabel_kalibrasi2)
                                dataTabel.text = getText(R.string.data_tabel)
                            }
                        }
                    })
                    tinggiMeja.addTextChangedListener(object : TextWatcher
                    {
                        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }

                        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }

                        override fun afterTextChanged(p0: Editable?) {
                            if (tinggiCairan.text.toString().isNotEmpty() and tinggiMeja.text.toString().isNotEmpty()) {
                                val soundingCorrected = (tinggiCairan.text.toString().toDouble() + tinggiMeja.text.toString().toDouble()).toString()
                                val soundingCorrectedFinal = ((tinggiCairan.text.toString().toDouble() + tinggiMeja.text.toString().toDouble())/1000).toString()
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
                                delta = dataTabel.text.toString().subSequence(dataTabel.text.toString().length-4,dataTabel.text.toString().length-3).toString()
                                judulTabelFraksi.text = String.format(getString(R.string.tabel_fraksi_edited), delta)
                                if (tinggiCairan.text.toString().isNotEmpty() and
                                    tinggiMeja.text.toString().isNotEmpty() and
                                    suhuCairan.text.toString().isNotEmpty() and
                                    suhuTetap.text.toString().isNotEmpty() and
                                    muai.text.toString().isNotEmpty() and
                                    densityCairan.text.toString().isNotEmpty()) {
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
                                            volumeMid = tabelKalibrasi1.text.toString().toDouble()+(delta.toDouble()/1000/0.01)*(tabelKalibrasi2.text.toString().toDouble()-tabelKalibrasi1.text.toString().toDouble())
                                            volumeApp = volumeMid
                                            volumeAbs = volumeApp*(1.0+((suhuCairan.text.toString().toDouble()-suhuTetap.text.toString().toDouble())*muai.text.toString().toDouble()))
                                            volume = volumeAbs/1000.0
                                            nilaiHasilKalkulator = round(volume*densityCairan.text.toString().toDouble()*100000.0)/100000.0
                                            nilaiHasilKalkulator = round(nilaiHasilKalkulator*10000.0)/10000.0
                                            nilaiHasilKalkulator = round(nilaiHasilKalkulator*1000.0)/1000.0
                                            print(volumeMid)
                                            print(volumeAbs)
                                            print(volume)
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
                            } else {
                                judulTabelKalibrasi.text = getText(R.string.tabel_kalibrasi)
                                judulTabelFraksi.text = getText(R.string.tabel_fraksi)
                                judulTabelKalibrasi1.text = getText(R.string.tabel_kalibrasi1)
                                judulTabelKalibrasi2.text = getText(R.string.tabel_kalibrasi2)
                                dataTabel.text = getText(R.string.data_tabel)
                            }
                        }
                    })
                    suhuCairan.addTextChangedListener(object : TextWatcher
                    {
                        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }

                        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }

                        override fun afterTextChanged(p0: Editable?) {
                            if (tinggiCairan.text.toString().isNotEmpty() and
                                tinggiMeja.text.toString().isNotEmpty() and
                                suhuCairan.text.toString().isNotEmpty() and
                                suhuTetap.text.toString().isNotEmpty() and
                                muai.text.toString().isNotEmpty() and
                                densityCairan.text.toString().isNotEmpty()) {
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
                                        delta = dataTabel.text.toString().subSequence(dataTabel.text.toString().length-4,dataTabel.text.toString().length-3).toString()
                                        volumeMid = tabelKalibrasi1.text.toString().toDouble()+(delta.toDouble()/1000/0.01)*(tabelKalibrasi2.text.toString().toDouble()-tabelKalibrasi1.text.toString().toDouble())
                                        volumeApp = volumeMid
                                        volumeAbs = volumeApp*(1.0+((suhuCairan.text.toString().toDouble()-suhuTetap.text.toString().toDouble())*muai.text.toString().toDouble()))
                                        volume = volumeAbs/1000.0
                                        nilaiHasilKalkulator = round(volume*densityCairan.text.toString().toDouble()*100000.0)/100000.0
                                        nilaiHasilKalkulator = round(nilaiHasilKalkulator*10000.0)/10000.0
                                        nilaiHasilKalkulator = round(nilaiHasilKalkulator*1000.0)/1000.0
                                        print(volumeMid)
                                        print(volumeAbs)
                                        print(volume)
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
                    })
                    suhuTetap.addTextChangedListener(object : TextWatcher
                    {
                        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }

                        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }

                        override fun afterTextChanged(p0: Editable?) {
                            if (tinggiCairan.text.toString().isNotEmpty() and
                                tinggiMeja.text.toString().isNotEmpty() and
                                suhuCairan.text.toString().isNotEmpty() and
                                suhuTetap.text.toString().isNotEmpty() and
                                muai.text.toString().isNotEmpty() and
                                densityCairan.text.toString().isNotEmpty()) {
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
                                        delta = dataTabel.text.toString().subSequence(dataTabel.text.toString().length-4,dataTabel.text.toString().length-3).toString()
                                        volumeMid = tabelKalibrasi1.text.toString().toDouble()+(delta.toDouble()/1000/0.01)*(tabelKalibrasi2.text.toString().toDouble()-tabelKalibrasi1.text.toString().toDouble())
                                        volumeApp = volumeMid
                                        volumeAbs = volumeApp*(1.0+((suhuCairan.text.toString().toDouble()-suhuTetap.text.toString().toDouble())*muai.text.toString().toDouble()))
                                        volume = volumeAbs/1000.0
                                        nilaiHasilKalkulator = round(volume*densityCairan.text.toString().toDouble()*100000.0)/100000.0
                                        nilaiHasilKalkulator = round(nilaiHasilKalkulator*10000.0)/10000.0
                                        nilaiHasilKalkulator = round(nilaiHasilKalkulator*1000.0)/1000.0
                                        print(volumeMid)
                                        print(volumeAbs)
                                        print(volume)
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
                    })
                    muai.addTextChangedListener(object : TextWatcher
                    {
                        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }

                        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }

                        override fun afterTextChanged(p0: Editable?) {
                            if (tinggiCairan.text.toString().isNotEmpty() and
                                tinggiMeja.text.toString().isNotEmpty() and
                                suhuCairan.text.toString().isNotEmpty() and
                                suhuTetap.text.toString().isNotEmpty() and
                                muai.text.toString().isNotEmpty() and
                                densityCairan.text.toString().isNotEmpty()) {
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
                                        delta = dataTabel.text.toString().subSequence(dataTabel.text.toString().length-4,dataTabel.text.toString().length-3).toString()
                                        volumeMid = tabelKalibrasi1.text.toString().toDouble()+(delta.toDouble()/1000/0.01)*(tabelKalibrasi2.text.toString().toDouble()-tabelKalibrasi1.text.toString().toDouble())
                                        volumeApp = volumeMid
                                        volumeAbs = volumeApp*(1.0+((suhuCairan.text.toString().toDouble()-suhuTetap.text.toString().toDouble())*muai.text.toString().toDouble()))
                                        volume = volumeAbs/1000.0
                                        nilaiHasilKalkulator = round(volume*densityCairan.text.toString().toDouble()*100000.0)/100000.0
                                        nilaiHasilKalkulator = round(nilaiHasilKalkulator*10000.0)/10000.0
                                        nilaiHasilKalkulator = round(nilaiHasilKalkulator*1000.0)/1000.0
                                        print(volumeMid)
                                        print(volumeAbs)
                                        print(volume)
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
                    })
                    tabelFraksi.addTextChangedListener(object : TextWatcher
                    {
                        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }

                        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }

                        override fun afterTextChanged(p0: Editable?) {
                            if (tinggiCairan.text.toString().isNotEmpty() and
                                tinggiMeja.text.toString().isNotEmpty() and
                                suhuCairan.text.toString().isNotEmpty() and
                                suhuTetap.text.toString().isNotEmpty() and
                                muai.text.toString().isNotEmpty() and
                                densityCairan.text.toString().isNotEmpty()) {
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
                                        delta = dataTabel.text.toString().subSequence(dataTabel.text.toString().length-4,dataTabel.text.toString().length-3).toString()
                                        volumeMid = tabelKalibrasi1.text.toString().toDouble()+(delta.toDouble()/1000/0.01)*(tabelKalibrasi2.text.toString().toDouble()-tabelKalibrasi1.text.toString().toDouble())
                                        volumeApp = volumeMid
                                        volumeAbs = volumeApp*(1.0+((suhuCairan.text.toString().toDouble()-suhuTetap.text.toString().toDouble())*muai.text.toString().toDouble()))
                                        volume = volumeAbs/1000.0
                                        nilaiHasilKalkulator = round(volume*densityCairan.text.toString().toDouble()*100000.0)/100000.0
                                        nilaiHasilKalkulator = round(nilaiHasilKalkulator*10000.0)/10000.0
                                        nilaiHasilKalkulator = round(nilaiHasilKalkulator*1000.0)/1000.0
                                        print(volumeMid)
                                        print(volumeAbs)
                                        print(volume)
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
                    })
                    tabelKalibrasi.addTextChangedListener(object : TextWatcher
                    {
                        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }

                        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }

                        override fun afterTextChanged(p0: Editable?) {
                            if (tinggiCairan.text.toString().isNotEmpty() and
                                tinggiMeja.text.toString().isNotEmpty() and
                                suhuCairan.text.toString().isNotEmpty() and
                                suhuTetap.text.toString().isNotEmpty() and
                                muai.text.toString().isNotEmpty() and
                                densityCairan.text.toString().isNotEmpty()) {
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
                                        delta = dataTabel.text.toString().subSequence(dataTabel.text.toString().length-4,dataTabel.text.toString().length-3).toString()
                                        volumeMid = tabelKalibrasi1.text.toString().toDouble()+(delta.toDouble()/1000/0.01)*(tabelKalibrasi2.text.toString().toDouble()-tabelKalibrasi1.text.toString().toDouble())
                                        volumeApp = volumeMid
                                        volumeAbs = volumeApp*(1.0+((suhuCairan.text.toString().toDouble()-suhuTetap.text.toString().toDouble())*muai.text.toString().toDouble()))
                                        volume = volumeAbs/1000.0
                                        nilaiHasilKalkulator = round(volume*densityCairan.text.toString().toDouble()*100000.0)/100000.0
                                        nilaiHasilKalkulator = round(nilaiHasilKalkulator*10000.0)/10000.0
                                        nilaiHasilKalkulator = round(nilaiHasilKalkulator*1000.0)/1000.0
                                        print(volumeMid)
                                        print(volumeAbs)
                                        print(volume)
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
                    })
                    tabelKalibrasi1.addTextChangedListener(object : TextWatcher
                    {
                        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }

                        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }

                        override fun afterTextChanged(p0: Editable?) {
                            if (tinggiCairan.text.toString().isNotEmpty() and
                                tinggiMeja.text.toString().isNotEmpty() and
                                suhuCairan.text.toString().isNotEmpty() and
                                suhuTetap.text.toString().isNotEmpty() and
                                muai.text.toString().isNotEmpty() and
                                densityCairan.text.toString().isNotEmpty()) {
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
                                        delta = dataTabel.text.toString().subSequence(dataTabel.text.toString().length-4,dataTabel.text.toString().length-3).toString()
                                        volumeMid = tabelKalibrasi1.text.toString().toDouble()+(delta.toDouble()/1000/0.01)*(tabelKalibrasi2.text.toString().toDouble()-tabelKalibrasi1.text.toString().toDouble())
                                        volumeApp = volumeMid
                                        volumeAbs = volumeApp*(1.0+((suhuCairan.text.toString().toDouble()-suhuTetap.text.toString().toDouble())*muai.text.toString().toDouble()))
                                        volume = volumeAbs/1000.0
                                        nilaiHasilKalkulator = round(volume*densityCairan.text.toString().toDouble()*100000.0)/100000.0
                                        nilaiHasilKalkulator = round(nilaiHasilKalkulator*10000.0)/10000.0
                                        nilaiHasilKalkulator = round(nilaiHasilKalkulator*1000.0)/1000.0
                                        print(volumeMid)
                                        print(volumeAbs)
                                        print(volume)
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
                    })
                    tabelKalibrasi2.addTextChangedListener(object : TextWatcher
                    {
                        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }

                        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }

                        override fun afterTextChanged(p0: Editable?) {
                            if (tinggiCairan.text.toString().isNotEmpty() and
                                tinggiMeja.text.toString().isNotEmpty() and
                                suhuCairan.text.toString().isNotEmpty() and
                                suhuTetap.text.toString().isNotEmpty() and
                                muai.text.toString().isNotEmpty() and
                                densityCairan.text.toString().isNotEmpty()) {
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
                                        delta = dataTabel.text.toString().subSequence(dataTabel.text.toString().length-4,dataTabel.text.toString().length-3).toString()
                                        volumeMid = tabelKalibrasi1.text.toString().toDouble()+(delta.toDouble()/1000/0.01)*(tabelKalibrasi2.text.toString().toDouble()-tabelKalibrasi1.text.toString().toDouble())
                                        volumeApp = volumeMid
                                        volumeAbs = volumeApp*(1.0+((suhuCairan.text.toString().toDouble()-suhuTetap.text.toString().toDouble())*muai.text.toString().toDouble()))
                                        volume = volumeAbs/1000.0
                                        nilaiHasilKalkulator = round(volume*densityCairan.text.toString().toDouble()*100000.0)/100000.0
                                        nilaiHasilKalkulator = round(nilaiHasilKalkulator*10000.0)/10000.0
                                        nilaiHasilKalkulator = round(nilaiHasilKalkulator*1000.0)/1000.0
                                        print(volumeMid)
                                        print(volumeAbs)
                                        print(volume)
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
                    })
                    densityCairan.addTextChangedListener(object : TextWatcher
                    {
                        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }

                        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }

                        override fun afterTextChanged(p0: Editable?) {
                            if (tinggiCairan.text.toString().isNotEmpty() and
                                tinggiMeja.text.toString().isNotEmpty() and
                                suhuCairan.text.toString().isNotEmpty() and
                                suhuTetap.text.toString().isNotEmpty() and
                                muai.text.toString().isNotEmpty() and
                                densityCairan.text.toString().isNotEmpty()) {
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
                                        delta = dataTabel.text.toString().subSequence(dataTabel.text.toString().length-4,dataTabel.text.toString().length-3).toString()
                                        volumeMid = tabelKalibrasi1.text.toString().toDouble()+(delta.toDouble()/1000/0.01)*(tabelKalibrasi2.text.toString().toDouble()-tabelKalibrasi1.text.toString().toDouble())
                                        volumeApp = volumeMid
                                        volumeAbs = volumeApp*(1.0+((suhuCairan.text.toString().toDouble()-suhuTetap.text.toString().toDouble())*muai.text.toString().toDouble()))
                                        volume = volumeAbs/1000.0
                                        nilaiHasilKalkulator = round(volume*densityCairan.text.toString().toDouble()*100000.0)/100000.0
                                        nilaiHasilKalkulator = round(nilaiHasilKalkulator*10000.0)/10000.0
                                        nilaiHasilKalkulator = round(nilaiHasilKalkulator*1000.0)/1000.0
                                        print(volumeMid)
                                        print(volumeAbs)
                                        print(volume)
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

    // Function is used show the list of inserted data
    private fun setupListOfUserDataIntoRecyclerView(employeesList:ArrayList<UserEntity>,
                                                userDao: UserDao) {

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

    //method for saving records in database
    private fun addRecordUser(userDao: UserDao) {
        val name = _binding3?.etName?.text.toString()
        val nip = _binding3?.etNIPId?.text.toString()

        when {
            name.isEmpty() -> {
                Toast.makeText(context, "Mohon masukkan Nama Anda", Toast.LENGTH_SHORT).show()
            }
            nip.isEmpty() -> {
                Toast.makeText(context, "Mohon masukkan NIP Anda", Toast.LENGTH_SHORT).show()
            }
            digitcheck(nip) -> {
                Toast.makeText(context, "Mohon cek kembali NIP Anda", Toast.LENGTH_SHORT).show()
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

            when {
                name.isEmpty() -> {
                    Toast.makeText(context, "Mohon masukkan Nama Anda", Toast.LENGTH_SHORT).show()
                }
                nip.isEmpty() -> {
                    Toast.makeText(context, "Mohon masukkan NIP Anda", Toast.LENGTH_SHORT).show()
                }
                digitcheck(nip) -> {
                    Toast.makeText(context, "Mohon cek kembali NIP Anda", Toast.LENGTH_SHORT).show()
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

    // Method is used to show the Alert Dialog.
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

    private fun setupListOfExporterDataIntoRecyclerView(exporterList:ArrayList<ExporterEntity>,
                                                        userDao: UserDao) {

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

    // Method is used to show the Alert Dialog.
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

    private fun setupListOfDataIntoRecyclerViewCompany(companyList:ArrayList<CompanyEntity>,
                                                userDao: UserDao) {
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
        builder.setIcon(android.R.drawable.ic_dialog_alert)

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

    private fun Long.longlength() = when(this) {
        0L -> 1
        else -> log10(abs(toDouble())).toInt() + 1
    }

    private fun EditText.placeCursorToEnd() {
        this.setSelection(this.text.length)
    }

    private fun EditText.placeCursorToStart() {
        this.setSelection(0)
    }
}