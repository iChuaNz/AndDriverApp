//package servicetutorial.service;
package sg.com.commute_solutions.bustracker.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import sg.com.commute_solutions.bustracker.common.Constants;
import sg.com.commute_solutions.bustracker.common.Preferences;
import sg.com.commute_solutions.bustracker.fragments.LoginActivity;
import sg.com.commute_solutions.bustracker.fragments.MapsActivity;
import sg.com.commute_solutions.bustracker.util.StringUtil;
import sg.com.commute_solutions.bustracker.webservices.WebServiceTask;
import sg.com.commute_solutions.bustracker.webservices.WebServiceUtils;

/**
 * Created by deepshikha on 24/11/16.
 */

public class GoogleServices extends Service {

    boolean isGPSEnable = false;
    boolean isNetworkEnable = false;
    double latitude, longitude;
    LocationManager locationManager;
    Location location;
    private Handler mHandler = new Handler();
    private Timer mTimer = null;
    long notify_interval = 5000;
    public static String str_receiver = "servicetutorial.service.receiver";
    Intent intent;
    int counter = 0;

    private Timer timer = null;
    private TimerTask timerTask = null;

    private ContentValues contentValues;
    private JSONObject obj, jsonObject;
    private GoogleServices.LocationTask mLocationTask = null;
    private final ContentValues authenticationToken = new ContentValues();
    private static final String LOG_TAG = GoogleServices.class.getSimpleName();
    private String authToken;
    private SharedPreferences prefs;
    LocationRequest request;
    private boolean isServiceStart;

    public GoogleServices() {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = this.getSharedPreferences(Constants.SHARE_PREFERENCE_PACKAGE, Context.MODE_PRIVATE);
        authToken = StringUtil.deNull(prefs.getString(Preferences.AUTH_TOKEN, ""));
        authenticationToken.put(Constants.TOKEN, authToken);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            createNotificationChanel();
        } else {
            startForeground(1, new Notification());
        }
        requestLocationUpdates();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        isServiceStart = true;
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, RestartBackgroundService.class);
        this.sendBroadcast(broadcastIntent);
        stopForeground(true);
        isServiceStart = false;
        stopSelf();
    }

    @RequiresApi(26)
    private final void createNotificationChanel() {
        String NOTIFICATION_CHANNEL_ID = "com.getlocationbackground";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                channelName,
                NotificationManager.IMPORTANCE_NONE
        );
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(chan);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("App is running count::" + counter)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    private final void requestLocationUpdates() {
        request = new LocationRequest();
        request.setInterval(10000);
        request.setFastestInterval(5000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        final FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient((Context) this);
        int permission = ContextCompat.checkSelfPermission((Context) this, "android.permission.ACCESS_FINE_LOCATION");
        if (permission == PackageManager.PERMISSION_GRANTED) {
            client.requestLocationUpdates(request, (LocationCallback) (new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    Location location = locationResult.getLastLocation();
                    if(!isServiceStart){
                        client.removeLocationUpdates(this);
                    }
                    if (location != null && isServiceStart) {
                        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy, H:mm:ss");
                        Calendar cal = Calendar.getInstance();
                        df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                        cal.setTime(new Date());
                        cal.add(Calendar.HOUR_OF_DAY, 8);
                        final Date adjustedDate = cal.getTime();
                        final String date = df.format(adjustedDate);
                        sendCurrentLocation(location.getLongitude(), location.getLatitude(), location.getAltitude(), location.getAccuracy(), location.getSpeed(), date);
                    }

                }
            }), null);
        }

    }

    private class LocationTask extends WebServiceTask {
        final SharedPreferences.Editor editor = prefs.edit();

        LocationTask(double longitude, double latitude, double altitude, float accuracy, float speed, String date) {
            super(GoogleServices.this);
            contentValues = new ContentValues();
            contentValues.put(Constants.LONGTITUDE, longitude);
            contentValues.put(Constants.LATITUDE, latitude);
            contentValues.put(Constants.ALTITUDE, altitude);
            contentValues.put(Constants.ACCURACY, accuracy);
            contentValues.put(Constants.SPEED, speed);
            contentValues.put(Constants.DATE, date);
            Log.d(LOG_TAG, "latitude:" + latitude);
            Log.d(LOG_TAG, "longitude:" + longitude);
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
            int count = 5;

            for (int i = 0; i < count; i++) {
                obj = WebServiceUtils.requestJSONObject(Constants.LOCATION_URL, WebServiceUtils.METHOD.POST, authenticationToken, contentValues);
                if (obj != null) {
                    jsonObject = obj.optJSONObject("data");
                    Log.d(LOG_TAG, "Attempt " + (i + 1) + "::" + obj.toString());
                    if ((jsonObject.optString(Constants.BUS_CHARTER_MESSAGE).equalsIgnoreCase("Updated"))) {
                        return true;
                    }
                } else {
                    i = count;
                }
            }
            return false;
        }

        @Override
        public void performSuccessfulOperation() {
            Log.d(LOG_TAG, "Logged Successful!");
        }

        @Override
        public void onFailedAttempt() {
            //do nothing
        }
    }

    public void sendCurrentLocation(double longitude, double latitude, double altitude, float accuracy, float speed, String date) {
        mLocationTask = new GoogleServices.LocationTask(longitude, latitude, altitude, accuracy, speed, date);
        mLocationTask.execute((Void) null);

    }
}
