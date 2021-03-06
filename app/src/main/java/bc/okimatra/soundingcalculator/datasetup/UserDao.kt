package bc.okimatra.soundingcalculator.datasetup

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert
    suspend fun insertUser(pegawaiEntity: PegawaiEntity)

    @Update
    suspend fun updateUser(pegawaiEntity: PegawaiEntity)

    @Delete
    suspend fun deleteUser(pegawaiEntity: PegawaiEntity)

    @Query("Select count(*) from `user-table`")
    fun countAllUser():Flow<Int>

    @Query("Select * from `user-table` ORDER BY nama_pegawai ASC")
    fun fetchAllUser():Flow<List<PegawaiEntity>>

    @Query("Select * from `user-table` where id=:id")
    fun fetchUserById(id:Int):Flow<PegawaiEntity>

    @Query("Select * from `user-table` where nama_pegawai=:name")
    fun fetchUserByName(name:String):Flow<PegawaiEntity>

    @Insert
    suspend fun insertCompany(perusahaanEntity: PerusahaanEntity)

    @Update
    suspend fun updateCompany(perusahaanEntity: PerusahaanEntity)

    @Delete
    suspend fun deleteCompany(perusahaanEntity: PerusahaanEntity)

    @Query("Select count(*) from `company-table`")
    fun countAllCompany():Flow<Int>

    @Query("Select * from `company-table` ORDER BY nama_perusahaan ASC")
    fun fetchAllCompany():Flow<List<PerusahaanEntity>>

    @Query("Select * from `company-table` where id=:id")
    fun fetchCompanyById(id:Int):Flow<PerusahaanEntity>

    @Query("Select * from `company-table` where nama_perusahaan=:name")
    fun fetchCompanyByName(name:String):Flow<PerusahaanEntity>

    @Insert
    suspend fun insertServiceUser(penggunaJasaEntity: PenggunaJasaEntity)

    @Update
    suspend fun updateServiceUser(penggunaJasaEntity: PenggunaJasaEntity)

    @Delete
    suspend fun deleteServiceUser(penggunaJasaEntity: PenggunaJasaEntity)

    @Query("Select count(*) from `service-user-table`")
    fun countAllServiceUser():Flow<Int>

    @Query("Select * from `service-user-table` ORDER BY nama_pengguna_jasa ASC")
    fun fetchAllServiceUser():Flow<List<PenggunaJasaEntity>>

    @Query("Select * from `service-user-table` where id=:id")
    fun fetchServiceUserById(id:Int):Flow<PenggunaJasaEntity>

    @Query("Select * from `service-user-table` where nama_pengguna_jasa=:name")
    fun fetchServiceUserByName(name:String):Flow<PenggunaJasaEntity>

    @Insert
    suspend fun insertSounding(soundingEntity: SoundingEntity)

    @Update
    suspend fun updateSounding(soundingEntity: SoundingEntity)

    @Delete
    suspend fun deleteSounding(soundingEntity: SoundingEntity)

    @Query("Select count(*) from `sounding-raw-table`")
    fun countAllSounding():Flow<Int>

    @Query("Select * from `sounding-raw-table` ORDER BY date DESC")
    fun fetchAllSounding():Flow<List<SoundingEntity>>

    @Query("Select * from `sounding-raw-table` WHERE id=:id")
    fun fetchSoundingById(id:Int):Flow<SoundingEntity>

    @Query("Select * from `sounding-raw-table` WHERE no_tangki=:noTangki AND waktu!=:waktu")
    fun fetchSoundingByNoTangkiNotWaktu(noTangki: String, waktu:String):Flow<List<SoundingEntity>>

    @Query("Select * from `sounding-raw-table` WHERE no_tangki=:noTangki AND waktu=:waktu")
    fun fetchSoundingByNoTangkiAndWaktu(noTangki:String, waktu:String):Flow<SoundingEntity>

    @Insert
    suspend fun insertReport(reportEntity: ReportEntity)

    @Update
    suspend fun updateReport(reportEntity: ReportEntity)

    @Delete
    suspend fun deleteReport(reportEntity: ReportEntity)

    @Query("Select count(*) from `final-report-table`")
    fun countAllReport():Flow<Int>

    @Query("Select * from `final-report-table` ORDER BY date DESC")
    fun fetchAllReport():Flow<List<ReportEntity>>

    @Query("Select * from `final-report-table` where id=:id")
    fun fetchReportById(id:Int):Flow<ReportEntity>

    @Insert
    suspend fun insertUserOffice(kantorEntity: KantorEntity)

    @Update
    suspend fun updateUserOffice(kantorEntity: KantorEntity)

    @Delete
    suspend fun deleteUserOffice(kantorEntity: KantorEntity)

    @Query("Select count(*) from `office-table`")
    fun countAllUserOffice():Flow<Int>

    @Query("Select * from `office-table`")
    fun fetchAllUserOffice():Flow<List<KantorEntity>>

    @Query("Select * from `office-table` WHERE kota=:kota")
    fun fetchUserOfficeByKota(kota:String):Flow<KantorEntity>

    @Query("Select * from `office-table` where id=:id")
    fun fetchUserOfficeById(id:Int):Flow<KantorEntity>
}