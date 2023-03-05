package com.martinmimigames.littlemusicplayer;

import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;

class PlaylistGenerator {

  private final Context context;

  PlaylistGenerator(Context context) {
    this.context = context;
  }

  /**
   * Get the mime type of the Uri.
   *
   * @param location the Uri to obtain the mime type from
   * @return mime type, or null if invalid/unavailable
   */
  String getExtension(Uri location) {
    String scheme = location.getScheme();
    if ("content".equals(scheme)) {
      return context.getContentResolver().getType(location);
    } else {
      // if "file://" or otherwise, need to handle null
      var file = new File(location.getPath());
      var mimeMap = MimeTypeMap.getSingleton();
      var name = file.getName();
      var lastDotIndex = name.lastIndexOf(".");
      if (lastDotIndex >= 0 && lastDotIndex + 1 < name.length()) {
        name = name.substring(name.lastIndexOf(".") + 1);
      }
      return mimeMap.getMimeTypeFromExtension(name);
    }
  }

  AudioEntry[] getPlaylist(Uri location) {
    return getPlaylist(new File(location.getPath()).getName(), location, new ArrayList<>(1)).toArray(new AudioEntry[0]);
  }

  private ArrayList<AudioEntry> getPlaylist(String title, Uri location, ArrayList<AudioEntry> entries) {
    var allowLoop = true;
    String scheme = location.getScheme();
    // if statement to handle null
    if ("http".equals(scheme) || "https".equals(scheme)) {
      allowLoop = false;
    } else {
      var extension = getExtension(location);
      if ("audio/x-mpegurl".equals(extension)) {
        // special processing if it is a m3u file
        var parser = new M3UParser(context);
        try {
          for (var entry : parser.parse(location)) {
            // recursion to set correct audio type/ m3u processing
            entries = getPlaylist(entry.title, entry.location, entries);
          }
          return entries;
        } catch (FileNotFoundException e) {
          Exceptions.throwError(context, "File not found!\nLocation: " + location);
        }
      }
    }

    var entry = new AudioEntry();
    entry.title = title;
    entry.location = location;
    entry.canLoop = allowLoop;
    entries.add(entry);
    return entries;
  }
}
