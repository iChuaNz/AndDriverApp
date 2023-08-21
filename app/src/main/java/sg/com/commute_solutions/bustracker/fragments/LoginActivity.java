package sg.com.commute_solutions.bustracker.fragments;

import android.app.usage.ConfigurationStats;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import android.os.StrictMode;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
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

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONObject;

import sg.com.commute_solutions.bustracker.R;
import sg.com.commute_solutions.bustracker.common.Preferences;
import sg.com.commute_solutions.bustracker.fragments.charters.AvailableCharterActivity;
import sg.com.commute_solutions.bustracker.fragments.charters.CharterProfileActivity;
import sg.com.commute_solutions.bustracker.fragments.charters.DisputedCharterActivity;
import sg.com.commute_solutions.bustracker.fragments.charters.NewCharterActivity;
import sg.com.commute_solutions.bustracker.fragments.charters.UpdateCharterActivity;
import sg.com.commute_solutions.bustracker.fragments.charters.YourCharterActivity;
import sg.com.commute_solutions.bustracker.fragments.charters.contractservices.ViewAllContracts;
import sg.com.commute_solutions.bustracker.util.GPSUtil;
import sg.com.commute_solutions.bustracker.common.Constants;
import sg.com.commute_solutions.bustracker.util.StringUtil;
import sg.com.commute_solutions.bustracker.webservices.WebServiceTask;
import sg.com.commute_solutions.bustracker.webservices.WebServiceUtils;

public class LoginActivity extends AppCompatActivity {
    private String LOG_TAG = LoginActivity.class.getSimpleName();

    private EditText mPhoneNumberView;
    private TextView mTxtModeTitle;
    private ProgressBar mLoginProgressBar;
    private SharedPreferences prefs;
    private String phoneNumber, deviceToken, lang;
    private int loginAttempts;

    private Intent intent;
    private Context context;

