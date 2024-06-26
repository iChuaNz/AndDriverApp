package sg.com.commute_solutions.bustracker.fragments;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.acs.bluetooth.BluetoothReader;
import com.acs.bluetooth.BluetoothReaderGattCallback;
import com.acs.bluetooth.BluetoothReaderManager;
import com.acs.smartcard.Reader;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import sg.com.commute_solutions.bustracker.R;
import sg.com.commute_solutions.bustracker.adapters.RouteAdapter;
import sg.com.commute_solutions.bustracker.common.Constants;
import sg.com.commute_solutions.bustracker.common.Preferences;
import sg.com.commute_solutions.bustracker.data.Adhoc;
import sg.com.commute_solutions.bustracker.data.Job;
import sg.com.commute_solutions.bustracker.data.RoutePoint;
import sg.com.commute_solutions.bustracker.data.Passenger;
import sg.com.commute_solutions.bustracker.data.RoutePath;
import sg.com.commute_solutions.bustracker.databinding.ActivityMapsBinding;
import sg.com.commute_solutions.bustracker.service.GoogleServices;
import sg.com.commute_solutions.bustracker.util.CEPAS.CEPASUtil;
import sg.com.commute_solutions.bustracker.util.GPSUtil;
import sg.com.commute_solutions.bustracker.util.ObjectSerializer;
import sg.com.commute_solutions.bustracker.util.StringUtil;
import sg.com.commute_solutions.bustracker.webservices.WebServiceTask;
import sg.com.commute_solutions.bustracker.webservices.WebServiceUtils;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
//        SensorEventListener,
        LocationListener,
        NfcAdapter.ReaderCallback {

    private static final String LOG_TAG = MapsActivity.class.getSimpleName();
    private static final LatLng SINGAPORE = new LatLng(1.3520830, 103.8198360);
    private static final Double MAXDISTANCEFORCHECKING = 125.0;

    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private MapFragment mMapFragment;
    private GoogleMap mMap;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private LatLng currentlatLng;
    private HandlerThread mHandlerThread;
    private static Handler mHandler;
//    private float mDeclination;
//    private float[] mRotationMatrix = new float[16];

//    private SensorManager mSensorManager;
//    private Sensor mAccelerometer;

    private Context context;
    private SharedPreferences prefs;
    private PendingIntent mPermissionIntent;
    private Bitmap bmBluetooth;
    private Resources resources;
    private ProcessEzlink ezlink;

    private JSONObject obj, jsonObject;
    private PassengersInfoTask mPassengersInfoTask = null;
    private LocationTask mLocationTask = null;
    private AttendanceTask mAttendanceTask = null;
    private StartTripTask mStartTripTask = null;
    private EndTripTask mEndTripTask = null;
    private EndTripRouteTask mEndTripRouteTask = null;

    //    private AlertDialog loadingScreen;
//    private Button btnAddtoTodayList;

    private NfcAdapter mNfcAdapter;
    private boolean useExternalNFC = false;
    private boolean useInternalNFC = false;
    private boolean useUsbReader = false;
    private boolean isTodaySelected = true;

    private String message;
    private boolean isSchoolBus;
    private boolean isDropOff = false;
    private boolean followUser = true;
    private String fourDigitCode = "";
    private String codeName = "";
    private int routeID = 0;
    private String remarks = "";
    private String pocContactNo = "";
    private ArrayList<Boolean> isDropOffArray;

//    private int maxVolume, sound;
//    private SoundPool soundPool;
    private boolean enableInternalNFC, enableExternalNFC;
    private boolean isShareTransport, isAdhoc;

    private Adhoc adhocJob;

//    private final String[][] techList = new String[][] {
//            new String[] {
//                    NfcA.class.getName(),
//                    NfcB.class.getName(),
//                    NfcF.class.getName(),
//                    NfcV.class.getName(),
//                    IsoDep.class.getName(),
//                    MifareClassic.class.getName(),
//                    MifareUltralight.class.getName(), Ndef.class.getName()
//            }
//    };
    private float[] distance = new float[3];
    private DialogInterface dialogInterface;

    //storing locational data to poll location
    private int pollingInterval, proximityInterval, sendPassengerListInterval;
    private double mLongitude, mLatitude, mAltitude;
    private float mAccuracy, mSpeed;
    private String mDate;

    private ArrayList<Double> aLongitude, aLatitude, aAltitude;
    private ArrayList<Float> aAccuracy, aSpeed;
    private ArrayList<String> aDate;
    private ArrayList<Passenger> passengers, boardingList, boardListNoClearing;
    private ArrayList<RoutePoint> routePoints, proxmityCheck;
    private ArrayList<RoutePath> routePaths;
    private ArrayList<LatLng> routeCoordinatesList, jobMarkerList, proximityList;
    private String lang, authToken, NFCIndicator, currentDate, lastUpdatedDate;
    private PolylineOptions busRoute;
    private MarkerOptions jobMarker, timeMarker, mCurrentPositionLogo;
    private Marker currentPosition;
//    private CircleOptions mCircle;
    private final ContentValues authenticationToken = new ContentValues();
    private ContentValues contentValues;
    private final ArrayList<ContentValues> passengerRecords = new ArrayList<>();

    private byte[] masterKey;
    private boolean isAuthenticated = false;
    private boolean hasSentApdu1 = false;
    private int searchingDeviceCount = 0;
    private BluetoothReader mBluetoothReader;
    private BluetoothReaderManager mBluetoothReaderManager;
    private BluetoothReaderGattCallback mGattCallback;
    private BluetoothGatt mBluetoothGatt;

    private UsbManager mManager;
    private Reader mReader;
    private ArrayAdapter<String> mReaderAdapter;
    private final UsbDevice[] mDevice = new UsbDevice[1];
    private String deviceName;

    private static final int CHECK_BLUETOOTH_ON = 1;
    private static final int CHECK_NFC_ON = 2;
    private static final int CHECK_GPS_ON = 3;
    private static final int REQUEST_CHECK_SETTINGS = 4;

    private int exitCount = 0;

//    GoogleServices mLocationService = new GoogleServices();
//    Intent mServiceIntent;
    Activity mActivity;
    ActivityMapsBinding activityMapsBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMapsBinding = ActivityMapsBinding.inflate(getLayoutInflater());

        setContentView(activityMapsBinding.getRoot());
        // clear FLAG_TRANSLUCENT_STATUS flag:
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorActivated));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        turnOnLocationSetting();

        prefs = this.getSharedPreferences(Constants.SHARE_PREFERENCE_PACKAGE, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Preferences.CURRENTACTIVITY, "map");
        editor.apply();
        context = this;
        mHandlerThread = new HandlerThread("HandlerThread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        pollingInterval = 5000;
        proximityInterval = 1000;
        sendPassengerListInterval = 300000;
        mActivity = this;

        //Get nfc usage
        NFCIndicator = prefs.getString(Preferences.NFCINDICATOR, "");
        if (NFCIndicator.equalsIgnoreCase("internal")) {
            useInternalNFC = true;
        } else if (NFCIndicator.equalsIgnoreCase("external")) {
            useExternalNFC = true;
        } else if (NFCIndicator.equalsIgnoreCase("usb")) {
            useUsbReader = true;
        }

        //For storing fields when offline
        initialiseMapValues();

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        editor.putString(Preferences.CAN_ID, "");
        editor.apply();

        authToken = StringUtil.deNull(prefs.getString(Preferences.AUTH_TOKEN, ""));
        if (authToken.isEmpty()) {
            resetApp();
        } else {
            authenticationToken.put(Constants.TOKEN, authToken);
        }

        lang = prefs.getString(Preferences.LANGUAGE, "");
        if (lang != null && lang.isEmpty() || lang.equalsIgnoreCase("")) {
            lang = Constants.ENGLISH;
            editor.putString(Preferences.LANGUAGE, Constants.ENGLISH);
            editor.apply();
        }

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mMapFragment = MapFragment.newInstance(new GoogleMapOptions().mapToolbarEnabled(true));
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.map, mMapFragment);
        fragmentTransaction.commit();
        mMapFragment.getMapAsync(this);

        if (mMapFragment == null) {
            mMapFragment = MapFragment.newInstance();
            fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.map, mMapFragment);
            fragmentTransaction.commit();
            mMapFragment.getMapAsync(this);
        }

//        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
//        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

