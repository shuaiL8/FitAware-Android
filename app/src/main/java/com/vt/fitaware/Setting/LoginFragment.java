package com.vt.fitaware.Setting;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.navigation.Navigation;
import com.vt.fitaware.MainActivity;
import com.vt.fitaware.MyNotificationService;
import com.vt.fitaware.R;
//import com.example.fitaware.network.NetworkUtil;
import com.google.firebase.database.*;
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import retrofit2.adapter.rxjava.HttpException;
//import rx.android.schedulers.AndroidSchedulers;
//import rx.schedulers.Schedulers;
//import rx.subscriptions.CompositeSubscription;
import android.os.Handler;

import java.util.HashMap;
import java.util.Map;

import static com.vt.fitaware.utils.Validation.validateEmail;
import static com.vt.fitaware.utils.Validation.validateFields;


public class LoginFragment extends Fragment {
    public static final String TAG = "LoginFragment";
    private DatabaseReference myRef;

    private EditText mEtEmail;
    private EditText mEtPassword;
    private CheckBox rememberPasswordCheckBox;
    private Button mBtLogin;
    private TextView mTvRegister;
    private TextView mTvForgotPassword;
    private TextInputLayout mTiEmail;
    private TextInputLayout mTiPassword;
    private ProgressBar mProgressBar;

    private String email = "none";
    private String password = "none";
    private Map<String,String> myData;
    //    private CompositeSubscription mSubscriptions;
    private SharedPreferences mSharedPreferences;
    //    public int loginStatus = 0;
    public String user_id = "none";
    public String team = "none";
    public String my_goal = "none";
    public String teamGoal = "none";
    public String captain = "none";
    public String periodical = "none";

