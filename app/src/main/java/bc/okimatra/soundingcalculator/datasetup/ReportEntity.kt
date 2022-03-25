package bc.okimatra.soundingcalculator.datasetup

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "final-report-table")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int=0,
    val tinggi_cairan: ArrayList<Double>,
    val suhu_cairan: ArrayList<Double>,
    val suhu_kalibrasi_tangki: ArrayList<Double>,
    val tinggi_meja: ArrayList<Double>,
    val faktor_muai: ArrayList<Double>,
    val tinggi_cairan_terkoreksi: ArrayList<Double>,
    val volume_kalibrasi1: ArrayList<Double>,
    val density_cairan: ArrayList<Double>,
    val volume_fraksi: ArrayList<Double>,
    val volume_kalibrasi2: ArrayList<Double>,
    val volume_mid: ArrayList<Double>,
    val volume_app: ArrayList<Double>,
    val volume_obs: ArrayList<Double>,
    val volume: ArrayList<Double>,
    val hasil_sounding: ArrayList<Double>,
    val no_tangki: ArrayList<String>,
    val pegawai_sounding: ArrayList<String>,
    val nip_pegawai: ArrayList<String>,
    val pengguna_jasa_sounding: ArrayList<String>,
    val jabatan_pengguna_jasa: ArrayList<String>,
    val perusahaan_sounding: ArrayList<String>,
    val npwp_perusahaan_sounding: ArrayList<String>,
    val alamat_perusahaan_sounding: ArrayList<String>,
    val lokasi_sounding: ArrayList<String>,
    val waktu: ArrayList<String>,
    val nomor_dokumen: ArrayList<String>,
    val produk: ArrayList<String>,
    val bentuk: ArrayList<String>,
    @ColumnInfo(name = "date")
    val waktu_date: Long = 0L,
    val judulKalibrasi1: ArrayList<String>,
    val judulKalibrasi2: ArrayList<String>,
    val judulFraksi: ArrayList<String>,
    val judulDataTabel: ArrayList<String>,
    val nama_sarkut: String,
    val tanggal_ba: String,
    val lokasi_ba: String,
    val jumlah_contoh: String,
    val waktu_aju: String
        )
