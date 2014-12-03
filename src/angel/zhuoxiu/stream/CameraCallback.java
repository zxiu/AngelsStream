package angel.zhuoxiu.stream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Bitmap.CompressFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback; //import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class CameraCallback implements SurfaceHolder.Callback {
    private Context mContext;
    private Camera mCamera;
    private boolean isShowFrame;
    private SurfaceHolder mHolder;
    // 在2.3的Camera.CameraInfo类中
    // CAMERA_FACING_BACK常量的值为0，CAMERA_FACING_FRONT为1
    private static final int CAMERA_FACING_BACK = 0;
    private static final int CAMERA_FACING_FRONT = 1;

    private final int FLASH_MODE_AUTO = 0;
    private final int FLASH_MODE_ON = 1;
    private final int FLASH_MODE_OFF = 2;

    private Parameters mParameters;

    public CameraCallback(Context context) {
        this.mContext = context;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
        Log.e("tag", " surfaceChanged");
        mParameters = mCamera.getParameters();
        if (isSupportedFlashMode()) {// 需要判断是否支持闪光灯
            mParameters.setFlashMode(Parameters.FLASH_MODE_AUTO);
        }
        mParameters.setPictureFormat(PixelFormat.JPEG);
        Log.e("tag",
                "parameters.getPictureSize()"
                        + mParameters.getPictureSize().width);
        setPictureSize(mParameters);
        // parameters.setPreviewFormat(PixelFormat.JPEG);//
        Log.i("tag", "holder width:" + width + "  height:" + height);
        // parameters.setPreviewSize(width, height);//需要判断支持的预览

        mCamera.setParameters(mParameters);

        mCamera.startPreview();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mHolder = holder;
        Log.e("tag", " surfaceCreated");
        try {
            if (mCamera == null) {
                mCamera = Camera.open();
            }
            setDisplayOrientation(mCamera);
            mCamera.setPreviewDisplay(holder);
            Log.i("", "mCamera 2");

        } catch (IOException e) {
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
            e.printStackTrace();
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.e("tag", " surfaceDestroyed");
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

    }

    public int getNumberOfCameras() {
        try {
            Method method = Camera.class.getMethod("getNumberOfCameras", null);
            if (method != null) {
                Object object = method.invoke(mCamera, null);
                if (object != null) {
                    return (Integer) object;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void setDisplayOrientation(Camera camera) {
        try {
            Method method = Camera.class.getMethod("setDisplayOrientation",
                    int.class);
            if (method != null) {
                method.invoke(camera, 90);
            }
            Log.i("tag", "方法名：" + method.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Camera open(int i) {
        try {
            Method method = Camera.class.getMethod("open", int.class);
            if (method != null) {
                Object object = method.invoke(mCamera, i);
                if (object != null) {
                    return (Camera) object;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 设置图片大小
    private void setPictureSize(Parameters parameters) {
        List<Size> sizes = parameters.getSupportedPictureSizes();
        if (sizes == null) {
            return;
        }
        int maxSize = 0;
        int width = 0;
        int height = 0;
        for (int i = 0; i < sizes.size(); i++) {
            Size size = sizes.get(i);
            int pix = size.width * size.height;
            if (pix > maxSize) {
                maxSize = pix;
                width = size.width;
                height = size.height;
            }
        }
        Log.i("tag", "图片的大小：" + width + " height:" + height);
        parameters.setPictureSize(width, height);
    }

    public Camera getCamera() {
        return mCamera;
    }

    // 自动对焦
//    public void autoFocus(View v, MotionEvent event) {
//        if (isShowFrame) {
//            return;
//        }
//        mCamera.autoFocus(new AutoFocusCallback() {
//
//            @Override
//            public void onAutoFocus(boolean success, Camera camera) {
//            }
//        });
//
//        RelativeLayout layout = (RelativeLayout) v;
//
//        final ImageView imageView = new ImageView(mContext);
//        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(),
//                R.drawable.lcamera_focus_frame1);
//        imageView.setImageBitmap(bitmap);
//        LayoutParams params = new RelativeLayout.LayoutParams(
//                bitmap.getWidth(), bitmap.getHeight());
//        // imageView.setLayoutParams(params);
//        Log.e("tag", "bitmap.getWidth:" + bitmap.getWidth());
//        params.leftMargin = (int) (event.getX() - bitmap.getWidth() / 2);
//        params.topMargin = (int) (event.getY() - bitmap.getHeight() / 2);
//        layout.addView(imageView, params);
//        imageView.setVisibility(View.VISIBLE);
//        ScaleAnimation animation = new ScaleAnimation(1, 0.5f, 1, 0.5f,
//                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
//                0.5f);
//        animation.setDuration(300);
//        animation.setFillAfter(true);
//        animation.setAnimationListener(new AnimationListener() {
//
//            @Override
//            public void onAnimationStart(Animation animation) {
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//            }
//
//            @Override
//            public void onAnimationEnd(final Animation animation) {
//                imageView.setImageResource(R.drawable.lcamera_focus_frame2);
//
//                new Thread() {
//                    public void run() {
//                        try {
//                            Thread.sleep(400);
//                            ((Activity) (mContext))
//                                    .runOnUiThread(new Runnable() {
//
//                                        @Override
//                                        public void run() {
//                                            imageView
//                                                    .setImageResource(R.drawable.lcamera_focus_frame3);
//                                        }
//                                    });
//                            Thread.sleep(200);
//                            ((Activity) (mContext))
//                                    .runOnUiThread(new Runnable() {
//
//                                        @Override
//                                        public void run() {
//                                            imageView.clearAnimation();
//                                            imageView.setVisibility(View.GONE);
//                                            isShowFrame = false;
//                                        }
//                                    });
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//
//                    };
//                }.start();
//            }
//        });
//        imageView.startAnimation(animation);
//        isShowFrame = true;
//    }

    // 拍照
//    public void takePicture(final Handler handler) {
//        mCamera.takePicture(null, null, new PictureCallback() {
//
//            @Override
//            public void onPictureTaken(byte[] data, Camera camera) {
//                FileOutputStream fos = null;
//                try {
//                    File directory;
//                    if (Environment.getExternalStorageState().equals(
//                            Environment.MEDIA_MOUNTED)) {
//                        directory = new File(Environment
//                                .getExternalStorageDirectory(), "camera");
//                    } else {
//                        directory = new File(mContext.getCacheDir(), "camera");
//                    }
//                    if (!directory.exists()) {
//                        directory.mkdir();
//                    }
//                    File file = new File(directory, System.currentTimeMillis()
//                            + ".jpg");
//                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0,
//                            data.length);
//                    fos = new FileOutputStream(file);
//                    boolean compress = bitmap.compress(CompressFormat.JPEG,
//                            100, fos);
//                    if (compress) {
//                        handler.sendEmptyMessage(MainActivity.MESSAGE_SVAE_SUCCESS);
//                    } else {
//                        handler.sendEmptyMessage(MainActivity.MESSAGE_SVAE_FAILURE);
//                    }
//                    mCamera.startPreview();
//                    Log.i("tag", " 保存是否成功:" + compress + "  file.exists:"
//                            + file.exists());
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                } finally {
//                    if (fos != null) {
//                        try {
//                            fos.close();
//                        } catch (IOException e) {
//                        }
//                    }
//                }
//
//            }
//        });
//
//    }

    // 多镜头切换
    public void switchCamera(SurfaceView surfaceView, boolean isFrontCamera) {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

        int cameraId = isFrontCamera ? CAMERA_FACING_FRONT : CAMERA_FACING_BACK;// CAMERA_FACING_FRONT为前置摄像头

        mCamera = open(cameraId);
        Parameters parameters = mCamera.getParameters();
        Log.e("tag",
                "parameters.getPictureSize()"
                        + parameters.getPictureSize().width);
        setPictureSize(parameters);

        parameters.setPictureFormat(PixelFormat.JPEG);
        mCamera.setParameters(parameters);
        Log.e("tag",
                "2 parameters.getPictureSize()"
                        + parameters.getPictureSize().width);
        setDisplayOrientation(mCamera);
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public boolean isSupportedZoom() {
        if (mCamera != null) {
            Parameters parameters = mCamera.getParameters();
            return parameters.isZoomSupported();
        }
        return false;
    }

    public int getMaxZoom() {
        if (mCamera == null) {
            mCamera = Camera.open();
        }
        mParameters = mCamera.getParameters();
        return mParameters.getMaxZoom();
    }

    // 设置Zoom
    public void setZoom(int value) {
        Log.i("tag", "value:" + value);
        mParameters.setZoom(value);
        mCamera.setParameters(mParameters);
        mCamera.startPreview();
    }

    public boolean isSupportedFlashMode() {
        if (mCamera == null) {
            mCamera = Camera.open();
        }
        Parameters parameters = mCamera.getParameters();
        List<String> modes = parameters.getSupportedFlashModes();
        if (modes != null && modes.size() != 0) {
            boolean autoSupported = modes.contains(Parameters.FLASH_MODE_AUTO);
            boolean onSupported = modes.contains(Parameters.FLASH_MODE_ON);
            boolean offSupported = modes.contains(Parameters.FLASH_MODE_OFF);
            return autoSupported && onSupported && offSupported;
        }
        return false;
    }

    // 设置闪光灯模式
    public void SetFlashMode(int flashMode) {
        switch (flashMode) {
        case FLASH_MODE_AUTO:
            mParameters.setFlashMode(Parameters.FLASH_MODE_AUTO);
            break;
        case FLASH_MODE_ON:
            mParameters.setFlashMode(Parameters.FLASH_MODE_ON);
            break;
        case FLASH_MODE_OFF:
            mParameters.setFlashMode(Parameters.FLASH_MODE_OFF);
            break;
        }
        mCamera.setParameters(mParameters);
        mCamera.startPreview();
    }
}