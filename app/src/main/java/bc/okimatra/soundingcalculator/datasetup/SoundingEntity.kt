package bc.okimatra.soundingcalculator.datasetup

import androidx.room.Entity
import androidx.room.PrimaryKey

//creating a Data Model Class
@Entity(tableName = "sounding-raw-table")
data class SoundingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int=0,
    val tinggiCairan: Double,
    val suhuCairan: Double,
    val suhuKalibrasiTangki: Double,
    val tinggiMeja: Double,
    val faktorMuai: Double,
    val tinggiCairanTerkoreksi: Double,
    val volumeKalibrasi1: Double,
    val densityCairan: Double,
    val volumeFraksi: Double,
    val volumeKalibrasi2: Double,
    val volumeMid: Double,
    val volumeApp: Double,
    val volumeObs: Double,
    val volume: Double,
    val hasilSounding: Double,
    val noTangki: String="",
    val pegawai_sounding: String="",
    val nip_pegawai: String="",
    val pengguna_jasa_sounding: String="",
    val jabatan_pengguna_jasa: String="",
    val perusahaan_sounding: String="",
    val npwp_perusahaan_sounding: String="",
    val alamat_perusahaan_sounding: String="",
    val lokasi_sounding: String="",
    val waktu: String="",
    val nomor_dokumen: String="",
    val produk: String="",
    val bentuk: String=""
        )
