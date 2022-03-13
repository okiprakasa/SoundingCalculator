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
}