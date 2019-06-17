package com.clickntap.tap;


import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.google.android.gms.common.images.Size;
import com.google.android.gms.vision.CameraSource;

public class TapCameraPreview extends RelativeLayout {
    private Context mContext;
    private SurfaceView mSurfaceView;
    private boolean mStartRequested;
    private boolean mSurfaceAvailable;
    private CameraSource mCameraSource;


    public TapCameraPreview(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public TapCameraPreview(Context context, AttributeSet attrs){
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init(){
        mStartRequested = false;
        mSurfaceAvailable = false;
        mSurfaceView = new SurfaceView(mContext);
        mSurfaceView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                mSurfaceAvailable = true;
                try {
                    startIfReady();
                } catch (SecurityException se) {
                    Log.d(TapActivity.TAG,"Do not have permission to start the camera", se);
                } catch (Exception e) {
                    Log.d(TapActivity.TAG, "Could not start camera source.", e);
                }
            }
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                mSurfaceAvailable = false;
            }
        });
        addView(mSurfaceView);
    }

    public void start(CameraSource cameraSource) throws Exception {
        if (cameraSource == null) {
            stop();
        }

        mCameraSource = cameraSource;

        if (mCameraSource != null) {
            mStartRequested = true;
            startIfReady();
        }
    }


    public void stop() {
        if (mCameraSource != null) {
            mCameraSource.stop();
        }
    }

    public void release() {
        if (mCameraSource != null) {
            mCameraSource.release();
            mCameraSource = null;
        }
    }

    private void startIfReady() throws Exception{
        if (mStartRequested && mSurfaceAvailable) {
            mCameraSource.start(mSurfaceView.getHolder());
            mStartRequested = false;
        }
    }

}
