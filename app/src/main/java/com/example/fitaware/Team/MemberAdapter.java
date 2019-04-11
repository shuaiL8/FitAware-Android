package com.example.fitaware.Team;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
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

public class MemberAdapter extends ArrayAdapter<Member> {

    private DecoView mDecoView;
    private int mBackIndex = 0;
    private int mSeries1Index = 0;

    private float memberSteps = 0;

    private String mColor;

    private Context context;
    private int layoutResourceId;
    private ArrayList<Member> data = new ArrayList<Member>();

    public MemberAdapter(Context context, int layoutResourceId, ArrayList<Member> data) {
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
            holder.mRank = row.findViewById(R.id.textRank);
            holder.mName = row.findViewById(R.id.memberName);
            holder.mSteps = row.findViewById(R.id.memberSteps);
            mDecoView = row.findViewById(R.id.dynamicArcViewRank);

            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }


        Member item = data.get(position);

        mColor = item.getmColor();
        memberSteps = Float.valueOf(item.getmSteps());

        holder.mRank.setText(item.getmRank());
        holder.mName.setText(item.getmName());
        holder.mSteps.setText(item.getmSteps());

        holder.mRank.setTextColor(Color.parseColor(mColor));
        holder.mName.setTextColor(Color.parseColor(mColor));
        holder.mSteps.setTextColor(Color.parseColor(mColor));


        createBackSeries();
        createDataSeries();
        createEvents();

        return row;
    }

    class ViewHolder {
        TextView mRank;
        TextView mName;
        TextView mSteps;
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
                .setRange(0, 2000f, 0)
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
                new DecoEvent.Builder(memberSteps)
                        .setIndex(mSeries1Index)
                        .setDuration(1000)
                        .setDelay(100)
                        .build()
        );

    }
}
