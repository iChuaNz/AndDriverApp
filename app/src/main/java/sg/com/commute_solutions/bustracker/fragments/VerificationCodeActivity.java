package sg.com.commute_solutions.bustracker.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import sg.com.commute_solutions.bustracker.fragments.charters.AvailableCharterActivity;
import sg.com.commute_solutions.bustracker.fragments.charters.NewCharterActivity;
import sg.com.commute_solutions.bustracker.util.GPSUtil;
import sg.com.commute_solutions.bustracker.webservices.WebServiceTask;
import sg.com.commute_solutions.bustracker.webservices.WebServiceUtils;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONObject;

import sg.com.commute_solutions.bustracker.R;
import sg.com.commute_solutions.bustracker.common.Constants;
import sg.com.commute_solutions.bustracker.common.Preferences;
import sg.com.commute_solutions.bustracker.util.StringUtil;

public class VerificationCodeActivity extends AppCompatActivity {
    private String LOG_TAG = LoginActivity.class.getSimpleName();

    private TextView lblVeriCodePhone,lblVeriCode;
    private EditText mSixDigitCodeView;
    private ProgressBar mCodeProgressBar;
    private SharedPreferences prefs;
    private String deviceToken, lang;
    private int loginAttempts;
    public String phoneNumber;

    private Intent intent;
    private Context context;

    private FrameLayout flSelection;
    private String selection;
    private Button btnNext, btnSelectionNext;
    private ImageView btnLanguage;
    private String driverRole, isValidMobile;
    private JSONObject obj, jsonObject;
    private ContentValues contentValues;
    private final ContentValues firebaseToken = new ContentValues();
    private UserLoginTask mUserLoginTask = null;

    private void initViews() {
        mCodeProgressBar = (ProgressBar) findViewById(R.id.codeProgressBar);
        mSixDigitCodeView = (EditText) findViewById(R.id.sixDigitCode);
        btnNext = (Button) findViewById(R.id.btnNext);
        btnSelectionNext = (Button) findViewById(R.id.btn_Next);
        btnLanguage = (ImageView) findViewById(R.id.btn_language);
        flSelection = (FrameLayout) findViewById(R.id.fl_popupSelection);
        flSelection.setAlpha(0.8f);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification_code);
        context = this;
        intent = getIntent();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        prefs = this.getSharedPreferences(Constants.SHARE_PREFERENCE_PACKAGE, Context.MODE_PRIVATE);
        loginAttempts = 0;

        deviceToken = FirebaseInstanceId.getInstance().getToken();
        firebaseToken.put(Constants.DEVICE_TOKEN, deviceToken);

        String loggedInUsername = prefs.getString(Preferences.LOGGED_IN_USERNAME, "");
        lang = StringUtil.deNull(prefs.getString(Preferences.LANGUAGE, ""));
        if (lang.isEmpty() || lang.equalsIgnoreCase("")) {
            lang = Constants.ENGLISH;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(Preferences.LANGUAGE, Constants.ENGLISH);
            editor.commit();
        }

        lblVeriCodePhone = (TextView) findViewById(R.id.lblVeriCodePhone);
        lblVeriCode = (TextView) findViewById(R.id.lblVeriCode);

        lblVeriCodePhone.setText(prefs.getString(Preferences.LOGGED_IN_PHONENUMBER, "+65 9XXX XXXX"));
        phoneNumber = lblVeriCodePhone.getText().toString();
        if (!loggedInUsername.equalsIgnoreCase("")) {
            Log.d(LOG_TAG, "User: " + loggedInUsername);
        }

