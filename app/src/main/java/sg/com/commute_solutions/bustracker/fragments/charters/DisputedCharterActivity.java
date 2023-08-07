package sg.com.commute_solutions.bustracker.fragments.charters;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;

import sg.com.commute_solutions.bustracker.R;
import sg.com.commute_solutions.bustracker.common.Constants;
import sg.com.commute_solutions.bustracker.common.Preferences;
import sg.com.commute_solutions.bustracker.data.Disputes;
import sg.com.commute_solutions.bustracker.fragments.LoginActivity;
import sg.com.commute_solutions.bustracker.fragments.MapsActivity;
import sg.com.commute_solutions.bustracker.fragments.RouteActivity;
import sg.com.commute_solutions.bustracker.util.StringUtil;
import sg.com.commute_solutions.bustracker.webservices.WebServiceTask;
import sg.com.commute_solutions.bustracker.webservices.WebServiceUtils;

/**
 * Created by Kyle on 21/6/17.
 */

public class DisputedCharterActivity extends AppCompatActivity {
    private static final String LOG_TAG = DisputedCharterActivity.class.getSimpleName();

    private Context context;
    private SharedPreferences prefs;
    private String authToken;
    private final ContentValues authenticationToken = new ContentValues();

    private FloatingActionMenu fabCharteringMenu;
    private FloatingActionButton fabViewCharter;
    private FloatingActionButton fabNewCharter;
    private FloatingActionButton fabYourCharter;
    private FloatingActionButton fabSuccessfulCharter;
    private FloatingActionButton fabToTracker;
    private FloatingActionButton fabProfile;

    private AlertDialog loadingScreen;
    private LinearLayout ll_empty_page;
    private ImageView iv_empty_page;
    private TextView txt_empty_page;
    private GetDisputedCharterList mGetDisputedCharterList;
    private SendDisputeResponseTask mSendDisputeResponseTask;

    private TableLayout disputeListing;
    private ArrayList<Disputes> disputeList;

    private JSONObject obj, jsonObject;
    private ContentValues contentValues;

