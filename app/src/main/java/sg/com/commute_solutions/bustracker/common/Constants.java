package sg.com.commute_solutions.bustracker.common;

/**
 * Created by Kyle on 31/8/16.
 */
public class Constants {
    //common
    public static final String SHARE_PREFERENCE_PACKAGE = "sg.com.commute_solutions.bustracker";

    //Messages
    public static final String CANNOT_LOGIN1 = "Please check your phone number.";
    public static final String CANNOT_LOGIN2 = "Unable to connect to the server. Please contact your operations team.";
    public static final String WRONG_SIX_DIGIT = "Invalid 6 Digit Code Entered.";
    public static final String WRONG_ACCESS_CODE = "Invalid Route Access Code Entered.";
    public static final String WRONG_SIX_DIGIT2 = "Too Many Attempt. Please contact your operations team.";
    public static final String RETRY_MESSAGE = "No jobs detected from the server but tracking is still available. You may retry to fetch jobs from server. Alternatively, you may also contact your operations team.";
    public static final String RETRY_MESSAGE2 = "Driver app functions will be limited. Are you sure you wish to proceed without any jobs?";
    public static final String RETRY_MESSAGE3 = "Network is unstable. Please check your network settings. We will keep trying to connect in the meantime.";
    public static final String UNAUTHORISED_MESSAGE = "You have been logged out by another person. Please login again.";
    public static final String GENERIC_ERROR_MSG = "Unexpected error has occured. Please re-login again.";
    public static final String ON_CONNECTION_SUSPENDED = "Connection Suspended";
    public static final String END_JOB_SUCCESS = "Job had been sucessfully ended";
    public static final String END_JOB_UNSUCCESS = "There's an error when ending the job.";
    public static final String ON_CONNECTION_FAILED = "Connection Failed";
    public static final String LANGUAGE_TITLE = "Language Setting";
    public static final String LANGUAGE_MESSAGE = "Please select a language";
    public static final String CURRENT_LANGUAGE = "App is already in english.";
    public static final String RESTART_APP = "App will now close. Please re-open the app to see the selected language.";
    public static final String GREETINGS_DESC = "today is :";
    public static final String GREETINGS_TITLE = "HELLO, ";

    public static final String CANNOT_FETCH_AVAILABLE_JOBS = "Unable to verify job(s). Please check your internet connection.";
    public static final String CANNOT_FETCH_DRIVER_LIST = "Unable to retrieve driver(s). Please check your internet connection.";
    public static final String TRACKING_CHARTER_MESSAGE = "You may track the driver status by tapping on the map! You may also tap and hold the map to copy the URL for tracking.";
    public static final String LOGOUT_CONFIRMATION = "Are you sure you want to logout?";
    public static final String INSUFFICIENT_DATA_FOR_NEW_CHARTER = "Unable to process the information entered. Kindly check if you have filled in all required details before trying again. \n\nIf the problem still persist, please try again later as the server may be busy. \n\nAlternatively, you may also contact your operations team.";
    public static final String CONFIRMATION_MESSAGE = "Please check all information entered before submission of new charter. \n\nPlease note that once the charter is submitted and there are changes to be made, You will have to go to your charters to delete it.";
    public static final String RESUBMISSION_CONFIRMATION_MESSAGE = "Please enter a new sub out price. Leave blank if you do not wish to change. Amount is in SGD($)";
    public static final String EXPIRY_MESSAGE = "Please set an expiry duration for this charter. (in Hours)";
    public static final String TWO_WAY_CONFIRMATION_MESSAGE = "Please kindly note that all Two-way charters will be split into 2 x One-way charters for your convenience.\n\nPlease also note that the pricing will be divided accordingly.";
    public static final String POC_CONFIRMATION_MESSAGE = "Please enter the POC Contact Details. Leave blank if there are no changes.";
    public static final String ACCEPT_CONFIRMATION_MESSAGE = "Please confirm you wish to accept this charter.";
    public static final String DELETE_CONFIRMATION_MESSAGE = "Please confirm you wish to delete this charter.";
    public static final String DELETE_CONFIRMATION_MESSAGE2 = "This charter is already scheduled! Withdrawing from this charter may incur penalty fees. Do you still wish to proceed?";

