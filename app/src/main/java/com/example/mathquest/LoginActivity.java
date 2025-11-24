package com.example.mathquest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmailLogin, etPasswordLogin;
    private Button btnLogin;
    private TextView tvGoRegister;
    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth and Database
        try {
            mAuth = FirebaseAuth.getInstance();
            mDatabase = FirebaseDatabase.getInstance().getReference();
        } catch (IllegalStateException e) {
            // If Firebase is not initialized, show error and return
            Toast.makeText(this, "Firebase initialization failed. Please check your configuration.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initializeViews();
        setupClickListeners();

        checkAutoLogin();
    }

    private void initializeViews() {
        etEmailLogin = findViewById(R.id.EmailLogin);
        etPasswordLogin = findViewById(R.id.PasswordLogin);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoRegister = findViewById(R.id.tvGoRegister);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Connexion...");
        progressDialog.setCancelable(false);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> loginUser());
        tvGoRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void checkAutoLogin() {
        SharedPreferences sharedPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (isLoggedIn && mAuth.getCurrentUser() != null) {
            // User is already logged in, go to MainActivity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void loginUser() {
        String email = etEmailLogin.getText().toString().trim();
        String password = etPasswordLogin.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        // Authenticate with Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Login successful, get user data from database
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                String userId = firebaseUser.getUid();
                                loadUserData(userId);
                            }
                        } else {
                            progressDialog.dismiss();
                            String errorMessage = "Échec de la connexion";
                            if (task.getException() != null) {
                                errorMessage += ": " + task.getException().getMessage();
                            }
                            Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void loadUserData(String userId) {
        // Load user data from /users/{userId}
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        // Load user progress
                        loadUserProgress(userId, user);
                    }
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Données utilisateur non trouvées", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, "Erreur de base de données: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserProgress(String userId, User user) {
        // Load user progress from /userProgress/{userId}
        mDatabase.child("userProgress").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressDialog.dismiss();

                if (dataSnapshot.exists()) {
                    // Save user data and progress to SharedPreferences
                    saveUserAndProgressToSharedPreferences(user, dataSnapshot);
                } else {
                    // Initialize progress if not exists
                    initializeNewUserProgress(userId, user);
                }

                Toast.makeText(LoginActivity.this, "Connexion réussie!", Toast.LENGTH_SHORT).show();

                // Navigate to MainActivity
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, "Erreur lors du chargement de la progression: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserAndProgressToSharedPreferences(User user, DataSnapshot progressSnapshot) {
        SharedPreferences sharedPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Save user data
        editor.putString("id", user.getId());
        editor.putString("email", user.getEmail());
        editor.putString("nom", user.getNom());
        editor.putInt("score", user.getScore());
        editor.putInt("coeur", user.getCoeur());
        editor.putInt("argent", user.getArgent());
        editor.putBoolean("isLoggedIn", true);
        editor.apply();

        // Save progress data
        SharedPreferences progressPrefs = getSharedPreferences("GameProgress", MODE_PRIVATE);
        SharedPreferences.Editor progressEditor = progressPrefs.edit();

        // Set default values first
        progressEditor.putInt("facile_last_unlocked", 1);
        progressEditor.putInt("moyen_last_unlocked", 1);
        progressEditor.putInt("difficile_last_unlocked", 1);
        progressEditor.putInt("premium_last_unlocked", 1);
        progressEditor.putInt("money", 0);
        progressEditor.putInt("hearts", 3);
        progressEditor.putBoolean("premium_unlocked", false);

        // Override with Firebase data if available
        if (progressSnapshot.child("facile_last_unlocked").exists()) {
            progressEditor.putInt("facile_last_unlocked", progressSnapshot.child("facile_last_unlocked").getValue(Integer.class));
        }
        if (progressSnapshot.child("moyen_last_unlocked").exists()) {
            progressEditor.putInt("moyen_last_unlocked", progressSnapshot.child("moyen_last_unlocked").getValue(Integer.class));
        }
        if (progressSnapshot.child("difficile_last_unlocked").exists()) {
            progressEditor.putInt("difficile_last_unlocked", progressSnapshot.child("difficile_last_unlocked").getValue(Integer.class));
        }
        if (progressSnapshot.child("premium_last_unlocked").exists()) {
            progressEditor.putInt("premium_last_unlocked", progressSnapshot.child("premium_last_unlocked").getValue(Integer.class));
        }
        if (progressSnapshot.child("money").exists()) {
            progressEditor.putInt("money", progressSnapshot.child("money").getValue(Integer.class));
        }
        if (progressSnapshot.child("hearts").exists()) {
            progressEditor.putInt("hearts", progressSnapshot.child("hearts").getValue(Integer.class));
        }
        if (progressSnapshot.child("premium_unlocked").exists()) {
            progressEditor.putBoolean("premium_unlocked", progressSnapshot.child("premium_unlocked").getValue(Boolean.class));
        }

        progressEditor.apply();
    }

    private void initializeNewUserProgress(String userId, User user) {
        HashMap<String, Object> progress = new HashMap<>();
        progress.put("facile_last_unlocked", 1);
        progress.put("moyen_last_unlocked", 1);
        progress.put("difficile_last_unlocked", 1);
        progress.put("premium_last_unlocked", 1);
        progress.put("money", 0);
        progress.put("hearts", 3);
        progress.put("premium_unlocked", false);

        mDatabase.child("userProgress").child(userId).setValue(progress);

        // Save to SharedPreferences
        SharedPreferences progressPrefs = getSharedPreferences("GameProgress", MODE_PRIVATE);
        SharedPreferences.Editor progressEditor = progressPrefs.edit();
        progressEditor.putInt("facile_last_unlocked", 1);
        progressEditor.putInt("moyen_last_unlocked", 1);
        progressEditor.putInt("difficile_last_unlocked", 1);
        progressEditor.putInt("premium_last_unlocked", 1);
        progressEditor.putInt("money", 0);
        progressEditor.putInt("hearts", 3);
        progressEditor.putBoolean("premium_unlocked", false);
        progressEditor.apply();
    }
}
