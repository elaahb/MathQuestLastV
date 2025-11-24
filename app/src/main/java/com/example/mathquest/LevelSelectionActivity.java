package com.example.mathquest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.gridlayout.widget.GridLayout;  // ← CHANGE THIS LINE (was android.widget.GridLayout)

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

public class LevelSelectionActivity extends AppCompatActivity {
    private String levelType;
    private int totalMiniLevels;
    private GridLayout gridLayout;
    private TextView titleText;
    private TextView subtitleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_selection);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Sélection de Niveau");
        }

        levelType = getIntent().getStringExtra("level");
        if (levelType == null) {
            finish();
            return;
        }

        switch (levelType) {
            case "facile":
                totalMiniLevels = 3;
                break;
            case "moyen":
                totalMiniLevels = 6;
                break;
            case "difficile":
                totalMiniLevels = 9;
                break;
            case "premium":
                totalMiniLevels = 8;
                break;
            default:
                totalMiniLevels = 3;
        }

        titleText = findViewById(R.id.titleText);
        subtitleText = findViewById(R.id.subtitleText);
        gridLayout = findViewById(R.id.levelGrid);

        String levelName = "";
        switch (levelType) {
            case "facile":
                levelName = "Facile";
                break;
            case "moyen":
                levelName = "Moyen";
                break;
            case "difficile":
                levelName = "Difficile";
                break;
            case "premium":
                levelName = "Premium";
                break;
        }
        titleText.setText("Niveau " + levelName);
        subtitleText.setText("Participez à l'aventure et gagnez le trophée");

        setupLevelGrid();
    }

    private void setupLevelGrid() {
        gridLayout.removeAllViews();

        int columns = 3;
        gridLayout.setColumnCount(columns);

        SharedPreferences prefs = getSharedPreferences("GameProgress", MODE_PRIVATE);
        int lastUnlockedLevel = prefs.getInt(levelType + "_last_unlocked", 1);

        for (int i = 1; i <= totalMiniLevels; i++) {
            CardView levelCard = createLevelCard(i, i <= lastUnlockedLevel);
            gridLayout.addView(levelCard);
        }
    }

    private CardView createLevelCard(int levelNumber, boolean isUnlocked) {
        CardView card = new CardView(this);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(8, 8, 8, 8);
        card.setLayoutParams(params);

        card.setRadius(20);
        card.setCardElevation(10);
        card.setUseCompatPadding(true);

        SharedPreferences prefs = getSharedPreferences("GameProgress", MODE_PRIVATE);
        boolean isCompleted = prefs.getBoolean(levelType + "_level_" + levelNumber + "_completed", false);

        // Set card color based on state with pastel colors
        if (isCompleted) {
            card.setCardBackgroundColor(Color.parseColor("#A5D6A7")); // Pastel green for completed
        } else if (isUnlocked) {
            card.setCardBackgroundColor(Color.parseColor("#B3D9FF")); // Pastel blue for unlocked
        } else {
            card.setCardBackgroundColor(Color.parseColor("#E0E0E0")); // Light gray for locked
        }

        TextView levelText = new TextView(this);
        levelText.setText(String.valueOf(levelNumber));
        levelText.setTextSize(28);
        levelText.setTypeface(null, android.graphics.Typeface.BOLD);
        if (isCompleted) {
            levelText.setTextColor(Color.parseColor("#2E7D32"));
        } else if (isUnlocked) {
            levelText.setTextColor(Color.parseColor("#1976D2"));
        } else {
            levelText.setTextColor(Color.parseColor("#757575"));
        }
        levelText.setGravity(Gravity.CENTER);
        levelText.setPadding(40, 40, 40, 40);

        card.addView(levelText);

        if (isUnlocked) {
            card.setOnClickListener(v -> startLevel(levelNumber));
        } else {
            card.setAlpha(0.5f);
            card.setOnClickListener(v -> {
                Toast.makeText(this, "Niveau verrouillé. Complétez les niveaux précédents.", Toast.LENGTH_SHORT).show();
            });
        }

        return card;
    }

    private void startLevel(int miniLevel) {
        Intent intent = new Intent(LevelSelectionActivity.this, GameActivity.class);
        intent.putExtra("level", levelType);
        intent.putExtra("miniLevel", miniLevel);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh grid when returning to show updated progress
        setupLevelGrid();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}