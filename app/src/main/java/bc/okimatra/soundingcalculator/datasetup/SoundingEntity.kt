package bc.okimatra.soundingcalculator.datasetup

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sounding-raw-table")
data class SoundingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int=0,
    val tinggi_cairan: Double,
    val suhu_cairan: Double,
    val suhu_kalibrasi_tangki: Double,
    val tinggi_meja: Double,
    val faktor_muai: Double,
    val tinggi_cairan_terkoreksi: Double,
    val volume_kalibrasi1: Double,
    val density_cairan: Double,
    val volume_fraksi: Double,
    val volume_kalibrasi2: Double,
    val volume_mid: Double,
    val volume_app: Double,
    val volume_obs: Double,
    val volume: Double,
    val hasil_sounding: Double,
    val no_tangki: String="",
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
    val bentuk: String="",
    @ColumnInfo(name = "date")
    val waktu_date: Long
        )
