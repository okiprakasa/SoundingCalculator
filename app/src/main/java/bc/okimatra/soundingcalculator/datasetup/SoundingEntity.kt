package bc.okimatra.soundingcalculator.datasetup

import androidx.room.Entity
import androidx.room.PrimaryKey

//creating a Data Model Class
@Entity(tableName = "company-table")
data class SoundingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int=0,
    val namaPerusahaan: String="",
    val hariTanggal: String="",
    val jam: String="",
    val nomorDokumen: String="",
    val produk: String="",
    val lokasi: String="",
    val bentuk: String="",
    val namaTangki: String="",
    val tinggiCairan: Double,
    val tinggiMeja: Double,
    val tinggiCairanCorrected: Double = tinggiCairan+tinggiMeja,
    val suhuCairan: Double,
    val suhuKalibrasiTangki: Double,
    val faktorMuai: Double,
    val tabelKalibrasi1: Double,
    val tabelKalibrasi2: Double,
    val tabelFraksi: Double = 0.0,
    val densityCairan: Double,
    val volumeApp: Double = tabelFraksi,
    val hasilSounding: Double
        )
