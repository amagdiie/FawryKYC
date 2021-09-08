package fawry.kyc.lib.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smarteist.autoimageslider.SliderView;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.List;

import fawry.kyc.lib.AssetsUtils;
import fawry.kyc.lib.MainActivity;
import fawry.kyc.lib.R;
import fawry.kyc.lib.SliderAdapterExample;

public class SubmitDataFragment extends Fragment {

    private View mView;

    private SliderView mImageSlider;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_submit_data, container, false);

        ((MainActivity)getActivity()).mCancelSubmit.setVisibility(View.VISIBLE);
        ((MainActivity)getActivity()).mAppBarLayout.setBackgroundColor(getActivity().getResources().getColor(R.color.white));
        ((MainActivity)getActivity()).mAppBarTitle.setText("myFawry Prepaid card");
        ((MainActivity)getActivity()).mAppBarTitle.setTextColor(getActivity().getResources().getColor(R.color.black));

        mImageSlider = mView.findViewById(R.id.imageSlider);

        SliderAdapterExample sliderAdapterExample = new SliderAdapterExample(getContext());
        sliderAdapterExample.addItem(AssetsUtils.frontIdImageBitmap);
        sliderAdapterExample.addItem(AssetsUtils.backIdImageBitmap);
        mImageSlider.setSliderAdapter(sliderAdapterExample);

        return mView;
    }
}