    private FrameLayout flSelection;
    private String selection;
    private Button btnNext;
    private ImageView btnLanguage;
    private String driverRole;
    private Boolean isValidMobile;
    private JSONObject obj, jsonObject;
    private ContentValues contentValues;
    private UserLoginTask mUserLoginTask = null;
    private final ContentValues firebaseToken = new ContentValues();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context = this;
        intent = getIntent();
        String intentAction = StringUtil.deNull(intent.getAction());
        if (intentAction.equalsIgnoreCase("OPEN_ALT_ACTIVITY")) {
            Bundle fromNotification = intent.getExtras();
            String additionalInfo = StringUtil.deNull(fromNotification.getString(Constants.EXTRA));
            String charterId = StringUtil.deNull(fromNotification.getString(Constants.BUS_CHARTER_ID));

            if (additionalInfo.equalsIgnoreCase("poc") || additionalInfo.equalsIgnoreCase("expiry")) {
                intent = new Intent(LoginActivity.this, UpdateCharterActivity.class);
                intent.putExtra(Constants.BUS_CHARTER_ID, charterId);
                intent.putExtra(Constants.EXTRA, additionalInfo);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else if (additionalInfo.equalsIgnoreCase("viewCharter")) {
                intent = new Intent(LoginActivity.this, AvailableCharterActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else if (additionalInfo.equalsIgnoreCase("acceptedcharter")) {
                intent = new Intent(LoginActivity.this, YourCharterActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("intention", "acceptedCharters");
                startActivity(intent);
                finish();
            } else if (additionalInfo.equalsIgnoreCase("subbedoutcharter")) {
                intent = new Intent(LoginActivity.this, YourCharterActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("intention", "ownCharter");
                startActivity(intent);
                finish();
            } else if (additionalInfo.equalsIgnoreCase("profile")) {
                intent = new Intent(LoginActivity.this, CharterProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else if (additionalInfo.equalsIgnoreCase("dispute")) {
                intent = new Intent(LoginActivity.this, DisputedCharterActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else if (additionalInfo.equalsIgnoreCase("contract")) {
                intent = new Intent(LoginActivity.this, ViewAllContracts.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        } else if (intentAction.equalsIgnoreCase("android.intent.action.VIEW")) {
            intent = new Intent(LoginActivity.this, AvailableCharterActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }


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

        if (!loggedInUsername.equalsIgnoreCase("")) {
            Log.d(LOG_TAG, "User: " + loggedInUsername);
        }

        initViews();
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attempVerifyNumber();
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
            mPhoneNumberView.setHint("输入你的电话号码");
            btnNext.setText("下一步");

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

    private void initViews() {
        mLoginProgressBar = (ProgressBar) findViewById(R.id.loginProgressBar);
        mPhoneNumberView = (EditText) findViewById(R.id.phoneNumber);
        btnNext = (Button) findViewById(R.id.next_button);
        btnLanguage = (ImageView) findViewById(R.id.btn_language);
    }

    public void attempVerifyNumber() {
        mPhoneNumberView.setError(null);
        phoneNumber = mPhoneNumberView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
            if (TextUtils.isEmpty(phoneNumber)) {
                mPhoneNumberView.setError(getString(R.string.error_field_required));
                focusView = mPhoneNumberView;
                cancel = true;
            } else if (!isPhoneNumberValid(phoneNumber)) {
                mPhoneNumberView.setError(getString(R.string.error_invalid_phonenumber));
                focusView = mPhoneNumberView;
                cancel = true;
            }
        } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {
            if (TextUtils.isEmpty(phoneNumber)) {
                mPhoneNumberView.setError("请输入您的电话号码");
                focusView = mPhoneNumberView;
                cancel = true;
            } else if (!isPhoneNumberValid(phoneNumber)) {
                mPhoneNumberView.setError("电话号只可以有8个字");
                focusView = mPhoneNumberView;
                cancel = true;
            }
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            mUserLoginTask = new UserLoginTask(phoneNumber);
            mUserLoginTask.execute((Void) null);
        }
    }

    private void displayErrorMessage() {
        loginAttempts++;
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
            if (loginAttempts == 3) {
                builder.setMessage(Constants.CANNOT_LOGIN2)
                        .setTitle("Error!")
                        .setCancelable(true).show();
                loginAttempts = 2;
            } else {
                builder.setMessage(Constants.CANNOT_LOGIN1)
                        .setTitle("Error!")
                        .setCancelable(true).show();
            }
        } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {
            if (loginAttempts == 3) {
                builder.setMessage(Constants.CANNOT_LOGIN2_CH)
                        .setTitle("Error!")
                        .setCancelable(true).show();
                loginAttempts = 2;
            } else {
                builder.setMessage(Constants.CANNOT_LOGIN1_CH)
                        .setTitle("Error!")
                        .setCancelable(true).show();
            }
        }
    }

    private boolean isPhoneNumberValid(String PhoneNumber) {
        return PhoneNumber.length() == 8;
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
                intent = new Intent(getApplicationContext(), MapsActivity.class);
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

    private void toast(String text) {
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        toast.show();
    }

    private class UserLoginTask extends WebServiceTask {
        UserLoginTask(String phoneNumber) {
            super(LoginActivity.this);
            contentValues = new ContentValues();
            mLoginProgressBar.setVisibility(View.VISIBLE);
            Log.d(LOG_TAG, "PhoneNumber: " + phoneNumber);
            contentValues.put(Constants.LOGINPHONE, phoneNumber);
            contentValues.put(Constants.DEVICE_TOKEN, deviceToken);

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
            isValidMobile = false;
            obj = WebServiceUtils.requestJSONObject(Constants.CHECK_PHONE_URL, WebServiceUtils.METHOD.POST, null, contentValues);
            if (!hasError(obj)) {
                Log.d(LOG_TAG, obj.toString());
                jsonObject = obj.optJSONObject(Constants.DATA);

                if (jsonObject != null) {
                    SharedPreferences.Editor editor = prefs.edit();
                    isValidMobile = jsonObject.optBoolean("success");

                    if (isValidMobile) {
                        toast(jsonObject.optString(Constants.LOGINPHONE));
                        editor.putString(Preferences.LOGGED_IN_PHONENUMBER, jsonObject.optString(Constants.LOGINPHONE));
                        editor.putString(Preferences.LOGGED_IN_SIXDIGIT, jsonObject.optString(Constants.LOGINSIXDIGIT));
                        editor.commit();
                        return true;
                    } else {
                        return false;
                    }
                }
            }
            return false;
        }

        @Override
        public void performSuccessfulOperation() {
            if (isValidMobile) {
                Intent intent = new Intent(context, VerificationCodeActivity.class);
                startActivity(intent);
                finish();
            } else {
                onFailedAttempt();
            }
        }

        @Override
        public void onFailedAttempt() {
            mLoginProgressBar.setVisibility(View.GONE);
            displayErrorMessage();
        }
    }
}