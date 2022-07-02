package com.martinmimigames.littlemusicplayer;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;

import java.io.IOException;

import mg.utils.notify.NotificationHelper;
import mg.utils.notify.ToastHelper;

public class AudioPlayer extends Thread implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

  private final Service service;
  private final MediaPlayer mediaPlayer;
  public AudioPlayer(Service service, Uri audioLocation) {
    this.service = service;
    mediaPlayer = new MediaPlayer();

    try {
      mediaPlayer.setDataSource(service, audioLocation);
    } catch (IllegalArgumentException e) {
      throwError(Exceptions.IllegalArgument);
      return;
    } catch (SecurityException e) {
      throwError(Exceptions.Security);
      return;
    } catch (IllegalStateException e) {
      throwError(Exceptions.IllegalState);
      return;
    } catch (IOException e) {
      throwError(Exceptions.IO);
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
      throwError(Exceptions.IllegalState);
    }
  }

  /* only invoked on sdk >= 19 */
  public void startPause() {
    try {
      if (mediaPlayer.isPlaying()) {
        mediaPlayer.pause();
        NotificationHelper.setText(service.notification, "Tap to start");
      } else {
        mediaPlayer.start();
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
    mediaPlayer.release();
    service.stopSelf();
  }

  private void throwError(String msg) {
    ToastHelper.showShort(service, msg);
  }

  private static final class Exceptions {
    static final String IllegalArgument = "Requires cookies, which the app does not support.";
    static final String IllegalState = "Unusable player state, close app and try again.";
    static final String IO = "Read error, try again later.";
    static final String Security = "File location protected, cannot be accessed.";
  }
}
