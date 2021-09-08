package fawry.kyc.lib;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.android.material.appbar.AppBarLayout;
import com.vinay.stepview.HorizontalStepView;
import com.vinay.stepview.models.Step;

import java.util.ArrayList;
import java.util.List;

import fawry.kyc.lib.fragments.ScanBackFragment;
import fawry.kyc.lib.fragments.ScanFaceFragment;
import fawry.kyc.lib.fragments.ScanFrontFragment;
import fawry.kyc.lib.fragments.SubmitDataFragment;

public class MainActivity extends AppCompatActivity {

    public List<Step> stepList;
    public HorizontalStepView horizontalStepView;

    public CardView mScanButton;

    private Context context;
    int PERMISSION_ALL = 1;
    boolean flagPermissions = false;

    public TextView mAppBarTitle, mCancelSubmit;

    String[] PERMISSIONS = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA
    };

    public PyObject pyObject;

    public int check = 0;

    private LinearLayout mBottomNav;

    public ImageView mGoBack;

    public AppBarLayout mAppBarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        horizontalStepView = findViewById(R.id.horizontal_step_view);
        stepList = new ArrayList<>();

        mCancelSubmit = findViewById(R.id.cancelSubmit);
        mCancelSubmit.setOnClickListener(v -> onBackPressed());
        mAppBarLayout = findViewById(R.id.appBarLayout);
        mGoBack = findViewById(R.id.goBack);
        mGoBack.setOnClickListener(v -> onBackPressed());

        stepList.add(new Step(""));
        stepList.add(new Step(""));
        stepList.add(new Step(""));
        stepList.add(new Step("")); // State defaults to NOT_COMPLETED

        mBottomNav = findViewById(R.id.bottomNav);

        mAppBarTitle = findViewById(R.id.titleAppBar);

        horizontalStepView
                .setCompletedStepIcon(AppCompatResources.getDrawable(this, R.drawable.ic_stepper_complete))
                .setNotCompletedStepIcon(AppCompatResources.getDrawable(this, R.drawable.ic_stepper_non_complete))
                .setCurrentStepIcon(AppCompatResources.getDrawable(this, R.drawable.ic_stepper_error))
                .setCompletedLineColor(Color.parseColor("#FFD404"))
                .setNotCompletedLineColor(Color.parseColor("#BBBBBB"))
                .setCircleRadius(15)
                .setLineLength(80);

        if (!flagPermissions) {
            checkPermissions();
        }

        context = this.getApplicationContext();


        new Thread(() -> runOnUiThread(() -> {
            if (!Python.isStarted()){
                Python.start(new AndroidPlatform(context));
            }

            Python py = Python.getInstance();
            pyObject = py.getModule("model_detection");
        })).start();

        mScanButton = findViewById(R.id.scanButtonClick);
        goToNext(check);


        mScanButton.setOnClickListener(v -> {
            if (!flagPermissions) {
                checkPermissions();
                return;
            }
            switch (check){
                case 0:
                    ScanFrontFragment scanFrontFragment = (ScanFrontFragment) getSupportFragmentManager()
                            .findFragmentByTag("ScanFront");
                    scanFrontFragment.startScan();
                    check++;
                    mScanButton.setEnabled(false);
                    break;
                case 1:
                    ScanBackFragment scanBackFragment = (ScanBackFragment) getSupportFragmentManager()
                            .findFragmentByTag("ScanBack");
                    scanBackFragment.startScan();
                    check++;
                    mScanButton.setEnabled(false);
                    break;
            }

        });

    }

    public void goToNext(int check){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        switch (check){
            case 0:
                ScanFrontFragment scanFrontFragment = new ScanFrontFragment();
                transaction.replace(R.id.fragmentContainer, scanFrontFragment, "ScanFront");
                transaction.addToBackStack(null);
                transaction.commit();
                stepList.get(0).setState(Step.State.CURRENT);
                stepList.get(1).setState(Step.State.NOT_COMPLETED);
                stepList.get(2).setState(Step.State.NOT_COMPLETED);
                stepList.get(3).setState(Step.State.NOT_COMPLETED);
                mScanButton.setEnabled(true);
                horizontalStepView.setSteps(stepList);
                break;
            case 1:
                ScanBackFragment scanBackFragment = new ScanBackFragment();
                transaction.replace(R.id.fragmentContainer, scanBackFragment, "ScanBack");
                transaction.addToBackStack(null);
                transaction.commit();
                stepList.get(0).setState(Step.State.COMPLETED);
                stepList.get(1).setState(Step.State.CURRENT);
                stepList.get(2).setState(Step.State.NOT_COMPLETED);
                stepList.get(3).setState(Step.State.NOT_COMPLETED);
                horizontalStepView.setSteps(stepList);
                runOnUiThread(() -> mScanButton.setEnabled(true));
                break;
            case 2:
                ScanFaceFragment scanFaceFragment = new ScanFaceFragment();
                transaction.replace(R.id.fragmentContainer, scanFaceFragment, "ScanFace");
                transaction.addToBackStack(null);
                transaction.commit();
                stepList.get(0).setState(Step.State.COMPLETED);
                stepList.get(1).setState(Step.State.COMPLETED);
                stepList.get(2).setState(Step.State.CURRENT);
                stepList.get(3).setState(Step.State.NOT_COMPLETED);
                horizontalStepView.setSteps(stepList);
                runOnUiThread(() -> mBottomNav.setVisibility(View.GONE));
                break;
            case 3:
                SubmitDataFragment submitDataFragment = new SubmitDataFragment();
                transaction.replace(R.id.fragmentContainer, submitDataFragment, "SubmitData");
                transaction.addToBackStack(null);
                transaction.commit();
                stepList.get(0).setState(Step.State.COMPLETED);
                stepList.get(1).setState(Step.State.COMPLETED);
                stepList.get(2).setState(Step.State.COMPLETED);
                stepList.get(3).setState(Step.State.CURRENT);
                horizontalStepView.setSteps(stepList);
                runOnUiThread(() -> mBottomNav.setVisibility(View.GONE));
                break;
        }
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

    @Override
    public void onBackPressed() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage("Are you sure you want to cancel your Prepaid Card submission request?");
        dialogBuilder.setPositiveButton("Cancel It", (dialog, which) -> {
            startActivity(new Intent(MainActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        });
        dialogBuilder.setNegativeButton("Keep It", (dialog, which) -> dialog.cancel());
        dialogBuilder.show();
    }
}