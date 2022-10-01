package com.martinmimigames.littlemusicplayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * activity for controlling the playback by invoking different logics based on incoming intents
 */
public class ServiceControl extends Activity {

  /**
   * key for audio location for intent flags
   */
  public static final String AUDIO_LOCATION = "audio_location";

  /**
   * redirect call to actual logic
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    onIntent(getIntent());
  }

  /**
   * redirect call to actual logic
   */
  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    onIntent(intent);
  }

  /**
   * call different logic based on required actions
   */
  private void onIntent(Intent intent) {
    /* check if called from self */
    if (intent.getIntExtra(ACTION.SELF_IDENTIFIER, ACTION.NULL) == ACTION.SELF_IDENTIFIER_ID) {
      /* redirect intent to service */
      intent.setClass(this, Service.class);
    } else {
      /* pause service to avoid overlapping */
      stopService(new Intent(this, Service.class));

      /* setup correct flags */
      intent = new Intent(this, Service.class)
          .putExtra(ACTION.TYPE, ACTION.SET_AUDIO);

      switch (getIntent().getAction()) {
        case Intent.ACTION_VIEW:
          intent.putExtra(AUDIO_LOCATION, getIntent().getData());
          break;
        case Intent.ACTION_SEND:
          intent.putExtra(AUDIO_LOCATION, (Bundle) getIntent().getParcelableExtra(Intent.EXTRA_STREAM));
          break;
        default:
          return;
      }
    }
    startService(intent);
    /* does not need to keep this activity */
    finish();
  }
}