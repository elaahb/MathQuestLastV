package com.example.mathquest;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class MusicService extends Service {
    private MediaPlayer player;
    private final float MAX_VOLUME = 0.3f;
    private final Handler handler = new Handler();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        player = MediaPlayer.create(this, R.raw.musique);
        player.setLooping(true);
        player.setVolume(0f, 0f);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (player != null && !player.isPlaying()) {
            player.start();
            fadeInMusic();

            handler.postDelayed(() -> {
                if (player != null && player.isPlaying()) {
                    fadeOutAndStop();
                }
            }, 30000);
        }
        return START_STICKY;
    }


    private void fadeInMusic() {
        new Thread(() -> {
            float volume = 0f;
            while (volume < MAX_VOLUME) {
                volume += 0.02f;
                player.setVolume(volume, volume);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            player.setVolume(MAX_VOLUME, MAX_VOLUME);
        }).start();
    }

    private void fadeOutAndStop() {
        new Thread(() -> {
            float volume = MAX_VOLUME;
            while (volume > 0f && player != null) {
                volume -= 0.02f;
                player.setVolume(Math.max(volume, 0f), Math.max(volume, 0f));
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (player != null) {
                player.stop();
                player.release();
                player = null;
            }
            stopSelf();
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player != null && player.isPlaying()) {
            fadeOutAndStop();
        } else if (player != null) {
            player.release();
            player = null;
        }
    }
}
