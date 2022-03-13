package bc.okimatra.soundingcalculator.datasetup

import androidx.room.Entity
import androidx.room.PrimaryKey

//creating a Data Model Class
@Entity(tableName = "company-table")
data class SoundingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int=0,
    val namaPerusahaan: String="",
    val waktu: String="",
    val nomorPEB: String="",
    val produk: String="",
    val bentuk: String="",
    val namaTangki: String="",
    val tinggiCairan: Double,
    val suhuCairan: Double,
    val suhuKalibrasiTangki: Double,
    val tinggiMeja: Double,
    val faktorMuai: Double,
    val tinggiCairanCorrected: Double = tinggiCairan+tinggiMeja,
    val tabelKalibrasi1: Double,
    val tabelFraksi: Double = 0.0,
    val densityCairan: Double,
    val tabelKalibrasi2: Double = tabelKalibrasi1,
    val volumeMid: Double,
    val volumeApp: Double = tabelFraksi,
    val hasilSounding: Double
        )
