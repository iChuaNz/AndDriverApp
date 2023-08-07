package sg.com.commute_solutions.bustracker.fragments.charters;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

import sg.com.commute_solutions.bustracker.R;
import sg.com.commute_solutions.bustracker.common.Constants;
import sg.com.commute_solutions.bustracker.common.Preferences;
import sg.com.commute_solutions.bustracker.fragments.LoginActivity;
import sg.com.commute_solutions.bustracker.util.StringUtil;
import sg.com.commute_solutions.bustracker.webservices.WebServiceTask;
import sg.com.commute_solutions.bustracker.webservices.WebServiceUtils;

/**
 * Created by Kyle on 17/5/17.
 */

public class UpdateCharterActivity extends AppCompatActivity {
    private static final String LOG_TAG = UpdateCharterActivity.class.getSimpleName();

    private Context context;
    private SharedPreferences prefs;
    private String authToken;
    private final ContentValues authenticationToken = new ContentValues();
    private boolean isOwner = true;

    private AlertDialog loadingScreen;
    private String charterId;
    private String charterType, date, cost, remarks;
    private int busType, disposalDuration;
    private String[] pickUpName, dropOffName, pickUpTime, dropOffTime;
    private TableLayout charterInfo;

    private JSONObject obj, jsonObject;
    private ContentValues contentValues;
    private String displayMessage;