    public static final String START_JOB_CONFIRMATION = "Please confirm you wish to start the adhoc job now.";
    public static final String START_JOB_MESSAGE = "Please tap on 'Start Trip' Button to begin this adhoc job";
    public static final String JOB_ONGOING_MESSAGE = "Job is in progress";
    public static final String END_JOB_CONFIRMATION = "Please confirm that this adhoc job is completed.";
    public static final String END_JOB_MESSAGE = "This job has already been completed!";
    public static final String JOB_EXPIRED_MESSAGE = "Job has passed its allowance time therefore it has been deemed incomplete. Kindly be noted the money with-held will be deducted for compensation";
    public static final String ADHOC_ERROR_MESSAGE = "There is an error processing this adhoc job. Kindly contact the operator and quote this charter ID for more information:\n";
    public static final String BOARDING_CODE_TITLE = "BOARDING CODE : ";

    public static final String DISPUTE_MESSAGE = "Please enter the details of the problem you wish to report.";
    public static final String DISPUTE_TITLE = "Confirm Dispute?";

    public static final String MINIMUM_DISPOSAL_HOURS_NOT_HIT = "Minimum disposal hours per charter is 3. You might wish to consider 'One-Way Charter' if the amount of disposal hours is less than 3.";
    public static final String ONE_WAY_MESSAGE_NO_RETURN_TIME = "No Return time required for One-Way Trip.";
    public static final String CREDIT_CARD_INVALID = "Card is invalid. Please check if you have entered the correct card details.";
    public static final String WITHDRAW_MESSAGE = "Please enter the amount you wish to cash out. Amount is in SGD($)";
    public static final String UPDATE_VEHICLE_MESSAGE = "Please enter the vehicle No";
//    public static final String BANKACCOUNT_CHANGE = "Please enter your bank account for cash out.";

    public static final String ON_PASSENGER_LIST_FULL_ALERT = "There are still passenger data stored in phone. Please use 'Send Data' to send our the passengers list before turning off tracking";

    public static final String IS_SEND_LOCATION = "Start sending location.......";
    public static final String NOT_SEND_LOCATION = "Stop sending location.";
    public static final String STOP_POLLING_CONFIRMATION = "Are you sure you want to stop polling location?";

    public static final String NO_CONNECTION_STORE_DATA = "No connection detected. Saving current location to database...";
    public static final String ON_UPDATE_LOCATION = "Location updated!";
    public static final String NO_PASSENGER_IN_LIST = "Nothing to send!";
    public static final String GPS_OFF = "Please enable your GPS while performing jobs belonging to Bus Link";

    public static final String INTERNAL_NFC_NULL = "Your phone does not support NFC. Please disable NFC function to continue using the tracker.";
    public static final String INTERNAL_NFC_OFF = "You have selected to use Internal NFC. Please enable your NFC function to continue using the tracker.";
    public static final String INTERNAL_NFC_ON = "Internal NFC is enabled.";

    public static final String EXTERNAL_NFC_NULL = "Your phone does not support Bluetooth. Please disable NFC function to continue using the tracker.";
    public static final String EXTERNAL_NFC_OFF = "You have selected to use External NFC. Please enable your Bluetooth function to continue using the tracker.";
    public static final String ON_NO_DEVICE_DECTECTED = "Unable to find previously paired device. Please select a new device from Settings or restart the NFC device and driver app to attempt again.";
    public static final String DEVICE_READY_TO_PAIR = "Device is ready to connect. Please press the red button on top to pair with the NFC device.";

    public static final String USB_ALERT = "You have selected to use the reader via USB. Please attach the USB reader to the phone now. \n\nOnce attached, please tap on the 'Ok' button to synchronize.";
    public static final String USB_READER_SELECT = "Please find the reader in the drop down menu.";

