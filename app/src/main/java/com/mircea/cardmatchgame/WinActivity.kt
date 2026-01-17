package com.mircea.cardmatchgame

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class WinActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_win)

        auth = FirebaseAuth.getInstance()

        val playerName = intent.getStringExtra("PLAYER_NAME") ?: "Player"

        val winText = findViewById<TextView>(R.id.winText)
        winText.text = getString(R.string.game_win, playerName)

        val newGameBtn = findViewById<Button>(R.id.newGameButton)
        newGameBtn.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("PLAYER_NAME", playerName)
            startActivity(intent)
            finish()
        }

        val signOutBtn = findViewById<Button>(R.id.signOutButton)
        signOutBtn.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}
