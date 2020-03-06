package com.reem_codes.camera;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class MainActivity extends AppCompatActivity {
    MediaPlayer mediaPlayer;
    public static final String TAG = MainActivity.class.getSimpleName();
    private Camera mCamera;
    private int PERMISSION_CALLBACK_CONSTANT = 1000;
    ImageView myImageView;
    int DELAYED_TIME = 2000;
    boolean IS_NEGATIVE = false;
    Camera.Parameters parameters;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startQRScanner();
    }

    private void sucessQr() {

        setContentView(R.layout.activity_main);
        checkAndGivePermission();
        // animation
        myImageView = (ImageView) findViewById(R.id.img);
        myImageView.setBackgroundResource(R.drawable.spin_animation);
        AnimationDrawable frameAnimation = (AnimationDrawable) myImageView.getBackground();
        frameAnimation.start();

        // play sound
        mediaPlayer = MediaPlayer.create(this, R.raw.sound);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        // negative
        parameters = mCamera.getParameters();
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try{
                    if(IS_NEGATIVE) {
                        parameters.setColorEffect(Camera.Parameters.EFFECT_NONE);

                    } else {
                        parameters.setColorEffect(Camera.Parameters.EFFECT_NEGATIVE);
                    }
                    mCamera.setParameters(parameters);
                    IS_NEGATIVE = !IS_NEGATIVE;

                }
                catch (Exception e){
                    System.out.println("GPDEBUG "+ e.getMessage());
                }
                handler.postDelayed(this, DELAYED_TIME);
            }
        };
        handler.post(runnable);



    }

    private void checkAndGivePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_CALLBACK_CONSTANT);
        } else {
            initialize();
        }
    }

    private void initialize() {
        mCamera = getCameraInstance();
        CameraPreview mPreview = new CameraPreview(this, mCamera);
        FrameLayout rlCameraPreview = (FrameLayout) findViewById(R.id.rlCameraPreview);
        if (rlCameraPreview != null) {
            rlCameraPreview.addView(mPreview);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_CALLBACK_CONSTANT){
            boolean allgranted = false;
            for(int i=0;i<grantResults.length;i++){
                if(grantResults[i] == PackageManager.PERMISSION_GRANTED){
                    allgranted = true;
                } else {
                    allgranted = false;
                    break;
                }
            }


            if(allgranted){
                initialize();
            } else if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.CAMERA)){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA},PERMISSION_CALLBACK_CONSTANT);
                }
            } else if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },PERMISSION_CALLBACK_CONSTANT);
                }
            } else {
                Toast.makeText(MainActivity.this,"Permission is mandatory, Try giving it from App Settings",Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return c;
    }



    @Override
    protected void onPause() {
        super.onPause();
        if(mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }


    private void startQRScanner() {
        new IntentIntegrator(this).initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        IntentResult result =   IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.getContents() != null) {
                updateText(result.getContents());
            } else{
                finish();
                moveTaskToBack(true);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void updateText(String scanCode) {
        if(scanCode.toLowerCase().equals("amal")) {
            sucessQr();

        } else{
            finish();
            moveTaskToBack(true);
        }
    }

}
