package com.quangtd.camera2demo01;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Size;
import android.view.TextureView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private String cameraId;
    private TextureView textureView;
    private CameraDevice cameraDevice;
    private Size previewSize;
    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            setupCamera(width, height);
            openCamera();
        }

        @Override public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    private CameraDevice.StateCallback cameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override public void onOpened(@NonNull CameraDevice camera) {
            // khi mở Camera thành công thì gán vào biến "cameraDevice"
            // về sau chung ta chỉ sử dụng biến "cameraDevie" để mở các kết nối, thực hiện các thao tác khác
            cameraDevice = camera;

            createCameraPreviewSession();
            Toast.makeText(getApplicationContext(), "Camera open", Toast.LENGTH_SHORT).show();

        }

        @Override public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
            cameraDevice = null;
        }

        @Override public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
            cameraDevice = null;
        }
    };
    private TextureView textTureView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textTureView = findViewById(R.id.textureView);
    }

    @Override protected void onResume() {
        super.onResume();
        if (textTureView.isAvailable()) {
            setupCamera(textureView.getWidth(), textureView.getHeight());
            openCamera();
        } else {
            textTureView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    @Override protected void onPause() {
        super.onPause();
    }

    private void setupCamera(int width, int height) {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String id : cameraManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(id);
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraMetadata.LENS_FACING_FRONT) {
                    continue;
                }
                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                // Set Size để hiển thị lên màn hình
                previewSize = getPreferredPreviewsSize(
                        map.getOutputSizes(SurfaceTexture.class),
                        width,
                        height);
                cameraId = id;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private Size getPreferredPreviewsSize(Size[] mapSize, int width, int height) {
        List<Size> collectorSize = new ArrayList<>();
        for (Size option : mapSize) {
            if (width > height) {
                if (option.getWidth() > width && option.getHeight() > height) {
                    collectorSize.add(option);
                }
            } else {
                if (option.getWidth() > height && option.getHeight() > width) {
                    collectorSize.add(option);
                }
            }
        }
        if (collectorSize.size() > 0) {
            return Collections.min(collectorSize, new Comparator<Size>() {
                @Override
                public int compare(Size lhs, Size rhs) {
                    return Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getHeight() * rhs.getWidth());
                }
            });
        }
        return mapSize[0];
    }

    private void openCamera() {
        CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            cameraManager.openCamera(cameraId, cameraDeviceStateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    private void createCameraPreviewSession() {
        //
    }
}
