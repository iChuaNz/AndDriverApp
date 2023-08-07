package sg.com.commute_solutions.bustracker.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONObject;
import org.w3c.dom.Text;

import sg.com.commute_solutions.bustracker.R;
import sg.com.commute_solutions.bustracker.common.Constants;
import sg.com.commute_solutions.bustracker.common.Preferences;
import sg.com.commute_solutions.bustracker.fragments.charters.AvailableCharterActivity;
import sg.com.commute_solutions.bustracker.fragments.charters.CharterProfileActivity;
import sg.com.commute_solutions.bustracker.fragments.charters.DisputedCharterActivity;
import sg.com.commute_solutions.bustracker.fragments.charters.NewCharterActivity;
import sg.com.commute_solutions.bustracker.fragments.charters.UpdateCharterActivity;
import sg.com.commute_solutions.bustracker.fragments.charters.YourCharterActivity;
import sg.com.commute_solutions.bustracker.fragments.charters.contractservices.ViewAllContracts;
import sg.com.commute_solutions.bustracker.util.GPSUtil;
import sg.com.commute_solutions.bustracker.util.StringUtil;
import sg.com.commute_solutions.bustracker.webservices.WebServiceTask;
import sg.com.commute_solutions.bustracker.webservices.WebServiceUtils;

public class RouteActivity extends AppCompatActivity {
    private String LOG_TAG = RouteActivity.class.getSimpleName();

    private TextView lblPhoneNumber,lblAcessCodeDesc;
    private EditText mAccessCodeView;
    private TextView mVehicleNoView;
    private ProgressBar mRouteProgressBar;
    private FrameLayout f1VehicleNo;
    private ImageView btnLanguage;
    private Button btnNext;
    private Button btnAutoFetch;
    private TextView lblUpdateVehicle;

    private LinearLayout flVehicleNo;
    private SharedPreferences prefs;
    private String phoneNumber, deviceToken, lang, vehicleNo, accessCode, authToken;

    private Intent intent;
    private Context context;
    private final ContentValues authenticationToken = new ContentValues();

    private JSONObject obj, jsonObject;
    private ContentValues contentValues;
    private RouteTask mRouteTask = null;
    private VehicleNoTask mVehicleNoTask = null;
    private final ContentValues firebaseToken = new ContentValues();

    private void initViews() {
        lblPhoneNumber = (TextView) findViewById(R.id.lblPhoneNumber);
        mRouteProgressBar = (ProgressBar) findViewById(R.id.routeProgressBar);
        mAccessCodeView = (EditText) findViewById(R.id.txtAccessCode);
        mVehicleNoView = (TextView) findViewById(R.id.txtVehicleNo);
        btnNext = (Button) findViewById(R.id.btnNext);
        btnAutoFetch = (Button) findViewById(R.id.btnAutoFetch);
        btnLanguage = (ImageView) findViewById(R.id.btn_language);
        flVehicleNo = (LinearLayout) findViewById(R.id.flVehicleNo);
        lblUpdateVehicle = (TextView) findViewById(R.id.lblUpdateVehicle);
        lblAcessCodeDesc = (TextView) findViewById(R.id.lblAcessCodeDesc);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker_route);
        context = this;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        prefs = this.getSharedPreferences(Constants.SHARE_PREFERENCE_PACKAGE, Context.MODE_PRIVATE);
        deviceToken = FirebaseInstanceId.getInstance().getToken();
        firebaseToken.put(Constants.DEVICE_TOKEN, deviceToken);

        authToken = StringUtil.deNull(prefs.getString(Preferences.AUTH_TOKEN, ""));
        if (authToken == null || authToken.isEmpty()) {
            resetApp();
        } else {
            authenticationToken.put(Constants.TOKEN, authToken);
        }

