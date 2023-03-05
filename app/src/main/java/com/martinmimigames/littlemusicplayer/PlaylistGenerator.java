package com.martinmimigames.littlemusicplayer;

import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;

class PlaylistGenerator {

  private final Context context;

  PlaylistGenerator(Context context) {
    this.context = context;
  }

  String getExtension(Uri location) {
    String scheme = location.getScheme();
    if ("content".equals(scheme)) {
      return context.getContentResolver().getType(location);
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

  AudioEntry[] getPlaylist(String title, Uri audioLocation) {
    var allowLoop = true;
    String scheme = audioLocation.getScheme();
    // if statement to handle null
    if ("http".equals(scheme) || "https".equals(scheme)) {
      audioLocation = getStreamUri(audioLocation);
      allowLoop = false;
      if (audioLocation.toString().startsWith("http://"))
        Exceptions.throwError(context, Exceptions.UsingHttp);
    } else {
      var extension = getExtension(audioLocation);
      if ("audio/x-mpegurl".equals(extension)) {
        var parser = new M3UParser(context);
        try {
          var audioEntry = parser.parse(audioLocation)[0];
          return getPlaylist(audioEntry.name, audioEntry.path);
        } catch (FileNotFoundException e) {
          Exceptions.throwError(context, "File not found!\nLocation: " + audioLocation);
        }
      }
    }
    var entry = new AudioEntry();
    entry.name = title;
    entry.path = audioLocation;
    entry.canLoop = allowLoop;
    return new AudioEntry[]{
      entry
    };
  }
}
