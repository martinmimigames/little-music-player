package com.martinmimigames.littlemusicplayer;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;

import java.io.IOException;

/**
 * audio playing logic class
 */
class AudioPlayer extends Thread implements MediaPlayer.OnCompletionListener, MediaPlayerStateListener {

  private final Service service;
  private final MediaPlayer mediaPlayer;

  /**
   * Initiate an audio player, throws exceptions if failed.
   *
   * @param service       the service initialising this.
   * @param audioLocation the Uri containing the location of the audio.
   * @throws IllegalArgumentException when the media player need cookies, but we do not supply it.
   * @throws IllegalStateException    when the media player is not in the correct state.
   * @throws SecurityException        when the audio file is protected and cannot be played.
   * @throws IOException              when the audio file cannot be read.
   */
  public AudioPlayer(Service service, Uri audioLocation) throws IllegalArgumentException, IllegalStateException, SecurityException, IOException {
    this.service = service;
    /* initiate new audio player */
    mediaPlayer = new MediaPlayer();

    /* setup player variables */
    mediaPlayer.setDataSource(service, audioLocation);

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
      mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    } else {
      mediaPlayer.setAudioAttributes(
        new AudioAttributes.Builder()
          .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
          .setUsage(AudioAttributes.USAGE_MEDIA)
          .build()
      );
    }

    mediaPlayer.setLooping(false);

    /* setup listeners for further logics */
    mediaPlayer.setOnCompletionListener(this);
  }

  @Override
  public void run() {
    /* get ready for playback */
    try {
      mediaPlayer.prepare();
      service.setState(true, false);
    } catch (IllegalStateException e) {
      Exceptions.throwError(service, Exceptions.IllegalState);
    } catch (IOException e) {
      Exceptions.throwError(service, Exceptions.IO);
    }
  }

  /**
   * check if audio is playing
   */
  public boolean isPlaying() {
    return mediaPlayer.isPlaying();
  }

  /**
   * check if audio is looping, always false on < android cupcake (sdk 3)
   */
  public boolean isLooping() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
      return mediaPlayer.isLooping();
    } else {
      return false;
    }
  }

  @Override
  public void setState(boolean playing, boolean looping) {
    if (playing) {
      mediaPlayer.start();
    } else {
      mediaPlayer.pause();
    }
    mediaPlayer.setLooping(looping);
  }

  /**
   * notifies playback completion
   */
  @Override
  public void onCompletion(MediaPlayer mp) {
    service.onMediaPlayerComplete();
  }

  @Override
  public void onMediaPlayerDestroy() {
    /* interrupt audio playback logic */
    if (!isInterrupted()) {
      interrupt();
    }
  }

  /**
   * release and kill service
   */
  @Override
  public void interrupt() {
    mediaPlayer.release();
    super.interrupt();
  }
}
