package sg.com.commute_solutions.bustracker.fragments.charters;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
 * Created by Kyle on 4/4/17.
 */

public class AvailableCharterActivity extends AppCompatActivity {
    private static final String LOG_TAG = AvailableCharterActivity.class.getSimpleName();

    private Context context;
    private SharedPreferences prefs;
    private String authToken;
    private final ContentValues authenticationToken = new ContentValues();

    private SwipeRefreshLayout swipeLayout;
    private ImageView btnNext, btnPrev;
    private TableLayout charterList;
    private ArrayList<Charters> charters;
    private AlertDialog loadingScreen;

    private FloatingActionMenu fabCharteringMenu;
    private FloatingActionButton fabNewCharter;
    private FloatingActionButton fabYourCharter;
    private FloatingActionButton fabSuccessfulCharter;
    private FloatingActionButton fabToTracker;
    private FloatingActionButton fabDispute;
    private FloatingActionButton fabProfile;

    private JSONObject obj, jsonObject;
    private ContentValues contentValues;
    private AvailableJobsTask mAvailableJobsTask = null;
    private FetchMoreAvailableJobsTask mFetchMoreAvailableJobsTask = null;

    private LinearLayout ll_empty_page;
    private ImageView iv_empty_page;
    private TextView txt_empty_page;

