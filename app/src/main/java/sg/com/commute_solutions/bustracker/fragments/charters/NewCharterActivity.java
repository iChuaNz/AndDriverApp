package sg.com.commute_solutions.bustracker.fragments.charters;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.libraries.places.api.model.Place;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import sg.com.commute_solutions.bustracker.R;
import sg.com.commute_solutions.bustracker.common.Constants;
import sg.com.commute_solutions.bustracker.common.Preferences;
import sg.com.commute_solutions.bustracker.data.Charters;
import sg.com.commute_solutions.bustracker.fragments.LoginActivity;
import sg.com.commute_solutions.bustracker.fragments.MapsActivity;
import sg.com.commute_solutions.bustracker.fragments.RouteActivity;
import sg.com.commute_solutions.bustracker.fragments.charters.contractservices.PickUpPointActivity;
import sg.com.commute_solutions.bustracker.util.StringUtil;
import sg.com.commute_solutions.bustracker.webservices.WebServiceTask;
import sg.com.commute_solutions.bustracker.webservices.WebServiceUtils;

/**
 * Created by Kyle on 12/4/17.
 */

public class NewCharterActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener
{
    private static final String LOG_TAG = NewCharterActivity.class.getSimpleName();
    private static final LatLng SINGAPORE = new LatLng(1.390910, 103.820629);

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

    private Context context;
    private SharedPreferences prefs;
    private String authToken;
    private final ContentValues authenticationToken = new ContentValues();

    private FloatingActionMenu fabCharteringMenu;
    private FloatingActionButton fabViewCharter;
    private FloatingActionButton fabYourCharter;
    private FloatingActionButton fabSuccessfulCharter;
    private FloatingActionButton fabToTracker;
    private FloatingActionButton fabDispute;
    private FloatingActionButton fabContract;
    private FloatingActionButton fabProfile;

    private Button txtChooseDate, txtChoosePickupTime, txtChooseReturnTime;
    private TextView txtStartLocation, txtEndLocation, txtPricingLabel;
    private EditText txtChooseDisposalHours, etBusQuantity, etSuboutPrice;
    private Spinner busTypeSpinner;
    private ImageView btnAddCharter, ivOneWay, ivTwoWay, ivDisposal, twoWaySignage;
    private Marker startLocationMarker, endLocationMarker;
    private MarkerOptions mStartLocation, mEndLocation;
    private double startLat, startLng, endLat, endLng;
    private boolean needAdditionalInput = false;

    private boolean isOMO = false;
    private String numOfOnlineUsers;
    private String mYear, mMonth, mDay;
    private DatePickerDialog datePickerDialog;
    private int mHrs, mMins;
    private String serviceType, pickUpTimeString, dropOffTimeString;
    private Charters charter;
    private boolean isReadyToProceed = false;
    private boolean hasSetDate = false;
    private boolean hasSetPickUpTime = false;
    private boolean hasSetDropOffTime = false;
    static final int DATE_DIALOG_ID = 0;
    static final int PICKUPTIME_DIALOG_ID = 1;
    static final int RETURNTIME_DIALOG_ID = 2;

    private AlertDialog loadingScreen;
    private JSONObject obj, jsonObject;
    private ContentValues contentValues;
    private String displayMessage;
    private boolean isSuccessful;
    private GetNumberOfOnlineUsersTask mGetNumberOfOnlineUsersTask = null;
    private SubmitNewJobTask mSubmitNewJobTask = null;

    //Google Map Places
    int PLACE_AUTOCOMPLETE_REQUEST_CODE_STARTLOCATION = 1;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE_ENDLOCATION = 2;