    public static final String CONTRACT_PICKUP_POINT_INITIAL_TEXT = "Please note that the app will only allow you to put up to 3 pick up points. If there are more than 3 pick up points, please put in the first 3 pick up points and state the total number of pick up points and their area in the remarks.\n" +
            "(e.g Total No. of pick up points: 6\n" +
            "Area: Woodlands.)\n" +
            "Thank you.\n";
    public static final String CONTRACT_DROPOFF_POINT_INITIAL_TEXT = "Please note that the app will only allow you to put up to 3 drop off points. If there are more than 3 drop off points, please put in the first 3 drop off points and state the total number of drop off points and their area in the remarks.\n" +
            "(e.g Total No. of drop off points: 4\n" +
            "Area: CBD Area.)\n" +
            "Thank you. \n";
    public static final String NO_POINTS_SELECTED = "Please choose at least 1 point for ";
    public static final String END_DATE_BEFORE_START_DATE = "The end date you entered is before the start date.";
    public static final String CONTRACT_LESS_THAN_7_DAYS = "The contract you are trying to list is less than 7 days.";
    public static final String BORDING_CODE_TEXT_EN = "BOARDING CODE: ";

    //Messages
    public static final String BORDING_CODE_TEXT_CN = "登入号: ";
    public static final String CANNOT_LOGIN1_CH = "请检查您的电话号码。";
    public static final String CANNOT_LOGIN2_CH = "无法连接。 请联络您的巴士运作服务员。";
    public static final String WRONG_ACCESS_CODE_CH = "您输入的路线代码不正确";
    public static final String WRONG_SIX_DIGIT_CH = "输入不正确.";
    public static final String WRONG_SIX_DIGIT2_CH = "您试图用错误的号码登录太多次了. 请联络您的巴士运作服务员";
    public static final String RETRY_MESSAGE_CH = "您暂时无工作趟，但仍能开始追踪器. 您可以重刷新趟，或者您也可以联络您的巴士运作服务员。";
    public static final String RETRY_MESSAGE2_CH = "追踪器能会被限制，你确定你想继续吗？";
    public static final String RETRY_MESSAGE3_CH = "网络不稳定。请检查您的网络。 我们会继续尝试连接。";
    public static final String UNAUTHORISED_MESSAGE_CH = "请重新登录。";
    public static final String GENERIC_ERROR_MSG_CH = "意外的错误发生了！请重新登录。";
    public static final String ON_CONNECTION_SUSPENDED_CH = "连接中止";
    public static final String ON_CONNECTION_FAILED_CH = "连接失败";
    public static final String LANGUAGE_TITLE_CH = "语言设定";
    public static final String LANGUAGE_MESSAGE_CH = "请选择一种语言";
    public static final String CURRENT_LANGUAGE_CH = "目前已经在使用中文。";
    public static final String RESTART_APP_CH = "应用程序现在将关闭。请重新打开应用程序。";
    public static final String GREETINGS_DESC_CN = "的登入号是：";
    public static final String GREETINGS_TITLE_CN = "你好, ";

    public static final String CANNOT_FETCH_AVAILABLE_JOBS_CH = "无法验证工作趟。请检查您的网络。";
    public static final String CANNOT_FETCH_DRIVER_LIST_CH = "无法取到司机资料。请检查您的网络。";
    public static final String TRACKING_CHARTER_MESSAGE_CH = "您可以通过点击地图来跟随司机的位置。您也可以长点夺取司机位置的链接。";
    public static final String LOGOUT_CONFIRMATION_CH = "你想退出吗？";
    public static final String INSUFFICIENT_DATA_FOR_NEW_CHARTER_CH = "工作趟资料不足。请检查您是否少给了哪一个重要的资料。\n\n如果问题依然存在，请稍等5分钟再试一次，或者您也可以联络您的巴士运作服务员。";
    public static final String CONFIRMATION_MESSAGE_CH = "提交前请检查输入的所有信息。\n\n一旦工作提交后，如有任何变动，都必须删除工作并重新创建工作。";
    public static final String RESUBMISSION_CONFIRMATION_MESSAGE_CH = "请输入新的价钱[SGD($)]。如果没有任何改变，此字段可以留空。";
    public static final String EXPIRY_MESSAGE_CH = "请输入您想要的过期小时数。";
    public static final String TWO_WAY_CONFIRMATION_MESSAGE_CH = "请注意所有双程工作趟将会分成两个单程工作趟。价钱将会平分。";
    public static final String POC_CONFIRMATION_MESSAGE_CH = "请输入联系人的名称和电话号码。如果没有任何改变，此字段可以留空。";
    public static final String ACCEPT_CONFIRMATION_MESSAGE_CH = "请确认您要接受这个工作趟。";
    public static final String DELETE_CONFIRMATION_MESSAGE_CH = "请确认您要撤回这个工作趟。";
    public static final String DELETE_CONFIRMATION_MESSAGE2_CH = "工作趟已被接受。撤回工作可能会面临罚款。请确定您是否要撤回这个工作趟。";

