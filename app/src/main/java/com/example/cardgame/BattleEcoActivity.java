package com.example.cardgame;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class BattleEcoActivity extends AppCompatActivity {

    // UI elements for health and rounds.
    private TextView playerHealthText, computerHealthText, roundCounterText, battleLogText;

    // Deck preview and drawn card target for AI (top) and Player (bottom).
    private ImageView aiDeckPreview, aiDrawnCard, aiCharacterImage;
    private ImageView playerDeckPreview, playerDrawnCard, playerCharacterImage;

    // Button for player to draw a card.
    private Button playerDrawButton;

    private int playerHealth = 100;
    private int computerHealth = 100;
    private int currentRound = 1;
    private final int MAX_ROUNDS = 15;

    private Player player, computer;

    // Handler for scheduling AI turn.
    private Handler handler = new Handler();

    // MediaPlayer for looping background music
    private MediaPlayer backgroundMusic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle_eco);

        // Initialize and start background music
        backgroundMusic = MediaPlayer.create(this, R.raw.music_cardgame);
        backgroundMusic.setLooping(true);
        backgroundMusic.start();

        // Bind UI components.
        playerHealthText = findViewById(R.id.playerHealthText);
        computerHealthText = findViewById(R.id.computerHealthText);
        roundCounterText = findViewById(R.id.roundCounterText);
        battleLogText = findViewById(R.id.battleLogText);

        aiDeckPreview = findViewById(R.id.aiDeckPreview);
        aiDrawnCard = findViewById(R.id.aiDrawnCard);
        aiCharacterImage = findViewById(R.id.aiCharacterImage);

        playerDeckPreview = findViewById(R.id.playerDeckPreview);
        playerDrawnCard = findViewById(R.id.playerDrawnCard);
        playerCharacterImage = findViewById(R.id.playerCharacterImage);

        playerDrawButton = findViewById(R.id.playerDrawButton);

        // Initialize players.
        player = new Player("Player", 100);
        computer = new Player("Computer", 100);

        // Add cards to player's deck.
        player.addCard(new BattleCard("Low carbon footprint: Biking", 20, R.drawable.green_cards_front));
        player.addCard(new BattleCard("High carbon footprint: Driving car", -30, R.drawable.red_cards_front));

        // Add cards to computer's deck.
        computer.addCard(new BattleCard("Low carbon footprint: Solar power", 20, R.drawable.green_cards_front));
        computer.addCard(new BattleCard("High carbon footprint: Air travel", -30, R.drawable.red_cards_front));

        // Set initial deck preview images (peek at the next card).
        updateDeckPreview(player, playerDeckPreview);
        updateDeckPreview(computer, aiDeckPreview);

        updateUI();
        updateRoundCounter();

        // Player's turn: tap button to draw card (if rounds remain).
        playerDrawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentRound <= MAX_ROUNDS && playerHealth > 0 && computerHealth > 0) {
                    processPlayerTurn();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop and release background music
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            backgroundMusic.release();
            backgroundMusic = null;
        }
    }

    // Update the deck preview image based on the next random card from the player's deck.
    private void updateDeckPreview(Player p, ImageView deckPreview) {
        BattleCard next = p.drawRandomCard();
        if (next != null) {
            // Set deck preview image based on the card type.
            // For green cards use green_cards_back; for red, red_cards_back.
            if (next.getEcoPoints() > 0) {
                deckPreview.setImageResource(R.drawable.green_cards_back);
            } else {
                deckPreview.setImageResource(R.drawable.red_cards_back);
            }
        }
    }

    // Process the player's turn.
    private void processPlayerTurn() {
        final BattleCard card = player.drawRandomCard();
        if (card != null) {
            // Animate the card draw from player deck preview to player's drawn card target.
            animateDeckDraw(playerDeckPreview, playerDrawnCard, card.getFrontImageResId(), new Runnable() {
                @Override
                public void run() {
                    // Apply the card effect.
                    if (card.getEcoPoints() > 0) {
                        playerHealth += card.getEcoPoints();
                        battleLogText.setText("Player drew GREEN card: " + card.getFact() +
                                "\nPlayer heals +" + card.getEcoPoints());
                    } else {
                        computerHealth += card.getEcoPoints(); // negative value reduces health
                        battleLogText.setText("Player drew RED card: " + card.getFact() +
                                "\nComputer takes " + (-card.getEcoPoints()) + " damage");
                    }
                    updateUI();
                    currentRound++;
                    updateRoundCounter();

                    // Update player's deck preview for next card.
                    updateDeckPreview(player, playerDeckPreview);

                    // If game not over, schedule AI turn after a delay.
                    if (currentRound <= MAX_ROUNDS && playerHealth > 0 && computerHealth > 0) {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                processComputerTurn();
                            }
                        }, 2000);
                    } else {
                        gameOver();
                    }
                }
            });
        }
    }

    // Process the AI (computer) turn.
    private void processComputerTurn() {
        final BattleCard card = computer.drawRandomCard();
        if (card != null) {
            animateDeckDraw(aiDeckPreview, aiDrawnCard, card.getFrontImageResId(), new Runnable() {
                @Override
                public void run() {
                    // Apply card effect.
                    if (card.getEcoPoints() > 0) {
                        computerHealth += card.getEcoPoints();
                        battleLogText.setText("Computer drew GREEN card: " + card.getFact() +
                                "\nComputer heals +" + card.getEcoPoints());
                    } else {
                        playerHealth += card.getEcoPoints(); // negative value reduces health
                        battleLogText.setText("Computer drew RED card: " + card.getFact() +
                                "\nPlayer takes " + (-card.getEcoPoints()) + " damage");
                    }
                    updateUI();
                    currentRound++;
                    updateRoundCounter();

                    // Update AI deck preview for next card.
                    updateDeckPreview(computer, aiDeckPreview);

                    // Re-enable player's draw button if game is still on.
                    if (currentRound <= MAX_ROUNDS && playerHealth > 0 && computerHealth > 0) {
                        playerDrawButton.setEnabled(true);
                    } else {
                        gameOver();
                    }
                }
            });
        }
    }

    // Updates the health displays.
    private void updateUI() {
        playerHealthText.setText("Player Health: " + playerHealth);
        computerHealthText.setText("Computer Health: " + computerHealth);
    }

    // Update round counter, change background, and update boss image every 5 rounds.
    private void updateRoundCounter() {
        roundCounterText.setText("Round: " + currentRound + " / " + MAX_ROUNDS);
        View root = findViewById(R.id.battleEcoRoot);
        if (currentRound <= 5) {
            root.setBackgroundResource(R.drawable.background1);
            aiCharacterImage.setImageResource(R.drawable.burger_boss);
        } else if (currentRound <= 10) {
            root.setBackgroundResource(R.drawable.background2);
            aiCharacterImage.setImageResource(R.drawable.taco_boss);
        } else {
            root.setBackgroundResource(R.drawable.background3);
            aiCharacterImage.setImageResource(R.drawable.cake_boss);
        }
    }

    // Ends the game.
    private void gameOver() {
        playerDrawButton.setEnabled(false);
        if (playerHealth <= 0) {
            battleLogText.setText("Game Over! Player is defeated.");
        } else if (computerHealth <= 0) {
            battleLogText.setText("Game Over! Computer is defeated.");
        } else {
            battleLogText.setText("Game Over! Rounds finished.");
        }
    }

    /**
     * Animates a card draw by taking the deck preview image as the start and moving a temporary drawn card
     * (with a flip animation) to the target drawn card view. Once the animation finishes, the onAnimationEnd callback is called.
     */
    private void animateDeckDraw(final ImageView deckPreview, final ImageView drawnCard, final int newImageResId, final Runnable onAnimationEnd) {
        // Play the flip sound
        final MediaPlayer mp = MediaPlayer.create(BattleEcoActivity.this, R.raw.cardflip);
        mp.start();
        mp.setOnCompletionListener(mediaPlayer -> mediaPlayer.release());

        // Prepare the drawn card
        drawnCard.setImageResource(newImageResId);
        drawnCard.setVisibility(View.INVISIBLE);

        // Get on-screen locations for the deck preview (start) and drawnCard (target).
        final int[] startPos = new int[2];
        final int[] targetPos = new int[2];
        deckPreview.getLocationOnScreen(startPos);
        drawnCard.getLocationOnScreen(targetPos);

        // Calculate translation deltas
        final float deltaX = startPos[0] - targetPos[0];
        final float deltaY = startPos[1] - targetPos[1];

        // Set initial translation so the card starts at the deck preview location
        drawnCard.setTranslationX(deltaX);
        drawnCard.setTranslationY(deltaY);
        drawnCard.setRotationY(0f);
        drawnCard.setVisibility(View.VISIBLE);

        // Animate a flip (0 -> 90), then translation, then flip (270 -> 360)
        ObjectAnimator flipOut = ObjectAnimator.ofFloat(drawnCard, "rotationY", 0f, 90f);
        flipOut.setDuration(200);
        flipOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Simulate flipping to the back
                drawnCard.setRotationY(270f);
                ObjectAnimator flipIn = ObjectAnimator.ofFloat(drawnCard, "rotationY", 270f, 360f);
                flipIn.setDuration(200);
                flipIn.start();
            }
        });

        // Animate translation from the deck preview position to the drawn card position
        ObjectAnimator translateX = ObjectAnimator.ofFloat(drawnCard, "translationX", deltaX, 0f);
        ObjectAnimator translateY = ObjectAnimator.ofFloat(drawnCard, "translationY", deltaY, 0f);
        translateX.setDuration(400);
        translateY.setDuration(400);

        // Start all animations
        translateX.start();
        translateY.start();
        flipOut.start();

        // Callback when translation finishes
        translateY.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (onAnimationEnd != null) {
                    onAnimationEnd.run();
                }
            }
        });
    }
}