    private GetCharterTask mGetCharterTask = null;
    private UpdatePOCTask mUpdatePOCTask = null;
    private UpdateJobTask mUpdateJobTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_charter);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        context = this;

        prefs = this.getSharedPreferences(Constants.SHARE_PREFERENCE_PACKAGE, Context.MODE_PRIVATE);
        authToken = StringUtil.deNull(prefs.getString(Preferences.AUTH_TOKEN, ""));
        authenticationToken.put(Constants.TOKEN, authToken);

        LayoutInflater lI = LayoutInflater.from(context);
        View promptView = lI.inflate(R.layout.loading_screen, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(promptView);
        loadingScreen = alertDialogBuilder.create();
        loadingScreen.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        loadingScreen.setCanceledOnTouchOutside(false);

        String info = getIntent().getStringExtra(Constants.EXTRA);
        charterId = getIntent().getStringExtra(Constants.BUS_CHARTER_ID);

        try {
            showLoadingScreen();
            mGetCharterTask = new GetCharterTask(charterId);
            mGetCharterTask.execute((Void) null);
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

        if (isOwner) {
            initCharterDetails(info);
        }
        hideLoadingScreen();
    }

    @Override
    public void onBackPressed() {
        if (isTaskRoot()) {
            //do nothing
        } else {
            super.onBackPressed();
        }
    }

    private void initCharterDetails(String info) {
        ScrollView.LayoutParams tableRowParams = new ScrollView.LayoutParams (TableLayout.LayoutParams.FILL_PARENT,TableLayout.LayoutParams.WRAP_CONTENT);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
        charterInfo = (TableLayout) findViewById(R.id.table_charterInfo);
        charterInfo.removeAllViews();
        charterInfo.setLayoutParams(tableRowParams);
        String text = "";

        //type
        TableRow serviceTypeRow = new TableRow(context);
        serviceTypeRow.setLayoutParams(lp);
        TextView typeView = new TextView(getApplicationContext());

        if (charterType.equalsIgnoreCase("oneway")) {
            text = "<b><u><br/>One-Way</u></b>";
        } else if (charterType.equalsIgnoreCase("twoway")) {
            text = "<b><u><br/>Two-Way</u></b>";
        } else if (charterType.equalsIgnoreCase("disposal")) {
            text = "<b><u><br/>Disposal</u></b>";
        }

        typeView.setText(Html.fromHtml(text));
        typeView.setTextSize(19.0f);
        typeView.setTextColor(Color.BLACK);
        serviceTypeRow.addView(typeView);
        charterInfo.addView(serviceTypeRow);

        //date
        TableRow row1 = new TableRow(context);
        row1.setLayoutParams(lp);
        TextView dateView = new TextView(getApplicationContext());
        text = "<b><br/>" + date + "</b>→ <b>" + pickUpTime[0] + "</b>";
        dateView.setText(Html.fromHtml(text));
        dateView.setTextSize(17.0f);
        dateView.setTextColor(Color.BLACK);
        row1.addView(dateView);
        charterInfo.addView(row1);

        //cost
        TableRow row2 = new TableRow(context);
        row2.setLayoutParams(lp);
        TextView costView = new TextView(getApplicationContext());
        text = "<b><br/>Price: $</b>" + cost;
        costView.setText(Html.fromHtml(text));
        costView.setTextColor(Color.BLACK);
        costView.setTextSize(17.0f);
        row2.addView(costView);
        charterInfo.addView(row2);

        //pickup name
        String[] pickupPointName = pickUpName;
        int noOfPickUpPoints = pickupPointName.length;
        for (int i = 0; i < noOfPickUpPoints; i++) {
            TableRow row3 = new TableRow(context);
            row3.setLayoutParams(lp);
            TextView pickupPoint = new TextView(getApplicationContext());
            pickupPoint.setSingleLine(false);
            if (i == 0) {
                text = "<b><font color='#28C546'>From</font>: </b>" + pickupPointName[i];
                try {
                    text = new StringBuilder(text).insert(77, "<br/>").toString();
                } catch (Exception e) {
                    //do nothing
                }
//                finally {
//                    text += " → <b>" + pickUpTime[i] + "</b>";
//                }
                if (pickupPointName.length > 1) {
                    text = new StringBuilder(text).append("<br/><b>Additional points(Not Shown in Map): </b>").toString();
                }
            } else {
                text = "<b>" + (i + 1) +": </b>" + pickupPointName[i];
                try {
                    text = new StringBuilder(text).insert(45, "<br/>").toString();
                } catch (Exception e) {
                    //do nothing
                }
//                finally {
//                    text += " → <b>" + pickUpTime[i] + "</b>";
//                }
            }
            pickupPoint.setText(Html.fromHtml(text));
            pickupPoint.setTextColor(Color.BLACK);
            pickupPoint.setTextSize(17.0f);
            row3.addView(pickupPoint);
            charterInfo.addView(row3);
        }

        //dropoff name
        String[] dropoffPointName = dropOffName;
        int noOfDropOffPoints = dropoffPointName.length;
        for (int i = 0; i < noOfDropOffPoints; i++) {
            TableRow row4 = new TableRow(context);
            row4.setLayoutParams(lp);
            TextView dropOffPoint = new TextView(getApplicationContext());
            dropOffPoint.setSingleLine(false);
            if (i == 0) {
                text = "<b><br/><font color='#289FC5'>To</font>: </b>" + dropoffPointName[i];
                try {
                    text = new StringBuilder(text).insert(77, "<br/>").toString();
                } catch (Exception e) {
                    //do nothing
                }
//                finally {
//                    if (dropOffTime.length != 0) {
//                        text += " → <b>" + dropOffTime[i] + "</b>";
//                    }
//                }
                if (dropoffPointName.length > 1) {
                    text = new StringBuilder(text).append("<br/><b>Additional points(Not Shown in Map): </b>").toString();
                }
            } else {
                text = "<b>" + (i + 1) +": </b><u>" + dropoffPointName[i] + "</u>";
                try {
                    text = new StringBuilder(text).insert(45, "<br/>").toString();
                } catch (Exception e) {
                    //do nothing
                }
//                finally {
//                    if (dropOffTime.length != 0) {
//                        text += " → <b>" + dropOffTime[i] + "</b>";
//                    }
//                }
            }

            dropOffPoint.setText(Html.fromHtml(text));
            dropOffPoint.setTextColor(Color.BLACK);
            dropOffPoint.setTextSize(17.0f);
            row4.addView(dropOffPoint);
            charterInfo.addView(row4);
        }

        //bus type
        TableRow row5 = new TableRow(context);
        row5.setLayoutParams(lp);
        TextView busTypeView = new TextView(getApplicationContext());
        text = "<b><br/>Bus Type: </b>" + busType + " - Seater";
        text = new StringBuilder(text).append("<br/>").toString();
        busTypeView.setText(Html.fromHtml(text));
        busTypeView.setTextColor(Color.BLACK);
        busTypeView.setTextSize(17.0f);
        row5.addView(busTypeView);
        charterInfo.addView(row5);

        //chartering
        if (disposalDuration > 2) {
            TableRow row6 = new TableRow(context);
            row6.setLayoutParams(lp);
            TextView charteringHrsView = new TextView(getApplicationContext());
            text = "<b>No. of Hours: </b>" + disposalDuration;
            charteringHrsView.setText(Html.fromHtml(text));
            charteringHrsView.setTextColor(Color.BLACK);
            charteringHrsView.setTextSize(17.0f);
            row6.addView(charteringHrsView);
            charterInfo.addView(row6);
        }

        //remarks
        if (remarks != null && !remarks.equalsIgnoreCase("null")) {
            TableRow row7 = new TableRow(context);
            row7.setLayoutParams(lp);
            TextView remarksView = new TextView(getApplicationContext());

            text = "<b>Remarks: </b>" + remarks;
            try {
                text = new StringBuilder(text).insert(45, "<br/>").toString();
            } catch (Exception e) {
                //do nothing
            }
            remarksView.setText(Html.fromHtml(text));
            remarksView.setTextColor(Color.BLACK);
            remarksView.setTextSize(17.0f);
            row7.addView(remarksView);
            charterInfo.addView(row7);
        }

        //buttons
        TableRow row8 = new TableRow(context);
        row8.setLayoutParams(lp);
        LinearLayout btnLayout = new LinearLayout(context);

        if (info.equalsIgnoreCase("poc")) {
            Button editBtn = new Button(context);
            editBtn.setText("Edit POC");
            editBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
                                    try {
                                        String pocName = txt_pocName.getText().toString();
                                        String pocContactNo = txt_pocContactNo.getText().toString();

                                        try {
                                            Thread.sleep(3000);
                                        } catch(InterruptedException e) {
                                            //do nothing
                                        }
                                        mUpdatePOCTask = new UpdatePOCTask(charterId, pocName, pocContactNo);
                                        mUpdatePOCTask.execute((Void) null);
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
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }
            });

            btnLayout.addView(editBtn);
        } else if (info.equalsIgnoreCase("expiry")) {
            Button resubmitBtn = new Button(context);
            resubmitBtn.setText("Resubmit Charter");
            resubmitBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    final EditText txt_cost = new EditText(context);
                    txt_cost.setInputType(InputType.TYPE_CLASS_NUMBER);
                    txt_cost.setHint("(Optional) eg. 100");

                    builder.setMessage(Constants.RESUBMISSION_CONFIRMATION_MESSAGE)
                            .setView(txt_cost)
                            .setCancelable(false)
                            .setPositiveButton("Resubmit", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        dialog.dismiss();
                                        showLoadingScreen();
                                        String newCost = txt_cost.getText().toString();
                                        try {
                                            Thread.sleep(4000);
                                        } catch(InterruptedException e) {
                                            //do nothing
                                        }
                                        mUpdateJobTask = new UpdateJobTask(charterId, newCost);
                                        mUpdateJobTask.execute((Void) null);
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
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }
            });

            btnLayout.addView(resubmitBtn);
        }

        Button cancelBtn = new Button(context);
        cancelBtn.setText("Later");
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AvailableCharterActivity.class);
                startActivity(intent);
                finish();
            }
        });
        btnLayout.addView(cancelBtn);

        row8.addView(btnLayout);
        charterInfo.addView(row8);

    }

    private void showLoadingScreen() {
        LayoutInflater lI = LayoutInflater.from(context);
        View promptView = lI.inflate(R.layout.loading_screen, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(promptView);

        loadingScreen = alertDialogBuilder.create();
        loadingScreen.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        loadingScreen.show();
        loadingScreen.setCanceledOnTouchOutside(false);
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
                        UpdateCharterActivity.this.finishAffinity();
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

    private class GetCharterTask extends WebServiceTask {
        GetCharterTask(String id){
            super(UpdateCharterActivity.this);
            contentValues = new ContentValues();
            contentValues.put(Constants.BUS_CHARTER_ID, id);

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
            obj = WebServiceUtils.requestJSONObject(Constants.RETRIEVEJOB_URL, WebServiceUtils.METHOD.POST, authenticationToken, null, context, contentValues);
            if(!hasError(obj)) {
                Log.d(LOG_TAG, obj.toString());
                jsonObject = obj.optJSONObject(Constants.DATA);

                if (jsonObject == null) {
                    return false;
                } else {
                    try {
                        displayMessage = jsonObject.getString(Constants.BUS_CHARTER_MESSAGE);
                        JSONObject objPx = jsonObject.optJSONObject(Constants.CHARTER);

                        charterType = objPx.getString(Constants.CHARTER_SERVICE_TYPE);
                        date = objPx.getString(Constants.CHARTER_DATE);
                        cost = objPx.getString(Constants.CHARTER_COST);
                        busType = objPx.getInt(Constants.CHARTER_BUSTYPE);
                        disposalDuration = objPx.getInt(Constants.CHARTER_DISPOSAL_DURATION);
                        JSONArray tempArray1 = objPx.getJSONArray(Constants.CHARTER_PICKUP_NAME);
                        pickUpName = new String[tempArray1.length()];
                        for (int j = 0; j < tempArray1.length(); j++) {
                            pickUpName[j] = tempArray1.getString(j);
                        }
                        JSONArray tempArray2 = objPx.getJSONArray(Constants.CHARTER_DROPOFF_NAME);
                        dropOffName = new String[tempArray2.length()];
                        for (int j = 0; j < tempArray2.length(); j++) {
                            dropOffName[j] = tempArray2.getString(j);
                        }
                        JSONArray tempArray3 = objPx.getJSONArray(Constants.CHARTER_PICKUPTIME);
                        pickUpTime = new String[tempArray3.length()];
                        for (int j = 0; j < tempArray3.length(); j++) {
                            pickUpTime[j] = tempArray3.getString(j);
                        }
                        JSONArray tempArray4 = objPx.getJSONArray(Constants.CHARTER_DROPOFFTIME);
                        dropOffTime = new String[tempArray4.length()];
                        for (int j = 0; j < tempArray4.length(); j++) {
                        dropOffTime[j] = tempArray4.getString(j);
                        }
                        remarks = objPx.getString(Constants.CHARTER_REMARKS);

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
            if (!displayMessage.equalsIgnoreCase("") || !displayMessage.isEmpty()) {
                isOwner = false;
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
                                intent.putExtra("intention","ownCharter");
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

    private class UpdateJobTask extends WebServiceTask {
        UpdateJobTask(String id, String newCost){
            super(UpdateCharterActivity.this);
            contentValues = new ContentValues();
            contentValues.put(Constants.BUS_CHARTER_ID, id);
            contentValues.put(Constants.CHARTER_COST, newCost);

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
            obj = WebServiceUtils.requestJSONObject(Constants.UPDATEJOB_URL, WebServiceUtils.METHOD.POST, authenticationToken, null, context, contentValues);
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
                            intent.putExtra("intention","ownCharter");
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

    private class UpdatePOCTask extends WebServiceTask {
        boolean success =  false;
        UpdatePOCTask(String jobId, String name, String contactNo){
            super(UpdateCharterActivity.this);
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
                                intent.putExtra("intention", "ownCharter");
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
            }
        }
    }
}
