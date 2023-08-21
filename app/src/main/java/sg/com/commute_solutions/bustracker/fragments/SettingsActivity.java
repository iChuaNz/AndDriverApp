package sg.com.commute_solutions.bustracker.fragments;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.Context;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import android.provider.Settings;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.MotionEvent;

import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.w3c.dom.Text;

import sg.com.commute_solutions.bustracker.BuildConfig;
import sg.com.commute_solutions.bustracker.R;
import sg.com.commute_solutions.bustracker.util.StringUtil;
import sg.com.commute_solutions.bustracker.common.Constants;
import sg.com.commute_solutions.bustracker.common.Preferences;
import sg.com.commute_solutions.bustracker.webservices.WebServiceTask;
import sg.com.commute_solutions.bustracker.webservices.WebServiceUtils;

/**
 * Created by Kyle on 7/9/16.
 */
public class SettingsActivity extends AppCompatActivity {

    private static final String LOG_TAG = SettingsActivity.class.getSimpleName();
    private LinearLayout llPassengerList, llConnectedDevice, llDeviceScan;
    private SharedPreferences prefs;
    private String lang;
    private TextView txtUsername, txtVersionNo, txtLastUpdateTime, txtPassengerList, txtShowMessage, txtShowCanId, txtSelectedNFC, txtConnectedDevice;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private NfcAdapter mNFCAdapter;
    private Context context;

    private JSONObject obj, jsonObject;
    private final ContentValues authenticationToken = new ContentValues();
    private LogoutTask mLogoutTask = null;

    private boolean mScanning, enableInternalNFC, enableExternalNFC;
    private Handler mHandler;
    private View dialogView, showMessageView, showCanIdView;

    private Switch swShowMessages, swShowCanId;
    private RadioGroup rgNFC;
    private RadioButton rgInternal, rgExternal, rgUsb;
    private Button btnLogout, btnChgSettings, btnScan, btnDisconnect;
    private String NFC, authToken, prefsNFC;

    private int count = 0;

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final long SCAN_PERIOD = 10000;
    private static final int CHECK_BLUETOOTH_ON = 1;
    private static final int CHECK_NFC_ON = 2;
    private static final String SCAN = "Scan";
    private static final String CANCEL = "Cancel";

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        context = this;
        /*
         * Initializes a Bluetooth adapter. For API level 18 and above, get a
         * reference to BluetoothAdapter through BluetoothManager.
         */
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        prefs = this.getSharedPreferences(Constants.SHARE_PREFERENCE_PACKAGE, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();
        lang = prefs.getString(Preferences.LANGUAGE, "");
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mNFCAdapter = ((NfcManager) getApplicationContext().getSystemService(Context.NFC_SERVICE)).getDefaultAdapter();
        mHandler = new Handler();

        authToken = StringUtil.deNull(prefs.getString(Preferences.AUTH_TOKEN, ""));
        authenticationToken.put(Constants.TOKEN, authToken);

        enableInternalNFC = prefs.getBoolean(Preferences.ENABLE_INTERNAL_NFC, Boolean.FALSE);
        enableExternalNFC = prefs.getBoolean(Preferences.ENABLE_EXTERNAL_NFC, Boolean.FALSE);

        txtUsername = (TextView) findViewById(R.id.txt_username);
        txtLastUpdateTime = (TextView) findViewById(R.id.txt_lastUpdateTime);
        txtPassengerList = (TextView) findViewById(R.id.txt_passenger_list);
        txtVersionNo = (TextView) findViewById(R.id.txt_version_no);
        txtSelectedNFC = (TextView) findViewById(R.id.txt_currentNFCSelection);
        txtConnectedDevice = (TextView) findViewById(R.id.lbl_connecteddevice);

        txtShowMessage = (TextView) findViewById(R.id.txt_show_messages);
        swShowMessages = (Switch) findViewById(R.id.switch_show_messages);
        showMessageView = findViewById(R.id.view_show_messages);

        txtShowCanId = (TextView) findViewById(R.id.txt_show_CanID);
        swShowCanId = (Switch) findViewById(R.id.switch_show_CanID);
        showCanIdView = findViewById(R.id.view_show_CanID);

        rgNFC = (RadioGroup) findViewById(R.id.radio_nfc);
        rgInternal = (RadioButton) findViewById(R.id.radio_internalnfc);
        rgExternal = (RadioButton) findViewById(R.id.radio_externalnfc);
        rgUsb = (RadioButton) findViewById(R.id.radio_usbreader);

        btnLogout = (Button) findViewById(R.id.btn_logout);
        btnChgSettings = (Button) findViewById(R.id.btn_chgNFCsettings);
        btnScan = (Button) findViewById(R.id.btn_scan);
        btnDisconnect = (Button) findViewById(R.id.btn_disconnect);

        llPassengerList = (LinearLayout) findViewById(R.id.ll_passengerlist);
        llConnectedDevice = (LinearLayout) findViewById(R.id.ll_connecteddevice);
        llDeviceScan = (LinearLayout) findViewById(R.id.ll_flomio_scan);

        String loggedInUsername = prefs.getString(Preferences.LOGGED_IN_USERNAME, "");
        String lastUpdateTime = prefs.getString(Preferences.LAST_UPDATED_TIME, "");

        txtLastUpdateTime.setText(lastUpdateTime);
        txtUsername.setText(loggedInUsername.toUpperCase());
        txtVersionNo.setText(BuildConfig.VERSION_NAME);
//        txtVersionNo.setText("Staging");

        if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
            btnScan.setText("Scan");
            btnDisconnect.setText("Disconnect");
        } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {
            btnScan.setText("扫描");
            btnDisconnect.setText("断开");
        }

