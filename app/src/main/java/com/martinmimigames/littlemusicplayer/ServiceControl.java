package com.martinmimigames.littlemusicplayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

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
    intent.setClass(this, Service.class);
    stopService(intent);
    startService(intent);
    /* does not need to keep this activity */
    finish();
  }
}