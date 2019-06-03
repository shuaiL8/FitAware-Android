package com.example.fitaware.Home.Calendar;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.fitaware.R;

import java.util.ArrayList;

class DialogAdaptorStudent extends BaseAdapter {
    Activity activity;

    private Activity context;
    private ArrayList<Dialogpojo> alCustom;
    private String sturl;


    public DialogAdaptorStudent(Activity context, ArrayList<Dialogpojo> alCustom) {
        this.context = context;
        this.alCustom = alCustom;

    }

    @Override
    public int getCount() {
        return alCustom.size();

    }

    @Override
    public Object getItem(int i) {
        return alCustom.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View listViewItem = inflater.inflate(R.layout.row_addapt, null, true);

        TextView tvTitle=(TextView)listViewItem.findViewById(R.id.tv_name);
        TextView tvSubject=(TextView)listViewItem.findViewById(R.id.tv_type);
        TextView tvDuedate=(TextView)listViewItem.findViewById(R.id.tv_desc);
        TextView tvDescription=(TextView)listViewItem.findViewById(R.id.tv_class);
        TextView tv_section=(TextView)listViewItem.findViewById(R.id.tv_section);

        tvTitle.setText("Duration : "+alCustom.get(position).getDuration());
        float s = Integer.valueOf(alCustom.get(position).getSteps());
        float g = Integer.valueOf(alCustom.get(position).getGoal());
        float percent = (s/g)*100;
        tvSubject.setText(String.format("Steps: %.0f / %.0f (%.0f%%)", s, g, percent));
        tvDuedate.setText("HeartPoints : "+alCustom.get(position).getHeartPoints());
        tvDescription.setText("Distance : "+alCustom.get(position).getDisatnce());
        tv_section.setText("Calorie : "+alCustom.get(position).getCals());

        return  listViewItem;
    }

}