        btnDisconnect.setEnabled(false);
        txtUsername.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                count++;
                if (count == 10) {
                    txtShowMessage.setVisibility(View.VISIBLE);
                    swShowMessages.setVisibility(View.VISIBLE);
                    showMessageView.setVisibility(View.VISIBLE);

                    txtShowCanId.setVisibility(View.VISIBLE);
                    swShowCanId.setVisibility(View.VISIBLE);
                    showCanIdView.setVisibility(View.VISIBLE);

                    btnLogout.setVisibility(View.VISIBLE);
                    Toast.makeText(SettingsActivity.this, R.string.logout_unlocked, Toast.LENGTH_SHORT).show();
                    count = 0;
                }
                return false;
            }
        });

        boolean showMessages = prefs.getBoolean(Preferences.SHOW_MESSAGE, Boolean.FALSE);
        swShowMessages.setChecked(showMessages);
        swShowMessages.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean(Preferences.SHOW_MESSAGE, isChecked);
                editor.apply();
            }
        });

        boolean showCanId = prefs.getBoolean(Preferences.SHOW_MESSAGE, Boolean.FALSE);
        swShowCanId.setChecked(showCanId);
        swShowCanId.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean(Preferences.SHOW_CAN_ID, isChecked);
                editor.apply();
            }
        });

        txtShowMessage.setVisibility(View.GONE);
        swShowMessages.setVisibility(View.GONE);
        showMessageView.setVisibility(View.GONE);

        txtShowCanId.setVisibility(View.GONE);
        swShowCanId.setVisibility(View.GONE);
        showCanIdView.setVisibility(View.GONE);

        rgNFC.setVisibility(View.GONE);
        if (!enableInternalNFC && !enableExternalNFC) {
            btnChgSettings.setVisibility(View.GONE);
        }
        btnChgSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rgNFC.setVisibility(View.VISIBLE);
                btnChgSettings.setVisibility(View.GONE);
            }
        });

        if (!enableInternalNFC) {
            rgInternal.setVisibility(View.GONE);
        }

        if (!enableExternalNFC) {
            rgExternal.setVisibility(View.GONE);
            rgUsb.setVisibility(View.GONE);
        }

        String showNFCIndicator = prefs.getString(Preferences.NFCINDICATOR, "");
        String deviceAddress = prefs.getString(Preferences.BLUETOOTH_DEVICE_ADDRESS, "");
        String deviceName = prefs.getString(Preferences.BLUETOOTH_DEVICE_NAME, "");
        //int passengerCount = prefs.getInt(Preferences.PASSENGERCOUNT, 0);

        //if (passengerCount > 0) {
            //txtPassengerList.setText(passengerCount + "");
            //llPassengerList.setVisibility(View.VISIBLE);
        //}

        if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
            if (deviceAddress == null || deviceAddress.equals("")) {
                txtConnectedDevice.setText("No Device saved! Tap Scan to setup new device.");
            } else if (deviceName == null || deviceName.equals("") || deviceName.equals(R.string.unknown_device)) {
                txtConnectedDevice.setText("Non NFC device installed. Tap Scan to setup new device");
            } else  {
                txtConnectedDevice.setText("Device installed: " + deviceName);
                btnDisconnect.setEnabled(true);
            }
        } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {
            if (deviceAddress == null || deviceAddress.equals("")) {
                txtConnectedDevice.setText("设备未保存！点击'扫描'以设置新的设备。");
            } else if (deviceName == null || deviceName.equals("") || deviceName.equals(R.string.unknown_device)) {
                txtConnectedDevice.setText("这不是nfc设备。点击'扫描'以设置新的设备。");
            } else  {
                txtConnectedDevice.setText("目前安装：" + deviceName);
                btnDisconnect.setEnabled(true);
            }
        }

        if (showNFCIndicator.equalsIgnoreCase("external")) {
            NFC = "External NFC";
            llDeviceScan.setVisibility(View.VISIBLE);
            llConnectedDevice.setVisibility(View.VISIBLE);
        } else if (showNFCIndicator.equals("internal")) {
            NFC = "Internal NFC";
        } else if (showNFCIndicator.equals("usb")) {
            NFC = "USB reader";
        } else {
            NFC = "Not using NFC";
        }

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // Android M Permission check
                    requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                }
                    /*
                     * Use this check to determine whether BLE is supported on the device.
                     * Then you can selectively disable BLE-`
                     */
                if (!getPackageManager().hasSystemFeature(
                        PackageManager.FEATURE_BLUETOOTH_LE)) {
                    Toast.makeText(getApplicationContext(), R.string.btle_not_supported,Toast.LENGTH_SHORT);
                } else if (mBluetoothAdapter == null) {
                    Toast.makeText(getApplicationContext(), R.string.bt_not_supported,
                            Toast.LENGTH_SHORT).show();
                } else {
                    mLeDeviceListAdapter = new LeDeviceListAdapter();
                    showBluetoothScanningDialog(context, mLeDeviceListAdapter);
                }
            }
        });

        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor.putString(Preferences.BLUETOOTH_DEVICE_ADDRESS, "");
                editor.putString(Preferences.BLUETOOTH_DEVICE_NAME, "");
                editor.commit();
                btnDisconnect.setEnabled(false);
                if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
                    txtConnectedDevice.setText("No Device saved! Tap Scan to setup new device.");
                } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {
                    txtConnectedDevice.setText("无设备！点击'扫描'以设置新的设备。");
                }
            }
        });
        if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
            txtSelectedNFC.setText("Current Setting for NFC: " + NFC);
        } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {
            txtSelectedNFC.setText("当前设置：" + NFC);
        }

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if (authToken!= null) {
//                    mLogoutTask.execute((Void) null);
//                }
//                clearApplicationData();
                prefs.edit().clear().commit();
                SettingsActivity.this.finishAffinity();
                Intent i = new Intent(getApplicationContext(),LoginActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        final Dialog dialog = new Dialog(context);
        dialog.dismiss();
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_nonfc:
                if (checked) {
                    llDeviceScan.setVisibility(View.GONE);
                    llConnectedDevice.setVisibility(View.GONE);
                    prefsNFC = "none";
                    break;
                }
            case R.id.radio_internalnfc:
                if (checked) {
                    llDeviceScan.setVisibility(View.GONE);
                    llConnectedDevice.setVisibility(View.GONE);
                    if (mNFCAdapter == null) {
                        // If Device does not support Internal NFC
                        if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
                            Toast.makeText(getApplicationContext(), "Your phone does not support NFC.", Toast.LENGTH_SHORT).show();
                        } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {
                            Toast.makeText(getApplicationContext(), "您的手机没有NFC设置。", Toast.LENGTH_SHORT).show();
                        }
                        rgNFC.clearCheck();
                        break;
                    } else if (!mNFCAdapter.isEnabled()) {
                        if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
                            Toast.makeText(getApplicationContext(), "Please turn on NFC first.", Toast.LENGTH_SHORT).show();
                        } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {
                            Toast.makeText(getApplicationContext(), "请打开NFC设置。", Toast.LENGTH_SHORT).show();
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            startActivityForResult(new Intent(Settings.ACTION_NFC_SETTINGS), CHECK_NFC_ON);
                        } else {
                            startActivityForResult(new Intent(Settings.ACTION_WIRELESS_SETTINGS), CHECK_NFC_ON);
                        }
                        break;
                    } else {
                        prefsNFC = "internal";
                        break;
                    }
                }
            case R.id.radio_externalnfc:
                if (checked) {
                    if (mBluetoothAdapter == null) {
                        // If Device does not support Bluetooth
                        if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
                            Toast.makeText(getApplicationContext(), "Your phone does not support Bluetooth.", Toast.LENGTH_SHORT).show();
                        } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {
                            Toast.makeText(getApplicationContext(), "你的手机没有蓝牙。", Toast.LENGTH_SHORT).show();
                        }
                        rgNFC.clearCheck();
                        break;
                    } else if (!mBluetoothAdapter.isEnabled()) {
                        if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
                            Toast.makeText(getApplicationContext(), "Please turn on Bluetooth first.", Toast.LENGTH_SHORT).show();
                        } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {
                            Toast.makeText(getApplicationContext(), "请打开蓝牙。", Toast.LENGTH_SHORT).show();
                        }
                        startActivityForResult(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS), CHECK_BLUETOOTH_ON);
                        break;
                    } else {
                        prefsNFC = "external";
                        llDeviceScan.setVisibility(View.VISIBLE);
                        llConnectedDevice.setVisibility(View.VISIBLE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(Preferences.NFCINDICATOR, prefsNFC);
                        editor.apply();
                        break;
                    }
                }
            case R.id.radio_usbreader:
                if (checked) {
                    prefsNFC = "usb";
                    llDeviceScan.setVisibility(View.GONE);
                    llConnectedDevice.setVisibility(View.GONE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(Preferences.NFCINDICATOR, prefsNFC);
                    editor.apply();
                    break;
                }
            default:
                prefsNFC = "none";
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHECK_BLUETOOTH_ON) {
            // Make sure the request was successful
            if (resultCode != RESULT_OK) {
                if (!mBluetoothAdapter.isEnabled()) {
                    if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
                        Toast.makeText(getApplicationContext(), "Please turn on Bluetooth first.", Toast.LENGTH_SHORT).show();
                    } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {
                        Toast.makeText(getApplicationContext(), "请打开蓝牙。", Toast.LENGTH_SHORT).show();
                    }
                    startActivityForResult(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS), CHECK_BLUETOOTH_ON);
                } else {
                    llDeviceScan.setVisibility(View.VISIBLE);
                    llConnectedDevice.setVisibility(View.VISIBLE);
                }
            }
        }

        if (requestCode == CHECK_NFC_ON) {
            // Make sure the request was successful
            if (resultCode != RESULT_OK) {
                if (!mNFCAdapter.isEnabled()) {
                    if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
                        Toast.makeText(getApplicationContext(), "Please turn on NFC first.", Toast.LENGTH_SHORT).show();
                    } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {
                        Toast.makeText(getApplicationContext(), "请打开NFC设置。", Toast.LENGTH_SHORT).show();
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        startActivityForResult(new Intent(Settings.ACTION_NFC_SETTINGS), CHECK_NFC_ON);
                    } else {
                        startActivityForResult(new Intent(Settings.ACTION_WIRELESS_SETTINGS), CHECK_NFC_ON);
                    }
                }
            }
        }
    }

    private void showBluetoothScanningDialog(Context context, final LeDeviceListAdapter mLeDeviceListAdapter) {
        final Dialog dialog =  new Dialog(context);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if(!((Activity) context).isFinishing()) {
            builder.setTitle(R.string.bt_title)
                    .setPositiveButton(SCAN, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
            /* Initializes list view adapter. */
                            mLeDeviceListAdapter.clear();
                            scanLeDevice(true);
                        }
                    }).setNegativeButton(CANCEL, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialog.dismiss();
                }
            }).setCancelable(false)
                    .show();
        }
    }

