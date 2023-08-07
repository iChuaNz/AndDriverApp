package sg.com.commute_solutions.bustracker.fragments.charters;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;

import sg.com.commute_solutions.bustracker.R;
import sg.com.commute_solutions.bustracker.common.Constants;
import sg.com.commute_solutions.bustracker.common.Preferences;
import sg.com.commute_solutions.bustracker.data.Charters;
import sg.com.commute_solutions.bustracker.fragments.LoginActivity;
import sg.com.commute_solutions.bustracker.fragments.MapsActivity;
import sg.com.commute_solutions.bustracker.fragments.RouteActivity;
import sg.com.commute_solutions.bustracker.util.StringUtil;
import sg.com.commute_solutions.bustracker.webservices.WebServiceTask;
import sg.com.commute_solutions.bustracker.webservices.WebServiceUtils;

/**
 * Created by Kyle on 2/5/17.
 */

public class YourCharterActivity extends AppCompatActivity {
    private static final String LOG_TAG = YourCharterActivity.class.getSimpleName();

    private Context context;
    private SharedPreferences prefs;
    private String authToken;
    private final ContentValues authenticationToken = new ContentValues();
    private boolean isViewAcceptedCharters = false;

    private FloatingActionMenu fabCharteringMenu;
    private FloatingActionButton fabViewCharter;
    private FloatingActionButton fabNewCharter;
    private FloatingActionButton fabSuccessfulCharter;
    private FloatingActionButton fabToTracker;
    private FloatingActionButton fabDispute;
    private FloatingActionButton fabProfile;

    private SwipeRefreshLayout swipeLayout;
    private TextView txtTitle;
    private TableLayout charterList;
    private ArrayList<Charters> charters;
    private ArrayList<String> withdrawReasons;

    private LinearLayout ll_empty_page;
    private ImageView iv_empty_page;
    private TextView txt_empty_accept_page;
    private TextView txt_empty_subout_page;

    private FetchOwnJobsTask mFetchOwnJobsTask;
    private FetchAcceptedJobsTask mFetchAcceptedJobsTask;
    private JSONObject obj, jsonObject;

