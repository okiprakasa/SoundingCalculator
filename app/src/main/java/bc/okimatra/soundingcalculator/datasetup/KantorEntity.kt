package bc.okimatra.soundingcalculator.datasetup

import androidx.room.Entity
import androidx.room.PrimaryKey

//creating a Data Model Class
@Entity(tableName = "office-table")
data class KantorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int=0,
    val kota: String="",
    val kantor: String="",
    val kanwil: String="",
    val lokasi_ba: String="",
    val format_ba_sounding: String="",
    val format_ba_sampling: String="",
    val format_3d: String="",
    val alamat_kantor: String="",
    val telepon: String="",
    val faximile: String="",
        )
