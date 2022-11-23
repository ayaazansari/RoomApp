package com.example.roomapp

import android.app.Application

class EmployeeApp:Application() {
    val db by lazy {
        EmployeeDatabase.getInstance(this)
    }
}