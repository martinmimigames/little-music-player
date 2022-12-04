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
 * activity for giving instructions
 */
public class MainActivity extends Activity {

  private static final int REQUEST_CODE = 3216487;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    /* set listener for button */
    findViewById(R.id.file_opener)
      .setOnClickListener(
        v -> {
          /* request a file from the system */
          final Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
          fileIntent.setType("audio/*"); // intent type to filter application based on your requirement
          startActivityForResult(fileIntent, REQUEST_CODE);
        });
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
      findViewById(R.id.open_settings).setOnClickListener(
        v -> {
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
    if (requestCode != REQUEST_CODE || resultCode != Activity.RESULT_OK) return;
    /* redirect to service */
    intent.setClass(this, ServiceControl.class);
    intent.setAction(Intent.ACTION_VIEW);
    startActivity(intent);
  }
}