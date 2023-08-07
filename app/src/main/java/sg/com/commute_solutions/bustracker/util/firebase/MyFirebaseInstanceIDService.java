package sg.com.commute_solutions.bustracker.util.firebase;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.json.JSONException;
import org.json.JSONObject;

import sg.com.commute_solutions.bustracker.common.Constants;
import sg.com.commute_solutions.bustracker.common.Preferences;
import sg.com.commute_solutions.bustracker.fragments.LoginActivity;
import sg.com.commute_solutions.bustracker.util.StringUtil;
import sg.com.commute_solutions.bustracker.webservices.WebServiceTask;
import sg.com.commute_solutions.bustracker.webservices.WebServiceUtils;

/**
 * Created by Kyle on 15/5/17.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String LOG_TAG = MyFirebaseInstanceIDService.class.getSimpleName();
    private SharedPreferences prefs;
    private Context context;
    private ForwardNewDeviceTokenTask mForwardNewDeviceTokenTask = null;

    private String authToken;
    private final ContentValues authenticationToken = new ContentValues();
    private JSONObject obj, jsonObject;
    private ContentValues contentValues;
    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(LOG_TAG, "Refreshed token: " + refreshedToken);
        context = this;

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(refreshedToken);
    }
    // [END refresh_token]

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        prefs = this.getSharedPreferences(Constants.SHARE_PREFERENCE_PACKAGE, Context.MODE_PRIVATE);
        authToken = StringUtil.deNull(prefs.getString(Preferences.AUTH_TOKEN, ""));
        if (authToken.isEmpty() && !authToken.equalsIgnoreCase("")) {
            authenticationToken.put(Constants.TOKEN, authToken);
            mForwardNewDeviceTokenTask = new ForwardNewDeviceTokenTask(token);
            mForwardNewDeviceTokenTask.execute((Void) null);
        }
    }

    private class ForwardNewDeviceTokenTask extends WebServiceTask {
        ForwardNewDeviceTokenTask(String deviceToken){
            super(MyFirebaseInstanceIDService.this);
            contentValues = new ContentValues();
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
            obj = WebServiceUtils.requestJSONObject(Constants.REFRESH_DRIVER_TOKEN_URL, WebServiceUtils.METHOD.POST, authenticationToken, null, context, contentValues);
            if (!hasError(obj)) {
                Log.d(LOG_TAG, obj.toString());
                jsonObject = obj.optJSONObject(Constants.DATA);

                if (jsonObject != null) {
                    Boolean success = false;

                    try {
                        success = jsonObject.getBoolean(Constants.BUS_CHARTER_SUCCESS);
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "Error processing Json data = " + e.getMessage());
                    }

                    if (success) {
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
            Log.d(LOG_TAG, "Successfully updated server of new token!");
        }

        @Override
        public void onFailedAttempt() {
            prefs.edit().clear().commit();
            Intent i = new Intent(getApplicationContext(),LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }
    }
}
