package sg.com.commute_solutions.bustracker.fragments.charters;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.android.view.CardInputWidget;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.net.HttpURLConnection;

import sg.com.commute_solutions.bustracker.BuildConfig;
import sg.com.commute_solutions.bustracker.R;
import sg.com.commute_solutions.bustracker.common.Constants;
import sg.com.commute_solutions.bustracker.common.Preferences;
import sg.com.commute_solutions.bustracker.fragments.LoginActivity;
import sg.com.commute_solutions.bustracker.fragments.MapsActivity;
import sg.com.commute_solutions.bustracker.fragments.RouteActivity;
import sg.com.commute_solutions.bustracker.util.StringUtil;
import sg.com.commute_solutions.bustracker.webservices.WebServiceTask;
import sg.com.commute_solutions.bustracker.webservices.WebServiceUtils;

/**
 * Created by Kyle on 9/5/17.
 */

public class CharterProfileActivity extends AppCompatActivity
        implements Serializable {
    private static final String LOG_TAG = CharterProfileActivity.class.getSimpleName();
    private SharedPreferences prefs;
    private Context context;

    private String authToken;
    private final ContentValues authenticationToken = new ContentValues();

    private TextView txtUsername, txteWallet, txtWithheld, txtCreditCard, txtBankAccount, txtVersionNo;
    private Button btnLogout, btnCashout, btnSubmitCreditCard, btnDeleteCreditCard, btnViewWalletHistory;
    private LinearLayout llCreditCardFunctions;
    private String walletString, withHeldAmountString, creditCardString, bankAccountString;

    private FloatingActionMenu fabCharteringMenu;
    private FloatingActionButton fabViewCharter;
    private FloatingActionButton fabNewCharter;
    private FloatingActionButton fabYourCharter;
    private FloatingActionButton fabSuccessfulCharter;
    private FloatingActionButton fabToTracker;
    private FloatingActionButton fabDispute;

    private AlertDialog loadingScreen;
    private CardInputWidget cardInputWidget;
    private Card card;

//    private final String stripePublicKey = Constants.STRIPE_DEBUG_KEY;
    private final String stripePublicKey = Constants.STRIPE_LIVE_KEY;
    private final String obfuscateCreditCardString = "**** **** **** ";
    private String stripeToken;

    private ChangeCreditCardTask mChangeCreditCardTask;
    private CashOutTask mCashOutTask;
    private FetchUserData mFetchUserData;
//    private ChangeBankAccountTask mChangeBankAccountTask;

    private JSONObject obj, jsonObject;
    private ContentValues contentValues;

    private boolean isOMO = false;
    private String displayMessage;
    private boolean canProceed = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charter_profile);
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
        showLoadingScreen();

        prefs = this.getSharedPreferences(Constants.SHARE_PREFERENCE_PACKAGE, Context.MODE_PRIVATE);
        authToken = StringUtil.deNull(prefs.getString(Preferences.AUTH_TOKEN, ""));
        authenticationToken.put(Constants.TOKEN, authToken);

        txtUsername = (TextView) findViewById(R.id.txt_charter_username);
        txteWallet = (TextView) findViewById(R.id.txt_eWallet);
        txtWithheld = (TextView) findViewById(R.id.txt_withheldAmount);
        txtCreditCard = (TextView) findViewById(R.id.txt_creditcard);
        txtBankAccount = (TextView) findViewById(R.id.txt_bankAccount);
        txtVersionNo = (TextView) findViewById(R.id.txt_charter_version_no);

        fabCharteringMenu = (FloatingActionMenu) findViewById(R.id.fab_charteringMenu4);
        fabViewCharter = (FloatingActionButton) findViewById(R.id.fab_viewCharter4);
        fabNewCharter = (FloatingActionButton) findViewById(R.id.fab_newCharter4);
        fabYourCharter = (FloatingActionButton) findViewById(R.id.fab_ownCharter4);
        fabSuccessfulCharter = (FloatingActionButton) findViewById(R.id.fab_successfulBids4);
        fabToTracker = (FloatingActionButton) findViewById(R.id.fab_toTracking4);
        fabDispute = (FloatingActionButton) findViewById(R.id.fab_dispute4);

        cardInputWidget = (CardInputWidget) findViewById(R.id.card_input_widget);
        llCreditCardFunctions = (LinearLayout) findViewById(R.id.ll_credit_card_functions);
        btnSubmitCreditCard = (Button) findViewById(R.id.btn_submit_new_credit_card);
        btnDeleteCreditCard = (Button) findViewById(R.id.btn_delete_credit_card);
        txtCreditCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardInputWidget.setVisibility(View.VISIBLE);
                llCreditCardFunctions.setVisibility(View.VISIBLE);
            }
        });

        btnSubmitCreditCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                card = cardInputWidget.getCard();
                showLoadingScreen();
                try {
                    if (!card.validateCard()) {
                        throw new Exception();
                    } else {
                        Stripe stripe = new Stripe(context, stripePublicKey);
                        stripe.createToken(card,
                                new TokenCallback() {
                                    public void onSuccess(Token token) {
                                        stripeToken = token.getId();
                                        try {
                                            mChangeCreditCardTask = new ChangeCreditCardTask(stripeToken, false);
                                            mChangeCreditCardTask.execute((Void) null);
                                        } catch (Exception e) {
                                            //do nothing
                                        }
                                    }

                                    public void onError(Exception error) {
                                        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                        builder.setMessage(Constants.CREDIT_CARD_INVALID)
                                                .setCancelable(false)
                                                .setPositiveButton("Understood", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                }).show();
                                    }
                                }
                        );
                    }
                } catch (Exception e) {
                    hideLoadingScreen();
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage(Constants.CREDIT_CARD_INVALID)
                            .setCancelable(false)
                            .setPositiveButton("Understood", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();
                }
            }
        });

        btnDeleteCreditCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mChangeCreditCardTask = new ChangeCreditCardTask("", true);
                    mChangeCreditCardTask.execute((Void) null);
                } catch (Exception e) {
                    //do nothing
                }
            }
        });

        String driverRole = prefs.getString(Preferences.ROLE, "");
        if (driverRole.equalsIgnoreCase("OMO")) {
            isOMO = true;
        }

        if (isOMO) {
            fabToTracker.setVisibility(View.VISIBLE);
        }

        btnCashout = (Button) findViewById(R.id.btn_cashout);
        btnCashout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoadingScreen();
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                final EditText txt_amount = new EditText(context);
                txt_amount.setInputType(InputType.TYPE_CLASS_NUMBER);
                txt_amount.setHint("eg. 100");

                builder.setMessage(Constants.WITHDRAW_MESSAGE)
                        .setView(txt_amount)
                        .setCancelable(false)
                        .setPositiveButton("Understood", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    String cashOutAmount = txt_amount.getText().toString();
                                    mCashOutTask = new CashOutTask(cashOutAmount);
                                    mCashOutTask.execute((Void) null);
                                } catch (Exception e) {
                                    //do nothing
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
        });

        btnViewWalletHistory = (Button) findViewById(R.id.btn_viewWalletHistory);
        btnViewWalletHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TransactionHistoryActivity.class);
                startActivity(intent);
            }
        });

        fabCharteringMenu.setIconAnimated(false);
        fabCharteringMenu.setMenuButtonColorNormal(Color.parseColor("#F68B1F"));
        fabViewCharter.setOnClickListener(clickListener);
        fabViewCharter.setColorNormal(Color.parseColor("#F68B1F"));
        fabNewCharter.setOnClickListener(clickListener);
        fabNewCharter.setColorNormal(Color.parseColor("#F68B1F"));
        fabYourCharter.setOnClickListener(clickListener);
        fabYourCharter.setColorNormal(Color.parseColor("#F68B1F"));
        fabSuccessfulCharter.setOnClickListener(clickListener);
        fabSuccessfulCharter.setColorNormal(Color.parseColor("#F68B1F"));
        fabToTracker.setOnClickListener(clickListener);
        fabToTracker.setColorNormal(Color.parseColor("#F68B1F"));
        fabDispute.setOnClickListener(clickListener);
        fabDispute.setColorNormal(Color.parseColor("#F68B1F"));

        String loggedInUsername = prefs.getString(Preferences.LOGGED_IN_USERNAME, "");
        txtUsername.setText(loggedInUsername.toUpperCase());
        txtVersionNo.setText(BuildConfig.VERSION_NAME);
