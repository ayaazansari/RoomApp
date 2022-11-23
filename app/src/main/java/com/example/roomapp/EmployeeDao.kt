package com.example.roomapp

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {
    @Insert
    suspend fun insert(employeeEntity: EmployeeEntity)

    @Update
    suspend fun update(employeeEntity: EmployeeEntity)

    @Delete
    suspend fun delete(employeeEntity: EmployeeEntity)

    @Query("Select * from `employee-table`")
    fun fetchAllEmployees():Flow<List<EmployeeEntity>>

    @Query("Select * from `employee-table` where id=:id")
    fun fetEmployeeById(id:Int):Flow<EmployeeEntity>
}