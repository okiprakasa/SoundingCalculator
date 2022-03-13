package bc.okimatra.soundingcalculator.datasetup

import androidx.room.Entity
import androidx.room.PrimaryKey

//creating a Data Model Class
@Entity(tableName = "service-user-table")
data class PenggunaJasaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int=0,
    val nama_pengguna_jasa: String="",
    val jabatan: String="",
    val perusahaan_pengguna_jasa: String="",
    val npwp_perusahaan: String="",
    val alamat_perusahaan: String=""
        )
