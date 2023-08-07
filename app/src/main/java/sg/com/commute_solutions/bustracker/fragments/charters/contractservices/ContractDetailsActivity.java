package sg.com.commute_solutions.bustracker.fragments.charters.contractservices;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.blackcat.currencyedittext.CurrencyEditText;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import sg.com.commute_solutions.bustracker.R;
import sg.com.commute_solutions.bustracker.common.Constants;
import sg.com.commute_solutions.bustracker.common.Preferences;
import sg.com.commute_solutions.bustracker.data.Contract;
import sg.com.commute_solutions.bustracker.fragments.LoginActivity;
import sg.com.commute_solutions.bustracker.fragments.charters.NewCharterActivity;
import sg.com.commute_solutions.bustracker.util.LineEditText;
import sg.com.commute_solutions.bustracker.util.StringUtil;
import sg.com.commute_solutions.bustracker.webservices.WebServiceTask;
import sg.com.commute_solutions.bustracker.webservices.WebServiceUtils;

/**
 * Created by Kyle on 10/1/18.
 */

public class ContractDetailsActivity extends AppCompatActivity {
    private static final String LOG_TAG = ContractDetailsActivity.class.getSimpleName();

    private Context context;
    private SharedPreferences prefs;
    private String authToken;
    private final ContentValues authenticationToken = new ContentValues();

    private FloatingActionMenu fabContractMenu;
    private FloatingActionButton fabToCharter;
    private FloatingActionButton fabViewContracts;

    private Contract contract;

    private TextView etStartDate, etEndDate, etTime, filler;
    private EditText etContactNo;
    private CurrencyEditText etCost;
    private LineEditText etAdditionalInfo;
    private CheckBox cbERP;
    private Spinner busSizeSpinner;
    private Button  btnSubmit;

    private DatePickerDialog datePickerDialog;

    private boolean hasEnteredStartDate, hasEnteredEndDate, hasEnteredPickupTime;

    private SubmitNewContractJob mSubmitNewContractJob = null;
    private AlertDialog loadingScreen;
    private JSONObject obj, jsonObject;
    private ContentValues contentValues;
    boolean isSuccessful = false;
    private String displayMessage;

