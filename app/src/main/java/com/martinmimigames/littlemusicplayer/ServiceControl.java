package com.martinmimigames.littlemusicplayer;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

/**
 * activity for controlling the playback by invoking different logics based on incoming intents
 */
public class ServiceControl extends Activity {

  public static final String TYPE = "type";
  public static final byte NULL = 0;
  public static final byte PLAY_PAUSE = 1;
  public static final byte KILL = 2;
  public static final byte PLAY = 3;
  public static final byte PAUSE = 4;
  public static final byte SELF_IDENTIFIER_ID = 45;
  public static final String SELF_IDENTIFIER = Service.class.toString();

  public static final int REQUEST_CODE = 44130840;

  /**
   * redirect call to actual logic
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    if (getIntent().getByteExtra(ServiceControl.SELF_IDENTIFIER, ServiceControl.NULL) != ServiceControl.SELF_IDENTIFIER_ID
      && !Intent.ACTION_VIEW.equals(getIntent().getAction())
      && !Intent.ACTION_SEND.equals(getIntent().getAction())) {

      /* set listener for button */
      findViewById(R.id.file_opener)
        .setOnClickListener(
          v -> {
            /* request a file from the system */
            final Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
            fileIntent.setType("audio/*"); // intent type to filter application based on your requirement
            startActivityForResult(fileIntent, ServiceControl.REQUEST_CODE);
          });
      return;
    }
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
   * restarts service
   */
  private void onIntent(Intent intent) {
    intent.setClass(this, Service.class);
    stopService(intent);
    startService(intent);

    /* does not need to keep this activity */
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
      finishAndRemoveTask();
    else
      finish();
  }

  @Override
  protected void onStart() {
    super.onStart();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
      this.getPackageManager()
        .checkPermission(
          Manifest.permission.POST_NOTIFICATIONS, this.getPackageName())
        != PackageManager.PERMISSION_GRANTED) {
      findViewById(R.id.request).setVisibility(View.VISIBLE);
      findViewById(R.id.open_settings).setVisibility(View.VISIBLE);
      findViewById(R.id.open_settings).setOnClickListener(v -> {
        final Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, this.getPackageName());
        this.startActivity(intent);
      });
    }
  }

  /**
   * call service control on receiving file
   */
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    /* if result unusable, discard */
    if (requestCode != ServiceControl.REQUEST_CODE || resultCode != Activity.RESULT_OK) return;
    /* redirect to service */
    intent.setClass(this, ServiceControl.class);
    intent.setAction(Intent.ACTION_VIEW);
    startActivity(intent);
  }
}