//        mCircle = new CircleOptions();
//        mCircle.radius(125.0);
//        mCircle.strokeColor(Color.TRANSPARENT);
//        mCircle.fillColor(Color.TRANSPARENT);

        resources = this.getResources();
        dialogInterface = new DialogInterface() {
            @Override
            public void cancel() {
                //do nothing
            }

            @Override
            public void dismiss() {
                //do nothing
            }
        };

        mCurrentPositionLogo = new MarkerOptions();

        //Connection light for external bluetooth device
        if (useInternalNFC) {
            activityMapsBinding.ivNfcConnectionStatus.setImageResource(android.R.color.transparent);
        }
        if (useExternalNFC) {
            bmBluetooth = BitmapFactory.decodeResource(resources, R.drawable.signal_red);
            activityMapsBinding.ivNfcConnectionStatus.setImageBitmap(bmBluetooth);
            BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
            String deviceAddress = prefs.getString(Preferences.BLUETOOTH_DEVICE_ADDRESS, "");
            try {
                String masterKeyHexString = StringUtil.toHexString(Constants.DEFAULT_MASTER_KEY.getBytes(StandardCharsets.UTF_8));
                masterKey = StringUtil.getText2HexBytes(masterKeyHexString);
            } catch (Exception e){
                e.printStackTrace();
            }

            mBluetoothReaderManager = new BluetoothReaderManager();
            mGattCallback = new BluetoothReaderGattCallback();

            final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            mBluetoothGatt = device.connectGatt(this, false, mGattCallback);

            while (mBluetoothReader == null) {
                try {
                    mGattCallback.setOnConnectionStateChangeListener((bluetoothGatt, state, newState) -> {
                        if (newState == BluetoothProfile.STATE_CONNECTED) {
                            /* Detect the connected reader. */
                            mBluetoothReaderManager.detectReader(bluetoothGatt, mGattCallback);
                            mBluetoothReaderManager.setOnReaderDetectionListener(bluetoothReader -> mBluetoothReader = bluetoothReader);
                        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                            mBluetoothReader = null;
                            /*
                             * Release resources occupied by Bluetooth
                             * GATT client.
                             */
                            if (mBluetoothGatt != null) {
                                mBluetoothGatt.close();
                                mBluetoothGatt = null;
                            }
                        }
                    });
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }

                searchingDeviceCount++;
                if (searchingDeviceCount == 10) {
                    break;
                }
            }

            try {
                searchingDeviceCount = 0;
                if (mBluetoothReader != null) {
                    setListener(mBluetoothReader);
                    try {
                        mBluetoothReader.enableNotification(true);
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    if (mBluetoothReader.authenticate(masterKey)) {
                        Log.v(LOG_TAG, "Authenticated");
                        if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
                            builder.setMessage(Constants.DEVICE_READY_TO_PAIR)
                                    .setTitle("Message")
                                    .setCancelable(false)
                                    .setPositiveButton("Understood", (dialog, which) -> dialog.dismiss()).show();
                        } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {
                            builder.setMessage(Constants.DEVICE_READY_TO_PAIR_CH)
                                    .setCancelable(false)
                                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss()).show();
                        }
                    } else {
                        Log.v(LOG_TAG, "Unable to authenticate");
                    }
                } else {
                    Log.v(LOG_TAG, "No reader detected");
                }
            } catch (Exception e) {
                Log.v(LOG_TAG, "Unable to locate device!");
            }
            activityMapsBinding.ivNfcConnectionStatus.setOnClickListener(v -> connectReader());
        }
        if (useUsbReader) {
            activityMapsBinding.ivNfcConnectionStatus.setImageResource(android.R.color.transparent);
            if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
                builder.setMessage(Constants.USB_ALERT)
                        .setTitle("Attention!")
                        .setCancelable(false)
                        .setPositiveButton("Ok", (dialog, which) -> {
                            dialog.dismiss();
                            readerSelectionDialog().show();
                            readerSelectionDialog().setCanceledOnTouchOutside(false);
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {
                builder.setMessage(Constants.USB_ALERT_CH)
                        .setCancelable(false)
                        .setPositiveButton("Ok", (dialog, which) -> {
                            dialog.dismiss();
                            readerSelectionDialog().show();
                            readerSelectionDialog().setCanceledOnTouchOutside(false);
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }

            // Get USB manager
            mManager = (UsbManager) getSystemService(Context.USB_SERVICE);

            // Initialize reader
            mReader = new Reader(mManager);
            int preferredProtocols = Reader.PROTOCOL_UNDEFINED;
            preferredProtocols |= Reader.PROTOCOL_T0;
            preferredProtocols |= Reader.PROTOCOL_T1;
            // Register receiver for USB permission
            mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(Constants.ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);
            IntentFilter filter = new IntentFilter();
            filter.addAction(Constants.ACTION_USB_PERMISSION);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
            registerReceiver(mReceiver, filter);

            final int finalPreferredProtocols = preferredProtocols;
            mReader.setOnStateChangeListener((slotNum, prevState, currState) -> {

                if (prevState < Reader.CARD_UNKNOWN || prevState > Reader.CARD_SPECIFIC) {
                    prevState = Reader.CARD_UNKNOWN;
                }

                if (currState < Reader.CARD_UNKNOWN || currState > Reader.CARD_SPECIFIC) {
                    currState = Reader.CARD_UNKNOWN;
                }

                // Create output string
                final String outputString = "Slot " + slotNum + ": " + Constants.stateStrings[prevState] + " -> " + Constants.stateStrings[currState];
                Log.d(LOG_TAG, outputString);

                if (currState == 2) {
                    runOnUiThread(() -> {
                        try {
                            //Power up Reader
                            mReader.power(0, 2);
                            mReader.setProtocol(0, finalPreferredProtocols);

                            // Transmit APDU
                            byte[] response = new byte[300];
                            byte[] command = StringUtil.hexString2Bytes(Constants.APDU1);
                            mReader.transmit(0, command, command.length, response, response.length);
                            command = StringUtil.hexString2Bytes(Constants.APDU2);
                            mReader.transmit(0, command, command.length, response, response.length);
                            String result = StringUtil.toHexString(response);
                            result = result.replaceAll("\\s+", "");
                            if (result.length() > 32) {
                                String canId = result.substring(16, 32);
                                checkPassengerList(canId, true);
                            } else {
                                String canId = "Error";
                                checkPassengerList(canId, true);
                            }
                        } catch (Exception e) {
                            Log.d(LOG_TAG, e.toString());
                        }
                    });
                }
            });
        }

        activityMapsBinding.ivNfcConnectionStatus.invalidate();

        ezlink = new ProcessEzlink();

        if (boardingList == null || boardingList.size() == 0) {
            boardingList = new ArrayList<>();
            isDropOffArray = new ArrayList<>();
        }

        try {
            boardListNoClearing = (ArrayList<Passenger>) ObjectSerializer.deserialize(prefs.getString(Preferences.PASSENGERLIST, ObjectSerializer.serialize(new ArrayList<Passenger>())));
        } catch (Exception e) {
            boardListNoClearing = new ArrayList<>();
        }

        if (boardListNoClearing == null) {
            boardListNoClearing = new ArrayList<>();
        }

        activityMapsBinding.btnMaps.setOnClickListener(view -> {
            if(routePoints.size() > 0){
                StringBuilder stringBuilder = new StringBuilder();
                for (RoutePoint point : routePoints) {
                    stringBuilder.append(point.getLatitude()).append(",").append(point.getLongitude()).append("+to:");
                }
                stringBuilder.substring(0,stringBuilder.length() - 4);
                Uri mapsUri = Uri.parse("http://maps.google.com/maps?daddr=" + stringBuilder);
                Intent mapsIntent = new Intent(Intent.ACTION_VIEW, mapsUri);
                mapsIntent.setPackage("com.google.android.apps.maps");

                try {
                    startActivity(mapsIntent);
                } catch (ActivityNotFoundException ex) {
                    toast("Google Maps application not installed");
                }
            } else {
                toast("Route point is empty, please reload job");
            }
        });

        activityMapsBinding.btnPhone.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            if (isAdhoc){
                if (pocContactNo.isEmpty()){
                    toast("Adhoc job has no contact number");
                    return;
                } else {
                    callIntent.setData(Uri.parse("tel:" + pocContactNo));
                }
            } else {
                callIntent.setData(Uri.parse("tel:67340147"));
            }
            if (ActivityCompat.checkSelfPermission(MapsActivity.this,
                    Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MapsActivity.this,
                        new String[]{Manifest.permission.CALL_PHONE},
                        1);
                return;
            }
            startActivity(callIntent);
        });

        editor.putBoolean(Preferences.ON_OFF_TRACKER, true);
        editor.apply();

        activityMapsBinding.btnReload.setOnClickListener(view -> loadJob());

        activityMapsBinding.btnEndRoute.setOnSwipeListener(() ->
                builder.setMessage(Constants.END_ROUTE_CONFIRMATION)
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    mEndTripRouteTask = new EndTripRouteTask(String.valueOf(routeID));
                    mEndTripRouteTask.execute((Void) null);
                })
                .setNegativeButton("No", (dialogInterface, i) -> {
                    //do nothing
                    activityMapsBinding.btnEndRoute.showResultIcon(false, true);
                })
                .show());

        activityMapsBinding.btnCamera.setOnClickListener(v -> {
            if (followUser) {
                followUser = false;
                activityMapsBinding.btnCamera.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
                    activityMapsBinding.btnCamera.setText(R.string.camera_manual);
                    toast(getResources().getString(R.string.camera_manual));
                } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {
                    activityMapsBinding.btnCamera.setText(R.string.camera_manual_ch);
                    toast(getResources().getString(R.string.camera_manual_ch));
                }
            } else {
                followUser = true;
                activityMapsBinding.btnCamera.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorBlue)));
                if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
                    activityMapsBinding.btnCamera.setText(R.string.camera_auto);
                    toast(getResources().getString(R.string.camera_auto));
                } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {
                    activityMapsBinding.btnCamera.setText(R.string.camera_auto_ch);
                    toast(getResources().getString(R.string.camera_auto_ch));
                }
            }
        });

        //To retrieve passenger list
        loadJob();

//        boolean isFirstTime = prefs.getBoolean(Preferences.IS_FIRST_TIME, false);
//        if (isFirstTime) {
//            editor.putBoolean(Preferences.IS_FIRST_TIME, false);
//            editor.putBoolean(Preferences.ON_OFF_TRACKER, true);
//            editor.apply();
//            Intent intent = new Intent(context, SettingsActivity.class);
//            startActivity(intent);
//        }

        activityMapsBinding.fabSettings.setOnClickListener(v -> {
            sendPassengerListForToday();
            Intent intent = new Intent(MapsActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        activityMapsBinding.fabSchedule.setOnClickListener(v -> {
            if(isTodaySelected){
                activityMapsBinding.btnRouteToday.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorActivated)));
                activityMapsBinding.btnRouteTmr.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorIdle)));
            } else {
                activityMapsBinding.btnRouteToday.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorIdle)));
                activityMapsBinding.btnRouteTmr.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorActivated)));
            }
            JobScheduleTask mJobScheduleTask = new JobScheduleTask();
            mJobScheduleTask.execute((Void) null);
        });

        startMyService();
    }

    private void turnOnLocationSetting() {
        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(3000)
                .setFastestInterval(1000)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());

        result.addOnCompleteListener(task -> {
            try {
                LocationSettingsResponse response = task.getResult(ApiException.class);
                // All location settings are satisfied. The client can initialize location
                // requests here.
            } catch (ApiException exception) {
                switch (exception.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the
                        // user a dialog.
                        try {
                            // Cast to a resolvable exception.
                            ResolvableApiException resolvable = (ResolvableApiException) exception;
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            resolvable.startResolutionForResult(
                                    MapsActivity.this,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        } catch (ClassCastException e) {
                            // Ignore, should be an impossible error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        if(activityMapsBinding.flPopupWindow.getVisibility() == View.VISIBLE){
            activityMapsBinding.flPopupWindow.setVisibility(View.GONE);
        } else {
            exitCount++;
            if (exitCount > 1) {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.settings) {
//            Intent intent = new Intent(MapsActivity.this, SettingsActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            startActivity(intent);
//        }
        if (id == android.R.id.home) {
            return false;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.getUiSettings().setMapToolbarEnabled(true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SINGAPORE, 12));
        enableLocationPermission();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setFastestInterval(500); // 1/2 sec to poll
        if (haveNetworkConnection()) {
            mLocationRequest.setInterval(1000); //1 second
        } else {
            mLocationRequest.setInterval(5000); //5 seconds
        }

        buildGoogleApiClient();
        mGoogleApiClient.connect();

        drawMarkersAndRoutes();

        if (!isAdhoc) {
            startCheckingProximity();
        }
        startPollingLocation();

        if (useInternalNFC) {
            checkCEPASCard();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableLocationPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);

                // LOCATION_PERMISSION_REQUEST_CODE is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else if (mMap != null) {
            //Comment away to use custom location icon
//            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            enableLocationPermission();
        } else {
            // Display the missing permission error dialog when the fragments resume.
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, new IntentFilter(GoogleServices.str_receiver));
//        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        startPollingLocation();
        startCheckingProximity();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
//        mSensorManager.unregisterListener(this);
        stopPollingLocation();
        stopCheckingProximity();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (useUsbReader) {
            // Unregister receiver
            unregisterReceiver(mReceiver);
        }
        stopPollingLocation();
        stopCheckingProximity();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
            toast("Initializing...");
        } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {
            toast("地图启动中。。。");
        }

        Location mLastLocation = null;
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            enableLocationPermission();
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }
        if (mLastLocation != null) {
            Log.d(LOG_TAG, "Latitude: " + mLastLocation.getLatitude() + " , Longitude: " + mLastLocation.getLatitude());
            currentlatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            //zoom to current position:
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(currentlatLng).zoom(17).build();
            mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f));
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
//        setNFCSettings(true);
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
            toast(Constants.ON_CONNECTION_SUSPENDED);
        } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {
            toast(Constants.ON_CONNECTION_SUSPENDED_CH);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
            toast(Constants.ON_CONNECTION_FAILED);
        } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {
            toast(Constants.ON_CONNECTION_FAILED_CH);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

//        GeomagneticField field = new GeomagneticField(
//                (float)location.getLatitude(),
//                (float)location.getLongitude(),
//                (float)location.getAltitude(),
//                System.currentTimeMillis()
//        );

        // getDeclination returns degrees
//        mDeclination = field.getDeclination();

        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy, H:mm:ss", Locale.ENGLISH);
        Calendar cal = Calendar.getInstance();
        lastUpdatedDate = df.format(cal.getTime());
        df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH);
        cal.setTime(new Date());
        cal.add(Calendar.HOUR_OF_DAY, 8);
        final Date adjustedDate = cal.getTime();
        final String date = df.format(adjustedDate);

        mLongitude = location.getLongitude();
        mLatitude = location.getLatitude();
        mAltitude = location.getAltitude();
        mAccuracy = location.getAccuracy();
        mSpeed = location.getSpeed();
        mDate = date;
        float bearing = location.getBearing();

        if (currentPosition != null) {
            currentPosition.remove();
        }
//            mCurrentPositionLogo.position(currentlatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.bus)).rotation(bearing);
        if (currentlatLng != null) {
            mCurrentPositionLogo.position(currentlatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.bus));
            currentPosition = mMap.addMarker(mCurrentPositionLogo);
        }

        if (followUser) {
            //zoom to current position

            currentlatLng = new LatLng(mLatitude, mLongitude);
            updateCameraBearing(mMap, bearing);
//                CameraPosition cameraPosition = new CameraPosition.Builder().target(currentlatLng).bearing(bearing + 530).zoom(16).build();
//                mMap.animateCamera(CameraUpdateFactory.zoomTo(16.0f));
//                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
//        setNFCSettings(false);
    }

    private void updateCameraBearing(GoogleMap googleMap, float bearing) {
        if (googleMap == null) {
            return;
        }
        CameraPosition camPos = new CameraPosition.Builder(mMap.getCameraPosition()).target(currentlatLng).bearing(bearing).zoom(16).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));
    }

