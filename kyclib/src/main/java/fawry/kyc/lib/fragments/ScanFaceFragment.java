package fawry.kyc.lib.fragments;

import android.animation.Animator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.chaquo.python.PyObject;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.VideoResult;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import fawry.kyc.lib.AssetsUtils;
import fawry.kyc.lib.MainActivity;
import fawry.kyc.lib.R;

public class ScanFaceFragment extends Fragment{

    private View mView;
    private CameraView cameraView;

    private LottieAnimationView lottieAnimationView;
    private String status;
    private String imageName;

    private Context context;
    int PERMISSION_ALL = 1;
    boolean flagPermissions = false;

    String[] PERMISSIONS = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA
    };

    private ProgressDialog mProgressDialog;

    private ImageView mRecordImage;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_scan_face, container, false);
        cameraView = mView.findViewById(R.id.cameraView);
        cameraView.setLifecycleOwner(getViewLifecycleOwner());

        context = getActivity().getApplicationContext();

        mRecordImage = mView.findViewById(R.id.recordImage);

        if (!flagPermissions) {
            checkPermissions();
        }

//        SizeSelector width = SizeSelectors.maxWidth(R.integer.image_resolution_face);
//        SizeSelector height = SizeSelectors.maxHeight(R.integer.image_resolution_face);
//        SizeSelector dimensions = SizeSelectors.and(width, height); // Matches sizes bigger than 1000x2000.
//        SizeSelector ratio = SizeSelectors.aspectRatio(AspectRatio.of(1, 2), 0); // Matches 1:1 sizes.
//        SizeSelector result = SizeSelectors.or(
//                SizeSelectors.and(ratio, dimensions), // Try to match both constraints
//                ratio, // If none is found, at least try to match the aspect ratio
//                SizeSelectors.biggest() // If none is found, take the biggest
//        );
//        cameraView.setPictureSize(result);

        cameraView.addCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(@NonNull PictureResult result) {
                try {

                    Log.e("Ahmed", "orientation taken: "+ result.getRotation());

                    result.toFile(createImageFile(), file -> {
                        if (file != null){
                            imageName = file.getName();
                            AssetsUtils.faceIdImage = imageName;
                            Log.e("Ahmed", "FaceScan:" +imageName);
                            Log.e("Ahmed", "FrontID:" +AssetsUtils.frontIdImage);
                            result.toBitmap(1000, 1000, bitmap -> {
                                if (bitmap != null){
                                    AssetsUtils.faceIdImageBitmap = bitmap;
                                    Log.e("Ahmed", "FaceScanBitmap: True");
                                    doOCR(AssetsUtils.frontIdImage, AssetsUtils.faceIdImage);
                                }
                            });
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("Ahmed", e.getMessage());
                }
            }

            @Override
            public void onOrientationChanged(int orientation) {
                super.onOrientationChanged(orientation);
                Log.e("Ahmed", "orientation: "+ orientation);

            }

            @Override
            public void onVideoTaken(@NonNull VideoResult result) {


            }
        });


        mRecordImage.setOnClickListener(v -> {
            lottieAnimationView.playAnimation();
            lottieAnimationView.setVisibility(View.VISIBLE);
            mRecordImage.setVisibility(View.GONE);
        });
        lottieAnimationView = mView.findViewById(R.id.record);
        lottieAnimationView.setSpeed(1);
        lottieAnimationView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                new Handler().postDelayed(() -> cameraView.takePicture(), 1000);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mRecordImage.setVisibility(View.VISIBLE);
                lottieAnimationView.setVisibility(View.GONE);
                cameraView.clearFrameProcessors();
                cameraView.close();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        ((MainActivity)getActivity()).mAppBarTitle.setText("Record Video");
        return mView;
    }

//    private void checkImage() {
//        if (mProgressDialog == null) {
//            mProgressDialog = ProgressDialog.show(getActivity(), "Processing",
//                    "Doing OCR...", true);
//        } else {
//            mProgressDialog.show();
//        }
//        new Thread(() -> getActivity().runOnUiThread(() -> {
//            PyObject outPut = ((MainActivity)getActivity()).pyObject.callAttr("faceComparison", AssetsUtils.faceIdImage, imageName);
//            if (outPut.toString().equals("success")){
//                Toast.makeText(getActivity(), "Success Scan Front ID!", Toast.LENGTH_SHORT).show();
//                Log.e("Ahmed", "FrontScanBitmap: Face Detected");
//            }else{
//                Toast.makeText(getActivity(), "Error in Front ID, Please try again.!", Toast.LENGTH_SHORT).show();
//                Log.e("Ahmed", "FrontScanBitmap: No Face Detected");
//                ((MainActivity)getActivity()).mScanButton.setEnabled(true);
//                ((MainActivity)getActivity()).check = 0;
//                cameraView.setLifecycleOwner(getViewLifecycleOwner());
//            }
//            mProgressDialog.dismiss();
//        })).start();
//    }

    private void doOCR(String imageId, String imageFace) {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(getActivity(), "Processing",
                    "Doing Detection...", true);
        } else {
            mProgressDialog.show();
        }
        new Thread(() -> getActivity().runOnUiThread(() -> {
            PyObject outPut = ((MainActivity)getActivity()).pyObject.callAttr("faceComparison", imageId, imageFace);
            Log.e("Ahmed", outPut.toString());
            status = outPut.toString();
            if (status.equals("[True]")) {
                ((MainActivity)getActivity()).goToNext(3);
                Toast.makeText(getActivity(), "Successfully Recognition", Toast.LENGTH_SHORT).show();
                Log.e("Ahmed", "Successfully Recognition");
            }else{
                Toast.makeText(getActivity(), "Failed Recognition "+ status+" please try again later.!", Toast.LENGTH_SHORT).show();
                Log.e("Ahmed", "Failed Recognition "+ status);
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                getActivity().startActivity(intent);
            }
            mProgressDialog.dismiss();
        })).start();
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
}