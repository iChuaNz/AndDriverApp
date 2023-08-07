package sg.com.commute_solutions.bustracker.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
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

import com.github.gcacace.signaturepad.views.SignaturePad;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

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

public class LogSheetActivity extends AppCompatActivity {
    private String LOG_TAG = LoginActivity.class.getSimpleName();

    private EditText mUserView, mFullNameView;
    private TextView mTxtTitle, mLblUser, mLblFullName;
    private ProgressBar mlogSheetProgressBar;
    private SharedPreferences prefs;
    private String username, fullname, lang, deviceToken, charterId;
    private Bitmap signImage;

    private SignaturePad signaturePad;

    private Intent intent;
    private Context context;

    private Button btnSubmit, btnClear;
    private ImageView btnLanguage;
    private JSONObject obj, jsonObject;
    private ContentValues contentValues;
    private LogSheetActivity.LogSheetTask mLogSheetTask = null;
    private final ContentValues firebaseToken = new ContentValues();
    private final ContentValues authenticationToken = new ContentValues();
    private String authToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_sheet);
        context = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        prefs = this.getSharedPreferences(Constants.SHARE_PREFERENCE_PACKAGE, Context.MODE_PRIVATE);
        deviceToken = FirebaseInstanceId.getInstance().getToken();
        firebaseToken.put(Constants.DEVICE_TOKEN, deviceToken);
        lang = StringUtil.deNull(prefs.getString(Preferences.LANGUAGE, ""));
        if (lang.isEmpty() || lang.equalsIgnoreCase("")) {
            lang = Constants.ENGLISH;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(Preferences.LANGUAGE, Constants.ENGLISH);
            editor.commit();
        }

        authToken = StringUtil.deNull(prefs.getString(Preferences.AUTH_TOKEN, ""));
        if (authToken == null || authToken.isEmpty()) {
            resetApp();
        } else {
            authenticationToken.put(Constants.TOKEN, authToken);
        }
        initViews();
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSubmit();
            }
        });
        signaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {

            @Override
            public void onStartSigning() {
                //Event triggered when the pad is touched
            }

            @Override
            public void onSigned() {
                btnSubmit.setEnabled(true);
                btnClear.setEnabled(true);
            }

            @Override
            public void onClear() {
                btnSubmit.setEnabled(false);
                btnClear.setEnabled(false);
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signaturePad.clear();
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
            //mTxtTitle.setText("To Be");
            //mLblUser.setText("登录");
            //mUserView.setHint("用户名");
            //mLblFullName.setText("包车服务");
            //mFullNameView.setHint("請輸入您的全名");
            btnSubmit.setText("確認");

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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        startActivity(intent);
        finish();
    }

    private void resetApp() {
        prefs.edit().clear().commit();
        LogSheetActivity.this.finishAffinity();
        Intent i = new Intent(null, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    private void initViews() {
        mUserView = (EditText) findViewById(R.id.txtLogSheetUser);
        mFullNameView = (EditText) findViewById(R.id.txtLogSheetFullName);
        mlogSheetProgressBar = (ProgressBar) findViewById(R.id.logSheetProgressBar);
        mTxtTitle = (TextView) findViewById(R.id.lbl_log_sheet_title);
        mLblFullName = (TextView) findViewById(R.id.lbl_log_sheet_fullname);
        mLblUser = (TextView) findViewById(R.id.lbl_log_sheet_user);
        btnSubmit = (Button) findViewById(R.id.btn_log_sheet_submit);
        btnLanguage = (ImageView) findViewById(R.id.btn_language);
        signaturePad = (SignaturePad) findViewById(R.id.signature_pad);
        btnClear = (Button) findViewById(R.id.btn_log_sheet_clear);

        btnSubmit.setEnabled(false);
    }

    public void attemptSubmit() {
        mUserView.setError(null);
        mFullNameView.setError(null);

        username = mUserView.getText().toString();
        fullname = mFullNameView.getText().toString();
        signImage = signaturePad.getSignatureBitmap();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(username)) {
            mUserView.setError(getString(R.string.error_password_length));
            focusView = mUserView;
            cancel = true;
        }
        if (TextUtils.isEmpty(fullname)) {
            mFullNameView.setError(getString(R.string.error_field_required));
            focusView = mFullNameView;
            cancel = true;
        }

        charterId = prefs.getString(Preferences.ADHOC_CHARTER_ID, "CARLSONTEST");
        if (cancel) {
            focusView.requestFocus();
        } else {
            mLogSheetTask = new LogSheetActivity.LogSheetTask();
            mLogSheetTask.execute((Void) null);
        }
    }

    private void toast(String text) {
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        toast.show();
    }

    private String getStringFromBitmap(Bitmap bitmapPicture) {
        final int COMPRESSION_QUALITY = 100;
        String encodedImage;
        ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
        bitmapPicture.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY,
                byteArrayBitmapStream);
        byte[] b = byteArrayBitmapStream.toByteArray();
        encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
        return encodedImage;
    }
    private class LogSheetTask extends WebServiceTask {

        LogSheetTask() {
            super(LogSheetActivity.this);
            contentValues = new ContentValues();
            mlogSheetProgressBar.setVisibility(View.VISIBLE);
            Log.d(LOG_TAG, "Username: " + username + " fullname: " + fullname + " routeAcessCode: " + charterId);
            contentValues.put(Constants.LOG_SHEET_BUSCHARTERID, charterId);
            contentValues.put(Constants.LOG_SHEET_USERNAME, username);
            contentValues.put(Constants.LOG_SHEET_FULLNAME, fullname);
            contentValues.put(Constants.LOG_SHEET_SIGN_IMG,getStringFromBitmap(signImage));
            contentValues.put(Constants.DEVICE_TOKEN, "");

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
            obj = WebServiceUtils.requestJSONObject(Constants.LOG_SHEET_URL, WebServiceUtils.METHOD.POST, authenticationToken, contentValues);
            if (!hasError(obj)) {
                Boolean success = obj.optBoolean("success");
                if (success) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    if (lang.equalsIgnoreCase(Constants.CHINESE)) {
                        builder.setMessage(obj.optString("message"))
                                .setTitle("上傳成功!")
                                .setCancelable(true).show();
                    } else {
                        builder.setMessage(obj.optString("message"))
                                .setTitle("Succeess!")
                                .setCancelable(true).show();
                    }
                    return true;
                }
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                if (lang.equalsIgnoreCase(Constants.CHINESE)) {
                    builder.setMessage(obj.optString("message"))
                            .setTitle("上傳失敗!")
                            .setCancelable(true).show();
                } else {
                    builder.setMessage(obj.optString("message"))
                            .setTitle("Submission Failed!")
                            .setCancelable(true).show();
                }
                return false;

            }
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            if (lang.equalsIgnoreCase(Constants.CHINESE)) {
                builder.setMessage("請聯絡后端部門，來助您!")
                        .setTitle("上傳失敗!")
                        .setCancelable(true).show();
            } else {
                builder.setMessage(username + "@" + fullname + "@" + charterId + "Please Contact the Operation for further help")
                        .setTitle("Submission Failed!")
                        .setCancelable(true).show();
            }
            return false;
        }

        @Override
        public void performSuccessfulOperation() {
            mlogSheetProgressBar.setVisibility(View.GONE);
            mUserView.setText("");
            mFullNameView.setText("");
            signaturePad.clear();
        }

        @Override
        public void onFailedAttempt() {
            mlogSheetProgressBar.setVisibility(View.GONE);
        }
    }
}
