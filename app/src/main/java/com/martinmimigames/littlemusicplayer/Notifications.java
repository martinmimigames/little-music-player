package com.martinmimigames.littlemusicplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;

import java.io.File;

import mg.utils.notify.NotificationHelper;

public class Notifications {

  /**
   * notification channel id
   */
  public static final String NOTIFICATION_CHANNEL = "LittleMusicPlayer notifications";
  /**
   * notification id
   */
  public final int NOTIFICATION = 1;
  private final Service service;
  /**
   * notification for playback control
   */
  Notification notification;

  public Notifications(Service service) {
    this.service = service;
  }

  public void create() {
    /* create a notification channel */
    final CharSequence name = "Playback Control";
    final String description = "Allows for control over audio playback.";
    final int importance = (Build.VERSION.SDK_INT > 24) ? NotificationManager.IMPORTANCE_LOW : 0;
    NotificationHelper.setupNotificationChannel(service, NOTIFICATION_CHANNEL, name, description, importance);
  }

  /**
   * Switch to pause state
   */
  void pausePlayback() {
    NotificationHelper.setText(notification, "Tap to start");
    update();
  }

  /**
   * Switch to play state
   */
  void startPlayback() {
    NotificationHelper.setText(notification, "Tap to stop");
    update();
  }

  /**
   * create and start playback control notification
   */
  void getNotification(final Uri uri) {

    /* setup notification variable */
    final String title = "now playing : " + new File(uri.getPath()).getName();
    notification = NotificationHelper.createNotification(service, NOTIFICATION_CHANNEL, (Build.VERSION.SDK_INT > 21) ? android.app.Notification.CATEGORY_SERVICE : null);
    notification.icon = R.drawable.ic_notif; // icon display
    //notification.largeIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_launcher); // large icon display
    notification.defaults = android.app.Notification.DEFAULT_ALL; // set defaults
    notification.when = System.currentTimeMillis(); // set time of notification
    notification.tickerText = title;// set popup text
    notification.flags = android.app.Notification.FLAG_AUTO_CANCEL; // automatically close popup
    notification.audioStreamType = android.app.Notification.STREAM_DEFAULT;
    notification.sound = null;

    /* flags for control logics on notification */
    final int pendingIntentFlag =
      (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) ?
        (PendingIntent.FLAG_UPDATE_CURRENT |
          ((Build.VERSION.SDK_INT > 23) ?
            PendingIntent.FLAG_IMMUTABLE : 0)
        ) : 0;
    /* calls for control logic by starting activity with flags */
    final PendingIntent killIntent =
      PendingIntent
        .getService(
          service,
          1,
          new Intent(
            service,
            Service.class
          ).addFlags(
            Intent.FLAG_ACTIVITY_NO_HISTORY |
              (
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) ?
                  Intent.FLAG_ACTIVITY_NO_ANIMATION : 0)
          ).putExtra(
            ServiceControl.SELF_IDENTIFIER,
            ServiceControl.SELF_IDENTIFIER_ID
          ).putExtra(
            ServiceControl.TYPE,
            ServiceControl.KILL
          ),
          pendingIntentFlag
        );

    /* extra variables for notification setup */
    /* different depending on sdk version as they require different logic */
    if (Build.VERSION.SDK_INT >= 19) {

      notification.extras.putCharSequence(android.app.Notification.EXTRA_TITLE, title);
      notification.extras.putCharSequence(android.app.Notification.EXTRA_TEXT, "Tap to stop");

      notification.contentIntent = PendingIntent.getService(service, 2, new Intent(service, Service.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_NO_HISTORY).putExtra(ServiceControl.TYPE, ServiceControl.PLAY_PAUSE).putExtra(ServiceControl.SELF_IDENTIFIER, ServiceControl.SELF_IDENTIFIER_ID), pendingIntentFlag);

      notification.actions = new android.app.Notification.Action[]{new android.app.Notification.Action(R.drawable.ic_launcher, "close", killIntent)};
    } else {
      notification.contentView = new RemoteViews("com.martinmimigames.littlemusicplayer", R.layout.notif);
      NotificationHelper.setText(notification, R.id.notif_title, title);
      notification.contentIntent = killIntent;
    }

    /* set to not notify again when update */
    notification.flags |= android.app.Notification.FLAG_ONLY_ALERT_ONCE;
    update();

  }

  /**
   * update notification content and place on stack
   */
  private void update() {
    NotificationHelper.send(service, NOTIFICATION, notification);
  }

  void destroy() {
    /* remove notification from stack */
    NotificationHelper.unsend(service, NOTIFICATION);
  }
}
