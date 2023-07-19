package com.martinmimigames.littlemusicplayer;

import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

final class PlaylistGenerator {

    private final Context context;
    private final Playlist playlist;
    private final M3UParser m3UParser;

    PlaylistGenerator(Context context, Playlist playlist) {
        this.context = context;
        this.playlist = playlist;
        this.m3UParser = new M3UParser(context, this);
    }

    /**
     * Get the mime type of the Uri.
     *
     * @param location the Uri to obtain the mime type from
     * @return mime type, or null if invalid/unavailable
     */
    private String getExtension(Uri location) {
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

    void generate(Uri location) {
        generate(new File(location.getPath()).getName(), location);
    }

    private void generate(String title, Uri location) {
        var allowLoop = true;
        var scheme = location.getScheme();
        // if statement to handle null
        if ("http".equals(scheme) || "https".equals(scheme)) {
            allowLoop = false;
        } else if ("audio/x-mpegurl".equals(getExtension(location))) {
            // special processing if it is a m3u file
            m3UParser.parse(location);
            return;
        }

        var entry = new Playlist.Entry();
        entry.title = title;
        entry.location = location;
        entry.canLoop = allowLoop;
        playlist.add(entry);
    }

    public static final class M3UParser {

        private final Context context;
        private final PlaylistGenerator generator;

        M3UParser(Context context, PlaylistGenerator generator) {
            this.context = context;
            this.generator = generator;
        }

        void parse(Uri m3uLocation) {
            try {
                if ("content".equals(m3uLocation.getScheme())) {
                    parse(new Scanner(context.getContentResolver().openInputStream(m3uLocation)));
                } else {
                    // assume scheme is file://
                    parse(new Scanner(new File(m3uLocation.toString())));
                }
            } catch (FileNotFoundException e) {
                Exceptions.throwError(context, "File not found!\nLocation: " + m3uLocation);
            }
        }

        private void parse(Scanner input) {
            while (input.hasNextLine()) {
                var line = input.nextLine().trim();
                if (line.length() == 0)
                    continue;
                var entry = new Playlist.Entry();
                if (line.startsWith("#EXTINF:")) {
                    var infoAndName = line.split(",");
                    entry.title = infoAndName[infoAndName.length - 1];
                    entry.location = Uri.parse(input.nextLine());
                    generator.generate(entry.title, entry.location);
                } else if (!line.startsWith("#")) {
                    entry.title = new File(line).getName();
                    entry.location = Uri.parse(line);
                    generator.generate(entry.title, entry.location);
                }
            }
        }
    }
}
