package com.example.fitaware.History;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.example.fitaware.R;
import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.DecoDrawEffect;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;

import java.util.ArrayList;

/**
 * Created by fredliu on 12/3/17.
 */

public class HistoryAdapter extends ArrayAdapter<Histories> {
    private static final String TAG = "HistoryAdapter";

    private DecoView mDecoView;
    private int mBackIndex = 0;
    private int mSeries1Index = 0;

    private float his_steps = 0;
    private float his_goal = 0;


    private Context context;
    private int layoutResourceId;
    private ArrayList<Histories> data = new ArrayList<Histories>();

    public HistoryAdapter(Context context, int layoutResourceId, ArrayList<Histories> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.mDate = row.findViewById(R.id.textDate);
            holder.mRank = row.findViewById(R.id.textRank);
            holder.mSteps = row.findViewById(R.id.his_Steps);
            holder.mDuration = row.findViewById(R.id.his_duration);
            holder.mHeatPoints = row.findViewById(R.id.his_heartPoints);
            holder.mDistance = row.findViewById(R.id.his_distance);
            holder.mCalories = row.findViewById(R.id.his_calories);

            mDecoView = row.findViewById(R.id.dynamicArcViewRank);

            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }


        Histories item = data.get(position);

        Log.i(TAG, "memberSteps: "+ item.getmSteps());

        his_steps = Float.valueOf(item.getmSteps());
        his_goal = Float.valueOf(item.getmGoal());


        holder.mDate.setText(item.getmDate());
        holder.mRank.setText(item.getmRank());
        holder.mSteps.setText(item.getmSteps());
        holder.mDuration.setText(item.getmDuration());
        holder.mHeatPoints.setText(item.getmHeartPoints());
        holder.mDistance.setText(item.getmDistance());
        holder.mCalories.setText(item.getmCalories());

        createBackSeries();
        createDataSeries();
        createEvents();

        return row;
    }

    class ViewHolder {
        TextView mDate;
        TextView mRank;
        TextView mSteps;
        TextView mDuration;
        TextView mHeatPoints;
        TextView mDistance;
        TextView mCalories;

    }

    private void createBackSeries() {
        SeriesItem seriesItem = new SeriesItem.Builder(Color.parseColor("#FFE2E2E2"))
                .setRange(0, 2000f, 0)
                .setInitialVisibility(true)
                .build();

        mBackIndex = mDecoView.addSeries(seriesItem);

    }

    private void createDataSeries() {
        SeriesItem seriesItem = new SeriesItem.Builder(Color.parseColor("#3ebfab"))
                .setRange(-1, his_goal, 0)
                .setInitialVisibility(false)
                .build();

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
                new DecoEvent.Builder(his_steps)
                        .setIndex(mSeries1Index)
                        .setDuration(1000)
                        .setDelay(100)
                        .build()
        );

    }
}
