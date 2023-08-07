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
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import sg.com.commute_solutions.bustracker.R;
import sg.com.commute_solutions.bustracker.common.Constants;
import sg.com.commute_solutions.bustracker.common.Preferences;
import sg.com.commute_solutions.bustracker.data.TransactionHistory;
import sg.com.commute_solutions.bustracker.fragments.LoginActivity;
import sg.com.commute_solutions.bustracker.util.StringUtil;
import sg.com.commute_solutions.bustracker.webservices.WebServiceTask;
import sg.com.commute_solutions.bustracker.webservices.WebServiceUtils;

/**
 * Created by Kyle on 23/5/17.
 */

public class TransactionHistoryActivity extends AppCompatActivity
        implements Serializable {
    private static final String LOG_TAG = TransactionHistoryActivity.class.getSimpleName();
    private Context context;
    private SharedPreferences prefs;

    private String authToken;
    private final ContentValues authenticationToken = new ContentValues();

    private JSONObject obj, jsonObject;
    private GetEWalletHistory mGetEWalletHistory;

    private LinearLayout ll_empty_page;
    private ImageView iv_empty_page;
    private TextView txt_empty_page;

    private AlertDialog loadingScreen;
    private TableLayout transactionListing;
    private ArrayList<TransactionHistory> transactionHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);
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

        ll_empty_page = (LinearLayout) findViewById(R.id.empty_transaction_background);
        iv_empty_page = (ImageView) findViewById(R.id.iv_empty_transaction_logo);
        txt_empty_page = (TextView) findViewById(R.id.txt_empty_transaction_message);

        transactionHistory = new ArrayList<>();
        try {
            mGetEWalletHistory = new GetEWalletHistory();
            mGetEWalletHistory.execute((Void) null);
        } catch (Exception e) {
            //do nothing
        }

        transactionListing = (TableLayout) findViewById(R.id.table_transactionhistory);
        initTransactionHistoryDetails();
        hideLoadingScreen();
    }

    private void initTransactionHistoryDetails() {
        transactionListing.removeAllViews();

        int count = transactionHistory.size();
        if (count == 0) {
            ll_empty_page.setVisibility(View.VISIBLE);
            iv_empty_page.setVisibility(View.VISIBLE);
            txt_empty_page.setVisibility(View.VISIBLE);
            transactionListing.setVisibility(View.GONE);
        } else {
            for (int i = 0; i < count; i++) {
                final TransactionHistory history = transactionHistory.get(i);
                transactionListing.addView(createRow(history.getId(),history.getTransactionAmount(), history.getTransactionDetails(), history.getTransactionMethod(), history.isWithdraw()));
            }
        }
    }

    private View createRow(String id, String transactionAmt, String details, String paymentMethod, boolean isWithdraw) {
        TableRow tr = new TableRow(this);
        View v = LayoutInflater.from(context).inflate(R.layout.transaction_row_template, tr, false);

        TextView txtCharterId = (TextView)v.findViewById(R.id.txt_charter_id);
        txtCharterId.setText(id);

        TextView txtCharterCost = (TextView)v.findViewById(R.id.txt_charter_cost);
        txtCharterCost.setText("$" + transactionAmt);

        TextView txtCharterDetails = (TextView)v.findViewById(R.id.txt_charter_details);
        txtCharterDetails.setText(details);

        TextView txtTransaction = (TextView)v.findViewById(R.id.txt_transaction_type);
        if (isWithdraw && paymentMethod.equalsIgnoreCase("eWallet")) {
            txtTransaction.setText("Deducted from wallet");
            txtTransaction.setTextColor(Color.RED);
        } else if (isWithdraw && paymentMethod.equalsIgnoreCase("creditCard")) {
            txtTransaction.setText("Deducted from Credit Card");
            txtTransaction.setTextColor(Color.RED);
        } else if (!isWithdraw && paymentMethod.equalsIgnoreCase("eWallet")) {
            txtTransaction.setText("Added to wallet");
            txtTransaction.setTextColor(Color.GREEN);
        } else {
            txtTransaction.setText("Refunded back to Credit Card");
            txtTransaction.setTextColor(Color.DKGRAY);
        }

        return v;
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
                        TransactionHistoryActivity.this.finishAffinity();
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
    private class GetEWalletHistory extends WebServiceTask {
        GetEWalletHistory(){
            super(TransactionHistoryActivity.this);
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
            String URL = Constants.WALLETHISTORY_URL;
            obj = WebServiceUtils.requestJSONObject(URL, WebServiceUtils.METHOD.GET, authenticationToken, context);
            if(!hasError(obj)) {
                Log.d(LOG_TAG, obj.toString());
                jsonObject = obj.optJSONObject(Constants.DATA);

                if (jsonObject == null) {
                    return false;
                } else {
                    try {
                        JSONArray walletHistory = jsonObject.optJSONArray(Constants.HISTORY);
                        for (int i = 0; i < walletHistory.length(); i++) {
                            JSONObject objPx = walletHistory.getJSONObject(i);
                            String id = objPx.getString(Constants.CHARTER_ID);
                            String transactionDate = objPx.getString(Constants.CHARTER_DATE);
                            boolean isWithdraw = objPx.getBoolean(Constants.TRANSACTION_TYPE);
                            String transactionDetails = objPx.getString(Constants.TRANSACTION_DETAILS);
                            String serviceType = objPx.getString(Constants.CHARTER_SERVICE_TYPE);
                            String transactionAmount = objPx.getString(Constants.TRANSACTED_AMOUNT);
                            String transactionMethod = objPx.getString(Constants.TRANSACTION_METHOD);

                            TransactionHistory listing = new TransactionHistory(id, transactionDate, isWithdraw, transactionDetails, serviceType, transactionAmount, transactionMethod);
                            transactionHistory.add(listing);
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
}