//    public void clearApplicationData() {
//        File cacheDirectory = getCacheDir();
//        File applicationDirectory = new File(cacheDirectory.getParent());
//
//        if (applicationDirectory.exists()) {
//            String[] fileNames = applicationDirectory.list();
//            for (String fileName : fileNames) {
//                if (!fileName.equals("lib")) {
//                    deleteFile(new File(applicationDirectory, fileName));
//                }
//            }
//        }
//    }
//
//    public static boolean deleteFile(File file) {
//        boolean deletedAll = true;
//
//        if (file != null) {
//            if (file.isDirectory()) {
//                String[] children = file.list();
//
//                for (int i = 0; i < children.length; i++) {
//                    deletedAll = deleteFile(new File(file, children[i])) && deletedAll;
//                }
//            } else {
//                deletedAll = file.delete();
//            }
//        }
//        return deletedAll;
//    }

    private synchronized void scanLeDevice(final boolean enable) {
        if (enable) {
            /* Stops scanning after a pre-defined scan period. */
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mScanning) {
                        mScanning = false;
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    }
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            showDeviceDialog();
        } else if (mScanning) {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    /* Adapter for holding devices found through scanning. */
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;
        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<>();
            mInflator = LayoutInflater.from(getApplicationContext());
        }

        public void addDevice(BluetoothDevice device) {
            if (!mLeDevices.contains(device)) {
                mLeDevices.add(device);
                showDeviceDialog();
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            /* General ListView optimization code. */
            if (view == null) {
                view = mInflator.inflate(R.layout.activity_settings, null);
                viewHolder = new ViewHolder();
//                viewHolder.deviceAddress = (TextView) view
//                        .findViewById(R.id.device_address);
//                viewHolder.deviceName = (TextView) view
//                        .findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            dialogView = view;

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();

            if (deviceName != null && deviceName.length() > 0) {
                viewHolder.deviceName.setText(deviceName);
            } else {
                viewHolder.deviceName.setText(R.string.unknown_device);
            }
            viewHolder.deviceAddress.setText(device.getAddress());

            return dialogView;
        }
    }

    private void showDeviceDialog() {
        int deviceCount =  mLeDeviceListAdapter.getCount();
        if(!((Activity) context).isFinishing())
        {
            if (deviceCount > 0) {

                CharSequence[] deviceArray = new CharSequence[deviceCount];

                for (int i = 0; i < deviceCount; i++) {
                    String name = mLeDeviceListAdapter.getDevice(i).getName();
                    if (name == null || name.equalsIgnoreCase("")){
                        name = "Unknown Device";
                    }
                    deviceArray[i] = name;
                }

                final Dialog dialog =  new Dialog(context);
                dialog.dismiss();
                final Intent intent = getIntent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                builder.setTitle(R.string.bt_found)
                        .setNegativeButton(CANCEL, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialog.dismiss();
                                finish();
                                startActivity(intent);
                            }
                        }).setCancelable(false)
                        .setItems(deviceArray, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                final BluetoothDevice device = mLeDeviceListAdapter.getDevice(i);
                                if (device == null) {
                                    return;
                                }

                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString(Preferences.BLUETOOTH_DEVICE_ADDRESS, device.getAddress());
                                editor.putString(Preferences.BLUETOOTH_DEVICE_NAME, device.getName());
                                editor.commit();

                                dialog.dismiss();
                                finish();
                                startActivity(intent);
                            }
                        }).show();
            } else {
                final Dialog dialog =  new Dialog(context);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                builder.setTitle(R.string.bt_found)
                        .setNegativeButton(CANCEL, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialog.dismiss();
                            }
                        }).setCancelable(false)
                        .setMessage(R.string.no_device_found).show();
            }
        }

    }

    /* Device scan callback. */
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi,
                             byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLeDeviceListAdapter.addDevice(device);
                    mLeDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(getApplicationContext(), R.string.settings_saved, Toast.LENGTH_SHORT).show();
        if (StringUtil.deNull(prefsNFC).equalsIgnoreCase("")){
            prefsNFC = prefs.getString(Preferences.NFCINDICATOR, "");
        } else {
            if (StringUtil.deNull(prefs.getString(Preferences.NFCINDICATOR, "")).equalsIgnoreCase("external")) {
                NFC = "External NFC";
            } else if (StringUtil.deNull(prefs.getString(Preferences.NFCINDICATOR, "")).equals("internal")) {
                NFC = "Internal NFC";
            } else if (StringUtil.deNull(prefs.getString(Preferences.NFCINDICATOR, "")).equals("none")) {
                NFC = "Not using NFC";
            }
            if (lang.equalsIgnoreCase(Constants.ENGLISH)) {
                txtSelectedNFC.setText("Current Setting for NFC: " + NFC);
            } else if (lang.equalsIgnoreCase(Constants.CHINESE)) {
                txtSelectedNFC.setText("当前设置：" + NFC);
            }
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Preferences.NFCINDICATOR, prefsNFC);
        editor.apply();

        String device = prefs.getString(Preferences.BLUETOOTH_DEVICE_ADDRESS, "");
        if (NFC.equalsIgnoreCase("External NFC") && (device == null || device.isEmpty() || device.equalsIgnoreCase(""))) {
            final Dialog dialog =  new Dialog(context);
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            if(!((Activity) context).isFinishing()) {
                builder.setTitle(R.string.generic_title_error)
                        .setMessage(R.string.no_external_device)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialog.dismiss();
                            }
                        }).setCancelable(false)
                        .show();
            }
        } else {
            Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private class LogoutTask extends WebServiceTask {
        LogoutTask() {
            super(SettingsActivity.this);
        }

        @Override
        public void showProgress() {
//            SettingsActivity.this.showProgress(true);
        }

        @Override
        public void hideProgress() {
//            SettingsActivity.this.showProgress(false);
        }

        @Override
        public boolean performRequest() {
            int count = 5;

            for (int i = 0; i < count; i++){
                obj = WebServiceUtils.requestJSONObject(Constants.LOGOUT_URL,
                        WebServiceUtils.METHOD.GET, authenticationToken, null, null, true);
                Log.d(LOG_TAG, "Attempt " + (i+1) + "::" + obj.toString());
                jsonObject = obj.optJSONObject("data");
                if (jsonObject != null) {
                    if ((jsonObject.optString("message").equalsIgnoreCase("Updated"))) {
                        return true;
                    }
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
}