        String loggedInUsername = prefs.getString(Preferences.LOGGED_IN_USERNAME, "");
        lang = StringUtil.deNull(prefs.getString(Preferences.LANGUAGE, ""));
        phoneNumber = prefs.getString(Preferences.LOGGED_IN_PHONENUMBER, "9XXX XXXX");
        vehicleNo = prefs.getString(Preferences.LOGGED_IN_VEHICLENO, "ABC1234");
        if (lang.isEmpty() || lang.equalsIgnoreCase("")) {
            lang = Constants.ENGLISH;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(Preferences.LANGUAGE, Constants.ENGLISH);
            editor.putString(Preferences.CURRENTACTIVITY, "route");
            editor.commit();
        }
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Preferences.CURRENTACTIVITY, "route");
        editor.commit();

        if (!loggedInUsername.equalsIgnoreCase("")) {
            Log.d(LOG_TAG, "User: " + loggedInUsername);
        }

        initViews();

        lblPhoneNumber.setText("Welcome " + phoneNumber);
        mVehicleNoView.setText(vehicleNo);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptAssignRoute();
            }
        });
        flVehicleNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptChangeVehicleNo();
            }
        });
        btnAutoFetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToMapActivity();
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(Preferences.MANUAL_ACCESSCODE, "");
                editor.commit();
            }
        });
        if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
            btnLanguage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage(Constants.LANGUAGE_MESSAGE)
                            .setTitle(Constants.LANGUAGE_TITLE)
                            .setCancelable(false)
                            .setPositiveButton("English", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    toast(Constants.CURRENT_LANGUAGE);
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton("中文", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putString(Preferences.LANGUAGE, Constants.CHINESE);
                                    editor.commit();
                                    dialog.dismiss();
                                    AlertDialog.Builder builder2 = new AlertDialog.Builder(context);
                                    builder2.setMessage(Constants.RESTART_APP_CH)
                                            .setCancelable(false)
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    finishAndRemoveTask();
                                                }
                                            }).show();
                                }
                            }).setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            });
        } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {
            mAccessCodeView.setHint("输入您的路线号代码");
            btnNext.setText("下一步");
            btnAutoFetch.setText("获取后台设定的路线");
            lblUpdateVehicle.setText("更改车牌号");
            lblPhoneNumber.setText("您好 " + phoneNumber);
            lblAcessCodeDesc.setText("输入您的路线号代码");


            btnLanguage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage(Constants.LANGUAGE_MESSAGE_CH)
                            .setTitle(Constants.LANGUAGE_TITLE_CH)
                            .setCancelable(false)
                            .setPositiveButton("English", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putString(Preferences.LANGUAGE, Constants.ENGLISH);
                                    editor.commit();
                                    dialog.dismiss();
                                    AlertDialog.Builder builder2 = new AlertDialog.Builder(context);
                                    builder2.setMessage(Constants.RESTART_APP)
                                            .setCancelable(false)
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    finishAndRemoveTask();
                                                }
                                            }).show();
                                }
                            })
                            .setNegativeButton("中文", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    toast(Constants.CURRENT_LANGUAGE_CH);
                                    dialog.dismiss();
                                }
                            }).setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            });
        }
    }

    private void resetApp() {
        prefs.edit().clear().commit();
        RouteActivity.this.finishAffinity();
        Intent i = new Intent(context, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    public void attemptAssignRoute() {
        mAccessCodeView.setError(null);
        accessCode = mAccessCodeView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
            if (TextUtils.isEmpty(phoneNumber)) {
                mAccessCodeView.setError(getString(R.string.error_field_required));
                focusView = mAccessCodeView;
                cancel = true;
            }
        } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {
            if (TextUtils.isEmpty(phoneNumber)) {
                mAccessCodeView.setError("请输入您的路线代码");
                focusView = mAccessCodeView;
                cancel = true;
            }
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            mRouteTask = new RouteTask();
            mRouteTask.execute((Void) null);
        }
    }

    private void attemptChangeVehicleNo() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final EditText txt_vehicleNo = new EditText(context);
        txt_vehicleNo.setInputType(InputType.TYPE_CLASS_TEXT);
        txt_vehicleNo.setHint("eg. SG 1234");
        String msg = "";
        if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
            msg = Constants.UPDATE_VEHICLE_MESSAGE;
        } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {
            msg = Constants.UPDATE_VEHICLE_MESSAGE_CH;
        }
        builder.setMessage(msg)
                .setView(txt_vehicleNo)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            String vehicleNo = txt_vehicleNo.getText().toString();
                            mVehicleNoTask = new VehicleNoTask(vehicleNo);
                            mVehicleNoTask.execute((Void) null);
                        } catch (Exception e) {
                            //do nothing
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    private void goToMapActivity() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }else {
            if (!GPSUtil.isLocationEnabled(context)) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);

                if (lang.equalsIgnoreCase(Constants.CHINESE)) {
                    builder.setMessage(Constants.GPS_OFF_CH)
                            .setTitle("Error!")
                            .setCancelable(true).show();
                } else {
                    builder.setMessage(Constants.GPS_OFF)
                            .setTitle("Error!")
                            .setCancelable(true).show();
                }
            } else {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(Preferences.IS_FIRST_TIME, false);
                editor.remove(Preferences.PASSENGERLIST);
                editor.apply();
                intent = new Intent(getApplicationContext(), MapsActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    private void toast(String text) {
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        toast.show();
    }

    private class VehicleNoTask extends WebServiceTask {
        String myVehicleNo = "";

        VehicleNoTask(String vehicleNo) {
            super(RouteActivity.this);
            contentValues = new ContentValues();
            mRouteProgressBar.setVisibility(View.VISIBLE);
            contentValues.put(Constants.ACCESSCODE, accessCode);
            myVehicleNo = vehicleNo;
            performRequest();
        }

        @Override
        public void showProgress() {

        }

        @Override
        public void hideProgress() {

        }

        @Override
        public boolean performRequest() {
            mRouteProgressBar.setVisibility(View.GONE);
            String URL = Constants.UPDATE_VEHICLE_NO + "/" + myVehicleNo;
            obj = WebServiceUtils.requestJSONObject(URL, WebServiceUtils.METHOD.POST, authenticationToken, context);
            if (!hasError(obj)) {
                Log.d(LOG_TAG, obj.toString());
                jsonObject = obj.optJSONObject(Constants.DATA);

                if (jsonObject != null) {

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(Preferences.LOGGED_IN_VEHICLENO, myVehicleNo);
                    editor.commit();

                    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    if (!lang.equalsIgnoreCase(Constants.CHINESE)) {
                        builder.setMessage("Vehicle No  Update Sucessful")
                                .setTitle("Success!")
                                .setCancelable(true).show();
                    } else {
                        builder.setMessage("成功修改车牌号码")
                                .setTitle("Success!")
                                .setCancelable(true).show();
                    }
                }
            }
            return false;
        }

        @Override
        public void performSuccessfulOperation() {
            mRouteProgressBar.setVisibility(View.GONE);
            mVehicleNoView.setText(myVehicleNo);
        }

        @Override
        public void onFailedAttempt() {
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            if (lang.equalsIgnoreCase(Constants.CHINESE)) {
                builder.setMessage("Error in update vehicle No")
                        .setTitle("Error!")
                        .setCancelable(true).show();
            } else {
                builder.setMessage("修改不成功，请再试。")
                        .setTitle("Error!")
                        .setCancelable(true).show();
            }
        }
    }

    private class RouteTask extends WebServiceTask {
        boolean isSuccess = false;
        RouteTask() {
            super(RouteActivity.this);
            contentValues = new ContentValues();
            mRouteProgressBar.setVisibility(View.VISIBLE);
            contentValues.put(Constants.ACCESSCODE, accessCode);
            contentValues.put(Constants.DEVICE_TOKEN, deviceToken);

            isSuccess = performRequest();
        }

        @Override
        public void showProgress() {

        }

        @Override
        public void hideProgress() {

        }

        @Override
        public boolean performRequest() {
            String URL = Constants.CHECK_ACCESS_CODE;
            obj = WebServiceUtils.requestJSONObject(URL, WebServiceUtils.METHOD.POST, authenticationToken, contentValues);
            if (!hasError(obj)) {
                Log.d(LOG_TAG, obj.toString());
                jsonObject = obj.optJSONObject(Constants.DATA);

                if (jsonObject != null) {
                    Boolean success = jsonObject.optBoolean("success");
                    if(success) {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(Preferences.MANUAL_ACCESSCODE, accessCode);
                        editor.commit();
                        return true;
                    }
                    return false;
                }
                return false;
            }
            return false;
        }

        @Override
        public void performSuccessfulOperation() {

            mRouteProgressBar.setVisibility(View.GONE);
            if(isSuccess){
                goToMapActivity();
            }else{
                mRouteProgressBar.setVisibility(View.GONE);
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                if (lang.equalsIgnoreCase(Constants.CHINESE)) {
                    builder.setMessage(Constants.WRONG_ACCESS_CODE_CH)
                            .setTitle("Error!")
                            .setCancelable(true).show();
                } else {
                    builder.setMessage(Constants.WRONG_ACCESS_CODE)
                            .setTitle("Error!")
                            .setCancelable(true).show();
                }
            }
        }

        @Override
        public void onFailedAttempt() {
            mRouteProgressBar.setVisibility(View.GONE);
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            if (lang.equalsIgnoreCase(Constants.CHINESE)) {
                builder.setMessage(Constants.WRONG_ACCESS_CODE_CH)
                        .setTitle("Error!")
                        .setCancelable(true).show();
            } else {
                builder.setMessage(Constants.WRONG_ACCESS_CODE)
                        .setTitle("Error!")
                        .setCancelable(true).show();
            }
        }
    }
}