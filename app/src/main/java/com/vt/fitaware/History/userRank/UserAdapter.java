package com.vt.fitaware.History.userRank;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.vt.fitaware.FirebaseMessagingService.MySingleton;
import com.vt.fitaware.MainActivity;
import com.vt.fitaware.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

public class UserAdapter extends ArrayAdapter<UserDetail> {

    private static final String TAG = "UserAdapter";


    private DatabaseReference database;
    private SharedPreferences mSharedPreferences;

    private StorageReference mStorageRef;

    private int layoutResourceId;
    private Context context;
    private ArrayList<UserDetail> data = new ArrayList<UserDetail>();

    private String user_id ="none";
    private String token ="none";

    String NOTIFICATION_TITLE;
    String NOTIFICATION_MESSAGE;
    String TOPIC;

    final private String FCM_API = "https://fcm.googleapis.com/fcm/send";
    final private String serverKey = "key=" + "AAAAHI365o4:APA91bGxezHjOHv41CX8F78ItUHCkV7d3Xt0gXOGlq0SGpS75VYBo_JlqlVmg3EgSuy1sDQxbxodtbyX4ulI5oydWcrkqJasEnrFzaBKsjnprqsed-d9mczv1EO-1OUYwgDDV47s7h-1";
    final private String contentType = "application/json";

    public UserAdapter(Context context, int layoutResourceId, ArrayList<UserDetail> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        UserAdapter.ViewHolder holder;
        initSharedPreferences();


        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new UserAdapter.ViewHolder();
            holder.mName = row.findViewById(R.id.user_name);
            holder.mRank = row.findViewById(R.id.user_rank);
            holder.mSteps = row.findViewById(R.id.user_steps);
            holder.mLikes = row.findViewById(R.id.user_likes);
            holder.image = row.findViewById(R.id.imageView_user);
            holder.imageButton_like = row.findViewById(R.id.imageButton_like);


            row.setTag(holder);
        } else {
            holder = (UserAdapter.ViewHolder) row.getTag();
        }


        UserDetail item = data.get(position);

        Log.i(TAG, "memberSteps: "+ item.getmSteps());

        database = FirebaseDatabase.getInstance().getReference();


        holder.mName.setText(item.getmName());
        holder.mRank.setText(item.getmRank());
        holder.mSteps.setText(String.valueOf(item.getmSteps()));
        holder.mLikes.setText(item.getmLikes());

        user_id = mSharedPreferences.getString("user_id", "none");

        if(item.getmChecked() == "checked") {
            holder.imageButton_like.setImageResource(R.drawable.ic_like_red);

        }
        else {
            holder.imageButton_like.setImageResource(R.drawable.ic_like);
        }

        Log.e(TAG, "getmToken: " + item.getmToken() );

        token = mSharedPreferences.getString("token", "none");



        holder.imageButton_like.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                if(item.getmChecked() == "unchecked") {

                    writePost(item.getmName(), item.getmDate(), user_id);

                    if(!item.getmToken().equals("none") && !item.getmToken().equals(token)) {
                        TOPIC = item.getmToken(); //topic must match with what the receiver subscribed to
                    }

                    NOTIFICATION_TITLE = "FitAware";
                    NOTIFICATION_MESSAGE = user_id + " liked you steps progress!";

                    JSONObject notification = new JSONObject();
                    JSONObject notifcationBody = new JSONObject();
                    try {
                        notifcationBody.put("title", NOTIFICATION_TITLE);
                        notifcationBody.put("message", NOTIFICATION_MESSAGE);

                        notification.put("to", TOPIC);
                        notification.put("data", notifcationBody);
                    } catch (JSONException e) {
                        Log.e(TAG, "onCreate: " + e.getMessage() );
                    }
                    sendNotification(notification);

                }
                else {
                    database.child("/DailyRecord/"+item.getmName()+"/"+item.getmDate()+"/Likes/"+user_id).removeValue();

                }


            }
        });


        mStorageRef = FirebaseStorage.getInstance().getReference();

        StorageReference iconRef = mStorageRef.child("user_icon/" + item.getmName() + "/icon.jpg");

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


        return row;
    }

    class ViewHolder {
        TextView mName;
        TextView mRank;
        TextView mSteps;
        TextView mLikes;

        ImageView image;
        ImageButton imageButton_like;
    }

    private void writePost(String id, String date, String userId) {
        HashMap<String, Object> childUpdates = new HashMap<>();

        String update = "/DailyRecord/" + id + "/" + date + "/Likes/" + userId;
        Log.w(TAG, "update Likes: "+update);

        childUpdates.put(update, userId);

        Log.w(TAG, "childUpdates Likes: "+childUpdates);

        database.updateChildren(childUpdates);
    }

    private void sendNotification(JSONObject notification) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(FCM_API, notification,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "onResponse: " + response.toString());
                        NOTIFICATION_TITLE = "";
                        NOTIFICATION_MESSAGE = "";
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
//                        Toast.makeText(getContext(), "Request error", Toast.LENGTH_LONG).show();
                        Log.i(TAG, "onErrorResponse: Didn't work");
                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", serverKey);
                params.put("Content-Type", contentType);
                return params;
            }
        };
        MySingleton.getInstance(getContext().getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }

    private void initSharedPreferences() {

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
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
