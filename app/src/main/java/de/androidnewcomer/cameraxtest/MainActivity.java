package de.androidnewcomer.cameraxtest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int RC_PERMISSION = 123;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private PreviewView viewFinder;
    private View captureButton;
    private ImageCapture imageCapture;

    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewFinder = findViewById(R.id.viewFinder);
        captureButton = findViewById(R.id.camera_capture_button);
        captureButton.setOnClickListener(this::takePhoto);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        if(permissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, RC_PERMISSION);
        }
    }

    private void takePhoto(View view) {
        if(imageCapture!=null) {
            DateFormat simpleDateFormat = SimpleDateFormat.getDateTimeInstance();
            File file = new File(ContextCompat.getDataDir(this), simpleDateFormat.format(new Date())+".jpg");
            ImageCapture.OutputFileOptions options = (new ImageCapture.OutputFileOptions.Builder(file)).build();
            imageCapture.takePicture(options, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                    Toast.makeText(getBaseContext(),"pic saved as " + Uri.fromFile(file),Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(@NonNull ImageCaptureException exception) {
                    Log.e(getClass().getSimpleName(), "could not capture pic",exception);
                }
            });
        } else {
            Toast.makeText(getBaseContext(), "camera not yet available", Toast.LENGTH_SHORT).show();
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> listenableFuture = ProcessCameraProvider.getInstance(this);
        listenableFuture.addListener(()->{
            try {
                ProcessCameraProvider processCameraProvider = listenableFuture.get();
                Preview.Builder builder = new Preview.Builder();
                Preview preview = builder.build();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());
                processCameraProvider.unbindAll();

                imageCapture = (new ImageCapture.Builder()).build();
                processCameraProvider.bindToLifecycle(this,CameraSelector.DEFAULT_BACK_CAMERA,preview, imageCapture);

            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), "startCamera failed",e);
            }
        },ContextCompat.getMainExecutor(this));
    }

    private boolean permissionsGranted() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == RC_PERMISSION) {
            if ( grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                // Pech
                finish();
            } else {
                startCamera();
            }
        }
    }
}