package com.vt.fitaware.Team;

import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.DecoDrawEffect;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;
import com.squareup.picasso.Picasso;
import com.vt.fitaware.R;

import java.util.ArrayList;

public class TeamAdapter extends ArrayAdapter<Team> {
    private Context context;
    private int layoutResourceId;
    private ArrayList<Team> data = new ArrayList<Team>();

    private int mBackIndex = 0;
    private int mSeries1Index = 0;

    private String mColor ="#000000";
    private float teamGoal = 0;
    private float teamSteps = 0;


    private StorageReference mStorageRef;


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
            holder.team_steps = (TextView) row.findViewById(R.id.team_steps);
            holder.mDecoView = row.findViewById(R.id.dynamicArcViews);


            row.setTag(holder);
        } else {
            holder = (TeamAdapter.ViewHolder) row.getTag();
        }


        Team item = data.get(position);
        mColor = item.getColor();
        holder.name.setText(item.getName());
        holder.captain.setText(item.getCaptain());
        holder.rank.setText(item.getRank());
        holder.team_steps.setText(String.valueOf(item.getTeamSteps()));
        teamGoal = Float.valueOf(item.getGoal());
        teamSteps = Float.valueOf(item.getTeamSteps());

        mStorageRef = FirebaseStorage.getInstance().getReference();

        StorageReference iconRef = mStorageRef.child("team_icon/" + item.getName() + "/icon.jpg");


        iconRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadIconUrl = task.getResult();

                    Picasso.get().load(downloadIconUrl).into(holder.image);

                } else {
                    Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.teamwork);
                    bitmap = getCroppedBitmap(bitmap);
                    holder.image.setImageBitmap(bitmap);
                }
            }
        });

        createBackSeries(holder.mDecoView, teamGoal);
        createDataSeries(holder.mDecoView, teamGoal);
        createEvents(holder.mDecoView, teamGoal, teamSteps);


        return row;
    }

    class ViewHolder {
        TextView name;
        TextView captain;
        TextView rank;
        TextView team_steps;

        DecoView mDecoView;
        ImageView image;
    }


    private void createBackSeries(DecoView decoView, Float mGoal) {
        SeriesItem seriesItem = new SeriesItem.Builder(Color.parseColor("#FFE2E2E2"))
                .setRange(-1f, mGoal, 0f)
                .setInitialVisibility(true)
                .build();

        mBackIndex = decoView.addSeries(seriesItem);

    }

    private void createDataSeries(DecoView decoView, Float mGoal) {
        SeriesItem seriesItem = new SeriesItem.Builder(Color.parseColor(mColor))
                .setRange(-1f, mGoal, 0f)
                .setInitialVisibility(false)
                .build();


        mSeries1Index = decoView.addSeries(seriesItem);


    }

    private void createEvents(DecoView decoView, Float mGoal, Float mValue) {
        decoView.executeReset();

        decoView.addEvent(
                new DecoEvent.Builder(mGoal)
                        .setIndex(mBackIndex)
                        .setDuration(1000)
                        .setDelay(100)
                        .build()
        );

        decoView.addEvent(
                new DecoEvent.Builder(DecoDrawEffect.EffectType.EFFECT_SPIRAL_OUT)
                        .setIndex(mSeries1Index)
                        .setDuration(1000)
                        .setDelay(300)
                        .build()
        );

        decoView.addEvent(
                new DecoEvent.Builder(mValue)
                        .setIndex(mSeries1Index)
                        .setDuration(1000)
                        .setDelay(100)
                        .build()
        );

    }

    public Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output  = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        int color = 0xff424242;
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Rect rect  = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle((bitmap.getWidth() / 2), (bitmap.getHeight() / 2),
                (bitmap.getWidth() / 2), paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

}
