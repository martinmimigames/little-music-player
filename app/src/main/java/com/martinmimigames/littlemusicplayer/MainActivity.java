package com.martinmimigames.littlemusicplayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * activity for giving instructions
 */
public class MainActivity extends Activity implements View.OnClickListener {

  private static final int REQUEST_CODE = 3216487;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    /* set listener for button */
    findViewById(R.id.file_opener).setOnClickListener(this);
  }

  /**
   * button logic
   */
  @Override
  public void onClick(View v) {
    /* request a file from the system */
    Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
    fileIntent.setType("audio/*"); // intent type to filter application based on your requirement
    startActivityForResult(fileIntent, REQUEST_CODE);
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