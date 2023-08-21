package sg.com.commute_solutions.bustracker.fragments.charters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import sg.com.commute_solutions.bustracker.R;
import sg.com.commute_solutions.bustracker.common.Constants;
import sg.com.commute_solutions.bustracker.common.Preferences;
import sg.com.commute_solutions.bustracker.data.Charters;
import sg.com.commute_solutions.bustracker.data.Drivers;
import sg.com.commute_solutions.bustracker.fragments.LoginActivity;
import sg.com.commute_solutions.bustracker.util.OnMapAndViewReadyListener;
import sg.com.commute_solutions.bustracker.util.StringUtil;
import sg.com.commute_solutions.bustracker.webservices.WebServiceTask;
import sg.com.commute_solutions.bustracker.webservices.WebServiceUtils;

/**
 * Created by Kyle on 24/4/17.
 */

public class ViewCharterActivity extends AppCompatActivity
        implements Serializable,
        OnMapReadyCallback,
        OnMapAndViewReadyListener.OnGlobalLayoutAndMapReadyListener,
        ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String LOG_TAG = ViewCharterActivity.class.getSimpleName();

    private GoogleMap mMap;
    private static final LatLng SINGAPORE = new LatLng(1.360405, 103.819363);

    private Context context;
    private SharedPreferences prefs;
    private String authToken;
//    private String driverRole;
    private final ContentValues authenticationToken = new ContentValues();
    private String intention;

    private Charters charter;
    private ArrayList<Drivers> drivers;
    private ArrayList<String> withdrawReasons;

    private AlertDialog loadingScreen;
    private TextView title;
    private TableLayout selectedCharter;
    private int noOfPickUpPoints, noOfDropOffPoints;

    private JSONObject obj, jsonObject;
    private ContentValues contentValues;
    private String displayMessage;
    private RetrieveDriverListTask mRetrieveDriverListTask;
    private AcceptJobsTask mAcceptJobsTask;
    private UpdateDriverTask mUpdateDriverTask;
    private UpdatePOCTask mUpdatePOCTask;
    private DeleteJobsTask mDeleteJobTask;
    private WithdrawJobTask mWithdrawJobTask;
    private NewDisputeTask mNewDisputeTask;
    private String myDescription = "";

    private ClipboardManager myClipboard;
    private ClipData myClip;

    private String charterId, driverId, driverName, driverContactNo, vehicleNo, pocName, pocContactNo, reasons;
    private int driverNum;

    private boolean isUpdateDriver = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_charters);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        context = this;

        LayoutInflater lI = LayoutInflater.from(context);
        View promptView = lI.inflate(R.layout.loading_screen, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(promptView);

        loadingScreen = alertDialogBuilder.create();
        loadingScreen.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        loadingScreen.setCanceledOnTouchOutside(false);

        prefs = this.getSharedPreferences(Constants.SHARE_PREFERENCE_PACKAGE, Context.MODE_PRIVATE);
//        driverRole = prefs.getString(Preferences.ROLE, "");
        authToken = StringUtil.deNull(prefs.getString(Preferences.AUTH_TOKEN, ""));
        authenticationToken.put(Constants.TOKEN, authToken);

        Intent intent = getIntent();
        Gson gson = new Gson();
        String jsonString = intent.getStringExtra("selectedCharter");
        charter = gson.fromJson(jsonString, Charters.class);
        charterId = charter.getId();

        intention = intent.getStringExtra("intention");
        drivers = new ArrayList<>();
        driverName = StringUtil.deNull(charter.getDriverName());
        driverContactNo = StringUtil.deNull(charter.getDriverContactNo());
        vehicleNo = StringUtil.deNull(charter.getVehicleNo());

        if (StringUtil.deNull(intention).equalsIgnoreCase("acceptedCharters")) {
            if (drivers.size() > 0) {
                try {
                    mRetrieveDriverListTask = new RetrieveDriverListTask();
                    mRetrieveDriverListTask.execute((Void) null);
                } catch (Exception e) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage(Constants.CANNOT_FETCH_DRIVER_LIST)
                            .setCancelable(false)
                            .setPositiveButton("Understood", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();
                }
            }
            String jsonString2 = getIntent().getStringExtra("reasons");
            withdrawReasons = gson.fromJson(jsonString2, new TypeToken<ArrayList<String>>() {}.getType());
        } else {
            withdrawReasons = new ArrayList<>();
        }

        title = (TextView) findViewById(R.id.lbl_Title);
        title.setText("View charter");

        initCharterDetails();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.selected_charter_map);
        new OnMapAndViewReadyListener(mapFragment, this);

        if (intention != null && charter.isAccepted()) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(Constants.TRACKING_CHARTER_MESSAGE)
                    .setCancelable(true).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        addMarkers();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SINGAPORE, 9.5f));
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (intention != null && charter.isAccepted()) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(charter.getTrackingUrl()));
                    startActivity(browserIntent);
                }
            }
        });
    }

    private void initCharterDetails() {
        ScrollView.LayoutParams tableRowParams = new ScrollView.LayoutParams (TableLayout.LayoutParams.FILL_PARENT,TableLayout.LayoutParams.WRAP_CONTENT);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
        selectedCharter = (TableLayout) findViewById(R.id.table_selectedCharter);
        selectedCharter.removeAllViews();
        selectedCharter.setLayoutParams(tableRowParams);
        boolean isDisposal = false;
        if (charter.getDisposalDuration() > 0) {
            isDisposal = true;
        }
        String text;

        //accept
        TableRow goToGoogleMap = new TableRow(context);
        goToGoogleMap.setLayoutParams(lp);

        Button pickUpBtn = new Button(getApplicationContext());
        pickUpBtn.setText("View PickUp Point");
        pickUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String geoUriString = "geo:0,0?q=" + charter.getPickupLatitude()[0] + "," + charter.getPickupLongitude()[0] + "(" + charter.getPickupName()[0] +")";
                Uri gmmIntentUri = Uri.parse(geoUriString);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });
        Button dropOffBtn = new Button(getApplicationContext());
        dropOffBtn.setText("View DropOff Point");
        dropOffBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String geoUriString = "geo:0,0?q=" + charter.getDropOffLatitude()[0] + "," + charter.getDropOffLongitude()[0] + "(" + charter.getDropOffName()[0] +")";
                Uri gmmIntentUri = Uri.parse(geoUriString);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });

        LinearLayout googleMapBtnLayout = new LinearLayout(context);
        googleMapBtnLayout.addView(pickUpBtn);
        googleMapBtnLayout.addView(dropOffBtn);
        goToGoogleMap.addView(googleMapBtnLayout);
        selectedCharter.addView(goToGoogleMap);

        //ID
        TableRow idRow = new TableRow(context);
        idRow.setLayoutParams(lp);
        TextView idTextView = new TextView(getApplicationContext());
        text = "<br/><b>ID: " + charter.getAccessCode() + "</b>";

        myDescription += "ID: " + charter.getAccessCode() + System.getProperty("line.separator");
        idTextView.setText(Html.fromHtml(text));
        idTextView.setTextSize(17.0f);
        idTextView.setTextColor(Color.BLACK);
        idRow.addView(idTextView);
        selectedCharter.addView(idRow);

        //date
        TableRow row1 = new TableRow(context);
        row1.setLayoutParams(lp);
        TextView date = new TextView(getApplicationContext());
        text = "<b><br/>" + charter.getDate() + "</b>";
        myDescription += ""+charter.getDate()+ System.getProperty("line.separator");
        date.setText(Html.fromHtml(text));
        date.setTextSize(17.0f);
        date.setTextColor(Color.BLACK);
        row1.addView(date);
        selectedCharter.addView(row1);

        //time
        String[] pickUpDropOfftime = charter.getTime();
        TableRow row2 = new TableRow(context);
        row2.setLayoutParams(lp);
        TextView time = new TextView(getApplicationContext());