    private String displayMessage;
    private boolean isOMO = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispute_charter);
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

        String driverRole = prefs.getString(Preferences.ROLE, "");
        if (driverRole.equalsIgnoreCase("OMO")) {
            isOMO = true;
        }

        fabCharteringMenu = (FloatingActionMenu) findViewById(R.id.fab_charteringMenu5);
        fabViewCharter = (FloatingActionButton) findViewById(R.id.fab_viewCharter5);
        fabViewCharter.setColorNormal(Color.parseColor("#F68B1F"));
        fabNewCharter = (FloatingActionButton) findViewById(R.id.fab_newCharter5);
        fabNewCharter.setColorNormal(Color.parseColor("#F68B1F"));
        fabSuccessfulCharter = (FloatingActionButton) findViewById(R.id.fab_successfulBids5);
        fabSuccessfulCharter.setColorNormal(Color.parseColor("#F68B1F"));
        fabYourCharter = (FloatingActionButton) findViewById(R.id.fab_ownCharter5);
        fabYourCharter.setColorNormal(Color.parseColor("#F68B1F"));
        fabToTracker = (FloatingActionButton) findViewById(R.id.fab_toTracking5);
        fabToTracker.setColorNormal(Color.parseColor("#F68B1F"));
        fabProfile = (FloatingActionButton) findViewById(R.id.fab_profile5);
        fabProfile.setColorNormal(Color.parseColor("#F68B1F"));

        if (isOMO) {
            fabToTracker.setVisibility(View.VISIBLE);
        }

        ll_empty_page = (LinearLayout) findViewById(R.id.empty_dispute_background);
        iv_empty_page = (ImageView) findViewById(R.id.iv_empty_dispute_logo);
        txt_empty_page = (TextView) findViewById(R.id.txt_empty_dispute_message);

        disputeList = new ArrayList<>();
        try {
            mGetDisputedCharterList = new GetDisputedCharterList();
            mGetDisputedCharterList.execute((Void) null);
        } catch (Exception e) {
            //do nothing
        }

        disputeListing = (TableLayout) findViewById(R.id.table_dispute);
        initDisputeList();
        hideLoadingScreen();

        fabCharteringMenu.setIconAnimated(false);
        fabCharteringMenu.setMenuButtonColorNormal(Color.parseColor("#F68B1F"));
        fabViewCharter.setOnClickListener(clickListener);
        fabNewCharter.setOnClickListener(clickListener);
        fabSuccessfulCharter.setOnClickListener(clickListener);
        fabYourCharter.setOnClickListener(clickListener);
        fabToTracker.setOnClickListener(clickListener);
        fabProfile.setOnClickListener(clickListener);

    }

    private void initDisputeList() {
//        TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams (TableLayout.LayoutParams.FILL_PARENT,TableLayout.LayoutParams.WRAP_CONTENT);
//        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
//        String text;
//        tableRowParams.setMargins(0,25,0,0);
        int count;
        try {
            count = disputeList.size();
        } catch (Exception e) {
            count = 0;
        }

        if (count == 0) {
            ll_empty_page.setVisibility(View.VISIBLE);
            iv_empty_page.setVisibility(View.VISIBLE);
            txt_empty_page.setVisibility(View.VISIBLE);
            disputeListing.setVisibility(View.GONE);
        } else {
            for (int i = 0; i < count; i++) {
                final Disputes list = disputeList.get(i);
                disputeListing.addView(createRow(list.getId(), list.getAccessCode(), list.getCost() + "", list.getDetails(), list.isRequiresAction(), list.isSettled(),
                        list.isPendingAdmin(), list.getDisputeReasons(), list.getRejectedReason(), list.getAdminInput()));
            }
        }
    }

    private View createRow(final String id, String accessCode, final String cost, String details, boolean requiresAction, boolean isSettled, boolean pendingAdmin, final String disputeReason, String rebukedReason,
                           String adminComments) {
        TableRow tr = new TableRow(this);
        View v = LayoutInflater.from(context).inflate(R.layout.transaction_row_template, tr, false);

        TextView txtCharterId = (TextView)v.findViewById(R.id.txt_charter_id);
        txtCharterId.setText(accessCode);

        TextView txtCharterCost = (TextView)v.findViewById(R.id.txt_charter_cost);
        txtCharterCost.setText("$" + cost);

        TextView txtCharterDetails = (TextView)v.findViewById(R.id.txt_charter_details);
        txtCharterDetails.setText(details);

        TextView txtDisputeStatusTitle = (TextView)v.findViewById(R.id.transaction_title);
        txtDisputeStatusTitle.setText("Status");
        TextView txtDisputeStatus = (TextView)v.findViewById(R.id.txt_transaction_type);
        if (requiresAction) {
            txtDisputeStatus.setText("Requires action");
            txtDisputeStatus.setTextColor(Color.RED);
        } else if (isSettled) {
            txtDisputeStatus.setText("Settled");
            txtDisputeStatus.setTextColor(Color.GREEN);
        } else if (pendingAdmin) {
            txtDisputeStatus.setText("Pending");
        } else {
            txtDisputeStatus.setText("Pending");
        }
        TextView txtDisputeReasonTitle = (TextView)v.findViewById(R.id.txt_dispute_reason_title);
        txtDisputeReasonTitle.setVisibility(View.VISIBLE);
        TextView txtDisputeReason = (TextView)v.findViewById(R.id.txt_dispute_reason);
        txtDisputeReason.setVisibility(View.VISIBLE);
        txtDisputeReason.setText(disputeReason);

        if (!rebukedReason.isEmpty() && !rebukedReason.equalsIgnoreCase("")) {
            TextView txtRebukedReasonTitle = (TextView)v.findViewById(R.id.txt_your_reason_title);
            txtRebukedReasonTitle.setVisibility(View.VISIBLE);
            TextView txtRebukedReason = (TextView) v.findViewById(R.id.txt_your_reason);
            txtRebukedReason.setVisibility(View.VISIBLE);
            txtRebukedReason.setText(rebukedReason);
        }

        if (!adminComments.isEmpty() && !adminComments.equalsIgnoreCase("")) {
            TextView txtAdminCommentsTitle = (TextView)v.findViewById(R.id.txt_admin_comments_title);
            txtAdminCommentsTitle.setVisibility(View.VISIBLE);
            TextView txtAdminComments = (TextView) v.findViewById(R.id.txt_admin_comments);
            txtAdminComments.setVisibility(View.VISIBLE);
            txtAdminComments.setText(adminComments);
        }

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                showLoadingScreen();
                builder.setTitle(Constants.DISPUTE_TITLE)
                        .setMessage(disputeReason + " | $" + cost)
                        .setCancelable(false)
                        .setPositiveButton("Yes - I agree to compensate for the charges above", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mSendDisputeResponseTask = new SendDisputeResponseTask(true, id, "");
                                mSendDisputeResponseTask.execute((Void) null);
                            }
                        })
                        .setNegativeButton("No - I do not agree to the charges above", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final AlertDialog.Builder builder2 = new AlertDialog.Builder(context);
                                final EditText txt_remarks = new EditText(context);
                                txt_remarks.setHint("Please key in your argument for this dispute");

                                builder2.setView(txt_remarks)
                                        .setCancelable(false)
                                        .setPositiveButton("Submit my argument", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                try {
                                                    dialog.dismiss();
                                                    String additionalInfo = txt_remarks.getText().toString();

                                                    mSendDisputeResponseTask = new SendDisputeResponseTask(false, id, additionalInfo);
                                                    mSendDisputeResponseTask.execute((Void) null);
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
                        .setNeutralButton("Return to dispute list", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                hideLoadingScreen();
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

        return v;
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        Intent intent;
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.fab_newCharter5:
                    intent = new Intent(getApplicationContext(), NewCharterActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case R.id.fab_viewCharter5:
                    intent = new Intent(getApplicationContext(), AvailableCharterActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case R.id.fab_successfulBids5:
                    intent = new Intent(getApplicationContext(), YourCharterActivity.class);
                    intent.putExtra("intention","acceptedCharters");
                    startActivity(intent);
                    finish();
                    break;
                case R.id.fab_ownCharter5:
                    intent = new Intent(getApplicationContext(), YourCharterActivity.class);
                    intent.putExtra("intention","ownCharter");
                    startActivity(intent);
                    finish();
                    break;
                case R.id.fab_toTracking5:
                    intent = new Intent(getApplicationContext(), RouteActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case R.id.fab_profile5:
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
                        DisputedCharterActivity.this.finishAffinity();
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
    private class GetDisputedCharterList extends WebServiceTask {
        GetDisputedCharterList(){
            super(DisputedCharterActivity.this);
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
            String URL = Constants.VIEW_DISPUTE_LIST_URL;
            obj = WebServiceUtils.requestJSONObject(URL, WebServiceUtils.METHOD.GET, authenticationToken, context);
            if(!hasError(obj)) {
                Log.d(LOG_TAG, obj.toString());
                jsonObject = obj.optJSONObject(Constants.DATA);

                if (jsonObject == null) {
                    return false;
                } else {
                    try {
                        JSONArray disputeArray = jsonObject.optJSONArray(Constants.HISTORY);
                        for (int i = 0; i < disputeArray.length(); i++) {
                            JSONObject objPx = disputeArray.getJSONObject(i);
                            String accessCode = objPx.getString(Constants.CHARTER_ACCESS_CODE);
                            String details = objPx.getString(Constants.TRANSACTION_DETAILS);
                            Double cost = objPx.getDouble(Constants.CHARTER_COST);
                            boolean requiresAction = objPx.getBoolean(Constants.REQUIRESACTION);
                            boolean isSettled = objPx.getBoolean(Constants.ISSETTLED);
                            boolean pendingAdmin = objPx.getBoolean(Constants.PENDINGADMIN);
                            String id = objPx.getString(Constants.CHARTER_ID);
                            String rebukedReason = objPx.getString(Constants.REBUKEDREASON);
                            String reasons = objPx.getString(Constants.REASON);
                            String adminInput = objPx.getString(Constants.ADMININPUT);

                            Disputes listing = new Disputes(accessCode, details, cost, requiresAction, isSettled, pendingAdmin, id, rebukedReason, reasons, adminInput);
                            disputeList.add(listing);
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
            //do nothing
        }

        @Override
        public void onFailedAttempt() {
            int statusCode = prefs.getInt(Preferences.STATUSCODE, 0);
            if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                resetApp();
            }
        }
    }

    private class SendDisputeResponseTask extends WebServiceTask {
        SendDisputeResponseTask(boolean hasAgreed, String id, String rebukedReason){
            super(DisputedCharterActivity.this);
            contentValues = new ContentValues();
            contentValues.put(Constants.HASAGREED, hasAgreed);
            contentValues.put(Constants.CHARTER_ID, id);
            contentValues.put(Constants.REBUKEDREASON, rebukedReason);

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
            obj = WebServiceUtils.requestJSONObject(Constants.DISPUTE_RESPONSE_URL, WebServiceUtils.METHOD.POST, authenticationToken, null, context, contentValues);
            if(!hasError(obj)) {
                Log.d(LOG_TAG, obj.toString());
                jsonObject = obj.optJSONObject(Constants.DATA);

                if (jsonObject != null) {
                    displayMessage = "";
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
                            Intent intent = new Intent(getApplicationContext(), DisputedCharterActivity.class);
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
            }
        }
    }
}
