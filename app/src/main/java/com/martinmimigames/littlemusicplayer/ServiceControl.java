package com.martinmimigames.littlemusicplayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class ServiceControl extends Activity {

  public static final String AUDIO_LOCATION = "audio_location";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    onIntent(getIntent());
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    onIntent(intent);
  }

  private void onIntent(Intent intent) {
    if (intent.getIntExtra(ACTION.SELF_IDENTIFIER, ACTION.NULL) == ACTION.SELF_IDENTIFIER_ID) {
      intent.setClass(this, Service.class);
    } else {
      intent = new Intent(this, Service.class)
          .putExtra(ACTION.TYPE, ACTION.SET_AUDIO)
          .putExtra(AUDIO_LOCATION, getIntent());
    }
    startService(intent);
    finish();
  }
}