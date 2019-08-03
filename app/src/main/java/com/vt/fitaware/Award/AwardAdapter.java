package com.vt.fitaware.Award;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.vt.fitaware.R;
import android.widget.ArrayAdapter;
import java.util.ArrayList;

public class AwardAdapter extends ArrayAdapter<Award>{

    private Context context;
    private int layoutResourceId;
    private ArrayList<Award> data = new ArrayList<Award>();

    public AwardAdapter(Context context, int layoutResourceId, ArrayList<Award> data) {
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
            holder.prize = (TextView) row.findViewById(R.id.award_prize);
            holder.name = (TextView) row.findViewById(R.id.award_name);
            holder.steps = (TextView) row.findViewById(R.id.award_steps);
            holder.date = (TextView) row.findViewById(R.id.award_date);
            holder.image = (ImageView) row.findViewById(R.id.imageView_awards);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }


        Award item = data.get(position);
        holder.prize.setText(item.getPrize());
        holder.name.setText(item.getName());
        holder.steps.setText(item.getSteps() + " steps");
        holder.date.setText(item.getDate());

        holder.image.setImageBitmap(item.getImage());
        return row;
    }

    class ViewHolder {
        TextView prize;
        TextView name;
        TextView steps;
        TextView date;

        ImageView image;
    }
}
