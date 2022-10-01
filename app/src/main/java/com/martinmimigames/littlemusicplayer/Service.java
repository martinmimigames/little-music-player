package com.martinmimigames.littlemusicplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.widget.RemoteViews;

import java.io.File;

import mg.utils.notify.NotificationHelper;

/**
 * service for playing music
 */
public class Service extends android.app.Service {

  /**
   * notification channel id
   */
  private static final String NOTIFICATION_CHANNEL = "martinmimigames.simpleMusicPlayer notification channel";

  /**
   * notification id
   */
  public final int NOTIFICATION = 1;

  /**
   * notification for playback control
   */
  Notification notification;

  /**
   * audio playing logic class
   */
  private AudioPlayer audioPlayer;

  /**
   * unused
   */
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  /**
   * setup
   */
  @Override
  public void onCreate() {
    /* create a notification channel */
    final CharSequence name = "playback control";
    final String description = "Allows for control over audio playback.";
    final int importance = (Build.VERSION.SDK_INT > 24) ? NotificationManager.IMPORTANCE_DEFAULT : 0;
    NotificationHelper.setupNotificationChannel(this, NOTIFICATION_CHANNEL, name, description, importance);

    super.onCreate();
  }

  /**
   * older api support - hopefully
   */
  @Override
  public void onStart(final Intent intent, final int startId) {
    switch (intent.getIntExtra(ACTION.TYPE, ACTION.NULL)) {

      /* start or pause audio playback */
      case ACTION.START_PAUSE:
        audioPlayer.startPause();
        return;

      /* cancel audio playback and kill service */
      case ACTION.KILL:
        stopSelf();
        return;

      /* setup new audio for playback */
      case ACTION.SET_AUDIO:

        /* get audio location */
        final Uri audioLocation = intent.getParcelableExtra(ServiceControl.AUDIO_LOCATION);

        /* create notification for playback control */
        notification = createNotification(audioLocation);

        /* start service as foreground */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR)
          startForeground(NOTIFICATION, notification);

        /* get audio playback logic and start async */
        audioPlayer = new AudioPlayer(this, audioLocation);
        audioPlayer.start();
        return;
    }
  }

  /**
   * startup logic
   */
  @Override
  public int onStartCommand(final Intent intent, final int flags, final int startId) {
    onStart(intent, startId);
    return START_STICKY;
  }

  /**
   * service killing logic
   */
  @Override
  public void onDestroy() {

    /* remove notification from stack */
    NotificationHelper.unsend(this, NOTIFICATION);
    /* interrupt audio playback logic */
    if (!audioPlayer.isInterrupted()) audioPlayer.interrupt();

    super.onDestroy();
  }

  /**
   * create and start playback control notification
   */
  private Notification createNotification(Uri uri) {

    /* setup notification variable */
    final String title = "now playing : " + new File(uri.getPath()).getName();
    notification = NotificationHelper.createNotification(this, NOTIFICATION_CHANNEL, (Build.VERSION.SDK_INT > 21) ? Notification.CATEGORY_SERVICE : null);
    notification.icon = R.drawable.ic_launcher; // icon display
    //notification.largeIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_launcher); // large icon display
    notification.defaults = Notification.DEFAULT_ALL; // set defaults
    notification.when = System.currentTimeMillis(); // set time of notification
    notification.tickerText = title;// set popup text
    notification.flags = Notification.FLAG_AUTO_CANCEL; // automatically close popup
    notification.audioStreamType = Notification.STREAM_DEFAULT;
    notification.sound = null;

    /* flags for control logics on notification */
    final int pendingIntentFlag = PendingIntent.FLAG_UPDATE_CURRENT | ((Build.VERSION.SDK_INT > 23) ? PendingIntent.FLAG_IMMUTABLE : 0);
    /* calls for control logic by starting activity with flags */
    final PendingIntent killIntent =
        PendingIntent
            .getActivity(
                this,
                1,
                new Intent(
                    this,
                    ServiceControl.class
                ).addFlags(
                    Intent.FLAG_ACTIVITY_NO_HISTORY |
                        (
                            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) ?
                                Intent.FLAG_ACTIVITY_NO_ANIMATION : 0)
                ).putExtra(
                    ACTION.SELF_IDENTIFIER,
                    ACTION.SELF_IDENTIFIER_ID
                ).putExtra(
                    ACTION.TYPE,
                    ACTION.KILL
                ),
                pendingIntentFlag
            );

    /* extra variables for notification setup */
    /* different depending on sdk version as they require different logic */
    if (Build.VERSION.SDK_INT >= 19) {

      notification.extras.putCharSequence(Notification.EXTRA_TITLE, title);
      notification.extras.putCharSequence(Notification.EXTRA_TEXT, "Tap to stop");

      notification.contentIntent = PendingIntent.getActivity(this, 2, new Intent(this, ServiceControl.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_NO_HISTORY).putExtra(ACTION.TYPE, ACTION.START_PAUSE).putExtra(ACTION.SELF_IDENTIFIER, ACTION.SELF_IDENTIFIER_ID), pendingIntentFlag);

      notification.actions = new Notification.Action[]{new Notification.Action(R.drawable.ic_launcher, "close", killIntent)};
    } else {
      notification.contentView = new RemoteViews("com.martinmimigames.littlemusicplayer", R.layout.notif);
      NotificationHelper.setText(notification, R.id.notiftitle, title);
      notification.contentIntent = killIntent;
    }

    /* set to not notify again when update */
    notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
    updateNotification();

    return notification;
  }

  /**
   * update notification content and place on stack
   */
  void updateNotification() {
    NotificationHelper.send(this, NOTIFICATION, notification);
  }
}
