package com.example.fitaware.Setting;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.navigation.Navigation;
import com.example.fitaware.MainActivity;
import com.example.fitaware.R;
import com.example.fitaware.model.Response;
//import com.example.fitaware.network.NetworkUtil;
import com.example.fitaware.utils.Constants;
import com.google.firebase.database.*;
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import retrofit2.adapter.rxjava.HttpException;
//import rx.android.schedulers.AndroidSchedulers;
//import rx.schedulers.Schedulers;
//import rx.subscriptions.CompositeSubscription;
import android.os.Handler;

import java.io.IOException;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

import static com.example.fitaware.utils.Validation.validateEmail;
import static com.example.fitaware.utils.Validation.validateFields;


public class LoginFragment extends Fragment {
    public static final String TAG = LoginFragment.class.getSimpleName();
    private DatabaseReference myRef;

    private EditText mEtEmail;
    private EditText mEtPassword;
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


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login,container,false);
//        mSubscriptions = new CompositeSubscription();
        initViews(view);
        initSharedPreferences();


        return view;
    }

    private void initViews(View v) {

        mEtEmail = (EditText) v.findViewById(R.id.et_email);
        mEtPassword = (EditText) v.findViewById(R.id.et_password);
        mBtLogin = (Button) v.findViewById(R.id.btn_login);
        mTiEmail = (TextInputLayout) v.findViewById(R.id.ti_email);
        mTiPassword = (TextInputLayout) v.findViewById(R.id.ti_password);
        mProgressBar = (ProgressBar) v.findViewById(R.id.progress);
        mTvRegister = (TextView) v.findViewById(R.id.tv_register);
        mTvForgotPassword = (TextView) v.findViewById(R.id.tv_forgot_password);

        mBtLogin.setOnClickListener(view -> login());
        mTvRegister.setOnClickListener(view -> goToRegister());
        mTvForgotPassword.setOnClickListener(view -> showeRsetpasswordDialog());

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
            editor.putString("team", team);
            editor.putString("my_goal", my_goal);
            editor.putString("team_goal", teamGoal);
            editor.putString("captain", captain);

            editor.putInt("loginStatus", 1);
            editor.commit();
            startActivity();
        }
        else {
            showSnackBarMessage("Wrong Email or Password !");

        }

    }


    private void startActivity() {
        Intent i = new Intent(getActivity().getBaseContext(),
                MainActivity.class);

        getActivity().startActivity(i);
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
