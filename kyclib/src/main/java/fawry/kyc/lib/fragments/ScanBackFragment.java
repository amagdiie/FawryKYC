package fawry.kyc.lib.fragments;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.PictureResult;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import fawry.kyc.lib.AssetsUtils;
import fawry.kyc.lib.ClassificationFrameProcessor;
import fawry.kyc.lib.ClassificationResult;
import fawry.kyc.lib.MainActivity;
import fawry.kyc.lib.R;
import fawry.kyc.lib.configs.GtsrbQuantConfig;
import fawry.kyc.lib.configs.ModelConfig;

public class ScanBackFragment extends Fragment implements ClassificationFrameProcessor.ClassificationListener{

    private View mView;
    private CameraView cameraView;
    private ClassificationFrameProcessor classificationFrameProcessor;

    private String imageName;

    private Context context;
    int PERMISSION_ALL = 1;
    boolean flagPermissions = false;

    String[] PERMISSIONS = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_scan_back, container, false);

        cameraView = mView.findViewById(R.id.cameraView);
        cameraView.setLifecycleOwner(getViewLifecycleOwner());
        initClassification();

        context = getActivity().getApplicationContext();
        if (!flagPermissions) {
            checkPermissions();
        }

//        SizeSelector width = SizeSelectors.maxWidth(R.integer.image_resolution);
//        SizeSelector height = SizeSelectors.maxHeight(R.integer.image_resolution);
//        SizeSelector dimensions = SizeSelectors.and(width, height); // Matches sizes bigger than 1000x2000.
//        SizeSelector ratio = SizeSelectors.aspectRatio(AspectRatio.of(2, 1), 0); // Matches 1:1 sizes.
//
//        SizeSelector result = SizeSelectors.or(
//                SizeSelectors.and(ratio, dimensions), // Try to match both constraints
//                ratio, // If none is found, at least try to match the aspect ratio
//                SizeSelectors.biggest() // If none is found, take the biggest
//        );
//        cameraView.setPictureSize(result);

        cameraView.addCameraListener(new CameraListener() {
            @Override
            public void onOrientationChanged(int orientation) {
                super.onOrientationChanged(orientation);
                Log.e("Ahmed", "orientation: "+ orientation);

            }

            @Override
            public void onPictureTaken(@NonNull PictureResult result) {
                try {
                    Log.e("Ahmed", "orientation taken: "+ result.getRotation());

                    result.toFile(createImageFile(), file -> {
                        if (file != null){
                            imageName = file.getName();
                            AssetsUtils.backIdImage = imageName;
                            Log.e("Ahmed", "BackScan:" +imageName);
                            cameraView.clearFrameProcessors();
                            cameraView.close();

                            result.toBitmap(1000, 1000, bitmap -> {
                                if (bitmap != null){
                                    AssetsUtils.backIdImageBitmap = bitmap;
                                    Log.e("Ahmed", "BackScanBitmap: True");
                                    ((MainActivity)getActivity()).goToNext(2);
                                }
                            });
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("Ahmed", e.getMessage());
                }
            }
        });

        return mView;
    }

    void checkPermissions() {
        if (!hasPermissions(context, PERMISSIONS)) {
            requestPermissions(PERMISSIONS,
                    PERMISSION_ALL);
            flagPermissions = false;
        }
        flagPermissions = true;

    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public void startScan(){
        cameraView.addFrameProcessor(classificationFrameProcessor);
    }

    private void initClassification() {
        try {
            ModelConfig modelConfig = new GtsrbQuantConfig();
            classificationFrameProcessor = new ClassificationFrameProcessor(getActivity(), this, modelConfig);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Ahmed", e.getMessage());
            Toast.makeText(getActivity(), "Frame Processor initialization failed", Toast.LENGTH_SHORT).show();
        }
    }

    public File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("MMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".png",         /* suffix */
                storageDir      /* directory */
        );
        return image;
    }

    @Override
    public void onClassifiedFrame(List<ClassificationResult> classificationResults) {
        StringBuilder results = new StringBuilder("Classification:\n");
        if (classificationResults.size() == 0) {
            results.append("No results");
        } else {
            for (ClassificationResult classificationResult : classificationResults) {
                results.append(classificationResult.title)
                        .append("(")
                        .append(classificationResult.confidence * 100)
                        .append("%)\n");

                if (classificationResult.confidence * 100 >= 75 && classificationResult.title.equals("BackID")){
                    Toast.makeText(getActivity(), classificationResult.title, Toast.LENGTH_SHORT).show();
                    cameraView.takePicture();
                }
            }
        }
    }
}