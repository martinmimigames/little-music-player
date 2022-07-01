package com.martinmimigames.littlemusicplayer;

import android.app.Notification;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import java.io.IOException;

import mg.utils.notify.NotificationHelper;
import mg.utils.notify.ToastHelper;

public class AudioPlayer extends Thread implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

  private static final class Exceptions {
    static final String IllegalArgument = "Requires cookies, which the app does not support.";
    static final String IllegalState = "Unusable player state, close app and try again.";
    static final String IO = "Read error, try again later.";
    static final String Security = "File location protected, cannot be accessed.";
  }

  private final Service service;
  private final MediaPlayer mediaPlayer;

  public AudioPlayer(Service service, Uri audioLocation) {
    this.service = service;
    mediaPlayer = new MediaPlayer();

    try {
      mediaPlayer.setDataSource(service, audioLocation);
    } catch (IllegalArgumentException e) {
      throwError(e, Exceptions.IllegalArgument);
      return;
    } catch (SecurityException e) {
      throwError(e, Exceptions.Security);
      return;
    } catch (IllegalStateException e) {
      throwError(e, Exceptions.IllegalState);
      return;
    } catch (IOException e) {
      throwError(e, Exceptions.IO);
      return;
    }

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

    mediaPlayer.setLooping(false);
    mediaPlayer.setOnPreparedListener(this);
    mediaPlayer.setOnCompletionListener(this);
  }

  @Override
  public void run() {
    try {
      mediaPlayer.prepareAsync();
    } catch (IllegalStateException e) {
      throwError(e, Exceptions.IllegalState);
    }
  }

  public void startPause() {
    try {
      if (mediaPlayer.isPlaying()) {
        mediaPlayer.pause();
        if (Build.VERSION.SDK_INT >= 19)
          NotificationHelper.setText(service.notification, "Tap to start");
      } else {
        mediaPlayer.start();
        if (Build.VERSION.SDK_INT >= 19)
          NotificationHelper.setText(service.notification, "Tap to stop");
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

  private void throwError(Exception e, String msg) {
    ToastHelper.showShort(service, msg);
    Log.v("little music player", "an error had occurred:" +
        "\nError ID: " + msg +
        "\nError information: " + e);
  }
}
