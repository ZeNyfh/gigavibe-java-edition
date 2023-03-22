# Gigavibe

Discord bot written in java using JDA and lavaplayer for audio functionality. Has various site support, custom (user
defined)
dj users or roles and some other cool features for you to play with once you [invite](http://zenvibe.ddns.net/) or
self-host the bot.

## Requirements

* [JDK 16](https://www.oracle.com/java/technologies/javase/jdk16-archive-downloads.html)

* [yt-dlp on Windows](https://github.com/yt-dlp/yt-dlp/releases)

* [FFmpeg & FFprobe on Windows](https://www.gyan.dev/ffmpeg/builds/ffmpeg-git-essentials.7z)

A guide for the yt-dlp fork of FFmpeg & FFprobe on Linux is under the Installation section

On Windows, keep these files named as "ffprobe.exe", "yt-dlp.exe" and "ffmpeg.exe".
The latest version of ffmpeg and yt-dlp are recommended. Be sure to download the correct version for your correct
system.

## Usage

Make sure to define a token and change any parameters you wish to change (such as the prefix) within the .env file.
If there is no .env file, try running the bot once. It will proceed to create any missing files.

Run the bot using the latest included .jar file in releases or compile the jar yourself from source.

## Installation

FFmpeg for linux. Be sure to have xz-utils installed on your system, or a way to install stuff from tar.xz files.

* [FFMPEG Download for ARM Linux](https://github.com/yt-dlp/FFmpeg-Builds/releases/download/latest/ffmpeg-master-latest-linuxarm64-gpl.tar.xz)

* [FFMPEG Download for x86 | x64 Linux](https://github.com/yt-dlp/FFmpeg-Builds/releases/download/latest/ffmpeg-master-latest-linux64-gpl.tar.xz)

Run `sudo tar -xvf {FILENAME}.tar.xz`. You can then remove the tar.xz as you won't need it anymore.

JDK 16 can be acquired
from [The Oracle Archive](https://www.oracle.com/java/technologies/javase/jdk16-archive-downloads.html) or from other
trusted sources.

Here is how to install the latest version of yt-dlp:

* `sudo wget https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp -O /usr/local/bin/yt-dlp`
* `sudo chmod a+rx /usr/local/bin/yt-dlp`

If you wish to install the requirements, place the files from the download links in the modules folder. If this folder
doesnt exist, make it where the .jar file is but make sure that FFprobe is called "ffprobe", yt-dlp is called "yt-dlp"
and FFmpeg is called "ffmpeg".

## Disclaimers

If you are on linux, be sure to use the correct commands to install the requirements for your distribution. Multiple
distributions are supported, but only ubuntu is guaranteed to have stability.

Be sure that your ffmpeg version is `>=4.3.1`. This is to ensure that videodl and audiodl work correctly. You can get
this from the snap store or elsewhere.
