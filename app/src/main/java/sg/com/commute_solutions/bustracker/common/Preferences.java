package sg.com.commute_solutions.bustracker.common;

/**
 * Created by Kyle on 31/8/16.
 */
public class Preferences {
    //Authenication
    public static final String AUTH_TOKEN = "authToken";
    public static final String IS_FIRST_TIME = "isFirstTime";
    public static final String LOGGED_IN_USERNAME = "loggedInUsername";
    public static final String LOGGED_IN_PHONENUMBER = "loggedInPhoneNumber";
    public static final String LOGGED_IN_VEHICLENO = "loggedInVehicleNo";
    public static final String LOGGED_IN_SIXDIGIT = "loggedInSixDigit";
    public static final String MANUAL_ACCESSCODE = "manualAccessCode";
    public static final String ROUTE_ID = "RouteId";

    //LogSheet
    public static final String ADHOC_CHARTER_ID = "BusCharterId";

    //Settings
    public static final String NFCINDICATOR = "useNfc";
    public static final String ON_OFF_TRACKER = "onOffTracker";

    //Location
    public static final String LAST_UPDATED_TIME = "lastUpdatedTime";
    public static final String TO_CHECK_PROXIMITY = "toCheckProximity";

    //Bluetooth
    public static final String BLUETOOTH_DEVICE_ADDRESS = "bluetoothDeviceAddress";
    public static final String BLUETOOTH_DEVICE_NAME = "bluetoothDeviceName";

    //Passenger
    public static final String PASSENGERCOUNT = "passsengerCount";
    public static final String PASSENGERLIST = "passengerList";
    public static final String CAN_ID = "canId";

    //Admin Permissions
    public static final String SHOW_MESSAGE = "showMessage";
    public static final String SHOW_CAN_ID = "showCanId";
    public static final String ENABLE_INTERNAL_NFC = "enableInternalNFC";
    public static final String ENABLE_EXTERNAL_NFC = "enableExternalNFC";
    public static final String CURRENTACTIVITY = "currentActivity";

    //Chartering
    public static final String ROLE = "role";

    //Misc
    public static final String LANGUAGE = "language";
    public static final String STATUSCODE = "statusCode";
    public static final String LAST_CHARTER_ID = "lastCharterId";
    public static final String BADGE_COUNT = "batchCount";
}
