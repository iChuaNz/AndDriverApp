package sg.com.commute_solutions.bustracker.util.CEPAS;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by Kyle on 8/9/16.
 */
//enum CepasCardStatus { IDLE, PROCESSING, NOT_INIT, FAILED_OR_EMPTY, OK }
public class CEPASUtil {

    private static String LOG_TAG = CEPASUtil.class.getSimpleName();
//    private CepasCardStatus mCepasCardStatus;
    private String mCanId = "";
    // list of NFC technologies detected:
    private final String[][] techList = new String[][] {
            new String[] {
                    NfcA.class.getName(),
                    NfcB.class.getName(),
                    NfcF.class.getName(),
                    NfcV.class.getName(),
                    IsoDep.class.getName(),
                    MifareClassic.class.getName(),
                    MifareUltralight.class.getName(), Ndef.class.getName()
            }
    };
    final public static byte[] CEPAS_SELECT_FILE_COMMAND = new byte[]{
            (byte) 0x00, (byte) 0xA4, (byte) 0x00, (byte) 0x00,
            (byte) 0x02, (byte) 0x40, (byte) 0x00};

    // Ezlink card (Iso 14443 Type B, Tech: IsoDep & NfcB)
    private String processCepasCanId(Tag tag)
    {
        try {
            IsoDep isoDep = IsoDep.get(tag);
            isoDep.connect();

            try {
                byte[] results = isoDep.transceive(CEPAS_SELECT_FILE_COMMAND);

                Log.d(LOG_TAG, results.toString());

                ByteArrayOutputStream output = new ByteArrayOutputStream();

                byte[] result = isoDep.transceive(wrapMessage((byte) 0x32, (byte) (3), (byte) 0, (byte) 0, new byte[]{(byte) 0}));
                Log.d(LOG_TAG, result.toString());

                if (result[result.length -  2] == (byte) 0x90) {
                    output.write(result, 0, result.length - 2);

                    byte status = result[result.length - 1];

                    //Operation Ok
                    if(status == (byte) 0x00) {
                        byte[] purseBuff = output.toByteArray();

                        // Extract the CAN number from the Purse
                        byte[] can = new byte[8];
                        Log.d(LOG_TAG, String.valueOf(purseBuff.length) + " *****");
                        System.arraycopy(purseBuff, 8, can, 0, can.length);

                        String can_tmp = bytesToHexString(can);
                        if (can_tmp != null && !can_tmp.isEmpty()) {
                            mCanId = can_tmp;
                        }
                    }
                }
            } catch (Exception e) {
                Log.d(LOG_TAG, "ERROR: " + e.getMessage());
                return null;
            }
            isoDep.close();
            return mCanId;
        } catch (Exception e) {
            Log.d(LOG_TAG, "ERROR: " + e.getMessage());
            return null;
        }
    }

    public String getmCanId() {
        return mCanId;
    }

    public static String bytesToHexString(byte[] src) {
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

    public static byte[] wrapMessage(byte command, byte p1, byte p2, byte lc, byte[] parameters) throws IOException {
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

//    public String[][] getTechList() {
//        return techList;
//    }


    public void reset(){
//        this.mCepasCardStatus = CepasCardStatus.IDLE;
        this.mCanId = null;
    }

    public void startProcess(){
//        this.mCepasCardStatus = CepasCardStatus.PROCESSING;
        this.mCanId = null;
    }

    public class DecryptCepas extends AsyncTask<Tag, Void, String> {
        @Override
        protected void onPostExecute(String canId) {
            mCanId = canId;
            Log.v(LOG_TAG, "Can Id Found: "+ canId);

//            if(mCanId == null){
//                mCepasCardStatus = CepasCardStatus.FAILED_OR_EMPTY;
//            }else{
//                mCepasCardStatus = CepasCardStatus.OK;
//            }
        }

        @Override
        protected String doInBackground(Tag... params) {
            if(params == null)
            {
                Log.d(LOG_TAG, "Tag cannot be found");
                return  null;
            }

            String canId = processCepasCanId(params[0]);
            if(canId != null)
            {
                return canId;
            }

            return null;
        }
    }
}

