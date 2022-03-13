package bc.okimatra.soundingcalculator.datasetup

import androidx.room.Entity
import androidx.room.PrimaryKey

//creating a Data Model Class
@Entity(tableName = "sounding-table")
data class SoundingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int=0,
    val noTangki: String="",
    val pegawai_sounding: String="",
    val nip_pegawai: Long=0,
    val pengguna_jasa_sounding: String="",
    val jabatan_pengguna_jasa: String="",
    val perusahaan_sounding: String="",
    val npwp_perusahaan_sounding: String="",
    val alamat_perusahaan_sounding: String="",
    val lokasi_sounding: String="",
    val waktu: String="",
    val nomor_dokumen: String="",
    val produk: String="",
    val bentuk: String="",
    val tinggiCairan: Double,
    val suhuCairan: Double,
    val suhuKalibrasiTangki: Double,
    val tinggiMeja: Double,
    val faktorMuai: Double,
    val tinggiCairanCorrected: Double = tinggiCairan+tinggiMeja,
    val tabelKalibrasi1: Double,
    val densityCairan: Double,
    val tabelFraksi: Double = 0.0,
    val tabelKalibrasi2: Double = tabelKalibrasi1,
    val volumeMid: Double,
    val volumeApp: Double = tabelFraksi,
    val hasilSounding: Double
        )
