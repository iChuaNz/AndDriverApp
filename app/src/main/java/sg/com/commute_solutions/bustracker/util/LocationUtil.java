package sg.com.commute_solutions.bustracker.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;

public class LocationUtil {
    public static boolean isMyServiceRunning(Class serviceClass, Activity mActivity) {
        ActivityManager manager  = (ActivityManager)mActivity.getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("Service status", "Running");
                return true;
            }
        }
        Log.i("Service status", "Not running");
        return false;
    }

    public static void showAlertLocation(final Context context, String title, String message, String btnText) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Understood", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        context.startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
                    }
                });
        builder.show();
    }

    public static boolean isLocationEnabledOrNot(Context context) {
        LocationManager locationManager = null;
        locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);

        return locationManager.isProviderEnabled("gps") || locationManager.isProviderEnabled("network");
    }
}
