package com.martinmimigames.littlemusicplayer;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class M3UParser {

  final Context context;

  M3UParser(Context context) {
    this.context = context;
  }

  ArrayList<AudioEntry> parse(Uri m3uLocation) throws FileNotFoundException {
    String scheme = m3uLocation.getScheme();
    if ("content".equals(scheme)) {
      return parseInternal(new Scanner(context.getContentResolver().openInputStream(m3uLocation)));
    } else {
      // assume scheme is file://
      return parseInternal(new Scanner(new File(m3uLocation.toString())));
    }
  }

  private ArrayList<AudioEntry> parseInternal(Scanner input) {
    var entries = new ArrayList<AudioEntry>();
    while (input.hasNextLine()) {
      var line = input.nextLine().trim();
      if (line.trim().length() == 0)
        continue;
      var entry = new AudioEntry();
      if (line.startsWith("#EXTINF:")) {
        var infoAndName = line.split(",");
        entry.title = infoAndName[infoAndName.length - 1];
        entry.location = Uri.parse(input.nextLine());
        entries.add(entry);
      } else if (!line.startsWith("#")) {
        entry.title = new File(line).getName();
        entry.location = Uri.parse(line);
        entries.add(entry);
      }
    }

    return entries;
  }
}