    public static final String START_JOB_CONFIRMATION_CH = "请确认这工作趟现在开始。";
    public static final String START_JOB_MESSAGE_CH = "请点击'Start Trip'即可开始工作趟。";
    public static final String JOB_ONGOING_MESSAGE_CH = "正在进行工作趟。";
    public static final String END_JOB_CONFIRMATION_CH = "请确认您已经完成工作趟。";
    public static final String END_JOB_MESSAGE_CH = "这工作趟已经完成。";
    public static final String JOB_EXPIRED_MESSAGE_CH = "工作趟早已超过允许的时间，所以这份工作趟已被视为未完成。扣押的钱会用来赔偿未完成的工作趟。";
    public static final String ADHOC_ERROR_MESSAGE_CH = "注册工作趟时发生错误，请引用此工作趟ID联络管理员:\n";
    public static final String BOARDING_CODE_TITLE_CN = "登入号 : ";

    public static final String DISPUTE_MESSAGE_CH = "请输入您要上诉的资料。";
    public static final String DISPUTE_TITLE_CH = "争议";

    public static final String MINIMUM_DISPOSAL_HOURS_NOT_HIT_CH = "小时包车趟最少3小时。如果您需要少过3小时的包车，您可考虑‘单程包车’。";
    public static final String ONE_WAY_MESSAGE_NO_RETURN_TIME_CH = "单程趟无需输入回程时间。";
    public static final String CREDIT_CARD_INVALID_CH = "信用卡无效。请检查并确认银行卡的资料。";
    public static final String WITHDRAW_MESSAGE_CH = "请输入您要兑现的数目。以新币计算($)";
    public static final String BANKACCOUNT_CHANGE_CH = "请输入您的银行户口号码以进行兑现";

    public static final String ON_PASSENGER_LIST_FULL_ALERT_CH = "还有未发送的乘客资料。在关闭定位系统前，请按'Send Data'来发送乘客资料。";

    public static final String IS_SEND_LOCATION_CH = "开始发送定位。";
    public static final String NOT_SEND_LOCATION_CH = "以停止发送定位。";
    public static final String STOP_POLLING_CONFIRMATION_CH = "确定要停止发送定位信息？";
    public static final String UPDATE_VEHICLE_MESSAGE_CH = "请输入您的新车牌号码";

    public static final String NO_CONNECTION_STORE_DATA_CH = "无法连接服务器。";
    public static final String ON_UPDATE_LOCATION_CH = "位置更新！";
    public static final String NO_PASSENGER_IN_LIST_CH = "没有数据。";
    public static final String GPS_OFF_CH = "在执行Bus Link的工作趟时，请启动您的GPS定位设备";
    public static final String END_JOB_SUCCESS_CH = "恭喜工作完成";
    public static final String END_JOB_UNSUCCESS_CH = "无法连接服务器。";

    public static final String INTERNAL_NFC_NULL_CH = "您的手机找不到NFC。请关闭NFC设备以继续使用定位系统。";
    public static final String INTERNAL_NFC_OFF_CH = "您已选择內置NFC。请开启NFC以继续使用定位系统。";
    public static final String INTERNAL_NFC_ON_CH = "正在启用手机NFC。";

    public static final String EXTERNAL_NFC_NULL_CH = "您的手机找不到蓝牙。请关闭蓝牙设备以继续使用定位系统。";
    public static final String EXTERNAL_NFC_OFF_CH = "您已选择外置NFC。请启动蓝牙以继续使用定位系统。";
    public static final String ON_NO_DEVICE_DECTECTED_CH = "无法找到之前匹配的设备。请选择新的设备或重启NFC设备和定位系统。";
    public static final String DEVICE_READY_TO_PAIR_CH = "设备可以开始连接。请点击左上角的红色按钮以匹配NFC设备。";