    static final int STARTDATE_DIALOG_ID = 0;
    static final int ENDDATE_DIALOG_ID = 1;
    static final int PICKUPTIME_DIALOG_ID = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contract_details);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        context = this;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

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

        fabContractMenu = (FloatingActionMenu) findViewById(R.id.fab_contractMenu3);
        fabToCharter = (FloatingActionButton) findViewById(R.id.fab_toChartering3);
        fabViewContracts = (FloatingActionButton) findViewById(R.id.fab_viewContracts3);

        fabContractMenu.setIconAnimated(false);
        fabContractMenu.setMenuButtonColorNormal(Color.parseColor("#F68B1F"));
        fabToCharter.setOnClickListener(clickListener);
        fabToCharter.setColorNormal(Color.parseColor("#F68B1F"));
        fabViewContracts.setOnClickListener(clickListener);
        fabViewContracts.setColorNormal(Color.parseColor("#F68B1F"));

        populateData();

        etStartDate = (TextView) findViewById(R.id.et_contractStartDate);
        etEndDate = (TextView) findViewById(R.id.et_contractEndDate);
        etTime = (TextView) findViewById(R.id.et_contractPickupTime);
        filler = (TextView) findViewById(R.id.filler);
        etContactNo = (EditText) findViewById(R.id.et_contractContactNo);
        etCost = (CurrencyEditText) findViewById(R.id.et_contractCostPerMonth);
        busSizeSpinner = (Spinner) findViewById(R.id.contractBusSizeSpinner);
        etAdditionalInfo = (LineEditText) findViewById(R.id.et_contractAdditionalInfo);
        cbERP = (CheckBox) findViewById(R.id.cb_includesERP);
        btnSubmit = (Button) findViewById(R.id.btn_submitContract);

        etStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(STARTDATE_DIALOG_ID);
            }
        });

        etEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(ENDDATE_DIALOG_ID);
            }
        });

        etTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(PICKUPTIME_DIALOG_ID);
            }
        });

        etCost.setLocale(Locale.US);

        boolean hasERP = contract.isHasERP();
        if (!hasERP) {
            cbERP.setVisibility(View.GONE);
            filler.setVisibility(View.GONE);
        }

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoadingScreen();
                String contactNo = StringUtil.deNull(String.valueOf(etContactNo.getText()));
                contract.setContactNo(contactNo);
                String cost = StringUtil.deNull(String.valueOf(etCost.getText()));
                double costInDouble = etCost.getRawValue() / 100;
                contract.setCostPerMonth(costInDouble);
                String busType = StringUtil.deNull(String.valueOf(busSizeSpinner.getSelectedItem()));
                if (busType.isEmpty()) {
                    busType = "11-Seater";
                }
                contract.setBusSize(busType);
                String remarks = String.valueOf(etAdditionalInfo.getText());
                contract.setAdditionalInfo(remarks);

                if (cbERP.isChecked()) {
                    contract.setIncludesERP(true);
                } else {
                    contract.setIncludesERP(false);
                }

                if (!hasEnteredStartDate) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Error")
                            .setMessage("Please fill in the Start Date.")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    hideLoadingScreen();
                                    dialog.dismiss();
                                }
                            }).show();
                } else if (!hasEnteredEndDate) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Error")
                            .setMessage("Please fill in an End Date.")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    hideLoadingScreen();
                                    dialog.dismiss();
                                }
                            }).show();
                } else if (!hasEnteredPickupTime) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Error")
                            .setMessage("Please fill in the Pick up Time.")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    hideLoadingScreen();
                                    dialog.dismiss();
                                }
                            }).show();
                } else if (contactNo.length() < 8) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Error")
                            .setMessage("Please fill in a valid phone number.")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    hideLoadingScreen();
                                    dialog.dismiss();
                                }
                            }).show();
                } else if (costInDouble < 500) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Error")
                            .setMessage("Please fill in a reasonable cost. If your contract is less than a month. Please multiply the cost per trip with the number of working days. Thank you.")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    hideLoadingScreen();
                                    dialog.dismiss();
                                }
                            }).show();
                } else {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
                        String startDate = contract.getStartDate();
                        String endDate = contract.getEndDate();
                        Date start = sdf.parse(startDate);
                        Date end = sdf.parse(endDate);

                        long difference = end.getTime() - start.getTime();
                        long secondsInMilli = 1000;
                        long minutesInMilli = secondsInMilli * 60;
                        long hoursInMilli = minutesInMilli * 60;
                        long daysInMilli = hoursInMilli * 24;

                        long elapsedDays = difference / daysInMilli;
                        double elapsedDaysInDouble = (double)elapsedDays;

                        if (start.after(end)) {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setMessage(Constants.END_DATE_BEFORE_START_DATE)
                                    .setCancelable(false)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            hideLoadingScreen();
                                            dialog.dismiss();
                                        }
                                    }).show();
                        } else if (elapsedDaysInDouble < 7.0){
                            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setMessage(Constants.CONTRACT_LESS_THAN_7_DAYS)
                                    .setCancelable(false)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            hideLoadingScreen();
                                            dialog.dismiss();
                                        }
                                    }).show();
                        } else {
                            String summary = "";
                            String pickupTime = contract.getPickupTime();
                            String pickupPointName1 = contract.getPickup1Name();
                            String pickupPointName2 = contract.getPickup2Name();
                            String pickupPointName3 = contract.getPickup3Name();
                            String dropoffPointName1 = contract.getDropoff1Name();
                            String dropoffPointName2 = contract.getDropoff2Name();
                            String dropoffPointName3 = contract.getDropoff3Name();

                                //P1 D1
                            if (pickupPointName2.isEmpty() && pickupPointName3.isEmpty() && dropoffPointName2.isEmpty() && dropoffPointName3.isEmpty()) {
                                summary = String.valueOf(new StringBuilder()
                                        .append("Date: ").append(startDate).append("➡").append(endDate).append("\n")
                                        .append("From: ").append(pickupPointName1).append("\n")
                                        .append("To: ").append(dropoffPointName1).append("\n")
                                        .append("Time: ").append(pickupTime).append("\n")
                                        .append("Contact No: ").append(contactNo).append("\n")
                                        .append("Cost: ").append(cost).append("\n")
                                        .append("Bus Required: ").append(busType).append("\n")
                                        .append("Additional Info: ").append(remarks).append("\n"));
                                //P1 P2 D1
                            } else if (!pickupPointName2.isEmpty() && pickupPointName3.isEmpty() && dropoffPointName2.isEmpty() && dropoffPointName3.isEmpty()) {
                                summary = String.valueOf(new StringBuilder()
                                        .append("Date: ").append(startDate).append("➡").append(endDate).append("\n")
                                        .append("From: ").append(pickupPointName1).append(",").append(pickupPointName2).append("\n")
                                        .append("To: ").append(dropoffPointName1).append("\n")
                                        .append("Time: ").append(pickupTime).append("\n")
                                        .append("Contact No: ").append(contactNo).append("\n")
                                        .append("Cost: ").append(cost).append("\n")
                                        .append("Bus Required: ").append(busType).append("\n")
                                        .append("Additional Info: ").append(remarks).append("\n"));
                                //P1 P2 P3 D1
                            } else if (!pickupPointName2.isEmpty() && !pickupPointName3.isEmpty() && dropoffPointName2.isEmpty() && dropoffPointName3.isEmpty()) {
                                summary = String.valueOf(new StringBuilder()
                                        .append("Date: ").append(startDate).append("➡").append(endDate).append("\n")
                                        .append("From: ").append(pickupPointName1).append(",").append(pickupPointName2).append(",").append(pickupPointName3).append("\n")
                                        .append("To: ").append(dropoffPointName1).append("\n")
                                        .append("Time: ").append(pickupTime).append("\n")
                                        .append("Contact No: ").append(contactNo).append("\n")
                                        .append("Cost: ").append(cost).append("\n")
                                        .append("Bus Required: ").append(busType).append("\n")
                                        .append("Additional Info: ").append(remarks).append("\n"));
                                //P1 D1 D2
                            } else if (pickupPointName2.isEmpty() && pickupPointName3.isEmpty() && !dropoffPointName2.isEmpty() && dropoffPointName3.isEmpty()) {
                                summary = String.valueOf(new StringBuilder()
                                        .append("Date: ").append(startDate).append("➡").append(endDate).append("\n")
                                        .append("From: ").append(pickupPointName1).append("\n")
                                        .append("To: ").append(dropoffPointName1).append(",").append(dropoffPointName2).append("\n")
                                        .append("Time: ").append(pickupTime).append("\n")
                                        .append("Contact No: ").append(contactNo).append("\n")
                                        .append("Cost: ").append(cost).append("\n")
                                        .append("Bus Required: ").append(busType).append("\n")
                                        .append("Additional Info: ").append(remarks).append("\n"));
                                //P1 D1 D2 D3
                            } else if (pickupPointName2.isEmpty() && pickupPointName3.isEmpty() && !dropoffPointName2.isEmpty() && !dropoffPointName3.isEmpty()) {
                                summary = String.valueOf(new StringBuilder()
                                        .append("Date: ").append(startDate).append("➡").append(endDate).append("\n")
                                        .append("From: ").append(pickupPointName1).append("\n")
                                        .append("To: ").append(dropoffPointName1).append(",").append(dropoffPointName2).append(",").append(dropoffPointName3).append("\n")
                                        .append("Time: ").append(pickupTime).append("\n")
                                        .append("Contact No: ").append(contactNo).append("\n")
                                        .append("Cost: ").append(cost).append("\n")
                                        .append("Bus Required: ").append(busType).append("\n")
                                        .append("Additional Info: ").append(remarks).append("\n"));
                                //P1 P2 D1 D2
                            } else if (!pickupPointName2.isEmpty() && pickupPointName3.isEmpty() && !dropoffPointName2.isEmpty() && dropoffPointName3.isEmpty()) {
                                summary = String.valueOf(new StringBuilder()
                                        .append("Date: ").append(startDate).append("➡").append(endDate).append("\n")
                                        .append("From: ").append(pickupPointName1).append(",").append(pickupPointName2).append("\n")
                                        .append("To: ").append(dropoffPointName1).append(",").append(dropoffPointName2).append("\n")
                                        .append("Time: ").append(pickupTime).append("\n")
                                        .append("Contact No: ").append(contactNo).append("\n")
                                        .append("Cost: ").append(cost).append("\n")
                                        .append("Bus Required: ").append(busType).append("\n")
                                        .append("Additional Info: ").append(remarks).append("\n"));
                                //P1 P2 D1 D2 D3
                            } else if (!pickupPointName2.isEmpty() && pickupPointName3.isEmpty() && !dropoffPointName2.isEmpty() && !dropoffPointName3.isEmpty()) {
                                summary = String.valueOf(new StringBuilder()
                                        .append("Date: ").append(startDate).append("➡").append(endDate).append("\n")
                                        .append("From: ").append(pickupPointName1).append(",").append(pickupPointName2).append("\n")
                                        .append("To: ").append(dropoffPointName1).append(",").append(dropoffPointName2).append(",").append(dropoffPointName3).append("\n")
                                        .append("Time: ").append(pickupTime).append("\n")
                                        .append("Contact No: ").append(contactNo).append("\n")
                                        .append("Cost: ").append(cost).append("\n")
                                        .append("Bus Required: ").append(busType).append("\n")
                                        .append("Additional Info: ").append(remarks).append("\n"));
                                //P1 P2 P3 D1 D2
                            } else if (!pickupPointName2.isEmpty() && !pickupPointName3.isEmpty() && !dropoffPointName2.isEmpty() && dropoffPointName3.isEmpty()) {
                                summary = String.valueOf(new StringBuilder()
                                        .append("Date: ").append(startDate).append("➡").append(endDate).append("\n")
                                        .append("From: ").append(pickupPointName1).append(",").append(pickupPointName2).append(",").append(pickupPointName3).append("\n")
                                        .append("To: ").append(dropoffPointName1).append(",").append(dropoffPointName2).append("\n")
                                        .append("Time: ").append(pickupTime).append("\n")
                                        .append("Contact No: ").append(contactNo).append("\n")
                                        .append("Cost: ").append(cost).append("\n")
                                        .append("Bus Required: ").append(busType).append("\n")
                                        .append("Additional Info: ").append(remarks).append("\n"));
                                //P1 P2 P3 D1 D2 D3
                            } else if (!pickupPointName2.isEmpty() && !pickupPointName3.isEmpty() && !dropoffPointName2.isEmpty() && !dropoffPointName3.isEmpty()) {
                                summary = String.valueOf(new StringBuilder()
                                        .append("Date: ").append(startDate).append("➡").append(endDate).append("\n")
                                        .append("From: ").append(pickupPointName1).append(",").append(pickupPointName2).append(",").append(pickupPointName3).append("\n")
                                        .append("To: ").append(dropoffPointName1).append(",").append(dropoffPointName2).append(",").append(dropoffPointName3).append("\n")
                                        .append("Time: ").append(pickupTime).append("\n")
                                        .append("Contact No: ").append(contactNo).append("\n")
                                        .append("Cost: ").append(cost).append("\n")
                                        .append("Bus Required: ").append(busType).append("\n")
                                        .append("Additional Info: ").append(remarks).append("\n"));
                            }

                            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Please check the information you have entered")
                                    .setMessage(summary)
                                    .setCancelable(false)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            mSubmitNewContractJob = new SubmitNewContractJob(contract);
                                            mSubmitNewContractJob.execute((Void) null);
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
                    } catch (ParseException e) {
                        hideLoadingScreen();
                        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setMessage("An error has occurred.")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    }
                                }).show();
                    }
                }
            }
        });
    }

    private void populateData() {
        Intent intent = getIntent();
        contract = new Contract(intent.getStringExtra(Constants.PICKUP_POINT_1_NAME), intent.getStringExtra(Constants.PICKUP_POINT_2_NAME), intent.getStringExtra(Constants.PICKUP_POINT_3_NAME),
                new LatLng(intent.getDoubleExtra(Constants.PICKUP_POINT_1_LATITUDE, 0.0),intent.getDoubleExtra(Constants.PICKUP_POINT_1_LONGITUDE, 0.0)),
                new LatLng(intent.getDoubleExtra(Constants.PICKUP_POINT_2_LATITUDE, 0.0),intent.getDoubleExtra(Constants.PICKUP_POINT_2_LONGITUDE, 0.0)),
                new LatLng(intent.getDoubleExtra(Constants.PICKUP_POINT_3_LATITUDE, 0.0),intent.getDoubleExtra(Constants.PICKUP_POINT_3_LONGITUDE, 0.0)),
                intent.getStringExtra(Constants.DROPOFF_POINT_1_NAME), intent.getStringExtra(Constants.DROPOFF_POINT_2_NAME), intent.getStringExtra(Constants.DROPOFF_POINT_3_NAME),
                new LatLng(intent.getDoubleExtra(Constants.DROPOFF_POINT_1_LATITUDE, 0.0),intent.getDoubleExtra(Constants.DROPOFF_POINT_1_LONGITUDE, 0.0)),
                new LatLng(intent.getDoubleExtra(Constants.DROPOFF_POINT_2_LATITUDE, 0.0),intent.getDoubleExtra(Constants.DROPOFF_POINT_2_LONGITUDE, 0.0)),
                new LatLng(intent.getDoubleExtra(Constants.DROPOFF_POINT_3_LATITUDE, 0.0),intent.getDoubleExtra(Constants.DROPOFF_POINT_3_LONGITUDE, 0.0)),
                intent.getBooleanExtra(Constants.HAS_ERP, false));

        hasEnteredStartDate = false;
        hasEnteredEndDate = false;
        hasEnteredPickupTime = false;
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        Intent intent;
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.fab_toChartering3:
                    intent = new Intent(getApplicationContext(), NewCharterActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case R.id.fab_viewContracts3:
                    intent = new Intent(getApplicationContext(), ViewAllContracts.class);
                    startActivity(intent);
                    finish();
                    break;
            }
        }
    };

    @Override
    protected Dialog onCreateDialog(int id) {
        final Calendar c = Calendar.getInstance();
        int mYear =  c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        int mHrs = c.get(Calendar.HOUR_OF_DAY);
        int mMins = c.get(Calendar.MINUTE);

        switch (id) {
            case STARTDATE_DIALOG_ID:
                datePickerDialog = new DatePickerDialog(this, mStartDateSetListener, mYear, mMonth, mDay);
                datePickerDialog.getDatePicker().setMinDate(c.getTimeInMillis());
                return datePickerDialog;
            case ENDDATE_DIALOG_ID:
                datePickerDialog = new DatePickerDialog(this,mEndDateSetListener, mYear, mMonth, mDay);
                datePickerDialog.getDatePicker().setMinDate(c.getTimeInMillis());
                return datePickerDialog;
            case PICKUPTIME_DIALOG_ID:
                return new TimePickerDialog(this, mPickupTimeSetListener, mHrs, mMins, true);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener mStartDateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            String startDate = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year;
            try {
                Date dateInString = sdf.parse(startDate);
                sdf = new SimpleDateFormat("dd-MMM-yyyy");
                startDate = sdf.format(dateInString);
                contract.setStartDate(startDate);
                hasEnteredStartDate = true;
            } catch (ParseException e) {
                startDate = "Error";
            } finally {
                etStartDate.setText(startDate);
                etStartDate.setTextColor(Color.BLACK);
            }
        }
    };

    private DatePickerDialog.OnDateSetListener mEndDateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            String endDate = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year;
            try {
                Date dateInString = sdf.parse(endDate);
                sdf = new SimpleDateFormat("dd-MMM-yyyy");
                endDate = sdf.format(dateInString);
                contract.setEndDate(endDate);
                hasEnteredEndDate = true;
            } catch (ParseException e) {
                endDate = "Error";
            } finally {
                etEndDate.setText(endDate);
                etEndDate.setTextColor(Color.BLACK);
            }
        }
    };

    private TimePickerDialog.OnTimeSetListener mPickupTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            String pickupTime;
            if (minute < 10) {
                pickupTime = hourOfDay + ":0" + minute;
            } else {
                pickupTime = hourOfDay + ":" + minute;
            }

            if (hourOfDay < 12) {
                pickupTime = pickupTime + " AM";
            } else {
                pickupTime = pickupTime + " PM";

            }
            contract.setPickupTime(pickupTime);
            etTime.setText(pickupTime);
            etTime.setTextColor(Color.BLACK);
            hasEnteredPickupTime = true;
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
                        ContractDetailsActivity.this.finishAffinity();
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
    private class SubmitNewContractJob extends WebServiceTask {
        SubmitNewContractJob(Contract contract){
            super(ContractDetailsActivity.this);
            contentValues = new ContentValues();
            contentValues.put(Constants.PICKUP_POINT_1_NAME, contract.getPickup1Name());
            contentValues.put(Constants.PICKUP_POINT_2_NAME, contract.getPickup2Name());
            contentValues.put(Constants.PICKUP_POINT_3_NAME, contract.getPickup3Name());

            contentValues.put(Constants.DROPOFF_POINT_1_NAME, contract.getDropoff1Name());
            contentValues.put(Constants.DROPOFF_POINT_2_NAME, contract.getDropoff2Name());
            contentValues.put(Constants.DROPOFF_POINT_3_NAME, contract.getDropoff3Name());

            contentValues.put(Constants.PICKUP_POINT_1_LATITUDE, contract.getPickup1LatLng().latitude);
            contentValues.put(Constants.PICKUP_POINT_2_LATITUDE, contract.getPickup2LatLng().latitude);
            contentValues.put(Constants.PICKUP_POINT_3_LATITUDE, contract.getPickup3LatLng().latitude);

            contentValues.put(Constants.PICKUP_POINT_1_LONGITUDE, contract.getPickup1LatLng().longitude);
            contentValues.put(Constants.PICKUP_POINT_2_LONGITUDE, contract.getPickup2LatLng().longitude);
            contentValues.put(Constants.PICKUP_POINT_3_LONGITUDE, contract.getPickup3LatLng().longitude);

            contentValues.put(Constants.DROPOFF_POINT_1_LATITUDE, contract.getDropoff1LatLng().latitude);
            contentValues.put(Constants.DROPOFF_POINT_2_LATITUDE, contract.getDropoff2LatLng().latitude);
            contentValues.put(Constants.DROPOFF_POINT_3_LATITUDE, contract.getDropoff3LatLng().latitude);

            contentValues.put(Constants.DROPOFF_POINT_1_LONGITUDE, contract.getDropoff1LatLng().longitude);
            contentValues.put(Constants.DROPOFF_POINT_2_LONGITUDE, contract.getDropoff2LatLng().longitude);
            contentValues.put(Constants.DROPOFF_POINT_3_LONGITUDE, contract.getDropoff3LatLng().longitude);

            contentValues.put(Constants.HAS_ERP, contract.isHasERP());
            contentValues.put(Constants.INCLUDES_ERP, contract.getIncludesERP());
            contentValues.put(Constants.CONTRACT_COST, contract.getCostPerMonth());

            contentValues.put(Constants.CONTRACT_START_DATE, contract.getStartDate());
            contentValues.put(Constants.CONTRACT_END_DATE, contract.getEndDate());
            contentValues.put(Constants.CHARTER_PICKUPTIME, contract.getPickupTime());

            contentValues.put(Constants.CONTRACT_CONTACT_NO, contract.getContactNo());
            contentValues.put(Constants.CONTRACT_BUS_SIZE, contract.getBusSize());
            contentValues.put(Constants.ADDITIONAL_INFO, contract.getAdditionalInfo());

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
            obj = WebServiceUtils.requestJSONObject(Constants.CREATE_CONTRACT_URL, WebServiceUtils.METHOD.POST, authenticationToken, null, context, contentValues);
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
                                Intent intent = new Intent(getApplicationContext(), PickUpPointActivity.class);
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
