package sg.com.commute_solutions.bustracker.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.zxing.Result;

import org.json.JSONException;
import org.json.JSONObject;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import sg.com.commute_solutions.bustracker.R;
import sg.com.commute_solutions.bustracker.common.Constants;
import sg.com.commute_solutions.bustracker.common.Preferences;
import sg.com.commute_solutions.bustracker.fragments.charters.AvailableCharterActivity;
import sg.com.commute_solutions.bustracker.util.GPSUtil;
import sg.com.commute_solutions.bustracker.util.StringUtil;
import sg.com.commute_solutions.bustracker.webservices.WebServiceTask;
import sg.com.commute_solutions.bustracker.webservices.WebServiceUtils;

import static android.Manifest.permission.CAMERA;

public class ScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    int MY_PERMISSION_REQUEST_CAMERA = 0;
    private static final int REQUEST_CAMERA = 1;
    ZXingScannerView mScannerView;
    private UserScanTask mUserScanTask = null;
    private ContentValues contentValues;
    private JSONObject obj, jsonObject;
    private SharedPreferences prefs;
    private final ContentValues authenticationToken = new ContentValues();
    private String authToken;
    private int routeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("onCreate", "onCreate");
        prefs = this.getSharedPreferences(Constants.SHARE_PREFERENCE_PACKAGE, Context.MODE_PRIVATE);
        mScannerView = new ZXingScannerView(this);
        routeId = prefs.getInt(Preferences.ROUTE_ID, 0);
        setContentView(mScannerView);
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.M) {
            if (checkPermission()) {
                Toast.makeText(getApplicationContext(), "Permission already granted", Toast.LENGTH_LONG).show();
            } else {
                requestPermission();
            }
        }
        authToken = StringUtil.deNull(prefs.getString(Preferences.AUTH_TOKEN, ""));
        if (authToken == null || authToken.isEmpty()) {
            resetApp();
        } else {
            authenticationToken.put(Constants.TOKEN, authToken);
        }

    }

    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        startActivity(intent);
        finish();
    }

    private void resetApp() {
        prefs.edit().clear().commit();
        ScannerActivity.this.finishAffinity();
        Intent i = new Intent(null, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    private boolean checkPermission() {
        return (ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, REQUEST_CAMERA);
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted) {
                        Toast.makeText(getApplicationContext(), "Permission Granted, Now you can access camera", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Permission Denied, You cannot access and camera", Toast.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(CAMERA)) {
                                showMessageOKCancel("You need to allow access to both the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{CAMERA},
                                                            REQUEST_CAMERA);
                                                }
                                            }
                                        });
                                return;
                            }
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(ScannerActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();

        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.M) {
            if (checkPermission()) {
                if (mScannerView == null) {
                    mScannerView = new ZXingScannerView(this);
                    setContentView(mScannerView);
                }
                mScannerView.setResultHandler(this);
                mScannerView.startCamera();
            } else {
                requestPermission();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result result) {

        final String myresult = result.getText();
        Log.d("QRCodeScanner", myresult);
        Log.d("QRCodeScanner", result.getBarcodeFormat().toString());

        int UserRiderId = Integer.parseInt(myresult);
        int BusRouteId = routeId;
        mUserScanTask = new UserScanTask(UserRiderId, BusRouteId);
        mUserScanTask.execute((Void) null);

        onResume();
    }

    private class UserScanTask extends WebServiceTask {
        int UserRiderId, BusRouteId;
        boolean res = false;
        String data = "";
        String error = "";

        UserScanTask(int UserRiderId, int BusRouteId) {
            super(ScannerActivity.this);
            this.UserRiderId = UserRiderId;
            this.BusRouteId = BusRouteId;
            performRequest();
        }

        @Override
        public boolean performRequest() {
            String URI = Constants.SCAN_QR_STUDENT + "/" + UserRiderId + "/" + BusRouteId;
            obj = WebServiceUtils.requestJSONObject(URI, WebServiceUtils.METHOD.POST, authenticationToken, contentValues);
            if (!hasError(obj)) {
                try {
                    res = obj.getBoolean("success");
                    data = obj.getString("data");
                    error = obj.getString("error");
                } catch (JSONException e) {
                    return false;
                }
                if (res) {
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
            if (res) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ScannerActivity.this);
                builder.setTitle("Scan Result");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.setNegativeButton("Back to Menu", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent;
                        intent = new Intent(getApplicationContext(), MapsActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
                if (data.length() > 0) {
                    builder.setMessage(data);
                } else {
                    builder.setMessage("Scan Successful");
                }
                builder.setMessage(data);
                AlertDialog alert1 = builder.create();
                alert1.show();
            } else {
                onFailedAttempt();
            }
        }

        @Override
        public void onFailedAttempt() {
            AlertDialog.Builder builder = new AlertDialog.Builder(ScannerActivity.this);
            builder.setTitle("Scan Result");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.setNegativeButton("Back to Menu", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent;
                    intent = new Intent(getApplicationContext(), MapsActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
            if (error.length() > 0) {
                builder.setMessage(error);
            } else {
                builder.setMessage("Scan Failed, Please Try Again");
            }
            AlertDialog alert1 = builder.create();
            alert1.show();
        }
    }
}