    private final String[] busType = {"11 - Seater","13 - Seater","19 - Seater","23 - Seater","40 - Seater","45 - Seater","49 - Seater"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_charter);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        context = this;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        prefs = this.getSharedPreferences(Constants.SHARE_PREFERENCE_PACKAGE, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();
        authToken = StringUtil.deNull(prefs.getString(Preferences.AUTH_TOKEN, ""));
        authenticationToken.put(Constants.TOKEN, authToken);
        editor.putString(Preferences.CURRENTACTIVITY, "createcharter");
        editor.apply();

        fabCharteringMenu = (FloatingActionMenu) findViewById(R.id.fab_charteringMenu2);
        fabViewCharter = (FloatingActionButton) findViewById(R.id.fab_viewCharter2);
        fabSuccessfulCharter = (FloatingActionButton) findViewById(R.id.fab_successfulBids2);
        fabYourCharter = (FloatingActionButton) findViewById(R.id.fab_ownCharter2);
        fabToTracker = (FloatingActionButton) findViewById(R.id.fab_toTracking2);
        fabDispute = (FloatingActionButton) findViewById(R.id.fab_dispute2);
        fabContract = (FloatingActionButton) findViewById(R.id.fab_contract2);
        fabProfile = (FloatingActionButton) findViewById(R.id.fab_profile2);

        try {
            numOfOnlineUsers = "0";
            mGetNumberOfOnlineUsersTask = new GetNumberOfOnlineUsersTask();
            mGetNumberOfOnlineUsersTask.execute((Void) null);
        } catch (Exception e) {
            //do nothing
        } finally {
            TextView txtOnlineUsers = (TextView) findViewById(R.id.txt_onlineUsers);
            txtOnlineUsers.setText("Users online: " + numOfOnlineUsers);
        }

        final Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
//        String formattedDate = df.format(c.getTime());
        mHrs = c.getTime().getHours();
        mMins = c.getTime().getMinutes();

        mYear = "" + c.get(Calendar.YEAR);
        mMonth = "" + c.get(Calendar.MONTH);
        mDay = "" + c.get(Calendar.DAY_OF_MONTH);

        txtChooseDate = (Button) findViewById(R.id.txt_chooseDate);
        txtChooseDate.setHint("Select Date");
        txtChooseDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID);
                datePickerDialog.getDatePicker().setMinDate(c.getTimeInMillis());
                c.add(Calendar.MONTH, 2);
                datePickerDialog.getDatePicker().setMaxDate(c.getTimeInMillis());
            }
        });
        ImageView ivDateSignage = (ImageView) findViewById(R.id.iv_dateSignage);
        ivDateSignage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtChooseDate.performClick();
            }
        });

        txtChoosePickupTime = (Button) findViewById(R.id.txt_choosePickupTime);
        txtChoosePickupTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(PICKUPTIME_DIALOG_ID);
            }
        });
        ImageView ivTimeSignage = (ImageView) findViewById(R.id.iv_pickupSignage);
        ivTimeSignage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtChoosePickupTime.performClick();
            }
        });

        txtChooseDisposalHours = (EditText) findViewById(R.id.txt_chooseDisposalHours);
        txtChooseReturnTime = (Button) findViewById(R.id.txt_chooseReturnTime);
        txtChooseReturnTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (needAdditionalInput) {
                    showDialog(RETURNTIME_DIALOG_ID);
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage(Constants.ONE_WAY_MESSAGE_NO_RETURN_TIME)
                            .setTitle("Message")
                            .setCancelable(false)
                            .setPositiveButton("Understood", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();
                }
            }
        });
        txtChooseDisposalHours.setInputType(InputType.TYPE_CLASS_NUMBER);

        ivOneWay = (ImageView) findViewById(R.id.iv_oneway);
        ivTwoWay = (ImageView) findViewById(R.id.iv_twoway);
        twoWaySignage = (ImageView) findViewById(R.id.twoway_signage);
        ivDisposal = (ImageView) findViewById(R.id.iv_disposal);

        //Default Values
        serviceType = "oneway";
        setLocked(ivTwoWay);
        setLocked(ivDisposal);
        twoWaySignage.setVisibility(View.INVISIBLE);
        txtChooseReturnTime.setVisibility(View.INVISIBLE);
        txtPricingLabel = (TextView) findViewById(R.id.txt_pricing_label);

        ivOneWay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                needAdditionalInput = false;
                serviceType = "oneway";
                txtPricingLabel.setText("Sub Out Price($)");
                twoWaySignage.setVisibility(View.INVISIBLE);
                txtChooseReturnTime.setVisibility(View.INVISIBLE);
                txtChooseDisposalHours.setVisibility(View.GONE);
                setUnlocked(ivOneWay);
                setLocked(ivTwoWay);
                setLocked(ivDisposal);
            }
        });
        ivTwoWay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(Constants.TWO_WAY_CONFIRMATION_MESSAGE)
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
                needAdditionalInput = true;
                twoWaySignage.setVisibility(View.VISIBLE);
                txtChooseReturnTime.setVisibility(View.VISIBLE);
                txtChooseReturnTime.setHint("Return time");
                txtChooseDisposalHours.setVisibility(View.GONE);
                txtPricingLabel.setText("Price per Trip($)");
                serviceType = "twoway";
                setUnlocked(ivTwoWay);
                setLocked(ivOneWay);
                setLocked(ivDisposal);
            }
        });
        ivDisposal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                needAdditionalInput = true;
                txtChooseReturnTime.setVisibility(View.GONE);
                twoWaySignage.setVisibility(View.VISIBLE);
                txtChooseDisposalHours.setVisibility(View.VISIBLE);
                txtChooseDisposalHours.setText("");
                txtPricingLabel.setText("Price per Hour($)");
                serviceType = "disposal";
                setUnlocked(ivDisposal);
                setLocked(ivOneWay);
                setLocked(ivTwoWay);
            }
        });

        etBusQuantity = (EditText) findViewById(R.id.et_busQuantity);
        etBusQuantity.setInputType(InputType.TYPE_CLASS_NUMBER);
        etSuboutPrice = (EditText) findViewById(R.id.et_suboutPrice);
        etSuboutPrice.setInputType(InputType.TYPE_CLASS_NUMBER);
        busTypeSpinner = (Spinner) findViewById(R.id.busTypeSpinner);
        busTypeSpinner.setAdapter(new SpinnerAdapter(context, R.layout.spinner_row, busType));
        TextView txtVehicleCap = (TextView) findViewById(R.id.txt_vehicleType);
        txtVehicleCap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                busTypeSpinner.performClick();
            }
        });

        mMapFragment = MapFragment.newInstance();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.charter_map, mMapFragment);
        fragmentTransaction.commit();
        mMapFragment.getMapAsync(this);

        if (mMapFragment == null) {
            mMapFragment = MapFragment.newInstance();
            fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.charter_map, mMapFragment);
            fragmentTransaction.commit();
            mMapFragment.getMapAsync(this);
        }

        mStartLocation = new MarkerOptions();
        mEndLocation = new MarkerOptions();

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key), Locale.US);
        }

        txtStartLocation = (TextView) findViewById(R.id.txt_startLocation);
        txtStartLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);
                    Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                            .setCountry("SG")
                            .build(NewCharterActivity.this);
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE_STARTLOCATION);
                } catch (Exception e) {
                    // TODO: Handle the error.
                    Log.e("txtStartLocation.Click",e.getMessage());
                }
            }
        });
        txtEndLocation = (TextView) findViewById(R.id.txt_endLocation);
        txtEndLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);
                    Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                            .setCountry("SG")
                            .build(NewCharterActivity.this);
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE_ENDLOCATION);
                } catch (Exception e) {
                    // TODO: Handle the error.
                    Log.e("txtEndLocation.Click",e.getMessage());
                }
            }
        });

        String driverRole = prefs.getString(Preferences.ROLE, "");
        if (driverRole.equalsIgnoreCase("OMO")) {
            isOMO = true;
        }

        if (isOMO) {
            fabToTracker.setVisibility(View.VISIBLE);
        }

        LayoutInflater lI = LayoutInflater.from(context);
        View promptView = lI.inflate(R.layout.loading_screen, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(promptView);
        loadingScreen = alertDialogBuilder.create();
        loadingScreen.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        loadingScreen.setCanceledOnTouchOutside(false);

        fabCharteringMenu.setIconAnimated(false);
        fabCharteringMenu.setMenuButtonColorNormal(Color.parseColor("#F68B1F"));
        fabViewCharter.setOnClickListener(clickListener);
        fabViewCharter.setColorNormal(Color.parseColor("#F68B1F"));
        fabSuccessfulCharter.setOnClickListener(clickListener);
        fabSuccessfulCharter.setColorNormal(Color.parseColor("#F68B1F"));
        fabYourCharter.setOnClickListener(clickListener);
        fabYourCharter.setColorNormal(Color.parseColor("#F68B1F"));
        fabToTracker.setOnClickListener(clickListener);
        fabToTracker.setColorNormal(Color.parseColor("#F68B1F"));
        fabDispute.setOnClickListener(clickListener);
        fabDispute.setColorNormal(Color.parseColor("#F68B1F"));
        fabContract.setOnClickListener(clickListener);
        fabContract.setColorNormal(Color.parseColor("#F68B1F"));
        fabProfile.setOnClickListener(clickListener);
        fabProfile.setColorNormal(Color.parseColor("#F68B1F"));

        btnAddCharter = (ImageView) findViewById(R.id.btn_add_charter);
        btnAddCharter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String[] pickupName = {txtStartLocation.getText().toString()};
                    String[] dropOffName = {txtEndLocation.getText().toString()};
                    String[] pickupLatitude = {String.valueOf(startLat)};
                    String[] pickupLongitude = {String.valueOf(startLng)};
                    String[] dropOffLatitude = {String.valueOf(endLat)};
                    String[] dropOffLongitude = {String.valueOf(endLng)};
                    if (pickupName[0].isEmpty() || dropOffName[0].isEmpty() || pickupLatitude[0].isEmpty() || pickupLongitude[0].isEmpty() || dropOffLatitude[0].isEmpty() || dropOffLongitude[0].isEmpty()) {
                        throw new Exception();
                    }
                    String[] pickUpTime = {String.valueOf(new StringBuilder()
                            .append(mYear).append("-")
                            .append(mMonth).append("-")
                            .append(mDay)
                            .append(" ")
                            .append(pickUpTimeString)
                            .append(":00.000"))};
                    String[] dropOffTime = {String.valueOf(new StringBuilder()
                            .append(mYear).append("-")
                            .append(mMonth).append("-")
                            .append(mDay)
                            .append(" ")
                            .append(dropOffTimeString)
                            .append(":00.000"))};

                    if (!hasSetDate) {
                        throw new Exception();
                    }

                    if (serviceType.equalsIgnoreCase("oneway")) {
                        if (!hasSetPickUpTime) {
                            throw new Exception();
                        }
                    }

                    if (serviceType.equalsIgnoreCase("twoway")) {
                        if (!hasSetDropOffTime || !hasSetPickUpTime) {
                            throw new Exception();
                        }
                    }

                    int disposalDuration = 0;
                    if (serviceType.equalsIgnoreCase("disposal")) {
                        disposalDuration = Integer.parseInt(txtChooseDisposalHours.getText().toString());
                        if (!hasSetPickUpTime) {
                            throw new Exception();
                        }
                    }

                    String cost = etSuboutPrice.getText().toString();
                    if (cost.isEmpty()) {
                        throw new Exception();
                    }

                    String busTypeString = busTypeSpinner.getSelectedItem().toString();
                    busTypeString = busTypeString.substring(0, 2);
                    int busType = Integer.parseInt(busTypeString);
                    int busQty = Integer.parseInt(etBusQuantity.getText().toString());

                    if (serviceType.equalsIgnoreCase("oneway")) {
                        charter = new Charters(serviceType, pickupName, dropOffName, pickUpTime, null, cost, disposalDuration,
                                pickupLatitude, pickupLongitude, dropOffLatitude, dropOffLongitude, busType, "", busQty);
                        isReadyToProceed = true;
                    } else if (serviceType.equalsIgnoreCase("twoway")) {
                        charter = new Charters(serviceType, pickupName, dropOffName, pickUpTime, dropOffTime, cost, disposalDuration,
                                pickupLatitude, pickupLongitude, dropOffLatitude, dropOffLongitude, busType, "", busQty);
                        isReadyToProceed = true;
                    } else {
                        charter = new Charters(serviceType, pickupName, dropOffName, pickUpTime, null, cost, disposalDuration,
                                pickupLatitude, pickupLongitude, dropOffLatitude, dropOffLongitude, busType, "", busQty);
                        isReadyToProceed = true;
                    }

                } catch (Exception e ) {
                    Log.d(LOG_TAG, "Information is insufficient to add to server");
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage(Constants.INSUFFICIENT_DATA_FOR_NEW_CHARTER)
                            .setTitle("Error!")
                            .setCancelable(false)
                            .setPositiveButton("Understood", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();
                }

                if (isReadyToProceed) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    final EditText txt_remarks = new EditText(context);
                    txt_remarks.setHint("(Optional) Additional Information - e.g Needs microphone");
                    showLoadingScreen();

                    builder.setMessage(Constants.CONFIRMATION_MESSAGE)
                            .setView(txt_remarks)
                            .setCancelable(false)
                            .setPositiveButton("Yes - Proceed", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    final AlertDialog.Builder builder2 = new AlertDialog.Builder(context);
                                    final EditText txtExpiryHrs = new EditText(context);
                                    txtExpiryHrs.setHint("e.g 8");
                                    txtExpiryHrs.setInputType(InputType.TYPE_CLASS_NUMBER);

                                    builder2.setMessage(Constants.EXPIRY_MESSAGE)
                                            .setView(txtExpiryHrs)
                                            .setCancelable(false)
                                            .setPositiveButton("Yes - Proceed", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    try {
                                                        dialog.dismiss();
                                                        String additionalInfo = txt_remarks.getText().toString();
                                                        String expiryHrsInString = txtExpiryHrs.getText().toString();
                                                        try {
                                                            Thread.sleep(4000);
                                                        } catch(InterruptedException e) {
                                                            //do nothing
                                                        }
                                                        mSubmitNewJobTask = new SubmitNewJobTask(charter, serviceType, additionalInfo, expiryHrsInString);
                                                        mSubmitNewJobTask.execute((Void) null);
                                                    } catch (Exception e) {
                                                        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                                        builder.setMessage(Constants.CANNOT_FETCH_AVAILABLE_JOBS)
                                                                .setCancelable(false)
                                                                .setPositiveButton("Understood", new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        hideLoadingScreen();
                                                                        dialog.dismiss();
                                                                    }
                                                                }).show();
                                                    }
                                                }
                                            })
                                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    hideLoadingScreen();
                                                    dialog.dismiss();
                                                }
                                            })
                                            .show();

                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    hideLoadingScreen();
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (isTaskRoot()) {
            //do nothing
        } else {
            super.onBackPressed();
        }
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SINGAPORE, 12));
        enableLocationPermission();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (haveNetworkConnection()) {
            mLocationRequest.setFastestInterval(500); // 0.5 sec to poll
            mLocationRequest.setInterval(1000); //1 second
        } else {
            mLocationRequest.setFastestInterval(2500); // 2.5 sec to poll
            mLocationRequest.setInterval(5000); //5 seconds
        }

        buildGoogleApiClient();
        mGoogleApiClient.connect();

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
    public void onConnected(@Nullable Bundle bundle) {
        Location mLastLocation = null;
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            enableLocationPermission();
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }
        if (mLastLocation != null) {
            Log.d(LOG_TAG, "Latitude: "+ mLastLocation.getLatitude() + " , Longitude: "+ mLastLocation.getLatitude());
            currentlatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            //zoom to current position:
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(currentlatLng).zoom(14).build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        toast(Constants.ON_CONNECTION_SUSPENDED);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        toast(Constants.ON_CONNECTION_FAILED);
    }

    @Override
    public void onLocationChanged (Location location) {
        if (location == null) {
            toast("NO GPS. Please wait while we try to locate you.. ");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE_STARTLOCATION) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                txtStartLocation.setText(place.getName());
                if (startLocationMarker != null) {
                    startLocationMarker.remove();
                }
                LatLng selectedLatLng = place.getLatLng();
                startLat = selectedLatLng.latitude;
                startLng = selectedLatLng.longitude;

                mStartLocation.position(selectedLatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.green_marker));
                startLocationMarker = mMap.addMarker(mStartLocation);

                if (endLocationMarker == null) {
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(selectedLatLng).zoom(13).build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                } else {
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(SINGAPORE).zoom(10).build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }

                Log.i(LOG_TAG, "Place: " + place.getName());
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                // TODO: Handle the error.
                Log.i(LOG_TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        } else if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE_ENDLOCATION) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                txtEndLocation.setText(place.getName());
                if (endLocationMarker != null) {
                    endLocationMarker.remove();
                }

                LatLng selectedLatLng = place.getLatLng();
                endLat = selectedLatLng.latitude;
                endLng = selectedLatLng.longitude;

                mEndLocation.position(selectedLatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.blue_marker));
                endLocationMarker = mMap.addMarker(mEndLocation);

                if (startLocationMarker == null) {
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(selectedLatLng).zoom(13).build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                } else {
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(SINGAPORE).zoom(10).build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }

                Log.i(LOG_TAG, "Place: " + place.getName());
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                // TODO: Handle the error.
                Log.i(LOG_TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        Intent intent;
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.fab_viewCharter2:
                    intent = new Intent(getApplicationContext(), AvailableCharterActivity.class);
                    startActivity(intent);
                    stopLocationUpdates();
                    finish();
                    break;
                case R.id.fab_successfulBids2:
                    intent = new Intent(getApplicationContext(), YourCharterActivity.class);
                    intent.putExtra("intention","acceptedCharters");
                    startActivity(intent);
                    stopLocationUpdates();
                    finish();
                    break;
                case R.id.fab_ownCharter2:
                    intent = new Intent(getApplicationContext(), YourCharterActivity.class);
                    intent.putExtra("intention","ownCharter");
                    startActivity(intent);
                    stopLocationUpdates();
                    finish();
                    break;
                case R.id.fab_toTracking2:
                    intent = new Intent(getApplicationContext(), RouteActivity.class);
                    startActivity(intent);
                    stopLocationUpdates();
                    finish();
                    break;
                case R.id.fab_dispute2:
                    intent = new Intent(getApplicationContext(), DisputedCharterActivity.class);
                    startActivity(intent);
                    stopLocationUpdates();
                    break;
                case R.id.fab_contract2:
                    intent = new Intent(getApplicationContext(), PickUpPointActivity.class);
                    startActivity(intent);
                    stopLocationUpdates();
                    break;
                case R.id.fab_profile2:
                    intent = new Intent(getApplicationContext(), CharterProfileActivity.class);
                    startActivity(intent);
                    stopLocationUpdates();
                    break;
            }
        }
    };

    public class SpinnerAdapter extends ArrayAdapter<String> {

        public SpinnerAdapter(Context context, int textViewResourceId, String[] objects) {
            super(context, textViewResourceId, objects);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            View row = inflater.inflate(R.layout.spinner_row, parent, false);
            TextView label = (TextView) row.findViewById(R.id.busTypeLabel);
            label.setTextSize(12.5f);
            label.setText(busType[position]);

            ImageView icon = (ImageView)row.findViewById(R.id.busTypeIcon);
            if (busType[position].equalsIgnoreCase("11 - Seater")){
                icon.setImageResource(R.drawable.bus_type_13);
            } else if (busType[position].equalsIgnoreCase("13 - Seater")){
                icon.setImageResource(R.drawable.bus_type_13);
            } else if (busType[position].equalsIgnoreCase("19 - Seater")){
                icon.setImageResource(R.drawable.bus_type_23);
            } else if (busType[position].equalsIgnoreCase("23 - Seater")){
                icon.setImageResource(R.drawable.bus_type_23);
            } else if (busType[position].equalsIgnoreCase("40 - Seater")){
                icon.setImageResource(R.drawable.bus_type_40);
            } else if (busType[position].equalsIgnoreCase("45 - Seater")) {
                icon.setImageResource(R.drawable.bus_type_40);
            } else if (busType[position].equalsIgnoreCase("49 - Seater")) {
                icon.setImageResource(R.drawable.bus_type_40);
            }

//            return getCustomView(position, convertView, parent);
            return row;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        public View getCustomView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            View row = inflater.inflate(R.layout.spinner_row, parent, false);
            TextView label = (TextView) row.findViewById(R.id.busTypeLabel);
            label.setTextSize(12.5f);
            label.setPadding(10, 0, 0, 0);
            label.setText(busType[position]);

            ImageView icon = (ImageView)row.findViewById(R.id.busTypeIcon);
            icon.setVisibility(View.GONE);
//            if (busType[position].equalsIgnoreCase("11 - Seater")){
//                icon.setImageResource(R.drawable.bus_type_13);
//            } else if (busType[position].equalsIgnoreCase("13 - Seater")){
//                icon.setImageResource(R.drawable.bus_type_13);
//            } else if (busType[position].equalsIgnoreCase("19 - Seater")){
//                icon.setImageResource(R.drawable.bus_type_23);
//            } else if (busType[position].equalsIgnoreCase("23 - Seater")){
//                icon.setImageResource(R.drawable.bus_type_23);
//            } else if (busType[position].equalsIgnoreCase("40 - Seater")){
//                icon.setImageResource(R.drawable.bus_type_40);
//            }

            //return super.getView(position, convertView, parent);
            return row;
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                datePickerDialog = new DatePickerDialog(this, mDateSetListener, Integer.parseInt(mYear), Integer.parseInt(mMonth), Integer.parseInt(mDay));
                return datePickerDialog;
            case PICKUPTIME_DIALOG_ID:
                return new TimePickerDialog(this, mTimeSetListener, mHrs, mMins, true);
            case RETURNTIME_DIALOG_ID:
                return new TimePickerDialog(this, mTimeSetListener2, mHrs, mMins, true);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mYear = "" + year;
            if (monthOfYear < 9) {
                mMonth = "0" + (monthOfYear + 1);
            } else {
                mMonth = "" + (monthOfYear + 1);
            }
            if (dayOfMonth < 10) {
                mDay = "0" + dayOfMonth;
            } else {
                mDay = "" + dayOfMonth;
            }
            hasSetDate = true;
            updateDisplay(true, false, false);
        }
    };

    private TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            mHrs = hourOfDay;
            mMins = minute;
            hasSetPickUpTime = true;
            updateDisplay(false, true, false);
        }
    };

    private TimePickerDialog.OnTimeSetListener mTimeSetListener2 = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            mHrs = hourOfDay;
            mMins = minute;
            hasSetDropOffTime = true;
            updateDisplay(false, false, true);
        }
    };

    private void updateDisplay(boolean isDate, boolean isPickupTime, boolean isReturnTime) {
        if (isDate) {
            txtChooseDate.setText(new StringBuilder()
                    .append(mDay).append("/")
                    .append(mMonth).append("/")
                    .append(mYear));
        }

        if (isPickupTime) {
            if (mMins < 10) {
                pickUpTimeString = String.valueOf(new StringBuilder()
                        .append(mHrs)
                        .append(":")
                        .append("0")
                        .append(mMins));

            } else {
                pickUpTimeString = String.valueOf(new StringBuilder()
                        .append(mHrs)
                        .append(":")
                        .append(mMins));
            }

            txtChoosePickupTime.setText(new StringBuilder()
                    .append(pickUpTimeString)
                    .append(" hrs"));
        }

        if (isReturnTime) {
            if (mMins < 10) {
                dropOffTimeString = String.valueOf(new StringBuilder()
                        .append(mHrs)
                        .append(":")
                        .append("0")
                        .append(mMins));
            } else {
                dropOffTimeString = String.valueOf(new StringBuilder()
                        .append(mHrs)
                        .append(":")
                        .append(mMins));
            }

            txtChooseReturnTime.setText(new StringBuilder()
                    .append(dropOffTimeString)
                    .append(" hrs"));
        }
    }

    private void toast(String text){
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

    public static void setLocked (ImageView v) {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
        v.setColorFilter(cf);
        v.setImageAlpha(128);
    }

    public static void setUnlocked (ImageView v) {
        v.setColorFilter(null);
        v.setImageAlpha(255);
    }

    private void showLoadingScreen() {
        loadingScreen.show();
    }

    private void hideLoadingScreen() {
        loadingScreen.dismiss();
    }

    private void resetApp() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(Constants.UNAUTHORISED_MESSAGE)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        prefs.edit().clear().commit();
                        NewCharterActivity.this.finishAffinity();
                        Intent i = new Intent(getApplicationContext(),LoginActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                    }
                }).show();
    }

    /*
*
* START OF JSON TASK
*
* */

    private class GetNumberOfOnlineUsersTask extends WebServiceTask {
        GetNumberOfOnlineUsersTask(){
            super(NewCharterActivity.this);
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
            String URL = Constants.ONLINE_USERS_URL;
            obj = WebServiceUtils.requestJSONObject(URL, WebServiceUtils.METHOD.GET, authenticationToken, context);
            if(!hasError(obj)) {
                Log.d(LOG_TAG, obj.toString());
                jsonObject = obj.optJSONObject(Constants.DATA);

                if (jsonObject == null) {
                    return false;
                } else {
                    try {
                        numOfOnlineUsers = jsonObject.getString(Constants.ONLINEUSERS);
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "Error processing Json data = " + e.getMessage());
                    }
                }
                return true;
            }
            return false;
        }

        @Override
        public void performSuccessfulOperation() {
            Log.d(LOG_TAG, "Retrieved Data successfully!");
        }

        @Override
        public void onFailedAttempt() {
            int statusCode = prefs.getInt(Preferences.STATUSCODE, 0);
            if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                resetApp();
            }
        }
    }


    private class SubmitNewJobTask extends WebServiceTask {
        SubmitNewJobTask(Charters charter, String svcType, String remarks, String expiryHrs){
            super(NewCharterActivity.this);
            contentValues = new ContentValues();
            contentValues.put(Constants.CHARTER_COST, charter.getCost());
            contentValues.put(Constants.CHARTER_BUSTYPE, charter.getBusType());
            contentValues.put(Constants.CHARTER_DISPOSAL_DURATION, charter.getDisposalDuration());
            contentValues.put(Constants.CHARTER_BUSQUANTITY, charter.getBusQuantity());
            contentValues.put(Constants.CHARTER_SERVICE_TYPE, svcType);
            contentValues.put(Constants.CHARTER_PICKUP_NAME, charter.getPickupName()[0]);
            contentValues.put(Constants.CHARTER_DROPOFF_NAME, charter.getDropOffName()[0]);
            contentValues.put(Constants.CHARTER_PICKUPTIME, charter.getPickUpTime()[0]);
            try {
                contentValues.put(Constants.CHARTER_DROPOFFTIME, charter.getDropOffTime()[0]);
            } catch (Exception e) {
                contentValues.put(Constants.CHARTER_DROPOFFTIME, "");
            }
            contentValues.put(Constants.CHARTER_PICKUP_LATITUDE, charter.getPickupLatitude()[0]);
            contentValues.put(Constants.CHARTER_PICKUP_LONGITUDE, charter.getPickupLongitude()[0]);
            contentValues.put(Constants.CHARTER_DROPOFF_LATITUDE, charter.getDropOffLatitude()[0]);
            contentValues.put(Constants.CHARTER_DROPOFF_LONGITUDE, charter.getDropOffLongitude()[0]);
            contentValues.put(Constants.CHARTER_REMARKS, remarks);
            contentValues.put(Constants.CHARTER_EXPIRY_DURATION, Integer.parseInt(expiryHrs));

            if (isReadyToProceed) {
                isReadyToProceed = false;
                performRequest();
            }
        }

        @Override
        public void showProgress() {

        }

        @Override
        public void hideProgress() {

        }

        @Override
        public boolean performRequest() {
            obj = WebServiceUtils.requestJSONObject(Constants.CREATECHARTER_URL, WebServiceUtils.METHOD.POST, authenticationToken, null, context, contentValues);
            if(!hasError(obj)) {
                Log.d(LOG_TAG, obj.toString());
                jsonObject = obj.optJSONObject(Constants.DATA);

                if (jsonObject != null) {
                    try {
                        isSuccessful = jsonObject.getBoolean(Constants.BUS_CHARTER_SUCCESS);
                        displayMessage = jsonObject.getString(Constants.BUS_CHARTER_MESSAGE);
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "Error processing Json data = " + e.getMessage());
                    }
                }
                return true;
            }
            return false;
        }

        @Override
        public void performSuccessfulOperation() {
            hideLoadingScreen();
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            displayMessage = displayMessage.replaceAll("\\.", "\n");
            builder.setMessage(displayMessage)
                    .setTitle("Message")
                    .setCancelable(false)
                    .setPositiveButton("Understood", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (isSuccessful) {
                                Intent intent = new Intent(getApplicationContext(), NewCharterActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                dialog.dismiss();
                            }
                        }
                    }).show();
        }

        @Override
        public void onFailedAttempt() {
            int statusCode = prefs.getInt(Preferences.STATUSCODE, 0);
            if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                resetApp();
            }
        }
    }
}
