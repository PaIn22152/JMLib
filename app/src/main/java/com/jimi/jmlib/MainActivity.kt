package com.jimi.jmlib

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.jimi.jmutil.TestJM

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        TestJM.gettestStr()
    }
}