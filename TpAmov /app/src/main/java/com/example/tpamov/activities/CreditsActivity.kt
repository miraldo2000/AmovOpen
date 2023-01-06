package com.example.tpamov.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.tpamov.R

class CreditsActivity : AppCompatActivity() {
    companion object ContextStatic{
        @SuppressLint("StaticFieldLeak")
        fun start(context : Context) : Intent {
            return Intent(context, CreditsActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_credits)

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
            //startActivity(CreditsActivity.start(this))
        }
    }
}