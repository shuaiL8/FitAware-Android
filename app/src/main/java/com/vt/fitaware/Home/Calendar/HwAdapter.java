package com.vt.fitaware.Home.Calendar;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.navigation.Navigation;
import com.vt.fitaware.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

class HwAdapter extends BaseAdapter {
    private Activity context;
    private static final String TAG = "HwAdapter";

    private Calendar month;
    public GregorianCalendar pmonth;


    /**
     * calendar instance for previous month for getting complete view
     */
    public GregorianCalendar pmonthmaxset;
    private GregorianCalendar selectedDate;
    int firstDay;
    int maxWeeknumber;
    int maxP;
    int calMaxP;
    int mnthlength;
    String itemvalue, curentDateString;
    DateFormat df;
    int cDay, cMonth, vMonth;

    private ArrayList<String> items;
    public static List<String> day_string;
    public ArrayList<HomeCollection> date_collection_arr;
    private String gridvalue;
    private TextView tv_date;
    private ListView listTeachers;
    private ArrayList<Dialogpojo> alCustom=new ArrayList<Dialogpojo>();


    public HwAdapter(Activity context, GregorianCalendar monthCalendar, ArrayList<HomeCollection> date_collection_arr) {
        this.date_collection_arr=date_collection_arr;
        HwAdapter.day_string = new ArrayList<String>();
        Locale.setDefault(Locale.US);
        month = monthCalendar;
        selectedDate = (GregorianCalendar) monthCalendar.clone();
        this.context = context;
        month.set(GregorianCalendar.DAY_OF_MONTH, 1);


        this.items = new ArrayList<String>();
        df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        curentDateString = df.format(selectedDate.getTime());
        refreshDays();

    }

    public int getCount() {
        return day_string.size();
    }

    public Object getItem(int position) {
        return day_string.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new view for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        TextView dayView, tv_month;
        if (convertView == null) { // if it's not recycled, initialize some
            // attributes
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.cal_item, null);

        }

        Calendar calander = Calendar.getInstance();
        cDay = calander.get(Calendar.DAY_OF_MONTH);
        cMonth = calander.get(Calendar.MONTH) + 1;
        vMonth = month.get(Calendar.MONTH) + 1;



        dayView = v.findViewById(R.id.date);

        String[] separatedTime = day_string.get(position).split("-");


        gridvalue = separatedTime[2].replaceFirst("^0*", "");
        if ((Integer.parseInt(gridvalue) > 1) && (position < firstDay)) {
            dayView.setTextColor(Color.parseColor("#A9A9A9"));
            dayView.setClickable(false);
            dayView.setFocusable(false);
        } else if ((Integer.parseInt(gridvalue) < 7) && (position > 28)) {
            dayView.setTextColor(Color.parseColor("#A9A9A9"));
            dayView.setClickable(false);
            dayView.setFocusable(false);
        } else {

            if(Integer.parseInt(gridvalue) == cDay && vMonth == cMonth) {
                dayView.setTextColor(Color.parseColor("#ff1a1a"));
            }
            else {
                dayView.setTextColor(Color.parseColor("#696969"));
            }

        }


        if (day_string.get(position).equals(curentDateString)) {
            v.setBackgroundColor(Color.parseColor("#ffffff"));
        } else {
            v.setBackgroundColor(Color.parseColor("#ffffff"));
        }


        dayView.setText(gridvalue);

        // create date string for comparison
        String date = day_string.get(position);

        if (date.length() == 1) {
            date = "0" + date;
        }
        String monthStr = "" + (month.get(GregorianCalendar.MONTH) + 1);
        if (monthStr.length() == 1) {
            monthStr = "0" + monthStr;
        }

        setEventView(v, position,dayView);

