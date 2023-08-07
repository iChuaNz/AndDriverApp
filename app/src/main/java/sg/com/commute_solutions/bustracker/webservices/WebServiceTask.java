package sg.com.commute_solutions.bustracker.webservices;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import sg.com.commute_solutions.bustracker.common.Constants;
import sg.com.commute_solutions.bustracker.R;

/**
 * Created by Kyle on 27/9/16.
 */
public abstract class WebServiceTask extends AsyncTask<Void, Void, Boolean> {

    private boolean isSuccessfullCall = false;

    private static final String TAG = WebServiceTask.class.getName();

    public abstract void showProgress();

    public abstract boolean performRequest();

    public abstract void performSuccessfulOperation();

    public abstract void onFailedAttempt();

    public abstract void hideProgress();

//    private String mMessage;
    private Context mContext;

    public WebServiceTask(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    protected void onPreExecute() {
        showProgress();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        if(!WebServiceUtils.hasInternetConnection(mContext)) {
//            mMessage = Constants.CONNECTION_MESSAGE;
            return false;
        }
        return isSuccessfullCall;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        hideProgress();
        if(success) {
            performSuccessfulOperation();
        } else {
            onFailedAttempt();
        }
//        if(mMessage != null && !mMessage.isEmpty()) {
//            Toast.makeText(mContext, mMessage, Toast.LENGTH_LONG).show();
//        }
    }

    @Override
    protected void onCancelled(Boolean aBoolean) {
        hideProgress();
    }

    public boolean hasError(JSONObject obj) {
        if(obj != null) {
            Log.d(TAG, "Response: " + obj.toString());
//          mMessage = obj.optString(Constants.MESSAGE);
            isSuccessfullCall = true;
            return false;
        } else {
//          mMessage = mContext.getString(R.string.error_url_not_found);
            return true;
        }

    }
}
