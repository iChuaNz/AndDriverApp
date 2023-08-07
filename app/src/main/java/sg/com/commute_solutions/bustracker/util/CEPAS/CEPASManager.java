package sg.com.commute_solutions.bustracker.util.CEPAS;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import sg.com.commute_solutions.bustracker.util.StringUtil;

/**
 * Created by Kyle on 23/9/16.
 */
public class CEPASManager extends AppCompatActivity {
    /* Default master key. */
    private static final String DEFAULT_3901_MASTER_KEY = "FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF";
    private static final String DEFAULT_1255_MASTER_KEY = "ACR1255U-J1 Auth";
    /* Get 8 bytes random number APDU. */
    private static final String DEFAULT_3901_APDU_COMMAND = "80 84 00 00 08";
    /* Read 16 bytes from the binary block 0x04 (MIFARE 1K or 4K). */
    private static final String DEFAULT_1255_APDU_COMMAND = "FF CA 00 00 00";
    /* Get Serial Number command (0x02) escape command. */
    private static final String DEFAULT_3901_ESCAPE_COMMAND = "02";
    private String LOG_TAG = CEPASManager.class.getSimpleName();

    /* Get firmware version escape command. */
    private static final String DEFAULT_1255_ESCAPE_COMMAND = "E0 00 00 18 00";
    private SharedPreferences prefs;
    private String canId;

    public final byte[] CEPAS_SELECT_FILE_COMMAND = new byte[]{
            (byte) 0x00, (byte) 0xA4, (byte) 0x00, (byte) 0x00,
            (byte) 0x02, (byte) 0x40, (byte) 0x00};

    public class DecryptCepas extends AsyncTask<String, Void, String> {
        @Override
        protected void onPostExecute(String s) {
            if(s != null)
            {
                canId = s;
            }
        }

        @Override
        protected String doInBackground(String... params) {
            byte[] result = StringUtil.hexString2Bytes(params[0].replace(" ", ""));
            if (result[result.length -  2] == (byte) 0x90) {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                output.write(result, 0, result.length - 2);

                byte status = result[result.length - 1];
                if(status == (byte) 0x00) {
                    byte[] purseBuff = output.toByteArray();
                    byte[] can = new byte[8];
                    try
                    {
                        System.arraycopy(purseBuff, 8, can, 0, can.length);
                        String can_tmp = bytesToHexString(can);
                        if (can_tmp != null && !can_tmp.isEmpty()) {
                            String mCanId = can_tmp;
                            Log.d(LOG_TAG, "**************");
                            Log.d(LOG_TAG, mCanId);
                            Log.d(LOG_TAG, "**************");
                            return  mCanId;
                        }
                    }catch (Exception e){
                        Log.d(LOG_TAG, "error: " + e.getMessage());
                        return null;
                    }
                }
            }
            return null;
        }
    }

    public String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        char[] buffer = new char[2];
        for (int i = 0; i < src.length; i++) {
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
            stringBuilder.append(buffer);
        }
        Log.d(LOG_TAG, stringBuilder.toString());
        return stringBuilder.toString();
    }

    public byte[] wrapMessage(byte command, byte p1, byte p2, byte lc, byte[] parameters) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write((byte) 0x90); // CLA
        stream.write(command);     // INS
        stream.write(p1);          // P1
        stream.write(p2);          // P2
        stream.write(lc);          // Lc
        // Write Lc and data fields
        if (parameters != null) {
            stream.write(parameters); // Data field
        }
        return stream.toByteArray();
    }

    public AsyncTask<String, Void, String> decrypt(String msg, SharedPreferences sharedPreferences) {
        prefs = sharedPreferences;
        return new DecryptCepas().execute(msg);
    }

}
