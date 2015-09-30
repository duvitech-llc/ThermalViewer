package com.d3engineering.thermalviewer;

import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import org.videolan.libvlc.EventHandler;
import org.videolan.libvlc.IVideoPlayer;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaList;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, IVideoPlayer {

    // Display Surface
    private SurfaceView         mSurface;
    private SurfaceHolder       holder;

    // media player
    private LibVLC libvlc;
    private int                 mVideoWidth;
    private int                 mVideoHeight;
    private final static int    VideoSizeChanged = -1;

    private void releasePlayer() {
        EventHandler.getInstance().removeHandler(mHandler);
        if (libvlc == null)
            return;
        libvlc.stop();
        libvlc.detachSurface();
        holder = null;
        libvlc.closeAout();

        mVideoWidth = 0;
        mVideoHeight = 0;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSurface = (SurfaceView) findViewById(R.id.vlc_surface);

        holder = mSurface.getHolder();
        holder.addCallback(this);

        // Create a new media player
        try {
            libvlc = LibVLC.getInstance();
            libvlc.setHardwareAcceleration(LibVLC.HW_ACCELERATION_FULL);
            libvlc.eventVideoPlayerActivityCreated(true);
            libvlc.setSubtitlesEncoding("");
            libvlc.setAout(LibVLC.AOUT_OPENSLES);
            libvlc.setTimeStretching(true);
            libvlc.setChroma("RV32");
            libvlc.setVerboseMode(true);
            LibVLC.restart(this);
            EventHandler.getInstance().addHandler(mHandler);
            holder.setFormat(PixelFormat.RGBX_8888);
            holder.setKeepScreenOn(true);
            MediaList list = libvlc.getMediaList();
            list.clear();
            list.add(new Media(libvlc, LibVLC.PathToURI("rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov")), false);
            libvlc.playIndex(0);
            Toast.makeText(this.getApplicationContext(), "VLC Lib Ready", Toast.LENGTH_LONG).show();
        }catch (Exception ex){
            Toast.makeText(this.getApplicationContext(), "Failed to create VLC Lib", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {

        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(libvlc != null) {
            releasePlayer();
            libvlc.destroy();
            libvlc = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (libvlc != null)
            libvlc.attachSurface(holder.getSurface(), this);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void setSurfaceSize(int width, int height, int visible_width, int visible_height, int sar_num, int sar_den) {

    }

    // events
    private Handler mHandler = new MyHandler(this);

    private static class MyHandler extends Handler {
        private WeakReference<MainActivity> mOwner;
        private String TAG = "Handler";

        public MyHandler(MainActivity owner) {
            mOwner = new WeakReference<MainActivity>(owner);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity player = mOwner.get();

            // Player events
            if (msg.what == VideoSizeChanged) {
                Log.d(TAG, "VideoSizeChanged X: " + msg.arg1 + " Y: " + msg.arg2);
                return;
            }

            // Libvlc events
            Bundle b = msg.getData();
            switch (b.getInt("event")) {
                case EventHandler.MediaPlayerEndReached:
                    player.releasePlayer();
                    break;
                case EventHandler.MediaPlayerPlaying:
                case EventHandler.MediaPlayerPaused:
                case EventHandler.MediaPlayerStopped:
                default:
                    break;
            }
        }
    }
}