        return v;
    }

    public void refreshDays() {
        // clear items
        items.clear();
        day_string.clear();
        Locale.setDefault(Locale.US);
        pmonth = (GregorianCalendar) month.clone();
        // month start day. ie; sun, mon, etc
        firstDay = month.get(GregorianCalendar.DAY_OF_WEEK);
        // finding number of weeks in current month.
        maxWeeknumber = month.getActualMaximum(GregorianCalendar.WEEK_OF_MONTH);
        // allocating maximum row number for the gridview.
        mnthlength = maxWeeknumber * 7;
        maxP = getMaxP(); // previous month maximum day 31,30....
        calMaxP = maxP - (firstDay - 1);// calendar offday starting 24,25 ...
        pmonthmaxset = (GregorianCalendar) pmonth.clone();

        pmonthmaxset.set(GregorianCalendar.DAY_OF_MONTH, calMaxP + 1);


        for (int n = 0; n < mnthlength; n++) {

            itemvalue = df.format(pmonthmaxset.getTime());
            pmonthmaxset.add(GregorianCalendar.DATE, 1);
            day_string.add(itemvalue);

        }
    }

    private int getMaxP() {
        int maxP;
        if (month.get(GregorianCalendar.MONTH) == month
                .getActualMinimum(GregorianCalendar.MONTH)) {
            pmonth.set((month.get(GregorianCalendar.YEAR) - 1),
                    month.getActualMaximum(GregorianCalendar.MONTH), 1);
        } else {
            pmonth.set(GregorianCalendar.MONTH,
                    month.get(GregorianCalendar.MONTH) - 1);
        }
        maxP = pmonth.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);

        return maxP;
    }




    public void setEventView(View v, int pos, TextView txt){

        int len=date_collection_arr.size();
        for (int i = 0; i < len; i++) {
            HomeCollection cal_obj=date_collection_arr.get(i);
            String date=cal_obj.date;
            int len1=day_string.size();
            if (len1>pos) {

                if (day_string.get(pos).equals(date)) {
                    if ((Integer.parseInt(gridvalue) > 1) && (pos < firstDay)) {

                    } else if ((Integer.parseInt(gridvalue) < 7) && (pos > 28)) {

                    } else {
                        v.setBackgroundColor(Color.parseColor("#343434"));

                        for (int j=0; j<len; j++){
                            if (date_collection_arr.get(j).date.equals(date)){

                                if(date_collection_arr.get(j).steps != null && date_collection_arr.get(j).goal != null) {
                                    float strSteps = Integer.valueOf(date_collection_arr.get(j).steps);
                                    float  strGoal = Integer.valueOf(date_collection_arr.get(j).goal);

                                    float strPercentage = strSteps/strGoal;

                                    if(strPercentage < 0.5) {
                                        v.setBackgroundResource(R.drawable.rounded_calender);
                                    }
                                    else if(strPercentage > 0.5){
                                        v.setBackgroundResource(R.drawable.rounded_calender_green);
                                    }
                                    else {
                                        v.setBackgroundResource(R.drawable.rounded_calender_orange);
                                    }
                                }

                            }
                        }

                        if(Integer.parseInt(gridvalue) == cDay && vMonth == cMonth) {
                            txt.setTextColor(Color.parseColor("#ff1a1a"));
                        }
                        else {
                            txt.setTextColor(Color.parseColor("#696969"));
                        }
                    }

                }
            }}
    }

    public void getPositionList(final String date, final Activity act){

        final int len= date_collection_arr.size();
        JSONArray jbarrays=new JSONArray();
        for (int j=0; j<len; j++){
            if (date_collection_arr.get(j).date.equals(date)){
                HashMap<String, String> maplist = new HashMap<String, String>();
                maplist.put("duration",date_collection_arr.get(j).duration);
                maplist.put("steps",date_collection_arr.get(j).steps);
                maplist.put("heartPoints",date_collection_arr.get(j).heartPoints);
                maplist.put("distance",date_collection_arr.get(j).distance);
                maplist.put("cals",date_collection_arr.get(j).cals);
                maplist.put("goal",date_collection_arr.get(j).goal);

                JSONObject json1 = new JSONObject(maplist);
                jbarrays.put(json1);
            }
        }
        if (jbarrays.length()!=0) {
            final Dialog dialogs = new Dialog(context);
            dialogs.setContentView(R.layout.dialog_inform);

//            WindowManager.LayoutParams wmlp = dialogs.getWindow().getAttributes();
//            wmlp.gravity = Gravity.TOP | Gravity.LEFT;
//            wmlp.x = 75;   //x position
//            wmlp.y = 1100;   //y position

            tv_date = (TextView) dialogs.findViewById(R.id.tv_date);
            tv_date.setText(date);
            listTeachers = (ListView) dialogs.findViewById(R.id.list_teachers);
            ImageView imgCross = (ImageView) dialogs.findViewById(R.id.img_cross);
            listTeachers.setAdapter(new DialogAdaptorStudent(context, getMatchList(jbarrays + "")));
            listTeachers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                    if (position == 0) {
                        Bundle args = new Bundle();
                        args.putString("date", date);
                        for (int j=0; j<len; j++){
                            if (date_collection_arr.get(j).date.equals(date)){
                                args.putString("duration", date_collection_arr.get(j).duration);
                                args.putString("steps", date_collection_arr.get(j).steps);
                                args.putString("heartPoints", date_collection_arr.get(j).heartPoints);
                                args.putString("distance", date_collection_arr.get(j).distance);
                                args.putString("cals", date_collection_arr.get(j).cals);
                                args.putString("goal", date_collection_arr.get(j).goal);

                            }
                        }

                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                        String team = sharedPreferences.getString("team", "none");
                        Log.i(TAG, "team: " + team);
                        BottomNavigationView bottomNavigationView = context.findViewById(R.id.bottomNavigation);


                        if(!team.equals("none")) {
                            bottomNavigationView.setSelectedItemId(R.id.navigation_history);
                            Navigation.findNavController(context, R.id.my_nav_host_fragment).navigate(R.id.userFragment);
                            dialogs.dismiss();


                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("newSelectedDate", date);

                            editor.commit();
                        }
                        else {
                            bottomNavigationView.setSelectedItemId(R.id.navigation_history);
                            Navigation.findNavController(context, R.id.my_nav_host_fragment).navigate(R.id.historyFragment);
                            dialogs.dismiss();

                        }

                    }
                }
            });
            imgCross.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogs.dismiss();
                }
            });
            dialogs.show();

        }

    }

    private ArrayList<Dialogpojo> getMatchList(String detail) {
        try {
            JSONArray jsonArray = new JSONArray(detail);
            alCustom = new ArrayList<Dialogpojo>();
            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject jsonObject = jsonArray.optJSONObject(i);

                Dialogpojo pojo = new Dialogpojo();

                pojo.setDuration(jsonObject.optString("duration"));
                pojo.setSteps(jsonObject.optString("steps"));
                pojo.setHeartPoints(jsonObject.optString("heartPoints"));
                pojo.setDisatnce(jsonObject.optString("distance"));
                pojo.setCals(jsonObject.optString("cals"));
                pojo.setGoal(jsonObject.optString("goal"));

                alCustom.add(pojo);

            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return alCustom;
    }

}



