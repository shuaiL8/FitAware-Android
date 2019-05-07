package com.example.fitaware.Home;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.fitaware.R;
import android.widget.ArrayAdapter;
import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.DecoDrawEffect;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;

import java.util.ArrayList;

public class TeammatesAdapter extends ArrayAdapter<Teammates>{
    private static final String TAG = "TeammatesAdapter";

    private Context context;
    private int layoutResourceId;
    private ArrayList<Teammates> data = new ArrayList<Teammates>();
    private DecoView mDecoView;
    private TextView stepsOfGoal;
    private int mBackIndex = 0;
    private int mSeries1Index = 0;

    private float memberSteps = 0;
    private float goal = 0;
    private String mColor ="#000000";

    public TeammatesAdapter(Context context, int layoutResourceId, ArrayList<Teammates> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        TeammatesAdapter.ViewHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new TeammatesAdapter.ViewHolder();
            holder.rankOfTeammates = (TextView) row.findViewById(R.id.rankOfTeammates);
            holder.userID = (TextView) row.findViewById(R.id.userID);
            stepsOfGoal = (TextView) row.findViewById(R.id.stepsOfGoal);
            mDecoView = row.findViewById(R.id.dynamicArcViews);

            holder.image = (ImageView) row.findViewById(R.id.imageView_userIcon);
            row.setTag(holder);
        } else {
            holder = (TeammatesAdapter.ViewHolder) row.getTag();
        }


        Teammates item = data.get(position);

        if(item.getColor() != "") {
            mColor = item.getColor();
        }
        memberSteps = Float.valueOf(item.getSteps());
        goal = Float.valueOf(item.getGoal());


        holder.rankOfTeammates.setText("No. "+item.getRank());
        holder.userID.setText(item.getName());

        stepsOfGoal.setText(String.format("%.0f / %.0f", memberSteps, goal));

        holder.image.setImageBitmap(item.getImage());


        createBackSeries();
        createDataSeries();
        createEvents();

        return row;
    }

    class ViewHolder {
        TextView rankOfTeammates;
        TextView userID;

        ImageView image;
    }

    private void createBackSeries() {
        SeriesItem seriesItem = new SeriesItem.Builder(Color.parseColor("#FFE2E2E2"))
                .setRange(0, 2000f, 0)
                .setInitialVisibility(true)
                .build();

        mBackIndex = mDecoView.addSeries(seriesItem);

    }

    private void createDataSeries() {
        SeriesItem seriesItem = new SeriesItem.Builder(Color.parseColor(mColor))
                .setRange(-1, goal, 0)
                .setInitialVisibility(false)
                .build();

        seriesItem.addArcSeriesItemListener(new SeriesItem.SeriesItemListener() {
            @Override
            public void onSeriesItemAnimationProgress(float percentComplete, float currentPosition) {

                stepsOfGoal.setText(String.format("%.0f / %.0f", memberSteps, goal));
            }

            @Override
            public void onSeriesItemDisplayProgress(float percentComplete) {

            }
        });

        mSeries1Index = mDecoView.addSeries(seriesItem);


    }

    private void createEvents() {
        mDecoView.executeReset();

        mDecoView.addEvent(
                new DecoEvent.Builder(2000f)
                        .setIndex(mBackIndex)
                        .setDuration(1000)
                        .setDelay(100)
                        .build()
        );

        mDecoView.addEvent(
                new DecoEvent.Builder(DecoDrawEffect.EffectType.EFFECT_SPIRAL_OUT)
                        .setIndex(mSeries1Index)
                        .setDuration(1000)
                        .setDelay(300)
                        .build()
        );

        mDecoView.addEvent(
                new DecoEvent.Builder(memberSteps)
                        .setIndex(mSeries1Index)
                        .setDuration(1000)
                        .setDelay(100)
                        .build()
        );

    }

    private void refreshEvents() {

        mDecoView.addEvent(
                new DecoEvent.Builder(memberSteps)
                        .setIndex(mSeries1Index)
                        .setDuration(1000)
                        .setDelay(100)
                        .build()
        );
    }
}
