package com.martinmimigames.littlemusicplayer;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;

/**
 * service for playing music
 */
public class Service extends android.app.Service {

  /**
   * audio playing logic class
   */
  private AudioPlayer audioPlayer;

  Notifications nm;

  public Service() {
    nm = new Notifications(this);
  }

  /**
   * unused
   */
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  /**
   * setup
   */
  @Override
  public void onCreate() {
    nm.create();

    super.onCreate();
  }

  /**
   * startup logic
   */
  @Override
  public void onStart(final Intent intent, final int startId) {
    switch (intent.getIntExtra(ACTION.TYPE, ACTION.NULL)) {

      /* start or pause audio playback */
      case ACTION.START_PAUSE:
        audioPlayer.startPause();
        return;

      /* cancel audio playback and kill service */
      case ACTION.KILL:
        stopSelf();
        return;

      /* setup new audio for playback */
      case ACTION.SET_AUDIO:

        /* get audio location */
        final Uri audioLocation = intent.getParcelableExtra(ServiceControl.AUDIO_LOCATION);

        /* create notification for playback control */
        nm.getNotification(audioLocation);

        /* start service as foreground */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR)
          startForeground(nm.NOTIFICATION, nm.notification);

        /* get audio playback logic and start async */
        audioPlayer = new AudioPlayer(this, audioLocation);
        audioPlayer.start();
    }
  }

  /**
   * forward to startup logic for newer androids
   */
  @TargetApi(Build.VERSION_CODES.ECLAIR)
  @Override
  public int onStartCommand(final Intent intent, final int flags, final int startId) {
    onStart(intent, startId);
    return START_STICKY;
  }

  /**
   * service killing logic
   */
  @Override
  public void onDestroy() {
    nm.destroy();
    /* interrupt audio playback logic */
    if (!audioPlayer.isInterrupted()) audioPlayer.interrupt();

    super.onDestroy();
  }
}
