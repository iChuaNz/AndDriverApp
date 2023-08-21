package sg.com.commute_solutions.bustracker.fragments.charters.contractservices;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;

import sg.com.commute_solutions.bustracker.R;
import sg.com.commute_solutions.bustracker.common.Constants;
import sg.com.commute_solutions.bustracker.common.Preferences;
import sg.com.commute_solutions.bustracker.data.Contract;
import sg.com.commute_solutions.bustracker.fragments.LoginActivity;
import sg.com.commute_solutions.bustracker.fragments.charters.NewCharterActivity;
import sg.com.commute_solutions.bustracker.util.StringUtil;
import sg.com.commute_solutions.bustracker.webservices.WebServiceTask;
import sg.com.commute_solutions.bustracker.webservices.WebServiceUtils;

/**
 * Created by Kyle on 10/1/18.
 */

public class ViewAllContracts extends AppCompatActivity {
    private static final String LOG_TAG = ViewAllContracts.class.getSimpleName();

    private Context context;
    private SharedPreferences prefs;
    private String authToken;
    private final ContentValues authenticationToken = new ContentValues();

    private FloatingActionMenu fabContractMenu;
    private FloatingActionButton fabToCharter;
    private FloatingActionButton fabCreateContracts;

    private SwipeRefreshLayout swipeLayout;
    private TableLayout contractList;
    private AlertDialog loadingScreen;

