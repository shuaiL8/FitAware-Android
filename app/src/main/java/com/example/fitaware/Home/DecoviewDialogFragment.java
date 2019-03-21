package com.example.fitaware.Home;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.fitaware.R;
import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DecoviewDialogFragment extends DialogFragment {

    private DecoView mDecoView, mDecoView2, mDecoView3;
    private int mBackIndex, mBackIndex2, mBackIndex3;
    private int mSeries1Index;
    private int mSeries2Index;
    private int mSeries3Index;
    private final float mSeriesMax = 6000;
    TextView textPercentage;
    TextView textRemaining;
    TextView textActivity1;
    TextView textActivity2;
    TextView textActivity3;
    TextView textDate;

    float percentFilled = 0;
    float remainingMins = 0;
    float remainingKm = 0;
    float remainingCals = 0;

    String date = "2018";
    float time = 0;
    float distance = 0;
    float cals = 0;
    public static final String TAG = DecoviewDialogFragment.class.getSimpleName();

    public DecoviewDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_decoview_dialog, container, false);

        mDecoView = view.findViewById(R.id.dynamicArcView);
        mDecoView2 = view.findViewById(R.id.dynamicArcView2);
        mDecoView3 = view.findViewById(R.id.dynamicArcView3);

        textPercentage = view.findViewById(R.id.textPercentage);
        textRemaining = view.findViewById(R.id.textRemaining);
        textActivity1 = view.findViewById(R.id.textActivity1);
        textActivity2 = view.findViewById(R.id.textActivity2);
        textActivity3 = view.findViewById(R.id.textActivity3);
        textDate = view.findViewById(R.id.textDate);

        if(getArguments() != null) {
            String strDate = getArguments().getString("date");
            date = strDate;

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat mdformat = new SimpleDateFormat("yyyy-MM-dd");
            String strCurrentDate = mdformat.format(calendar.getTime());

            if(date.compareTo(strCurrentDate) <= 0){
                String strTime = getArguments().getString("time");
                time = Float.valueOf(strTime.replaceAll("steps", ""));

                String strDistance = getArguments().getString("distance");
                distance = Float.valueOf(strDistance.replaceAll("steps", ""));

                String strCals = getArguments().getString("cal");
                cals = Float.valueOf(strCals.replaceAll("steps", ""));
            }
            else {
                time = 0;

                distance = 0;

                cals = 0;
            }


        }


        textDate.setText(date);


        // Create required data series on the DecoView
        createBackSeries();
        createDataSeries3();
        createDataSeries2();
        createDataSeries1();

        // Setup events to be fired on a schedule
        createEvents();

        return view;
    }


    private void createBackSeries() {
        SeriesItem seriesItem = new SeriesItem.Builder(Color.parseColor("#FFE2E2E2"))
                .setRange(0, mSeriesMax, 0)
                .setInitialVisibility(true)
                .build();

        mBackIndex = mDecoView.addSeries(seriesItem);

        SeriesItem seriesItem2 = new SeriesItem.Builder(Color.parseColor("#FFE2E2E2"))
                .setRange(0, mSeriesMax, 0)
                .setInitialVisibility(true)
                .build();

        mBackIndex2 = mDecoView2.addSeries(seriesItem2);

        SeriesItem seriesItem3 = new SeriesItem.Builder(Color.parseColor("#FFE2E2E2"))
                .setRange(0, mSeriesMax, 0)
                .setInitialVisibility(true)
                .build();

        mBackIndex3 = mDecoView3.addSeries(seriesItem3);
    }

    private void createDataSeries1() {
        SeriesItem seriesItem = new SeriesItem.Builder(Color.parseColor("#ff6347")) //colorTime
                .setRange(0, mSeriesMax, 0)
                .setInitialVisibility(false)
                .build();

        seriesItem.addArcSeriesItemListener(new SeriesItem.SeriesItemListener() {
            @Override
            public void onSeriesItemAnimationProgress(float percentComplete, float currentPosition) {
                textActivity1.setText(String.format("%.0f / 2000", currentPosition));
            }

            @Override
            public void onSeriesItemDisplayProgress(float percentComplete) {

            }
        });

        mSeries1Index = mDecoView.addSeries(seriesItem);
    }

    private void createDataSeries2() {
        SeriesItem seriesItem = new SeriesItem.Builder(Color.parseColor("#2e8b57")) //colorCals
                .setRange(0, 2000, 0)
                .setInitialVisibility(false)
                .build();


        seriesItem.addArcSeriesItemListener(new SeriesItem.SeriesItemListener() {
            @Override
            public void onSeriesItemAnimationProgress(float percentComplete, float currentPosition) {
                textActivity2.setText(String.format("%.0f / 2000", currentPosition));
            }

            @Override
            public void onSeriesItemDisplayProgress(float percentComplete) {

            }
        });

        mSeries2Index = mDecoView2.addSeries(seriesItem);
    }

    private void createDataSeries3() {
        final SeriesItem seriesItem = new SeriesItem.Builder(Color.parseColor("#6a5acd")) //colorDis
                .setRange(0, 2000, 0)
                .setInitialVisibility(false)
                .build();


        seriesItem.addArcSeriesItemListener(new SeriesItem.SeriesItemListener() {
            @Override
            public void onSeriesItemAnimationProgress(float percentComplete, float currentPosition) {
                textActivity3.setText(String.format("%.0f / 2000", currentPosition));
            }

            @Override
            public void onSeriesItemDisplayProgress(float percentComplete) {

            }
        });


        seriesItem.addArcSeriesItemListener(new SeriesItem.SeriesItemListener() {
            @Override
            public void onSeriesItemAnimationProgress(float percentComplete, float currentPosition) {
                percentFilled = ((currentPosition - seriesItem.getMinValue()) / (seriesItem.getMaxValue() - seriesItem.getMinValue()));
                textPercentage.setText(String.format("%.0f%%", percentFilled * 100f));
            }

            @Override
            public void onSeriesItemDisplayProgress(float percentComplete) {

            }
        });


        seriesItem.addArcSeriesItemListener(new SeriesItem.SeriesItemListener() {
            @Override
            public void onSeriesItemAnimationProgress(float percentComplete, float currentPosition) {
                remainingKm = 2000 - currentPosition;

                textRemaining.setText(String.format("%.0f steps to goal", remainingKm));

            }

            @Override
            public void onSeriesItemDisplayProgress(float percentComplete) {

            }
        });

        mSeries3Index = mDecoView3.addSeries(seriesItem);
    }


    private void createEvents() {
        mDecoView.executeReset();

        mDecoView.addEvent(new DecoEvent.Builder(mSeriesMax)
                .setIndex(mBackIndex)
                .setDuration(3000)
                .setDelay(100)
                .build());
        mDecoView2.addEvent(new DecoEvent.Builder(mSeriesMax)
                .setIndex(mBackIndex2)
                .setDuration(3000)
                .setDelay(100)
                .build());
        mDecoView3.addEvent(new DecoEvent.Builder(mSeriesMax)
                .setIndex(mBackIndex3)
                .setDuration(3000)
                .setDelay(100)
                .build());

        mDecoView.addEvent(new DecoEvent.Builder(time)
                .setIndex(mSeries1Index)
                .setDelay(110)
                .build());

        mDecoView2.addEvent(new DecoEvent.Builder(cals)
                .setIndex(mSeries2Index)
                .setDelay(120)
                .build());

        mDecoView3.addEvent(new DecoEvent.Builder(distance)
                .setIndex(mSeries3Index)
                .setDelay(130)
                .build());

    }
}