//    @Override
//    public void onAccuracyChanged(Sensor sensor, int accuracy) {
//    }
//
//    @Override
//    public void onSensorChanged(SensorEvent event) {
//        if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
//            SensorManager.getRotationMatrixFromVector(mRotationMatrix , event.values);
//            float[] orientation = new float[3];
//            SensorManager.getOrientation(mRotationMatrix, orientation);
//            float bearing = (float) Math.toDegrees(orientation[0]) + mDeclination;
//            updateCamera(bearing);
//        }
//    }

//    private void updateCamera(float bearing) {
//        CameraPosition oldPos = mMap.getCameraPosition();
//
//        CameraPosition pos = CameraPosition.builder(oldPos).bearing(bearing).build();
//        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos));
//    }

//    private int convertMetersToPixels(double lat, double lng, double radiusInMeters) {
//
//        double lat1 = radiusInMeters / Constants.EARTH_RADIUS;
//        double lng1 = radiusInMeters / (Constants.EARTH_RADIUS * Math.cos((Math.PI * lat / 180)));
//
//        double lat2 = lat + lat1 * 180 / Math.PI;
//        double lng2 = lng + lng1 * 180 / Math.PI;
//
//        Point p1 = mMap.getProjection().toScreenLocation(new LatLng(lat, lng));
//        Point p2 = mMap.getProjection().toScreenLocation(new LatLng(lat2, lng2));
//
//        return Math.abs(p1.x - p2.x);
//    }

    private void drawMarkersAndRoutes() {
        if (routePoints != null && routePoints.size() > 0) {
            isShareTransport = false;
            for(RoutePoint point : routePoints){
                if (point.getNumberOfPassengers() > 0) {
                    isShareTransport = true;
                    break;
                }
            }

            busRoute = new PolylineOptions();
            jobMarker = new MarkerOptions();
            timeMarker = new MarkerOptions();

            if (!isAdhoc) {
                //Route
                routeCoordinatesList = new ArrayList<>();
                for (int i = 0; i < routePaths.size(); i++) {
                    routeCoordinatesList.add(new LatLng(routePaths.get(i).getLatitude(), routePaths.get(i).getLongitude()));
                }
                busRoute.addAll(routeCoordinatesList).width(15.0f).color(Color.RED);
                mMap.addPolyline(busRoute);
            }

            //Marker
            jobMarkerList = new ArrayList<>();
            for (int i = 0; i < routePoints.size(); i++) {
                jobMarkerList.add(new LatLng(routePoints.get(i).getLatitude(), routePoints.get(i).getLongitude()));
            }
            proximityList = jobMarkerList;

            if (isShareTransport) {
                for (int i = 0; i < jobMarkerList.size(); i++) {
//                    mCircle.center((new LatLng(jobMarkerList.get(i).latitude,jobMarkerList.get(i).longitude)));

                    if (routePoints.get(i).getType() == 1) {
                        jobMarker.position(jobMarkerList.get(i))
                                .title(routePoints.get(i).getPointName())
                                .snippet(routePoints.get(i).getTime())
                                .icon(BitmapDescriptorFactory.fromBitmap
                                        (writeNoOfPassengersOnMarker(R.drawable.blue_marker, "" + routePoints.get(i).getNumberOfPassengers())));

                    } else if (routePoints.get(i).getType() == 0) {
                        jobMarker.position(jobMarkerList.get(i))
                                .title(routePoints.get(i).getPointName())
                                .snippet(routePoints.get(i).getTime())
                                .icon(BitmapDescriptorFactory.fromBitmap
                                        (writeNoOfPassengersOnMarker(R.drawable.green_marker, "" + routePoints.get(i).getNumberOfPassengers())));
                    } else {
                        if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
                            toast(Constants.GENERIC_ERROR_MSG);
                        } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {
                            toast(Constants.GENERIC_ERROR_MSG_CH);
                        }
                        Intent intent = new Intent(MapsActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }

                    timeMarker.position((jobMarkerList.get(i)))
                            .icon(BitmapDescriptorFactory.fromBitmap(writeTimeOnMarker(routePoints.get(i).getTime())));

                    mMap.addMarker(timeMarker);
                    mMap.addMarker(jobMarker);
//                    mMap.addCircle(mCircle);
                }
            } else {
                for (int i = 0; i < jobMarkerList.size(); i++) {
                    if (routePoints.get(i).getType() == 1) {
                        jobMarker.position(jobMarkerList.get(i))
                                .title(routePoints.get(i).getPointName())
                                .snippet(routePoints.get(i).getTime())
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.blue_marker));
                    } else if (routePoints.get(i).getType() == 0) {
                        jobMarker.position(jobMarkerList.get(i))
                                .title(routePoints.get(i).getPointName())
                                .snippet(routePoints.get(i).getTime())
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.green_marker));
                    } else {
                        if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
                            toast(Constants.GENERIC_ERROR_MSG);
                        } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {
                            toast(Constants.GENERIC_ERROR_MSG_CH);
                        }
                        Intent intent = new Intent(MapsActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }

                    String markerTime = routePoints.get(i).getTime();
                    timeMarker.position((jobMarkerList.get(i)))
                            .icon(BitmapDescriptorFactory.fromBitmap(writeTimeOnMarker(markerTime)));

                    if (!markerTime.equalsIgnoreCase("")) {
                        mMap.addMarker(timeMarker);
                    }
                    mMap.addMarker(jobMarker);
                }
            }
        }
    }

    private Bitmap writeNoOfPassengersOnMarker(int drawableId, String text) {

        Bitmap bm = BitmapFactory.decodeResource(getResources(), drawableId)
                .copy(Bitmap.Config.ARGB_8888, true);

        Typeface tf = Typeface.create("Helvetica", Typeface.BOLD);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setTypeface(tf);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(convertToPixels(context, 25));

        Rect textRect = new Rect();
        paint.setTextSize(1000000.0f);
        paint.getTextBounds(text, 0, text.length(), textRect);

        Canvas canvas = new Canvas(bm);

        //If the text is bigger than the canvas , reduce the font size
        if (textRect.width() >= (canvas.getWidth() - 4))     //the padding on either sides is considered as 4, so as to appropriately fit in the text
            paint.setTextSize(convertToPixels(context, 25));        //Scaling needs to be used for different dpi's

        //Calculate the positions
        int xPos = (canvas.getWidth() / 2) - 2;     //-2 is for regulating the x position offset
        float yPos = (float) (canvas.getHeight() / 2.5);

        canvas.drawText(text, xPos, yPos, paint);

        return bm;
    }

    private Bitmap writeTimeOnMarker(String text) {

        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.black)
                .copy(Bitmap.Config.ARGB_8888, true);

        Typeface tf = Typeface.create("Helvetica", Typeface.BOLD);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTypeface(tf);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(convertToPixels(context, 25));

        Rect textRect = new Rect();
        paint.setTextSize(1000000.0f);
        paint.getTextBounds(text, 0, text.length(), textRect);

        Canvas canvas = new Canvas(bm);

        //If the text is bigger than the canvas , reduce the font size
        if (textRect.width() >= (canvas.getWidth() - 4))     //the padding on either sides is considered as 4, so as to appropriately fit in the text
            paint.setTextSize(convertToPixels(context, 15));        //Scaling needs to be used for different dpi's

        //Calculate the positions
        int xPos = (canvas.getWidth() / 2) - 2;     //-2 is for regulating the x position offset

        //"- ((paint.descent() + paint.ascent()) / 2)" is the distance from the baseline to the center.
