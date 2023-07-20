# Little Music Player

![Icon](/docs/images/icon.png)

[![Read License](https://img.shields.io/github/license/martinmimigames/little-music-player?style=flat-square)](https://github.com/martinmimigames/little-music-player/blob/main/LICENSE.md)

[![Code Quality](https://img.shields.io/codefactor/grade/github/martinmimigames/little-music-player/main?style=flat-square)](https://www.codefactor.io/repository/github/martinmimigames/little-music-player)

## Descriptions

A mini, simple, yet compatible notification-based audio player.

Open-sourced and publicly-viewable code for anyone worrying about being locked in or privacy
invasion.

Supports Android 1.0+

It allows you to play audio and video files, like how you expected to. Most importantly it is
designed in hope to make it compatible with different Android versions.

Its usage is as easy as opening or sharing the file you want to play with the app, or selecting it
directly from the app. Enjoy your music!

Currently partially support ```m3u```/```m3u8``` playlist files.
More information below in section **Playlist Support**.

Note that this project was originally known as *Simple Music Player*.

Location of webpage:
[https://martinmimigames.github.io/projects/little-music-player](https://martinmimigames.github.io/projects/little-music-player)

You can find most information about *Little Music Player* just below.

## screenshot

![example screenshot](/docs/images/2.jpg)

## Notices

The app will not be able to function properly without notification permissions being granted.
For Android 13+, due to new notification restrictions, the app will request for the notification
permission if the permission is not granted.

## Different variations

- [Tiny Music Player](https://github.com/martinmimigames/tiny-music-player)

  Smallest, most minimal, with the trade-off of only having the most basic features.

## Playlist support

- Folder-based playlist on Android 5.0+
- Temporary playlist via multi-share function
- Partial ```m3u```/```m3u8``` support

```m3u```/```m3u8``` features supported:

- Audio stream url (```http```/```https```)
- Absolute audio path (e.g. ```/storage/emulated/0/Music/Example.mp3```)
- Nested ```m3u```/```m3u8``` files (Absolute path only)
- Custom audio name via ```#EXTINF: [duration], [custom name]```. does not support duration

## Features

- It's free

  Everyone should have the right to listen to music, therefore we aim to make it accessible.

- No advertisement

  We simply want a distraction-free experience for you, so you can relax and enjoy!

- Compact

  Less than half a MB in size, one of the tiniest audio player apps on Android! No need to worry
  about bloated apps again. (Currently less than 30 KB)

- Unbelievably compatible

  Strangely addicted to support, we believe that nobody should be excluded due to an older device. Therefore we have theoretical support starting all the way back from Android 1.0, and tested support on Android 2.3 and above. Isn't it amazing?

- Clean & Simple

  No awkward layout, no hidden options and straightforward, so you can focus on the content. Enjoyment is key, right?

- It simply works :)

  Even in the modern world, there is always something seeming to fail. Luckily for you, this app was designed to work just like it is supposed to. It supports all audio types that your device supports.

- Minimal permissions required

  Have you ever met an app asking for a bunch of unrelated permissions? Have you ever had some
  strange requests reminding you of a malware? **Not this app!** We will explain how we use all our
  permissions, so you know it's safe to use.

- No hidden third-parties

  It is completely open-source, and uses as little third-party libraries as possible.

If you worry about size, compatibility or privacy, this is the best app for you!

We use the foreground service permission to provide audio playing while using other apps.

We use the notification permission for displaying playback control, as we are mainly a notification
based music player.

We use the internet permission to allow for audio streaming, however such function is still being
developed, and can be buggy.

We use the read storage/audio/video permission to open the audio files.
Required for older devices, and ```m3u```/```m3u8``` processing.

(Beta function)

Share a url containing a media file to Little Music Player to stream it.

Or use a m3u file with the stream url inside.

## Development

If you want to contribute by making code changes, you are welcomed!

For starters, check [how to download/run the source code](/docs/contribution.md)

## Branches

- main: latest source-code

## Issues

Issues and pull requests are always welcome!

Since we do not have telemetry in the app, we rely on you to report issues and give feedback.

You can submit issues the following ways:

- via [Github Issues](https://github.com/martinmimigames/little-music-player/issues)
- via email: <martinmimigames@gmail.com>

## Latest Versions

- v2.13
  - Now support folder-based playlist for Android 5.0+
  - Now support temporary playlist via multi-share

- v2.12
  - Fixed permission missing on newer android
  - Now runs on Android 14 (emulator)
  - Added permission blocks at the bottom of activity

- v2.11
  - New loop function for non-streaming audio
  - Playlist support (via m3u) (does not work with relative path yet) (may require storage
    permissions)
  - Skip button for playlist
- v2.10
  - Support internet streaming (new internet permission)
  - Added required permissions for notifications (Android 13+)
  - Better error reporting
  - Fixed audio player null when destroying
  - Fix spelling mistake
  - Updated notifications
  - Added required permissions for audio file opening (older androids)
- v2.9
  - reverted icon
  - fixed incorrect instructions
- v2.8
  - updated description
  - fixed notifications making alert sound (at least on emulators)
  - fixed notifications not working on android 13 (sdk 33)
  - new icons

## Installation

[<img src="https://martinmimigames.github.io/res/get-it-on/f-droid.png"
     alt="Get it on F-Droid"
     height="80">](https://f-droid.org/packages/com.martinmimigames.littlemusicplayer/)

[<img src="https://martinmimigames.github.io/res/get-it-on/github.png"
     alt="Get it on Github"
     height="80">](https://github.com/martinmimigames/little-music-player/releases/latest)

[Get it from the official website](https://martinmimigames.github.io/projects/little-music-player)

## Made by Martinmimigames

Official Website at [https://martinmimigames.github.io](https://martinmimigames.github.io)

### Last update of README

Date : 20-07-2023 dd-mm-yyyy

### Important

**Please read the license!**
