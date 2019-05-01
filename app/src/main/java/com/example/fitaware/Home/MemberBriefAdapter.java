package com.example.fitaware.Home;

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
import com.example.fitaware.Team.Member;

import java.util.ArrayList;

public class MemberBriefAdapter extends ArrayAdapter<Member> {
    private static final String TAG = "MemberBriefAdapter";
    private Context context;
    private int layoutResourceId;
    private ArrayList<Member> data = new ArrayList<Member>();

    private String mColor;
    private float memberSteps = 0;
    private float goal = 0;

    public MemberBriefAdapter(Context context, int layoutResourceId, ArrayList<Member> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        MemberBriefAdapter.ViewHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new MemberBriefAdapter.ViewHolder();
            holder.mRank = row.findViewById(R.id.textRank);
            holder.mName = row.findViewById(R.id.memberName);
            holder.mSteps = row.findViewById(R.id.memberSteps);

            row.setTag(holder);
        } else {
            holder = (MemberBriefAdapter.ViewHolder) row.getTag();
        }


        Member item = data.get(position);

        Log.i(TAG, "memberSteps: "+ item.getmSteps());
        Log.i(TAG, "goal: "+ item.getmGoal());

        mColor = item.getmColor();
        if(item.getmGoal() != "") {
            memberSteps = Float.valueOf(item.getmSteps());
            goal = Float.valueOf(item.getmGoal());
        }

        if(item.getmName() == "Display None") {
            holder.mRank.setText("");
            holder.mSteps.setText("");
            holder.mName.setText("Display None");

        }
        else {
            holder.mRank.setText("");
            holder.mName.setText(item.getmName());
            holder.mSteps.setText(Integer.toString(item.getmSteps()));
        }


        holder.mRank.setTextColor(Color.parseColor(mColor));
        holder.mName.setTextColor(Color.parseColor(mColor));
        holder.mSteps.setTextColor(Color.parseColor(mColor));


        return row;
    }

    class ViewHolder {
        TextView mRank;
        TextView mName;
        TextView mSteps;
    }

}