    private boolean hasMoreCharters;
    private boolean isOMO = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chartering);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        context = this;

        Intent intent = getIntent();
        if(intent!=null && intent.getData() != null){
            String a = intent.getData().toString();
        }

        LayoutInflater lI = LayoutInflater.from(context);
        View promptView = lI.inflate(R.layout.loading_screen, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(promptView);
        loadingScreen = alertDialogBuilder.create();
        loadingScreen.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        loadingScreen.setCanceledOnTouchOutside(false);
        showLoadingScreen();

        prefs = this.getSharedPreferences(Constants.SHARE_PREFERENCE_PACKAGE, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Preferences.CURRENTACTIVITY, "createcharter");
        editor.apply();

        authToken = StringUtil.deNull(prefs.getString(Preferences.AUTH_TOKEN, ""));
        authenticationToken.put(Constants.TOKEN, authToken);

        ll_empty_page = (LinearLayout) findViewById(R.id.empty_charter_background);
        iv_empty_page = (ImageView) findViewById(R.id.iv_empty_charter_logo);
        txt_empty_page = (TextView) findViewById(R.id.txt_empty_charter_message);

        fabCharteringMenu = (FloatingActionMenu) findViewById(R.id.fab_charteringMenu1);
        fabNewCharter = (FloatingActionButton) findViewById(R.id.fab_newCharter1);
        fabNewCharter.setColorNormal(Color.parseColor("#F68B1F"));
        fabSuccessfulCharter = (FloatingActionButton) findViewById(R.id.fab_successfulBids1);
        fabSuccessfulCharter.setColorNormal(Color.parseColor("#F68B1F"));
        fabYourCharter = (FloatingActionButton) findViewById(R.id.fab_ownCharter1);
        fabYourCharter.setColorNormal(Color.parseColor("#F68B1F"));
        fabToTracker = (FloatingActionButton) findViewById(R.id.fab_toTracking1);
        fabToTracker.setColorNormal(Color.parseColor("#F68B1F"));
        fabDispute = (FloatingActionButton) findViewById(R.id.fab_dispute1);
        fabDispute.setColorNormal(Color.parseColor("#F68B1F"));
        fabProfile = (FloatingActionButton) findViewById(R.id.fab_profile1);
        fabProfile.setColorNormal(Color.parseColor("#F68B1F"));

        String driverRole = prefs.getString(Preferences.ROLE, "");
        if (driverRole.equalsIgnoreCase("OMO")) {
            isOMO = true;
        }

        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Intent intent = new Intent(getApplicationContext(), AvailableCharterActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        btnNext = (ImageView) findViewById(R.id.btn_nextSet);
        btnPrev = (ImageView) findViewById(R.id.btn_previousSet);
        charterList = (TableLayout) findViewById(R.id.table_charterList);
        charterList.removeAllViews();

        boolean isNextPage = getIntent().getBooleanExtra(Constants.HASMORECHARTERS, false);

        if (isNextPage) {
            String lastCharterId = getIntent().getStringExtra(Constants.LASTCHARTERID);
            try {
                mFetchMoreAvailableJobsTask = new FetchMoreAvailableJobsTask(lastCharterId);
                mFetchMoreAvailableJobsTask.execute((Void) null);
                btnPrev.setVisibility(View.VISIBLE);

                btnPrev.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });
            } catch (Exception e) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(Constants.CANNOT_FETCH_AVAILABLE_JOBS)
                        .setCancelable(false)
                        .setPositiveButton("Understood", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        } else {
            try {
                mAvailableJobsTask = new AvailableJobsTask();
                mAvailableJobsTask.execute((Void) null);
            } catch (Exception e) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(Constants.CANNOT_FETCH_AVAILABLE_JOBS)
                        .setCancelable(false)
                        .setPositiveButton("Understood", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        }

        if (hasMoreCharters) {
            btnNext.setVisibility(View.VISIBLE);
            btnNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String latestCharterId = prefs.getString(Preferences.LAST_CHARTER_ID, "");
                    Intent intent = new Intent(AvailableCharterActivity.this, AvailableCharterActivity.class);
                    intent.putExtra(Constants.LASTCHARTERID, latestCharterId);
                    intent.putExtra(Constants.HASMORECHARTERS, true);
                    startActivity(intent);
                }
            });
        }
        initCharterList();
        hideLoadingScreen();

        if (isOMO) {
            fabToTracker.setVisibility(View.VISIBLE);
        }

        fabCharteringMenu.setIconAnimated(false);
        fabCharteringMenu.setMenuButtonColorNormal(Color.parseColor("#F68B1F"));
        fabNewCharter.setOnClickListener(clickListener);
        fabSuccessfulCharter.setOnClickListener(clickListener);
        fabYourCharter.setOnClickListener(clickListener);
        fabToTracker.setOnClickListener(clickListener);
        fabDispute.setOnClickListener(clickListener);
        fabProfile.setOnClickListener(clickListener);
    }

    private void initCharterList() {
        int count;
        try {
            count = charters.size();
        } catch (Exception e) {
            count = 0;
        }

        if (count == 0) {
            ll_empty_page.setVisibility(View.VISIBLE);
            iv_empty_page.setVisibility(View.VISIBLE);
            txt_empty_page.setVisibility(View.VISIBLE);
            charterList.setVisibility(View.GONE);
        } else {
            for (int i = 0; i < charters.size(); i++) {
                final Charters charter = charters.get(i);
                charterList.addView(createRow(charter, charter.getDate(), charter.getTime()[0], charter.getCost(), charter.getBusType() + "", charter.getPickupName()[0],
                        charter.getDropOffName()[(charter.getDropOffName().length - 1)], charter.isMyCharter(), charter.getExpiryDate()));
            }
        }
    }

    private View createRow(final Charters charter, String charterDate, String charterTime, String charterCost, String charterBusCapacity, String charterPickupPoint, String charterDropoffPoint,
                           boolean isMyCharter, String expiryDate) {
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
        if (isMyCharter) {
            txtStatus.setText("         Your Charter!         ");
            txtStatus.setBackgroundColor(Color.MAGENTA);
        } else {
            txtStatus.setText("           Available           ");
            txtStatus.setBackgroundColor(Color.BLUE);
        }

        LinearLayout llExpiryDate = (LinearLayout)v.findViewById(R.id.layoutExpiry);
        llExpiryDate.setVisibility(View.VISIBLE);

        TextView txtExpiry = (TextView)v.findViewById(R.id.charterExpiry);
        txtExpiry.setText("Expires " + expiryDate);

        Button btnViewCharter = (Button)v.findViewById(R.id.btnTapToView);
        btnViewCharter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                        Gson gson = new Gson();
                        String jsonString = gson.toJson(charter);
                        Intent intent = new Intent(getApplicationContext(), ViewCharterActivity.class);
                        intent.putExtra("selectedCharter", jsonString);
                        startActivity(intent);
            }
        });

        return v;
    }

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
                case R.id.fab_newCharter1:
                    intent = new Intent(getApplicationContext(), NewCharterActivity.class);
                    startActivity(intent);
//                    stopTimerRefresh();
                    finish();
                    break;
                case R.id.fab_successfulBids1:
                    intent = new Intent(getApplicationContext(), YourCharterActivity.class);
                    intent.putExtra("intention","acceptedCharters");
                    startActivity(intent);
                    finish();
                    break;
                case R.id.fab_ownCharter1:
                    intent = new Intent(getApplicationContext(), YourCharterActivity.class);
                    intent.putExtra("intention","ownCharter");
                    startActivity(intent);
                    finish();
                    break;
                case R.id.fab_toTracking1:
                    intent = new Intent(getApplicationContext(), RouteActivity.class);
                    startActivity(intent);
