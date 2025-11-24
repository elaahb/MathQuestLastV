package com.example.mathquest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Random;

public class GameActivity extends AppCompatActivity {
    private static final String TAG = "GameActivity";

    int num1, num2, correctAnswer;
    Random random = new Random();
    ProgressBar progressBar;
    TextView questionText, timerText, scoreText;
    ProgressBar questionProgressBar;
    TextInputEditText answerInput;
    Button validateButton, nextButton;
    int questionCount = 1;
    final int totalQuestions = 10;

    CountDownTimer countDownTimer;
    int timeLeft = 30;
    int score = 0;
    String level;
    int miniLevel = 1;
    boolean isAnswered = false;

    private DatabaseReference mDatabase;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game2);

        try {
            mDatabase = FirebaseDatabase.getInstance().getReference();
            SharedPreferences userPrefs = getSharedPreferences("CurrentUser", MODE_PRIVATE);
            userId = userPrefs.getString("id", "");
            Log.d(TAG, "Firebase initialized, userId: " + userId);
        } catch (Exception e) {
            Log.e(TAG, "Firebase initialization failed", e);
        }

        questionText = findViewById(R.id.questionText);
        timerText = findViewById(R.id.timerText);
        progressBar = findViewById(R.id.progressBar);
        questionProgressBar = findViewById(R.id.questionProgressBar);
        answerInput = findViewById(R.id.answerInput);
        validateButton = findViewById(R.id.validateButton);
        nextButton = findViewById(R.id.nextButton);
        scoreText = findViewById(R.id.scoreText);

        questionProgressBar.setMax(totalQuestions);
        questionProgressBar.setProgress(questionCount);
        scoreText.setText("Score : 0");

        level = getIntent().getStringExtra("level");
        miniLevel = getIntent().getIntExtra("miniLevel", 1);

        validateButton.setOnClickListener(v -> validateAnswer());
        nextButton.setOnClickListener(v -> nextQuestion());

        generateQuestion();
        startTimer();
    }

    private void generateQuestion() {
        String operation = "+";

        if (level == null) {
            level = "facile";
        }

        switch (level) {
            case "facile":
                // Facile: Addition (+) and Subtraction (-)
                int maxFacile = 10 + (miniLevel * 5); // Increases with mini-level
                num1 = random.nextInt(maxFacile) + 1;
                num2 = random.nextInt(maxFacile) + 1;

                if (random.nextBoolean()) {
                    correctAnswer = num1 + num2;
                    operation = "+";
                } else {
                    if (num1 < num2) {
                        int temp = num1;
                        num1 = num2;
                        num2 = temp;
                    }
                    correctAnswer = num1 - num2;
                    operation = "-";
                }
                questionText.setText(num1 + " " + operation + " " + num2 + " = ?");
                break;

            case "moyen":
                // Moyen: Addition (+), Subtraction (-), Multiplication (×)
                int maxMoyen = 20 + (miniLevel * 10);
                num1 = random.nextInt(maxMoyen) + 1;
                num2 = random.nextInt(maxMoyen) + 1;

                int opType = random.nextInt(3);
                if (opType == 0) {
                    correctAnswer = num1 + num2;
                    operation = "+";
                } else if (opType == 1) {
                    if (num1 < num2) {
                        int temp = num1;
                        num1 = num2;
                        num2 = temp;
                    }
                    correctAnswer = num1 - num2;
                    operation = "-";
                } else {
                    num2 = random.nextInt(12) + 1; // Smaller for multiplication
                    correctAnswer = num1 * num2;
                    operation = "×";
                }
                questionText.setText(num1 + " " + operation + " " + num2 + " = ?");
                break;

            case "difficile":
                // Difficile: Multiplication (×), Square Root (√), Power (^)
                int diffType = random.nextInt(3);

                if (diffType == 0) {
                    // Multiplication
                    num1 = random.nextInt(20 + miniLevel * 5) + 1;
                    num2 = random.nextInt(15) + 1;
                    correctAnswer = num1 * num2;
                    questionText.setText(num1 + " × " + num2 + " = ?");
                } else if (diffType == 1) {
                    // Square root: √x = ?
                    int base = random.nextInt(10) + 2; // 2-11
                    correctAnswer = base;
                    num1 = base * base; // Perfect square
                    questionText.setText("√" + num1 + " = ?");
                } else {
                    // Power: x^2 = ? or x^3 = ?
                    num1 = random.nextInt(8) + 2; // 2-9
                    int power = (miniLevel > 5) ? (random.nextBoolean() ? 2 : 3) : 2;
                    correctAnswer = (int) Math.pow(num1, power);
                    questionText.setText(num1 + "^" + power + " = ?");
                }
                break;

            case "premium":
                // Premium: Simple equations like y+4=9, k+5=7, m-7=10, etc.
                String[] variables = {"x", "y", "k", "m", "v", "z", "g", "h"};
                String variable = variables[random.nextInt(variables.length)];

                int premiumType = random.nextInt(4);

                if (premiumType == 0) {
                    // x + a = b  →  x = b - a
                    num2 = random.nextInt(10) + 1; // a
                    correctAnswer = random.nextInt(20) + 1; // x
                    num1 = correctAnswer + num2; // b
                    questionText.setText(variable + " + " + num2 + " = " + num1);
                } else if (premiumType == 1) {
                    // x - a = b  →  x = b + a
                    num2 = random.nextInt(10) + 1; // a
                    correctAnswer = random.nextInt(10) + num2 + 1; // x (must be > a)
                    num1 = correctAnswer - num2; // b
                    questionText.setText(variable + " - " + num2 + " = " + num1);
                } else if (premiumType == 2) {
                    // x * a = b  →  x = b / a
                    num2 = random.nextInt(8) + 2; // a (2-9)
                    correctAnswer = random.nextInt(10) + 1; // x
                    num1 = correctAnswer * num2; // b
                    questionText.setText(variable + " × " + num2 + " = " + num1);
                } else {
                    // x / a = b  →  x = b * a
                    num2 = random.nextInt(8) + 2; // a (2-9)
                    num1 = random.nextInt(10) + 1; // b
                    correctAnswer = num1 * num2; // x
                    questionText.setText(variable + " ÷ " + num2 + " = " + num1);
                }
                break;
        }

        // Reset UI for new question
        answerInput.setText("");
        answerInput.setEnabled(true);
        validateButton.setEnabled(true);
        nextButton.setEnabled(false);
        isAnswered = false;
    }

    private void validateAnswer() {
        if (isAnswered) {
            return;
        }

        String answerText = answerInput.getText().toString().trim();

        if (answerText.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer une réponse", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int userAnswer = Integer.parseInt(answerText);

            if (userAnswer == correctAnswer) {
                Toast.makeText(this, "✅ Félicitations ! C'est correct", Toast.LENGTH_SHORT).show();
                score++;
                scoreText.setText("Score : " + score);
            } else {
                Toast.makeText(this, "❌ Oops! C'est faux. La réponse est : " + correctAnswer, Toast.LENGTH_LONG).show();
            }

            isAnswered = true;
            answerInput.setEnabled(false);
            validateButton.setEnabled(false);
            nextButton.setEnabled(true);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Veuillez entrer un nombre valide", Toast.LENGTH_SHORT).show();
        }
    }

    private void nextQuestion() {
        if (questionCount < totalQuestions) {
            questionCount++;
            questionProgressBar.setProgress(questionCount);
            generateQuestion();

            // Reset timer
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            timeLeft = 30;
            startTimer();
        } else {
            // Level completed
            saveProgress();
            syncProgressToFirebase();
            Toast.makeText(this, "🎉 Niveau terminé! Score: " + score + "/" + totalQuestions, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void saveProgress() {
        SharedPreferences prefs = getSharedPreferences("GameProgress", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Mark this mini-level as completed
        editor.putBoolean(level + "_level_" + miniLevel + "_completed", true);

        // Unlock next level if it exists
        int maxLevels = 0;
        switch (level) {
            case "facile":
                maxLevels = 3;
                break;
            case "moyen":
                maxLevels = 6;
                break;
            case "difficile":
                maxLevels = 9;
                break;
            case "premium":
                maxLevels = 8;
                break;
        }

        if (miniLevel < maxLevels) {
            int nextLevel = miniLevel + 1;
            int currentUnlocked = prefs.getInt(level + "_last_unlocked", 1);
            if (nextLevel > currentUnlocked) {
                editor.putInt(level + "_last_unlocked", nextLevel);
            }
        }

        // Save score for this level
        int previousScore = prefs.getInt(level + "_level_" + miniLevel + "_score", 0);
        if (score > previousScore) {
            editor.putInt(level + "_level_" + miniLevel + "_score", score);
        }

        // Update money and hearts
        int currentMoney = prefs.getInt("money", 0);
        int earnedMoney = score * 10; // 10 coins per correct answer
        editor.putInt("money", currentMoney + earnedMoney);

        // Check if premium should be unlocked
        int totalMoney = prefs.getInt("money", 0);
        if (totalMoney >= 500) {
            editor.putBoolean("premium_unlocked", true);
        }

        editor.apply();

        Log.d(TAG, "Progress saved: level=" + level + ", miniLevel=" + miniLevel + ", score=" + score);
    }

    private void syncProgressToFirebase() {
        if (userId != null && !userId.isEmpty() && mDatabase != null) {
            try {
                SharedPreferences progressPrefs = getSharedPreferences("GameProgress", MODE_PRIVATE);

                HashMap<String, Object> progressUpdates = new HashMap<>();
                progressUpdates.put("money", progressPrefs.getInt("money", 0));
                progressUpdates.put("hearts", progressPrefs.getInt("hearts", 3));
                progressUpdates.put("premium_unlocked", progressPrefs.getBoolean("premium_unlocked", false));
                progressUpdates.put("facile_last_unlocked", progressPrefs.getInt("facile_last_unlocked", 1));
                progressUpdates.put("moyen_last_unlocked", progressPrefs.getInt("moyen_last_unlocked", 1));
                progressUpdates.put("difficile_last_unlocked", progressPrefs.getInt("difficile_last_unlocked", 1));
                progressUpdates.put("premium_last_unlocked", progressPrefs.getInt("premium_last_unlocked", 1));

                mDatabase.child("userProgress").child(userId).updateChildren(progressUpdates);

                // Update user stats
                SharedPreferences userPrefs = getSharedPreferences("CurrentUser", MODE_PRIVATE);
                HashMap<String, Object> userUpdates = new HashMap<>();
                userUpdates.put("score", userPrefs.getInt("score", 0) + score);
                userUpdates.put("argent", progressPrefs.getInt("money", 0));

                mDatabase.child("users").child(userId).updateChildren(userUpdates);

                Log.d(TAG, "Progress synced to Firebase");
            } catch (Exception e) {
                Log.e(TAG, "Failed to sync to Firebase", e);
            }
        }
    }

    private void startTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        timeLeft = 30;
        progressBar.setMax(30);
        progressBar.setProgress(30);

        countDownTimer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft = (int) (millisUntilFinished / 1000);
                timerText.setText("⏱️ Temps : " + timeLeft + "s");
                progressBar.setProgress(timeLeft);
            }

            @Override
            public void onFinish() {
                timerText.setText("⏱️ Temps écoulé !");
                progressBar.setProgress(0);
                if (!isAnswered) {
                    Toast.makeText(GameActivity.this, "⏰ Temps écoulé! La réponse était : " + correctAnswer, Toast.LENGTH_LONG).show();
                    isAnswered = true;
                    answerInput.setEnabled(false);
                    validateButton.setEnabled(false);
                    nextButton.setEnabled(true);
                }
            }
        }.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean soundEnabled = prefs.getBoolean("sound_enabled", true);
        if (soundEnabled) {
            startService(new Intent(this, MusicService.class));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopService(new Intent(this, MusicService.class));
    }

    @Override
    protected void onPause() {
        super.onPause();
        syncProgressToFirebase();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        syncProgressToFirebase();
    }
}