//        if (pickUpDropOfftime.length == 1) {
        text = "<b>" + pickUpDropOfftime[0] + " ($" + charter.getCost() + ")</b> ";
        myDescription += pickUpDropOfftime[0] + " ($" + charter.getCost() + ")"+ System.getProperty("line.separator");
//        } else {
//            if (charter.getDisposalDuration() > 0) {
//                isDisposal = true;
//                text = "<b>" + pickUpDropOfftime[0] + " - " + pickUpDropOfftime[1] + " ($" + charter.getCost() + "/hr)</b> ";
//            } else {
//                text = "<b>" + pickUpDropOfftime[0] + " | " + pickUpDropOfftime[1] + " ($" + charter.getCost() + ")</b> ";
//            }
//        }
        text = new StringBuilder(text).append("<br/>").toString();
        time.setText(Html.fromHtml(text));
        time.setTextColor(Color.BLACK);
        time.setTextSize(17.0f);
        row2.addView(time);
        selectedCharter.addView(row2);

        //pickup name
        String[] pickupPointName = charter.getPickupName();
        noOfPickUpPoints = pickupPointName.length;
        for (int i = 0; i < noOfPickUpPoints; i++) {
            TableRow row3 = new TableRow(context);
            row3.setLayoutParams(lp);
            TextView pickupPoint = new TextView(getApplicationContext());
            pickupPoint.setSingleLine(false);
            if (i == 0) {
                text = "<b><font color='#28C546'>From</font>: </b>" + pickupPointName[i];
                myDescription += "From : " + pickupPointName[i]+ System.getProperty("line.separator");
                try {
                    text = new StringBuilder(text).insert(77, "<br/>").toString();
                } catch (Exception e) {
                    //do nothing
                }
                if (pickupPointName.length > 1) {
                    text = new StringBuilder(text).append("<br/><b>Additional points(Not Shown in Map): </b>").toString();
                    myDescription += "Additional points(Not Shown in Map): "+System.getProperty("line.separator");
                }
            } else {
                text = "<b>" + (i + 1) +": </b>" + pickupPointName[i];
                myDescription += pickupPointName[i]+ System.getProperty("line.separator");
                try {
                    text = new StringBuilder(text).insert(45, "<br/>").toString();
                } catch (Exception e) {
                    //do nothing
                }
            }
            pickupPoint.setText(Html.fromHtml(text));
            pickupPoint.setTextColor(Color.BLACK);
            pickupPoint.setTextSize(17.0f);
            row3.addView(pickupPoint);
            selectedCharter.addView(row3);
        }

        //dropoff name
        String[] dropoffPointName = charter.getDropOffName();
        noOfDropOffPoints = dropoffPointName.length;
        for (int i = 0; i < noOfDropOffPoints; i++) {
            TableRow row4 = new TableRow(context);
            row4.setLayoutParams(lp);
            TextView dropOffPoint = new TextView(getApplicationContext());
            dropOffPoint.setSingleLine(false);
            if (i == 0) {
                text = "<b><br/><font color='#289FC5'>To</font>: </b>" + dropoffPointName[i];
                myDescription += "To : " + dropoffPointName[i] +System.getProperty("line.separator");
                try {
                    text = new StringBuilder(text).insert(77, "<br/>").toString();
                } catch (Exception e) {
                    //do nothing
                }
                if (dropoffPointName.length > 1) {
                    text = new StringBuilder(text).append("<br/><b>Additional points(Not Shown in Map): </b>").toString();
                    myDescription +="Additional points(Not Shown in Map): "+System.getProperty("line.separator");
                }
            } else {
                text = "<b>" + (i + 1) +": </b><u>" + dropoffPointName[i] + "</u>";
                try {
                    text = new StringBuilder(text).insert(45, "<br/>").toString();
                } catch (Exception e) {
                    //do nothing
                }
                myDescription += dropoffPointName[i] + System.getProperty("line.separator");
            }

            dropOffPoint.setText(Html.fromHtml(text));
            dropOffPoint.setTextColor(Color.BLACK);
            dropOffPoint.setTextSize(17.0f);
            row4.addView(dropOffPoint);
            selectedCharter.addView(row4);
        }

        //bus type
        TableRow row5 = new TableRow(context);
        row5.setLayoutParams(lp);
        TextView busType = new TextView(getApplicationContext());
        text = "<b><br/>Bus Type: </b>" + charter.getBusType() + " - Seater";
        myDescription += "Bus Type: " + charter.getBusType() + " - Seater" + System.getProperty("line.separator");
        text = new StringBuilder(text).append("<br/>").toString();
        busType.setText(Html.fromHtml(text));
        busType.setTextColor(Color.BLACK);
        busType.setTextSize(17.0f);
        row5.addView(busType);
        selectedCharter.addView(row5);

        //disposal
        if (isDisposal) {
            TableRow row6 = new TableRow(context);
            row6.setLayoutParams(lp);
            TextView charteringHrs = new TextView(getApplicationContext());
            text = "<b>No. of Hours for disposal: </b>" + charter.getDisposalDuration();
            myDescription += "No. of Hours for disposal: " + charter.getDisposalDuration() + System.getProperty("line.separator");
            charteringHrs.setText(Html.fromHtml(text));
            charteringHrs.setTextColor(Color.BLACK);
            charteringHrs.setTextSize(17.0f);
            row6.addView(charteringHrs);
            selectedCharter.addView(row6);
        }

        //remarks
        if (charter.getRemark() != null && !charter.getRemark().equalsIgnoreCase("null")) {
            TableRow row7 = new TableRow(context);
            row7.setLayoutParams(lp);
            TextView remarks = new TextView(getApplicationContext());

            text = "<b>Remarks: </b>" + charter.getRemark();
            myDescription += "Remarks: " + charter.getRemark() + System.getProperty("line.separator");

            try {
                text = new StringBuilder(text).insert(45, "<br/>").toString();
            } catch (Exception e) {
                //do nothing
            }
            remarks.setText(Html.fromHtml(text));
            remarks.setTextColor(Color.BLACK);
            remarks.setTextSize(17.0f);
            row7.addView(remarks);
            selectedCharter.addView(row7);
        }

        //poc details
        if (intention != null) {
            TableRow row8 = new TableRow(context);
            row8.setLayoutParams(lp);

            TextView pocDetails = new TextView(getApplicationContext());

            text = "<b><br/>POC Name: </b>" + charter.getPocName();
            text = new StringBuilder(text).append("<b><br/>POC Contact Number: </b><u>").append(charter.getPocContactNo()).append("</u>").toString();
//        text = new StringBuilder(text).append("<b><br/>POC Email: </b>").append(charter.getPocEmail()).toString();
//        text = new StringBuilder(text).append("<b><br/>POC Company: </b>").append(charter.getPocCompany()).toString();
            text = new StringBuilder(text).append("<br/>").toString();
            pocDetails.setText(Html.fromHtml(text));
            pocDetails.setTextColor(Color.BLACK);
            pocDetails.setTextSize(17.0f);
            row8.addView(pocDetails);
            row8.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (charter.getPocContactNo().isEmpty() || charter.getPocContactNo().equalsIgnoreCase("")){
                        //do nothing
                    } else {
                        Intent call = new Intent(Intent.ACTION_DIAL);
                        call.setData(Uri.parse("tel:" + charter.getPocContactNo()));
                        startActivity(call);
                    }
                }
            });
            selectedCharter.addView(row8);
        }

        //driver details
        if (StringUtil.deNull(intention).equalsIgnoreCase("acceptedCharters") || StringUtil.deNull(intention).equalsIgnoreCase("ownCharter")) {
            TableRow row9 = new TableRow(context);
            row9.setLayoutParams(lp);
            TextView driverDetails = new TextView(getApplicationContext());

            text = "<b>Driver Name: </b>" + driverName;
            text = new StringBuilder(text).append("<b><br/>Driver Contact No.: </b>").append(driverContactNo).toString();
            text = new StringBuilder(text).append("<b><br/>Vehicle No.: </b>").append(vehicleNo).toString();
            text = new StringBuilder(text).append("<br/>").toString();
            driverDetails.setText(Html.fromHtml(text));
            driverDetails.setTextColor(Color.BLACK);
            driverDetails.setTextSize(17.0f);
            row9.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!driverContactNo.isEmpty() && !driverContactNo.equalsIgnoreCase("")){
                        Intent call = new Intent(Intent.ACTION_DIAL);
                        call.setData(Uri.parse("tel:" + driverContactNo));
                        startActivity(call);
                    }
                }
            });
            row9.addView(driverDetails);
            selectedCharter.addView(row9);
        }

        //buttons
        boolean isMyCharter = false;
        try {
            isMyCharter = charter.isMyCharter();
        } catch (Exception e){
            //do nothing
        }

        boolean canResubmit = false;
        try {
            canResubmit = charter.isCanResubmit();
        } catch (Exception e){
            //do nothing
        }

        TableRow row10 = new TableRow(context);
        row10.setLayoutParams(lp);
        LinearLayout btnLayout = new LinearLayout(context);
        if (!isMyCharter) {
            if (intention == null) {
                Button acceptBtn = new Button(getApplicationContext());
                acceptBtn.setText("Accept");
                acceptBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showLoadingScreen();
                        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setMessage(Constants.ACCEPT_CONFIRMATION_MESSAGE)
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        mAcceptJobsTask = new AcceptJobsTask(charterId, null);
                                        mAcceptJobsTask.execute((Void) null);
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        hideLoadingScreen();
                                        dialog.dismiss();
                                    }
                                }).show();
                    }
                });
                Button cancelBtn = new Button(getApplicationContext());
                cancelBtn.setText("Back");
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(), AvailableCharterActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });

                Button copyBtn = new Button(getApplicationContext());
                copyBtn.setText("Copy Description");
                copyBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("URL", myDescription);
                        clipboard.setPrimaryClip(clip);
                        Toast toast = Toast.makeText(context, "Description Copied to Clipboard", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
                btnLayout.addView(acceptBtn);
                btnLayout.addView(cancelBtn);
                btnLayout.addView(copyBtn);

                row10.addView(btnLayout);
                selectedCharter.addView(row10);
            } else if (StringUtil.deNull(intention).equalsIgnoreCase("ownCharter")) {
                if (!charter.isCompleted()) {
                    if (charter.isAccepted()) {
                        Button editBtn = new Button(getApplicationContext());
                        editBtn.setText("Edit POC");
                        editBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showLoadingScreen();
                                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                LinearLayout layout = new LinearLayout(context);
                                layout.setOrientation(LinearLayout.VERTICAL);
                                final EditText txt_pocName = new EditText(context);
                                final EditText txt_pocContactNo = new EditText(context);
                                txt_pocContactNo.setInputType(InputType.TYPE_CLASS_NUMBER);
                                txt_pocName.setHint("Please enter POC Name");
                                txt_pocContactNo.setHint("Please enter POC Contact No.");
                                layout.addView(txt_pocName);
                                layout.addView(txt_pocContactNo);

                                builder.setMessage(Constants.POC_CONFIRMATION_MESSAGE)
                                        .setView(layout)
                                        .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                pocName = txt_pocName.getText().toString();
                                                pocContactNo = txt_pocContactNo.getText().toString();
                                                dialog.dismiss();
                                                mUpdatePOCTask = new UpdatePOCTask(charterId, pocName, pocContactNo);
                                                mUpdatePOCTask.execute((Void) null);
                                            }
                                        })
                                        .setNegativeButton("Back", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                hideLoadingScreen();
                                                dialog.dismiss();
                                            }
                                        })
                                        .show();
                            }
                        });
                        btnLayout.addView(editBtn);
                    }

                    Button deleteBtn = new Button(getApplicationContext());
                    deleteBtn.setText("Remove Listing");
                    deleteBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showLoadingScreen();
                            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setMessage(Constants.DELETE_CONFIRMATION_MESSAGE)
                                    .setCancelable(false)
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            mDeleteJobTask = new DeleteJobsTask(charterId, "");
                                            mDeleteJobTask.execute((Void) null);
                                        }
                                    })
                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            hideLoadingScreen();
                                            dialog.dismiss();
                                        }
                                    }).show();
                        }
                    });
                    btnLayout.addView(deleteBtn);

                    Button copyBtn = new Button(getApplicationContext());
                    copyBtn.setText("Copy Description");
                    copyBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("URL", "TEST");
                            clipboard.setPrimaryClip(clip);
                            Toast toast = Toast.makeText(context, "Description Copied to Clipboard", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    });
                    btnLayout.addView(copyBtn);

                    row10.addView(btnLayout);
                    selectedCharter.addView(row10);
                }
            } else if (StringUtil.deNull(intention).equalsIgnoreCase("acceptedCharters")) {
                if (!charter.isCompleted()) {
                    Button deleteBtn = new Button(getApplicationContext());
                    final CharSequence[] cs = withdrawReasons.toArray(new CharSequence[withdrawReasons.size()]);
                    final ArrayList selectedItems = new ArrayList();
                    deleteBtn.setText("Withdraw from Charter");
                    deleteBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showLoadingScreen();
                            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setMessage(Constants.DELETE_CONFIRMATION_MESSAGE2)
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            final AlertDialog.Builder builder2 = new AlertDialog.Builder(context);
                                            builder2.setTitle("Select a reason")
                                                    .setMultiChoiceItems(cs, null, new DialogInterface.OnMultiChoiceClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                                            if (isChecked) {
                                                                // If the user checked the item, add it to the selected items
                                                                selectedItems.add(which);
                                                            } else if (selectedItems.contains(which)) {
                                                                // Else, if the item is already in the array, remove it
                                                                selectedItems.remove(Integer.valueOf(which));
                                                            }
                                                        }
                                                    })
                                                    .setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            hideLoadingScreen();
                                                            reasons = "";
                                                            for (int i = 0; i < selectedItems.size(); i++) {
                                                                String index = selectedItems.get(i).toString();
                                                                reasons += withdrawReasons.get(Integer.parseInt(index)) + " ";
                                                            }
                                                            if (reasons.isEmpty() || reasons.equalsIgnoreCase("")) {
                                                                dialog.dismiss();
                                                            } else {
                                                                mWithdrawJobTask = new WithdrawJobTask(charterId, reasons);
                                                                mWithdrawJobTask.execute((Void) null);
                                                            }
                                                        }
                                                    })
                                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            selectedItems.clear();
                                                            hideLoadingScreen();
                                                            dialog.dismiss();
                                                        }
                                                    }).show();

                                        }
                                    })
                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            hideLoadingScreen();
                                            dialog.dismiss();
                                        }
                                    }).show();
                        }
                    });
