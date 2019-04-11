package com.example.fitaware.Team;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.fitaware.R;

import java.util.ArrayList;

public class TeamAdapter extends ArrayAdapter<Team> {
    private Context context;
    private int layoutResourceId;
    private ArrayList<Team> data = new ArrayList<Team>();

    public TeamAdapter(Context context, int layoutResourceId, ArrayList<Team> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        TeamAdapter.ViewHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new TeamAdapter.ViewHolder();
            holder.name = (TextView) row.findViewById(R.id.team_name);
            holder.captain = (TextView) row.findViewById(R.id.team_captain);
            holder.image = (ImageView) row.findViewById(R.id.imageView_teams);
            holder.rank = (TextView) row.findViewById(R.id.team_rank);

            row.setTag(holder);
        } else {
            holder = (TeamAdapter.ViewHolder) row.getTag();
        }


        Team item = data.get(position);
        holder.name.setText(item.getName());
        holder.captain.setText(item.getCaptain());
        holder.rank.setText(item.getRank());

        holder.image.setImageBitmap(item.getImage());
        return row;
    }

    class ViewHolder {
        TextView name;
        TextView captain;
        TextView rank;

        ImageView image;
    }

}
