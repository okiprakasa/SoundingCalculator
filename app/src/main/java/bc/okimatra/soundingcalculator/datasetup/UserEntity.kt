package bc.okimatra.soundingcalculator.datasetup

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

//creating a Data Model Class
@Entity(tableName = "user-table")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int=0,
    val nama: String="",
    val nip: Long=0
        )