package textrecognition.blog.com.textrecognition;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class text_Recognition_camera extends AppCompatActivity {

    private SurfaceView surfaceView;
    private TextView textView;
    private CameraSource cameraSource;
    final int RequestCameraPermissionID = 1001;
    Button btn;
    public static final String PROFILE_DATA_KEY = "profile_data_key";
    private List<String> profile_data = new ArrayList<String>();
    TextRecognizer textRecognizer;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {

        switch (requestCode)
        {
            case RequestCameraPermissionID:
            {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                        return;
                    try
                    {
                        cameraSource.start(surfaceView.getHolder());
                    }
                    catch (IOException e)
                    {e.printStackTrace();}
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text__recognition_camera);

        surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        textView = (TextView) findViewById(R.id.camera_txt);
        btn = (Button) findViewById(R.id.btn);

       textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        if (!textRecognizer.isOperational()) {
            Log.w("MainActivity", "Detector dependencies are not yet available.");

        }
        else
        {
            cameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setAutoFocusEnabled(true)
                    .setRequestedPreviewSize(1280, 1024)
                    .setRequestedFps(2.0f)
                    .build();

            SurfaceHolder sfhTrackHolder = surfaceView.getHolder();
            sfhTrackHolder.addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {

                    try {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(text_Recognition_camera.this,new String[]{Manifest.permission.CAMERA},RequestCameraPermissionID);
                            return;
                        }
                        cameraSource.start(surfaceView.getHolder());
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder)
                {
                    cameraSource.stop();
                }
            });
        }

        textRecognizer.setProcessor(new Detector.Processor<TextBlock>()
        {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<TextBlock> detections)
            {
                final SparseArray<TextBlock> items=detections.getDetectedItems();
                if(items.size() != 0)
                {
                    textView.post(new Runnable() {
                        @Override
                        public void run()
                        {
                            StringBuilder stringBuilder =new StringBuilder();
                            for (int i=0;i<items.size();++i)
                            {
                                TextBlock item=items.valueAt(i);
                                stringBuilder.append(item.getValue());
                                stringBuilder.append("\n");
                            }

                            String capturedString = stringBuilder.toString();
                            textView.setText(capturedString);

                            if (StringUtils.isNotBlank(capturedString)){
                                profile_data.add(capturedString);
                            }
                        }
                    });
                }
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textRecognizer.release();
                Intent intent = new Intent(text_Recognition_camera.this, ProfileCreatorActivity.class);
                intent.putStringArrayListExtra(PROFILE_DATA_KEY, (ArrayList<String>) profile_data);
                startActivity(intent);
                finish();

            }
        });

    }
}
