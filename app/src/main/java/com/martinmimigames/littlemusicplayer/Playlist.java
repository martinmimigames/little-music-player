package com.martinmimigames.littlemusicplayer;

import android.content.Context;
import android.net.Uri;

import java.util.ArrayList;

final class Playlist extends ArrayList<Playlist.Entry> {

    private final PlaylistGenerator generator;

    Playlist(Context context) {
        generator = new PlaylistGenerator(context, this);
    }

    void generate(Uri location) {
        generator.generate(location);
    }

    static final class Entry {
        String title;
        Uri location;
        boolean canLoop;
    }
}
