package com.martinmimigames.littlemusicplayer;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class M3UParser {

  ArrayList<Entry> entries;

  final Context context;

  M3UParser(Context context) {
    this.context = context;
    entries = new ArrayList<>();
  }

  void parse(Uri m3uLocation) throws FileNotFoundException {
    switch (m3uLocation.getScheme()) {
      case "content" ->
        parseScanner(new Scanner(context.getContentResolver().openInputStream(m3uLocation)));
      case "file" -> parseScanner(new Scanner(new File(m3uLocation.toString())));
    }
  }

  private void parseScanner(Scanner input) {
    entries.clear();
    while (input.hasNextLine()) {
      var line = input.nextLine().trim();
      if (line.trim().length() == 0)
        continue;
      var entry = new Entry();
      if (line.startsWith("#EXTINF:")) {
        var infoAndName = line.split(",");
        entry.name = infoAndName[infoAndName.length - 1];
        entry.path = input.nextLine();
        entries.add(entry);
      } else if (!line.startsWith("#")) {
        entry.name = line;
        entry.path = line;
        entries.add(entry);
      }
    }
  }

  Entry[] getEntries() {
    var out = new Entry[entries.size()];
    for (int i = 0; i < out.length; i++) {
      out[i] = entries.get(i);
    }
    return out;
  }

  static class Entry {
    String name;
    String path;
  }
}
