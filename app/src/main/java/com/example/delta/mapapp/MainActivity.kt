package com.example.delta.mapapp

import android.app.Dialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
        const val ERROR_DIALOG_REQUEST = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (isServicesOK()) {
            init()
        }
    }

    fun init() {
        var btnMap: Button = findViewById<Button>(R.id.btnMap)
        btnMap.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent);
        }

    }

    fun isServicesOK(): Boolean {

        Log.d(TAG, "isServicesOK: Checking google services version")

        var available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable((this))

        if (available == ConnectionResult.SUCCESS) {
            //it is ok
            Log.d(TAG, "isServiceOK: Google play services is working")
            return true
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //error occurred
            Log.d(TAG, "isServicesOK: an error occurred but it is resolvable")
            var dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, available, ERROR_DIALOG_REQUEST)
            dialog.show()

        } else {
            Toast.makeText(this, "You can't request maps", Toast.LENGTH_SHORT).show()
        }
        return false

    }


}
