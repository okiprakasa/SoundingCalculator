package bc.okimatra.soundingcalculator.datasetup

import androidx.room.Entity
import androidx.room.PrimaryKey

//creating a Data Model Class
@Entity(tableName = "user-table")
data class PegawaiEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int=0,
    val nama_pegawai: String="",
    val nip: String="",
    val gol: String="",
    val pangkat: String="",
    val jabatan_pegawai: String="",
    val kota_pegawai: String="",
    val kantor_pegawai: String="",
    val kanwil_pegawai: String="",
    val lokasi_ba_pegawai: String="",
    val format_ba_pegawai: String="",
    val format_3d_pegawai: String=""
        )