    private AlertDialog loadingScreen;
    private boolean isOMO = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_charter);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        context = this;

        LayoutInflater lI = LayoutInflater.from(context);
        View promptView = lI.inflate(R.layout.loading_screen, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(promptView);
        loadingScreen = alertDialogBuilder.create();
        loadingScreen.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        loadingScreen.setCanceledOnTouchOutside(false);
        showLoadingScreen();

        prefs = this.getSharedPreferences(Constants.SHARE_PREFERENCE_PACKAGE, Context.MODE_PRIVATE);
        authToken = StringUtil.deNull(prefs.getString(Preferences.AUTH_TOKEN, ""));
        authenticationToken.put(Constants.TOKEN, authToken);

        ll_empty_page = (LinearLayout) findViewById(R.id.empty_jobs_background);
        iv_empty_page = (ImageView) findViewById(R.id.iv_empty_job_logo);
        txt_empty_accept_page = (TextView) findViewById(R.id.txt_no_accept_job_message);
        txt_empty_subout_page = (TextView) findViewById(R.id.txt_no_subout_job_message);

        String driverRole = prefs.getString(Preferences.ROLE, "");
        if (driverRole.equalsIgnoreCase("OMO")) {
            isOMO = true;
        }

        fabCharteringMenu = (FloatingActionMenu) findViewById(R.id.fab_charteringMenu3);
        fabViewCharter = (FloatingActionButton) findViewById(R.id.fab_viewCharter3);
        fabNewCharter = (FloatingActionButton) findViewById(R.id.fab_newCharter3);
        fabSuccessfulCharter = (FloatingActionButton) findViewById(R.id.fab_successfulBids3);
        fabToTracker = (FloatingActionButton) findViewById(R.id.fab_toTracking3);
        fabDispute = (FloatingActionButton) findViewById(R.id.fab_dispute3);
        fabProfile = (FloatingActionButton) findViewById(R.id.fab_profile3);

        if (isOMO) {
            fabToTracker.setVisibility(View.VISIBLE);
        }

        fabCharteringMenu.setIconAnimated(false);
        fabCharteringMenu.setMenuButtonColorNormal(Color.parseColor("#F68B1F"));
        fabViewCharter.setOnClickListener(clickListener);
        fabViewCharter.setColorNormal(Color.parseColor("#F68B1F"));
        fabNewCharter.setOnClickListener(clickListener);
        fabNewCharter.setColorNormal(Color.parseColor("#F68B1F"));
        fabSuccessfulCharter.setOnClickListener(clickListener);
        fabSuccessfulCharter.setColorNormal(Color.parseColor("#F68B1F"));
        fabToTracker.setOnClickListener(clickListener);
        fabToTracker.setColorNormal(Color.parseColor("#F68B1F"));
        fabDispute.setOnClickListener(clickListener);
        fabDispute.setColorNormal(Color.parseColor("#F68B1F"));
        fabProfile.setOnClickListener(clickListener);
        fabProfile.setColorNormal(Color.parseColor("#F68B1F"));

        Intent intent = getIntent();
        final String intention = intent.getStringExtra("intention");
        if (intention.equalsIgnoreCase("acceptedCharters")) {
            isViewAcceptedCharters = true;
        }

        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh2);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Intent intent = new Intent(getApplicationContext(), YourCharterActivity.class);
                intent.putExtra("intention", intention);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        txtTitle = (TextView) findViewById(R.id.txt_title_your_successful_charter);
        if (isViewAcceptedCharters) {
            txtTitle.setText("Scheduled Jobs");
            fabSuccessfulCharter.setLabelText("My Subout Jobs");
        }

        charterList = (TableLayout) findViewById(R.id.table_yourSuccessfulCharterList);
        initCharterList();
        hideLoadingScreen();
    }

    private void initCharterList() {
        charterList.removeAllViews();
        charters = new ArrayList<>();
        try {
            if (isViewAcceptedCharters) {
                withdrawReasons = new ArrayList<>();
                mFetchAcceptedJobsTask = new FetchAcceptedJobsTask();
                mFetchAcceptedJobsTask.execute((Void) null);
            } else {
                mFetchOwnJobsTask = new FetchOwnJobsTask();
                mFetchOwnJobsTask.execute((Void) null);
            }
        } catch (Exception e) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(Constants.CANNOT_FETCH_AVAILABLE_JOBS)
                    .setTitle("Message")
                    .setCancelable(false)
                    .setPositiveButton("Understood", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
        }

//        TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams (TableLayout.LayoutParams.FILL_PARENT,TableLayout.LayoutParams.WRAP_CONTENT);
//        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
//        String text;
//        tableRowParams.setMargins(0,25,0,33);

        int count = charters.size();
        if (count == 0) {
            if (isViewAcceptedCharters) {
                ll_empty_page.setVisibility(View.VISIBLE);
                iv_empty_page.setVisibility(View.VISIBLE);
                txt_empty_accept_page.setVisibility(View.VISIBLE);
                txt_empty_accept_page.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(), AvailableCharterActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
            } else {
                ll_empty_page.setVisibility(View.VISIBLE);
                iv_empty_page.setVisibility(View.VISIBLE);
                txt_empty_subout_page.setVisibility(View.VISIBLE);
                txt_empty_subout_page.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(), NewCharterActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        } else {
            for (int i = 0; i < count; i++) {
                final Charters charter = charters.get(i);
                charterList.addView(createRow(charter, charter.getDate(), charter.getTime()[0], charter.getCost(), charter.getBusType() + "", charter.getPickupName()[0],
                        charter.getDropOffName()[(charter.getDropOffName().length - 1)], charter.isCancelled(), charter.isCompleted(),charter.isAccepted()));
            }
        }
    }

    private View createRow(final Charters charter, String charterDate, String charterTime, String charterCost, String charterBusCapacity, String charterPickupPoint, String charterDropoffPoint,
                           boolean isCancelled, boolean isCompleted,boolean isAccepted) {
        TableRow tr = new TableRow(this);
        View v = LayoutInflater.from(context).inflate(R.layout.charter_row_template, tr, false);

        TextView txtCharterDate = (TextView)v.findViewById(R.id.charterDate);
        txtCharterDate.setText(charterDate);

        TextView txtCharterTime = (TextView)v.findViewById(R.id.charterTime);
        txtCharterTime.setText(charterTime);

        TextView txtCharterCost = (TextView)v.findViewById(R.id.charterCost);
        txtCharterCost.setText("SGD $" + charterCost);

        TextView txtBusCapacity = (TextView)v.findViewById(R.id.charterBusCapacity);
        txtBusCapacity.setText(charterBusCapacity + " - Seater");

        TextView txtPickupPoint = (TextView)v.findViewById(R.id.charterPickupDetails);
        txtPickupPoint.setText(charterPickupPoint);

        TextView txtDropOffPoint = (TextView)v.findViewById(R.id.charterDropOffDetails);
        txtDropOffPoint.setText(charterDropoffPoint);

        TextView txtStatus = (TextView)v.findViewById(R.id.charterStatus);
        Button btnViewCharter = (Button)v.findViewById(R.id.btnTapToView);
        if (isCancelled) {
            txtStatus.setText("           Cancelled!          ");
            txtStatus.setBackgroundColor(Color.RED);
            btnViewCharter.setEnabled(false);
            btnViewCharter.setPaintFlags(btnViewCharter.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            if (isCompleted) {
                txtStatus.setText("           Completed!          ");
                txtStatus.setBackgroundColor(Color.GREEN);
            } else if (isAccepted) {
                txtStatus.setText("            Accepted            ");
                txtStatus.setBackgroundColor(Color.GREEN);
            } else {
                txtStatus.setText("            Pending            ");
                txtStatus.setBackgroundColor(Color.DKGRAY);
            }
            btnViewCharter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Gson gson = new Gson();
                    String jsonString = gson.toJson(charter);
                    Intent intent = new Intent(getApplicationContext(), ViewCharterActivity.class);
                    if (isViewAcceptedCharters) {
                        intent.putExtra("intention", "acceptedCharters");
                        String jsonString2 = gson.toJson(withdrawReasons);
                        intent.putExtra("reasons", jsonString2);
                    } else {
                        intent.putExtra("intention", "ownCharter");
                    }
                    intent.putExtra("selectedCharter", jsonString);
                    startActivity(intent);
                }
            });
        }

        return v;
    }