//        txtVersionNo.setText("Staging");

        try {
            mFetchUserData = new FetchUserData();
            mFetchUserData.execute((Void) null);
        } catch (Exception e) {
            //do nothing
        } finally {
            hideLoadingScreen();
        }

//        txtBankAccount.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                final EditText txt_bankAccount = new EditText(context);
//                txt_bankAccount.setInputType(InputType.TYPE_CLASS_NUMBER);
//                txt_bankAccount.setHint("1234567890 (Please omit dashes)");
//
//                builder.setMessage(Constants.BANKACCOUNT_CHANGE)
//                        .setView(txt_bankAccount)
//                        .setCancelable(false)
//                        .setPositiveButton("Yes - Proceed", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                try {
//                                    String bankAccountNo = txt_bankAccount.getText().toString();
//                                    mChangeBankAccountTask = new ChangeBankAccountTask(bankAccountNo);
//                                    mChangeBankAccountTask.execute((Void) null);
//                                } catch (Exception e) {
//                                    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                                    builder.setMessage(Constants.CANNOT_LOGIN2)
//                                            .setCancelable(false)
//                                            .setPositiveButton("Understood", new DialogInterface.OnClickListener() {
//                                                @Override
//                                                public void onClick(DialogInterface dialog, int which) {
//                                                    dialog.dismiss();
//                                                }
//                                            }).show();
//                                }
//                            }
//                        })
//                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                            }
//                        })
//                        .show();
//            }
//        });

        btnLogout = (Button) findViewById(R.id.btn_charter_logout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(Constants.LOGOUT_CONFIRMATION)
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                prefs.edit().clear().commit();
                                CharterProfileActivity.this.finishAffinity();
                                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
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
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        Intent intent;
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.fab_newCharter4:
                    intent = new Intent(getApplicationContext(), NewCharterActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case R.id.fab_viewCharter4:
                    intent = new Intent(getApplicationContext(), AvailableCharterActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case R.id.fab_successfulBids4:
                    intent = new Intent(getApplicationContext(), YourCharterActivity.class);
                    intent.putExtra("intention","acceptedCharters");
                    startActivity(intent);
                    finish();
                    break;
                case R.id.fab_ownCharter4:
                    intent = new Intent(getApplicationContext(), YourCharterActivity.class);
                    intent.putExtra("intention","ownCharter");
                    startActivity(intent);
                    finish();
                    break;
                case R.id.fab_toTracking4:
                    intent = new Intent(getApplicationContext(), RouteActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case R.id.fab_dispute4:
                    intent = new Intent(getApplicationContext(), DisputedCharterActivity.class);
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
                        CharterProfileActivity.this.finishAffinity();
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
    private class FetchUserData extends WebServiceTask {
        FetchUserData(){
            super(CharterProfileActivity.this);
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
            String URL = Constants.PROFILE_URL;
            obj = WebServiceUtils.requestJSONObject(URL, WebServiceUtils.METHOD.GET, authenticationToken, context);
            if(!hasError(obj)) {
                Log.d(LOG_TAG, obj.toString());
                jsonObject = obj.optJSONObject(Constants.DATA);

                if (jsonObject == null) {
                    return false;
                } else {
                    try {
                        walletString = jsonObject.getString(Constants.WALLET);
                        withHeldAmountString = jsonObject.getString(Constants.WITHHELD_WALLET);
                        bankAccountString = StringUtil.deNull(jsonObject.getString(Constants.BANK_ACCOUNT));
                        creditCardString = StringUtil.deNull(jsonObject.getString(Constants.CREDIT_CARD));
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
            txteWallet.setText(new StringBuilder("$").append(walletString).toString());
            txtWithheld.setText(new StringBuilder("$").append(withHeldAmountString).toString());
            if (creditCardString.equalsIgnoreCase("") || creditCardString.isEmpty()) {
                txtCreditCard.setText("No Credit card has been registered.");
            } else {
                txtCreditCard.setText(new StringBuilder(obfuscateCreditCardString).append(creditCardString).toString());
            }

            if (StringUtil.deNull(bankAccountString).equalsIgnoreCase("") || StringUtil.deNull(bankAccountString).isEmpty()) {
                txtBankAccount.setText("No Bank Account has been registered.");
            } else {
                txtBankAccount.setText(bankAccountString);
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

    private class CashOutTask extends WebServiceTask {
        CashOutTask(String amount){
            super(CharterProfileActivity.this);
            contentValues = new ContentValues();
            contentValues.put(Constants.CHARTER_COST, amount);

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
            obj = WebServiceUtils.requestJSONObject(Constants.CASHOUT_URL, WebServiceUtils.METHOD.POST, authenticationToken, null, context, contentValues);
            if(!hasError(obj)) {
                Log.d(LOG_TAG, obj.toString());
                jsonObject = obj.optJSONObject(Constants.DATA);

                if (canProceed) {
                    canProceed = false;
                    if (jsonObject != null) {
                        try {
                            displayMessage = jsonObject.getString(Constants.BUS_CHARTER_MESSAGE);
                        } catch (JSONException e) {
                            Log.e(LOG_TAG, "Error processing Json data = " + e.getMessage());
                        }
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
                    .setCancelable(false)
                    .setPositiveButton("Understood", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Intent intent = new Intent(getApplicationContext(), CharterProfileActivity.class);
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

    private class ChangeCreditCardTask extends WebServiceTask {
        ChangeCreditCardTask(String stripeToken, boolean toDeleteCreditCard){
            super(CharterProfileActivity.this);
            contentValues = new ContentValues();
            contentValues.put(Constants.STRIPE_TOKEN, stripeToken);
            contentValues.put(Constants.TO_DELETE_CREDIT_CARD, toDeleteCreditCard);

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
            obj = WebServiceUtils.requestJSONObject(Constants.CREDIT_CARD_URL, WebServiceUtils.METHOD.POST, authenticationToken, null, context, contentValues);
            if(!hasError(obj)) {
                Log.d(LOG_TAG, obj.toString());
                jsonObject = obj.optJSONObject(Constants.DATA);

                if (canProceed) {
                    canProceed = false;
                    if (jsonObject != null) {
                        try {
                            displayMessage = jsonObject.getString(Constants.BUS_CHARTER_MESSAGE);
                        } catch (JSONException e) {
                            Log.e(LOG_TAG, "Error processing Json data = " + e.getMessage());
                        }
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
                    .setCancelable(false)
                    .setPositiveButton("Understood", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Intent intent = new Intent(getApplicationContext(), CharterProfileActivity.class);
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


//    private class ChangeBankAccountTask extends WebServiceTask {
//        ChangeBankAccountTask(String bankAccountNumber){
//            super(CharterProfileActivity.this);
//            contentValues = new ContentValues();
//            contentValues.put(Constants.BANK_ACCOUNT, bankAccountNumber);
//
//            performRequest();
//
//        }
//
//        @Override
//        public void showProgress() {
//
//        }
//
//        @Override
//        public void hideProgress() {
//
//        }
//
//        @Override
//        public boolean performRequest() {
//            obj = WebServiceUtils.requestJSONObject(Constants.BANKACCOUNT_URL, WebServiceUtils.METHOD.POST, authenticationToken, null, null, contentValues);
//            if(!hasError(obj)) {
//                Log.d(LOG_TAG, obj.toString());
//                jsonObject = obj.optJSONObject(Constants.DATA);
//
//                if (jsonObject != null) {
//                    try {
//                        displayMessage = jsonObject.getString(Constants.BUS_CHARTER_MESSAGE);
//                    } catch (JSONException e) {
//                        Log.e(LOG_TAG, "Error processing Json data = " + e.getMessage());
//                    }
//                }
//                return true;
//            }
//            return false;
//        }
//
//        @Override
//        public void performSuccessfulOperation() {
//            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
//            builder.setMessage(displayMessage)
//                    .setCancelable(false)
//                    .setPositiveButton("Understood", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.dismiss();
//                            Intent intent = new Intent(getApplicationContext(), CharterProfileActivity.class);
//                            startActivity(intent);
//                            finish();
//                        }
//                    }).show();
//        }
//
//        @Override
//        public void onFailedAttempt() {
//            resetApp();
//        }
//    }
}
