package sg.com.commute_solutions.bustracker.webservices;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import sg.com.commute_solutions.bustracker.common.Constants;
import sg.com.commute_solutions.bustracker.common.Preferences;

/**
 * Created by Kyle on 27/9/16.
 */
public class WebServiceUtils {
    private static final String TAG = WebServiceUtils.class.getName();

    public enum METHOD {
        POST, GET, DELETE
    }

    public static JSONObject requestJSONObject(String serviceUrl, METHOD method,
                                               ContentValues headerValues, Context context) {
        return requestJSONObject(serviceUrl, method, headerValues, null, context, null);
    }

    public static JSONObject requestJSONObject(String serviceUrl, METHOD method,
                                               ContentValues headerValues, ContentValues urlValues,
                                               ArrayList<ContentValues> bodyValues, boolean hasAuthorization) {
        HttpURLConnection urlConnection = null;
        try {
            URL urlToRequest = new URL(serviceUrl);
            urlConnection = (HttpURLConnection) urlToRequest.openConnection();
            urlConnection.setConnectTimeout(Constants.CONNECTION_TIMEOUT);
            urlConnection.setReadTimeout(Constants.READ_TIMEOUT);
            Log.d(TAG, method.toString());
            urlConnection.setRequestMethod(method.toString());
            urlConnection.setDoInput(true);

            //if dooutput is true it auto change to get
            if(method != METHOD.GET){
                urlConnection.setDoOutput(true);
            }

            //Set token
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setRequestProperty("token", headerValues.getAsString("token"));

            if(bodyValues != null) {
                JSONObject jsonObject;
                JSONObject query = new JSONObject();
                JSONArray jsonArray = new JSONArray();

                for (int i = 0; i < bodyValues.size(); i++) {
                    jsonObject = new JSONObject();
                    for (String key : bodyValues.get(i).keySet()) {
                        jsonObject.put(key, bodyValues.get(i).getAsString(key));
                    }
                    jsonArray.put(jsonObject);
                }

                query.put("Attendances", jsonArray);
                String str = query.toString();
                OutputStreamWriter osw = new OutputStreamWriter(urlConnection.getOutputStream());
                osw.write(str);
                osw.flush();
                osw.close();
            }

            int statusCode = urlConnection.getResponseCode();
            if(statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                Log.d(TAG, "Unauthorized Access!");
            } else if(statusCode != HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "URL Response Error");
            }

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            return new JSONObject(convertInputStreamToString(in));

        } catch(MalformedURLException e) {
            Log.d(TAG, "Mailformed " + e.getMessage());
        } catch(SocketTimeoutException e) {
            Log.d(TAG, "Socket Timeout " +e.getMessage());
        } catch(IOException e) {
            Log.d(TAG, "IOException " + e.getMessage());
        } catch(JSONException e) {
            Log.d(TAG, "JSONException " + e.getMessage());
        } finally {
            if(urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return null;
    }

    public static JSONObject requestJSONObject(String serviceUrl, METHOD method,
                                               ContentValues headerValues,
                                               ContentValues bodyValues) {

        HttpURLConnection urlConnection = null;
        try {
            URL urlToRequest = new URL(serviceUrl);
            urlConnection = (HttpURLConnection) urlToRequest.openConnection();
            urlConnection.setConnectTimeout(Constants.CONNECTION_TIMEOUT);
            urlConnection.setReadTimeout(Constants.READ_TIMEOUT);
            Log.d(TAG, method.toString());
            urlConnection.setRequestMethod(method.toString());
            urlConnection.setDoInput(true);

            //if dooutput is true it auto change to get
            if(method != METHOD.GET){
                urlConnection.setDoOutput(true);
            }

            if(headerValues != null) {
                //Set token
                if (headerValues.containsKey("token")) {
                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    urlConnection.setRequestProperty("Accept", "application/json");
                    urlConnection.setRequestProperty("token", headerValues.getAsString("token"));
                    //For login
                } else {
                    Uri.Builder builder = new Uri.Builder();
                    for (String key : headerValues.keySet()) {
                        builder.appendQueryParameter(key, headerValues.getAsString(key));
                    }
                    String query = builder.build().getEncodedQuery();
                    OutputStream os = urlConnection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    writer.write(query);
                    writer.flush();
                    writer.close();
                    os.close();
                }
            } else {
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
            }

            if(bodyValues != null) {
                if (bodyValues.containsKey("deviceToken")) {
                    JSONObject jsonObject = new JSONObject();

                    for (String key : bodyValues.keySet()) {
                        jsonObject.put(key, bodyValues.getAsString(key));
                    }

                    String str = jsonObject.toString();
                    OutputStreamWriter osw = new OutputStreamWriter(urlConnection.getOutputStream());
                    osw.write(str);
                    osw.flush();
                    osw.close();
                } else {
                    JSONObject jsonObject = new JSONObject();
                    JSONObject query = new JSONObject();
                    JSONArray jsonArray = new JSONArray();

                    for (String key : bodyValues.keySet()) {
                        jsonObject.put(key, bodyValues.getAsString(key));
                    }
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                    String date = df.format(Calendar.getInstance().getTime());
                    Log.d("JSON::", "Sending string for location values");
                    jsonArray.put(jsonObject);
                    query.put("locationList", jsonArray);
                    query.put("requestTime", date);

                    String str = query.toString();
                    OutputStreamWriter osw = new OutputStreamWriter(urlConnection.getOutputStream());
                    osw.write(str);
                    osw.flush();
                    osw.close();
                }
            }

            int statusCode = urlConnection.getResponseCode();
            if(statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                Log.d(TAG, "Unauthorized Access!");
            } else if(statusCode != HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "URL Response Error");
            }

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            return new JSONObject(convertInputStreamToString(in));

        } catch(MalformedURLException e) {
            Log.d(TAG, "Mailformed " + e.getMessage());
        } catch(SocketTimeoutException e) {
            Log.d(TAG, "Socket Timeout " +e.getMessage());
        } catch(IOException e) {
            Log.d(TAG, "IOException " + e.getMessage());
        } catch(JSONException e) {
            Log.d(TAG, "JSONException " + e.getMessage());
        } finally {
            if(urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return null;
    }


    public static JSONObject requestJSONObject(String serviceUrl, METHOD method,
                                               ContentValues headerValues, ContentValues urlValues, Context context,
                                               ContentValues bodyValues) {

        HttpURLConnection urlConnection = null;
//        boolean toProceed = false;
//        if (requireSecondCheck) {
//            SharedPreferences prefs = context.getSharedPreferences(Constants.SHARE_PREFERENCE_PACKAGE, Context.MODE_PRIVATE);
//            toProceed = prefs.getBoolean(Preferences.APICALLED, false);
//        } else {
//            toProceed = true;
//        }
//
//        if (toProceed) {
        try {
//            if(urlValues != null) {
//                serviceUrl = addParametersToUrl(serviceUrl, urlValues);
//            }

            URL urlToRequest = new URL(serviceUrl);
            urlConnection = (HttpURLConnection) urlToRequest.openConnection();
            urlConnection.setConnectTimeout(Constants.CONNECTION_TIMEOUT);
            urlConnection.setReadTimeout(Constants.READ_TIMEOUT);
            Log.d(TAG, method.toString());
            urlConnection.setRequestMethod(method.toString());
            urlConnection.setDoInput(true);

            //if dooutput is true it auto change to get
            if (method != METHOD.GET) {
                urlConnection.setDoOutput(true);
            }

            if (headerValues != null) {
                //Set token
                if (headerValues.containsKey("token")) {
                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    urlConnection.setRequestProperty("Accept", "application/json");
                    urlConnection.setRequestProperty("token", headerValues.getAsString("token"));
                    //For login
                } else {
                    Uri.Builder builder = new Uri.Builder();
                    for (String key : headerValues.keySet()) {
                        builder.appendQueryParameter(key, headerValues.getAsString(key));
                    }
                    String query = builder.build().getEncodedQuery();
                    OutputStream os = urlConnection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    writer.write(query);
                    writer.flush();
                    writer.close();
                    os.close();
                }
            }

            if (bodyValues != null) {
                JSONObject jsonObject = new JSONObject();
                JSONObject query = new JSONObject();

                for (String key : bodyValues.keySet()) {
                    jsonObject.put(key, bodyValues.getAsString(key));
                }
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                String date = df.format(Calendar.getInstance().getTime());
                Log.d("JSON::", "Sending string for location values");
                query.put("data", jsonObject);
                query.put("requestTime", date);

                String str = query.toString();
                OutputStreamWriter osw = new OutputStreamWriter(urlConnection.getOutputStream());
                osw.write(str);
                osw.flush();
                osw.close();
            }

            int statusCode = urlConnection.getResponseCode();
            if (context != null) {
                SharedPreferences prefs = context.getSharedPreferences(Constants.SHARE_PREFERENCE_PACKAGE, Context.MODE_PRIVATE);
                final SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(Preferences.STATUSCODE, statusCode);
                editor.apply();
            }

            if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                Log.d(TAG, "Unauthorized Access!");
                return new JSONObject(statusCode + "");
            } else if (statusCode != HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "URL Response Error");
            }

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            return new JSONObject(convertInputStreamToString(in));

        } catch (MalformedURLException e) {
            Log.d(TAG, "Mailformed " + e.getMessage());
        } catch (SocketTimeoutException e) {
            Log.d(TAG, "Socket Timeout " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "IOException " + e.getMessage());
        } catch (JSONException e) {
            Log.d(TAG, "JSONException " + e.getMessage());
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
//        } else {
//            return new JSONObject();
//        }
        return null;
    }

    private static String convertInputStreamToString(InputStream inputStream) {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String responseText;
        try {
            while((responseText = bufferedReader.readLine()) != null) {
                stringBuilder.append(responseText);
            }
        } catch(IOException e) {
            Log.d(TAG, "IOException in convertInputStreamToString");
            e.printStackTrace();
        }

        return stringBuilder.toString();
    }

    public static boolean hasInternetConnection(Context context) {
        ConnectivityManager connectivityManager = ((ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager != null &&
                connectivityManager.getActiveNetworkInfo() != null &&
                connectivityManager.getActiveNetworkInfo().isConnected();
    }
}
