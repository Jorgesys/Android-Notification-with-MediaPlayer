package com.jorgesys.mediasessiongr;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.Rating;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

public class MediaPlayerService extends Service {

    private static final String TAG = "MediaPlayerService";
    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_REWIND = "action_rewind";
    public static final String ACTION_FAST_FORWARD = "action_fast_foward";
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_PREVIOUS = "action_previous";
    public static final String ACTION_STOP = "action_stop";
    private MediaPlayer mMediaPlayer;
    private MediaSessionManager mManager;
    private MediaSession mSession;
    private MediaController mController;
    //You can get audio examples from: https://www.soundhelix.com/audio-examples
    private static final String urlMedia = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3";
    private boolean playerReady;

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind() " + intent.getAction());
        return null;
    }

    private void handleIntent( Intent intent ) {
        Log.i(TAG, "handleIntent() " + intent.getAction());

        if( intent == null || intent.getAction() == null )
            return;

        String action = intent.getAction();

        if( action.equalsIgnoreCase( ACTION_PLAY ) ) {
            mController.getTransportControls().play();
        } else if( action.equalsIgnoreCase( ACTION_PAUSE ) ) {
            mController.getTransportControls().pause();
        } else if( action.equalsIgnoreCase( ACTION_FAST_FORWARD ) ) {
            mController.getTransportControls().fastForward();
        } else if( action.equalsIgnoreCase( ACTION_REWIND ) ) {
            mController.getTransportControls().rewind();
        } else if( action.equalsIgnoreCase( ACTION_PREVIOUS ) ) {
            mController.getTransportControls().skipToPrevious();
        } else if( action.equalsIgnoreCase( ACTION_NEXT ) ) {
            mController.getTransportControls().skipToNext();
        } else if( action.equalsIgnoreCase( ACTION_STOP ) ) {
            mController.getTransportControls().stop();
        }
    }

    private Notification.Action generateAction( int icon, String title, String intentAction ) {
        Log.i(TAG, "generateAction() ");
        Intent intent = new Intent( getApplicationContext(), MediaPlayerService.class );
        intent.setAction( intentAction );
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        return new Notification.Action.Builder( icon, title, pendingIntent ).build();
    }

    private void buildNotification( Notification.Action action ) {
        Log.i(TAG, "buildNotification() ");
        Notification.MediaStyle style = new Notification.MediaStyle();

        Intent intent = new Intent( getApplicationContext(), MediaPlayerService.class );
        intent.setAction( ACTION_STOP );
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);

        int icon = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? R.mipmap.ic_notification : R.mipmap.ic_launcher;

        Notification.Builder builder = new Notification.Builder( this )
                .setSmallIcon(icon/*R.drawable.ic_launcher*/)
                .setContentTitle( "Notification & MediaPlayer" )
                .setContentText( "vibrations that travel through the air or another medium and can be heard when they reach a person's or animal's ear." )
                .setDeleteIntent( pendingIntent )
                .setStyle(style);


        int iColor = R.color.colorPrimary;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && iColor != 0) {
            builder.setColor(ContextCompat.getColor(getApplicationContext(), iColor));
        }

        builder.addAction( generateAction( android.R.drawable.ic_media_previous, "Previous", ACTION_PREVIOUS ) );
        builder.addAction( generateAction( android.R.drawable.ic_media_rew, "Rewind", ACTION_REWIND ) );
        builder.addAction( action );
        builder.addAction( generateAction( android.R.drawable.ic_media_ff, "Fast Foward", ACTION_FAST_FORWARD ) );
        builder.addAction( generateAction( android.R.drawable.ic_media_next, "Next", ACTION_NEXT ) );
        style.setShowActionsInCompactView(0,1,2,3,4);

        NotificationManager notificationManager = (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
        notificationManager.notify( 1, builder.build() );
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand() " + startId);
        if( mManager == null ) {
            Log.i(TAG, "onStartCommand() > initMediaSessions()");
            initMediaSessions();
        }

        handleIntent( intent );
        return super.onStartCommand(intent, flags, startId);
    }

    private void initMediaSessions() {
        Log.i(TAG, "initMediaSessions() ");
        if(mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            /*---MediaPlayer--*/
            try {
                mMediaPlayer.setDataSource(urlMedia);
                mMediaPlayer.prepare();
                mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        playerReady = true;
                    }
                });

                mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        Toast.makeText(getApplicationContext(), "Gracias por escuchar mi musica!.",Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (IOException e) {
                Log.e(TAG, "ERROR: initMediaSessions() " + e.getMessage());
            }
        }

        mSession = new MediaSession(getApplicationContext(), "simple player session");
        mController = new MediaController(getApplicationContext(), mSession.getSessionToken());

        mSession.setCallback(new MediaSession.Callback(){
                                 @Override
                                 public void onPlay() {
                                     super.onPlay();
                                     if(!playerReady)
                                         return;

                                     Log.e( TAG, "onPlay()");
                                     buildNotification( generateAction( android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE ) );

                                     /*---MediaPlayer--*/
                                     mMediaPlayer.start();
                                     /*---MediaPlayer--*/

                                 }

                                 @Override
                                 public void onPause() {
                                     super.onPause();
                                     if(!playerReady)
                                         return;

                                     Log.e( TAG, "onPause()");
                                     buildNotification(generateAction(android.R.drawable.ic_media_play, "Play", ACTION_PLAY));
                                     /*---MediaPlayer--*/
                                     if(mMediaPlayer.isPlaying()){
                                         mMediaPlayer.pause();
                                     }
                                     /*---MediaPlayer--*/

                                 }

                                 @Override
                                 public void onSkipToNext() {
                                     super.onSkipToNext();
                                     Log.e( TAG, "onSkipToNext()");
                                     //Change media here
                                     buildNotification( generateAction( android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE ) );
                                 }

                                 @Override
                                 public void onSkipToPrevious() {
                                     super.onSkipToPrevious();
                                     Log.e( TAG, "onSkipToPrevious()");
                                     //Change media here
                                     buildNotification( generateAction( android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE ) );
                                 }

                                 @Override
                                 public void onFastForward() {
                                     super.onFastForward();
                                     Log.e( TAG, "onFastForward()");
                                     /*---MediaPlayer--*/
                                     int duration = mMediaPlayer.getDuration();
                                     int currentPosition = mMediaPlayer.getCurrentPosition();
                                     int fwdTime = mMediaPlayer.getCurrentPosition() + 25;
                                     Log.i(TAG, "duration: " + duration);
                                     Log.i(TAG, "currentPosition: " + currentPosition + " , fwdTime: " + fwdTime);
                                     if(fwdTime<duration) {
                                         mMediaPlayer.pause();
                                         mMediaPlayer.seekTo(fwdTime);
                                         mMediaPlayer.start();
                                     }
                                     /*---MediaPlayer--*/
                                 }

                                 @Override
                                 public void onRewind() {
                                     super.onRewind();
                                     Log.e( TAG, "onRewind()");
                                     /*---MediaPlayer--*/
                                     int duration = mMediaPlayer.getDuration();
                                     int currentPosition = mMediaPlayer.getCurrentPosition();
                                     int rwdTime = mMediaPlayer.getCurrentPosition() - 25;
                                     Log.i(TAG, "duration: " + duration);
                                     Log.i(TAG, "currentPosition: " + currentPosition + " , rwdTime: " + rwdTime);
                                     if(rwdTime>0) {
                                         mMediaPlayer.pause();
                                         mMediaPlayer.seekTo(rwdTime);
                                         mMediaPlayer.start();
                                     }
                                     /*---MediaPlayer--*/

                                 }

                                 @Override
                                 public void onStop() {
                                     super.onStop();
                                     if(!playerReady)
                                         return;
                                     Log.e( TAG, "onStop()");
                                     NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                                     notificationManager.cancel( 1 );
                                     Intent intent = new Intent( getApplicationContext(), MediaPlayerService.class );
                                     stopService( intent );

                                     /*-----MediaPlayer----*/
                                     if (mMediaPlayer != null) {
                                         mMediaPlayer.release();
                                         mMediaPlayer = null;
                                     }
                                     /*-----MediaPlayer----*/
                                 }

                                 @Override
                                 public void onSeekTo(long pos) {
                                     Log.e( TAG, "onSeekTo() position: " + pos);
                                     super.onSeekTo(pos);
                                 }

                                 @Override
                                 public void onSetRating(Rating rating) {
                                     Log.e( TAG, "onSetRating() " + rating.getPercentRating());
                                     super.onSetRating(rating);
                                 }
                             }
        );
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "onUnbind() " + intent.getAction());
        mSession.release();
        return super.onUnbind(intent);
    }
}