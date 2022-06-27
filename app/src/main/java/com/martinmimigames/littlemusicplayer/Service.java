package com.martinmimigames.littlemusicplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import com.martinmimigames.simpleMusicPlayer.R;
import com.martinmimigames.util.notification.NotificationHelper;

import java.io.File;

public class Service extends android.app.Service {

  private static final String NOTIFICATION_CHANNEL = "martinmimigames.simpleMusicPlayer notification channel";
  public final int NOTIFICATION = 1;
  Notification notification;

  private AudioPlayer audioPlayer;

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onCreate() {

    CharSequence name = "Notification channel";
    String description = "A channel for showing notification";
    int importance = (Build.VERSION.SDK_INT > 24) ? NotificationManager.IMPORTANCE_DEFAULT : 0;
    NotificationHelper.setupNotificationChannel(this, NOTIFICATION_CHANNEL, name, description, importance);

    NotificationHelper.getNotificationManager(this);

    super.onCreate();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    switch (intent.getIntExtra(ACTION.TYPE, ACTION.NULL)) {
      case ACTION.START_PAUSE:
        audioPlayer.startPause();
        break;

      case ACTION.KILL:
        stopSelf();
        break;

      case ACTION.SET_AUDIO:
        Uri audioLocation;

        Intent audioIntent = intent.getParcelableExtra(ServiceControl.AUDIO_LOCATION);
        String action = audioIntent.getAction();
        if (action.contains(Intent.ACTION_VIEW)) {

          audioLocation = audioIntent.getData();

        } else if (action.contains(Intent.ACTION_SEND)) {

          audioLocation = audioIntent.getParcelableExtra(Intent.EXTRA_STREAM);

        } else return START_STICKY;

        notification = createNotification(audioLocation);

        startForeground(NOTIFICATION, notification);
        Toast.makeText(getApplicationContext(), "Starting service...", Toast.LENGTH_SHORT)
            .show();

        audioPlayer = new AudioPlayer(this, new MediaPlayer(), audioLocation);
        audioPlayer.start();
        break;
    }
    return START_STICKY;
  }

  @Override
  public void onDestroy() {

    NotificationHelper.unsend(this, NOTIFICATION);
    if (!audioPlayer.isInterrupted())
      audioPlayer.interrupt();

    Toast.makeText(getApplicationContext(), "Closing service...", Toast.LENGTH_SHORT)
        .show();

    super.onDestroy();
  }

  private Notification createNotification(Uri uri) {
    notification = NotificationHelper
        .createNotification(
            this,
            NOTIFICATION_CHANNEL,
            (Build.VERSION.SDK_INT > 21) ? Notification.CATEGORY_SERVICE : null);
    notification.icon = R.drawable.ic_launcher; // icon display
    //notification.largeIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_launcher); // large icon display
    notification.defaults = Notification.DEFAULT_ALL; // set defaults
    notification.when = System.currentTimeMillis(); // set time of notification
    notification.tickerText = "now playing : " + new File(uri.getPath()).getName(); // set popup text
    notification.flags = Notification.FLAG_AUTO_CANCEL; // automatically close popup
    notification.audioStreamType = Notification.STREAM_DEFAULT;
    notification.sound = null;

    final int pendingIntentFlag =
        PendingIntent.FLAG_UPDATE_CURRENT |
            ((Build.VERSION.SDK_INT > 23) ? PendingIntent.FLAG_IMMUTABLE : 0);

    //the PendingIntent to launch our activity if the user select this notification
    PendingIntent killIntent = PendingIntent
        .getActivity(this,
            1,
            new Intent(this, ServiceControl.class)
                .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION
                    | Intent.FLAG_ACTIVITY_NO_HISTORY)
                .putExtra(ACTION.SELF_IDENTIFIER, ACTION.SELF_IDENTIFIER_ID)
                .putExtra(ACTION.TYPE, ACTION.KILL),
            pendingIntentFlag);

    if (Build.VERSION.SDK_INT >= 19) {

      notification.extras.putCharSequence(Notification.EXTRA_TITLE, "now playing : " + new File(uri.getPath()).getName());
      notification.extras.putCharSequence(Notification.EXTRA_TEXT, "Tap to stop");

      PendingIntent startPauseIntent = PendingIntent
          .getActivity(
              this,
              2,
              new Intent(this, ServiceControl.class)
                  .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION
                      | Intent.FLAG_ACTIVITY_NO_HISTORY)
                  .putExtra(ACTION.TYPE, ACTION.START_PAUSE)
                  .putExtra(ACTION.SELF_IDENTIFIER, ACTION.SELF_IDENTIFIER_ID),
              pendingIntentFlag);

      notification.contentIntent = startPauseIntent;

      notification.actions = new Notification.Action[]{
          new Notification.Action(R.drawable.ic_launcher, "close", killIntent)
      };
    } else {
      notification.contentIntent = killIntent;
    }

    notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE; //set to not notify again when update
    updateNotification();

    return notification;
  }

  void updateNotification() {
    NotificationHelper.send(this, NOTIFICATION, notification);
  }
}
