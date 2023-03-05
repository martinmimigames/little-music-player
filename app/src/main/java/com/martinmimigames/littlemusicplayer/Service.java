package com.martinmimigames.littlemusicplayer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;

import java.io.File;
import java.io.IOException;

/**
 * service for playing music
 */
public class Service extends android.app.Service implements MediaPlayerStateListener {

  final HWListener hwListener;
  final Notifications notifications;
  private final PlaylistGenerator playlistGenerator;
  private AudioPlayer audioPlayer;

  public Service() {
    hwListener = new HWListener(this);
    notifications = new Notifications(this);
    playlistGenerator = new PlaylistGenerator(this);
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
    hwListener.create();
    notifications.create();

    super.onCreate();
  }

  /**
   * startup logic
   */
  @Override
  public void onStart(final Intent intent, final int startId) {
    /* check if called from self */
    if (intent.getAction() == null) {
      var isPLaying = audioPlayer.isPlaying();
      var isLooping = audioPlayer.isLooping();
      switch (intent.getByteExtra(Launcher.TYPE, Launcher.NULL)) {
        /* start or pause audio playback */
        case Launcher.PLAY_PAUSE -> setState(!isPLaying, isLooping);
        case Launcher.PLAY -> setState(true, isLooping);
        case Launcher.PAUSE -> setState(false, isLooping);
        case Launcher.LOOP -> setState(isPLaying, !isLooping);
        /* cancel audio playback and kill service */
        case Launcher.KILL -> stopSelf();
      }
    } else {
      switch (intent.getAction()) {
        case Intent.ACTION_VIEW -> setAudio(intent.getData());
        case Intent.ACTION_SEND -> {
          if (intent.getStringExtra(Intent.EXTRA_TEXT) != null) {
            setAudio(Uri.parse(intent.getStringExtra(Intent.EXTRA_TEXT)));
          } else {
            setAudio(intent.getParcelableExtra(Intent.EXTRA_STREAM));
          }
        }
      }
    }
  }

  void setAudio(Uri location) {
    var playlist = playlistGenerator.getPlaylist(new File(location.getPath()).getName(), location);
    for (var item : playlist) {
      setAudio(item.name, item.path, item.canLoop);
    }
  }

  private void setAudio(String title, Uri location, boolean allowLoop) {
    try {
      /* get audio playback logic and start async */
      audioPlayer = new AudioPlayer(this, location);
      audioPlayer.start();

      /* create notification for playback control */
      notifications.getNotification(title, allowLoop);

      /* start service as foreground */
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR)
        startForeground(Notifications.NOTIFICATION_ID, notifications.notification);

    } catch (IllegalArgumentException e) {
      Exceptions.throwError(this, Exceptions.IllegalArgument);
    } catch (SecurityException e) {
      Exceptions.throwError(this, Exceptions.Security);
    } catch (IllegalStateException e) {
      Exceptions.throwError(this, Exceptions.IllegalState);
    } catch (IOException e) {
      Exceptions.throwError(this, Exceptions.IO);
    }
  }

  @Override
  public void setState(boolean playing, boolean looping) {
    audioPlayer.setState(playing, looping);
    hwListener.setState(playing, looping);
    notifications.setState(playing, looping);
  }

  /**
   * forward to startup logic for newer androids
   */
  @SuppressLint("InlinedApi")
  @Override
  public int onStartCommand(final Intent intent, final int flags, final int startId) {
    onStart(intent, startId);
    return START_STICKY;
  }

  @Override
  public void onMediaPlayerDestroy() {
    // calls onDestroy()
    stopSelf();
  }

  /**
   * destroy on playback complete
   */
  void onMediaPlayerComplete() {
    onMediaPlayerDestroy();
  }

  /**
   * service killing logic
   */
  @Override
  public void onDestroy() {
    notifications.onMediaPlayerDestroy();
    hwListener.onMediaPlayerDestroy();
    audioPlayer.onMediaPlayerDestroy();

    super.onDestroy();
  }
}
