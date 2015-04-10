package com.lemi.mario.boring.walkman.player;

import android.content.Context;
import android.media.MediaPlayer;


/**
 * walkman implement by media player.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 */
public class WalkmanMediaPlayer {

  private Context context;
  private MediaPlayer player;


  public WalkmanMediaPlayer(Context context) {
    this.context = context;
    player = new MediaPlayer();
    player.setLooping(false);
  }

}
