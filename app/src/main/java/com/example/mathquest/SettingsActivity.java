package com.example.mathquest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {
    private SharedPreferences prefs;
    private Switch darkModeSwitch, soundSwitch;
    private SeekBar volumeSeekBar;
    private TextView volumeText, ringtoneText;
    private ImageView ringtoneIcon;
    private int currentRingtoneIndex = 0;
    private List<String> ringtoneNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Paramètres");
        }

        prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        darkModeSwitch = findViewById(R.id.darkModeSwitch);
        soundSwitch = findViewById(R.id.soundSwitch);
        volumeSeekBar = findViewById(R.id.volumeSeekBar);
        volumeText = findViewById(R.id.volumeText);
        ringtoneText = findViewById(R.id.ringtoneText);
        ringtoneIcon = findViewById(R.id.ringtoneIcon);

        // Initialize ringtone names
        ringtoneNames = new ArrayList<>();
        ringtoneNames.add("Défaut");
        ringtoneNames.add("Notification 1");
        ringtoneNames.add("Notification 2");
        ringtoneNames.add("Son doux");

        // Load saved settings
        loadSettings();

        // Dark mode switch
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                prefs.edit().putString("theme_mode", "dark").apply();
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                prefs.edit().putString("theme_mode", "light").apply();
            }
        });

        // Sound switch
        soundSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("sound_enabled", isChecked).apply();
            Toast.makeText(this, isChecked ? "Son activé 🔊" : "Son désactivé 🔇", Toast.LENGTH_SHORT).show();
        });

        // Volume seekbar
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                volumeText.setText("Volume: " + progress + "%");
                prefs.edit().putInt("sound_volume", progress).apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Ringtone selection
        ringtoneIcon.setOnClickListener(v -> {
            currentRingtoneIndex = (currentRingtoneIndex + 1) % ringtoneNames.size();
            ringtoneText.setText(ringtoneNames.get(currentRingtoneIndex));
            prefs.edit().putInt("ringtone_index", currentRingtoneIndex).apply();
            Toast.makeText(this, "Sonnerie: " + ringtoneNames.get(currentRingtoneIndex), Toast.LENGTH_SHORT).show();
        });

        ringtoneText.setOnClickListener(v -> ringtoneIcon.performClick());

        // Reset data button
        TextView resetDataButton = findViewById(R.id.resetDataButton);
        resetDataButton.setOnClickListener(v -> {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle("⚠️ Réinitialiser toutes les données");
            builder.setMessage("Êtes-vous sûr de vouloir réinitialiser toutes les données ? Cette action est irréversible.");
            builder.setPositiveButton("Oui, je suis sûr", (dialog, which) -> {
                prefs.edit().clear().apply();
                Toast.makeText(this, "Toutes les données ont été réinitialisées ✅", Toast.LENGTH_SHORT).show();
                finish();
            });
            builder.setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss());
            builder.show();
        });
    }

    private void loadSettings() {
        // Load theme
        String theme = prefs.getString("theme_mode", "light");
        darkModeSwitch.setChecked("dark".equals(theme));
        if ("dark".equals(theme)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        // Load sound settings
        boolean soundEnabled = prefs.getBoolean("sound_enabled", true);
        soundSwitch.setChecked(soundEnabled);

        int volume = prefs.getInt("sound_volume", 50);
        volumeSeekBar.setProgress(volume);
        volumeText.setText("Volume: " + volume + "%");

        currentRingtoneIndex = prefs.getInt("ringtone_index", 0);
        ringtoneText.setText(ringtoneNames.get(currentRingtoneIndex));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

