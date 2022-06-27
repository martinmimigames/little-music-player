package com.martinmimigames.littlemusicplayer;

import android.app.Notification;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

public class AudioPlayer extends Thread implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

  static Uri audio_location;
  private final Service service;
  private final MediaPlayer mediaPlayer;

  public AudioPlayer(Service service, MediaPlayer mediaPlayer, Uri audio_location) {
    AudioPlayer.audio_location = audio_location;
    this.service = service;
    this.mediaPlayer = mediaPlayer;
  }

  @Override
  public void run() {
    mediaPlayer.setLooping(false);
    mediaPlayer.setOnPreparedListener(this);
    mediaPlayer.setOnCompletionListener(this);
    if (Build.VERSION.SDK_INT < 21) {
      mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    } else {
      mediaPlayer.setAudioAttributes(
          new AudioAttributes.Builder()
              .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
              .setUsage(AudioAttributes.USAGE_MEDIA)
              .build()
      );
    }
    try {
      mediaPlayer.setDataSource(service, audio_location);
    } catch (IllegalArgumentException e) {
      throwError(e, 1);
      return;
    } catch (SecurityException e) {
      throwError(e, 2);
      return;
    } catch (IllegalStateException e) {
      throwError(e, 3);
      return;
    } catch (IOException e) {
      throwError(e, 4);
      return;
    }
    try {
      mediaPlayer.prepareAsync();
    } catch (IllegalStateException e) {
      throwError(e, 5);
    }
  }

  public void startPause() {
    try {
      if (mediaPlayer.isPlaying()) {
        mediaPlayer.pause();
        if (Build.VERSION.SDK_INT >= 19)
          service.notification.extras.putCharSequence(Notification.EXTRA_TEXT, "Tap to start");
      } else {
        mediaPlayer.start();
        if (Build.VERSION.SDK_INT >= 19)
          service.notification.extras.putCharSequence(Notification.EXTRA_TEXT, "Tap to stop");
      }
      service.updateNotification();
    } catch (IllegalStateException e) {
      interrupt();
    }
  }

  @Override
  public void onPrepared(MediaPlayer mp) {
    mediaPlayer.start();
  }

  @Override
  public void onCompletion(MediaPlayer mp) {
    interrupt();
  }

  @Override
  public void interrupt() {
    super.interrupt();
    try {
      mediaPlayer.reset();
    } catch (IllegalStateException ignored) {
    }
    mediaPlayer.release();
    service.stopSelf();
  }

  private void throwError(Exception e, int errorId) {
    Toast.makeText(service, "An error had occurred", Toast.LENGTH_LONG).show();
    Log.v("simpleMusicPlayer", "an error had occurred : " +
        "\nError ID : " + errorId +
        "\nError information : " + e);
  }
}
