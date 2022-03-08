package bc.okimatra.soundingcalculator.datasetup

import androidx.room.Entity
import androidx.room.PrimaryKey

//creating a Data Model Class
@Entity(tableName = "exporter-table")
data class ExporterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int=0,
    val nama: String="",
    val jabatan: String=""
        )
