package com.martinmimigames.littlemusicplayer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;

/**
 * service for playing music
 */
public class Service extends android.app.Service implements MediaPlayerStateListener {

  final HWListener hwListener;
  final Notifications notifications;
  /**
   * audio playing logic class
   */
  private AudioPlayer audioPlayer;

  public Service() {
    hwListener = new HWListener(this);
    notifications = new Notifications(this);
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

  String getExtension(Uri location) {
    String scheme = location.getScheme();
    if ("content".equals(scheme)) {
      return getContentResolver().getType(location);
    } else {
      // if "file://" or otherwise, need to handle null
      var file = new File(location.getPath());
      var mimeMap = MimeTypeMap.getSingleton();
      var name = file.getName();
      name = name.substring(name.lastIndexOf("."));
      return mimeMap.getMimeTypeFromExtension(name);
    }
  }

  Uri getStreamUri(Uri audioLocation) {
    if (audioLocation.toString().startsWith("https"))
      return audioLocation;
    var urlLocation = new AtomicReference<>(audioLocation.toString());
    var t = new Thread(() -> {
      try {
        var https = "https://" + urlLocation.get().substring(7);
        var url = new URL(https);
        var connection = (HttpsURLConnection) url.openConnection();
        connection.connect();
        urlLocation.set(https);
        connection.disconnect();
      } catch (SSLHandshakeException ignored) {
      } catch (MalformedURLException ignored) {
      } catch (IOException ignored) {
      }
    });
    t.start();
    try {
      t.join();
    } catch (InterruptedException ignored) {
    }
    return Uri.parse(urlLocation.get());
  }

  void setAudio(Uri location) {
    setAudio(new File(location.getPath()).getName(), location);
  }

  void setAudio(String title, Uri audioLocation) {
    var allowLoop = true;
    String scheme = audioLocation.getScheme();
    // if statement to handle null
    if ("http".equals(scheme) || "https".equals(scheme)) {
      audioLocation = getStreamUri(audioLocation);
      allowLoop = false;
      if (audioLocation.toString().startsWith("http://"))
        Exceptions.throwError(this, Exceptions.UsingHttp);
    } else {
      var extension = getExtension(audioLocation);
      if ("audio/x-mpegurl".equals(extension)) {
        var parser = new M3UParser(this);
        try {
          var audioEntry = parser.parse(audioLocation)[0];
          setAudio(audioEntry.name, Uri.parse(audioEntry.path));
          return;
        } catch (FileNotFoundException e) {
          Exceptions.throwError(this, "File not found!\nLocation: " + audioLocation);
        }
      }
    }
    setAudio(title, audioLocation, allowLoop);
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
