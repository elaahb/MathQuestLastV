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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvGoLogin;
    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth and Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        etName = findViewById(R.id.nom);
        etEmail = findViewById(R.id.email);
        etPassword = findViewById(R.id.password);
        etConfirmPassword = findViewById(R.id.cpassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoLogin = findViewById(R.id.goToLogin);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Création du compte...");
        progressDialog.setCancelable(false);
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> registerUser());
        tvGoLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Le mot de passe doit contenir au moins 6 caractères", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        // Create user with Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                String userId = firebaseUser.getUid();

                                User user = new User(email, name, password, userId);

                                saveUserToDatabase(user);
                            }
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Erreur lors de la création du compte: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void saveUserToDatabase(User user) {
        mDatabase.child("users").child(user.getId()).setValue(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Initialize user progress
                            initializeUserProgress(user.getId());
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Erreur lors de la sauvegarde des données", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void initializeUserProgress(String userId) {
        // Initialize default progress for new user
        HashMap<String, Object> progress = new HashMap<>();
        progress.put("facile_last_unlocked", 1);
        progress.put("moyen_last_unlocked", 1);
        progress.put("difficile_last_unlocked", 1);
        progress.put("premium_last_unlocked", 1);
        progress.put("money", 0);
        progress.put("hearts", 3);
        progress.put("premium_unlocked", false);
        progress.put("score", 0);

        // Save progress under /userProgress/{userId}
        mDatabase.child("userProgress").child(userId).setValue(progress)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            // Save user data to SharedPreferences for auto-login
                            saveUserToSharedPreferences(userId);

                            Toast.makeText(RegisterActivity.this, "Compte créé avec succès!", Toast.LENGTH_SHORT).show();

                            // Navigate to MainActivity
                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Erreur lors de l'initialisation de la progression", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveUserToSharedPreferences(String userId) {
        SharedPreferences sharedPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("id", userId);
        editor.putString("email", etEmail.getText().toString().trim());
        editor.putString("nom", etName.getText().toString().trim());
        editor.putInt("score", 0);
        editor.putInt("coeur", 3);
        editor.putInt("argent", 0);
        editor.putBoolean("isLoggedIn", true);
        editor.apply();
    }
}