package com.mircea.cardmatchgame

import android.os.Bundle
import android.content.Intent
import android.widget.TextView
import android.widget.Button
import android.widget.GridLayout
import android.view.ViewGroup
import android.content.res.ColorStateList
import androidx.core.graphics.toColorInt
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class GameActivity : AppCompatActivity() {
    private lateinit var cardList: MutableList<Card>

    private var firstFlippedCard: Card? = null
    private var secondFlippedCard: Card? = null
    private var firstFlippedButton: Button? = null
    private var secondFlippedButton: Button? = null
    private var pendingReset: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val playerName = intent.getStringExtra("PLAYER_NAME") ?: "Player"
        val welcomeText = findViewById<TextView>(R.id.welcomeText)
        welcomeText.text = getString(R.string.game_title, playerName)

        val emojis = listOf("🐶", "🐱", "🐭", "🦊", "🐼")

        cardList = mutableListOf()
        var idCounter = 0
        for (emoji in emojis) {
            cardList.add(Card(idCounter++, emoji))
            cardList.add(Card(idCounter++, emoji))
        }

        cardList.shuffle()

        val gridLayout = findViewById<GridLayout>(R.id.cardGrid)

        fun resetOpenCards() {
            firstFlippedButton?.text = "❓"
            secondFlippedButton?.text = "❓"

            firstFlippedCard?.isFaceUp = false
            secondFlippedCard?.isFaceUp = false

            firstFlippedCard = null
            secondFlippedCard = null
            firstFlippedButton = null
            secondFlippedButton = null
        }

        fun compareCards() {
            val firstCard = firstFlippedCard ?: return
            val secondCard = secondFlippedCard ?: return
            val firstButton = firstFlippedButton!!
            val secondButton = secondFlippedButton!!

            if (firstCard.emoji == secondCard.emoji) {
                firstCard.isMatched = true
                secondCard.isMatched = true

                val matchColor = "#C8E6C9".toColorInt()
                firstButton.backgroundTintList = ColorStateList.valueOf(matchColor)
                secondButton.backgroundTintList = ColorStateList.valueOf(matchColor)

                secondButton.postDelayed({
                    firstButton.visibility = Button.INVISIBLE
                    secondButton.visibility = Button.INVISIBLE

                    // Check for win AFTER the cards disappear
                    if (cardList.all { it.isMatched }) {
                        val intent = Intent(this, WinActivity::class.java)
                        intent.putExtra("PLAYER_NAME", playerName)
                        startActivity(intent)
                        finish()
                    }
                }, 1000)

                firstFlippedCard = null
                firstFlippedButton = null
                secondFlippedCard = null
                secondFlippedButton = null
            } else {
                val runnable = Runnable {
                    resetOpenCards()
                    pendingReset = null
                }
                pendingReset = runnable
                secondButton.postDelayed(runnable, 1000)
            }
        }

        fun setupGrid() {
            gridLayout.removeAllViews()

            cardList.shuffle()

            firstFlippedCard = null
            secondFlippedCard = null
            firstFlippedButton = null
            secondFlippedButton = null

            for (card in cardList) {
                card.isFaceUp = false
                card.isMatched = false

                val button = Button(this).apply {
                    text = "❓"
                    textSize = 32f
                    setPadding(0, 60, 0, 60)
                    tag = card.id
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = 0
                        height = ViewGroup.LayoutParams.WRAP_CONTENT
                        columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                        rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                        setMargins(8, 8, 8, 8)
                    }

                    setOnClickListener {
                        if (card.isFaceUp || card.isMatched) return@setOnClickListener

                        pendingReset?.let {
                            secondFlippedButton?.removeCallbacks(it)
                            resetOpenCards()  // immediately flip the previous cards back
                            pendingReset = null
                        }

                        text = card.emoji
                        card.isFaceUp = true

                        if (firstFlippedCard == null) {
                            firstFlippedCard = card
                            firstFlippedButton = this
                        } else {
                            secondFlippedCard = card
                            secondFlippedButton = this
                            compareCards()
                        }
                    }
                }

                gridLayout.addView(button)
            }
        }

        val resetBtn = findViewById<Button>(R.id.resetButton)
        resetBtn.setOnClickListener {
            setupGrid()
        }

        val signOutBtn = findViewById<Button>(R.id.signOutButton)
        signOutBtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        setupGrid()
    }
}
