package bc.okimatra.soundingcalculator.datasetup

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "final-report-table")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int=0,
    val tinggi_cairan: ArrayList<Double> = arrayListOf(0.0),
    val suhu_cairan: ArrayList<Double> = arrayListOf(0.0),
    val suhu_kalibrasi_tangki: ArrayList<Double> = arrayListOf(0.0),
    val tinggi_meja: ArrayList<Double> = arrayListOf(0.0),
    val faktor_muai: ArrayList<Double> = arrayListOf(0.0),
    val tinggi_cairan_terkoreksi: ArrayList<Double> = arrayListOf(0.0),
    val volume_kalibrasi1: ArrayList<Double> = arrayListOf(0.0),
    val density_cairan: ArrayList<Double> = arrayListOf(0.0),
    val volume_fraksi: ArrayList<Double> = arrayListOf(0.0),
    val volume_kalibrasi2: ArrayList<Double> = arrayListOf(0.0),
    val volume_mid: ArrayList<Double> = arrayListOf(0.0),
    val volume_app: ArrayList<Double> = arrayListOf(0.0),
    val volume_obs: ArrayList<Double> = arrayListOf(0.0),
    val volume: ArrayList<Double> = arrayListOf(0.0),
    val hasil_sounding: ArrayList<Double> = arrayListOf(0.0),
    val no_tangki: ArrayList<String> = arrayListOf(""),
    val pegawai_sounding: ArrayList<String> = arrayListOf(""),
    val nip_pegawai: ArrayList<String> = arrayListOf(""),
    val pengguna_jasa_sounding: ArrayList<String> = arrayListOf(""),
    val jabatan_pengguna_jasa: ArrayList<String> = arrayListOf(""),
    val perusahaan_sounding: ArrayList<String> = arrayListOf(""),
    val npwp_perusahaan_sounding: ArrayList<String> = arrayListOf(""),
    val alamat_perusahaan_sounding: ArrayList<String> = arrayListOf(""),
    val lokasi_sounding: ArrayList<String> = arrayListOf(""),
    val waktu: ArrayList<String> = arrayListOf(""),
    @ColumnInfo(name = "date")
    val waktu_date: Long = 0L,
    val judulKalibrasi1: ArrayList<String> = arrayListOf(""),
    val judulKalibrasi2: ArrayList<String> = arrayListOf(""),
    val judulFraksi: ArrayList<String> = arrayListOf(""),
    val judulDataTabel: ArrayList<String> = arrayListOf(""),
    val hasil: ArrayList<String> = arrayListOf(""),
    val hasil_perhitungan: String = "",
    val nomor_dokumen: String = "",
    val produk: String = "",
    val bentuk: String = "",
    val nama_sarkut: String = "",
    val nomor_ba: String = "",
    val tanggal_ba: String = "",
    val lokasi_ba: String = "",
    val jumlah_contoh: String = "",
    val waktu_aju: String = ""
        )