    public static final String USB_ALERT_CH = "您已选择通过USB使用读卡器。 请将读卡器连接到手机。\n\n完成连接后，请按'Ok'以进行同步";
    public static final String USB_READER_SELECT_CH = "请从下列菜单选择您的读卡器。";

    // Constants for web connections
    public static final int CONNECTION_TIMEOUT = 10000;
    public static final int READ_TIMEOUT = 100000;
//    public static final int STATUS_ERROR = 400;
//    public static final int STATUS_UNAUTHORIZED = 401;

    // URL's to be used to access the API
    //V1
//    public static final String END_POINT = "https://bustracker.azurewebsites.net/api/0";
//    public static final String LOGIN_URL = END_POINT + "/user/login";
//    public static final String LOCATION_URL1 = END_POINT + "/user/";
//    public static final String LOCATION_URL2 = "/location";
//    public static final String PASSENGER_URL = END_POINT + "/passenger/";
//    public static final String ROUTEPATHS = END_POINT + "/track/RoutePath/";
//    public static final String ROUTEPOINTS = END_POINT + "/track/RoutePoints/";

    //Staging
    //public static final String END_POINT = "https://bustrackerstaging.azurewebsites.net/api/2";
    //Production
    public static final String END_POINT = "https://bustracker.azurewebsites.net/api/2";
    public static final String LOGIN_URL = END_POINT + "/user/login";
    public static final String LOG_SHEET_URL = END_POINT + "/logsheet/PostLogSheet";
    public static final String LOGIN_URL_V2 = END_POINT + "/user/loginV2";
    public static final String CHECK_PHONE_URL = END_POINT + "/user/checkPhoneNumber";
    public static final String CHECK_ACCESS_CODE = END_POINT + "/Jobs/CheckRouteAccessCode";
    public static final String GET_MANUAL_JOB = END_POINT + "/Jobs/GetSelfSetJob";
    public static final String MANUAL_END_JOB = END_POINT + "/Jobs/EndRouteTrip";
    public static final String UPDATE_VEHICLE_NO = END_POINT + "/Jobs/UpdateVehicleNo";

    public static final String SCAN_QR_STUDENT = END_POINT + "/QR/scan";

    public static final String VERSION_URL = END_POINT + "/user/login";
    public static final String LOCATION_URL = END_POINT + "/locations/gps";
    public static final String JOBS_URL = END_POINT + "/Jobs";
    public static final String START_TRIP_URL = JOBS_URL + "/startTrip";
    public static final String END_TRIP_URL = JOBS_URL + "/endTrip";
    public static final String ATTENDANCE_URL = JOBS_URL + "/attendances";
    public static final String LOGOUT_URL = END_POINT + "/user/logout";

    public static final String VENDOR_URL = END_POINT + "/vendor";
    public static final String REFRESH_DRIVER_TOKEN_URL = VENDOR_URL + "/driverToken";
    public static final String ONLINE_USERS_URL = VENDOR_URL + "/OnlineUsers";
    public static final String VIEWCHARTER_URL = VENDOR_URL + "/viewcharters";
    public static final String VIEWMORECHARTER_URL = VENDOR_URL + "/viewmorecharters";
    public static final String VIEWOWNCHARTER_URL = VENDOR_URL + "/viewsuboutcharters";
    public static final String VIEWACCEPTEDCHARTER_URL = VENDOR_URL + "/viewacceptedcharters";
    public static final String CREATECHARTER_URL = VENDOR_URL + "/createcharter";
    public static final String ACCEPTCHARTER_URL = VENDOR_URL + "/accept";
    public static final String DELETECHARTER_URL = VENDOR_URL + "/delete";
    public static final String WITHDRAWCHARTER_URL = VENDOR_URL + "/Withdraw";
    public static final String WALLETHISTORY_URL = VENDOR_URL + "/ewallethistory";

    public static final String CHARTER_URL = VENDOR_URL + "/charter";
    public static final String RETRIEVEJOB_URL = CHARTER_URL + "/viewJob";
    public static final String RETRIEVEDRIVERLIST_URL = CHARTER_URL + "/viewDrivers";
    public static final String UPDATEJOB_URL = CHARTER_URL + "/updateJob";
    public static final String UPDATEDRIVER_URL = CHARTER_URL + "/updateDriver";
    public static final String UPDATEPOC_URL = CHARTER_URL + "/updatePOC";