//                    stopTimerRefresh();
                    finish();
                    break;
                case R.id.fab_dispute1:
                    intent = new Intent(getApplicationContext(), DisputedCharterActivity.class);
                    startActivity(intent);
                    break;
                case R.id.fab_profile1:
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
                        AvailableCharterActivity.this.finishAffinity();
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
    private class AvailableJobsTask extends WebServiceTask {
        AvailableJobsTask(){
            super(AvailableCharterActivity.this);
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
            String URL = Constants.VIEWCHARTER_URL;
            obj = WebServiceUtils.requestJSONObject(URL, WebServiceUtils.METHOD.GET, authenticationToken, context);
            if(!hasError(obj)) {
                Log.d(LOG_TAG, obj.toString());
                JSONObject objPx;
                jsonObject = obj.optJSONObject(Constants.DATA);

                if (jsonObject == null) {
                    return false;
                } else {
                    hasMoreCharters = jsonObject.optBoolean(Constants.HASMORECHARTERS);
                    JSONArray charterList = jsonObject.optJSONArray(Constants.VIEW_CHARTER_LIST);
                    try {
                        charters = new ArrayList<>();
                        for (int i = 0; i < charterList.length(); i++) {
                            objPx = charterList.getJSONObject(i);
                            String id = objPx.getString(Constants.CHARTER_ID);
                            String serviceType = objPx.getString(Constants.CHARTER_SERVICE_TYPE);
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

                            String remarks = objPx.getString(Constants.CHARTER_REMARKS);
                            String accessCode = objPx.getString(Constants.CHARTER_ACCESS_CODE);

                            boolean isMyCharter = objPx.getBoolean(Constants.CHARTER_OWN_CHARTER);
                            boolean canResubmit = objPx.getBoolean(Constants.CHARTER_CAN_RESUBMIT);

                            Charters charter = new Charters(id, serviceType, date, pickUpName, dropOffName, time, cost, disposalDuration, expiryDate,
                                    pickUpLatitude, pickupLongitude, dropOffLatitude, dropOffLongitude, busType, remarks,
                                    accessCode, isMyCharter, canResubmit);

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
            if (hasMoreCharters) {
                int count = charters.size();
                String latestCharterId = charters.get(count - 1).getId();
                final SharedPreferences.Editor editor = prefs.edit();
                editor.putString(Preferences.LAST_CHARTER_ID, latestCharterId);
                editor.apply();
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

    private class FetchMoreAvailableJobsTask extends WebServiceTask {
        FetchMoreAvailableJobsTask(String charterId){
            super(AvailableCharterActivity.this);
            contentValues = new ContentValues();
            contentValues.put(Constants.CHARTER_ID, charterId);
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
            obj = WebServiceUtils.requestJSONObject(Constants.VIEWMORECHARTER_URL, WebServiceUtils.METHOD.POST, authenticationToken, null, context, contentValues);
            if(!hasError(obj)) {
                Log.d(LOG_TAG, obj.toString());
                JSONObject objPx;
                jsonObject = obj.optJSONObject(Constants.DATA);

                if (jsonObject == null) {
                    return false;
                } else {
                    hasMoreCharters = jsonObject.optBoolean(Constants.HASMORECHARTERS);
                    JSONArray charterList = jsonObject.optJSONArray(Constants.VIEW_CHARTER_LIST);
                    try {
                        charters = new ArrayList<>();
                        for (int i = 0; i < charterList.length(); i++) {
                            objPx = charterList.getJSONObject(i);
                            String id = objPx.getString(Constants.CHARTER_ID);
                            String serviceType = objPx.getString(Constants.CHARTER_SERVICE_TYPE);
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

                            String remarks = objPx.getString(Constants.CHARTER_REMARKS);
                            String accessCode = objPx.getString(Constants.CHARTER_ACCESS_CODE);

                            boolean isMyCharter = objPx.getBoolean(Constants.CHARTER_OWN_CHARTER);
                            boolean canResubmit = objPx.getBoolean(Constants.CHARTER_CAN_RESUBMIT);

                            Charters charter = new Charters(id, serviceType, date, pickUpName, dropOffName, time, cost, disposalDuration, expiryDate,
                                    pickUpLatitude, pickupLongitude, dropOffLatitude, dropOffLongitude, busType, remarks,
                                    accessCode, isMyCharter, canResubmit);

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
            if (hasMoreCharters) {
                int count = charters.size();
                String latestCharterId = charters.get(count - 1).getId();
                final SharedPreferences.Editor editor = prefs.edit();
                editor.putString(Preferences.LAST_CHARTER_ID, latestCharterId);
                editor.apply();
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
