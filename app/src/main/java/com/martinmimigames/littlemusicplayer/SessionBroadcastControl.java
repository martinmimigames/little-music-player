package com.martinmimigames.littlemusicplayer;


import static android.content.Intent.EXTRA_KEY_EVENT;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Build;
import android.view.KeyEvent;

public class SessionBroadcastControl extends BroadcastReceiver {

  private Service service;
  private MediaSession mediaSession;
  private PlaybackState.Builder playbackStateBuilder;

  public SessionBroadcastControl() {
    super();
  }

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
      playbackStateBuilder = new PlaybackState.Builder();
      playbackStateBuilder.setActions(PlaybackState.ACTION_PLAY | PlaybackState.ACTION_PAUSE | PlaybackState.ACTION_PLAY_PAUSE);
      mediaSession.setPlaybackState(playbackStateBuilder.build());
      mediaSession.setActive(true);
    }
  }

  void play() {
    if (Build.VERSION.SDK_INT >= 21) {
      playbackStateBuilder.setState(PlaybackState.STATE_PLAYING, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 1);
      mediaSession.setPlaybackState(playbackStateBuilder.build());
    }
  }

  void pause() {
    if (Build.VERSION.SDK_INT >= 21) {
      playbackStateBuilder.setState(PlaybackState.STATE_PAUSED, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 0);
      mediaSession.setPlaybackState(playbackStateBuilder.build());
    }
  }

  void destroy() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      mediaSession.setActive(false);
      mediaSession.release();
    }
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    KeyEvent event = intent.getParcelableExtra(EXTRA_KEY_EVENT);
    if (event.getAction() == KeyEvent.ACTION_DOWN) {
      intent = new Intent(context, Service.class);
      intent.putExtra(ACTION.SELF_IDENTIFIER, ACTION.SELF_IDENTIFIER_ID);
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
      switch (event.getKeyCode()) {
        case KeyEvent.KEYCODE_MEDIA_PLAY:
          intent.putExtra(ACTION.TYPE, ACTION.PLAY);
          break;
        case KeyEvent.KEYCODE_MEDIA_PAUSE:
          intent.putExtra(ACTION.TYPE, ACTION.PAUSE);
          break;
        case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
          intent.putExtra(ACTION.TYPE, ACTION.PLAY_PAUSE);
          break;
        case KeyEvent.KEYCODE_MEDIA_STOP:
          intent.putExtra(ACTION.TYPE, ACTION.KILL);
          break;
        default:
          return;
      }
      context.startService(intent);
    }
  }
}