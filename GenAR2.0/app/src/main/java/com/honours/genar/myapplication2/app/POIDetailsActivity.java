package com.honours.genar.myapplication2.app;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.github.ksoichiro.android.observablescrollview.*;
import com.nineoldandroids.view.ViewHelper;

import java.io.InputStream;


public class POIDetailsActivity extends AppCompatActivity implements ObservableScrollViewCallbacks{

    private static final float MAX_TEXT_SCALE_DELTA = 0.3f;

    private View mOverlayView;
    private TextView mTitleView;
    private View mImageView;
    private int mFlexibleSpaceImageHeight;
    private int mActionBarSize;
    private double lattitude;
    private double longitude;

    ///////////////////////////////////////////////////////

    private String TAG = "POIDetailsActivity";
    private String vidLink;
    private String webLink;
    private MediaPlayer player = new MediaPlayer();
    private String audioString;
    private ImageView icPlay, icPause, icStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poi_details);

        mFlexibleSpaceImageHeight = getResources().getDimensionPixelSize(R.dimen.flexible_space_image_height);
        final TypedArray styledAttributes = getApplicationContext().getTheme().obtainStyledAttributes(
                new int[] { android.R.attr.actionBarSize });
        mActionBarSize = (int) styledAttributes.getDimension(0,0);
        styledAttributes.recycle();

        mImageView = findViewById(R.id.image);
        mOverlayView = findViewById(R.id.overlay);
        ObservableScrollView mScrollView = (ObservableScrollView) findViewById(R.id.scroll);
        mScrollView.setScrollViewCallbacks(this);
        mTitleView = (TextView) findViewById(R.id.title);


        onScrollChanged(300, true, false);

        mTitleView.setText(getTitle());
        setTitle("");


        ///////////////////////////////////////////////////////////////////////////////////////////////////////////

        // Get reference to components
        //TextView txtName = (TextView)findViewById(R.id.txtName);
        TextView txtDesc = (TextView)findViewById(R.id.txtDescription);

        icPlay = (ImageView)findViewById(R.id.icPlay);
        icPause = (ImageView)findViewById(R.id.icPause);
        icStop = (ImageView)findViewById(R.id.icStop);

        // Receive intent
        Intent intent = getIntent();

        String name;
        String desc;

        if (intent != null) {
            name = intent.getStringExtra("Name");
            desc = intent.getStringExtra("Desc");
            lattitude = intent.getDoubleExtra("lat", 0.0);
            longitude = intent.getDoubleExtra("lon", 0.0);

            mTitleView.setText(name);
            txtDesc.setText(desc);


            //Image
            String image;
            if (intent.hasExtra("Image")){
                image = (intent.getStringExtra("Image"));
            }
            else
            {
                String DEF_IMAGE = "http://poiinfo.csdev.nmmu.ac.za/Image/NoPhotoDefault.png";
                image = DEF_IMAGE;
            }

            new DownloadImage().execute(image);


            //Audio
            if (intent.hasExtra("Audio")){
                audioString = (intent.getStringExtra("Audio"));
            }
            else
            {
                CardView tileAudio = (CardView)findViewById(R.id.tileAudio);
                tileAudio.setVisibility(View.GONE);
            }

            //Video
            if (intent.hasExtra("Video")){
                vidLink = (intent.getStringExtra("Video"));
            }
            else
            {
                CardView tileVideo = (CardView)findViewById(R.id.cvVideo);
                tileVideo.setVisibility(View.GONE);
            }
            //Website
            if (intent.hasExtra("Web")){
                webLink = (intent.getStringExtra("Web"));
            }
            else
            {
                CardView tileWeb = (CardView)findViewById(R.id.cvWebsite);
                tileWeb.setVisibility(View.GONE);
            }
        }

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                paused = false;
                icPause.setVisibility(View.GONE);
                icStop.setVisibility(View.GONE);
                icPlay.setVisibility(View.VISIBLE);
                icPlay.setPadding(0,0,50,0);
            }
        });


    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        float flexibleRange = mFlexibleSpaceImageHeight - mActionBarSize;
        int minOverlayTransitionY = mActionBarSize - mOverlayView.getHeight();
        ViewHelper.setTranslationY(mOverlayView, ScrollUtils.getFloat(-scrollY, minOverlayTransitionY, 0));
        ViewHelper.setTranslationY(mImageView, ScrollUtils.getFloat(-scrollY / 2, minOverlayTransitionY, 0));

        ViewHelper.setAlpha(mOverlayView, ScrollUtils.getFloat((float) scrollY / flexibleRange, 0, 1));

        float scale = 1 + ScrollUtils.getFloat((flexibleRange - scrollY) / flexibleRange, 0, MAX_TEXT_SCALE_DELTA);
        ViewHelper.setPivotX(mTitleView, 0);
        ViewHelper.setPivotY(mTitleView, 0);
        ViewHelper.setScaleX(mTitleView, scale);
        ViewHelper.setScaleY(mTitleView, scale);

        int maxTitleTranslationY = (int) (mFlexibleSpaceImageHeight - mTitleView.getHeight() * scale);
        int titleTranslationY = maxTitleTranslationY - scrollY;
        ViewHelper.setTranslationY(mTitleView, titleTranslationY);
    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        /*ActionBar ab = getSupportActionBar();
        if (scrollState == ScrollState.UP) {
            if (ab.isShowing()) {
                ab.hide();
            }
        } else if (scrollState == ScrollState.DOWN) {
            if (!ab.isShowing()) {
                ab.show();
            }
        }*/
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    Boolean paused = false;

    public void onClickAudioPlay(View v){

        Log.d(TAG, "Play Clicked");

        if (!paused){
            try {
                player.reset();
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                Log.d(TAG, "ic_audio Stream Set");
                player.setDataSource(audioString);
                Log.d(TAG, "Data Source set to: " + audioString);
                player.prepare();
                Log.d(TAG, "Preparing");
            } catch (Exception e) {
                Log.e(TAG, "ic_audio Error: " + e.getMessage());
                Toast.makeText(getApplicationContext(), "Unable to retrieve audio", Toast.LENGTH_SHORT);
            }
        }

        icPlay.setVisibility(View.GONE);
        icPause.setVisibility(View.VISIBLE);
        icStop.setVisibility(View.VISIBLE);
        Log.d(TAG, "Views Changed");

        player.start();
        paused = false;


    }

    public void onClickAudioPause(View v){
        Log.d(TAG, "Pause Clicked");
        player.pause();
        paused = true;
        icPlay.setVisibility(View.VISIBLE);
        icPlay.setPadding(0, 0, 10, 0);
        icPause.setVisibility(View.GONE);

    }

    public void onClickAudioStop(View v) {
        Log.d(TAG, "Stop Clicked");
        player.stop();
        paused = false;
        icPause.setVisibility(View.GONE);
        icStop.setVisibility(View.GONE);
        icPlay.setVisibility(View.VISIBLE);
        icPlay.setPadding(0, 0, 50, 0);
    }

    public void onClickVideo(View v){

        /*Uri uri = Uri.parse(vidLink);
        Intent vidInt = new Intent(Intent.ACTION_VIEW,uri);
        startActivity(vidInt);*/
        Intent vidIntent = new Intent(this, PlayerActivity.class);
        vidIntent.putExtra("uri",vidLink);
        startActivity(vidIntent);


    }

    public void onClickWebsite(View v){

        Uri uri = Uri.parse(webLink);
        Intent webInt = new Intent(Intent.ACTION_VIEW,uri);
        startActivity(webInt);
    }

    public void onClickDirections(View v){
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?daddr="+lattitude+","+longitude));
        startActivity(intent);

    }

    // DownloadImage AsyncTask
    private class DownloadImage extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... URL) {

            String imageURL = URL[0];

            Bitmap bitmap = null;
            try {
                // Download Image from URL
                InputStream input = new java.net.URL(imageURL).openStream();
                // Decode Bitmap
                bitmap = BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
                ((ImageView)mImageView).setImageResource(R.drawable.loading);
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            // Set the bitmap into ImageView
            ((ImageView)mImageView).setImageBitmap(result);

        }
    }



}
