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
public class Service extends android.app.Service {

  final HWListener sbc;
  final Notifications nm;
  /**
   * audio playing logic class
   */
  private AudioPlayer audioPlayer;

  public Service() {
    sbc = new HWListener(this);
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
    sbc.create();
    nm.create();

    super.onCreate();
  }

  /**
   * startup logic
   */
  @Override
  public void onStart(final Intent intent, final int startId) {
    switch (intent.getByteExtra(Launcher.TYPE, Launcher.NULL)) {

      /* start or pause audio playback */
      case Launcher.PLAY_PAUSE -> {
        playPause();
      }
      case Launcher.PLAY -> play();
      case Launcher.PAUSE -> pause();

      /* cancel audio playback and kill service */
      case Launcher.KILL -> stopSelf();
      default -> {
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
  }

  String getExtension(Uri location) {
    switch (location.getScheme()) {
      case "content":
        return getContentResolver().getType(location);
      case "file":
        var file = new File(location.getPath());
        var mimeMap = MimeTypeMap.getSingleton();
        var name = file.getName();
        name = name.substring(name.lastIndexOf("."));
        return mimeMap.getMimeTypeFromExtension(name);
      default:
        return null;
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

  void setAudio(Uri audioLocation) {
    switch (audioLocation.getScheme()) {
      case "http", "https" -> {
        audioLocation = getStreamUri(audioLocation);
        if (audioLocation.toString().startsWith("http://"))
          Exceptions.throwError(this, Exceptions.UsingHttp);
      }
      default -> {
        var extension = getExtension(audioLocation);
        if ("audio/x-mpegurl".equals(extension)) {
          var parser = new M3UParser(this);
          try {
            parser.parse(audioLocation);
            var locations = parser.getEntries();
            setAudio(Uri.parse(locations[0].path));
            return;
          } catch (FileNotFoundException e) {
            Exceptions.throwError(this, "File not found!\nLocation: " + audioLocation);
          }
        }
      }
    }

    /* create notification for playback control */
    nm.getNotification(audioLocation);

    /* start service as foreground */
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR)
      startForeground(nm.NOTIFICATION, nm.notification);

    /* get audio playback logic and start async */
    audioPlayer = new AudioPlayer(this, audioLocation);
    audioPlayer.start();
  }

  /**
   * Switch to play or pause state, depending on current state
   */
  void playPause() {
    if (audioPlayer.isPlaying())
      pause();
    else
      play();
  }

  /**
   * Switch to play state
   */
  void play() {
    audioPlayer.play();
    sbc.play();
    nm.startPlayback();
  }

  /**
   * Switch to pause state
   */
  void pause() {
    audioPlayer.pause();
    sbc.pause();
    nm.pausePlayback();
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

  /**
   * service killing logic
   */
  @Override
  public void onDestroy() {
    nm.destroy();
    sbc.destroy();
    /* interrupt audio playback logic */
    if (audioPlayer != null && !audioPlayer.isInterrupted()) audioPlayer.interrupt();

    super.onDestroy();
  }
}
