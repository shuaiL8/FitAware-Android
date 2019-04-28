package com.example.fitaware.Setting;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.navigation.Navigation;
import com.example.fitaware.MainActivity;
import com.example.fitaware.R;
import com.example.fitaware.model.Response;
import com.example.fitaware.model.User;
import com.google.firebase.database.*;
//import com.example.fitaware.network.NetworkUtil;
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import retrofit2.adapter.rxjava.HttpException;
//import rx.android.schedulers.AndroidSchedulers;
//import rx.schedulers.Schedulers;
//import rx.subscriptions.CompositeSubscription;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.example.fitaware.utils.Validation.validateEmail;
import static com.example.fitaware.utils.Validation.validateFields;


public class RegisterFragment extends Fragment {
    public static final String TAG = RegisterFragment.class.getSimpleName();

    private EditText mEtName;
    private EditText mEtEmail;
    private EditText mEtPassword;
    private EditText mEtStepsGoal;

    private Button   mBtRegister;
    private TextView mTvLogin;
    private TextInputLayout mTiName;
    private TextInputLayout mTiEmail;
    private TextInputLayout mTiPassword;
    private TextInputLayout mTiStepsGoal;
    private Spinner mEtSpinner;

    private ProgressBar mProgressbar;

    private DatabaseReference database;
    public int loginStatus = 0;
    public String user_id = "";


//    private CompositeSubscription mSubscriptions;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_register,container,false);
//        mSubscriptions = new CompositeSubscription();
        initViews(view);

        database = FirebaseDatabase.getInstance().getReference();

        return view;
    }

    private void initViews(View v) {

        mEtName = (EditText) v.findViewById(R.id.et_name);
        mEtEmail = (EditText) v.findViewById(R.id.et_email);
        mEtPassword = (EditText) v.findViewById(R.id.et_password);
        mEtStepsGoal = (EditText) v.findViewById(R.id.et_stepsGoal);

        mBtRegister = (Button) v.findViewById(R.id.btn_register);
        mTvLogin = (TextView) v.findViewById(R.id.tv_login);
        mTiName = (TextInputLayout) v.findViewById(R.id.ti_name);
        mTiEmail = (TextInputLayout) v.findViewById(R.id.ti_email);
        mTiPassword = (TextInputLayout) v.findViewById(R.id.ti_password);
        mTiStepsGoal = (TextInputLayout) v.findViewById(R.id.ti_stepsGoal);
        mProgressbar = (ProgressBar) v.findViewById(R.id.progress);

        mEtSpinner = (Spinner) v.findViewById(R.id.mEtSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.Daily_Weekly_Monthly, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mEtSpinner.setAdapter(adapter);

        mBtRegister.setOnClickListener(view -> register());
        mTvLogin.setOnClickListener(view -> goToLogin());
    }

    private void writeNewPost(String name, String email, String password, String stepsGoal, String captain, String team, String currentSteps, String periodical) {

        User user = new User();

        user.setId(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setGoal(stepsGoal);
        user.setCaptain(captain);
        user.setTeam(team);
        user.setCurrentSteps(currentSteps);
        user.setPeriodical(periodical);

        database.child("User/"+name).setValue(user);

        loginStatus = 1;
        sendData();

    }

    private void register() {

        setError();

        String inputName = mEtName.getText().toString();
        String inputEmail = mEtEmail.getText().toString();
        String inputPassword = mEtPassword.getText().toString();
        String inputGoal = mEtStepsGoal.getText().toString();
        String inputPeriodical = mEtSpinner.getSelectedItem().toString();


        int err = 0;

        if (!validateFields(inputName)) {

            err++;
            mTiName.setError("Name should not be empty !");
        }

        if (!validateEmail(inputEmail)) {

            err++;
            mTiEmail.setError("Email should be valid !");
        }

        if (!validateFields(inputPassword)) {

            err++;
            mTiPassword.setError("Password should not be empty !");
        }

        if (!validateFields(inputGoal)) {

            err++;
            mTiStepsGoal.setError("Goal should not be empty !");
        }


        if (err == 0) {

//            User user = new User();
//            user.setName(name);
//            user.setEmail(email);
//            user.setPassword(password);

            mProgressbar.setVisibility(View.VISIBLE);
            user_id = inputName;
            writeNewPost(inputName, inputEmail, inputPassword, inputGoal, "none", "none", "0", inputPeriodical);

        } else {

            showSnackBarMessage("Enter Valid Details !");
        }

        Log.i(TAG, "inputEmail: " + inputEmail);
        Log.i(TAG, "inputPassword: " + inputPassword);
    }

    private void sendData()
    {
        //INTENT OBJ
        Intent i = new Intent(getActivity().getBaseContext(),
                MainActivity.class);

        //PACK DATA
        i.putExtra("Login_Status", loginStatus);
        i.putExtra("user_id", user_id);
        Log.i(TAG, "putExtra user_id: " + user_id);

        //START ACTIVITY
        getActivity().startActivity(i);
    }

    private void setError() {

        mTiName.setError(null);
        mTiEmail.setError(null);
        mTiPassword.setError(null);
    }

    private void registerProcess(User user) {

//        mSubscriptions.add(NetworkUtil.getRetrofit().register(user)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeOn(Schedulers.io())
//                .subscribe(this::handleResponse,this::handleError));
    }

    private void handleResponse(Response response) {

        mProgressbar.setVisibility(View.GONE);
        showSnackBarMessage(response.getMessage());
    }

    private void handleError(Throwable error) {

        mProgressbar.setVisibility(View.GONE);

//        if (error instanceof HttpException) {
//
//            Gson gson = new GsonBuilder().create();
//
//            try {
//
//                String errorBody = ((HttpException) error).response().errorBody().string();
//                Response response = gson.fromJson(errorBody,Response.class);
//                showSnackBarMessage(response.getMessage());
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        } else {
//
//            showSnackBarMessage("Network Error !");
//        }
    }

    private void showSnackBarMessage(String message) {

        if (getView() != null) {

            Snackbar.make(getView(),message, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void goToLogin(){

        Navigation.findNavController(getActivity(), R.id.my_nav_host_fragment).navigate(R.id.loginFragment);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        mSubscriptions.unsubscribe();
    }
}