//        int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2)); <- middle
        int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2)) - (canvas.getHeight() / 3);

        canvas.drawText(text, xPos, yPos, paint);

        return bm;
    }

    public static int convertToPixels(Context context, int nDP) {
        final float conversionScale = context.getResources().getDisplayMetrics().density;

        return (int) ((nDP * conversionScale) + 0.5f);

    }

    public void sendCurrentLocation(double longitude, double latitude, double altitude,
                                    float accuracy, float speed, String date) {
        boolean showToastMessage = prefs.getBoolean(Preferences.SHOW_MESSAGE, Boolean.FALSE);

        if (mLocationRequest != null) {
            mLocationTask = new LocationTask(longitude, latitude, altitude, accuracy, speed, date);
            mLocationTask.execute((Void) null);
            if (showToastMessage) {
                if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
                    toast(Constants.ON_UPDATE_LOCATION);
                } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {
                    toast(Constants.ON_UPDATE_LOCATION_CH);
                }
            }
        }
    }

    public void checkCEPASCard() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        mNfcAdapter.enableReaderMode(this, this, 287, null);
    }

    @Override
    public void onTagDiscovered(Tag tag) {
        Log.v(LOG_TAG, "Tag found");
        ezlink.execute(tag);
    }

    public class ProcessEzlink extends CEPASUtil {
        public void execute(Tag tag) {
            super.startProcess();
            DecryptedData decryptedData = new DecryptedData();
            decryptedData.execute(tag);
        }

        public class DecryptedData extends DecryptCepas {
            protected void onPostExecute(String processedCanId) {
                super.onPostExecute(processedCanId);
                String canId = getmCanId();
                checkPassengerList(canId, true);
            }
        }
    }

    Runnable pollLocation = new Runnable() {
        @Override
        public void run() {
            try {
                if (haveNetworkConnection()) {
                    if (mAccuracy > 0) {
                        sendCurrentLocation(mLongitude, mLatitude, mAltitude, mAccuracy, mSpeed, mDate);

                        int noOfLocationStored = aLongitude.size();
                        if (noOfLocationStored > 0) {
                            toast("Sending off stored locations while disconnected...");
                            for (int i = 0; i < noOfLocationStored; i++) {
                                sendCurrentLocation(aLongitude.get(i),
                                        aLatitude.get(i),
                                        aAltitude.get(i),
                                        aAccuracy.get(i),
                                        aSpeed.get(i),
                                        aDate.get(i));
                            }
                            initialiseMapValues();
                        }
                    }
                } else {
                    if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
                        toast(Constants.NO_CONNECTION_STORE_DATA);
                    } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {
                        toast(Constants.NO_CONNECTION_STORE_DATA_CH);
                    }
                    aLongitude.add(mLongitude);
                    aLatitude.add(mLatitude);
                    aAltitude.add(mAltitude);
                    aAccuracy.add(mAccuracy);
                    aSpeed.add(mSpeed);
                    aDate.add(mDate);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mHandler.postDelayed(pollLocation, pollingInterval);
            }
        }
    };

    Runnable checkProximity = new Runnable() {
        @Override
        public void run() {
            try {
                if (proximityList != null) {
                    for (int i = 0; i < proximityList.size(); i++) {
                        Location.distanceBetween(proximityList.get(i).latitude,
                                proximityList.get(i).longitude, mLatitude,
                                mLongitude, distance);

                        int derivedDistance = Math.round(distance[0]);
                        //Log.d("Test", "Current Distance from marker " + i + ": " + derivedDistance);
                        if (derivedDistance < MAXDISTANCEFORCHECKING) {
                            //Somehow setting it once doesnt work. Dont ask why.
                            String toCheckProximity = prefs.getString(Preferences.TO_CHECK_PROXIMITY, "");
                            Log.d("Test", toCheckProximity);
                            if (StringUtil.deNull(toCheckProximity).isEmpty() || toCheckProximity.equalsIgnoreCase("Yes")) {
                                final SharedPreferences.Editor editor = prefs.edit();
                                editor.putString(Preferences.TO_CHECK_PROXIMITY, "No");
                                editor.apply();
                                try {
                                    int noOfPassengers = proxmityCheck.get(i).getNumberOfPassengers();
                                    if (noOfPassengers == 0) {
                                        final MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.zero);
                                        proximityList.remove(i);
                                        proxmityCheck.remove(i);
                                        runOnUiThread(mediaPlayer::start);
                                    } else if (noOfPassengers == 1) {
                                        final MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.one);
                                        activityMapsBinding.ivPassengerResult.setImageResource(R.drawable.number_one_in_a_circle);
                                        activityMapsBinding.ivPassengerResult.setVisibility(View.VISIBLE);
                                        fadeOutAndHideView(activityMapsBinding.ivPassengerResult);
                                        proximityList.remove(i);
                                        proxmityCheck.remove(i);
                                        runOnUiThread(mediaPlayer::start);
                                    } else if (noOfPassengers == 2) {
                                        final MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.two);
                                        activityMapsBinding.ivPassengerResult.setImageResource(R.drawable.number_two_in_a_circle);
                                        activityMapsBinding.ivPassengerResult.setVisibility(View.VISIBLE);
                                        fadeOutAndHideView(activityMapsBinding.ivPassengerResult);
                                        proximityList.remove(i);
                                        proxmityCheck.remove(i);
                                        runOnUiThread(mediaPlayer::start);
                                    } else if (noOfPassengers == 3) {
                                        final MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.three);
                                        activityMapsBinding.ivPassengerResult.setImageResource(R.drawable.number_three_in_a_circle);
                                        activityMapsBinding.ivPassengerResult.setVisibility(View.VISIBLE);
                                        fadeOutAndHideView(activityMapsBinding.ivPassengerResult);
                                        proximityList.remove(i);
                                        proxmityCheck.remove(i);
                                        runOnUiThread(mediaPlayer::start);
                                    } else if (noOfPassengers == 4) {
                                        final MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.four);
                                        activityMapsBinding.ivPassengerResult.setImageResource(R.drawable.number_four_in_circular_button);
                                        activityMapsBinding.ivPassengerResult.setVisibility(View.VISIBLE);
                                        fadeOutAndHideView(activityMapsBinding.ivPassengerResult);
                                        proximityList.remove(i);
                                        proxmityCheck.remove(i);
                                        runOnUiThread(mediaPlayer::start);
                                    } else if (noOfPassengers == 5) {
                                        final MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.five);
                                        activityMapsBinding.ivPassengerResult.setImageResource(R.drawable.number_five_in_circular_button);
                                        activityMapsBinding.ivPassengerResult.setVisibility(View.VISIBLE);
                                        fadeOutAndHideView(activityMapsBinding.ivPassengerResult);
                                        proximityList.remove(i);
                                        proxmityCheck.remove(i);
                                        runOnUiThread(mediaPlayer::start);
                                    } else if (noOfPassengers == 6) {
                                        final MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.six);
                                        activityMapsBinding.ivPassengerResult.setImageResource(R.drawable.number_six_in_a_circle);
                                        activityMapsBinding.ivPassengerResult.setVisibility(View.VISIBLE);
                                        fadeOutAndHideView(activityMapsBinding.ivPassengerResult);
                                        proximityList.remove(i);
                                        proxmityCheck.remove(i);
                                        runOnUiThread(mediaPlayer::start);
                                    } else if (noOfPassengers == 7) {
                                        final MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.seven);
                                        activityMapsBinding.ivPassengerResult.setImageResource(R.drawable.number_seven_in_a_circle);
                                        activityMapsBinding.ivPassengerResult.setVisibility(View.VISIBLE);
                                        fadeOutAndHideView(activityMapsBinding.ivPassengerResult);
                                        proximityList.remove(i);
                                        proxmityCheck.remove(i);
                                        runOnUiThread(mediaPlayer::start);
                                    } else if (noOfPassengers == 8) {
                                        final MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.eight);
                                        activityMapsBinding.ivPassengerResult.setImageResource(R.drawable.number_eight_in_a_circle);
                                        activityMapsBinding.ivPassengerResult.setVisibility(View.VISIBLE);
                                        fadeOutAndHideView(activityMapsBinding.ivPassengerResult);
                                        proximityList.remove(i);
                                        proxmityCheck.remove(i);
                                        runOnUiThread(mediaPlayer::start);
                                    } else if (noOfPassengers == 9) {
                                        final MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.nine);
                                        activityMapsBinding.ivPassengerResult.setImageResource(R.drawable.number_nine_in_a_circle);
                                        activityMapsBinding.ivPassengerResult.setVisibility(View.VISIBLE);
                                        fadeOutAndHideView(activityMapsBinding.ivPassengerResult);
                                        proximityList.remove(i);
                                        proxmityCheck.remove(i);
                                        runOnUiThread(mediaPlayer::start);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mHandler.postDelayed(checkProximity, proximityInterval);
                final SharedPreferences.Editor editor = prefs.edit();
                editor.putString(Preferences.TO_CHECK_PROXIMITY, "Yes");
                editor.apply();
            }
        }
    };

    Runnable sendPassengerDataPeriodically = new Runnable() {
        @Override
        public void run() {
            try {
                sendPassengerListForToday();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mHandler.postDelayed(sendPassengerDataPeriodically, sendPassengerListInterval);
            }
        }
    };

    private void isPassengerFound(boolean isFound, String name, String gender, String canId) {
        final MediaPlayer mediaPlayer;
        String textToShow;

        try {
            if (isFound) {
                activityMapsBinding.ivPassengerResult.setImageResource(R.drawable.ok);
                textToShow = name + ", " + gender;
                mediaPlayer = MediaPlayer.create(this, R.raw.correct);
            } else {
                if (canId.contains("Error")) {
                    activityMapsBinding.ivPassengerResult.setImageResource(R.drawable.unknown);
                    textToShow = "Unable to detect card. Please tap again.\n请再次打卡!";
                    mediaPlayer = MediaPlayer.create(this, R.raw.unknown);
                } else {
                    activityMapsBinding.ivPassengerResult.setImageResource(R.drawable.wrong);
                    textToShow = "Unable to identify passenger!\n无法验证乘客的身份!";
                    mediaPlayer = MediaPlayer.create(this, R.raw.failure);
                }
            }

            boolean showCanID = prefs.getBoolean(Preferences.SHOW_CAN_ID, Boolean.FALSE);
            if (showCanID) {
                textToShow += "\n" + canId;
            }

            activityMapsBinding.txtPassengerName.setText(textToShow);

            activityMapsBinding.ivPassengerResult.setVisibility(View.VISIBLE);
            fadeOutAndHideView(activityMapsBinding.ivPassengerResult);

            activityMapsBinding.txtPassengerName.setVisibility(View.VISIBLE);
            fadeOutAndHideView(activityMapsBinding.txtPassengerName);

            runOnUiThread(mediaPlayer::start);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkPassengerList(String canId, Boolean playNotification) {
        if (StringUtil.deNull(canId).isEmpty()) {
            canId = prefs.getString(Preferences.CAN_ID, "");
        }

        SharedPreferences.Editor editor = prefs.edit();
        boolean isFound = false;
        String name = "Unknown";
        String gender = "Unknown";
        int size = 0;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH);
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.HOUR_OF_DAY, 8);
        final Date adjustedDate = cal.getTime();
        currentDate = df.format(adjustedDate);

        if (passengers != null) {
            size = passengers.size();
        }
        for (int i = 0; i < size; i++) {
            String originalCanId = passengers.get(i).getEzlinkCanId();
            if (canId.equalsIgnoreCase("Error")) {
                //do nothing
            } else {
                if (originalCanId.equalsIgnoreCase(canId)) {
                    isFound = true;
                    name = passengers.get(i).getName();
                    gender = passengers.get(i).getGender();
                    Passenger onBoardPassenger = new Passenger(canId, name, gender, currentDate);
                    boardingList.add(onBoardPassenger);
                    isDropOffArray.add(isDropOff);
                    boardListNoClearing.add(onBoardPassenger);
                    try {
                        editor.putString(Preferences.PASSENGERLIST, ObjectSerializer.serialize(boardListNoClearing));
                    } catch (Exception e) {
                        //do nothing
                    }
                }
            }
        }

        if (!isFound) {
            Passenger onBoardPassenger = new Passenger(canId, currentDate);
            boardingList.add(onBoardPassenger);
            isDropOffArray.add(isDropOff);
            boardListNoClearing.add(onBoardPassenger);

            try {
                editor.putString(Preferences.PASSENGERLIST, ObjectSerializer.serialize(boardListNoClearing));
            } catch (Exception e) {
                //do nothing
            }
        }

        if (playNotification) {
            isPassengerFound(isFound, name, gender, canId);
        }
        editor.apply();
    }

    private void setBlinkingView(TextView view) {
        Animation blink = new AlphaAnimation(1, 0);
        blink.setDuration(1000);
        blink.setInterpolator(new LinearInterpolator());
        blink.setRepeatCount(Animation.INFINITE);
        blink.setRepeatMode(Animation.REVERSE);
        view.startAnimation(blink);
    }

    private void fadeOutAndHideView(final ImageView img) {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(5000);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                img.setVisibility(View.GONE);
            }

            public void onAnimationRepeat(Animation animation) {

            }

            public void onAnimationStart(Animation animation) {

            }
        });

        img.startAnimation(fadeOut);
    }

    private void fadeOutAndHideView(final TextView txt) {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(5000);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                txt.setVisibility(View.GONE);
            }

            public void onAnimationRepeat(Animation animation) {

            }

            public void onAnimationStart(Animation animation) {

            }
        });

        txt.startAnimation(fadeOut);
    }

    private void startPollingLocation() {
        pollLocation.run();
        if (!isAdhoc) {
            sendPassengerDataPeriodically.run();
        }
    }

    private void startMyService() {
        startPollingLocation();
//        mLocationService = new GoogleServices();
//        mServiceIntent = new Intent(getApplicationContext(), GoogleServices.class);
//        if (!LocationUtil.isMyServiceRunning(GoogleServices.class, mActivity)) {
//            startService(mServiceIntent);
//            Toast.makeText(
//                    mActivity,
//                    getString(R.string.service_start_successfully),
//                    Toast.LENGTH_SHORT
//            ).show();
//        } else {
//            Toast.makeText(
//                    mActivity,
//                    getString(R.string.service_already_running),
//                    Toast.LENGTH_SHORT
//            ).show();
//        }
    }

    private void stopMyService() {
        stopPollingLocation();
//        mLocationService = new GoogleServices();
//        mServiceIntent = new Intent(getApplicationContext(), GoogleServices.class);
//        if (LocationUtil.isMyServiceRunning(GoogleServices.class, mActivity)) {
//            stopService(mServiceIntent);
//            Toast.makeText(
//                    mActivity, "Service Stop",
//                    Toast.LENGTH_SHORT
//            ).show();
//        } else {
//            Toast.makeText(
//                    mActivity,
//                    "Service Not Run",
//                    Toast.LENGTH_SHORT
//            ).show();
//        }
    }

    private void stopPollingLocation() {
        mHandler.removeCallbacks(pollLocation);
        if (!isAdhoc) {
            mHandler.removeCallbacks(sendPassengerDataPeriodically);
        }
    }

    private void startCheckingProximity() {
        checkProximity.run();
    }

    private void stopCheckingProximity() {
        mHandler.removeCallbacks(checkProximity);
    }

    private void toast(String text) {
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        toast.show();
    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    private void setNFCSettings(boolean msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (!GPSUtil.isLocationEnabled(context)) {
            if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
                builder.setMessage(Constants.GPS_OFF)
                        .setTitle("Warning! - GPS is off.")
                        .setCancelable(false)
                        .setPositiveButton("OK", (dialog, which) -> {
                            dialogInterface = dialog;
                            startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), CHECK_GPS_ON);
                        }).setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {
                builder.setMessage(Constants.GPS_OFF_CH)
                        .setCancelable(false)
                        .setPositiveButton("OK", (dialog, which) -> {
                            dialogInterface = dialog;
                            startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), CHECK_GPS_ON);
                        }).setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        }

        if (useInternalNFC) {
            NfcManager manager = (NfcManager) context.getSystemService(Context.NFC_SERVICE);
            NfcAdapter adapter = manager.getDefaultAdapter();
            if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
                if (adapter == null) {
                    builder.setMessage(Constants.INTERNAL_NFC_NULL)
                            .setTitle("Warning! - No NFC detected.")
                            .setCancelable(false)
                            .setPositiveButton("OK", (dialog, which) -> startActivity(new Intent(MapsActivity.this, SettingsActivity.class))).setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                } else if (!adapter.isEnabled()) {
                    builder.setMessage(Constants.INTERNAL_NFC_OFF)
                            .setTitle("Warning! - NFC is off.")
                            .setCancelable(false)
                            .setPositiveButton("OK", (dialog, which) -> {
                                dialogInterface = dialog;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    startActivityForResult(new Intent(Settings.ACTION_NFC_SETTINGS), CHECK_NFC_ON);
                                } else {
                                    startActivityForResult(new Intent(Settings.ACTION_WIRELESS_SETTINGS), CHECK_NFC_ON);
                                }
                            }).setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                } else {
                    if (msg) {
                        toast(Constants.INTERNAL_NFC_ON);
                    }
                }
            } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {
                if (adapter == null) {
                    builder.setMessage(Constants.INTERNAL_NFC_NULL_CH)
                            .setCancelable(false)
                            .setPositiveButton("OK", (dialog, which) -> startActivity(new Intent(MapsActivity.this, SettingsActivity.class))).setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                } else if (!adapter.isEnabled()) {
                    builder.setMessage(Constants.INTERNAL_NFC_OFF_CH)
                            .setCancelable(false)
                            .setPositiveButton("OK", (dialog, which) -> {
                                dialogInterface = dialog;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    startActivityForResult(new Intent(Settings.ACTION_NFC_SETTINGS), CHECK_NFC_ON);
                                } else {
                                    startActivityForResult(new Intent(Settings.ACTION_WIRELESS_SETTINGS), CHECK_NFC_ON);
                                }
                            }).setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                } else {
                    if (msg) {
                        toast(Constants.INTERNAL_NFC_ON_CH);
                    }
                }
            }
        } else if (useExternalNFC) {
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
                if (mBluetoothAdapter == null) {
                    // Device does not support Bluetooth
                    builder.setMessage(Constants.EXTERNAL_NFC_NULL)
                            .setTitle("Warning! - No Bluetooth detected.")
                            .setCancelable(false)
                            .setPositiveButton("OK", (dialog, which) -> startActivity(new Intent(MapsActivity.this, SettingsActivity.class))).setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                } else if (!mBluetoothAdapter.isEnabled()) {
                    builder.setMessage(Constants.EXTERNAL_NFC_OFF)
                            .setTitle("Warning! - Bluetooth is off.")
                            .setCancelable(false)
                            .setPositiveButton("OK", (dialog, which) -> {
                                dialogInterface = dialog;
                                startActivityForResult(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS), CHECK_BLUETOOTH_ON);
                            }).setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {
                if (mBluetoothAdapter == null) {
                    // Device does not support Bluetooth
                    builder.setMessage(Constants.EXTERNAL_NFC_NULL_CH)
                            .setCancelable(false)
                            .setPositiveButton("OK", (dialog, which) -> startActivity(new Intent(MapsActivity.this, SettingsActivity.class))).setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                } else if (!mBluetoothAdapter.isEnabled()) {
                    builder.setMessage(Constants.EXTERNAL_NFC_OFF_CH)
                            .setCancelable(false)
                            .setPositiveButton("OK", (dialog, which) -> {
                                dialogInterface = dialog;
                                startActivityForResult(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS), CHECK_BLUETOOTH_ON);
                            }).setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CHECK_BLUETOOTH_ON:
                // Make sure the request was successful
                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                dialogInterface.dismiss();
                if (!mBluetoothAdapter.isEnabled()) {
                    if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
                        toast("Please turn on Bluetooth first.");
                    } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {
                        toast("请启动蓝牙以继续使用定位系统。");
                    }
                    startActivityForResult(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS), CHECK_BLUETOOTH_ON);
                } else {
                    //do nothing
                }
                break;
            case CHECK_NFC_ON:
                // Make sure the request was successful
                NfcManager mNFCmanager = (NfcManager) context.getSystemService(Context.NFC_SERVICE);
                NfcAdapter mNFCAdapter = mNFCmanager.getDefaultAdapter();
                dialogInterface.dismiss();
                if (!mNFCAdapter.isEnabled()) {
                    if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
                        Toast.makeText(context, "Please turn on NFC first.", Toast.LENGTH_SHORT).show();
                    }
                    if (lang.equalsIgnoreCase(Constants.CHINESE)) {
                        Toast.makeText(context, "请开启NFC以继续使用定位系统。", Toast.LENGTH_SHORT).show();
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        startActivityForResult(new Intent(Settings.ACTION_NFC_SETTINGS), CHECK_NFC_ON);
                    } else {
                        startActivityForResult(new Intent(Settings.ACTION_WIRELESS_SETTINGS), CHECK_NFC_ON);
                    }
                } else {
                    //do nothing
                }
                break;
            case CHECK_GPS_ON:
                // Make sure the request was successful
                dialogInterface.dismiss();
                if (!GPSUtil.isLocationEnabled(context)) {
                    dialogInterface.dismiss();
                    if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
                        Toast.makeText(context, "Please turn on GPS.", Toast.LENGTH_SHORT).show();
                    }
                    if (lang.equalsIgnoreCase(Constants.CHINESE)) {
                        Toast.makeText(context, "请打开GPS。", Toast.LENGTH_SHORT).show();
                    }

                    startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), CHECK_GPS_ON);
                } else {
                    //do nothing
                }
                break;
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        toast("Please turn on Location setting first");
                        turnOnLocationSetting();
                    default:
                        break;
                }
                break;
        }
    }

    private void resetApp() {
        prefs.edit().clear().apply();
        MapsActivity.this.finishAffinity();
        Intent i = new Intent(context, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    private void refreshActivity() {
        if (useExternalNFC) {
            if (mBluetoothReader == null) {
                //do nothing
            } else {
                if (mBluetoothReader.transmitEscapeCommand(Constants.UNPAIR_COMMAND)) {
                    mBluetoothReader.powerOffCard();
                    Log.d("", "UNPAIRED");
                }
            }
        }

        Intent intent = getIntent();
        stopPollingLocation();
        stopCheckingProximity();
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    private void loadJob() {
        stopPollingLocation();
        final SharedPreferences.Editor editor = prefs.edit();

        //To retrieve passenger list
        try {
            mPassengersInfoTask = new PassengersInfoTask();
            mPassengersInfoTask.execute((Void) null);

            editor.putInt(Preferences.PASSENGERCOUNT, passengers.size());
            editor.putBoolean(Preferences.ENABLE_INTERNAL_NFC, enableInternalNFC);
            editor.putBoolean(Preferences.ENABLE_EXTERNAL_NFC, enableExternalNFC);
            editor.apply();
        } catch (Exception e) {
            //do nothing
        }

        if(mMap != null){
            mMap.clear();
            drawMarkersAndRoutes();
        }
        startPollingLocation();
    }

    private void showAdhoc(){
        if (isAdhoc) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);

            int jobStatus = adhocJob.getJobStatus();
            switch (jobStatus) {
                case 0:
                    activityMapsBinding.txtJobStatus.setVisibility(View.GONE);
                    activityMapsBinding.btnStartAdhoc.setVisibility(View.VISIBLE);
                    activityMapsBinding.btnEndAdhoc.setVisibility(View.GONE);
                    if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
                        activityMapsBinding.btnStartAdhoc.setOnSwipeListener(() -> builder.setMessage(Constants.START_JOB_CONFIRMATION)
                                .setCancelable(false)
                                .setPositiveButton("Yes - Start Job", (dialog, which) -> {
                                    mStartTripTask = new StartTripTask(adhocJob.getId());
                                    mStartTripTask.execute((Void) null);
                                })
                                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                                .show());
                    } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {
                        activityMapsBinding.btnStartAdhoc.setOnSwipeListener(() -> builder.setMessage(Constants.START_JOB_CONFIRMATION_CH)
                                .setCancelable(false)
                                .setPositiveButton("开始", (dialog, which) -> {
                                    mStartTripTask = new StartTripTask(adhocJob.getId());
                                    mStartTripTask.execute((Void) null);
                                })
                                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                                .show());
                    }
                    break;
                case 1:
                    activityMapsBinding.txtJobStatus.setVisibility(View.GONE);
                    activityMapsBinding.btnStartAdhoc.setVisibility(View.GONE);
                    activityMapsBinding.btnEndAdhoc.setVisibility(View.VISIBLE);
                    if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
                        activityMapsBinding.btnEndAdhoc.setOnSwipeListener(() -> builder.setMessage(Constants.END_JOB_CONFIRMATION)
                                .setCancelable(false)
                                .setPositiveButton("Yes-I have finished this charter", (dialog, which) -> {
                                    mEndTripTask = new EndTripTask(adhocJob.getId(), false);
                                    mEndTripTask.execute((Void) null);
                                })
                                .setNeutralButton("No-Passenger did not show up", (dialog, which) -> {
                                    mEndTripTask = new EndTripTask(adhocJob.getId(), true);
                                    mEndTripTask.execute((Void) null);
                                })
                                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                                .show());
                    } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {
                        activityMapsBinding.btnEndAdhoc.setOnSwipeListener(() -> builder.setMessage(Constants.END_JOB_CONFIRMATION_CH)
                                .setCancelable(false)
                                .setPositiveButton("Yes-I have finished this charter", (dialog, which) -> {
                                    mEndTripTask = new EndTripTask(adhocJob.getId(), false);
                                    mEndTripTask.execute((Void) null);
                                })
                                .setNeutralButton("No-Passenger did not show up", (dialog, which) -> {
                                    mEndTripTask = new EndTripTask(adhocJob.getId(), true);
                                    mEndTripTask.execute((Void) null);
                                })
                                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                                .show());
                    }
                    break;
                case 2:
                    activityMapsBinding.txtJobStatus.setVisibility(View.VISIBLE);
                    activityMapsBinding.btnStartAdhoc.setVisibility(View.GONE);
                    activityMapsBinding.btnEndAdhoc.setVisibility(View.GONE);
                    if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
                        activityMapsBinding.txtJobStatus.setText(Constants.END_JOB_MESSAGE);
                    } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {
                        activityMapsBinding.txtJobStatus.setText(Constants.END_JOB_MESSAGE_CH);
                    }
                    activityMapsBinding.txtJobStatus.setTextColor(Color.BLACK);
                    break;
                case 3:
                    activityMapsBinding.txtJobStatus.setVisibility(View.VISIBLE);
                    activityMapsBinding.btnStartAdhoc.setVisibility(View.GONE);
                    activityMapsBinding.btnEndAdhoc.setVisibility(View.GONE);
                    if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
                        activityMapsBinding.txtJobStatus.setText(Constants.JOB_EXPIRED_MESSAGE);
                    } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {
                        activityMapsBinding.txtJobStatus.setText(Constants.JOB_EXPIRED_MESSAGE_CH);
                    }
                    activityMapsBinding.txtJobStatus.setTextColor(Color.RED);
                    break;
                default:
                    if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
                        builder.setMessage(Constants.ADHOC_ERROR_MESSAGE + adhocJob.getId())
                                .setCancelable(false)
                                .setPositiveButton("Understood", (dialog, which) -> dialog.dismiss()).show();
                    } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {
                        builder.setMessage(Constants.ADHOC_ERROR_MESSAGE_CH + adhocJob.getId())
                                .setCancelable(false)
                                .setPositiveButton("Understood", (dialog, which) -> dialog.dismiss()).show();
                    }
                    break;
            }
        }
    }

    private void initialiseMapValues() {
        aLongitude = new ArrayList<>();
        aLatitude = new ArrayList<>();
        aAltitude = new ArrayList<>();
        aAccuracy = new ArrayList<>();
        aSpeed = new ArrayList<>();
        aDate = new ArrayList<>();
    }

    //Bluetooth Reader - START
    private void setListener(BluetoothReader reader) {
        reader.setOnCardStatusChangeListener((bluetoothReader, sta) ->
            runOnUiThread(() -> {
                if (sta == 1) {
                    Log.v(LOG_TAG, "Card Absent");
                } else if (sta == 2) {
                    Log.v(LOG_TAG, "Card Present");
                    byte[] apdu1 = StringUtil.hexString2Bytes(Constants.APDU1);
                    mBluetoothReader.transmitApdu(apdu1);
                    hasSentApdu1 = true;
                }
            })
        );

        reader.setOnAuthenticationCompleteListener((bluetoothReader, errorCode) ->
            runOnUiThread(() -> {
                if (errorCode == BluetoothReader.ERROR_SUCCESS) {
                    Log.v(LOG_TAG, "Authentication Success!");
                    isAuthenticated = true;
                } else {
                    Log.v(LOG_TAG, "Authentication Failed!");
                }
            })
        );

        reader.setOnResponseApduAvailableListener((bluetoothReader, apdu, errorCode) ->
            runOnUiThread(() -> {
                if (hasSentApdu1) {
                    byte[] apdu2 = StringUtil.hexString2Bytes(Constants.APDU2);
                    mBluetoothReader.transmitApdu(apdu2);
                    hasSentApdu1 = false;
                } else {
                    String result = StringUtil.toHexString(apdu);
                    result = result.replaceAll("\\s+", "");
                    if (result.length() > 32) {
                        String canId = result.substring(16, 32);
                        checkPassengerList(canId, true);
                    } else {
                        String canId = "Error";
                        checkPassengerList(canId, true);
                    }
                }
            })
        );

        reader.setOnCardStatusAvailableListener((bluetoothReader, cardStatus, errorCode) ->
            runOnUiThread(() -> {
                Log.v(LOG_TAG, "Testing");
                //TODO: to check this
            })
        );

        reader.setOnEnableNotificationCompleteListener((bluetoothReader, result) ->
            runOnUiThread(() -> {
                if (result != BluetoothGatt.GATT_SUCCESS) {
                    Log.v(LOG_TAG, "The device is unable to set notification!");
                } else {
                    Log.v(LOG_TAG, "The device is ready to use!");
                }
            })
        );
    }

    private void connectReader() {
        try {
            if (isAuthenticated) {
                if (mBluetoothReader.transmitEscapeCommand(Constants.PAIRING_COMMAND)) {
                    bmBluetooth = BitmapFactory.decodeResource(resources, R.drawable.signal_green);
                    activityMapsBinding.ivNfcConnectionStatus.setImageBitmap(bmBluetooth);
                    activityMapsBinding.ivNfcConnectionStatus.invalidate();
                }
            } else {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
                    builder.setMessage(Constants.ON_NO_DEVICE_DECTECTED)
                            .setTitle("Error!")
                            .setCancelable(false)
                            .setPositiveButton("Understood", (dialog, which) -> dialog.dismiss()).show();
                } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {
                    builder.setMessage(Constants.ON_NO_DEVICE_DECTECTED_CH)
                            .setTitle("Error!")
                            .setCancelable(false)
                            .setPositiveButton("Understood", (dialog, which) -> dialog.dismiss()).show();
                }
            }
        } catch (Exception e) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
                builder.setMessage(Constants.ON_NO_DEVICE_DECTECTED)
                        .setTitle("Error!")
                        .setCancelable(false)
                        .setPositiveButton("Understood", (dialog, which) -> dialog.dismiss()).show();
            } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {
                builder.setMessage(Constants.ON_NO_DEVICE_DECTECTED_CH)
                        .setTitle("Error!")
                        .setCancelable(false)
                        .setPositiveButton("Understood", (dialog, which) -> dialog.dismiss()).show();
            }
        }
    }
    //Bluetooth Reader - END

    private void displayErrorMessage() {
        String driverRole = prefs.getString(Preferences.ROLE, "");
        int statusCode = prefs.getInt(Preferences.STATUSCODE, 0);
        if (statusCode == 401) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
                builder.setMessage(Constants.UNAUTHORISED_MESSAGE)
                        .setCancelable(false)
                        .setPositiveButton("Ok", (dialog, which) -> resetApp())
                        .show();
            } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {
                builder.setMessage(Constants.UNAUTHORISED_MESSAGE_CH)
                        .setCancelable(false)
                        .setPositiveButton("Ok", (dialog, which) -> resetApp())
                        .show();
            }
        } else if (!driverRole.equalsIgnoreCase("BA")) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
                builder.setTitle("Error!")
                        .setMessage(Constants.RETRY_MESSAGE)
                        .setPositiveButton("Retry", (dialog, which) -> loadJob())
                        .setNegativeButton("Cancel", (dialog, which) -> builder.setTitle("Warning!")
                                .setMessage(Constants.RETRY_MESSAGE2)
                                .setNegativeButton("Yes", (dialog12, which12) -> dialog12.dismiss())
                                .show())
                        .show();
            } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {
                builder.setMessage(Constants.RETRY_MESSAGE_CH)
                        .setPositiveButton("重试连接", (dialog, which) -> loadJob())
                        .setNegativeButton("取消", (dialog, which) -> builder.setTitle("Warning!")
                                .setMessage(Constants.RETRY_MESSAGE2_CH)
                                .setNegativeButton("是的", (dialog1, which1) -> dialog1.dismiss())
                                .show())
                        .show();
            }
        }
    }

    private void displayNetworkErrorMessage() {
        stopPollingLocation();
        stopCheckingProximity();

        try {
            Thread.sleep(10000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

        if (haveNetworkConnection()) {
            startPollingLocation();
            if (isAdhoc) {
                startCheckingProximity();
            }
        } else {
            displayNetworkErrorMessage();
        }
    }

    private void sendPassengerListForToday() {
        if (boardingList.isEmpty()) {
            Log.d(LOG_TAG, Constants.NO_PASSENGER_IN_LIST);
        } else {
            ArrayList<String> passengerCanId = new ArrayList<>();
            ArrayList<String> boardingTime = new ArrayList<>();
            ArrayList<Boolean> pickUpDropOffMode = new ArrayList<>();
            for (int i = 0; i < boardingList.size(); i++) {
                if (passengerCanId.size() >= 1) {
                    if (passengerCanId.contains(boardingList.get(i).getEzlinkCanId())) {
                        Log.d("Duplicate Found", "Removing Duplicates...");
                    } else {
                        passengerCanId.add(boardingList.get(i).getEzlinkCanId());
                        boardingTime.add(boardingList.get(i).getDate());
                        pickUpDropOffMode.add(isDropOffArray.get(i));
                    }
                } else {
                    passengerCanId.add(boardingList.get(i).getEzlinkCanId());
                    boardingTime.add(boardingList.get(i).getDate());
                    pickUpDropOffMode.add(isDropOffArray.get(i));
                }
            }
            mAttendanceTask = new AttendanceTask(passengerCanId, boardingTime, pickUpDropOffMode);
            mAttendanceTask.execute((Void) null);
            if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
                if (passengerCanId.size() > 1) {
                    toast("Updated " + passengerCanId.size() + " passenger records to database!");
                } else {
                    toast("Updated " + passengerCanId.size() + " passenger record to database!");
                }
            } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {
                toast("已上传 " + passengerCanId.size() + " 乘客数据到数据库！");
            }
        }
        boardingList.clear();
        isDropOffArray.clear();
    }

    //USB reader - Start
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Constants.ACTION_USB_PERMISSION)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            // Open reader
                            Log.d(LOG_TAG, "Opening reader: " + device.getDeviceName() + "...");
                            try {
                                mReader.open(device);
                            } catch (Exception e) {
                                //do nothing
                            }
                        }
                    } else {
                        Log.d(LOG_TAG, "Permission denied for device " + device.getDeviceName());
                    }
                }

            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                synchronized (this) {

                    // Update reader list
                    mReaderAdapter.clear();
                    for (UsbDevice device : mManager.getDeviceList().values()) {
                        if (mReader.isSupported(device)) {
                            mReaderAdapter.add(device.getDeviceName());
                        }
                    }

                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (device != null && device.equals(mReader.getDevice())) {
                        mReader.close();
                    }
                }
            }
        }
    };

    private AlertDialog readerSelectionDialog() {
        LayoutInflater lI = LayoutInflater.from(context);
        View promptView = lI.inflate(R.layout.spinner_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(promptView);

        if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
            alertDialogBuilder.setTitle(Constants.USB_READER_SELECT);
        } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {
            alertDialogBuilder.setTitle(Constants.USB_READER_SELECT_CH);
        }
        alertDialogBuilder.setIcon(R.drawable.info_icon_blue);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        final Spinner mReaderSpinner = (Spinner) promptView.findViewById(R.id.spinner_list);

        // Initialize reader spinner
        mReaderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        for (UsbDevice device : mManager.getDeviceList().values()) {
            if (mReader.isSupported(device)) {
                mReaderAdapter.add(device.getDeviceName());
            }
        }
        mReaderSpinner.setAdapter(mReaderAdapter);

        Button acceptBtn = (Button) promptView.findViewById(R.id.btn_acceptCharter);
        acceptBtn.setText("Select this device");
        acceptBtn.setOnClickListener(v -> {
            deviceName = (String) mReaderSpinner.getSelectedItem();
            if (deviceName != null) {
                // For each device
                for (UsbDevice device : mManager.getDeviceList().values()) {
                    // If device name is found
                    if (deviceName.equals(device.getDeviceName())) {
                        // Request permission
                        mManager.requestPermission(device, mPermissionIntent);
                        mDevice[0] = device;
                        alertDialog.dismiss();

                        break;
                    }
                }
            }
        });

        Button cancelSelectionBtn = promptView.findViewById(R.id.btn_reselectCharter);
        cancelSelectionBtn.setText("Cancel and return to map");
        cancelSelectionBtn.setOnClickListener(v -> alertDialog.dismiss());

        return alertDialog;
    }
    //USB reader - END

    /*
     *
     * START OF JSON TASK
     *
     * */

    private class PassengersInfoTask extends WebServiceTask {
        PassengersInfoTask() {
            super(MapsActivity.this);
            performRequest();
        }

        @Override
        public void showProgress() {
            if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
                toast("Receiving job from server...");
            } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {
                toast("正在加载工作。。。");
            }

        }

        @Override
        public void hideProgress() {

        }

        @Override
        public boolean performRequest() {

            String accessCode = prefs.getString(Preferences.MANUAL_ACCESSCODE, "CARLSONTEST");
            String URL = Constants.TRIPS_URL;
            if (!(accessCode.equalsIgnoreCase("CARLSONTEST") || accessCode.equalsIgnoreCase(""))) {
                URL = Constants.GET_MANUAL_JOB + "/" + accessCode;
                contentValues = new ContentValues();
                contentValues.put(Constants.ACCESSCODE, accessCode);
                obj = WebServiceUtils.requestJSONObject(URL, WebServiceUtils.METHOD.GET, authenticationToken, context);
            } else {

                obj = WebServiceUtils.requestJSONObject(URL, WebServiceUtils.METHOD.GET, authenticationToken, context);
            }
//            Boolean isSuccessful = false;
            if (!hasError(obj)) {

                JSONObject objPx;
                jsonObject = obj.optJSONObject(Constants.DATA);

                if (jsonObject == null) {
                    if(mMap != null){
                        mMap.clear();
                    }
                    onFailedAttempt();
                    return false;
                } else {
                    try {
//                        isSuccessful = jsonObject.getBoolean(Constants.BUS_CHARTER_SUCCESS);
                        isSchoolBus = jsonObject.getBoolean(Constants.ISSCHOOLBUS);
                        enableInternalNFC = jsonObject.getBoolean(Constants.ENABLEINTERNAL);
                        enableExternalNFC = jsonObject.getBoolean(Constants.ENABLEEXTERNAL);
                        codeName = jsonObject.getString(Constants.CODENAME);
                        fourDigitCode = jsonObject.getString(Constants.FOURDIGITCODE);
                        routeID = jsonObject.getInt(Constants.ROUTEID);
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "Error processing Json data = " + e.getMessage());
                        onFailedAttempt();
                        return false;
                    }

                    activityMapsBinding.btnEndRoute.setVisibility(View.VISIBLE);
                    activityMapsBinding.btnMaps.setVisibility(View.VISIBLE);
                    activityMapsBinding.btnReload.setVisibility(View.GONE);

                    if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
                        activityMapsBinding.txtBoardingCode.setText(codeName + " " + Constants.BORDING_CODE_TEXT_EN);
                    } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {
                        activityMapsBinding.txtBoardingCode.setText(codeName + " " + Constants.BORDING_CODE_TEXT_CN);
                    }

                    if (fourDigitCode.isEmpty()) {
                        activityMapsBinding.txtBoardingCode.setText("Current job: " + codeName);

                        if (isSchoolBus) {
                            final Dialog builder = new Dialog(MapsActivity.this);

                            View view = LayoutInflater.from(MapsActivity.this).inflate(R.layout.custom_greeting, null);
                            TextView txtTitleDialog = (TextView) view.findViewById(R.id.txtTitleDialog);
                            TextView txtDescriptionDialog = (TextView) view.findViewById(R.id.txtDescriptionDialog);
                            TextView txtFourDigitDialog = (TextView) view.findViewById(R.id.txtFourDigitDialog);
                            Button btnOkDialog = (Button) view.findViewById(R.id.btnOkDialog);

                            String loggedInUsername = prefs.getString(Preferences.LOGGED_IN_USERNAME, "");
                            if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
                                txtTitleDialog.setText(Constants.GREETINGS_TITLE + " " + loggedInUsername);
                                txtDescriptionDialog.setText("Your Route is " + codeName);
                            } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {

                                txtTitleDialog.setText(Constants.GREETINGS_TITLE_CN + " " + loggedInUsername);
                                txtDescriptionDialog.setText("您的路线是" + codeName);
                            }
                            txtFourDigitDialog.setText("");

                            btnOkDialog.setOnClickListener(v -> {
                                Toast.makeText(MapsActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                                builder.dismiss();
                            });
                            builder.setContentView(view);
                            builder.show();
                        }
                    } else {
                        activityMapsBinding.txtFourDigit.setText(fourDigitCode);
                        activityMapsBinding.txtFourDigit.setVisibility(View.VISIBLE);

                        final Dialog builder = new Dialog(MapsActivity.this);

                        View view = LayoutInflater.from(MapsActivity.this).inflate(R.layout.custom_greeting, null);
                        TextView txtTitleDialog = (TextView) view.findViewById(R.id.txtTitleDialog);
                        TextView txtDescriptionDialog = (TextView) view.findViewById(R.id.txtDescriptionDialog);
                        TextView txtFourDigitDialog = (TextView) view.findViewById(R.id.txtFourDigitDialog);
                        Button btnOkDialog = (Button) view.findViewById(R.id.btnOkDialog);

                        String loggedInUsername = prefs.getString(Preferences.LOGGED_IN_USERNAME, "");
                        if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
                            txtTitleDialog.setText(Constants.GREETINGS_TITLE + " " + loggedInUsername);
                            txtDescriptionDialog.setText("Boarding Code For " + codeName + " " + Constants.GREETINGS_DESC);
                        } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {

                            txtTitleDialog.setText(Constants.GREETINGS_TITLE_CN + " " + loggedInUsername);
                            txtDescriptionDialog.setText("今天" + codeName + Constants.GREETINGS_DESC_CN);
                        }
                        txtFourDigitDialog.setText(fourDigitCode);

                        btnOkDialog.setOnClickListener(v -> {
                            Toast.makeText(MapsActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                            builder.dismiss();
                        });
                        builder.setContentView(view);
                        builder.show();
                    }
                    //Passengers
                    JSONArray passengersArray = jsonObject.optJSONArray(Constants.PASSENGERS);
                    passengers = new ArrayList<>();
                    try {
                        for (int i = 0; i < passengersArray.length(); i++) {
                            objPx = passengersArray.getJSONObject(i);
                            String name = objPx.getString(Constants.NAME);
                            String gender = objPx.getString(Constants.GENDER);
                            String ezlinkCanId = objPx.getString(Constants.EZLINKCANID);
                            String picUrl = objPx.getString(Constants.PICURL);
                            Boolean smsNOK = objPx.getBoolean(Constants.SMSNOK);
                            String nokName = objPx.getString(Constants.NOKNAME);
                            String nokRelationship = objPx.getString(Constants.NOKRELATIONSHIP);
                            String nokContact = objPx.getString(Constants.NOKCONTACT);

                            Passenger passenger = new Passenger(ezlinkCanId, name, gender, picUrl, smsNOK, nokName, nokRelationship, nokContact);
                            Log.d(LOG_TAG, passenger.toString());

                            passengers.add(passenger);
                        }
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "Error processing Json data = " + e.getMessage());
                        onFailedAttempt();
                        return false;
                    }
                    // routePaths
                    JSONArray routeArray = jsonObject.optJSONArray(Constants.ROUTE_PATH);
                    routePaths = new ArrayList<>();
                    try {
                        for (int i = 0; i < routeArray.length(); i++) {
                            objPx = routeArray.getJSONObject(i);
                            Double routeLat = objPx.getDouble(Constants.LATITUDE);
                            Double routeLon = objPx.getDouble(Constants.LONGTITUDE);
                            RoutePath path = new RoutePath(routeLon, routeLat);
                            Log.d(LOG_TAG, path.toString());

                            routePaths.add(path);
                        }
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "Error processing Json data = " + e.getMessage());
                        onFailedAttempt();
                        return false;
                    }
                    // routePoints
                    JSONArray jobArray = jsonObject.optJSONArray(Constants.ROUTE_POINTS);
                    routePoints = new ArrayList<>();
                    try {
                        for (int i = 0; i < jobArray.length(); i++) {
                            objPx = jobArray.getJSONObject(i);
                            String pointName = objPx.getString(Constants.POINTNAME);
                            Double routeLat = objPx.getDouble(Constants.LATITUDE);
                            Double routeLon = objPx.getDouble(Constants.LONGTITUDE);
                            Integer type = objPx.getInt(Constants.TYPE);
                            String time = objPx.getString(Constants.TIME);
                            int numberOfPassengers = objPx.getInt(Constants.NOOFPASSENERS);

                            RoutePoint job = new RoutePoint(pointName, routeLon, routeLat, type, time, numberOfPassengers);
                            Log.d(LOG_TAG, job.toString());

                            routePoints.add(job);
                        }
                        proxmityCheck = routePoints;
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "Error processing Json data = " + e.getMessage());
                        onFailedAttempt();
                        return false;
                    }
                    //adhoc
                    try {
                        JSONObject adhocArray = jsonObject.optJSONObject(Constants.ADHOC);
                        String id = adhocArray.getString(Constants.BUS_CHARTER_ID);
                        int jobStatus = adhocArray.getInt(Constants.JOBSTATUS);
                        String pocName = adhocArray.getString(Constants.CHARTER_POC_NAME);
                        pocContactNo = adhocArray.getString(Constants.CHARTER_POC_CONTACT_NO);
                        remarks = adhocArray.getString(Constants.REMARKS);
                        if(!remarks.equals("null")) activityMapsBinding.txtRemarks.setText(remarks);
                        String duration = adhocArray.getString(Constants.DURATION);
                        if(!duration.equals("null") && !duration.isEmpty() && !duration.trim().isEmpty()){
                            duration += "h ";
                        } else {
                            duration = "";
                        }
                        String serviceType = adhocArray.getString(Constants.SERVICE_TYPE);
                        if(serviceType.equals("null")) serviceType = "";
                        String durationService = "";

                        if(!serviceType.isEmpty()){
                            durationService = " (" + duration + serviceType + ")";
                        }
                        activityMapsBinding.txtBoardingCode.setText(activityMapsBinding.txtBoardingCode.getText() + durationService);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(Preferences.ADHOC_CHARTER_ID, id);
                        editor.apply();

                        adhocJob = new Adhoc(id, jobStatus, pocName, pocContactNo);
                        Log.d(LOG_TAG, adhocJob.toString());
                        isAdhoc = true;
                        if (jobStatus == 99) {
                            editor.putString(Preferences.ADHOC_CHARTER_ID, "");
                            editor.apply();
                            isAdhoc = false;
                        }
                    } catch (JSONException e) {
                        onFailedAttempt();
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(Preferences.ADHOC_CHARTER_ID, "");
                        editor.apply();
                        isAdhoc = false;
                    }
                    //if everything ok, set to true
                    return true;
                }
            }
            return false;
        }

        @Override
        public void performSuccessfulOperation() {
            Log.d(LOG_TAG, "Data Retrieved Successful!");

            activityMapsBinding.btnReload.setVisibility(View.GONE);
            if(isAdhoc){
                showAdhoc();
                activityMapsBinding.btnEndRoute.setVisibility(View.GONE);
            } else {
                activityMapsBinding.btnEndRoute.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onFailedAttempt() {
            activityMapsBinding.txtBoardingCode.setText("No job available for today");
            activityMapsBinding.btnStartAdhoc.setVisibility(View.GONE);
            activityMapsBinding.btnEndAdhoc.setVisibility(View.GONE);
            activityMapsBinding.btnEndRoute.setVisibility(View.GONE);
            activityMapsBinding.btnReload.setVisibility(View.VISIBLE);
            displayErrorMessage();
        }
    }
    private class JobScheduleTask extends WebServiceTask {
        JobScheduleTask() {
            super(MapsActivity.this);
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

            obj = WebServiceUtils.requestJSONObject(Constants.ALL_TRIPS_URL, WebServiceUtils.METHOD.GET, authenticationToken, context);

//            Boolean isSuccessful = false;
            if (!hasError(obj)) {

                JSONArray jsonArray = obj.optJSONArray(Constants.DATA);

                if (jsonArray == null) {
                    onFailedAttempt();
                    return false;
                } else {
                    List<List<Job>> allJobList = new ArrayList<>();

                    try {
                        // loop to get job list for today and tomorrow
                        for(int i = 0; i < 2; i++){
                            JSONArray jobListJson = jsonArray.getJSONArray(i);
                            List<Job> jobList = new ArrayList<>();

                            // loop to get job data
                            for(int j = 0; j < jobListJson.length(); j++){
                                jsonObject = jobListJson.getJSONObject(j);
                                String jobName = jsonObject.getString(Constants.CODENAME);
                                String vehicleNo = jsonObject.getString(Constants.VEHICLE_NO);
                                if(vehicleNo.equals("null")) vehicleNo = "";
                                JSONObject adhocDetails = jsonObject.getJSONObject(Constants.ADHOC);
                                String busCharterId = adhocDetails.getString(Constants.BUS_CHARTER_ID);
                                String duration = adhocDetails.getString(Constants.DURATION);
                                if(!duration.equals("null") && !duration.isEmpty() && !duration.trim().isEmpty()){
                                    duration += "h ";
                                } else {
                                    duration = "";
                                }
                                String serviceType = adhocDetails.getString(Constants.SERVICE_TYPE);
                                if(serviceType.equals("null")) serviceType = "";
                                String durationService = "";

                                if(!serviceType.isEmpty()){
                                    durationService = " (" + duration + serviceType + ")";
                                }

                                // routePaths
                                JSONArray routeArray = jsonObject.optJSONArray(Constants.ROUTE_PATH);
                                ArrayList<RoutePath> routePathList = new ArrayList<>();
                                try {
                                    for (int k = 0; k < routeArray.length(); k++) {
                                        Double routeLat = routeArray.getJSONObject(k).getDouble(Constants.LATITUDE);
                                        Double routeLon = routeArray.getJSONObject(k).getDouble(Constants.LONGTITUDE);
                                        RoutePath path = new RoutePath(routeLon, routeLat);
                                        Log.d(LOG_TAG, path.toString());

                                        routePathList.add(path);
                                    }
                                } catch (JSONException e) {
                                    Log.e(LOG_TAG, "Error processing Json data = " + e.getMessage());
                                    onFailedAttempt();
                                    return false;
                                }
                                // routePoints
                                JSONArray jobArray = jsonObject.optJSONArray(Constants.ROUTE_POINTS);
                                ArrayList<RoutePoint> routePointList = new ArrayList<>();
                                try {
                                    for (int l = 0; l < jobArray.length(); l++) {
                                        String pointName = jobArray.getJSONObject(l).getString(Constants.POINTNAME);
                                        Double routeLat = jobArray.getJSONObject(l).getDouble(Constants.LATITUDE);
                                        Double routeLon = jobArray.getJSONObject(l).getDouble(Constants.LONGTITUDE);
                                        Integer type = jobArray.getJSONObject(l).getInt(Constants.TYPE);
                                        String time = jobArray.getJSONObject(l).getString(Constants.TIME);
                                        int numberOfPassengers = jobArray.getJSONObject(l).getInt(Constants.NOOFPASSENERS);

                                        RoutePoint job = new RoutePoint(pointName, routeLon, routeLat, type, time, numberOfPassengers);
                                        Log.d(LOG_TAG, job.toString());

                                        routePointList.add(job);
                                    }
                                } catch (JSONException e) {
                                    Log.e(LOG_TAG, "Error processing Json data = " + e.getMessage());
                                    onFailedAttempt();
                                    return false;
                                }

                                Job job = new Job(jobName, busCharterId, routePathList, routePointList, vehicleNo, durationService);
                                jobList.add(job);
                            }

                            allJobList.add(jobList);
                        }

                        String adhocId = "";
                        if(adhocJob != null){
                            adhocId = adhocJob.getId();
                        }

                        RouteAdapter adapter = new RouteAdapter(allJobList.get(0), getApplicationContext(), codeName, adhocId, isTodaySelected);
                        adapter.setOnClickListener((position, model) -> {
                            adapter.setToday(isTodaySelected);
                            routePaths = model.getRoutePaths();
                            routePoints = model.getRoutePoints();
                            mMap.clear();
                            drawMarkersAndRoutes();
                            activityMapsBinding.flPopupWindow.setVisibility(View.GONE);

                            if (!codeName.equals(model.getJobName()) || (adhocJob != null && adhocJob.getId().equals(model.getBusCharterId()))) {
                                activityMapsBinding.btnEndRoute.setVisibility(View.GONE);
                                activityMapsBinding.txtFourDigit.setVisibility(View.GONE);
                                activityMapsBinding.btnStartAdhoc.setVisibility(View.GONE);
                                activityMapsBinding.btnEndAdhoc.setVisibility(View.GONE);
                                activityMapsBinding.btnReload.setVisibility(View.VISIBLE);

                                activityMapsBinding.txtBoardingCode.setText("Viewing job " + model.getJobName() + model.getServiceType());
                            }
                        });
                        activityMapsBinding.recyclerViewJobSchedule.setAdapter(adapter);
                        activityMapsBinding.recyclerViewJobSchedule.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

                        activityMapsBinding.btnRouteToday.setOnClickListener(v -> {
                            isTodaySelected = true;
                            if(adapter.isToday()){
                                adapter.setCurrentJobName(codeName);
                                if(adhocJob != null){
                                    adapter.setCurrentCharterId(adhocJob.getId());
                                }
                            } else {
                                adapter.setCurrentJobName(null);
                                adapter.setCurrentCharterId(null);
                            }
                            adapter.setJobData(allJobList.get(0));
                            adapter.notifyDataSetChanged();
                            activityMapsBinding.btnRouteToday.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorActivated)));
                            activityMapsBinding.btnRouteTmr.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorIdle)));
                        });
                        activityMapsBinding.btnRouteTmr.setOnClickListener(v -> {
                            isTodaySelected = false;
                            if(!adapter.isToday()){
                                adapter.setCurrentJobName(codeName);
                                if(adhocJob != null){
                                    adapter.setCurrentCharterId(adhocJob.getId());
                                }
                            } else {
                                adapter.setCurrentJobName(null);
                                adapter.setCurrentCharterId(null);
                            }
                            adapter.setJobData(allJobList.get(1));
                            adapter.notifyDataSetChanged();
                            activityMapsBinding.btnRouteToday.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorIdle)));
                            activityMapsBinding.btnRouteTmr.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorActivated)));
                        });
                        activityMapsBinding.flPopupWindow.setVisibility(View.VISIBLE);

                        activityMapsBinding.btnClosePopup.setOnClickListener(v -> {
                            activityMapsBinding.flPopupWindow.setVisibility(View.GONE);
                            isTodaySelected = true;
                        });

                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "Error processing Json data = " + e.getMessage());
                        onFailedAttempt();
                        return false;
                    }



                    //if everything ok, set to true
                    return true;
                }
            }
            return false;
        }

        @Override
        public void performSuccessfulOperation() {
            Log.d(LOG_TAG, "Data Retrieved Successfully!");
        }

        @Override
        public void onFailedAttempt() {
            toast("Error loading job schedule, please try again...");
        }
    }

    private class LocationTask extends WebServiceTask {
        final SharedPreferences.Editor editor = prefs.edit();

        LocationTask(double longitude, double latitude, double altitude, float accuracy, float speed, String date) {
            super(MapsActivity.this);
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
                Log.d(LOG_TAG, "Attempt " + (i + 1) + "::" + (obj == null ? "null" : obj.toString()));
                if (!hasError(obj)) {
                    jsonObject = obj.optJSONObject("data");
                    if ((jsonObject.optString(Constants.BUS_CHARTER_MESSAGE).equalsIgnoreCase("Updated"))) {
                        editor.putString(Preferences.LAST_UPDATED_TIME, lastUpdatedDate);
                        editor.apply();
                        return true;
                    }
                } else {
                    i = count;
                    if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
                        toast(Constants.RETRY_MESSAGE3);
                    } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {
                        toast(Constants.RETRY_MESSAGE3_CH);
                    }
//                    displayNetworkErrorMessage();
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

    private class AttendanceTask extends WebServiceTask {
        AttendanceTask(ArrayList<String> canId, ArrayList<String> date, ArrayList<Boolean> pickUpDropOffMode) {
            super(MapsActivity.this);

            for (int i = 0; i < canId.size(); i++) {
                contentValues = new ContentValues();
                contentValues.put(Constants.EZLINKCANID, canId.get(i));
                contentValues.put(Constants.DATE_TIME, date.get(i));
                contentValues.put(Constants.IS_DROP_OFF, pickUpDropOffMode.get(i));
                passengerRecords.add(contentValues);
            }
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
            obj = WebServiceUtils.requestJSONObject(Constants.ATTENDANCE_URL,
                    WebServiceUtils.METHOD.POST, authenticationToken, null, passengerRecords, true);
            passengerRecords.clear();
            if (obj != null) {
                Log.d(LOG_TAG, obj.toString());
                jsonObject = obj.optJSONObject("data");
                if (jsonObject != null) {
                    return jsonObject.optString(Constants.BUS_CHARTER_MESSAGE).equalsIgnoreCase("Updated");
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
//            displayErrorMessage();
            //do nothing
        }
    }

    private class StartTripTask extends WebServiceTask {
        StartTripTask(String charterId) {
            super(MapsActivity.this);

            contentValues = new ContentValues();
            contentValues.put(Constants.BUS_CHARTER_ID, charterId);
//            contentValues.put(Constants.WAITINGTIME, minutes);

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
            obj = WebServiceUtils.requestJSONObject(Constants.START_TRIP_URL, WebServiceUtils.METHOD.POST, authenticationToken, null, context, contentValues);
            boolean isSuccessful = false;
            if (!hasError(obj)) {
                Log.d(LOG_TAG, obj.toString());
                jsonObject = obj.optJSONObject(Constants.DATA);
                message = "";
                if (jsonObject != null) {
                    try {
                        isSuccessful = jsonObject.getBoolean(Constants.BUS_CHARTER_SUCCESS);
                        if (!isSuccessful) {
                            message = jsonObject.getString(Constants.BUS_CHARTER_MESSAGE);
                        }
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "Error processing Json data = " + e.getMessage());
                    }
                }
            }
            return isSuccessful;
        }

        @Override
        public void performSuccessfulOperation() {
            Log.d(LOG_TAG, "Logged Successful!");
            if (message.equalsIgnoreCase("") || message.isEmpty()) {
                activityMapsBinding.btnStartAdhoc.showResultIcon(true, true);
                loadJob();
            } else {
                onFailedAttempt();
            }
        }

        @Override
        public void onFailedAttempt() {
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(message)
                    .setCancelable(true).show();
            activityMapsBinding.btnStartAdhoc.showResultIcon(false, true);
        }
    }

    private class EndTripTask extends WebServiceTask {
        EndTripTask(String charterId, boolean noShow) {
            super(MapsActivity.this);

            contentValues = new ContentValues();
            contentValues.put(Constants.BUS_CHARTER_ID, charterId);
            contentValues.put(Constants.PASSENGERNOSHOW, noShow);
//            contentValues.put(Constants.EXCEEDEDTIME, minutes);

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
            obj = WebServiceUtils.requestJSONObject(Constants.END_TRIP_URL, WebServiceUtils.METHOD.POST, authenticationToken, null, context, contentValues);
            boolean isSuccessful = false;
            if (!hasError(obj)) {
                Log.d(LOG_TAG, obj.toString());
                jsonObject = obj.optJSONObject(Constants.DATA);
                message = "";
                if (jsonObject != null) {
                    try {
                        isSuccessful = jsonObject.getBoolean(Constants.BUS_CHARTER_SUCCESS);
                        if (!isSuccessful) {
                            message = jsonObject.getString(Constants.BUS_CHARTER_MESSAGE);
                        }
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "Error processing Json data = " + e.getMessage());
                    }
                }
            }
            return isSuccessful;
        }

        @Override
        public void performSuccessfulOperation() {
            Log.d(LOG_TAG, "Logged Successful!");
            if (message.equalsIgnoreCase("") || message.isEmpty()) {
                activityMapsBinding.btnEndAdhoc.showResultIcon(true, true);
                loadJob();
            } else {
                onFailedAttempt();
            }
        }

        @Override
        public void onFailedAttempt() {
            if (message.isEmpty()) message = "There is an error happen while ending the adhoc job, please try again";
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(message)
                    .setCancelable(true).show();
            activityMapsBinding.btnEndAdhoc.showResultIcon(false, true);
        }
    }

    private class EndTripRouteTask extends WebServiceTask {
        EndTripRouteTask(String charterId) {
            super(MapsActivity.this);

            contentValues = new ContentValues();
            contentValues.put(Constants.BUS_CHARTER_ID, charterId);
//            contentValues.put(Constants.EXCEEDEDTIME, minutes);

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
            obj = WebServiceUtils.requestJSONObject(Constants.END_TRIP_ROUTE_URL, WebServiceUtils.METHOD.POST, authenticationToken, null, context, contentValues);
            boolean isSuccessful = false;
            if (!hasError(obj)) {
                Log.d(LOG_TAG, obj.toString());
                jsonObject = obj.optJSONObject(Constants.DATA);
                message = "";
                if (jsonObject != null) {
                    try {
                        isSuccessful = jsonObject.getBoolean(Constants.BUS_CHARTER_SUCCESS);
                        message = jsonObject.getString(Constants.BUS_CHARTER_MESSAGE);
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "Error processing Json data = " + e.getMessage());
                    }
                }
            }
            return isSuccessful;
        }

        @Override
        public void performSuccessfulOperation() {
            Log.d(LOG_TAG, "End Route Trip Successful!");
            activityMapsBinding.btnEndRoute.showResultIcon(true, true);
            loadJob();
        }

        @Override
        public void onFailedAttempt() {
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(message).show();
            activityMapsBinding.btnEndRoute.showResultIcon(false, true);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermissionsSafely(String[] permissions, Integer requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.requestPermissions(permissions, requestCode);
        }
    }
    /*
     *
     * END OF JSON TASK
     *
     * */
}