    private ArrayList<Contract> contracts;
    private JSONObject obj, jsonObject;
    private FetchAllAvailableContracts mFetchAllAvailableContracts = null;
    private DeleteContractTask mDeleteContractTask = null;
    private ContentValues contentValues;
    boolean isSuccessful = false;
    private String displayMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contract_viewall);
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

        fabContractMenu = (FloatingActionMenu) findViewById(R.id.fab_contractMenu4);
        fabToCharter = (FloatingActionButton) findViewById(R.id.fab_toChartering4);
        fabCreateContracts = (FloatingActionButton) findViewById(R.id.fab_createcontract);

        fabContractMenu.setIconAnimated(false);
        fabContractMenu.setMenuButtonColorNormal(Color.parseColor("#F68B1F"));
        fabToCharter.setOnClickListener(clickListener);
        fabToCharter.setColorNormal(Color.parseColor("#F68B1F"));
        fabCreateContracts.setOnClickListener(clickListener);
        fabCreateContracts.setColorNormal(Color.parseColor("#F68B1F"));

        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipecontractrefresh);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Intent intent = new Intent(getApplicationContext(), ViewAllContracts.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        try {
            mFetchAllAvailableContracts = new FetchAllAvailableContracts();
            mFetchAllAvailableContracts.execute((Void) null);
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

        contractList = (TableLayout) findViewById(R.id.table_contractList);
        contractList.removeAllViews();

        if (contracts != null && contracts.size() > 0) {
            for (int i = 0; i < contracts.size(); i++) {
                contractList.addView(createRow(contracts.get(i)));
            }
        }
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        Intent intent;
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.fab_toChartering4:
                    intent = new Intent(getApplicationContext(), NewCharterActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case R.id.fab_createcontract:
                    intent = new Intent(getApplicationContext(), PickUpPointActivity.class);
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

    private View createRow(final Contract contract) {
        TableRow tr = new TableRow(this);
        View v = LayoutInflater.from(context).inflate(R.layout.contract_row_template, tr, false);

        //Start Date
        TextView txtContractStartDate = (TextView)v.findViewById(R.id.contractStartDate);
        txtContractStartDate.setText(contract.getStartDate());

        //Pickup Time
        TextView txtPickupDate = (TextView) v.findViewById(R.id.contractPickupTime);
        txtPickupDate.setText(contract.getPickupTime());

        //Pickup Point & Checkbox
        TextView txtPickupPoint = (TextView) v.findViewById(R.id.contractPickup);
        CheckBox cbMorePickup = (CheckBox) v.findViewById(R.id.cb_hasAdditionalPickupPoints);
        txtPickupPoint.setText(contract.getPickup1Name());
        String pickupPoint2 = StringUtil.deNull(contract.getPickup2Name());
        String pickupPoint3 = StringUtil.deNull(contract.getPickup3Name());
        if (!pickupPoint2.isEmpty() || !pickupPoint3.isEmpty()) {
            cbMorePickup.setChecked(true);
        }

        //Dropoff Point & Checkbox
        TextView txtDropoffPoint = (TextView) v.findViewById(R.id.contractDropoff);
        CheckBox cbMoreDropOff = (CheckBox) v.findViewById(R.id.cb_hasAdditionalDropOffPoints);
        String dropOffPoint = StringUtil.deNull(contract.getDropoff3Name());
        cbMoreDropOff.setChecked(true);
        if (dropOffPoint.isEmpty()) {
            dropOffPoint = StringUtil.deNull(contract.getDropoff2Name());
        }
        if (dropOffPoint.isEmpty()) {
            dropOffPoint = StringUtil.deNull(contract.getDropoff1Name());
            cbMoreDropOff.setChecked(false);
        }
        txtDropoffPoint.setText(dropOffPoint);

        TextView txtCost = (TextView) v.findViewById(R.id.contractCost);
        txtCost.setText("$" + contract.getCostPerMonthInString());

        TextView txtBusSize = (TextView) v.findViewById(R.id.contractBusType);
        txtBusSize.setText(contract.getBusSize());

        TextView txtStatus = (TextView) v.findViewById(R.id.contractStatus);
        boolean isOwnCharter = contract.isOwnCharter();
        if (isOwnCharter) {
            txtStatus.setText("Your Contract!");
            txtStatus.setBackgroundColor(Color.MAGENTA);
        } else {
            txtStatus.setBackgroundColor(Color.BLUE);
        }


        Button btnViewContract = (Button) v.findViewById(R.id.btnViewContractJobs);
        btnViewContract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Gson gson = new Gson();
                String jsonString = gson.toJson(contract);
                Intent intent = new Intent(getApplicationContext(), ViewContractService.class);
                intent.putExtra("selectedContract", jsonString);
                startActivity(intent);
            }
        });


        boolean isOwnContract = contract.isOwnCharter();
        final String id = contract.getId();
        Button btnRemoveListing = (Button)v.findViewById(R.id.btnRemoveContractJob);
        if (isOwnContract) {
            btnRemoveListing.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showLoadingScreen();
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Warning!")
                            .setMessage("Are you sure you wish to remove this listing?")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    mDeleteContractTask = new DeleteContractTask(id);
                                    mDeleteContractTask.execute((Void) null);
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
        } else {
            btnRemoveListing.setVisibility(View.GONE);
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
                        ViewAllContracts.this.finishAffinity();
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

    private class FetchAllAvailableContracts extends WebServiceTask {
        FetchAllAvailableContracts() {
            super(ViewAllContracts.this);
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
            String URL = Constants.VIEW_CONTRACT_URL;
            obj = WebServiceUtils.requestJSONObject(URL, WebServiceUtils.METHOD.GET, authenticationToken, context);

            if(!hasError(obj)) {
                Log.d(LOG_TAG, obj.toString());
                JSONObject objPx;
                jsonObject = obj.optJSONObject(Constants.DATA);

                if (jsonObject == null) {
                    return false;
                } else {
                    JSONArray contractList = jsonObject.optJSONArray(Constants.VIEW_CONTRACTLIST);
                    try {
                        contracts = new ArrayList<>();
                        for (int i = 0; i <contractList.length(); i++) {
                            objPx = contractList.getJSONObject(i);
                            String id = objPx.getString(Constants.CHARTER_ID);
                            String startDate = objPx.getString(Constants.CONTRACT_START_DATE);
                            String endDate = objPx.getString(Constants.CONTRACT_END_DATE);
                            String pickupTime = objPx.getString(Constants.CHARTER_PICKUPTIME);
                            String contactNo = objPx.getString(Constants.CONTRACT_CONTACT_NO);

                            String pickup1Name = objPx.getString(Constants.PICKUP_POINT_1_NAME);
                            LatLng pickup1LatLng = new LatLng(objPx.getDouble(Constants.PICKUP_POINT_1_LATITUDE), objPx.getDouble(Constants.PICKUP_POINT_1_LONGITUDE));

                            String pickup2Name = objPx.getString(Constants.PICKUP_POINT_2_NAME);
                            LatLng pickup2LatLng;
                            if (pickup2Name.isEmpty()) {
                                pickup2LatLng = new LatLng(0,0);
                            } else {
                                pickup2LatLng = new LatLng(objPx.getDouble(Constants.PICKUP_POINT_2_LATITUDE), objPx.getDouble(Constants.PICKUP_POINT_2_LONGITUDE));
                            }

                            String pickup3Name = objPx.getString(Constants.PICKUP_POINT_3_NAME);
                            LatLng pickup3LatLng;
                            if (pickup3Name.isEmpty()) {
                                pickup3LatLng = new LatLng(0,0);
                            } else {
                                pickup3LatLng = new LatLng(objPx.getDouble(Constants.PICKUP_POINT_3_LATITUDE), objPx.getDouble(Constants.PICKUP_POINT_3_LONGITUDE));
                            }

                            String dropoff1Name = objPx.getString(Constants.DROPOFF_POINT_1_NAME);
                            LatLng dropoff1LatLng = new LatLng(objPx.getDouble(Constants.DROPOFF_POINT_1_LATITUDE), objPx.getDouble(Constants.DROPOFF_POINT_1_LONGITUDE));

                            String dropoff2Name = objPx.getString(Constants.DROPOFF_POINT_2_NAME);
                            LatLng dropoff2LatLng;
                            if (dropoff2Name.isEmpty()) {
                                dropoff2LatLng = new LatLng(0,0);
                            } else {
                                dropoff2LatLng = new LatLng(objPx.getDouble(Constants.DROPOFF_POINT_2_LATITUDE), objPx.getDouble(Constants.DROPOFF_POINT_2_LONGITUDE));
                            }

                            String dropoff3Name = objPx.getString(Constants.DROPOFF_POINT_3_NAME);
                            LatLng dropoff3LatLng;
                            if (dropoff3Name.isEmpty()) {
                                dropoff3LatLng = new LatLng(0,0);
                            } else {
                                dropoff3LatLng = new LatLng(objPx.getDouble(Constants.DROPOFF_POINT_3_LATITUDE), objPx.getDouble(Constants.DROPOFF_POINT_3_LONGITUDE));
                            }

                            boolean hasERP = objPx.getBoolean(Constants.HAS_ERP);
                            boolean includesERP = objPx.getBoolean(Constants.INCLUDES_ERP);
                            String costPerMonth = objPx.getString(Constants.CONTRACT_COST);

                            String busSize = objPx.getString(Constants.CONTRACT_BUS_SIZE);
                            String additionalInfo = objPx.getString(Constants.ADDITIONAL_INFO);
                            boolean isOwnContract = objPx.getBoolean(Constants.IS_OWN_CONTRACT);

                            Contract contract = new Contract(id, pickup1Name, pickup2Name, pickup3Name, pickup1LatLng, pickup2LatLng, pickup3LatLng,
                                    dropoff1Name, dropoff2Name, dropoff3Name, dropoff1LatLng, dropoff2LatLng, dropoff3LatLng, hasERP, startDate, endDate,
                                    pickupTime, contactNo, costPerMonth, includesERP, busSize, additionalInfo, isOwnContract);

                            contracts.add(contract);
                        }
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "Error processing Json data = " + e.getMessage());
                    }
                }
            }
            return false;
        }

        @Override
        public void performSuccessfulOperation() {
            Log.d(LOG_TAG, "Successfully retrieved contract list!");
        }

        @Override
        public void onFailedAttempt() {
            int statusCode = prefs.getInt(Preferences.STATUSCODE, 0);
            if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                resetApp();
            }
        }
    }

    private class DeleteContractTask extends WebServiceTask {
        DeleteContractTask(String id) {
            super(ViewAllContracts.this);
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
            obj = WebServiceUtils.requestJSONObject(Constants.DELETE_CONTRACT_URL, WebServiceUtils.METHOD.POST, authenticationToken, null, context, contentValues);
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
            builder.setMessage(displayMessage)
                    .setTitle("Message")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (isSuccessful) {
                                Intent intent = new Intent(getApplicationContext(), ViewAllContracts.class);
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
