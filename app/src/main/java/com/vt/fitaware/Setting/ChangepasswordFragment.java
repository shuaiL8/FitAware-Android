package com.vt.fitaware.Setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.vt.fitaware.MainActivity;
import com.vt.fitaware.R;
//import com.example.fitaware.network.NetworkUtil;
import com.vt.fitaware.utils.Constants;
import com.google.firebase.database.*;
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import retrofit2.adapter.rxjava.HttpException;
//import rx.android.schedulers.AndroidSchedulers;
//import rx.schedulers.Schedulers;
//import rx.subscriptions.CompositeSubscription;

import java.util.HashMap;
import java.util.Map;

import static com.vt.fitaware.utils.Validation.validateFields;


public class ChangepasswordFragment extends DialogFragment {
    private DatabaseReference myRef;

    private Map<String,String> myData;
    public String user_id = "none";
    private String password = "none";

    private SharedPreferences mSharedPreferences;
    private DatabaseReference database;

    public interface Listener {

        void onPasswordChanged();
    }

    public static final String TAG = ChangepasswordFragment.class.getSimpleName();

    private EditText mEtOldPassword;
    private EditText mEtNewPassword;
    private Button mBtChangePassword;
    private Button mBtCancel;
    private TextView mTvMessage;
    private TextInputLayout mTiOldPassword;
    private TextInputLayout mTiNewPassword;
    private ProgressBar mProgressBar;

    private String mToken;
    private String mEmail;

    private MainActivity mListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_changepassword,container,false);
        initSharedPreferences();

        database = FirebaseDatabase.getInstance().getReference();


        getData();
        initViews(view);
        return view;
    }

    private void getData() {

        Bundle bundle = getArguments();

        mToken = bundle.getString(Constants.TOKEN);
        mEmail = bundle.getString(Constants.EMAIL);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (MainActivity) context;
    }

    private void initViews(View v) {

        mEtOldPassword = (EditText) v.findViewById(R.id.et_old_password);
        mEtNewPassword = (EditText) v.findViewById(R.id.et_new_password);
        mTiOldPassword = (TextInputLayout) v.findViewById(R.id.ti_old_password);
        mTiNewPassword = (TextInputLayout) v.findViewById(R.id.ti_new_password);
        mTvMessage = (TextView) v.findViewById(R.id.tv_message);
        mBtChangePassword = (Button) v.findViewById(R.id.btn_change_password);
        mBtCancel = (Button) v.findViewById(R.id.btn_cancel);
        mProgressBar = (ProgressBar) v.findViewById(R.id.progress);

        mBtChangePassword.setOnClickListener(view -> changePassword());
        mBtCancel.setOnClickListener(view -> dismiss());
    }

    private void changePassword() {

        setError();

        String oldPassword = mEtOldPassword.getText().toString();
        String newPassword = mEtNewPassword.getText().toString();


        int err = 0;

        if (!validateFields(oldPassword)) {

            err++;
            mTiOldPassword.setError("Password should not be empty !");
        }

        if (!validateFields(newPassword)) {

            err++;
            mTiNewPassword.setError("Password should not be empty !");
        }

        if (err == 0) {



            mProgressBar.setVisibility(View.VISIBLE);
            changePasswordProgress(newPassword,  oldPassword);

        }

        Log.i(TAG, "password: " + password);

        Log.i(TAG, "oldPassword: " + oldPassword);
        Log.i(TAG, "newPassword: " + newPassword);
        Log.i(TAG, "err: " + err);

    }

    private void writeNewPassowrdPost(String id, String newP) {
        HashMap<String, Object> childUpdates = new HashMap<>();

        String update = "/User/" + id + "/password";
        Log.w(TAG, "update newpassword: "+update);

        childUpdates.put(update, newP);

        Log.w(TAG, "childUpdates newpassword: "+childUpdates);

        database.updateChildren(childUpdates);
    }

    private void setError() {

        mTiOldPassword.setError(null);
        mTiNewPassword.setError(null);
    }

    private void changePasswordProgress(String newP, String oldP) {


        if(oldP.equals(password)) {
            Log.i(TAG, "test user_id: " + user_id);

            writeNewPassowrdPost(user_id, newP);

            mProgressBar.setVisibility(View.GONE);
            mListener.onPasswordChanged();
            dismiss();
        }
        else{
            mProgressBar.setVisibility(View.GONE);

            showMessage("Wrong Password!");
        }


    }


    private void showMessage(String message) {

        mTvMessage.setVisibility(View.VISIBLE);
        mTvMessage.setText(message);

    }

    public interface MyCallback {
        void onCallback(String value);
    }

    private void initSharedPreferences() {

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    public void readData(LoginFragment.MyCallback myCallback) {
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
    public void onStart() {
        super.onStart();
        user_id = mSharedPreferences.getString("user_id", "none");

        readData(new LoginFragment.MyCallback() {
            @Override
            public void onCallback(String value) {

                value = value.substring(1, value.length()-1);
                String[] keyValuePairs = value.split(",");
                myData = new HashMap<>();

                for(String pair : keyValuePairs) {
                    String[] entry = pair.split("=");
                    myData.put(entry[0].trim(), entry[1].trim());
                }

                password = myData.get("password");

                Log.d("testCallback", value);

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
