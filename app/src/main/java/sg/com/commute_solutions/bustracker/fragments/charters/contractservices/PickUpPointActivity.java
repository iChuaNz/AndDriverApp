package sg.com.commute_solutions.bustracker.fragments.charters.contractservices;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
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

import sg.com.commute_solutions.bustracker.R;
import sg.com.commute_solutions.bustracker.common.Constants;
import sg.com.commute_solutions.bustracker.fragments.charters.NewCharterActivity;

/**
 * Created by Kyle on 10/1/18.
 */

public class PickUpPointActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String LOG_TAG = PickUpPointActivity.class.getSimpleName();
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
    private GoogleApiClient mGoogleApiClient;
    private Context context;

    private FloatingActionMenu fabContractMenu;
    private FloatingActionButton fabToCharter;
    private FloatingActionButton fabViewContracts;

    private LinearLayout llAdditionalPickupPoint1, llAdditionalPickupPoint2;
    private ImageButton btnAddPoint1, btnAddPoint2;
    private TextView txtPickupPoint1, txtPickupPoint2, txtPickupPoint3;
    private Button btnNext;

    boolean didAddNewRow1, didAddNewRow2;
    private LatLng latlng1, latlng2, latlng3;
    private String pickUp1, pickUp2, pickUp3;
    private Marker pickUpPointMarker1, pickUpPointMarker2, pickUpPointMarker3;
    private MarkerOptions mPickUpPoint1, mPickUpPoint2, mPickUpPoint3;

    //Google Map Places
    int PLACE_AUTOCOMPLETE_REQUEST_CODE_STARTLOCATION1 = 1;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE_STARTLOCATION2 = 2;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE_STARTLOCATION3 = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contract_pick_up_point);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        context = this;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mMapFragment = MapFragment.newInstance();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.contract_pickup_map, mMapFragment);
        fragmentTransaction.commit();
        mMapFragment.getMapAsync(this);

        if (mMapFragment == null) {
            mMapFragment = MapFragment.newInstance();
            fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.contract_pickup_map, mMapFragment);
            fragmentTransaction.commit();
            mMapFragment.getMapAsync(this);
        }

        initialiseValues();
        final AutocompleteFilter countryFilter = new AutocompleteFilter.Builder()
                .setCountry("SG")
                .build();

        txtPickupPoint1 = (TextView) findViewById(R.id.txt_contract_pickup_1);
        txtPickupPoint2 = (TextView) findViewById(R.id.txt_contract_pickup_2);
        txtPickupPoint3 = (TextView) findViewById(R.id.txt_contract_pickup_3);

        txtPickupPoint1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .setFilter(countryFilter)
                            .build(PickUpPointActivity.this);
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE_STARTLOCATION1);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }
            }
        });

        txtPickupPoint2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .setFilter(countryFilter)
                            .build(PickUpPointActivity.this);
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE_STARTLOCATION2);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }
            }
        });

        txtPickupPoint3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .setFilter(countryFilter)
                            .build(PickUpPointActivity.this);
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE_STARTLOCATION3);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }
            }
        });

        fabContractMenu = (FloatingActionMenu) findViewById(R.id.fab_contractMenu1);
        fabToCharter = (FloatingActionButton) findViewById(R.id.fab_toChartering1);
        fabViewContracts = (FloatingActionButton) findViewById(R.id.fab_viewContracts1);

        fabContractMenu.setIconAnimated(false);
        fabContractMenu.setMenuButtonColorNormal(Color.parseColor("#F68B1F"));
        fabToCharter.setOnClickListener(clickListener);
        fabToCharter.setColorNormal(Color.parseColor("#F68B1F"));
        fabViewContracts.setOnClickListener(clickListener);
        fabViewContracts.setColorNormal(Color.parseColor("#F68B1F"));

        btnAddPoint1 = (ImageButton) findViewById(R.id.btn_add_pickup_1);
        btnAddPoint2 = (ImageButton) findViewById(R.id.btn_add_pickup_2);
        llAdditionalPickupPoint1 = (LinearLayout) findViewById(R.id.ll_additional_pickup_view_1);
        llAdditionalPickupPoint2 = (LinearLayout) findViewById(R.id.ll_additional_pickup_view_2);

        btnAddPoint1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (didAddNewRow1) {
                    llAdditionalPickupPoint1.setVisibility(View.GONE);
                    llAdditionalPickupPoint2.setVisibility(View.GONE);
                    txtPickupPoint2.setText("");
                    txtPickupPoint3.setText("");
                    pickUp2 = "";
                    pickUp3 = "";
                    latlng2 = new LatLng(0,0);
                    latlng3 = new LatLng(0,0);
                    didAddNewRow1 = false;
                    didAddNewRow2 = false;
                    btnAddPoint1.setBackgroundResource(R.drawable.addrow);
                    btnAddPoint2.setBackgroundResource(R.drawable.addrow);
                    if (pickUpPointMarker2 != null) {
                        pickUpPointMarker2.remove();
                    }
                    if (pickUpPointMarker3 != null) {
                        pickUpPointMarker3.remove();
                    }
                } else {
                    llAdditionalPickupPoint1.setVisibility(View.VISIBLE);
                    didAddNewRow1 = true;
                    btnAddPoint1.setBackgroundResource(R.drawable.minusrow);
                }
            }
        });

        btnAddPoint2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (didAddNewRow2) {
                    llAdditionalPickupPoint2.setVisibility(View.GONE);
                    txtPickupPoint3.setText("");
                    pickUp3 = "";
                    latlng3 = new LatLng(0,0);
                    didAddNewRow2 = false;
                    btnAddPoint2.setBackgroundResource(R.drawable.addrow);
                    if (pickUpPointMarker3 != null) {
                        pickUpPointMarker3.remove();
                    }
                } else {
                    llAdditionalPickupPoint2.setVisibility(View.VISIBLE);
                    didAddNewRow2 = true;
                    btnAddPoint2.setBackgroundResource(R.drawable.minusrow);
                }
            }
        });

        btnNext = (Button) findViewById(R.id.btn_toDropOffPage);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pickUp1.equalsIgnoreCase("")) {
                    toast(Constants.NO_POINTS_SELECTED + "pick up.");
                } else {
                    Intent intent = new Intent(PickUpPointActivity.this, DropOffPointActivity.class);
                    intent.putExtra(Constants.PICKUP_POINT_1_NAME, pickUp1);
                    intent.putExtra(Constants.PICKUP_POINT_2_NAME, pickUp2);
                    intent.putExtra(Constants.PICKUP_POINT_3_NAME, pickUp3);
                    intent.putExtra(Constants.PICKUP_POINT_1_LATITUDE, latlng1.latitude);
                    intent.putExtra(Constants.PICKUP_POINT_2_LATITUDE, latlng2.latitude);
                    intent.putExtra(Constants.PICKUP_POINT_3_LATITUDE, latlng3.latitude);
                    intent.putExtra(Constants.PICKUP_POINT_1_LONGITUDE, latlng1.longitude);
                    intent.putExtra(Constants.PICKUP_POINT_2_LONGITUDE, latlng2.longitude);
                    intent.putExtra(Constants.PICKUP_POINT_3_LONGITUDE, latlng3.longitude);
                    startActivity(intent);
                }
            }
        });

        //Opening Speech
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(Constants.CONTRACT_PICKUP_POINT_INITIAL_TEXT)
                .setTitle("Note:")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    private void initialiseValues() {
        didAddNewRow1 = false;
        didAddNewRow2 = false;
        mPickUpPoint1 = new MarkerOptions();
        mPickUpPoint2 = new MarkerOptions();
        mPickUpPoint3 = new MarkerOptions();
        pickUp1 = "";
        pickUp2 = "";
        pickUp3 = "";
        latlng1 = new LatLng(0,0);
        latlng2 = new LatLng(0,0);
        latlng3 = new LatLng(0,0);

    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        Intent intent;
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.fab_toChartering1:
                    intent = new Intent(getApplicationContext(), NewCharterActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case R.id.fab_viewContracts1:
                    intent = new Intent(getApplicationContext(), ViewAllContracts.class);
                    startActivity(intent);
                    finish();
                    break;
            }
        }
    };

    @Override
    public void onBackPressed() {
        if (isTaskRoot()) {
            //do nothing
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            enableLocationPermission();
        }
        //zoom to current position:
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(SINGAPORE).zoom(10.2f).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
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
    public void onLocationChanged(Location location) {
        //do nothing
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SINGAPORE, 10.2f));
        enableLocationPermission();

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE_STARTLOCATION1) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                if (pickUpPointMarker1 != null) {
                    pickUpPointMarker1.remove();
                }
                latlng1 = place.getLatLng();
                pickUp1 = "" + place.getName();
                txtPickupPoint1.setText(pickUp1);
                mPickUpPoint1.position(latlng1)
                        .icon(BitmapDescriptorFactory.fromBitmap(writeTextOnMarker(R.drawable.purple_marker, "1")));
                pickUpPointMarker1 = mMap.addMarker(mPickUpPoint1);

                Log.i(LOG_TAG, "Place: " + pickUp1);
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i(LOG_TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        } else if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE_STARTLOCATION2) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                if (pickUpPointMarker2 != null) {
                    pickUpPointMarker2.remove();
                }
                latlng2 = place.getLatLng();
                pickUp2 = "" + place.getName();
                txtPickupPoint2.setText(pickUp2);
                mPickUpPoint2.position(latlng2)
                        .icon(BitmapDescriptorFactory.fromBitmap(writeTextOnMarker(R.drawable.purple_marker, "2")));
                pickUpPointMarker2 = mMap.addMarker(mPickUpPoint2);

                Log.i(LOG_TAG, "Place: " + pickUp2);
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i(LOG_TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        } else if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE_STARTLOCATION3) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                if (pickUpPointMarker3 != null) {
                    pickUpPointMarker3.remove();
                }
                latlng3 = place.getLatLng();
                pickUp3 = "" + place.getName();
                txtPickupPoint3.setText(pickUp3);
                mPickUpPoint3.position(latlng3)
                        .icon(BitmapDescriptorFactory.fromBitmap(writeTextOnMarker(R.drawable.purple_marker, "3")));
                pickUpPointMarker3 = mMap.addMarker(mPickUpPoint3);

                Log.i(LOG_TAG, "Place: " + pickUp3);
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i(LOG_TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    private Bitmap writeTextOnMarker(int drawableId, String text) {

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
        if(textRect.width() >= (canvas.getWidth() - 4))     //the padding on either sides is considered as 4, so as to appropriately fit in the text
            paint.setTextSize(convertToPixels(context, 25));        //Scaling needs to be used for different dpi's

        //Calculate the positions
        int xPos = (canvas.getWidth() / 2) - 2;     //-2 is for regulating the x position offset
        float yPos = (float) (canvas.getHeight() / 2.5);

        canvas.drawText(text, xPos, yPos, paint);

        return bm;
    }

    public static int convertToPixels(Context context, int nDP) {
        final float conversionScale = context.getResources().getDisplayMetrics().density;

        return (int) ((nDP * conversionScale) + 0.5f) ;

    }

    private void toast(String text){
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        toast.show();
    }
}
