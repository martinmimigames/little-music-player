package com.martinmimigames.littlemusicplayer;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.webkit.MimeTypeMap;

import java.io.File;
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

  final SessionBroadcastControl sbc;
  final Notifications nm;
  /**
   * audio playing logic class
   */
  private AudioPlayer audioPlayer;

  public Service() {
    sbc = new SessionBroadcastControl(this);
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
    /* check if called from self */
    if (intent.getByteExtra(ServiceControl.SELF_IDENTIFIER, ServiceControl.NULL) == ServiceControl.SELF_IDENTIFIER_ID) {
      switch (intent.getByteExtra(ServiceControl.TYPE, ServiceControl.NULL)) {

        /* start or pause audio playback */
        case ServiceControl.PLAY_PAUSE:
          playPause();
          return;

        case ServiceControl.PLAY:
          play();
          return;

        case ServiceControl.PAUSE:
          pause();
          return;

        /* cancel audio playback and kill service */
        case ServiceControl.KILL:
          stopSelf();
          return;
      }
    } else {
      switch (intent.getAction()) {
        case Intent.ACTION_VIEW:
          setAudio(intent.getData());
          break;
        case Intent.ACTION_SEND:
          if (intent.getStringExtra(Intent.EXTRA_TEXT) != null) {
            setAudio(Uri.parse(intent.getStringExtra(Intent.EXTRA_TEXT)));
          } else {
            setAudio(intent.getParcelableExtra(Intent.EXTRA_STREAM));
          }
          break;
        default:
          return;
      }
    }
  }

  String getExtension(Uri m3uLocation) {
    switch (m3uLocation.getScheme()) {
      case "content":
        return getContentResolver().getType(m3uLocation);
      case "file":
        var m3u = new File(m3uLocation.getPath());
        var mimeMap = MimeTypeMap.getSingleton();
        var name = m3u.getName();
        name = name.substring(name.lastIndexOf("."));
        return mimeMap.getMimeTypeFromExtension(name);
      default:
        return null;
    }
  }

  boolean isPlayableMimeType(Uri audioLocation) {
    switch (audioLocation.getScheme()) {
      case "http":
      case "https":
        return true;
      default:
        var extension = getExtension(audioLocation);
        if (extension != null) {
          var index = extension.lastIndexOf("/");
          if (index != -1) {
            switch (extension.substring(0, index)) {
              case "audio":
              case "video":
                return true;
            }
          }
        }
    }
    Exceptions.throwError(this, Exceptions.FormatNotSupported);
    return false;
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
      case "http":
      case "https":
        audioLocation = getStreamUri(audioLocation);
        if (audioLocation.toString().startsWith("http://"))
          Exceptions.throwError(this, Exceptions.UsingHttp);
        break;
      default:
        if (!isPlayableMimeType(audioLocation))
          return;
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
    sbc.destroy();
    /* interrupt audio playback logic */
    if (audioPlayer != null && !audioPlayer.isInterrupted()) audioPlayer.interrupt();

    super.onDestroy();
  }
}
