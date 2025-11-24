package com.example.mathquest;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {
    private ImageView profileImage;
    private EditText etProfileName, etProfileEmail;
    private TextView tvProfileTitle, tvScore, tvCoins, tvHearts;
    private Button btnSaveProfile, btnLogout;

    private SharedPreferences sharedPreferences;
    private SharedPreferences progressPrefs;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private static final String TAG = "ProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase with error handling
        try {
            mAuth = FirebaseAuth.getInstance();
            mDatabase = FirebaseDatabase.getInstance().getReference();
            Log.d(TAG, "Firebase initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Firebase initialization failed: " + e.getMessage());
            // Continue without Firebase - the app will still work with local storage
        }

        initializeViews();
        loadUserData();
        setupClickListeners();
    }

    private void initializeViews() {
        profileImage = findViewById(R.id.profileImage);
        etProfileName = findViewById(R.id.etProfileName);
        etProfileEmail = findViewById(R.id.etProfileEmail);
        tvProfileTitle = findViewById(R.id.tvProfileTitle);
        tvScore = findViewById(R.id.tvScore);
        tvCoins = findViewById(R.id.tvCoins);
        tvHearts = findViewById(R.id.tvHearts);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnLogout = findViewById(R.id.btnLogout);

        sharedPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
        progressPrefs = getSharedPreferences("GameProgress", MODE_PRIVATE);
    }

    private void loadUserData() {
        // Load user data from SharedPreferences
        String name = sharedPreferences.getString("nom", "Joueur");
        String email = sharedPreferences.getString("email", "email@example.com");
        int score = sharedPreferences.getInt("score", 0);
        int coins = progressPrefs.getInt("money", 0);
        int hearts = progressPrefs.getInt("hearts", 3);

        etProfileName.setText(name);
        etProfileEmail.setText(email);
        tvProfileTitle.setText(name);
        tvScore.setText("Score: " + score);
        tvCoins.setText("Pièces: " + coins);
        tvHearts.setText("Cœurs: " + hearts);

        // Load profile image if exists
        SharedPreferences myPrefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String imageUri = myPrefs.getString("profile_image_uri", null);
        if (imageUri != null) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(Uri.parse(imageUri));
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                profileImage.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Error loading profile image", e);
            }
        }

        Log.d(TAG, "User data loaded: " + name);
    }

    private void setupClickListeners() {
        profileImage.setOnClickListener(v -> changeProfileImage());

        btnSaveProfile.setOnClickListener(v -> saveProfileChanges());

        btnLogout.setOnClickListener(v -> showLogoutConfirmation());
    }

    private void changeProfileImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    private void saveProfileChanges() {
        String newName = etProfileName.getText().toString().trim();

        if (newName.isEmpty()) {
            Toast.makeText(this, "Le nom ne peut pas être vide", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update in SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("nom", newName);
        editor.apply();

        // Update in Firebase if available
        String userId = sharedPreferences.getString("id", "");
        if (!userId.isEmpty() && mDatabase != null) {
            mDatabase.child("users").child(userId).child("nom").setValue(newName)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Profile name updated in Firebase");
                        } else {
                            Log.e(TAG, "Failed to update profile name in Firebase");
                        }
                    });
        }

        tvProfileTitle.setText(newName);
        Toast.makeText(this, "Profil mis à jour avec succès", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Profile updated: " + newName);
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Déconnexion")
                .setMessage("Êtes-vous sûr de vouloir vous déconnecter ? Votre progression sera sauvegardée.")
                .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        logoutUser();
                    }
                })
                .setNegativeButton("Non", null)
                .show();
    }

    private void logoutUser() {
        Log.d(TAG, "Starting logout process");

        // Sync all data to Firebase before logout (if Firebase is available)
        if (mDatabase != null && mAuth != null) {
            syncDataToFirebase();
        } else {
            Log.d(TAG, "Skipping Firebase sync - Firebase not available");
        }

        // Sign out from Firebase Authentication (if available)
        if (mAuth != null) {
            mAuth.signOut();
            Log.d(TAG, "Signed out from Firebase Auth");
        }

        // Clear all local data
        clearLocalData();

        Toast.makeText(this, "Déconnexion réussie", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Local data cleared, redirecting to LoginActivity");

        // Redirect to LoginActivity and clear back stack
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void syncDataToFirebase() {
        String userId = sharedPreferences.getString("id", "");
        if (!userId.isEmpty() && mDatabase != null) {
            Log.d(TAG, "Syncing data to Firebase for user: " + userId);

            // Sync user data
            HashMap<String, Object> userUpdates = new HashMap<>();
            userUpdates.put("nom", sharedPreferences.getString("nom", ""));
            userUpdates.put("score", sharedPreferences.getInt("score", 0));
            userUpdates.put("argent", sharedPreferences.getInt("argent", 0));
            userUpdates.put("coeur", sharedPreferences.getInt("coeur", 3));

            mDatabase.child("users").child(userId).updateChildren(userUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User data synced to Firebase");
                        } else {
                            Log.e(TAG, "Failed to sync user data to Firebase: " +
                                    (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                        }
                    });

            // Sync game progress
            HashMap<String, Object> progressUpdates = new HashMap<>();
            progressUpdates.put("facile_last_unlocked", progressPrefs.getInt("facile_last_unlocked", 1));
            progressUpdates.put("moyen_last_unlocked", progressPrefs.getInt("moyen_last_unlocked", 1));
            progressUpdates.put("difficile_last_unlocked", progressPrefs.getInt("difficile_last_unlocked", 1));
            progressUpdates.put("premium_last_unlocked", progressPrefs.getInt("premium_last_unlocked", 1));
            progressUpdates.put("money", progressPrefs.getInt("money", 0));
            progressUpdates.put("hearts", progressPrefs.getInt("hearts", 3));
            progressUpdates.put("premium_unlocked", progressPrefs.getBoolean("premium_unlocked", false));

            mDatabase.child("userProgress").child(userId).updateChildren(progressUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Progress data synced to Firebase");
                        } else {
                            Log.e(TAG, "Failed to sync progress data to Firebase: " +
                                    (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                        }
                    });
        } else {
            Log.d(TAG, "Cannot sync to Firebase - user ID empty or database not available");
        }
    }

    private void clearLocalData() {
        // Clear CurrentUser preferences
        SharedPreferences.Editor userEditor = sharedPreferences.edit();
        userEditor.clear();
        userEditor.apply();

        // Clear GameProgress preferences
        SharedPreferences.Editor progressEditor = progressPrefs.edit();
        progressEditor.clear();
        progressEditor.apply();

        // Clear MyPrefs (profile image, etc.)
        SharedPreferences myPrefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor myPrefsEditor = myPrefs.edit();
        myPrefsEditor.clear();
        myPrefsEditor.apply();

        Log.d(TAG, "All local data cleared");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                profileImage.setImageBitmap(bitmap);

                // Save image URI to SharedPreferences
                SharedPreferences myPrefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = myPrefs.edit();
                editor.putString("profile_image_uri", imageUri.toString());
                editor.apply();

                Log.d(TAG, "Profile image updated");

            } catch (FileNotFoundException e) {
                Log.e(TAG, "Error loading profile image", e);
                Toast.makeText(this, "Erreur lors du chargement de l'image", Toast.LENGTH_SHORT).show();
            }
        }
    }
}