    public static final String DISPUTE_URL = CHARTER_URL + "/dispute";
    public static final String NEW_DISPUTE_URL = DISPUTE_URL + "/new";
    public static final String DISPUTE_RESPONSE_URL = DISPUTE_URL + "/response";
    public static final String VIEW_DISPUTE_LIST_URL = DISPUTE_URL + "/history";

    public static final String PROFILE_URL = VENDOR_URL + "/profile";
    public static final String CASHOUT_URL = VENDOR_URL + "/cashout";
//    public static final String BANKACCOUNT_URL = PROFILE_URL + "/EditBankAccount";
    public static final String CREDIT_CARD_URL = PROFILE_URL + "/editCreditCard";

    //contract
    public static final String CONTRACT_URL = VENDOR_URL + "/contract";
    public static final String CREATE_CONTRACT_URL = CONTRACT_URL + "/createContract";
    public static final String VIEW_CONTRACT_URL = CONTRACT_URL + "/viewAllContracts";
    public static final String DELETE_CONTRACT_URL = CONTRACT_URL + "/deleteContract";
    public static final String CALL_COUNTER_URL = CONTRACT_URL + "/callCounter";

    //Payment(Stripe)
    public static final String STRIPE_DEBUG_KEY = "pk_test_Aat1gkieiiDil66wuS5frx8d";
    public static final String STRIPE_LIVE_KEY = "pk_live_8mffjVj4nCOGPSkuiPp06VQH";

    // Constants used in JSON Parsing or values attached in a URL server connection
//    public static final String AUTHORIZATION = "Authorization";
    public static final String TOKEN = "token";
    public static final String DATA = "data";
    public static final String VERSION = "versionNo";

    //User
    public static final String USERNAME = "userName";
    public static final String PASSWORD = "password";
    public static final String LOGINPHONE = "phoneNumber";
        public static final String  ACCESSCODE = "accessCode";
    public static final String LOGINSIXDIGIT = "sixDigitCode";
    public static final String LOGINVEHICLENO = "vehicleNo";
    public static final String DEVICE_TOKEN = "deviceToken";
    public static final String DRIVERROLE = "role";
//    public static final String USERID = "userId";

    //Log Sheet
    public static final String LOG_SHEET_USERNAME = "username";
    public static final String LOG_SHEET_FULLNAME = "fullName";
    public static final String LOG_SHEET_SIGN_IMG = "signImg";
    public static final String LOG_SHEET_BUSCHARTERID = "charterId";

    public static final String STATUS = "status";
    public static final String MESSAGE = "error";

    //Passenger
    public static final String PASSENGERS = "passengers";
    public static final String NAME = "name";
    public static final String GENDER = "gender";
    public static final String EZLINKCANID = "ezlinkCanId";
    public static final String PICURL = "picUrl";
    public static final String SMSNOK = "smsNOK";
    public static final String NOKNAME = "nokName";
    public static final String NOKRELATIONSHIP = "nokRelationship";
    public static final String NOKCONTACT = "nokContact";
    public static final String DATE_TIME = "presentDateTime";

    //Jobs
    public static final String ISSCHOOLBUS = "schoolBus";
    public static final String IS_DROP_OFF = "isDropOff";
    public static final String ENABLEINTERNAL = "internalNfc";
    public static final String ENABLEEXTERNAL = "externalNfc";
    public static final String FOURDIGITCODE = "fourDigitCode";
    public static final String CODENAME = "codeName";
    public static final String ROUTEID = "routeId";
//    public static final String ISADHOC = "isAdhoc";

    //Map
    public static final String LATITUDE = "latitude";
    public static final String LONGTITUDE = "longitude";
    public static final String ALTITUDE = "altitude";
    public static final String ACCURACY = "accuracy";
    public static final String SPEED = "Speed";
    public static final String DATE = "DateCreated";
    public static final double EARTH_RADIUS = 6378100.0;

    //Route
    public static final String JOBS = "points";
    public static final String ROUTE = "path";
    public static final String POINTNAME = "pointName";
    public static final String TYPE = "type";
    public static final String TIME = "time";
    public static final String NOOFPASSENERS = "numberOfPassengers";
    public static final String ATTENDANCES = "Attendances";

