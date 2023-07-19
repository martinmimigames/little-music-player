package com.martinmimigames.littlemusicplayer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

/**
 * service for playing music
 */
public class Service extends android.app.Service implements MediaPlayerStateListener {

    final HWListener hwListener;
    final Notifications notifications;
    private AudioPlayer audioPlayer;

    private final Playlist playlist;
    private Iterator<Playlist.Entry> iterator;

    public Service() {
        hwListener = new HWListener(this);
        notifications = new Notifications(this);
        playlist = new Playlist(this);
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
                case Launcher.SKIP -> playNextEntry();
                /* cancel audio playback and kill service */
                case Launcher.KILL -> stopSelf();
            }
        } else {
            playlist.clear();
            switch (intent.getAction()) {
                case Intent.ACTION_VIEW -> playlist.generate(intent.getData());
                case Intent.ACTION_SEND -> playlist.generate(
                        intent.getStringExtra(Intent.EXTRA_TEXT) != null
                                ? Uri.parse(intent.getStringExtra(Intent.EXTRA_TEXT))
                                : intent.getParcelableExtra(Intent.EXTRA_STREAM));
                case Intent.ACTION_SEND_MULTIPLE ->
                        playlist.generate(intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM));
            }
            iterator = playlist.iterator();
            playEntryFromPlaylist();
        }
    }

    /**
     * Get a audio stream url. Returns https if available.
     * Currently does not support self-signed certificate.
     *
     * @param location url of audio
     * @return original url or https url if available
     */
    Uri getStreamUri(Uri location) {
        if (location.toString().startsWith("https"))
            return location;
        var urlLocation = new AtomicReference<>(location.toString());
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

    private void playEntryFromPlaylist() {
        var entry = iterator.next();
        if ("http".equals(entry.location.getScheme())) {
            // assume the web link is an audio file
            entry.location = getStreamUri(entry.location);
            if ("http".equals(entry.location.getScheme()))
                Exceptions.throwError(this, Exceptions.UsingHttp);
        }
        try {

            /* get audio playback logic and start async */
            audioPlayer = new AudioPlayer(this, entry.location);
            audioPlayer.start();

            /* create notification for playback control */
            notifications.getNotification(entry.title, entry.canLoop, iterator.hasNext());

            /* start service as foreground */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                startForeground(Notifications.NOTIFICATION_ID, notifications.notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR)
                startForeground(Notifications.NOTIFICATION_ID, notifications.notification);

        } catch (IllegalArgumentException e) {
            Exceptions.throwError(this, Exceptions.IllegalArgument);
            playOrDestroy();
        } catch (SecurityException e) {
            Exceptions.throwError(this, Exceptions.Security);
            playOrDestroy();
        } catch (IllegalStateException e) {
            Exceptions.throwError(this, Exceptions.IllegalState);
            playOrDestroy();
        } catch (IOException e) {
            Exceptions.throwError(this, Exceptions.IO);
            playOrDestroy();
        }
    }

    public void playOrDestroy() {
        if (!playNextEntry())
            onMediaPlayerDestroy();
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
    public void onMediaPlayerReset() {
        notifications.onMediaPlayerReset();
        hwListener.onMediaPlayerReset();
        if (audioPlayer != null)
            audioPlayer.onMediaPlayerReset();
    }

    @Override
    public void onMediaPlayerDestroy() {
        // calls onDestroy()
        stopSelf();
    }

    boolean playNextEntry() {
        if (iterator.hasNext()) {
            onMediaPlayerReset();
            playEntryFromPlaylist();
            return true;
        }
        return false;
    }

    /**
     * destroy on playback complete
     */
    void onMediaPlayerComplete() {
        if (!playNextEntry())
            onMediaPlayerDestroy();
    }

    /**
     * service killing logic
     */
    @Override
    public void onDestroy() {
        onMediaPlayerReset();
        notifications.onMediaPlayerDestroy();
        hwListener.onMediaPlayerDestroy();
        if (audioPlayer != null)
            audioPlayer.onMediaPlayerDestroy();
        playlist.clear();

        super.onDestroy();
    }
}
