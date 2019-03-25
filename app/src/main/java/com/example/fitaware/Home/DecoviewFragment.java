package com.example.fitaware.Home;

import android.graphics.Color;
import android.opengl.Visibility;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.fitaware.R;
import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.DecoDrawEffect;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;

public class DecoviewFragment extends Fragment {

    private DecoView mDecoView,mDecoView2,mDecoView3;
    private int mBackIndex;
    private int mBackIndex2;
    private int mBackIndex3;

    private int mSeries1Index;
    private int mSeries2Index;
    private int mSeries3Index;
    private final float mSeriesMax = 6000;
    TextView textPercentage;
    TextView textRemaining;
    TextView textActivity1;
    TextView textActivity2;
    TextView textActivity3;
    TextView rankActivity1;
    TextView rankActivity2;
    TextView rankActivity3;

    ImageView imageActivity1;
    Button demo1, demo2, clear;
    float percentFilled = 0;
    float remainingMins = 0;
    float remainingKm = 0;
    float remainingCals = 0;

    float activity_1 = 1500;
    float activity_2 = 1200;
    float activity_3 = 4100;

    public DecoviewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_decoview, container, false);

        mDecoView = view.findViewById(R.id.dynamicArcView);
        mDecoView2 = view.findViewById(R.id.dynamicArcView2);
        mDecoView3 = view.findViewById(R.id.dynamicArcView3);

        textPercentage = view.findViewById(R.id.textPercentage);
        textRemaining = view.findViewById(R.id.textRemaining);
        textActivity1 = view.findViewById(R.id.textActivity1);
        textActivity2 = view.findViewById(R.id.textActivity2);
        textActivity3 = view.findViewById(R.id.textActivity3);

        rankActivity1 = view.findViewById(R.id.rankActivity1);
        rankActivity2 = view.findViewById(R.id.rankActivity2);
        rankActivity3 = view.findViewById(R.id.rankActivity3);

        demo1 = view.findViewById(R.id.demo1);
        demo2 = view.findViewById(R.id.demo2);
        clear = view.findViewById(R.id.clear);

        mDecoView.setVisibility(View.GONE);
        rankActivity1.setVisibility(View.GONE);
        textActivity1.setVisibility(View.GONE);

        imageActivity1 = view.findViewById(R.id.imageActivity1);
        imageActivity1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mDecoView.getVisibility() == View.GONE){
                    imageActivity1.setImageResource(R.drawable.ic_person_black_24dp);
                    mDecoView.setVisibility(View.VISIBLE);
                    rankActivity1.setVisibility(View.VISIBLE);
                    textActivity1.setVisibility(View.VISIBLE);
                }
                else {
                    imageActivity1.setImageResource(R.drawable.ic_add_black_24dp);
                    mDecoView.setVisibility(View.GONE);
                    rankActivity1.setVisibility(View.GONE);
                    textActivity1.setVisibility(View.GONE);
                }

            }
        });

        demo1.setVisibility(View.GONE);
        clear.setVisibility(View.GONE);
        demo2.setText("DEMO");

        demo1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                demo1();
            }
        });

        demo2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (demo2.getText() == "DEMO") {
                    demo1.setVisibility(View.VISIBLE);
                    clear.setVisibility(View.VISIBLE);
                    demo2.setText("100%");
                }
                else if(demo2.getText() == "100%") {
                    demo2();
                    demo1.setEnabled(false);
                }
            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                clear();
                demo1.setVisibility(View.GONE);
                clear.setVisibility(View.GONE);
                demo2.setText("DEMO");
                demo1.setEnabled(true);
            }
        });


        if(getArguments() != null) {
            String strTime = getArguments().getString("time");
            activity_1 = Float.parseFloat(strTime);

            String strDistance = getArguments().getString("distance");
            activity_2 = Float.parseFloat(strDistance);

            String strCals = getArguments().getString("cals");
            activity_3 = Float.parseFloat(strCals);
        }



        // Create required data series on the DecoView
        createBackSeries();
        createDataSeries1();
        createDataSeries2();
        createDataSeries3();

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
        SeriesItem seriesItem = new SeriesItem.Builder(Color.parseColor("#77e6f1")) //colorActivity1
                .setRange(0, 2000, 0)
                .setInitialVisibility(false)
                .build();

        seriesItem.addArcSeriesItemListener(new SeriesItem.SeriesItemListener() {
            @Override
            public void onSeriesItemAnimationProgress(float percentComplete, float currentPosition) {
                rankActivity1.setText("No. 1");
                textActivity1.setText(String.format("%.0f / 2000", currentPosition));
            }

            @Override
            public void onSeriesItemDisplayProgress(float percentComplete) {

            }
        });

        mSeries1Index = mDecoView.addSeries(seriesItem);
    }

    private void createDataSeries2() {

        final SeriesItem seriesItem = new SeriesItem.Builder(Color.parseColor("#3ebfab")) //colorActivity2
                .setRange(0, 2000, 0)
                .setInitialVisibility(false)
                .build();


        seriesItem.addArcSeriesItemListener(new SeriesItem.SeriesItemListener() {
            @Override
            public void onSeriesItemAnimationProgress(float percentComplete, float currentPosition) {
                rankActivity2.setText("No. 2");
                textActivity2.setText(String.format("%.0f / 2000", currentPosition));
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

        mSeries2Index = mDecoView2.addSeries(seriesItem);


    }

    private void createDataSeries3() {
        SeriesItem seriesItem = new SeriesItem.Builder(Color.parseColor("#ff6347")) //colorActivity3
                .setRange(0, mSeriesMax, 0)
                .setInitialVisibility(false)
                .build();


        seriesItem.addArcSeriesItemListener(new SeriesItem.SeriesItemListener() {
            @Override
            public void onSeriesItemAnimationProgress(float percentComplete, float currentPosition) {
                rankActivity3.setText("No. 2");
                textActivity3.setText(String.format("%.0f / 6000", currentPosition));
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
                .setDuration(1000)
                .setDelay(100)
                .build());
        mDecoView2.addEvent(new DecoEvent.Builder(mSeriesMax)
                .setIndex(mBackIndex2)
                .setDuration(1000)
                .setDelay(100)
                .build());
        mDecoView3.addEvent(new DecoEvent.Builder(mSeriesMax)
                .setIndex(mBackIndex3)
                .setDuration(1000)
                .setDelay(100)
                .build());


        mDecoView.addEvent(new DecoEvent.Builder(DecoDrawEffect.EffectType.EFFECT_SPIRAL_OUT)
                .setIndex(mSeries1Index)
                .setDuration(1000)
                .setDelay(300)
                .build());
        mDecoView2.addEvent(new DecoEvent.Builder(DecoDrawEffect.EffectType.EFFECT_SPIRAL_OUT)
                .setIndex(mSeries2Index)
                .setDuration(1000)
                .setDelay(300)
                .build());
        mDecoView3.addEvent(new DecoEvent.Builder(DecoDrawEffect.EffectType.EFFECT_SPIRAL_OUT)
                .setIndex(mSeries3Index)
                .setDuration(1000)
                .setDelay(300)
                .build());
    }

    private void demo1() {

        mDecoView.addEvent(new DecoEvent.Builder(activity_1)
                .setIndex(mSeries1Index)
                .setDuration(1000)
                .setDelay(100)
                .build());
        mDecoView2.addEvent(new DecoEvent.Builder(activity_2)
                .setIndex(mSeries2Index)
                .setDuration(1000)
                .setDelay(100)
                .build());
        mDecoView3.addEvent(new DecoEvent.Builder(activity_3)
                .setIndex(mSeries3Index)
                .setDuration(1000)
                .setDelay(100)
                .build());
    }

    private void demo2() {

        mDecoView.addEvent(new DecoEvent.Builder(2000).setIndex(mSeries1Index).setDelay(100).setDuration(1000).build());

        mDecoView2.addEvent(new DecoEvent.Builder(2000).setIndex(mSeries2Index).setDelay(100).setDuration(1000).build());

        mDecoView3.addEvent(new DecoEvent.Builder(mSeriesMax)
                .setIndex(mSeries3Index)
                .setDelay(100)
                .setDuration(1000)
                .build());

        mDecoView3.addEvent(new DecoEvent.Builder(DecoDrawEffect.EffectType.EFFECT_SPIRAL_EXPLODE)
                .setIndex(mSeries1Index)
                .setDelay(1500)
                .setDuration(2000)
                .build());

        mDecoView2.addEvent(new DecoEvent.Builder(DecoDrawEffect.EffectType.EFFECT_SPIRAL_EXPLODE)
                .setIndex(mSeries1Index)
                .setDelay(1500)
                .setDuration(2000)
                .build());


        mDecoView.addEvent(new DecoEvent.Builder(DecoDrawEffect.EffectType.EFFECT_SPIRAL_EXPLODE)
                .setIndex(mSeries1Index)
                .setDelay(1500)
                .setDuration(2000)
                .setDisplayText("Nice Job!")
                .setListener(new DecoEvent.ExecuteEventListener() {
                    @Override
                    public void onEventStart(DecoEvent decoEvent) {
                        resetText();
                    }

                    @Override
                    public void onEventEnd(DecoEvent decoEvent) {
                        mDecoView.addEvent(new DecoEvent.Builder(mSeriesMax)
                                .setIndex(mBackIndex)
                                .setDuration(2000)
                                .setDelay(100)
                                .build());
                        mDecoView2.addEvent(new DecoEvent.Builder(mSeriesMax)
                                .setIndex(mBackIndex2)
                                .setDuration(2000)
                                .setDelay(100)
                                .build());
                        mDecoView3.addEvent(new DecoEvent.Builder(mSeriesMax)
                                .setIndex(mBackIndex3)
                                .setDuration(2000)
                                .setDelay(100)
                                .build());

                        mDecoView.addEvent(new DecoEvent.Builder(2000)
                                .setIndex(mSeries1Index)
                                .setDelay(110)
                                .build());
                        mDecoView2.addEvent(new DecoEvent.Builder(2000)
                                .setIndex(mSeries2Index)
                                .setDelay(120)
                                .build());
                        mDecoView3.addEvent(new DecoEvent.Builder(mSeriesMax)
                                .setIndex(mSeries3Index)
                                .setDelay(130)
                                .build());

                    }
                })
                .build());
    }

    private void clear(){
        mDecoView.addEvent(new DecoEvent.Builder(0)
                .setIndex(mSeries1Index)
                .setDelay(0)
                .setDuration(0)
                .build());
        mDecoView2.addEvent(new DecoEvent.Builder(0)
                .setIndex(mSeries2Index)
                .setDelay(0)
                .setDuration(0)
                .build());
        mDecoView3.addEvent(new DecoEvent.Builder(0)
                .setIndex(mSeries3Index)
                .setDelay(0)
                .setDuration(0)
                .build());
    }

    private void resetText() {
//        textActivity1.setText("");
//        textActivity2.setText("");
//        textActivity3.setText("");
        textPercentage.setText("");
        textRemaining.setText("");

        rankActivity1.setText("");
        rankActivity2.setText("");
        rankActivity3.setText("");

    }

    public interface OnFragmentInteractionListener {
    }
}