    //Database Constants
    public static final String KEY_NO = "SN";
    public static final String KEY_ID = "ID";
    public static final String KEY_LONGTITUDE = "LONGTITUDE";
    public static final String KEY_LATITUDE = "LATITUDE";
    public static final String KEY_ALTITUDE = "ALTITUDE";
    public static final String KEY_ACCURACY = "ACCURACY";
    public static final String KEY_SPEED = "SPEED";
    public static final String KEY_DATE = "DATE";

    //Bluetooth
    public static final String DEFAULT_MASTER_KEY = "ACR1255U-J1 Auth";
    public static final byte[] PAIRING_COMMAND = { (byte) 0xE0, 0x00, 0x00, 0x40, 0x01 };
    public static final byte[] UNPAIR_COMMAND = { (byte) 0xE0, 0x00, 0x00, 0x40, 0x00 };
    public static final String APDU1 = "00a40000024000";
    public static final String APDU2 = "903203000000";

    //Chartering
    public static final String VIEW_CHARTER_LIST = "charterList";
    public static final String VIEW_SUB_OUT_LIST = "subOutCharters";
    public static final String VIEW_ACCEPTED_LIST = "acceptedCharters";
    public static final String CHARTER = "charter";
    public static final String CHARTER_ID = "id";
    public static final String CHARTER_DATE = "date";
    public static final String CHARTER_EXPIRY_DATE = "expiryDate";
    public static final String CHARTER_COST ="cost";
    public static final String CHARTER_BUSTYPE = "busType";
    public static final String CHARTER_DISPOSAL_DURATION = "disposalDuration";
    public static final String CHARTER_PICKUP_NAME = "pickUpName";
    public static final String CHARTER_DROPOFF_NAME = "dropOffName";
    public static final String CHARTER_TIME = "time";
    public static final String CHARTER_PICKUP_LATITUDE = "pickupLatitude";
    public static final String CHARTER_PICKUP_LONGITUDE = "pickupLongitude";
    public static final String CHARTER_DROPOFF_LATITUDE = "dropOffLatitude";
    public static final String CHARTER_DROPOFF_LONGITUDE = "dropOffLongitude";
    public static final String CHARTER_POC_NAME = "pocName";
//    public static final String CHARTER_POC_EMAIL ="pocEmail";
    public static final String CHARTER_POC_CONTACT_NO = "pocContactNo";
//    public static final String CHARTER_POC_COMPANY = "pocCompany";
    public static final String CHARTER_REMARKS = "remarks";
    public static final String CHARTER_EXPIRY_DURATION = "expiryDuration";
    public static final String CHARTER_ACCESS_CODE = "accessCode";
    public static final String CHARTER_CAN_RESUBMIT = "canResubmit";
    public static final String CHARTER_OWN_CHARTER = "isMyCharter";

    //Creation
    public static final String CHARTER_BUSQUANTITY = "busQty";
    public static final String CHARTER_SERVICE_TYPE = "serviceType";
    public static final String CHARTER_PICKUPTIME = "pickUpTime";
    public static final String CHARTER_DROPOFFTIME = "dropOffTime";

    //Driver/Accept Jobs
    public static final String BUS_CHARTER_ID = "busCharterId";
    public static final String BUS_CHARTER_DRIVER_LIST = "driversList";
    public static final String BUS_CHARTER_DRIVER_ID = "driverId";
    public static final String BUS_CHARTER_DRIVER_NAME = "driverName";
    public static final String BUS_CHARTER_DRIVER_CONTACT_NO = "driverContact";
    public static final String BUS_CHARTER_VEHICLE_NUMBER = "vehicleNo";
    public static final String BUS_CHARTER_SUCCESS = "success";
    public static final String BUS_CHARTER_ACCEPTED = "isAccepted";
    public static final String BUS_CHARTER_CANCELLED = "isCancelled";
    public static final String BUS_CHARTER_MESSAGE = "message";
    public static final String BUS_CHARTER_DISPUTABLE = "isDisputable";
    public static final String BUS_CHARTER_COMPLETED = "isCompleted";

