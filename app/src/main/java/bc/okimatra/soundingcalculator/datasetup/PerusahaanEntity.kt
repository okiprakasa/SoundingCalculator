package bc.okimatra.soundingcalculator.datasetup

import androidx.room.Entity
import androidx.room.PrimaryKey

//creating a Data Model Class
@Entity(tableName = "company-table")
data class PerusahaanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int=0,
    val nama_perusahaan: String="",
    val npwp: String="",
    val alamat: String=""
        )
