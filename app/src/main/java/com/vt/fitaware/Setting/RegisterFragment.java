package com.vt.fitaware.Setting;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.vt.fitaware.MainActivity;
import com.vt.fitaware.R;
import com.vt.fitaware.model.User;
import com.google.firebase.database.*;
//import com.example.fitaware.network.NetworkUtil;
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import retrofit2.adapter.rxjava.HttpException;
//import rx.android.schedulers.AndroidSchedulers;
//import rx.schedulers.Schedulers;
//import rx.subscriptions.CompositeSubscription;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

import static com.vt.fitaware.utils.Validation.validateEmail;
import static com.vt.fitaware.utils.Validation.validateFields;


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

    private StorageReference mStorageRef;


    private ProgressBar mProgressbar;
    private ImageView icon;
    private TextView setIcon;

    private SharedPreferences mSharedPreferences;

    private DatabaseReference database;

    private Bitmap bitmap = null;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_register,container,false);
        initViews(view);
        initSharedPreferences();

        TextView toolbarTiltle = getActivity().findViewById(R.id.toolbar_title);
        toolbarTiltle.setText("FitAware");

        database = FirebaseDatabase.getInstance().getReference();

        mStorageRef = FirebaseStorage.getInstance().getReference();

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
        icon = (ImageView) v.findViewById(R.id.icon);
        setIcon = (TextView) v.findViewById(R.id.tv_setIcon);


        bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.shuail8);
        bitmap = getCroppedBitmap(bitmap);

        icon.setImageBitmap(bitmap);

        setIcon.setOnClickListener(view -> setImage());

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

    private void setImage() {
        Intent pickPhoto = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        );
        startActivityForResult(pickPhoto, 0);
    }

    private void writeNewPost(String name, String email, String password, String stepsGoal, String captain, String team, String currentSteps, String periodical, String heartPoints, String duration, String distance, String calories, String teamGoal, String teamSteps) {

        User user = new User();

        user.setId(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setGoal(stepsGoal);
        user.setCaptain(captain);
        user.setTeam(team);
        user.setCurrentSteps(currentSteps);
        user.setHeartPoints(heartPoints);
        user.setDuration(duration);
        user.setDistance(distance);
        user.setCalories(calories);
        user.setTeamGoal(teamGoal);
        user.setTeamSteps(teamSteps);
        user.setPeriodical(periodical);

        String[] emailId = email.split("@");

        database.child("User/"+emailId[0].toLowerCase()).setValue(user);
    }


    private void initDaily(String id, String date, String Cals, String Goal, String HPs, String Minis, String Ms, String Rank, String Steps, String Token) {

        HashMap<String, Object> childUpdates = new HashMap<>();

        childUpdates.put("/DailyRecord/" + id + "/" + date + "/Cals", Cals);
        childUpdates.put("/DailyRecord/" + id + "/" + date + "/Goal", Goal);
        childUpdates.put("/DailyRecord/" + id + "/" + date + "/HPs", HPs);
        childUpdates.put("/DailyRecord/" + id + "/" + date + "/Minis", Minis);
        childUpdates.put("/DailyRecord/" + id + "/" + date + "/Ms", Ms);
        childUpdates.put("/DailyRecord/" + id + "/" + date + "/Rank", Rank);
        childUpdates.put("/DailyRecord/" + id + "/" + date + "/Steps", Steps);
        childUpdates.put("/DailyRecord/" + id + "/" + date + "/Token", Token);

        Log.w(TAG, "childUpdates: $childUpdates");

        FirebaseDatabase.getInstance().getReference().updateChildren(childUpdates);

    }

    private void initSharedPreferences() {

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    private void register() {

        setError();

        String inputName = mEtName.getText().toString().toLowerCase();
        String inputEmail = mEtEmail.getText().toString().toLowerCase();
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

            Handler handler = new Handler();

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    registerProcess(inputName, inputEmail, inputPassword, inputGoal, inputPeriodical);

                }
            }, 1000);

            mProgressbar.setVisibility(View.VISIBLE);

        } else {

            showSnackBarMessage("Enter Valid Details !");
        }

        Log.i(TAG, "inputEmail: " + inputEmail);
        Log.i(TAG, "inputPassword: " + inputPassword);
    }


    private void registerProcess(String inName, String inEmail, String  inPassword, String inGoal, String inPeriodical) {

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("yyyy-MM-dd");
        mdformat.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        String strDate = mdformat.format(calendar.getTime());
        SharedPreferences.Editor editor = mSharedPreferences.edit();


        String[] emailId = inEmail.split("@");

        editor.putString("user_id", emailId[0]);
        editor.putString("user_email", inEmail);
        editor.putString("user_password", inPassword);


        editor.putString("team", "none");
        editor.putString("my_goal", inGoal);
        editor.putString("periodical", inPeriodical);


        editor.putInt("loginStatus", 1);

        editor.putString("initDaily", strDate);

        editor.commit();


        writeNewPost(inName, inEmail, inPassword, inGoal, "none", "none", "0", inPeriodical, "0", "0", "0", "0", "0", "0");
        initDaily(emailId[0], strDate, "0", inGoal, "0", "0", "0", "0", "0", "0");

        if(bitmap != null) {
            StorageReference iconRef = mStorageRef.child("user_icon/"+inName+"/icon.jpg");

            byte[] uploadImage = convertBitmapToByteArray(bitmap);

            iconRef.putBytes(uploadImage);
        }


        goToHome();

    }

    private void setError() {

        mTiName.setError(null);
        mTiEmail.setError(null);
        mTiPassword.setError(null);
    }

    private void goToHome() {

        getActivity().finish();

        Intent intent = new Intent(getActivity().getBaseContext(),
                MainActivity.class);

        getActivity().startActivity(intent);
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


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if (requestCode == 0) {
            if (resultCode == Activity.RESULT_OK) {
                Uri selectedImage = imageReturnedIntent.getData();
                Log.i(TAG, "selectedImage: $selectedImage");

                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), selectedImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                bitmap = getCroppedBitmap(bitmap);

                icon.setImageBitmap(bitmap);
            }
        }
    }

    public byte[] convertBitmapToByteArray(Bitmap bMap) {
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            bMap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            return baos.toByteArray();
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
