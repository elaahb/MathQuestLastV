package com.example.mathquest;

import android.app.Application;
import android.util.Log;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;
import java.util.List;

public class MyApplication extends Application {
    private static final String TAG = "MyApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            // Check if Firebase is already initialized
            List<FirebaseApp> firebaseApps = FirebaseApp.getApps(this);

            if (firebaseApps.isEmpty()) {
                Log.d(TAG, "Firebase not initialized, initializing manually...");

                FirebaseApp app = FirebaseApp.initializeApp(this);

                if (app == null) {
                    Log.e(TAG, "Automatic Firebase initialization failed!");
                    Log.e(TAG, "Please check:");
                    Log.e(TAG, "1. google-services.json is in app/ folder");
                    Log.e(TAG, "2. Package name in google-services.json matches: com.example.mathquest");
                    Log.e(TAG, "3. apply plugin: 'com.google.gms.google-services' is at end of app/build.gradle");

                
                    initializeFirebaseManually();
                } else {
                    Log.d(TAG, "Firebase initialized successfully via auto-init");
                }
            } else {
                Log.d(TAG, "Firebase already initialized: " + firebaseApps.size() + " app(s)");
            }

            try {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                database.setPersistenceEnabled(true);
                Log.d(TAG, "Firebase Database persistence enabled");
            } catch (Exception e) {
                Log.e(TAG, "Could not enable persistence: " + e.getMessage());
            }

        } catch (Exception e) {
            Log.e(TAG, "Firebase initialization error: " + e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private void initializeFirebaseManually() {
        try {
            

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setApplicationId("1:652063062770:android:86dcbb03d54c0730b6ee6b")
                    .setApiKey("AIzaSyDtAbjF51i6ZARvFuwEbThm0tymxU2eXCk")        
                    .setDatabaseUrl("https://mathquest-66637-default-rtdb.firebaseio.com")
                    .setProjectId("mathquest-66637")                       
                    .setStorageBucket("mathquest-66637.firebasestorage.app")     
                    .build();

            FirebaseApp.initializeApp(this, options);
            Log.d(TAG, "Firebase initialized manually with custom options");
        } catch (Exception e) {
            Log.e(TAG, "Manual Firebase initialization also failed: " + e.getMessage());
        }
    }
}
