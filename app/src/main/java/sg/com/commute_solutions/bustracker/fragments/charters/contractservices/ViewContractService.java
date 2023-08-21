package sg.com.commute_solutions.bustracker.fragments.charters.contractservices;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.telecom.Call;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import sg.com.commute_solutions.bustracker.R;
import sg.com.commute_solutions.bustracker.common.Constants;
import sg.com.commute_solutions.bustracker.common.Preferences;
import sg.com.commute_solutions.bustracker.data.Contract;
import sg.com.commute_solutions.bustracker.fragments.LoginActivity;
import sg.com.commute_solutions.bustracker.util.StringUtil;
import sg.com.commute_solutions.bustracker.webservices.WebServiceTask;
import sg.com.commute_solutions.bustracker.webservices.WebServiceUtils;

/**
 * Created by Kyle on 10/1/18.
 */

public class ViewContractService extends AppCompatActivity {
    private static final String LOG_TAG = ViewContractService.class.getSimpleName();

    private Context context;
    private SharedPreferences prefs;
    private String authToken;
    private final ContentValues authenticationToken = new ContentValues();
    private AlertDialog loadingScreen;

    private CallCounterTask mCallCounterTask = null;
    private JSONObject obj, jsonObject;
    private ContentValues contentValues;
    boolean isSuccessful = false;
    private String displayMessage;
    private String contactNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contract_singleview);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        context = this;

        LayoutInflater lI = LayoutInflater.from(context);
        View promptView = lI.inflate(R.layout.loading_screen, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(promptView);
        loadingScreen = alertDialogBuilder.create();
        loadingScreen.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        loadingScreen.setCanceledOnTouchOutside(false);

        prefs = this.getSharedPreferences(Constants.SHARE_PREFERENCE_PACKAGE, Context.MODE_PRIVATE);
        authToken = StringUtil.deNull(prefs.getString(Preferences.AUTH_TOKEN, ""));
        authenticationToken.put(Constants.TOKEN, authToken);

        Intent intent = getIntent();
        Gson gson = new Gson();
        String jsonString = intent.getStringExtra("selectedContract");
        final Contract contract = gson.fromJson(jsonString, Contract.class);

        //Contract Period
        TextView txtContractPeriod = (TextView) findViewById(R.id.txt_contract_period);
        String contractPeriod = new StringBuilder(contract.getStartDate())
                .append("  →  ")
                .append(contract.getEndDate())
                .toString();
        txtContractPeriod.setText(contractPeriod);

        //First Pickup Time
        TextView txtFirstPickupTime = (TextView) findViewById(R.id.txt_first_pickup_time);
        String pickupTime = contract.getPickupTime();
        txtFirstPickupTime.setText(pickupTime);

        //Bus Size
        TextView txtBusSize = (TextView) findViewById(R.id.txt_bus_size);
        String busSize = contract.getBusSize();
        txtBusSize.setText(busSize);

        //Cost Per Month
        TextView txtContractCostPerMonth = (TextView) findViewById(R.id.txt_contract_cost_per_month);
        String cost = contract.getCostPerMonthInString();
        boolean hasERP = contract.isHasERP();
        boolean includesERP = contract.getIncludesERP();
        String costInString = "$";

        if (hasERP) {
            if (includesERP) {
                costInString = new StringBuilder(costInString)
                        .append(cost)
                        .append(" [Includes ERP]")
                        .toString();
            } else {
                costInString = new StringBuilder(costInString)
                        .append(cost)
                        .append(" [Does not include ERP]")
                        .toString();
            }
        } else {
            costInString = new StringBuilder(costInString)
                    .append(cost)
                    .append(" [No ERP]")
                    .toString();
        }
        txtContractCostPerMonth.setText(costInString);

        //Pick up 1
        TextView txtPickupPoint1 =  (TextView) findViewById(R.id.view_contract_pickup1);
        final String pickupPoint1 = contract.getPickup1Name();
        final LatLng pickup1LatLng = contract.getPickup1LatLng();
        txtPickupPoint1.setText(pickupPoint1);
        txtPickupPoint1.setTextColor(Color.BLUE);
        txtPickupPoint1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String geoUriString = "geo:0,0?q=" + pickup1LatLng.latitude + "," + pickup1LatLng.longitude + "(" + pickupPoint1 + ")";
                Uri gmmIntentUri = Uri.parse(geoUriString);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });

        //Pick up 2
        TextView txtPickupPoint2 =  (TextView) findViewById(R.id.view_contract_pickup2);
        final String pickupPoint2 = contract.getPickup2Name();
        if (pickupPoint2.isEmpty()) {
            txtPickupPoint2.setVisibility(View.GONE);
        } else {
            final LatLng pickup2LatLng = contract.getPickup2LatLng();
            txtPickupPoint2.setText(pickupPoint2);
            txtPickupPoint2.setTextColor(Color.BLUE);
            txtPickupPoint2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String geoUriString = "geo:0,0?q=" + pickup2LatLng.latitude + "," + pickup2LatLng.longitude + "(" + pickupPoint2 + ")";
                    Uri gmmIntentUri = Uri.parse(geoUriString);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                }
            });
        }

        //Pick up 3
        TextView txtPickupPoint3 =  (TextView) findViewById(R.id.view_contract_pickup3);
        final String pickupPoint3 = contract.getPickup3Name();
        if (pickupPoint3.isEmpty()) {
            txtPickupPoint3.setVisibility(View.GONE);
        } else {
            final LatLng pickup3LatLng = contract.getPickup3LatLng();
            txtPickupPoint3.setText(pickupPoint3);
            txtPickupPoint3.setTextColor(Color.BLUE);
            txtPickupPoint3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String geoUriString = "geo:0,0?q=" + pickup3LatLng.latitude + "," + pickup3LatLng.longitude + "(" + pickupPoint3 + ")";
                    Uri gmmIntentUri = Uri.parse(geoUriString);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                }
            });
        }

        //Drop off 1
        TextView txtDropOffPoint1 =  (TextView) findViewById(R.id.view_contract_dropoff1);
        final String dropOffPoint1 = contract.getDropoff1Name();
        final LatLng dropOff1LatLng = contract.getDropoff1LatLng();
        txtDropOffPoint1.setText(dropOffPoint1);
        txtDropOffPoint1.setTextColor(Color.BLUE);
        txtDropOffPoint1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String geoUriString = "geo:0,0?q=" + dropOff1LatLng.latitude + "," + dropOff1LatLng.longitude + "(" + dropOffPoint1 + ")";
                Uri gmmIntentUri = Uri.parse(geoUriString);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });

        //Drop off 2
        TextView txtDropOffPoint2 =  (TextView) findViewById(R.id.view_contract_dropoff2);
        final String dropOffPoint2 = contract.getDropoff2Name();
        if (dropOffPoint2.isEmpty()) {
            txtDropOffPoint2.setVisibility(View.GONE);
        } else {
            final LatLng dropOff2LatLng = contract.getDropoff2LatLng();
            txtDropOffPoint2.setText(dropOffPoint2);
            txtDropOffPoint2.setTextColor(Color.BLUE);
            txtDropOffPoint2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String geoUriString = "geo:0,0?q=" + dropOff2LatLng.latitude + "," + dropOff2LatLng.longitude + "(" + dropOffPoint2 + ")";
                    Uri gmmIntentUri = Uri.parse(geoUriString);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                }
            });
        }

        //Drop off 3
        TextView txtDropOffPoint3 =  (TextView) findViewById(R.id.view_contract_dropoff3);
        final String dropOffPoint3 = contract.getDropoff3Name();
        if (dropOffPoint3.isEmpty()) {
            txtDropOffPoint3.setVisibility(View.GONE);
        } else {
            final LatLng dropOff3LatLng = contract.getDropoff3LatLng();
            txtDropOffPoint3.setText(dropOffPoint3);
            txtDropOffPoint3.setTextColor(Color.BLUE);
            txtDropOffPoint3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String geoUriString = "geo:0,0?q=" + dropOff3LatLng.latitude + "," + dropOff3LatLng.longitude + "(" + dropOffPoint3 + ")";
                    Uri gmmIntentUri = Uri.parse(geoUriString);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                }
            });
        }

        //Additional Info
        TextView txtAdditionalInfo = (TextView) findViewById(R.id.view_contract_remarks);
        String additionalInfo = contract.getAdditionalInfo();
        if (additionalInfo.isEmpty()) {
            additionalInfo = "No additional information.";
        }
        txtAdditionalInfo.setText(additionalInfo);

        Button btnCallJobPoster = (Button) findViewById(R.id.btn_contact_job_poster);
        boolean isOwnContract = contract.isOwnCharter();
        if (isOwnContract) {
            btnCallJobPoster.setVisibility(View.GONE);
        } else {
            contactNo = contract.getContactNo();
            final String id = contract.getId();
            btnCallJobPoster.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCallCounterTask = new CallCounterTask(id);
                    mCallCounterTask.execute((Void) null);
                }
            });
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
                        ViewContractService.this.finishAffinity();
                        Intent i = new Intent(getApplicationContext(),LoginActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                    }
                }).show();
    }

    private class CallCounterTask extends WebServiceTask {
        CallCounterTask(String id) {
            super(ViewContractService.this);
            contentValues = new ContentValues();
            contentValues.put(Constants.CHARTER_ID, id);
            performRequest();
        }

        @Override
        public void showProgress() {
            showLoadingScreen();
        }

        @Override
        public void hideProgress() {
            hideLoadingScreen();
        }

        @Override
        public boolean performRequest() {
            obj = WebServiceUtils.requestJSONObject(Constants.CALL_COUNTER_URL, WebServiceUtils.METHOD.POST, authenticationToken, null, context, contentValues);
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
            if (isSuccessful) {
                Intent call = new Intent(Intent.ACTION_DIAL);
                call.setData(Uri.parse("tel:" + contactNo));
                startActivity(call);
            } else {
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(displayMessage)
                        .setTitle("Message")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Intent intent = new Intent(getApplicationContext(), ViewAllContracts.class);
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
            }
        }
    }
}