//                    if (driverRole.equalsIgnoreCase("admin")) {
                    if (drivers.size() > 0) {
                        Button editBtn = new Button(getApplicationContext());
                        editBtn.setText("Edit Driver");
                        editBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showLoadingScreen();
                                displayMessage = "Please select a driver you wish to change to.";
                                isUpdateDriver = true;
                                showDriverSpinnerSelection();
                            }
                        });

                        btnLayout.addView(editBtn);

                    }
                    Button copyBtn = new Button(getApplicationContext());
                    copyBtn.setText("Copy Description");
                    copyBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("URL", "TEST");
                            clipboard.setPrimaryClip(clip);
                            Toast toast = Toast.makeText(context, "Description Copied to Clipboard", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    });
                    btnLayout.addView(deleteBtn);
                    btnLayout.addView(copyBtn);
                    row10.addView(btnLayout);
                    selectedCharter.addView(row10);
                }
            }
        } else {
            if (!charter.isCompleted()) {
                Button deleteBtn = new Button(getApplicationContext());
                deleteBtn.setText("Remove Listing");
                deleteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showLoadingScreen();
                        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setMessage(Constants.DELETE_CONFIRMATION_MESSAGE)
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        mDeleteJobTask = new DeleteJobsTask(charterId, "");
                                        mDeleteJobTask.execute((Void) null);
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        hideLoadingScreen();
                                        dialog.dismiss();
                                    }
                                }).show();
                    }
                });
                btnLayout.addView(deleteBtn);
                row10.addView(btnLayout);
                selectedCharter.addView(row10);
            }
        }

        if (charter.isDisputable()) {
            TableRow row11 = new TableRow(context);
            row11.setLayoutParams(lp);
            LinearLayout btnLayout3 = new LinearLayout(context);

            Button reSubmitBtn = new Button(getApplicationContext());
            reSubmitBtn.setText("Remove Listing");
            reSubmitBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ViewCharterActivity.this, UpdateCharterActivity.class);
                    intent.putExtra(Constants.BUS_CHARTER_ID, charterId);
                    intent.putExtra(Constants.EXTRA, "expiry");
                    startActivity(intent);
                }
            });

            btnLayout3.addView(reSubmitBtn);
            row11.addView(btnLayout3);
            selectedCharter.addView(row11);
        }

        if (canResubmit) {
            TableRow row12 = new TableRow(context);
            row12.setLayoutParams(lp);
            LinearLayout btnLayout2 = new LinearLayout(context);

            Button disputeBtn = new Button(getApplicationContext());
            disputeBtn.setText("Resubmit");
            disputeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    final EditText txtDesc = new EditText(context);
                    txtDesc.setHint("Please key in your argument for this dispute");

                    showLoadingScreen();
                    builder.setView(txtDesc)
                            .setCancelable(false)
                            .setPositiveButton("Next", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    final String description = txtDesc.getText().toString();
                                    final AlertDialog.Builder builder2 = new AlertDialog.Builder(context);
                                    final EditText txtCost = new EditText(context);
                                    txtCost.setInputType(InputType.TYPE_CLASS_NUMBER);
                                    builder2.setTitle("Compensation Amount ($)")
                                            .setView(txtCost)
                                            .setCancelable(false)
                                            .setPositiveButton("Submit my argument", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    try {
                                                        dialog.dismiss();
                                                        Double cost = Double.parseDouble(txtCost.getText().toString());

                                                        mNewDisputeTask = new NewDisputeTask(charter.getId(), description, cost);
                                                        mNewDisputeTask.execute((Void) null);
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
            });

            btnLayout2.addView(disputeBtn);
            row12.addView(btnLayout2);
            selectedCharter.addView(row12);
        }
    }

    private void addMarkers() {
        //pick up
        for (int i = 0; i < noOfPickUpPoints; i++) {
            String[] arr = charter.getPickupLatitude();
            double coordinate1 = Double.parseDouble(arr[i]);
            arr = charter.getPickupLongitude();
            double coordinate2 = Double.parseDouble(arr[i]);
            LatLng pickupPoint = new LatLng(coordinate1, coordinate2);
            mMap.addMarker(new MarkerOptions()
                    .position(pickupPoint)
                    .title(charter.getPickupName()[i])
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.green_marker)));
        }

        //drop off
        for (int i = 0; i < noOfDropOffPoints; i++) {
            String[] arr = charter.getDropOffLatitude();
            double coordinate1 = Double.parseDouble(arr[i]);
            arr = charter.getDropOffLongitude();
            double coordinate2 = Double.parseDouble(arr[i]);
            LatLng dropOffPoint = new LatLng(coordinate1, coordinate2);
            mMap.addMarker(new MarkerOptions()
                    .position(dropOffPoint)
                    .title(charter.getDropOffName()[i])
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.blue_marker)));
        }
    }

    private void showDriverSpinnerSelection() {
        LayoutInflater lI = LayoutInflater.from(context);
        View promptView = lI.inflate(R.layout.spinner_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(promptView);

        alertDialogBuilder.setTitle(displayMessage);
        alertDialogBuilder.setIcon(R.drawable.info_icon_blue);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        final Spinner driverList = (Spinner) promptView.findViewById(R.id.spinner_list);

        ArrayList<String> driverNameArray = new ArrayList<>();
        for (int i = 0; i < drivers.size(); i++) {
            driverNameArray.add(drivers.get(i).getName());
        }
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, driverNameArray);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        driverList.setAdapter(arrayAdapter);

        driverList.setOnItemSelectedListener(new OnSpinnerItemClicked());

        Button acceptBtn = (Button) promptView.findViewById(R.id.btn_acceptCharter);
        acceptBtn.setText("Assign");
        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                driverId = drivers.get(driverNum).getId();
                if (isUpdateDriver) {
                    isUpdateDriver = false;
                    mUpdateDriverTask = new UpdateDriverTask(charterId, driverId);
                    mUpdateDriverTask.execute((Void) null);
                } else {
                    mAcceptJobsTask = new AcceptJobsTask(charterId, driverId);
                    mAcceptJobsTask.execute((Void) null);
                }
            }
        });

        Button cancelSelectionBtn = (Button) promptView.findViewById(R.id.btn_reselectCharter);
        cancelSelectionBtn.setText("Return to Listing Page");
        cancelSelectionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AvailableCharterActivity.class);
                startActivity(intent);
                finish();
            }
        });

        alertDialog.show();
        alertDialog.setCanceledOnTouchOutside(false);
    }

    private class OnSpinnerItemClicked implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            driverNum = position;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            driverNum = 0;
        }
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
                        ViewCharterActivity.this.finishAffinity();
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

    private class RetrieveDriverListTask extends WebServiceTask {
        RetrieveDriverListTask(){
            super(ViewCharterActivity.this);
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
            String URL = Constants.RETRIEVEDRIVERLIST_URL;
            obj = WebServiceUtils.requestJSONObject(URL, WebServiceUtils.METHOD.GET, authenticationToken, context);
            if(!hasError(obj)) {
                Log.d(LOG_TAG, obj.toString());
                jsonObject = obj.optJSONObject(Constants.DATA);

                try {
                    JSONArray driverList = jsonObject.optJSONArray(Constants.BUS_CHARTER_DRIVER_LIST);
                    Boolean success = jsonObject.getBoolean(Constants.BUS_CHARTER_SUCCESS);

                    if (success) {
                        drivers = new ArrayList<>();
                        if (driverList != null && driverList.length() > 0) {
                            for (int i = 0; i < driverList.length(); i++) {
                                JSONObject objPx = driverList.getJSONObject(i);
                                String id = objPx.getString(Constants.BUS_CHARTER_DRIVER_ID);
                                String name = objPx.getString(Constants.BUS_CHARTER_DRIVER_NAME);
                                Drivers driver = new Drivers(id, name);
                                drivers.add(driver);
                            }
                        }
                    }
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Error processing Json data = " + e.getMessage());
                }
                return true;
            }
            return false;
        }

        @Override
        public void performSuccessfulOperation() {
            Log.d(LOG_TAG, "Data Retrieved Successful!");
        }

        @Override
        public void onFailedAttempt() {
            int statusCode = prefs.getInt(Preferences.STATUSCODE, 0);
            if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                resetApp();
            }
        }
    }

    private class AcceptJobsTask extends WebServiceTask {
        AcceptJobsTask(String jobId, String driverId){
            super(ViewCharterActivity.this);
            contentValues = new ContentValues();
            contentValues.put(Constants.BUS_CHARTER_ID, jobId);
            try {
                contentValues.put(Constants.BUS_CHARTER_DRIVER_ID, driverId);
            } catch (Exception e) {
                //do nothing
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
            obj = WebServiceUtils.requestJSONObject(Constants.ACCEPTCHARTER_URL, WebServiceUtils.METHOD.POST, authenticationToken, null, context, contentValues);
            if(!hasError(obj)) {
                Log.d(LOG_TAG, obj.toString());
                jsonObject = obj.optJSONObject(Constants.DATA);

                if (jsonObject == null) {
                    return false;
                } else {
                    try {
                        JSONArray driverList = jsonObject.optJSONArray(Constants.BUS_CHARTER_DRIVER_LIST);
                        Boolean success = jsonObject.getBoolean(Constants.BUS_CHARTER_SUCCESS);
                        displayMessage = jsonObject.getString(Constants.BUS_CHARTER_MESSAGE);

                        if (success) {
                            drivers = new ArrayList<>();
//                            if (driverRole.equalsIgnoreCase("admin")) {
                            if (driverList != null && driverList.length() > 0) {
                                for (int i = 0; i < driverList.length(); i++) {
                                    JSONObject objPx = driverList.getJSONObject(i);
                                    String id = objPx.getString(Constants.BUS_CHARTER_DRIVER_ID);
                                    String name = objPx.getString(Constants.BUS_CHARTER_DRIVER_NAME);
                                    Drivers driver = new Drivers(id, name);
                                    drivers.add(driver);
                                }
                            }
//                            }
                        }
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "Error processing Json data = " + e.getMessage());
                    }
                    return true;
                }
            }
            return false;
        }

        @Override
        public void performSuccessfulOperation() {
            if (drivers != null && drivers.size() > 0) {
                showDriverSpinnerSelection();
            } else {
                hideLoadingScreen();
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(displayMessage)
                        .setTitle("Message")
                        .setCancelable(false)
                        .setPositiveButton("Understood", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Intent intent = new Intent(getApplicationContext(), AvailableCharterActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }).show();
            }
        }

        @Override
        public void onFailedAttempt() {
            int statusCode = prefs.getInt(Preferences.STATUSCODE, 0);
            if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                resetApp();
            } else {
                performSuccessfulOperation();
            }
        }
    }

    private class UpdateDriverTask extends WebServiceTask {
        UpdateDriverTask(String jobId, String driverId){
            super(ViewCharterActivity.this);
            contentValues = new ContentValues();
            contentValues.put(Constants.BUS_CHARTER_ID, jobId);
            contentValues.put(Constants.BUS_CHARTER_DRIVER_ID, driverId);

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
            obj = WebServiceUtils.requestJSONObject(Constants.UPDATEDRIVER_URL, WebServiceUtils.METHOD.POST, authenticationToken, null, context, contentValues);
            if(!hasError(obj)) {
                Log.d(LOG_TAG, obj.toString());
                jsonObject = obj.optJSONObject(Constants.DATA);

                if (jsonObject == null) {
                    return false;
                } else {
                    try {
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
            builder.setMessage(displayMessage)
                    .setTitle("Message")
                    .setCancelable(false)
                    .setPositiveButton("Understood", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Intent intent = new Intent(getApplicationContext(), YourCharterActivity.class);
                            intent.putExtra("intention","acceptedCharters");
                            startActivity(intent);
                            finish();
                        }
                    }).show();
        }

        @Override
        public void onFailedAttempt() {
            int statusCode = prefs.getInt(Preferences.STATUSCODE, 0);
            if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                resetApp();
            } else {
                performSuccessfulOperation();
            }
        }
    }

    private class UpdatePOCTask extends WebServiceTask {
        boolean success =  false;
        UpdatePOCTask(String jobId, String name, String contactNo){
            super(ViewCharterActivity.this);
            contentValues = new ContentValues();
            contentValues.put(Constants.BUS_CHARTER_ID, jobId);
            contentValues.put(Constants.CHARTER_POC_NAME, name);
            contentValues.put(Constants.CHARTER_POC_CONTACT_NO, contactNo);

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
            obj = WebServiceUtils.requestJSONObject(Constants.UPDATEPOC_URL, WebServiceUtils.METHOD.POST, authenticationToken, null, context, contentValues);
            if(!hasError(obj)) {
                Log.d(LOG_TAG, obj.toString());
                jsonObject = obj.optJSONObject(Constants.DATA);

                if (jsonObject == null) {
                    return false;
                } else {
                    try {
                        success = jsonObject.getBoolean(Constants.BUS_CHARTER_SUCCESS);
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
            builder.setMessage(displayMessage)
                    .setTitle("Message")
                    .setCancelable(false)
                    .setPositiveButton("Understood", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            if (success) {
                                Intent intent = new Intent(getApplicationContext(), YourCharterActivity.class);
                                intent.putExtra("intention","ownCharter");
                                startActivity(intent);
                                finish();
                            }
                        }
                    }).show();
        }

        @Override
        public void onFailedAttempt() {
            int statusCode = prefs.getInt(Preferences.STATUSCODE, 0);
            if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                resetApp();
            } else {
                performSuccessfulOperation();
            }
        }
    }

    private class DeleteJobsTask extends WebServiceTask {
        DeleteJobsTask(String jobId, String reasonString){
            super(ViewCharterActivity.this);
            contentValues = new ContentValues();
            contentValues.put(Constants.BUS_CHARTER_ID, jobId);
            contentValues.put(Constants.REASON, reasonString);

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
            obj = WebServiceUtils.requestJSONObject(Constants.DELETECHARTER_URL, WebServiceUtils.METHOD.POST, authenticationToken, null, context, contentValues);
            if(!hasError(obj)) {
                Log.d(LOG_TAG, obj.toString());
                jsonObject = obj.optJSONObject(Constants.DATA);

                if (jsonObject == null) {
                    return false;
                } else {
                    try {
                        displayMessage = jsonObject.getString(Constants.BUS_CHARTER_MESSAGE);
                        JSONArray reasonArray = jsonObject.optJSONArray(Constants.REASON);
                        withdrawReasons = new ArrayList<>();
                        if (reasonArray != null) {
                            for (int i = 0; i < reasonArray.length(); i++) {
                                withdrawReasons.add(reasonArray.getString(i));
                            }
                        }
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
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            if (withdrawReasons.size() > 0) {
                final CharSequence[] cs = withdrawReasons.toArray(new CharSequence[withdrawReasons.size()]);
                final ArrayList selectedItems = new ArrayList();
                builder.setMessage(displayMessage)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                final AlertDialog.Builder builder2 = new AlertDialog.Builder(context);
                                builder2.setTitle("Select a reason")
                                        .setMultiChoiceItems(cs, null, new DialogInterface.OnMultiChoiceClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                                if (isChecked) {
                                                    // If the user checked the item, add it to the selected items
                                                    selectedItems.add(which);
                                                } else if (selectedItems.contains(which)) {
                                                    // Else, if the item is already in the array, remove it
                                                    selectedItems.remove(Integer.valueOf(which));
                                                }
                                            }
                                        })
                                        .setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                hideLoadingScreen();
                                                reasons = "";
                                                for (int i = 0; i < selectedItems.size(); i++) {
                                                    String index = selectedItems.get(i).toString();
                                                    reasons += withdrawReasons.get(Integer.parseInt(index)) + " ";
                                                }
                                                mDeleteJobTask = new DeleteJobsTask(charterId, reasons);
                                                mDeleteJobTask.execute((Void) null);
                                            }
                                        })
                                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                selectedItems.clear();
                                                hideLoadingScreen();
                                                dialog.dismiss();
                                            }
                                        }).show();

                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                hideLoadingScreen();
                                dialog.dismiss();
                            }
                        }).show();
            } else {
                hideLoadingScreen();
                builder.setMessage(displayMessage)
                        .setTitle("Message")
                        .setCancelable(false)
                        .setPositiveButton("Understood", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Intent intent = new Intent(getApplicationContext(), YourCharterActivity.class);
                                intent.putExtra("intention", "ownCharter");
                                startActivity(intent);
                                finish();
                            }
                        }).show();
            }
        }

        @Override
        public void onFailedAttempt() {
            int statusCode = prefs.getInt(Preferences.STATUSCODE, 0);
            if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                resetApp();
            } else {
                performSuccessfulOperation();
            }
        }
    }

    private class WithdrawJobTask extends WebServiceTask {
        WithdrawJobTask(String jobId, String reasonString){
            super(ViewCharterActivity.this);
            contentValues = new ContentValues();
            contentValues.put(Constants.BUS_CHARTER_ID, jobId);
            contentValues.put(Constants.REASON, reasonString);

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
            obj = WebServiceUtils.requestJSONObject(Constants.WITHDRAWCHARTER_URL, WebServiceUtils.METHOD.POST, authenticationToken, null, context, contentValues);
            if(!hasError(obj)) {
                Log.d(LOG_TAG, obj.toString());
                jsonObject = obj.optJSONObject(Constants.DATA);

                if (jsonObject == null) {
                    return false;
                } else {
                    try {
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
            builder.setMessage(displayMessage)
                    .setTitle("Message")
                    .setCancelable(false)
                    .setPositiveButton("Understood", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Intent intent = new Intent(getApplicationContext(), YourCharterActivity.class);
                            intent.putExtra("intention","acceptedCharters");
                            startActivity(intent);
                            finish();
                        }
                    }).show();
        }

        @Override
        public void onFailedAttempt() {
            int statusCode = prefs.getInt(Preferences.STATUSCODE, 0);
            if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                resetApp();
            } else {
                performSuccessfulOperation();
            }
        }
    }

    private class NewDisputeTask extends WebServiceTask {
        NewDisputeTask(String jobId, String reasonString, double cost){
            super(ViewCharterActivity.this);
            contentValues = new ContentValues();
            contentValues.put(Constants.CHARTER_ID, jobId);
            contentValues.put(Constants.REASON, reasonString);
            contentValues.put(Constants.CHARTER_COST, cost);

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
            obj = WebServiceUtils.requestJSONObject(Constants.NEW_DISPUTE_URL, WebServiceUtils.METHOD.POST, authenticationToken, null, context, contentValues);
            if(!hasError(obj)) {
                Log.d(LOG_TAG, obj.toString());
                jsonObject = obj.optJSONObject(Constants.DATA);

                if (jsonObject == null) {
                    return false;
                } else {
                    try {
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
            builder.setMessage(displayMessage)
                    .setTitle("Message")
                    .setCancelable(false)
                    .setPositiveButton("Understood", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Intent intent = new Intent(getApplicationContext(), YourCharterActivity.class);
                            intent.putExtra("intention","acceptedCharters");
                            startActivity(intent);
                            finish();
                        }
                    }).show();
        }

        @Override
        public void onFailedAttempt() {
            int statusCode = prefs.getInt(Preferences.STATUSCODE, 0);
            if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                resetApp();
            } else {
                performSuccessfulOperation();
            }
        }
    }
}
