package com.vt.fitaware.Setting;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.vt.fitaware.MyNotificationService;
import com.vt.fitaware.R;

import java.util.ArrayList;

public class SettingAdaptor extends ArrayAdapter<Settings> {
    private static final String TAG = "SettingAdaptor";

    private SharedPreferences mSharedPreferences;

    private int loginStatus = 1;
    private String user_id = "none";
    private String team = "none";

    private Context context;
    private int layoutResourceId;
    private ArrayList<Settings> data = new ArrayList<Settings>();

    public SettingAdaptor(Context context, int layoutResourceId, ArrayList<Settings> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder;
        initSharedPreferences();
        loginStatus = mSharedPreferences.getInt("loginStatus", 0);
        user_id = mSharedPreferences.getString("user_id", "none");
        team = mSharedPreferences.getString("team", "none");

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.sName = row.findViewById(R.id.sName);
            holder.sSwitch = row.findViewById(R.id.sSwitch);

            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }
        Settings item = data.get(position);


        holder.sName.setText(item.getsName());
        if(item.getAddSwitch() == true) {
            holder.sSwitch.setVisibility(View.VISIBLE);

            if(item.getsSwitch() == true) {
                holder.sSwitch.setChecked(true);
            }
            else {
                holder.sSwitch.setChecked(false);
            }
        }
        else {
            holder.sSwitch.setVisibility(View.INVISIBLE);
        }

        holder.sSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putString("MyNotificationServiceStatus", "startMyNotificationService");
                    editor.commit();
                    startMyNotificationService();
                    Log.i(TAG, "myNotificationServiceStatus: startMyNotificationService");

                }
                else {
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putString("MyNotificationServiceStatus", "stopMyNotificationService");
                    editor.commit();
                    stopMyNotificationService();
                    Log.i(TAG, "myNotificationServiceStatus: stopMyNotificationService");

                }
            }
        });



        return row;
    }

    class ViewHolder {
        TextView sName;

        Switch sSwitch;
    }

    // register MyNotificationService
    private void startMyNotificationService() {
        Intent intent = new Intent(context, MyNotificationService.class);

        intent.putExtra("user_id", user_id);
        intent.putExtra("team", team);

        if(loginStatus == 1){
            if (!isServiceRunning(MyNotificationService.class)) {
                // Start the service
                if (Build.VERSION.SDK_INT >= 26) {
                    context.startForegroundService(intent);
                } else {
                    context.startService(intent);
                }
                Log.i(TAG, "Start MyNotificationService.");
            } else {
                Log.i(TAG, "MyNotificationService already running.");
            }
        }

    }

    private void stopMyNotificationService(){

        Intent intent = new Intent(context, MyNotificationService.class);

        intent.putExtra("user_id", "none");
        intent.putExtra("team", "none");

        context.stopService(intent);
        Log.i(TAG, "Stop MyNotificationService.");

    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void initSharedPreferences() {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

}
