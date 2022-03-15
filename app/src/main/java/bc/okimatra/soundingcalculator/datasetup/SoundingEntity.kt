package bc.okimatra.soundingcalculator.datasetup

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sounding-raw-table")
data class SoundingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int=0,
    val tinggi_cairan: Double = 0.0,
    val suhu_cairan: Double = 0.0,
    val suhu_kalibrasi_tangki: Double = 0.0,
    val tinggi_meja: Double = 0.0,
    val faktor_muai: Double = 0.0,
    val tinggi_cairan_terkoreksi: Double = 0.0,
    val volume_kalibrasi1: Double = 0.0,
    val density_cairan: Double = 0.0,
    val volume_fraksi: Double = 0.0,
    val volume_kalibrasi2: Double = 0.0,
    val volume_mid: Double = 0.0,
    val volume_app: Double = 0.0,
    val volume_obs: Double = 0.0,
    val volume: Double = 0.0,
    val hasil_sounding: Double = 0.0,
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
    val waktu_date: Long = 0L
        )
