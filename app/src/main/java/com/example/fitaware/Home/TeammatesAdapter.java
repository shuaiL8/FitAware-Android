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

    private int tab = -1;

    private float memberSteps = 0;
    private float goal = 0;
    private float memberDuration = 0;
    private float memberHeartPoints = 0;

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
        tab = Integer.valueOf(item.getTab());
        memberSteps = Float.valueOf(item.getSteps());
        goal = Float.valueOf(item.getGoal());
        memberDuration = Float.valueOf(item.getDuration());
        memberHeartPoints = Float.valueOf(item.getHeartPoints());

        holder.rankOfTeammates.setText("No. "+item.getRank());
        holder.userID.setText(item.getName());
        holder.image.setImageBitmap(item.getImage());


        if(tab == 0) {
            stepsOfGoal.setText(String.format("%.0f / %.0f", memberDuration, 60F));
            createBackSeries(60F);
            createDataSeries(60F);
            createEvents(60F, memberDuration);

        }

        else if (tab == 2) {
            stepsOfGoal.setText(String.format("%.0f / %.0f", memberHeartPoints, 10F));
            createBackSeries(10F);
            createDataSeries(10F);
            createEvents(10F, memberHeartPoints);

        }
        else{
            stepsOfGoal.setText(String.format("%.0f / %.0f", memberSteps, goal));
            createBackSeries(goal);
            createDataSeries(goal);
            createEvents(goal, memberSteps);

        }


        return row;
    }

    class ViewHolder {
        TextView rankOfTeammates;
        TextView userID;

        ImageView image;
    }

    private void createBackSeries(Float mGoal) {
        SeriesItem seriesItem = new SeriesItem.Builder(Color.parseColor("#FFE2E2E2"))
                .setRange(-1f, mGoal, 0f)
                .setInitialVisibility(true)
                .build();

        mBackIndex = mDecoView.addSeries(seriesItem);

    }

    private void createDataSeries(Float mGoal) {
        SeriesItem seriesItem = new SeriesItem.Builder(Color.parseColor(mColor))
                .setRange(-1f, mGoal, 0f)
                .setInitialVisibility(false)
                .build();


        mSeries1Index = mDecoView.addSeries(seriesItem);


    }

    private void createEvents(Float mGoal, Float mValue) {
        mDecoView.executeReset();

        mDecoView.addEvent(
                new DecoEvent.Builder(mGoal)
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
                new DecoEvent.Builder(mValue)
                        .setIndex(mSeries1Index)
                        .setDuration(1000)
                        .setDelay(100)
                        .build()
        );

    }

}
