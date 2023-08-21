package sg.com.commute_solutions.bustracker.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

import me.leolin.shortcutbadger.ShortcutBadger;
import sg.com.commute_solutions.bustracker.common.Preferences;
import sg.com.commute_solutions.bustracker.fragments.charters.NewCharterActivity;
import sg.com.commute_solutions.bustracker.common.Constants;
import sg.com.commute_solutions.bustracker.util.StringUtil;
import sg.com.commute_solutions.bustracker.webservices.WebServiceTask;
import sg.com.commute_solutions.bustracker.webservices.WebServiceUtils;

/**
 * Created by Kyle on 31/8/16.
 */
public class StartActivity extends AppCompatActivity {
    private final static String LOG_TAG = StartActivity.class.getSimpleName();

    private Intent intent;
    private SharedPreferences prefs;

    private String displayMessage = "";
    private boolean isSuccessful = true;

//    private CheckLatestVersionTask mCheckLatestVersionTask;
    private JSONObject obj, jsonObject;
    private ContentValues contentValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = this.getSharedPreferences(Constants.SHARE_PREFERENCE_PACKAGE, Context.MODE_PRIVATE);
        intent = new Intent(StartActivity.this, LoginActivity.class);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(Preferences.BADGE_COUNT, 0);
        ShortcutBadger.removeCount(StartActivity.this);

        String loggedInUsername = prefs.getString(Preferences.LOGGED_IN_USERNAME, "");
        String loggedInUserToken = prefs.getString(Preferences.AUTH_TOKEN, "");

        if(!StringUtil.deNull(loggedInUsername).isEmpty() && !StringUtil.deNull(loggedInUserToken).isEmpty()) {
            Log.d(LOG_TAG, loggedInUsername + "has logged in");
            String lastSavedState = prefs.getString(Preferences.CURRENTACTIVITY, "");
            if (StringUtil.deNull(lastSavedState).equalsIgnoreCase("map")) {
                intent = new Intent(StartActivity.this, MapsActivity.class);
            }
//            else if (StringUtil.deNull(lastSavedState).equalsIgnoreCase("settings")) {
//                intent = new Intent(StartActivity.this, SettingsActivity.class);
//            } else if (StringUtil.deNull(lastSavedState).equalsIgnoreCase("charterList")) {
//                intent = new Intent(StartActivity.this, AvailableCharterActivity.class);
//            }
            else if (StringUtil.deNull(lastSavedState).equalsIgnoreCase("createcharter")) {
                intent = new Intent(StartActivity.this, NewCharterActivity.class);
            }
        }

        String lang = prefs.getString(Preferences.LANGUAGE, "");
        if (StringUtil.deNull(lang).isEmpty() || StringUtil.deNull(lang).equalsIgnoreCase("")) {
            editor.putString(Preferences.LANGUAGE, Constants.ENGLISH);
            editor.commit();
        }

        if(isSuccessful) {
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(displayMessage)
                    .setCancelable(false)
                    .setPositiveButton("Understood", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Intent intent = new Intent(getApplicationContext(), StartActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }).show();
        }
    }

    /*
*
* START OF JSON TASK
*
* */
    private class CheckLatestVersionTask extends WebServiceTask {
        CheckLatestVersionTask(String versionNo){
            super(StartActivity.this);
            contentValues = new ContentValues();
            contentValues.put(Constants.VERSION, versionNo);
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
            obj = WebServiceUtils.requestJSONObject(Constants.VERSION_URL, WebServiceUtils.METHOD.POST, null, contentValues);
            if (!hasError(obj)) {
                Log.d(LOG_TAG, obj.toString());
                jsonObject = obj.optJSONObject(Constants.DATA);

                if (jsonObject != null) {
//                    try {
                        //TODO: get version No.
//                    } catch (JSONException e) {
//                        Log.e(LOG_TAG, "Error processing Json data = " + e.getMessage());
//                    }
                    return true;
                }
            }
            return false;
        }

        @Override
        public void performSuccessfulOperation() {

        }

        @Override
        public void onFailedAttempt() {

        }
    }
}
