package com.example.roomapp

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.roomapp.databinding.ActivityMainBinding
import com.example.roomapp.databinding.UpdateRecordBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val employeeDao = (application as EmployeeApp).db.employeeDao()

        binding?.btnAddRecord?.setOnClickListener {
            addRecord(employeeDao)
        }

        lifecycleScope.launch {
            employeeDao.fetchAllEmployees().collect {
                val list = ArrayList(it)
                setupListOfDataIntoRecyclerView(list, employeeDao)
            }
        }
    }

    private fun addRecord(employeeDao: EmployeeDao) {
        val name = binding?.name?.text.toString()
        val email = binding?.emailId?.text.toString()

        if (name.isNotEmpty() and email.isNotEmpty()) {
            lifecycleScope.launch {
                employeeDao.insert(EmployeeEntity(name = name, email = email))
                Toast.makeText(applicationContext, "Record saved", Toast.LENGTH_SHORT).show()

                binding?.name?.text?.clear()
                binding?.emailId?.text?.clear()
            }
        } else {
            Toast.makeText(
                this, "Name or email can not be blank",
                Toast.LENGTH_SHORT
            )
                .show()
        }
    }

    private fun setupListOfDataIntoRecyclerView(
        employeeList: ArrayList<EmployeeEntity>,
        employeeDao: EmployeeDao
    ) {
        if (employeeList.isNotEmpty()) {
            val itemAdapter = ItemAdapter(employeeList,
                { updateId ->
                    updateRecordDialog(updateId, employeeDao)
                },
                { deleteId ->
                    deleteRecordAlertDialog(deleteId, employeeDao)
                }
            )
            binding?.rvList?.layoutManager = LinearLayoutManager(this)
            binding?.rvList?.adapter = itemAdapter
            binding?.rvList?.visibility = View.VISIBLE
            binding?.noRecordAvailable?.visibility = View.GONE
        } else {
            binding?.rvList?.visibility = View.GONE
            binding?.noRecordAvailable?.visibility = View.VISIBLE
        }
    }

    private fun updateRecordDialog(id: Int, employeeDao: EmployeeDao) {
        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)
        val binding = UpdateRecordBinding.inflate(layoutInflater)
        updateDialog.setContentView(binding.root)

        lifecycleScope.launch {
            employeeDao.fetEmployeeById(id).collect {
                if (it != null) {
                    binding.etUpdateName.setText(it.name)
                    binding.etUpdateEmail.setText(it.email)
                }
            }
        }
        binding.tvUpdate.setOnClickListener {
            val name = binding.etUpdateName.text.toString()
            val email = binding.etUpdateEmail.text.toString()

            if (name.isNotEmpty() and email.isNotEmpty()) {
                lifecycleScope.launch {
                    employeeDao.update(EmployeeEntity(id, name, email))
                    Toast.makeText(
                        applicationContext,
                        "Record Updated",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateDialog.dismiss()
                }
            } else {
                Toast.makeText(
                    applicationContext,
                    "Name or email can not be blank",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        binding.tvCancel.setOnClickListener {
            updateDialog.dismiss()
        }
        updateDialog.show()
    }

    private fun deleteRecordAlertDialog(id: Int, employeeDao: EmployeeDao) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Record")
        builder.setIcon(R.drawable.ic_dialog_alert)
        lifecycleScope.launch {
            employeeDao.fetEmployeeById(id).collect {
                if (it != null) {
                    builder.setMessage("Are you sure you want to delete ${it.name}")
                }
            }
        }
        builder.setPositiveButton("Yes") { dialogInterface, _ ->
            lifecycleScope.launch {
                employeeDao.delete(EmployeeEntity(id))
                Toast.makeText(
                    applicationContext,
                    "Record deleted success",
                    Toast.LENGTH_SHORT
                ).show()
            }
            dialogInterface.dismiss()
        }

        builder.setNegativeButton("No") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
}