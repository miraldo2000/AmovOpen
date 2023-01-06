package com.example.tpamov

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.tpamov.activities.CreditsActivity
import com.example.tpamov.activities.GameActivity
import com.example.tpamov.activities.MultiGameActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    val db = Firebase.firestore
    lateinit var auth : FirebaseAuth
    private lateinit var tvBestResults : TextView
    private lateinit var tvBestTimes : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //val firebase : DatabaseReference = FirebaseDatabase.getInstance().getReference()


        val btnSingle = findViewById<Button>(R.id.btnSingle)

        btnSingle.setOnClickListener {
            startActivity(GameActivity.start(this))
        }

        //initScores()
        //initTimes()

        tvBestResults = findViewById<TextView>(R.id.tvBestResults)
        tvBestTimes = findViewById<TextView>(R.id.tvBestTimes)


        // btnSingle.setBackgroundColor(Color.WHITE)
        findViewById<Button>(R.id.btnMultiCli).setOnClickListener {
            startActivity(MultiGameActivity.getClientModeIntent(this))
        }
        findViewById<Button>(R.id.btnMultiServ).setOnClickListener {
            startActivity(MultiGameActivity.getServerModeIntent(this))
        }
        findViewById<Button>(R.id.btnCredits).setOnClickListener {
            startActivity(CreditsActivity.start(this))
        }

        refreshViews()

    }


    fun initScores(){
        db.collection("users").document("Top 5 Scores")
            .set(mapOf(
                "Top 1" to 0,
                "Top 2" to 0,
                "Top 3" to 0,
                "Top 4" to 0,
                "Top 5" to 0,
                ))
    }
    fun initTimes(){
        db.collection("users").document("Top 5 Times")
            .set(mapOf(
                "Top 1" to 0,
                "Top 2" to 0,
                "Top 3" to 0,
                "Top 4" to 0,
                "Top 5" to 0,
            ))
    }

    fun refreshViews(){
        db.collection("users").document("Top 5 Scores").get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val strAux = document.data!!.get("Top 1").toString().plus("\n")
                        .plus(document.data!!.get("Top 2").toString()).plus("\n")
                        .plus(document.data!!.get("Top 3").toString()).plus("\n")
                        .plus(document.data!!.get("Top 4").toString()).plus("\n")
                        .plus(document.data!!.get("Top 5").toString())

                    tvBestResults.text = getString(R.string.best_scores).plus("\n").plus(strAux)
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
        db.collection("users").document("Top 5 Times").get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val strAux = document.data!!.get("Top 1").toString().plus("\n")
                        .plus(document.data!!.get("Top 2").toString()).plus("\n")
                        .plus(document.data!!.get("Top 3").toString()).plus("\n")
                        .plus(document.data!!.get("Top 4").toString()).plus("\n")
                        .plus(document.data!!.get("Top 5").toString())

                    tvBestTimes.text = getString(R.string.best_times).plus("\n").plus(strAux)
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }

    companion object refreshViews{
        fun refresh() = refreshViews
    }
}