        initViews();
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attempVerifyCode();
            }
        });
        btnSelectionNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchNextActivity();
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
            mSixDigitCodeView.setHint("输入您信息收到的六个号码");
            btnNext.setText("下一步");
            lblVeriCode.setText("输入您信息收到的六个号码");

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

    private void launchNextActivity() {
        if (selection.equalsIgnoreCase("tracking")) {
            if (!GPSUtil.isLocationEnabled(context)) {
                intent = new Intent(getApplicationContext(), StartActivity.class);
                startActivity(intent);
                finish();
            } else {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(Preferences.IS_FIRST_TIME, false);
                editor.remove(Preferences.PASSENGERLIST);
                editor.apply();
                intent = new Intent(getApplicationContext(), RouteActivity.class);
                startActivity(intent);
                finish();
            }
        } else if (selection.equalsIgnoreCase("chartering")) {
            intent = new Intent(getApplicationContext(), NewCharterActivity.class);
            startActivity(intent);
            finish();
        } else {
            //do nothing
        }
    }

    private void attempVerifyCode() {
        mCodeProgressBar.setVisibility(View.VISIBLE);
        String sixDigitCode = prefs.getString(Preferences.LOGGED_IN_SIXDIGIT, "Invalid");
        mSixDigitCodeView = (EditText) findViewById(R.id.sixDigitCode);
        if (!sixDigitCode.equalsIgnoreCase("Invalid")) {
            if (sixDigitCode.equalsIgnoreCase(mSixDigitCodeView.getText().toString())) {
                displaySucessMessage();
            } else {
                displayErrorMessage();
            }
        } else {
            displayErrorMessage();
        }
    }

    private void displaySucessMessage() {
        mCodeProgressBar.setVisibility(View.GONE);

        mUserLoginTask = new UserLoginTask(phoneNumber);
        mUserLoginTask.execute((Void) null);

    }

    private void displayErrorMessage() {
        mCodeProgressBar.setVisibility(View.GONE);
        loginAttempts++;
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
            if (loginAttempts > 10) {
                builder.setMessage(Constants.WRONG_SIX_DIGIT2)
                        .setTitle("Error!")
                        .setCancelable(true).show();
            } else {
                builder.setMessage(Constants.WRONG_SIX_DIGIT)
                        .setTitle("Error!")
                        .setCancelable(true).show();
            }
        } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {
            if (loginAttempts > 10) {
                builder.setMessage(Constants.WRONG_SIX_DIGIT2_CH)
                        .setTitle("Error!")
                        .setCancelable(true).show();
            } else {
                builder.setMessage(Constants.WRONG_SIX_DIGIT_CH)
                        .setTitle("Error!")
                        .setCancelable(true).show();
            }
        }
    }

    private void toast(String text) {
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.radio_tracker:
                if (checked) {
                    selection = "tracking";
                    break;
                }
            case R.id.radio_charter:
                if (checked) {
                    selection = "chartering";
                }
        }
    }


    private class UserLoginTask extends WebServiceTask {
        UserLoginTask(String phoneNumber) {
            super(VerificationCodeActivity.this);
            contentValues = new ContentValues();
            mCodeProgressBar.setVisibility(View.VISIBLE);
            contentValues.put(Constants.LOGINPHONE, lblVeriCodePhone.getText().toString());
            contentValues.put(Constants.DEVICE_TOKEN, deviceToken);
            performRequest();
        }

        @Override
        public boolean performRequest() {
            obj = WebServiceUtils.requestJSONObject(Constants.LOGIN_URL_V2, WebServiceUtils.METHOD.POST, null, contentValues);
            if (!hasError(obj)) {
                Log.d(LOG_TAG, obj.toString());
                jsonObject = obj.optJSONObject(Constants.DATA);
                if (jsonObject != null) {
                    SharedPreferences.Editor editor = prefs.edit();
                    driverRole = jsonObject.optString(Constants.DRIVERROLE);
                    String authToken = jsonObject.optString(Constants.TOKEN);
                    editor.putString(Preferences.LOGGED_IN_USERNAME, jsonObject.optString(Constants.USERNAME));
                    editor.putString(Preferences.LOGGED_IN_VEHICLENO, jsonObject.optString(Constants.LOGINVEHICLENO));
                    editor.putString(Preferences.AUTH_TOKEN, jsonObject.optString(Constants.TOKEN));
                    editor.putString(Preferences.ROLE, driverRole);
                    editor.putBoolean(Preferences.IS_FIRST_TIME, true);
                    editor.commit();
                    return true;
                }
            }
            return false;
        }

        @Override
        public void showProgress() {
        }

        @Override
        public void hideProgress() {
        }

        @Override
        public void performSuccessfulOperation() {
            if (driverRole == null) {
                onFailedAttempt();
            } else {
                if (driverRole.equalsIgnoreCase("OMO")) {
                    mCodeProgressBar.setVisibility(View.GONE);
                    flSelection.setVisibility(View.VISIBLE);
                } else if (driverRole.equalsIgnoreCase("driver") || driverRole.equalsIgnoreCase("ba")) {
                    Intent intent;
                    if (!GPSUtil.isLocationEnabled(context)) {
                        intent = new Intent(context, StartActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean(Preferences.IS_FIRST_TIME, false);
                        editor.remove(Preferences.PASSENGERLIST);
                        editor.apply();
                        intent = new Intent(context, RouteActivity.class);
                        startActivity(intent);
                        finish();
                    }
                } else if (driverRole.equalsIgnoreCase("admin")) {
                    Intent intent = new Intent(context, AvailableCharterActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }

        @Override
        public void onFailedAttempt() {
            mCodeProgressBar.setVisibility(View.GONE);
            displayErrorMessage();
        }
    }
}
