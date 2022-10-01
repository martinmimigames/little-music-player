package com.martinmimigames.littlemusicplayer;


import static android.content.Intent.ACTION_MEDIA_BUTTON;
import static android.content.Intent.EXTRA_KEY_EVENT;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;

public class SessionBroadcastControl extends BroadcastReceiver {

  private final Service service;
  private MediaSession mediaSession;

  public SessionBroadcastControl(Service service) {
    this.service = service;
  }

  void create() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      mediaSession = new MediaSession(service, SessionBroadcastControl.class.toString());
      mediaSession.setCallback(new MediaSession.Callback() {
        @Override
        public boolean onMediaButtonEvent(Intent mediaButtonIntent) {
          onReceive(service, mediaButtonIntent);
          return super.onMediaButtonEvent(mediaButtonIntent);
        }
      });
      mediaSession.setActive(true);
    } else {
      service.registerReceiver(this, new IntentFilter(ACTION_MEDIA_BUTTON));
    }
  }

  void play() {
    if (Build.VERSION.SDK_INT >= 21) {
      PlaybackState.Builder builder = new PlaybackState.Builder();
      builder.setActions(PlaybackState.ACTION_PLAY);
      mediaSession.setPlaybackState(builder.build());
    }
  }

  void pause() {
    if (Build.VERSION.SDK_INT >= 21) {
      PlaybackState.Builder builder = new PlaybackState.Builder();
      builder.setActions(PlaybackState.ACTION_PAUSE);
      mediaSession.setPlaybackState(builder.build());
    }
  }

  void destroy() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      mediaSession.setActive(false);
      mediaSession.release();
    } else {
      service.unregisterReceiver(this);
    }
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    KeyEvent event = intent.getParcelableExtra(EXTRA_KEY_EVENT);
    switch (event.getKeyCode()) {
      case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
        service.playPause();
        break;
      case KeyEvent.KEYCODE_MEDIA_PLAY:
        service.play();
        break;
      case KeyEvent.KEYCODE_MEDIA_PAUSE:
        service.pause();
        break;
      case KeyEvent.KEYCODE_MEDIA_STOP:
        service.stopSelf();
        break;
    }
  }
}