//    private void setBlinkingView(TableLayout row) {
//        Animation blink = new AlphaAnimation(1,0);
//        blink.setDuration(1000);
//        blink.setInterpolator(new LinearInterpolator());
//        blink.setRepeatCount(Animation.INFINITE);
//        blink.setRepeatMode(Animation.REVERSE);
//        row.startAnimation(blink);
//    }

    @Override
    public void onBackPressed() {
        if (isTaskRoot()) {
            //do nothing
        } else {
            super.onBackPressed();
        }
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        Intent intent;
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.fab_newCharter3:
                    intent = new Intent(getApplicationContext(), NewCharterActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case R.id.fab_viewCharter3:
                    intent = new Intent(getApplicationContext(), AvailableCharterActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case R.id.fab_successfulBids3:
                    intent = new Intent(getApplicationContext(), YourCharterActivity.class);
                    if (isViewAcceptedCharters) {
                        intent.putExtra("intention","ownCharter");
                    } else {
                        intent.putExtra("intention","acceptedCharters");
                    }
                    startActivity(intent);
                    finish();
                    break;
                case R.id.fab_toTracking3:
                    intent = new Intent(getApplicationContext(), RouteActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case R.id.fab_dispute3:
                    intent = new Intent(getApplicationContext(), DisputedCharterActivity.class);
                    startActivity(intent);
                    break;
                case R.id.fab_profile3:
                    intent = new Intent(getApplicationContext(), CharterProfileActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    };

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
                        YourCharterActivity.this.finishAffinity();
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

    private class FetchOwnJobsTask extends WebServiceTask {
        FetchOwnJobsTask(){
            super(YourCharterActivity.this);
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
            String URL = Constants.VIEWOWNCHARTER_URL;
            obj = WebServiceUtils.requestJSONObject(URL, WebServiceUtils.METHOD.GET, authenticationToken, context);
            if(!hasError(obj)) {
                Log.d(LOG_TAG, obj.toString());
                JSONObject objPx;
                jsonObject = obj.optJSONObject(Constants.DATA);

                if (jsonObject == null) {
                    return false;
                } else {
                    JSONArray charterList = jsonObject.optJSONArray(Constants.VIEW_SUB_OUT_LIST);
                    try {
                        for (int i = 0; i < charterList.length(); i++) {
                            objPx = charterList.getJSONObject(i);
                            String id = objPx.getString(Constants.CHARTER_ID);
                            String date = objPx.getString(Constants.CHARTER_DATE);
                            String expiryDate = objPx.getString(Constants.CHARTER_EXPIRY_DATE);
                            String cost = objPx.getString(Constants.CHARTER_COST);
                            int busType = objPx.getInt(Constants.CHARTER_BUSTYPE);
                            int disposalDuration = objPx.getInt(Constants.CHARTER_DISPOSAL_DURATION);
                            JSONArray tempArray1 = objPx.getJSONArray(Constants.CHARTER_PICKUP_NAME);
                            String[] pickUpName = new String[tempArray1.length()];
                            for (int j = 0; j < tempArray1.length(); j++) {
                                pickUpName[j] = tempArray1.getString(j);
                            }
                            JSONArray tempArray2 = objPx.getJSONArray(Constants.CHARTER_DROPOFF_NAME);
                            String[] dropOffName = new String[tempArray2.length()];
                            for (int j = 0; j < tempArray2.length(); j++) {
                                dropOffName[j] = tempArray2.getString(j);
                            }
                            JSONArray tempArray3 = objPx.getJSONArray(Constants.CHARTER_TIME);
                            String[] time = new String[tempArray3.length()];
                            for (int j = 0; j < tempArray3.length(); j++) {
                                time[j] = tempArray3.getString(j);
                            }
                            JSONArray tempArray4 = objPx.getJSONArray(Constants.CHARTER_PICKUP_LATITUDE);
                            String[] pickUpLatitude = new String[tempArray4.length()];
                            for (int j = 0; j < tempArray4.length(); j++) {
                                pickUpLatitude[j] = tempArray4.getString(j);
                            }
                            JSONArray tempArray5 = objPx.getJSONArray(Constants.CHARTER_PICKUP_LONGITUDE);
                            String[] pickupLongitude = new String[tempArray5.length()];
                            for (int j = 0; j < tempArray5.length(); j++) {
                                pickupLongitude[j] = tempArray5.getString(j);
                            }
                            JSONArray tempArray6 = objPx.getJSONArray(Constants.CHARTER_DROPOFF_LATITUDE);
                            String[] dropOffLatitude = new String[tempArray6.length()];
                            for (int j = 0; j < tempArray6.length(); j++) {
                                dropOffLatitude[j] = tempArray6.getString(j);
                            }
                            JSONArray tempArray7 = objPx.getJSONArray(Constants.CHARTER_DROPOFF_LONGITUDE);
                            String[] dropOffLongitude = new String[tempArray7.length()];
                            for (int j = 0; j < tempArray7.length(); j++) {
                                dropOffLongitude[j] = tempArray7.getString(j);
                            }
                            String pocName = objPx.getString(Constants.CHARTER_POC_NAME);
//                            String pocEmail = objPx.getString(Constants.CHARTER_POC_EMAIL);
                            String pocContactNo = objPx.getString(Constants.CHARTER_POC_CONTACT_NO);
//                            String pocCompany = objPx.getString(Constants.CHARTER_POC_COMPANY);
                            String remarks = objPx.getString(Constants.CHARTER_REMARKS);
                            String accessCode = objPx.getString(Constants.CHARTER_ACCESS_CODE);

                            String driverName = objPx.getString(Constants.BUS_CHARTER_DRIVER_NAME);
                            String driverContactNo = objPx.getString(Constants.BUS_CHARTER_DRIVER_CONTACT_NO);
                            String vehicleNo = objPx.getString(Constants.BUS_CHARTER_VEHICLE_NUMBER);

                            String url = objPx.getString(Constants.TRACKING_URL);

                            boolean isAccepted = objPx.getBoolean(Constants.BUS_CHARTER_ACCEPTED);
                            boolean isCancelled = objPx.getBoolean(Constants.BUS_CHARTER_CANCELLED);
                            boolean isDisputable = objPx.getBoolean(Constants.BUS_CHARTER_DISPUTABLE);
                            boolean isCompleted = objPx.getBoolean(Constants.BUS_CHARTER_COMPLETED);
                            boolean canResubmit = objPx.getBoolean(Constants.CHARTER_CAN_RESUBMIT);

                            Charters charter = new Charters(id, date, pickUpName, dropOffName, time, cost, disposalDuration, expiryDate,
                                    pickUpLatitude, pickupLongitude, dropOffLatitude, dropOffLongitude, busType, remarks,
                                    pocName, "", pocContactNo, "", accessCode, driverName, driverContactNo, vehicleNo, isAccepted,
                                    isCancelled, isDisputable, isCompleted, url, canResubmit);

                            charters.add(charter);
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
            Log.d(LOG_TAG, "Successful API Call!");
        }

        @Override
        public void onFailedAttempt() {
            int statusCode = prefs.getInt(Preferences.STATUSCODE, 0);
            if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                resetApp();
            }
        }
    }

    private class FetchAcceptedJobsTask extends WebServiceTask {
        FetchAcceptedJobsTask(){
            super(YourCharterActivity.this);
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
            String URL = Constants.VIEWACCEPTEDCHARTER_URL;
            obj = WebServiceUtils.requestJSONObject(URL, WebServiceUtils.METHOD.GET, authenticationToken, context);
            if(!hasError(obj)) {
                Log.d(LOG_TAG, obj.toString());
                JSONObject objPx;
                jsonObject = obj.optJSONObject(Constants.DATA);

                if (jsonObject == null) {
                    return false;
                } else {
                    try {
                        JSONArray charterList = jsonObject.optJSONArray(Constants.VIEW_ACCEPTED_LIST);
                        //charter
                        for (int i = 0; i < charterList.length(); i++) {
                            objPx = charterList.getJSONObject(i);
                            String id = objPx.getString(Constants.CHARTER_ID);
                            String date = objPx.getString(Constants.CHARTER_DATE);
                            String expiryDate = objPx.getString(Constants.CHARTER_EXPIRY_DATE);
                            String cost = objPx.getString(Constants.CHARTER_COST);
                            int busType = objPx.getInt(Constants.CHARTER_BUSTYPE);
                            int disposalDuration = objPx.getInt(Constants.CHARTER_DISPOSAL_DURATION);
                            JSONArray tempArray1 = objPx.getJSONArray(Constants.CHARTER_PICKUP_NAME);
                            String[] pickUpName = new String[tempArray1.length()];
                            for (int j = 0; j < tempArray1.length(); j++) {
                                pickUpName[j] = tempArray1.getString(j);
                            }
                            JSONArray tempArray2 = objPx.getJSONArray(Constants.CHARTER_DROPOFF_NAME);
                            String[] dropOffName = new String[tempArray2.length()];
                            for (int j = 0; j < tempArray2.length(); j++) {
                                dropOffName[j] = tempArray2.getString(j);
                            }
                            JSONArray tempArray3 = objPx.getJSONArray(Constants.CHARTER_TIME);
                            String[] time = new String[tempArray3.length()];
                            for (int j = 0; j < tempArray3.length(); j++) {
                                time[j] = tempArray3.getString(j);
                            }
                            JSONArray tempArray4 = objPx.getJSONArray(Constants.CHARTER_PICKUP_LATITUDE);
                            String[] pickUpLatitude = new String[tempArray4.length()];
                            for (int j = 0; j < tempArray4.length(); j++) {
                                pickUpLatitude[j] = tempArray4.getString(j);
                            }
                            JSONArray tempArray5 = objPx.getJSONArray(Constants.CHARTER_PICKUP_LONGITUDE);
                            String[] pickupLongitude = new String[tempArray5.length()];
                            for (int j = 0; j < tempArray5.length(); j++) {
                                pickupLongitude[j] = tempArray5.getString(j);
                            }
                            JSONArray tempArray6 = objPx.getJSONArray(Constants.CHARTER_DROPOFF_LATITUDE);
                            String[] dropOffLatitude = new String[tempArray6.length()];
                            for (int j = 0; j < tempArray6.length(); j++) {
                                dropOffLatitude[j] = tempArray6.getString(j);
                            }
                            JSONArray tempArray7 = objPx.getJSONArray(Constants.CHARTER_DROPOFF_LONGITUDE);
                            String[] dropOffLongitude = new String[tempArray7.length()];
                            for (int j = 0; j < tempArray7.length(); j++) {
                                dropOffLongitude[j] = tempArray7.getString(j);
                            }
                            String pocName = objPx.getString(Constants.CHARTER_POC_NAME);
//                            String pocEmail = objPx.getString(Constants.CHARTER_POC_EMAIL);
                            String pocContactNo = objPx.getString(Constants.CHARTER_POC_CONTACT_NO);
//                            String pocCompany = objPx.getString(Constants.CHARTER_POC_COMPANY);
                            String remarks = objPx.getString(Constants.CHARTER_REMARKS);
                            String accessCode = objPx.getString(Constants.CHARTER_ACCESS_CODE);

                            String driverName = objPx.getString(Constants.BUS_CHARTER_DRIVER_NAME);
                            String driverContactNo = objPx.getString(Constants.BUS_CHARTER_DRIVER_CONTACT_NO);
                            String vehicleNo = objPx.getString(Constants.BUS_CHARTER_VEHICLE_NUMBER);

                            String url = objPx.getString(Constants.TRACKING_URL);

                            boolean isCancelled = objPx.getBoolean(Constants.BUS_CHARTER_CANCELLED);
                            boolean isDisputable = objPx.getBoolean(Constants.BUS_CHARTER_DISPUTABLE);
                            boolean isCompleted = objPx.getBoolean(Constants.BUS_CHARTER_COMPLETED);
                            boolean canResubmit = objPx.getBoolean(Constants.CHARTER_CAN_RESUBMIT);

                            Charters charter = new Charters(id, date, pickUpName, dropOffName, time, cost, disposalDuration, expiryDate,
                                    pickUpLatitude, pickupLongitude, dropOffLatitude, dropOffLongitude, busType, remarks,
                                    pocName, "", pocContactNo, "", accessCode, driverName, driverContactNo, vehicleNo, isCancelled,
                                    isDisputable, isCompleted, url, canResubmit);

                            charters.add(charter);
                        }
                        //withdraw reason
                        JSONArray reasons = jsonObject.optJSONArray(Constants.REASON);
                        for (int i = 0; i < reasons.length(); i++) {
                            String reasonString = reasons.getString(i);
                            withdrawReasons.add(reasonString);
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
            Log.d(LOG_TAG, "Successful API Call!");
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