    public String user_email = "none";
    public String user_password = "none";
    public String user_checkBox = "none";



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login,container,false);
//        mSubscriptions = new CompositeSubscription();
        initSharedPreferences();

        TextView toolbarTiltle = getActivity().findViewById(R.id.toolbar_title);
                toolbarTiltle.setText("FitAware");

        user_email = mSharedPreferences.getString("user_email", "none");
        user_password = mSharedPreferences.getString("user_password", "none");
        user_checkBox = mSharedPreferences.getString("user_checkBox", "none");

        Log.i(TAG, "user_email: " + user_email);
        Log.i(TAG, "user_password: " + user_password);
        Log.i(TAG, "user_checkBox: " + user_checkBox);

        mEtEmail = (EditText) view.findViewById(R.id.et_email);
        mEtPassword = (EditText) view.findViewById(R.id.et_password);
        rememberPasswordCheckBox = view.findViewById(R.id.rememberPasswordCheckBox);
        mBtLogin = (Button) view.findViewById(R.id.btn_login);
        mTiEmail = (TextInputLayout) view.findViewById(R.id.ti_email);
        mTiPassword = (TextInputLayout) view.findViewById(R.id.ti_password);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress);
        mTvRegister = (TextView) view.findViewById(R.id.tv_register);
        mTvForgotPassword = (TextView) view.findViewById(R.id.tv_forgot_password);


        if(!user_email.equals("none")) {
            mEtEmail.setText(user_email);
        }

        if(user_checkBox.equals("checked")){
            rememberPasswordCheckBox.setChecked(true);
            if(!user_password.equals("none")) {
                mEtPassword.setText(user_password);
            }
        }
        else{
            rememberPasswordCheckBox.setChecked(false);
        }

        mBtLogin.setOnClickListener(V -> login());
        mTvRegister.setOnClickListener(V -> goToRegister());
        mTvForgotPassword.setOnClickListener(V -> showeRsetpasswordDialog());

        rememberPasswordCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(rememberPasswordCheckBox.isChecked()){
                    user_checkBox = "checked";
                }
                else {
                    user_checkBox = "unchecked";
                }
                Log.i(TAG, "onCheckedChanged user_checkBox: " + user_checkBox);

                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putString("user_checkBox", user_checkBox);
                editor.commit();
            }
        });

        return view;
    }


    private void initSharedPreferences() {

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    private void login() {

        setError();

        String inputEmail = mEtEmail.getText().toString();
        String inputPassword = mEtPassword.getText().toString();

        String[] inputId = inputEmail.split("@");
        user_id = inputId[0];

        user_email = inputEmail;
        user_password = inputPassword;

        Log.i(TAG, "inputEmail: " + inputEmail);

        Log.i(TAG, "inputId: " + inputId[0]);

        readData(new MyCallback() {
            @Override
            public void onCallback(String value) {

                value = value.substring(1, value.length()-1);
                String[] keyValuePairs = value.split(",");
                myData = new HashMap<>();

                for(String pair : keyValuePairs) {
                    String[] entry = pair.split("=");
                    myData.put(entry[0].trim(), entry[1].trim());
                }


                email = myData.get("email");
                password = myData.get("password");
                team = myData.get("team");
                my_goal = myData.get("goal");
                teamGoal = myData.get("teamGoal");
                captain = myData.get("captain");
                periodical = myData.get("periodical");

                Log.d("testCallback", value);

            }
        });

        int err = 0;

        if (!validateEmail(inputEmail)) {

            err++;
            mTiEmail.setError("Email should be valid !");
        }

        if (!validateFields(inputPassword)) {

            err++;
            mTiPassword.setError("Password should not be empty !");
        }

        if (err == 0) {
            Handler handler = new Handler();

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loginProcess(inputEmail,inputPassword);

                }
            }, 1000);

            mProgressBar.setVisibility(View.VISIBLE);

        } else {

            showSnackBarMessage("Enter Valid Details !");
        }

        Log.i(TAG, "inputEmail: " + inputEmail);
        Log.i(TAG, "inputPassword: " + inputPassword);
        Log.i(TAG, "email: " + email);
        Log.i(TAG, "password: " + password);

        Log.i(TAG, "err: " + err);
    }

    private void setError() {

        mTiEmail.setError(null);
        mTiPassword.setError(null);
    }

    private void loginProcess(String e, String p) {
        if(e .equals(email)  && p.equals(password) ) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString("user_id", user_id);

            editor.putString("user_email", user_email);
            editor.putString("user_password", user_password);

            editor.putString("team", team);
            editor.putString("my_goal", my_goal);
            editor.putString("team_goal", teamGoal);
            editor.putString("captain", captain);
            editor.putString("periodical", periodical);

            editor.putInt("loginStatus", 1);
            editor.commit();
            goToHome();
        }
        else {
            showSnackBarMessage("Wrong Email or Password !");

        }

    }


    private void goToHome() {

        getActivity().finish();

        Intent intent = new Intent(getActivity().getBaseContext(),
                MainActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        getActivity().startActivity(intent);
    }

    private void showSnackBarMessage(String message) {

        if (getView() != null) {

            Snackbar.make(getView(),message, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void goToRegister(){

        Navigation.findNavController(getActivity(), R.id.my_nav_host_fragment).navigate(R.id.registerFragment);

    }

    private void showeRsetpasswordDialog(){

        ResetpasswordFragment resetpasswordFragment = new ResetpasswordFragment();

        resetpasswordFragment.show(getFragmentManager(), ResetpasswordFragment.TAG);
    }




    public interface MyCallback {
        void onCallback(String value);
    }

    public void readData(MyCallback myCallback) {
        myRef = FirebaseDatabase.getInstance().getReference().child("User/"+user_id);
        Log.w(TAG, "myRef" + myRef);

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                Log.w(TAG, "dataSnapshot" + dataSnapshot);

                if(dataSnapshot.getValue() != null) {
                    String my = dataSnapshot.getValue().toString();
                    myCallback.onCallback(my);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        myRef.addValueEventListener(postListener);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
