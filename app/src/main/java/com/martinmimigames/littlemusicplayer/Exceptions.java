package com.martinmimigames.littlemusicplayer;

import android.content.Context;

import mg.utils.notify.ToastHelper;

final class Exceptions {
  static final String IllegalArgument = "Requires cookies, which the app does not support.";
  static final String IllegalState = "Unusable player state, close app and try again.";
  static final String IO = "Read error, try again later.";
  static final String Security = "File location protected, cannot be accessed.";
  static final String FormatNotSupported = "Media Player Error, maybe format not support";
  static final String UsingHttp = "Using http for streaming, may be insecure.";

  /**
   * create and display error toast to report errors
   */
  static void throwError(Context context, String msg) {
    ToastHelper.showLong(context, msg);
  }
}
