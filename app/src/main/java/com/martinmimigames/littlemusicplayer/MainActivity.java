package com.martinmimigames.littlemusicplayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity implements View.OnClickListener {

  private static final int REQUEST_CODE = 3216487;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    findViewById(R.id.file_opener).setOnClickListener(this);
  }

  @Override
  public void onClick(View v) {
    Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
    fileIntent.setType("audio/*"); // intent type to filter application based on your requirement
    startActivityForResult(fileIntent, REQUEST_CODE);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    super.onActivityResult(requestCode, resultCode, intent);
    if (requestCode == REQUEST_CODE) {
      if (resultCode == Activity.RESULT_OK)
        intent.setClass(this, ServiceControl.class);
      intent.setAction(Intent.ACTION_VIEW);
      startActivity(intent);
    }
  }
}