    //Payment
    public static final String WALLET = "eWallet";
    public static final String WITHHELD_WALLET = "withheld";
    public static final String HISTORY = "history";
    public static final String CREDIT_CARD = "creditCardLast4";
    public static final String BANK_ACCOUNT = "bankAccount";
    public static final String STRIPE_TOKEN = "stripeToken";
    public static final String TO_DELETE_CREDIT_CARD = "isDelete";
    public static final String TRANSACTION_TYPE = "isWithdraw";
    public static final String TRANSACTION_DETAILS = "details";
    public static final String TRANSACTED_AMOUNT = "amount";
    public static final String TRANSACTION_METHOD = "method";

    //Misc
    public static final String ONLINEUSERS = "onlineUsers";
    public static final String EXTRA = "extra";
    public static final String HASMORECHARTERS = "hasMore";
    public static final String LASTCHARTERID = "lastCharterId";
    public static final String TRACKING_URL = "url";

    //Language
    public static final String CHINESE = "CH";
    public static final String ENGLISH = "EN";

    //Adhoc
    public static final String ADHOC = "adhoc";
    public static final String JOBSTATUS = "jobStatus";
    public static final String REASON = "reasons";
    public static final String PASSENGERNOSHOW = "noShow";
//    public static final String WAITINGTIME = "waitingTime";
//    public static final String EXCEEDEDTIME = "exceededTime";

    //Dispute
    public static final String REQUIRESACTION = "requiresAction";
    public static final String ISSETTLED = "isSettled";
    public static final String PENDINGADMIN = "pendingAdmin";
    public static final String REBUKEDREASON = "rebukeReason";
    public static final String ADMININPUT = "adminInput";
    public static final String HASAGREED = "hasAgreed";

    //USB
    public static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    public static final String[] stateStrings = { "Unknown", "Absent", "Present", "Swallowed", "Powered", "Negotiable", "Specific" };

    //Contract Serices
    public static final String PICKUP_POINT_1_NAME = "pickupPoint1Name";
    public static final String PICKUP_POINT_2_NAME = "pickupPoint2Name";
    public static final String PICKUP_POINT_3_NAME = "pickupPoint3Name";
    public static final String PICKUP_POINT_1_LATITUDE = "pickupPoint1Lat";
    public static final String PICKUP_POINT_2_LATITUDE = "pickupPoint2Lat";
    public static final String PICKUP_POINT_3_LATITUDE = "pickupPoint3Lat";
    public static final String PICKUP_POINT_1_LONGITUDE = "pickupPoint1Lng";
    public static final String PICKUP_POINT_2_LONGITUDE = "pickupPoint2Lng";
    public static final String PICKUP_POINT_3_LONGITUDE = "pickupPoint3Lng";
    public static final String DROPOFF_POINT_1_NAME = "dropoffPoint1Name";
    public static final String DROPOFF_POINT_2_NAME = "dropoffPoint2Name";
    public static final String DROPOFF_POINT_3_NAME = "dropoffPoint3Name";
    public static final String DROPOFF_POINT_1_LATITUDE = "dropoffPoint1Lat";
    public static final String DROPOFF_POINT_2_LATITUDE = "dropoffPoint2Lat";
    public static final String DROPOFF_POINT_3_LATITUDE = "dropoffPoint3Lat";
    public static final String DROPOFF_POINT_1_LONGITUDE = "dropoffPoint1Lng";
    public static final String DROPOFF_POINT_2_LONGITUDE = "dropoffPoint2Lng";
    public static final String DROPOFF_POINT_3_LONGITUDE = "dropoffPoint3Lng";
    public static final String HAS_ERP = "hasERP";
    public static final String INCLUDES_ERP = "includesERP";
    public static final String CONTRACT_COST = "contractCost";
    public static final String CONTRACT_START_DATE = "startDate";
    public static final String CONTRACT_END_DATE = "endDate";
    public static final String CONTRACT_BUS_SIZE = "busSize";
    public static final String CONTRACT_CONTACT_NO = "contactNo";
    public static final String ADDITIONAL_INFO = "additionalInfo";
    public static final String IS_OWN_CONTRACT = "isOwnCharter";
    public static final String VIEW_CONTRACTLIST = "contractList";
}
