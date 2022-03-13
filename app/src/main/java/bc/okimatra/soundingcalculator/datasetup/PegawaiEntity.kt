package bc.okimatra.soundingcalculator.datasetup

import androidx.room.Entity
import androidx.room.PrimaryKey

//creating a Data Model Class
@Entity(tableName = "user-table")
data class PegawaiEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int=0,
    val nama_pegawai: String="",
    val nip: String=""
        )
