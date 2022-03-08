package bc.okimatra.soundingcalculator.datasetup

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert
    suspend fun insertUser(userEntity: UserEntity)

    @Update
    suspend fun updateUser(userEntity: UserEntity)

    @Delete
    suspend fun deleteUser(userEntity: UserEntity)

    @Query("Select count(*) from `user-table`")
    fun countAllUser():Flow<Int>

    @Query("Select * from `user-table` ORDER BY nama ASC")
    fun fetchAllUser():Flow<List<UserEntity>>

    @Query("Select * from `user-table` where id=:id")
    fun fetchUserById(id:Int):Flow<UserEntity>

    @Insert
    suspend fun insertCompany(companyEntity: CompanyEntity)

    @Update
    suspend fun updateCompany(companyEntity: CompanyEntity)

    @Delete
    suspend fun deleteCompany(companyEntity: CompanyEntity)

    @Query("Select count(*) from `company-table`")
    fun countAllCompany():Flow<Int>

    @Query("Select * from `company-table` ORDER BY nama ASC")
    fun fetchAllCompany():Flow<List<CompanyEntity>>

    @Query("Select * from `company-table` where id=:id")
    fun fetchCompanyById(id:Int):Flow<CompanyEntity>

    @Insert
    suspend fun insertExporter(exporterEntity: ExporterEntity)

    @Update
    suspend fun updateExporter(exporterEntity: ExporterEntity)

    @Delete
    suspend fun deleteExporter(exporterEntity: ExporterEntity)

    @Query("Select count(*) from `exporter-table`")
    fun countAllExporter():Flow<Int>

    @Query("Select * from `exporter-table` ORDER BY nama ASC")
    fun fetchAllExporter():Flow<List<ExporterEntity>>

    @Query("Select * from `exporter-table` where id=:id")
    fun fetchExporterById(id:Int):Flow<ExporterEntity>
}