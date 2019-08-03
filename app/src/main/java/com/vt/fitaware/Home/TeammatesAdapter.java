package com.vt.fitaware.Home;

import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.vt.fitaware.R;
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
    private int mBackIndex = 0;
    private int mSeries1Index = 0;

    private int tab = -1;

    private float memberSteps = 0;
    private float goal = 0;
    private float memberDuration = 0;
    private float memberHeartPoints = 0;

    private String mColor ="#000000";

    private StorageReference mStorageRef;


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
            holder.stepsOfGoal = (TextView) row.findViewById(R.id.stepsOfGoal);
            holder.mDecoView = row.findViewById(R.id.dynamicArcViews);

            holder.image = (ImageView) row.findViewById(R.id.imageView_userIcon);
            row.setTag(holder);
        } else {
            holder = (TeammatesAdapter.ViewHolder) row.getTag();
        }


        Teammates item = data.get(position);
        mColor = item.getColor();
        tab = Integer.valueOf(item.getTab());
        memberSteps = Float.valueOf(item.getSteps());
        goal = Float.valueOf(item.getGoal());
        memberDuration = Float.valueOf(item.getDuration());
        memberHeartPoints = Float.valueOf(item.getHeartPoints());

        holder.rankOfTeammates.setText("No. "+item.getRank());
        holder.userID.setText(item.getName());

        mStorageRef = FirebaseStorage.getInstance().getReference();

        StorageReference iconRef = mStorageRef.child("user_icon/" +item.getName() + "/icon.jpg");

        iconRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadIconUrl = task.getResult();

                    Picasso.get().load(downloadIconUrl).into(holder.image);

                } else {
                    Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.shuail8);
                    bitmap = getCroppedBitmap(bitmap);
                    holder.image.setImageBitmap(bitmap);
                }
            }
        });




        if(tab == 0) {
            holder.stepsOfGoal.setText(String.format("%.0f / %.0f", memberDuration, 60F));
            createBackSeries(holder.mDecoView, 60F);
            createDataSeries(holder.mDecoView, 60F);
            createEvents(holder.mDecoView, 60F, memberDuration);

        }

        else if (tab == 2) {
            holder.stepsOfGoal.setText(String.format("%.0f / %.0f", memberHeartPoints, 10F));
            createBackSeries(holder.mDecoView, 10F);
            createDataSeries(holder.mDecoView, 10F);
            createEvents(holder.mDecoView, 10F, memberHeartPoints);

        }
        else{
            holder.stepsOfGoal.setText(String.format("%.0f / %.0f", memberSteps, goal));
            createBackSeries(holder.mDecoView, goal);
            createDataSeries(holder.mDecoView, goal);
            createEvents(holder.mDecoView, goal, memberSteps);

        }


        return row;
    }

    class ViewHolder {
        TextView rankOfTeammates;
        TextView userID;
        TextView stepsOfGoal;

        ImageView image;
        DecoView mDecoView;
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