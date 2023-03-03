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

  AudioEntry[] parse(Uri m3uLocation) throws FileNotFoundException {
    switch (m3uLocation.getScheme()) {
      case "content" -> {
        return parseInternal(new Scanner(context.getContentResolver().openInputStream(m3uLocation)));
      }
      case "file" -> {
        return parseInternal(new Scanner(new File(m3uLocation.toString())));
      }
      default -> {
        return new AudioEntry[0];
      }
    }
  }

  private AudioEntry[] parseInternal(Scanner input) {
    var entries = new ArrayList<AudioEntry>();
    while (input.hasNextLine()) {
      var line = input.nextLine().trim();
      if (line.trim().length() == 0)
        continue;
      var entry = new AudioEntry();
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

    var out = new AudioEntry[entries.size()];
    for (int i = 0; i < out.length; i++) {
      out[i] = entries.get(i);
    }
    return out;
  }
}
