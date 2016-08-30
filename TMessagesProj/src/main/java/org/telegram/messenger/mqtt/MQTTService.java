package org.telegram.messenger.mqtt;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Contacts.People;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.util.Log;
import android.widget.Toast;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.mqtt.Connection.ConnectionStatus;
import com.path.android.jobqueue.JobManager;

import org.telegram.messenger.shamChat.ChatMessage;

import org.telegram.messenger.shamChat.Constant;
import org.telegram.messenger.shamChat.FriendGroup;
import org.telegram.messenger.shamChat.FriendGroupMember;
import org.telegram.messenger.shamChat.MessageThread;
import org.telegram.messenger.shamChat.MyMessageType;
import org.telegram.messenger.shamChat.MessageContentTypeProvider.MessageContentType;
import org.telegram.messenger.shamChat.ChannelOnLeaveEvent;
import org.telegram.messenger.shamChat.CloseGroupActivityEvent;
import org.telegram.messenger.shamChat.FileUploadingProgressEvent;
import org.telegram.messenger.shamChat.NewMessageEvent;
import org.telegram.messenger.shamChat.RokhPref;
import org.telegram.messenger.shamChat.UpdateGroupMembersList;
import org.telegram.messenger.shamChat.DeleteChatMessageDBLoadJob;
import org.telegram.messenger.shamChat.PublishToTopicJob;
import org.telegram.messenger.shamChat.RoomRestoreJob;
import org.telegram.messenger.shamChat.SubscribeToAllTopicsJob;
import org.telegram.messenger.shamChat.SubscribeToEventsJob;
//import org.telegram.messenger.shamChat.SyncContactsJob;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.shamChat.User;
import org.telegram.messenger.shamChat.Utils;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

import de.greenrobot.event.EventBus;

public class MQTTService extends Service
{


    /************************************************************************/
    /*    CONSTANTS                                                         */
    /************************************************************************/

    // something unique to identify your app - used for stuff like accessing
    //   application preferences
    public static final String APP_ID = "com.rokhgroup.mqtt";

    public static final boolean DEBUG = false;
    // constants used to notify the Activity UI of received messages
    public static final String MQTT_MSG_RECEIVED_INTENT = "com.rokhgroup.mqtt.MSGRECVD";
    public static final String MQTT_MSG_RECEIVED_TOPIC  = "com.rokhgroup.mqtt.MSGRECVD_TOPIC";
    public static final String MQTT_MSG_RECEIVED_MSG    = "com.rokhgroup.mqtt.MSGRECVD_MSGBODY";

    // constants used to tell the Activity UI the connection status
    public static final String MQTT_STATUS_INTENT = "com.rokhgroup.mqtt.STATUS";
    public static final String MQTT_STATUS_MSG    = "com.rokhgroup.mqtt.STATUS_MSG";
    String userIdPub="102015";
    // constant used internally to schedule the next ping event
    public static final String MQTT_PING_ACTION = "com.rokhgroup.mqtt.PING";

    // constants used by status bar notifications
    public static final int MQTT_NOTIFICATION_ONGOING = 1;
    public static final int MQTT_NOTIFICATION_UPDATE  = 2;

    public TLRPC.TL_updateShortMessage messageTelegram;

    private Thread.UncaughtExceptionHandler androidDefaultUEH;
    private Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
        public void uncaughtException(Thread thread, Throwable ex) {
            Log.e("TestApplication", "Uncaught exception is: ", ex);
            //System.out.println("Uncaught exception is: "+ ex.toString());
            // log it & phone home.
            androidDefaultUEH.uncaughtException(thread, ex);


			/*try {
				NotifySimple.notifcation(getApplicationContext(), ex.toString(), new Intent(), R.string.ticker_title);

				File newFolder = Utils.getErrorsFolder();
				if (!newFolder.exists()) {
					newFolder.mkdir();
				}
				try {
					File file = new File(newFolder, "Errors"
							+ System.currentTimeMillis() + ".txt");
					file.createNewFile();

					byte[] data = ex.toString().getBytes();

					FileOutputStream fos = new FileOutputStream(file);
					fos.write(data);
					fos.flush();
					fos.close();

				} catch (Exception e) {
					System.out.println("ex: " + e);
				}
			} catch (Exception e) {
				System.out.println("e: " + e);
			}*/

        }
    };

    // constants used to define MQTT connection status
    public enum MQTTConnectionStatus
    {
        INITIAL,                            // initial status
        CONNECTING,                         // attempting to connect
        CONNECTED,                          // connected
        NOTCONNECTED_WAITINGFORINTERNET,    // can't connect because the phone
        //     does not have Internet access
        NOTCONNECTED_USERDISCONNECT,        // user has explicitly requested
        //     disconnection
        NOTCONNECTED_DATADISABLED,          // can't connect because the user
        //     has disabled data access
        NOTCONNECTED_UNKNOWNREASON          // failed to connect for some reason
    }

    // MQTT constants
    public static final int MAX_MQTT_CLIENTID_LENGTH = 22;

    /************************************************************************/
    /*    VARIABLES used to maintain state                                  */
    /************************************************************************/

    // status of MQTT client connection
    private MQTTConnectionStatus connectionStatus = MQTTConnectionStatus.INITIAL;


    /************************************************************************/
    /*    VARIABLES used to configure MQTT connection                       */
    /************************************************************************/

    // taken from preferences
    //    host name of the server we're receiving push notifications from
    private String          brokerHostName       = "";
    // taken from preferences
    //    topic we want to receive messages about
    //    can include wildcards - e.g.  '#' matches anything
    //private String          topicName            = "";


    // defaults - this sample uses very basic defaults for it's interactions
    //   with message brokers
    private int             brokerPortNumber     = 1883;
    private boolean         cleanStart           = false;
    private int[]           qualitiesOfService   = { 1 } ;

    //  how often should the app ping the server to keep the connection alive?
    //
    //   too frequently - and you waste battery life
    //   too infrequently - and you wont notice if you lose your connection
    //                       until the next unsuccessfull attempt to ping
    //
    //   it's a trade-off between how time-sensitive the data is that your
    //      app is handling, vs the acceptable impact on battery life
    //
    //   it is perhaps also worth bearing in mind the network's support for
    //     long running, idle connections. Ideally, to keep a connection open
    //     you want to use a keep alive value that is less than the period of
    //     time after which a network operator will kill an idle connection
    //private short           keepAliveSeconds     = 3 * 60; //every 5 minutes
    private short           keepAliveSeconds     = 60; //every 1min  //ping interval keep alive can be up to 18 hours

    // This is how the Android client app will identify itself to the
    //  message broker.
    // It has to be unique to the broker - two clients are not permitted to
    //  connect to the same broker using the same client ID.
    private String          mqttClientId = null;

    private String			clientHandle = null;

    /************************************************************************/
    /*    VARIABLES  - other local variables                                */
    /************************************************************************/
    // connection to the message broker
    private MqttAndroidClient mqttClient = null;

    // receiver that notifies the Service when the phone gets data connection
    private NetworkConnectionIntentReceiver netConnReceiver;

    // receiver that notifies the Service when the user changes data use preferences
    private BackgroundDataChangeIntentReceiver dataEnabledReceiver;

    // receiver that wakes the Service up when it's time to ping the server
    Handler  mHandler;
    private PingSender pingSender;
    private int mContactCount;
    private JobManager jobManager;

    private ChangeListener changeListener	= new ChangeListener();
    Connection connection;
    MqttConnectOptions conOpt;
    int connectRequestCount = 0;
    boolean isConnectedtoMqtt = false;
    private static boolean serviceRunning = false;

    private synchronized static boolean isRunning()
    {
		 /*
		  * Only run one instance of the service.
		  */
        if (serviceRunning == false)
        {
            serviceRunning = true;
            return false;
        }
        else
        {
            return true;
        }
    }
    /************************************************************************/
    /*    METHODS - core Service lifecycle methods                          */
    /************************************************************************/

    // see http://developer.android.com/guide/topics/fundamentals.html#lcycles

    @Override
    public void onCreate()
    {
        super.onCreate();

        androidDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(handler);

        // StrictMode.setThreadPolicy(new
        // StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
        // StrictMode.setVmPolicy(new
        // StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());

        // reset status variable to initial state
        connectionStatus = MQTTConnectionStatus.INITIAL;


        // create a binder that will let the Activity UI send
        //   commands to the Service
        mBinder = new LocalBinder<MQTTService>(this);

        // set the broker hostname
        //String server = "iot.eclipse.org";
        //String clientId = "testshamchat1";

        brokerHostName = Constant.MqttTcpHost;
        //reza_ak
        mqttClientId ="102015";
                //SHAMChatApplication.getConfig().getUserId();
        brokerPortNumber = Integer.valueOf(Constant.MqttTcpPort);

        //Session	= new RokhPref(getApplicationContext());

        jobManager =  ApplicationLoader.getInstance().getJobManager();
        //ApplicationLoader.getJobManager();

        // register to be notified whenever the user changes their preferences
        //  relating to background data use - so that we can respect the current
        //  preference

        dataEnabledReceiver = new BackgroundDataChangeIntentReceiver();
        registerReceiver(dataEnabledReceiver,
                new IntentFilter(ConnectivityManager.ACTION_BACKGROUND_DATA_SETTING_CHANGED));

        try {
             EventBus.getDefault().registerSticky(this,1);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // define the connection to the broker
        defineConnectionToBroker(brokerHostName);
    }


    @Override
    public void onStart(final Intent intent, final int startId)
    {
        // This is the old onStart method that will be called on the pre-2.0
        // platform.  On 2.0 or later we override onStartCommand() so this
        // method will not be called.

        new Thread(new Runnable() {
            @Override
            public void run() {
                handleStart(intent, startId);
            }
        }, "BackgroundService").start();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, final int startId)
    {

        if (isRunning())
        {
            return START_STICKY;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                handleStart(intent, startId);
            }
        }, "BackgroundService").start();

        // return START_NOT_STICKY - we want this Service to be left running
        //  unless explicitly stopped, and it's process is killed, we want it to
        //  be restarted

        return START_STICKY;
    }

    synchronized void handleStart(Intent intent, int startId)
    {
        // before we start - check for a couple of reasons why we should stop


        if (mqttClient == null)
        {
            // we were unable to define the MQTT client connection, so we stop
            //  immediately - there is nothing that we can do
            stopSelf();
            return;
        }

        ConnectivityManager cm = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        if (cm.getBackgroundDataSetting() == false) // respect the user's request not to use data!
        {
            // user has disabled background data
            connectionStatus = MQTTConnectionStatus.NOTCONNECTED_DATADISABLED;
            connection.changeConnectionStatus(ConnectionStatus.DISCONNECTED);

            // update the app to show that the connection has been disabled
            broadcastServiceStatus("Not connected - background data disabled");

            // we have a listener running that will notify us when this
            //   preference changes, and will call handleStart again when it
            //   is - letting us pick up where we leave off now
            return;
        }

        // the Activity UI has started the MQTT service - this may be starting
        //  the Service new for the first time, or after the Service has been
        //  running for some time (multiple calls to startService don't start
        //  multiple Services, but it does call this method multiple times)
        // if we have been running already, we re-send any stored data

        //Mast - TODO  disabled will use eventbus instead
        //rebroadcastStatus();
        //rebroadcastReceivedMessages();

        // if the Service was already running and we're already connected - we
        //   don't need to do anything
        boolean doConnect=true;;
        try {
            if (connectRequestCount>1) doConnect = false;
        } catch (Exception e) {
            ;
        }

        if (doConnect)
        {
            //if (isAlreadyConnected() == false)
            //{
            // set the status to show we're trying to connect
            connectionStatus = MQTTConnectionStatus.CONNECTING;
            connection.changeConnectionStatus(ConnectionStatus.CONNECTING);

            // we are creating a background service that will run forever until
            //  the user explicity stops it. so - in case they start needing
            //  to save battery life - we should ensure that they don't forget
            //  we're running, by leaving an ongoing notification in the status
            //  bar while we are running

            //mast - disabled below so the MQTT service is running notification don't get shown
            /*NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            Notification notification = new Notification(R.drawable.ic_launcher,
                                                         "MQTT",
                                                         System.currentTimeMillis());
            notification.flags |= Notification.FLAG_ONGOING_EVENT;
            notification.flags |= Notification.FLAG_NO_CLEAR;
            Intent notificationIntent = new Intent(this, MainWindow.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                                                                    notificationIntent,
                                                                    PendingIntent.FLAG_UPDATE_CURRENT);
            notification.setLatestEventInfo(this, "MQTT", "MQTT Service is running", contentIntent);
            nm.notify(MQTT_NOTIFICATION_ONGOING, notification);
            */

            // before we attempt to connect - we check if the phone has a
            //  working data connection
            if (isOnline())
            {
                // we think we have an Internet connection, so try to connect
                //  to the message broker
                connectionStatus = MQTTConnectionStatus.CONNECTING;
                connection.changeConnectionStatus(ConnectionStatus.CONNECTING);
                if (connectToBroker())
                {
                    //Subscribe to events topic
                    //jobManager.addJobInBackground(new SubscribeToEventsJob(mqttClient));

                    //Subscribe to all group topics
                    //jobManager.addJobInBackground(new SubscribeToAllTopicsJob(mqttClient));
                }
            }
            else
            {
                // we can't do anything now because we don't have a working
                //  data connection
                connectionStatus = MQTTConnectionStatus.NOTCONNECTED_WAITINGFORINTERNET;
                connection.changeConnectionStatus(ConnectionStatus.DISCONNECTED);

                // inform the app that we are not connected
                broadcastServiceStatus("Waiting for network connection");
            }
        }

        // changes to the phone's network - such as bouncing between WiFi
        //  and mobile data networks - can break the MQTT connection
        // the MQTT connectionLost can be a bit slow to notice, so we use
        //  Android's inbuilt notification system to be informed of
        //  network changes - so we can reconnect immediately, without
        //  haing to wait for the MQTT timeout
        if (netConnReceiver == null)
        {
            netConnReceiver = new NetworkConnectionIntentReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
            intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(netConnReceiver,intentFilter);

        }

        // creates the intents that are used to wake up the phone when it is
        //  time to ping the server
        if (pingSender == null)
        {
            pingSender = new PingSender();
            registerReceiver(pingSender, new IntentFilter(MQTT_PING_ACTION));
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        EventBus.getDefault().unregister(this);

        // disconnect immediately
        disconnectFromBroker();

        // inform the app that the app has successfully disconnected
        broadcastServiceStatus("Disconnected");

        //mast - remove all connections
        Map<String, Connection> connections = Connections.getInstance(this).getConnections();
        for (Connection connection : connections.values()){
            connection.registerChangeListener(changeListener);
            connection.getClient().unregisterResources();
        }

        // try not to leak the listener
        if (dataEnabledReceiver != null)
        {
            unregisterReceiver(dataEnabledReceiver);
            dataEnabledReceiver = null;
        }

        if (mBinder != null) {
            mBinder.close();
            mBinder = null;
        }


      //  getContentResolver().unregisterContentObserver(contentObserver);
    }


    /************************************************************************/
    /*    METHODS - broadcasts and notifications                            */
    /************************************************************************/

    // methods used to notify the Activity UI of something that has happened
    //  so that it can be updated to reflect status and the data received 
    //  from the server

    private void broadcastServiceStatus(String statusDescription)
    {
        // inform the app (for times when the Activity UI is running / 
        //   active) of the current MQTT connection status so that it 
        //   can update the UI accordingly
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MQTT_STATUS_INTENT);
        broadcastIntent.putExtra(MQTT_STATUS_MSG, statusDescription);
        sendBroadcast(broadcastIntent);
    }

    private void broadcastReceivedMessage(String topic, String message)
    {
        // pass a message received from the MQTT server on to the Activity UI 
        //   (for times when it is running / active) so that it can be displayed 
        //   in the app GUI
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MQTT_MSG_RECEIVED_INTENT);
        broadcastIntent.putExtra(MQTT_MSG_RECEIVED_TOPIC, topic);
        broadcastIntent.putExtra(MQTT_MSG_RECEIVED_MSG,   message);
        sendBroadcast(broadcastIntent);
    }

    // methods used to notify the user of what has happened for times when 
    //  the app Activity UI isn't running
    
    /*private void notifyUser(String alert, String title, String body)
    {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = new Notification(R.drawable.ic_launcher, alert,
                                                     System.currentTimeMillis());
        notification.defaults |= Notification.DEFAULT_LIGHTS;
        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;        
        notification.ledARGB = Color.MAGENTA;
        Intent notificationIntent = new Intent(this, MainWindow.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        
        //mast - temporary
        notification.setLatestEventInfo(this, title, body, contentIntent);
        notification.notify();
        
        nm.notify(MQTT_NOTIFICATION_UPDATE, notification);        
    }*/


    /************************************************************************/
    /*    METHODS - binding that allows access from the Actitivy            */
    /************************************************************************/

    // trying to do local binding while minimizing leaks - code thanks to
    //   Geoff Bruckner - which I found at  
    //   http://groups.google.com/group/cw-android/browse_thread/thread/d026cfa71e48039b/c3b41c728fedd0e7?show_docid=c3b41c728fedd0e7

    private LocalBinder<MQTTService> mBinder;

    @Override
    public IBinder onBind(Intent intent)
    {
        return mBinder;
    }
    public class LocalBinder<S> extends Binder
    {
        private WeakReference<S> mService;

        public LocalBinder(S service)
        {
            mService = new WeakReference<S>(service);
        }
        public S getService()
        {
            return mService.get();
        }
        public void close()
        {
            mService = null;
        }
    }

    // 
    // public methods that can be used by Activities that bind to the Service
    //

    public MQTTConnectionStatus getConnectionStatus()
    {
        return connectionStatus;
    }

    public void rebroadcastStatus()
    {
        String status = "";

        switch (connectionStatus)
        {
            case INITIAL:
                status = "Please wait";
                break;
            case CONNECTING:
                status = "Connecting...";
                break;
            case CONNECTED:
                status = "Connected";
                break;
            case NOTCONNECTED_UNKNOWNREASON:
                status = "Not connected - waiting for network connection";
                break;
            case NOTCONNECTED_USERDISCONNECT:
                status = "Disconnected";
                break;
            case NOTCONNECTED_DATADISABLED:
                status = "Not connected - background data disabled";
                break;
            case NOTCONNECTED_WAITINGFORINTERNET:
                status = "Unable to connect";
                break;
        }

        //
        // inform the app that the Service has successfully connected
        broadcastServiceStatus(status);
    }

    public void disconnect()
    {
        disconnectFromBroker();

        // set status 
        connectionStatus = MQTTConnectionStatus.NOTCONNECTED_USERDISCONNECT;
        connection.changeConnectionStatus(ConnectionStatus.DISCONNECTED);

        // inform the app that the app has successfully disconnected
        broadcastServiceStatus("Disconnected");
    }

    /************************************************************************/
    /*    METHODS - wrappers for some of the MQTT methods that we use       */
    /************************************************************************/
    
    /*
     * Create a client connection object that defines our connection to a
     *   message broker server 
     */
    private void defineConnectionToBroker(String brokerHostName)
    {

        //mast - remove all connections
        //Connections.getInstance(this).removeAllConnections();

        Persistence databasePersistence = new Persistence(this);
        //mast - added to avoid the bug that made 1000+ connections in the database
        if (databasePersistence.countConnections()>3) {
            databasePersistence.deleteAllConnections();
        }
        // mast -end



        conOpt = new MqttConnectOptions();

	    /*if (ssl) {
	      Log.e("SSLConnection", "Doing an SSL Connect");
	      uri = "ssl://";
	    }
	    else {
	      uri = "tcp://";
	    }		  
	    uri = uri + brokerHostName + ":" + brokerPortNumber;*/

        if (DEBUG) notifyUser2("Count before adding: "+connectRequestCount);
        connectRequestCount++;
        if (DEBUG) notifyUser2("Count after adding: "+connectRequestCount);

        String uri				= "tcp://" + brokerHostName + ":" + brokerPortNumber;
        boolean ssl				= false;

        try {

            boolean saveToDB = true;

            // create a client handle
            //clientHandle		= uri + mqttClientId;
            clientHandle		= "user"+mqttClientId;
            //reza_ak
            //Session.setClientHandle(clientHandle);
		
		/*if (Connections.getInstance(this).getConnection(clientHandle) != null)
		{
			connection = Connections.getInstance(this).getConnection(clientHandle);			
	        mqttClient = connection.getClient();			
			saveToDB = false;
		}
		

		if (mqttClient== null)
	        mqttClient	= Connections.getInstance(this).createClient(this, uri, mqttClientId);
		
		if (connection==null)
			connection	= new Connection(clientHandle, mqttClientId, brokerHostName, brokerPortNumber, this, mqttClient, ssl);			
		*/

            MemoryPersistence persistence = new MemoryPersistence();
            mqttClient = new MqttAndroidClient(this, uri, clientHandle, persistence);

            connection	= new Connection(clientHandle, mqttClientId, brokerHostName, brokerPortNumber, this, mqttClient, ssl);


            connection.registerChangeListener(changeListener);

            // connect client
            String[] actionArgs		= new String[1];
            actionArgs[0]			= mqttClientId;
            connection.changeConnectionStatus(ConnectionStatus.CONNECTING);
            connectionStatus = MQTTConnectionStatus.CONNECTING;

            conOpt.setCleanSession(cleanStart);
            //final ActionListener callback = new ActionListener(this, ActionListener.Action.CONNECT, clientHandle, actionArgs);

            mqttClient.setCallback(new MqttCallbackHandler(this, clientHandle));

            //set traceCallback
            mqttClient.setTraceCallback(new MqttTraceCallback());

            connection.addConnectionOptions(conOpt);


            // if (saveToDB)
            Connections.getInstance(this).addConnection(connection);

        }catch (Exception e) {
            //TODO: handle exception
            // something went wrong!
            mqttClient = null;
            connectionStatus = MQTTConnectionStatus.NOTCONNECTED_UNKNOWNREASON;
            connection.changeConnectionStatus(ConnectionStatus.DISCONNECTED);
        }

    }

    private class ChangeListener implements PropertyChangeListener{

        @Override
        public void propertyChange(PropertyChangeEvent event) {

            //Log.e("MQTT STATUS", event.toString());

            //if (connection.isConnected()) {}
            //if (connection.isConnectedOrConnecting()) {}

            //create intent to start activity
			
		    /*Intent intent = new Intent();
		    intent.setClassName(this, "org.eclipse.paho.android.service.sample.ConnectionDetails");
		    intent.putExtra("handle", clientHandle);

		    NotifySimple.notifcation(this,"MQTTService :"+ event.getSource() , intent, R.string.notifyTitle);*/

        }

    }

    public void notifyUser2(String message)
    {
        //create intent to start activity
        Intent intent = new Intent();
        intent.setClassName(getApplicationContext(), "org.eclipse.paho.android.service.sample.ConnectionDetails");
        intent.putExtra("handle", clientHandle);
        //notify all messages
        NotifySimple.notifcation(getApplicationContext(), message, intent, R.string.notifyTitle);

    }
    /*
     * (Re-)connect to the message broker
     */
    private boolean connectToBroker()
    {
        if (DEBUG) notifyUser2("connectToBroker called...");
        isConnectedtoMqtt = true;

        try {
            //String uri = "tcp://" + brokerHostName + ":" + brokerPortNumber;
            //mqttClient = Connections.getInstance(this).createClient(this, uri, mqttClientId);

            if (mqttClient!=null)
                mqttClient.connect(conOpt, null, new IMqttActionListener() {

                    @Override
                    public void onSuccess(IMqttToken arg0) {



                        if (DEBUG) notifyUser2("broker - success");

                        //reza_ak
                       /* boolean getTopics	= Session.getFirstRun();

                        if(!getTopics){

                            Session.setFirstRun(true);
                            //Mast - Restore rooms and subscribe job
                            jobManager.addJobInBackground(new RoomRestoreJob());


                        }
                        else {
                            //Subscribe to all group topics
                            jobManager.addJobInBackground(new SubscribeToAllTopicsJob());
                        }*/

                        //Subscribe to events topic
                        jobManager.addJobInBackground(new SubscribeToEventsJob());

                        // inform the app that the app has successfully connected
                        broadcastServiceStatus("Connected");

                        // we are connected
                        connection.changeConnectionStatus(ConnectionStatus.CONNECTED);
                        connectionStatus = MQTTConnectionStatus.CONNECTED;

                        // we need to wake up the phone's CPU frequently enough so that the
                        //  keep alive messages can be sent
                        // we schedule the first one of these now
                        scheduleNextPing();

                    }

                    @Override
                    public void onFailure(IMqttToken arg0, Throwable arg1) {
                        // TODO Auto-generated method stub
                        if (DEBUG) notifyUser2(" broker - failure");
                        if (connectRequestCount>0) {
                            connectRequestCount--;
                            if (DEBUG) notifyUser2("Count connectBroker failed: "+connectRequestCount);
                        }
                        isConnectedtoMqtt = false;
                        connection.changeConnectionStatus(ConnectionStatus.ERROR);
                        connectionStatus = MQTTConnectionStatus.NOTCONNECTED_UNKNOWNREASON;

                        if (arg1 instanceof MqttPersistenceException) {

                            if (((MqttPersistenceException) arg1).getReasonCode() == 32200) {
                                //if it is "Persistence already in use" error
                                NotifySimple.toast(getApplicationContext(), "P fail", Toast.LENGTH_SHORT);
							   
							    
							    
							    /*try {
							    	getApplicationContext().deleteDatabase(Persistence.DATABASE_NAME);
						            SQLiteDatabase db = openOrCreateDatabase(Persistence.DATABASE_NAME, MODE_PRIVATE, null);
				                    db.execSQL(Persistence.SQL_CREATE_ENTRIES);
				                    db.close();
							    } catch (Exception e) {
									// TODO: handle exception
								}*/


                            }
                        }
                        scheduleNextPing();

                    }
                });

            else {
                if (DEBUG) notifyUser2("connectToBroker mqttclient = null");
                Log.e("mqtt", "connectToBroker mqttclient = null");
                throw new IOException("connectToBroker mqttclient = null");
            }
        }catch (IllegalArgumentException e) {

            if (DEBUG) notifyUser2("invalid handle exception");
            Log.e("mqtt", "invalid handle exception");

        }catch (Exception e) {

            if (connectRequestCount>0) {
                connectRequestCount--;
                if (DEBUG) notifyUser2("Count connectBroker failed: "+connectRequestCount);
            }
            isConnectedtoMqtt = false;
            connection.changeConnectionStatus(ConnectionStatus.ERROR);
            connectionStatus = MQTTConnectionStatus.NOTCONNECTED_UNKNOWNREASON;
            //
            // inform the app that we failed to connect so that it can update
            //  the UI accordingly
            broadcastServiceStatus("Unable to connect");

            if (DEBUG) notifyUser2(" broker - exception ");
            //
            // inform the user (for times when the Activity UI isn't running)
            //   that we failed to connect
            notifyUser2("Unable to connect - will retry later");

            // if something has failed, we wait for one keep-alive period before
            //   trying again
            // in a real implementation, you would probably want to keep count
            //  of how many times you attempt this, and stop trying after a
            //  certain number, or length of time - rather than keep trying
            //  forever.
            // a failure is often an intermittent network issue, however, so
            //  some limited retry is a good idea
            scheduleNextPing();

            Log.e(this.getClass().getCanonicalName(), "MqttException Occured", e);
            return false;
        }


        if (!connection.isConnected()) return false;

        return mqttClient.isConnected();
    }


    /*
     * Terminates a connection to the message broker.
     */
    private void disconnectFromBroker()
    {
        // if we've been waiting for an Internet connection, this can be 
        //  cancelled - we don't need to be told when we're connected now
        try
        {
            if (netConnReceiver != null)
            {
                unregisterReceiver(netConnReceiver);
                netConnReceiver = null;
            }

            if (pingSender != null)
            {
                unregisterReceiver(pingSender);
                pingSender = null;
            }
        }
        catch (Exception eee)
        {
            // probably because we hadn't registered it
            Log.e("mqtt", "unregister failed", eee);
        }

        try
        {
            if (mqttClient != null)
            {
                mqttClient.disconnect();
            }
        }
        catch (MqttPersistenceException e)
        {
            Log.e("mqtt", "disconnect failed - persistence exception", e);
        }
        catch (MqttException e) {
            // TODO Auto-generated catch block
            Log.e("mqtt", "disconnect failed - persistence exception", e);
        }

        finally
        {
            mqttClient = null;
            isConnectedtoMqtt = false;
        }

        // we can now remove the ongoing notification that warns users that
        //  there was a long-running ongoing service running
        //mast - disabled
        //NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //nm.cancelAll();
    }

    /*
     * Checks if the MQTT client thinks it has an active connection
     */
    private boolean isAlreadyConnected()
    {
        return ((mqttClient != null) && (mqttClient.isConnected() == true));
    }

    private class BackgroundDataChangeIntentReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context ctx, Intent intent)
        {
            // we protect against the phone switching off while we're doing this
            //  by requesting a wake lock - we request the minimum possible wake 
            //  lock - just enough to keep the CPU running until we've finished
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MQTT");
            wl.acquire();
            try {
                ConnectivityManager cm = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
                if (cm.getBackgroundDataSetting())
                {
                    // user has allowed background data - we start again - picking
                    //  up where we left off in handleStart before
                    if (connectionStatus!=MQTTConnectionStatus.CONNECTING)
                    {
                        connectionStatus = MQTTConnectionStatus.CONNECTING;
                        connection.changeConnectionStatus(ConnectionStatus.CONNECTING);
                        defineConnectionToBroker(brokerHostName);
                        handleStart(intent, 0);
                    }
                }
                else
                {
                    // user has disabled background data
                    connectionStatus = MQTTConnectionStatus.NOTCONNECTED_WAITINGFORINTERNET;
                    connection.changeConnectionStatus(ConnectionStatus.DISCONNECTED);

                    // update the app to show that the connection has been disabled
                    broadcastServiceStatus("Not connected - background data disabled");

                    // disconnect from the broker
                    disconnectFromBroker();
                }
            } finally {

                // we're finished - if the phone is switched off, it's okay for the CPU
                //  to sleep now
                wl.release();
            }

        }
    }


    /*
     * Called in response to a change in network connection - after losing a 
     *  connection to the server, this allows us to wait until we have a usable
     *  data connection again
     */
    private class NetworkConnectionIntentReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context ctx, Intent intent)
        {
            // we protect against the phone switching off while we're doing this
            //  by requesting a wake lock - we request the minimum possible wake 
            //  lock - just enough to keep the CPU running until we've finished
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MQTT");
            wl.acquire();
            try {
                if (DEBUG) notifyUser2("network - net change");
                if (isOnline())
                {

                    // we have an internet connection - have another try at connecting
                    boolean doConnect = true;

                    try {
                        if (mqttClient!=null)
                            if (connectRequestCount>0) doConnect = false;
                    } catch (Exception e) {

                    } ;

                    if (doConnect)
                    {
                        if (DEBUG) notifyUser2("doConnect = true");
                        if (DEBUG) notifyUser2("connectRequestCount ="+connectRequestCount);

                        // assume the client connection is broken - trash it
                        try {

                            if (mqttClient != null) {
                                mqttClient.disconnect();
                            }
                            connectionStatus = MQTTConnectionStatus.NOTCONNECTED_USERDISCONNECT;
                            connection.changeConnectionStatus(ConnectionStatus.DISCONNECTED);
                        }
                        catch (Exception e1) {
                            Log.e("mqtt", "disconnect failed - exception", e1);
                        }


                        Intent intent2 = new Intent();
                        //if (mqttClient!=null)
                        //	if ((mqttClient.isConnected() || isConnectedtoMqtt==true)) doConnect = false;

                        connectionStatus = MQTTConnectionStatus.CONNECTING;
                        connection.changeConnectionStatus(ConnectionStatus.CONNECTING);
                        defineConnectionToBroker(brokerHostName);
                        handleStart(intent2, 0);
                        //connectToBroker();

                    }
                }
                else {
                    //mast - this will try to connect on ping interval even when no internet is not present
                    //mast - this is too much but we are testing :)
                    if (DEBUG) notifyUser2("no internet");
                    connectionStatus = MQTTConnectionStatus.NOTCONNECTED_WAITINGFORINTERNET;
                    connection.changeConnectionStatus(ConnectionStatus.DISCONNECTED);
                    isConnectedtoMqtt = false;
                    scheduleNextPing();
                }

            } finally {
                // we're finished - if the phone is switched off, it's okay for the CPU
                //  to sleep now
                wl.release();
            }


        }
    }


    /*
     * Schedule the next time that you want the phone to wake up and ping the 
     *  message broker server
     */
    private void scheduleNextPing()
    {
        // When the phone is off, the CPU may be stopped. This means that our 
        //   code may stop running.
        // When connecting to the message broker, we specify a 'keep alive' 
        //   period - a period after which, if the client has not contacted
        //   the server, even if just with a ping, the connection is considered
        //   broken.
        // To make sure the CPU is woken at least once during each keep alive
        //   period, we schedule a wake up to manually ping the server
        //   thereby keeping the long-running connection open
        // Normally when using this Java MQTT client library, this ping would be
        //   handled for us. 
        // Note that this may be called multiple times before the next scheduled
        //   ping has fired. This is good - the previously scheduled one will be
        //   cancelled in favour of this one.
        // This means if something else happens during the keep alive period, 
        //   (e.g. we receive an MQTT message), then we start a new keep alive
        //   period, postponing the next ping.

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,
                new Intent(MQTT_PING_ACTION),
                PendingIntent.FLAG_UPDATE_CURRENT);

        // in case it takes us a little while to do this, we try and do it 
        //  shortly before the keep alive period expires
        // it means we're pinging slightly more frequently than necessary 
        Calendar wakeUpTime = Calendar.getInstance();
        wakeUpTime.add(Calendar.SECOND, keepAliveSeconds);

        AlarmManager aMgr = (AlarmManager) getSystemService(ALARM_SERVICE);
        aMgr.set(AlarmManager.RTC_WAKEUP,
                wakeUpTime.getTimeInMillis(),
                pendingIntent);
    }


    /*
     * Used to implement a keep-alive protocol at this Service level - it sends 
     *  a PING message to the server, then schedules another ping after an 
     *  interval defined by keepAliveSeconds
     */
    public class PingSender extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            // Note that we don't need a wake lock for this method (even though
            //  it's important that the phone doesn't switch off while we're
            //  doing this).
            // According to the docs, "Alarm Manager holds a CPU wake lock as 
            //  long as the ic_add_post receiver's onReceive() method is executing.
            //  This guarantees that the phone will not sleep until you have 
            //  finished handling the broadcast."
            // This is good enough for our needs.

            //mast - TODO - disabled for now
            try
            {


                //mast - in new version of client there is no ping
                //we make a simple message sending to ourself instead to inform server that we are alive
                //mqttClient.ping();

                //mast - there is one class named
                //PingSender and ClientComms in new version of mqtt
                //but I still didn't test it
                //also TimerPingSender
                //ClientComms cc = new ClientComms(client, persistence, pingSender)
                //cc.checkForActivity();  --> is this new version of ping?
                //https://www.eclipse.org/paho/files/javadoc/org/eclipse/paho/client/mqttv3/internal/ClientComms.html#checkForActivity%28%29

                if (DEBUG) notifyUser2("ping to server");
                //reza_ak
                String userId	= "102015";
                        //SHAMChatApplication.getConfig().getUserId();
                String topic = "events/"+userId;
                String pingMessage = "ping";
                int qos = 1;
                boolean retained = false;

                String[] args = new String[2];
                args[0] = pingMessage;
                args[1] = topic+";qos:"+qos+";retained:"+retained;


                if (mqttClient!=null)
                {
                    mqttClient.publish(topic, pingMessage.getBytes(), 1, false, null, new IMqttActionListener() {

                        @Override
                        public void onSuccess(IMqttToken arg0) {
                            // TODO Auto-generated method stub
                            if (DEBUG) notifyUser2("ping publish success");
                            Log.e("mqtt", "ping publish success");
                        }

                        @Override
                        public void onFailure(IMqttToken arg0, Throwable arg1) {
                            // TODO Auto-generated method stub
                            //mast -what if publishing to server fails?
                            // if something goes wrong, it should result in connectionLost
                            //  being called, so we will handle it there
                            if (DEBUG) notifyUser2("ping publish failed");

		                Log.e("mqtt", "ping publish failed");


                            if (connectRequestCount!=0) {
                                if (DEBUG) notifyUser2("Count pingFailed zero: "+connectRequestCount);
                                connectRequestCount = 0;
                            }
                            // assume the client connection is broken - trash it
                            try {

                                mqttClient.disconnect();
                                connectionStatus = MQTTConnectionStatus.NOTCONNECTED_WAITINGFORINTERNET;
                                connection.changeConnectionStatus(ConnectionStatus.DISCONNECTED);
                            }
                            catch (Exception e1) {
                                //Log.e("mqtt", "disconnect failed - exception", e1);
                                ;
                            }

                            // reconnect
                            //if (connectToBroker()) {}
                            isConnectedtoMqtt = false;


                            Intent intent2 = new Intent();
                            boolean doConnect = true;
                            try {
                                if (connectRequestCount>0) doConnect = false;
                            } catch (Exception e) {
                                ;
                            }

                            if (doConnect)
                            {
                                connectionStatus = MQTTConnectionStatus.CONNECTING;
                                connection.changeConnectionStatus(ConnectionStatus.CONNECTING);
                                defineConnectionToBroker(brokerHostName);
                                handleStart(intent2, 0);
                                //connectToBroker();
                            }

                        }
                    });
                } else {
                    if (DEBUG) notifyUser2("ping mqttclient null");
                    Log.e("mqtt", "ping mqttclient null");
                    throw new IOException("ping mqttclient null");
                }

            }
            catch (Exception e)
            {
                // if something goes wrong, it should result in connectionLost
                //  being called, so we will handle it there
                if (DEBUG) notifyUser2("ping failed exception22");
                //Log.e("mqtt", "ping failed - MQTT exception", e);

                if (connectRequestCount!=0) {
                    if (DEBUG) notifyUser2("Count pingFailed zero: "+connectRequestCount);
                    connectRequestCount = 0;

                }
                // assume the client connection is broken - trash it
                try {

                    mqttClient.disconnect();
                    connectionStatus = MQTTConnectionStatus.NOTCONNECTED_WAITINGFORINTERNET;
                    connection.changeConnectionStatus(ConnectionStatus.DISCONNECTED);
                }
                catch (Exception e1) {
                    //Log.e("mqtt", "disconnect failed - exception", e1);
                    ;
                }

                // reconnect
                //if (connectToBroker()) {}
                isConnectedtoMqtt = false;

                Intent intent2 = new Intent();

                boolean doConnect = true;
                try {
                    if (connectRequestCount>0) doConnect = false;
                } catch (Exception e2) {
                    ;
                }

                //if (mqttClient!=null)
                //	if ((mqttClient.isConnected() || isConnectedtoMqtt==true)) doConnect = false;

                if (doConnect)
                {
                    connectionStatus = MQTTConnectionStatus.CONNECTING;
                    connection.changeConnectionStatus(ConnectionStatus.CONNECTING);
                    defineConnectionToBroker(brokerHostName);
                    handleStart(intent2, 0);
                    //connectToBroker();
                }

            }

            // start the next keep alive period 
            scheduleNextPing();
        }
    }



    /************************************************************************/
    /*   APP SPECIFIC - stuff that would vary for different uses of MQTT    */
    /************************************************************************/

    //  apps that handle very small amounts of data - e.g. updates and 
    //   notifications that don't need to be persisted if the app / phone
    //   is restarted etc. may find it acceptable to store this data in a 
    //   variable in the Service
    //  that's what I'm doing in this sample: storing it in a local hashtable 
    //  if you are handling larger amounts of data, and/or need the data to
    //   be persisted even if the app and/or phone is restarted, then 
    //   you need to store the data somewhere safely
    //  see http://developer.android.com/guide/topics/data/data-storage.html
    //   for your storage options - the best choice depends on your needs 

    // stored internally

    private Hashtable<String, String> dataCache = new Hashtable<String, String>();

    private boolean addReceivedMessageToStore(String key, String value)
    {
        String previousValue = null;

        if (value.length() == 0)
        {
            previousValue = dataCache.remove(key);
        }
        else
        {
            previousValue = dataCache.put(key, value);
        }

        // is this a new value? or am I receiving something I already knew?
        //  we return true if this is something new
        return ((previousValue == null) ||
                (previousValue.equals(value) == false));
    }

    // provide a public interface, so Activities that bind to the Service can 
    //  request access to previously received messages

    public void rebroadcastReceivedMessages()
    {
        Enumeration<String> e = dataCache.keys();
        while(e.hasMoreElements())
        {
            String nextKey = e.nextElement();
            String nextValue = dataCache.get(nextKey);

            broadcastReceivedMessage(nextKey, nextValue);
        }
    }


    public void onEventBackgroundThread(NewMessageEvent event) {

        String tId = event.getThreadId();
        String jsonMessageString = null;
        jsonMessageString = event.getJsonMessageString();
        //this event first goes to ChatInitialActivity and ChatActivity first
        //if they don't be present it mean user is not viewing them
        //so at last it comes here and we show notification to user

        if ((event.getConsumed() == false) && !(jsonMessageString == null))
        {

            NotifySimple.toast(getApplicationContext(), jsonMessageString,Toast.LENGTH_SHORT);

        }

    }

    /**
     * Send media (sendmedia) like image/video to server
     * @param event
     */
    public void onEventBackgroundThread(FileUploadingProgressEvent event) {

        Boolean isGroup = false;

        if (event.getThreadId().contains("g") || event.getThreadId().contains("ch")|| event.getThreadId().substring( event.getThreadId().indexOf("-")+1).startsWith("s"))  isGroup = true;

        if (event.getUploadedPercentage() == 100 && isGroup) {

            //reza_ak
           /*  Cursor cursor = null;

            cursor = getContentResolver().query(
                    ChatProviderNew.CONTENT_URI_CHAT,
                    null,
                    ChatMessage.THREAD_ID + "=? AND " + ChatMessage.PACKET_ID +"=?" ,
                    new String[] { event.getThreadId(), event.getPacketId() },null);

 chatProvider = new ChatProviderNew();


            if (chatProvider != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    final ChatMessage message = chatProvider.getChatMessageByCursor(cursor);

                    ContentResolver mContentResolver = getApplicationContext().getContentResolver();

                    if (message.getThreadId().equals(event.getThreadId())) {

                        final String uploadedUrl = message.getUploadedFileUrl();
                        //publishToTopic
                        User me = null;
                        FriendGroup group = null;

                        try {

                            Cursor cursorMe = mContentResolver.query(UserProvider.CONTENT_URI_USER,
                                    null, UserProvider.UserConstants.USER_ID + "=?",
                                    new String[] { SHAMChatApplication.getConfig().getUserId() },
                                    null);

                            cursorMe.moveToFirst();
                            me = UserProvider.userFromCursor(cursorMe);
                            cursorMe.close();


                            group = UserProvider.groupFromCursor(getContentResolver().query(
                                    UserProvider.CONTENT_URI_GROUP, null,
                                    FriendGroup.DB_ID + "=?", new String[] { message.getRecipient() }, null));

                        } catch (Exception e) {

                        }


                        JSONObject jsonMessageObject =  new JSONObject();
                        try {
                            jsonMessageObject.put("packet_type", "message");
                            jsonMessageObject.put("to", message.getRecipient());
                            jsonMessageObject.put("from", me.getMobileNo());
                            jsonMessageObject.put("from_userid", me.getUserId());
                            jsonMessageObject.put("messageBody", uploadedUrl);
                            jsonMessageObject.put("messageType",message.getMessageContentType().ordinal());
                            jsonMessageObject.put("messageTypeDesc",message.getDescription());
                            jsonMessageObject.put("timestamp",Utils.getTimeStamp());
                            jsonMessageObject.put("groupAlias",group.getName());
                            jsonMessageObject.put("latitude",message.getLatitude());
                            jsonMessageObject.put("longitude",message.getLongitude());
                            //jsonMessageObject.put("groupOwnerId",groupOwnerId);

                            jsonMessageObject.put("isGroupChat",1);
                            jsonMessageObject.put(ChatMessage.CHANNEL_VIEW , "1" );

                            jsonMessageObject.put("packetId",message.getPacketId());

                                jsonMessageObject.put(IncomingChannelMsg.ISFORWARDED ,message.getIsforward() ? 1  : 0 );
                                jsonMessageObject.put( IncomingChannelMsg.CHANNELTITLE , message.getChanneltitle() );
                                jsonMessageObject.put( IncomingChannelMsg.CHANNELTITLE  , message.getChanneltitle()  );
                                jsonMessageObject.put( IncomingChannelMsg.CHANNELHASHCODE  , message.getChannelhashcode() );







                            } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        //publishToTopic(jsonMessageObject.toString(),"groups/"+group.getId());


                        Log.d("jsonMessageObject.toString()" , jsonMessageObject.toString()) ;

                        if (message.getRecipient().startsWith("ch"))
                        {
                            jobManager.addJobInBackground(new PublishToTopicJob(jsonMessageObject.toString(), "channels/"+message.getRecipient()));
                        }else {
                            jobManager.addJobInBackground(new PublishToTopicJob(jsonMessageObject.toString(), "groups/"+message.getRecipient()));
                        }


                        //}
                    }
                }
            }

            cursor.close();*/

        }

    }

    /************************************************************************/
    /*    METHODS - internal utility methods                                */
    /************************************************************************/

    private String generateClientId()
    {
        // generate a unique client id if we haven't done so before, otherwise
        //   re-use the one we already have

        if (mqttClientId == null)
        {
            // generate a unique client ID - I'm basing this on a combination of 
            //  the phone device id and the current timestamp         
            String timestamp = "" + (new Date()).getTime();
            String android_id = Settings.System.getString(getContentResolver(),
                    Secure.ANDROID_ID);
            mqttClientId = timestamp + android_id;

            // truncate - MQTT spec doesn't allow client ids longer than 23 chars
            if (mqttClientId.length() > MAX_MQTT_CLIENTID_LENGTH) {
                mqttClientId = mqttClientId.substring(0, MAX_MQTT_CLIENTID_LENGTH);
            }
        }

        return mqttClientId;
    }

    private boolean isOnline()
    {
        try {


            ConnectivityManager cm = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
            if(cm.getActiveNetworkInfo() != null &&
                    cm.getActiveNetworkInfo().isAvailable() &&
                    cm.getActiveNetworkInfo().isConnected())
            {
                return true;
            }
        } catch (Exception e) {

        }

        return false;
    }


    /**
     * Handles call backs from the MQTT Client
     *
     */
    public class MqttCallbackHandler implements MqttCallback {

        /** {@link Context} for the application used to format and import external strings**/
        private Context context;
        /** Client handle to reference the connection that this handler is attached to**/
        private String clientHandle;

        private String CURRENT_USER_ID	= null;

        /**
         * Creates an <code>MqttCallbackHandler</code> object
         * @param context The application's context
         * @param clientHandle The handle to a {@link Connection} object
         */
        public MqttCallbackHandler(Context context, String clientHandle)
        {
            this.context = context;
            this.clientHandle = clientHandle;
            //reza_ak
            CURRENT_USER_ID	= "102015";
                    //SHAMChatApplication.getConfig().getUserId();
        }

        /**
         * @see org.eclipse.paho.client.mqttv3.MqttCallback#connectionLost(java.lang.Throwable)
         */
        @Override
        public void connectionLost(Throwable cause) {

            // we protect against the phone switching off while we're doing this
            //  by requesting a wake lock - we request the minimum possible wake
            //  lock - just enough to keep the CPU running until we've finished
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MQTT");
            wl.acquire();

            //
            // have we lost our data connection?
            //
            try {

                if (isOnline() == false)
                {
                    connectionStatus = MQTTConnectionStatus.NOTCONNECTED_WAITINGFORINTERNET;
                    connection.changeConnectionStatus(ConnectionStatus.DISCONNECTED);

                    // inform the app that we are not connected any more
                    broadcastServiceStatus("Connection lost - no network connection");

                    //
                    // inform the user (for times when the Activity UI isn't running)
                    //   that we are no longer able to receive messages
                    if (DEBUG)
                        notifyUser2("Connection lost - no network connection");

                    //if (connectRequestCount!=0) {
                    //  connectRequestCount = 0;
                    // 	if (DEBUG) notifyUser2("Count connectLost zero: "+connectRequestCount);
                    //}

                    //
                    // wait until the phone has a network connection again, when we
                    //  the network connection receiver will fire, and attempt another
                    //  connection to the broker
                }
                else
                {
                    //
                    // we are still online
                    //   the most likely reason for this connectionLost is that we've
                    //   switched from wifi to cell, or vice versa
                    //   so we try to reconnect immediately
                    //


                    connection.addAction("Connection Lost");

                    connectionStatus = MQTTConnectionStatus.NOTCONNECTED_UNKNOWNREASON;
                    connection.changeConnectionStatus(ConnectionStatus.DISCONNECTED);

                    // inform the app that we are not connected any more, and are
                    //   attempting to reconnect
                    broadcastServiceStatus("Connection lost - reconnecting...");

                    // try to reconnect
                    Intent intent2 = new Intent();
                    boolean doConnect = true;

                    try {
                        if (connectRequestCount>0) doConnect = false;
                    } catch (Exception e) {
                        ;
                    }

                    if (doConnect)
                    {
                        connectionStatus = MQTTConnectionStatus.CONNECTING;
                        connection.changeConnectionStatus(ConnectionStatus.CONNECTING);
                        //defineConnectionToBroker(brokerHostName);
                        //handleStart(intent2, 0);
                        //connectToBroker();
                    }
                    //if (connectToBroker()) {}

                }


            } finally {
                // we're finished - if the phone is switched off, it's okay for the CPU
                //  to sleep now
                wl.release();
            }


//    	  cause.printStackTrace();
        /*if (cause != null) {
          Connection c = Connections.getInstance(context).getConnection(clientHandle);
          c.addAction("Connection Lost");
          c.changeConnectionStatus(ConnectionStatus.DISCONNECTED);

          //format string to use a notification text
          Object[] args = new Object[2];
          args[0] = c.getId();
          args[1] = c.getHostName();

          String message = context.getString(R.string.connection_lost, args);

          //build intent
          Intent intent = new Intent();
          intent.setClassName(context, "org.eclipse.paho.android.service.sample.ConnectionDetails");
          intent.putExtra("handle", clientHandle);
          //notify the user
          NotifySimple.notifcation(context, message, intent, R.string.notifyTitle_connectionLost);
          
        }*/
        }

        /**
         * @see org.eclipse.paho.client.mqttv3.MqttCallback#messageArrived(java.lang.String, org.eclipse.paho.client.mqttv3.MqttMessage)
         */

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {

            // we protect against the phone switching off while we're doing this
            //  by requesting a wake lock - we request the minimum possible wake
            //  lock - just enough to keep the CPU running until we've finished
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MQTT");
            wl.acquire();



            Log.d("topic" ,  topic ) ;



            try {


                //Get connection object associated with this object
                //Connection c = Connections.getInstance(context).getConnection(clientHandle);

                //create arguments to format message arrived notifcation string
                String[] args = new String[2];
                args[0] = new String(message.getPayload(), "UTF-8");
                args[1] = topic+";qos:"+message.getQos()+";retained:"+message.isRetained();

                Log.e("ARRIVED MSG", new String(message.getPayload(), "UTF-8"));

                //get the string from strings.xml and format
                String messageString = context.getString(R.string.messageRecieved, (Object[]) args);

                //create intent to start activity
                Intent intent = new Intent();
                intent.setClassName(context, "org.eclipse.paho.android.service.sample.ConnectionDetails");
                intent.putExtra("handle", clientHandle);

                //format string args
                Object[] notifyArgs = new String[3];
                notifyArgs[0] = connection.getId();
                notifyArgs[1] = new String(message.getPayload(), "UTF-8");
                notifyArgs[2] = topic;

                //notify all messages if in debug mode
                if (DEBUG)
                    NotifySimple.notifcation(context, context.getString(R.string.notification, notifyArgs), intent, R.string.notifyTitle);

                String jsonMessageString = new String(message.getPayload(), "UTF-8");

                String packetType = null;
                if (jsonMessageString.equalsIgnoreCase("ping"))
                {
                    packetType = "ping";
                }
                else {
                    packetType = Utils.detectPacketType(jsonMessageString);
                }

                //if this is an event we received from server python script
                if (topic.contains("events/")) {
                    notifyArgs[1] = "received an event -  "+ "group?";
//    		doSubscribe(clientHandle,"groups/testgroup");


                    //if we are invited to a group - we save to add to friend_group, friend_group_members and chat_thread tables and then doSubscribe
                    //see process invitation --> smackableImp.java --> line 3727
                    // we can copy &paste processInvitation from smackableImp.java and use it



                    if (packetType.equals("invite") || packetType.equals("channel_invite")) {

                        processInvitation(jsonMessageString);
                        Log.e("INVITE", "PROCCESS INVITE CALLED");
                    }else if (packetType.equals("channel_update")) {


                        JSONObject js = new JSONObject(jsonMessageString).getJSONObject("topic") ;
                        String name , title , hashcode , description ;
                        name = js.getString("name") ;
                        title = js.getString("title") ;
                        hashcode = js.getString("hashcode") ;
                        description = js.getString("description") ;


                        ContentValues values = new ContentValues();
                        values.put(FriendGroup.DB_ID, hashcode);
                        values.put(FriendGroup.DB_NAME, title);
                        values.put(FriendGroup.CHAT_ROOM_NAME, hashcode);

                        if (js.has("logo")) {
                            if (!js.getString("logo").equals("")) {
                                values.put(FriendGroup.CHANNEL_LOGO,   js.getString("logo"));


                            }
                        }

                        if (hashcode.indexOf("ch") != -1) {
                            values.put(FriendGroup.DB_DESCRIPTION, description);
                            values.put(FriendGroup.DB_LINK_NAME, name);
                        }
//reza_ak
                      //  getApplicationContext().getContentResolver().update(UserProvider.CONTENT_URI_GROUP, values, FriendGroup.DB_ID + "=?", new String[]{hashcode});

                        EventBus.getDefault().postSticky(new NewMessageEvent());

                    }else if (packetType.equals("invite"))  {

                    }else if (packetType.equals("delete_channel")) {
                        KickFromChannel(jsonMessageString, true);

                    }
                    else if (packetType.equals("deletepost"))
                    {
                        DeleteChannelPost(jsonMessageString);

                    }

                    if (packetType.equals("left")) {
                        addLeftRoomToGroup(jsonMessageString);

                    }else if (packetType.equals("kick")){
                        if (!KickFromChannel(jsonMessageString, false) ) {
                            addKickedFromRoomToGroup(jsonMessageString);
                        }
                    }

                    else if (packetType.equals("ping")) {
                        Log.e("Ping received", "Ping received");
                    }

                    else if (packetType.equals("unknown")) {
                        Log.e("unknown received", "unknown packet type received");
                    }

//    		Notify.notifcation(context, context.getString(R.string.notification, notifyArgs), intent, R.string.notifyTitle);		
                }else { //if this is a group message

                    if (packetType.equals("message")){

                        String threadId = null;

                        JSONObject SampleMsg = null;
                        String packetId = null;
                        String from = null;
                        int fromUserId = 0;
                        String to=null;
                        String messageTypeDesc = null;
                        int messageType = 0;
                        String messageBody = null;
                        String timestamp = null;
                        //String groupOwnerId = null;
                        int isGroupChat = 0;
                        String latitude = null;
                        String longitude = null;

                        Boolean isChannel = false;
                        Boolean singleChat = false;

                        try {
                            SampleMsg = new JSONObject(jsonMessageString);
                            packetId = SampleMsg.getString("packetId");
                            from = SampleMsg.getString("from");
                            fromUserId = SampleMsg.getInt("from_userid");
                            to = SampleMsg.getString("to");
                            messageTypeDesc = SampleMsg.getString("messageTypeDesc");
                            timestamp = SampleMsg.getString("timestamp");
                            messageType = SampleMsg.getInt("messageType");
                            messageBody = SampleMsg.getString("messageBody");
                            //groupOwnerId = SampleMsg.getString("groupOwnerId");
                            isGroupChat = SampleMsg.getInt("isGroupChat");

                            if (SampleMsg.has("latitude"))
                                latitude = SampleMsg.getString("latitude");

                            if (SampleMsg.has("longitude"))
                                latitude = SampleMsg.getString("longitude");



                        } catch (JSONException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }





try {
    messageTelegram = new TLRPC.TL_updateShortMessage();

    messageTelegram.chat_id = 0;
    //messageTelegram.date = (int)Utils.DateToTimeStamp( timestamp);
    messageTelegram.date = 1472564180;
    messageTelegram.flags = 1;
    messageTelegram.from_id = 0;
    messageTelegram.id = 37;
    messageTelegram.message = messageBody;
    messageTelegram.out = false;
    messageTelegram.user_id = 107359676;
    messageTelegram.unread = false;
    messageTelegram.out = false;
    messageTelegram.mentioned = false;
    messageTelegram.media_unread = false;
    messageTelegram.silent = false;
    //messageTelegram.id = stream.readInt32(exception);
    messageTelegram.pts = 49;
    messageTelegram.pts_count = 1;
    messageTelegram.fwd_from = null;
    messageTelegram.via_bot_id = 0;
    messageTelegram.reply_to_msg_id = 0;

                     /*   if ((flags & 128) != 0) {
                            int magic = stream.readInt32(exception);
                            if (magic != 0x1cb5c415) {
                                if (exception) {
                                    throw new RuntimeException(String.format("wrong Vector magic, got %x", magic));
                                }
                                return;
                            }
                            int count = stream.readInt32(exception);
                            for (int a = 0; a < count; a++) {
                                MessageEntity object = MessageEntity.TLdeserialize(stream, stream.readInt32(exception), exception);
                                if (object == null) {
                                    return;
                                }
                                entities.add(object);
                            }
                        }*/
} catch (Exception e) {
    e.printStackTrace();
}


                        Utilities.stageQueue.postRunnable(new Runnable() {
                            @Override
                            public void run() {

                                MessagesController.getInstance().processUpdates((TLRPC.Updates) messageTelegram, false);
                            }
                        });
//reza_ak
                        //if it is my own packet just ignore it
                        /*
                        if (Utils.isMyOwnPacket(jsonMessageString)) return;

                        //notify the user
                        int messageContentType = Utils.detectMessageContentType(jsonMessageString);

                        addChatMessageToDB(MyMessageType.INCOMING_MSG.ordinal(), ChatMessage.MessageStatusType.QUEUED.ordinal(), messageContentType, jsonMessageString);

                        //here we can do something specific for each type of message like notification or other things
                        if (messageContentType == MessageContentType.TEXT.ordinal()) {}
                        else if (messageContentType == MessageContentType.IMAGE.ordinal()) {}
                        else if (messageContentType == MessageContentType.VIDEO.ordinal()) {}
                        else if (messageContentType == MessageContentType.STICKER.ordinal()) {}
                        else if (messageContentType == MessageContentType.LOCATION.ordinal()) {}
                        else if (messageContentType == MessageContentType.VOICE_RECORD.ordinal()) {}
                        else if (messageContentType == MessageContentType.MESSAGE_WITH_IMOTICONS.ordinal()) {}
                        */

                    }
                    //if user x is invited to group
                    //check friend_group_member first and if its userId doesn't exist we insert new record to it
                    //User addChatMessageToDB --> (saves to chat_message a new record with messageConteType of GROUP_INFO, save to chat_thread)
                    else if (packetType.equals("invite")) {

                        //String sampleInviteJson = "{\"topic\":{\"title\":\"Group 8\",\"sub_path\":\"groups/guBpJdoIreD\",\"cnt_members\":4,\"hashcode\":\"guBpJdoIreD\",\"admin\":{\"phone\":\"989357156909\",\"user_id\":14},\"id\":64},\"status\":200,\"packet_type\":\"invite\",\"target\":{\"phone\":\"989388389495\",\"user_id\":2},\"actor\":{\"phone\":\"989357156909\",\"user_id\":14}}";
                        //String sampleInviteJson = "{\"status\":200,\"target\":[{\"phone\":\"989195308970\",\"user_id\":70},{\"phone\":\"989195308971\",\"user_id\":71}],\"actor\":{\"phone\":\"989122335645\",\"user_id\":6},\"topic\":{\"title\":\"Group 18\",\"sub_path\":\"groups/gTotPtyMfrE\",\"cnt_members\":5,\"hashcode\":\"gTotPtyMfrE\",\"admin\":{\"phone\":\"989122335645\",\"user_id\":6},\"id\":61},\"packet_type\":\"invite\",\"packet_id\":\"packet-4-144147471370795\"}";
                        addInviteMessageToGroup(jsonMessageString);

                        //notifyArgs[1] = "user x joined room "+ packetType;
                        //Notify.notifcation(context, context.getString(R.string.notification, notifyArgs), intent, R.string.notifyTitle);
                    }
                    //if x has joined to group, we show a notification in group that x has joined group
                    // update friend_group_member --> DB_FRIEND_DID_JOIN of a user to 1
                    //update UI of group members listview
                    //smackableImp --> line 3855
                    //smackableImp --> line 2988
                    else if (packetType.equals("join"))  {
                        //there is no join in API script, "invite" is join actually
                    }

                    //if x has left group, we show a notification in group that x has left group
                    // update friend_group_member --> delete user from group list of members
                    //update UI of group members listview
                    //smackableImp --> line 2988

                    else if (packetType.equals("left")){
                        addLeftRoomToGroup(jsonMessageString);

                    }
                    //if x has been kicked from group, we show a notification in group that x has been kicked by admin
                    // update friend_group_member --> delete user from group list of members
                    //update UI of group members listview
                    //smackableImp --> line 2988
                    else if (packetType.equals("kick")){



                        if (!KickFromChannel(jsonMessageString, false) ) {
                            addKickedFromRoomToGroup(jsonMessageString);
                        }

                    }

                }


                //update client history - mqtt
                connection.addAction(messageString);

            } finally {

                // receiving this message will have kept the connection alive for us, so
                //  we take advantage of this to postpone the next scheduled ping
                scheduleNextPing();

                wl.release();
            }

        }

        /**
         * @see org.eclipse.paho.client.mqttv3.MqttCallback#deliveryComplete(org.eclipse.paho.client.mqttv3.IMqttDeliveryToken)
         */
        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            // Do nothing
        }


        /**
         *
         * @param direction  put 0 for outgoing message, 1 for incoming message OR
         * 				  MyMessageType.INCOMING_MSG.ordinal(), MyMessageType.OUTGOING_MSG.ordinal()
         * @param messageStatus queued = 0, sending = 1, sent = 2, delivered = 3, seen =4, failed = 5
         * @param messageContentType  text = 0, image = 1, sticker = 2, voice record = 3, favorite = 4,
         *  						  MESSAGE_WITH_IMOTICONS = 5, LOCATION = 6, INCOMING_CALL = 7, OUTGOING_CALL = 8, VIDEO = 9, GROUP_INFO = 10
         * @param jsonMessageString
         * @return
         */

        public boolean addChatMessageToDB(int direction, int messageStatus, int messageContentType,
                                          final String jsonMessageString) {

            boolean isExistingMessage = false;

            ContentResolver mContentResolver = getApplicationContext().getContentResolver();


            //mast - sample message to handle sent message and insert to db
            //String jsonSampleMsg = "{\"to\": \"/groups/testgroup\",\"from\": \"wizgoonId\",\"messageBody\": \"hello everyone!\",\"messageType\": 1,\"timestamp\": \"2014-03-07T00:00:00.000Z\",\"groupAlias\": \"Good friends group\"\"packetId\": \"userId_timestamp\"}";
            String threadId = null;

            JSONObject SampleMsg = null;
            String packetId = null;
            String from = null;
            int fromUserId = 0;
            String to=null;
            String messageTypeDesc = null;
            int messageType = 0;
            String messageBody = null;
            String timestamp = null;
            //String groupOwnerId = null;
            int isGroupChat = 0;
            String latitude = null;
            String longitude = null;

            Boolean isChannel = false;
            Boolean singleChat = false;

            try {
                SampleMsg = new JSONObject(jsonMessageString);
                packetId = SampleMsg.getString("packetId");
                from = SampleMsg.getString("from");
                fromUserId = SampleMsg.getInt("from_userid");
                to = SampleMsg.getString("to");
                messageTypeDesc = SampleMsg.getString("messageTypeDesc");
                timestamp = SampleMsg.getString("timestamp");
                messageType = SampleMsg.getInt("messageType");
                messageBody = SampleMsg.getString("messageBody");
                //groupOwnerId = SampleMsg.getString("groupOwnerId");
                isGroupChat = SampleMsg.getInt("isGroupChat");

                if (SampleMsg.has("latitude"))
                    latitude = SampleMsg.getString("latitude");

                if (SampleMsg.has("longitude"))
                    latitude = SampleMsg.getString("longitude");



            } catch (JSONException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }


            String groupId = to;
//reza_ak
            String threadOwner = "102015";
                    //SHAMChatApplication.getConfig().getUserId();

            //String threadId = threadId;
            //String groupId = null;
            String friendId = null;

            // Out going message

            //groupId = to;
            friendId = to;
            threadId = threadOwner + "-" + friendId;

            //mast - update message thread
            boolean threadSaveOrUpdateSuccess = saveOrUpdateThread(threadId, jsonMessageString, messageContentType, friendId, direction);


            if (threadSaveOrUpdateSuccess) {

                Cursor chatCursor = null;

//reza_ak
              /*  try {
                    //check to see if current message exists previously - ic_search using packetId of message

                    chatCursor = mContentResolver.query(
                            ChatProviderNew.CONTENT_URI_CHAT,
                            new String[] { MessageConstants._ID },
                            ChatMessage.PACKET_ID + "=?",
                            new String[] { packetId }, null);

                    if (chatCursor != null && chatCursor.getCount() > 0) {
                        isExistingMessage = true;
                    }

                    // If this is a totally new message
                    if (!isExistingMessage) {

                        ContentValues values = new ContentValues();

                        values.put(ChatMessage.MESSAGE_RECIPIENT, to);
                        //mast - put 0 for outgoing , 1 for incoming message
                        values.put(ChatMessage.MESSAGE_TYPE, direction);
                        values.put(ChatMessage.PACKET_ID, packetId);
                        values.put(ChatMessage.THREAD_ID, threadId);

                        values.put(ChatMessage.DESCRIPTION, messageTypeDesc);
                        values.put(ChatMessage.MESSAGE_DATETIME, timestamp);
                        values.put(ChatMessage.MESSAGE_CONTENT_TYPE, messageType);
                        values.put(ChatMessage.MESSAGE_STATUS, messageStatus);

                        //set posts view to 1
                        if (threadId.indexOf("-ch") != -1) {
                            values.put(ChatMessage.CHANNEL_ID  , "1");
                            values.put(ChatMessage.CHANNEL_VIEW  , "1");
                        }

                        if (isGroupChat == 1) {
                            values.put(ChatMessage.GROUP_ID, groupId);
                            if (groupId.startsWith("ch")){
                                isChannel = true;
                                singleChat=false;
                            }
                            else if(groupId.startsWith("g")) {
                                isChannel = false;
                                singleChat=false;
                            }
                            else if(groupId.startsWith("s")) {
                                isChannel = false;
                                singleChat=true;

                            }
                        }

                        if (SampleMsg.has("latitude"))
                            values.put(ChatMessage.LATITUDE,latitude);
                        if (SampleMsg.has("longitude"))
                            values.put(ChatMessage.LONGITUDE,longitude);


                        if (isGroupChat == 1
                                && direction == MyMessageType.INCOMING_MSG
                                .ordinal()) { // We
                            // handle
                            // Group messages slightly different compared to
                            // single chat
                            // We add "ABC says: " text in to certain messages
                            // group chat
                            // Then we need to manage the friends in the group

                            String fromGroup = groupId;

     								*//*String userId = fromGroup.substring(
     										fromGroup.indexOf("/") + 1,
     										fromGroup.indexOf("-"));

     								String username = getUsernameToDisplayForGroup(
     										userId, fromGroup);*//*
                            String userId = from;
                            //mast - will change later - currently it is phonenumber

                            String username = getContactNameFromPhone(getApplicationContext(),from);

                            // This is the actual sender, from value is
                            // the room name not the individual who sent it
                            values.put(ChatMessage.MESSAGE_SENDER, userId);

                            if (messageContentType != MessageContentType.GROUP_INFO.ordinal()) {
                                // Group, incoming, not group info

                                String formattedMessage = null;
                                if (messageContentType == MessageContentType.TEXT.ordinal()) {

                                    if (isChannel) {
                                        formattedMessage = messageBody;

                                    } else if (isChannel==false && singleChat==false) {
                                        formattedMessage = username
                                                + " "
                                                + ":"
                                                + " \n" + messageBody;
                                    }
                                    else if ( singleChat==true) {

                                       try {
                                           Cursor cursor2 = mContentResolver.query(UserProvider.CONTENT_URI_GROUP, null, FriendGroup.DB_ID + "=?", new String[]{groupId.toString()}, null);
                                           if (cursor2.getCount() <= 0) {
                                               ContentValues valuess = new ContentValues();
                                               valuess.put(FriendGroup.DB_ID, groupId.toString());                    // API name (HASH);
                                               valuess.put(FriendGroup.DB_NAME, from);

                                               // Group alias;
                                               valuess.put(FriendGroup.DB_RECORD_OWNER, threadOwner);                // User PhoneNumber;
                                               valuess.put(FriendGroup.CHAT_ROOM_NAME, groupId.toString());            // API name (HASH);
                                               //   mContentResolver.delete(UserProvider.CONTENT_URI_GROUP,FriendGroup.DB_ID+" = '"+  groupId.toString()+"'", null);
                                               mContentResolver.insert(UserProvider.CONTENT_URI_GROUP, valuess);

                                           }
                                           cursor2.close();
                                       }
                                       catch (Exception e)
                                       {}
                                        formattedMessage =  messageBody;
                                    }


                                } else {
                                    System.out
                                            .println("processMessage addChatMessageToDB messageContentType.getMessageContentType() != MessageContentType.TEXT");

                                    if (isChannel) {
                                        formattedMessage = " ";

                                    } else {

                                        formattedMessage = username
                                                + " "
                                                + context.getString(R.string.sent);

                                    }

                                    String body = messageBody;
                                    values.put(ChatMessage.UPLOADED_FILE_URL,
                                            body);

                                    if (body != null
                                            && body.contains("http://")) {
                                        try {
                                            URL u = new URL(body);
                                            int size = Utils.getFileSize(u);

                                            values.put(ChatMessage.FILE_SIZE,size);

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }

                                }
                                values.put(ChatMessage.TEXT_MESSAGE,
                                        formattedMessage);

                            } else { //

                            }

                        } else { //Group chat out going message*//*

                            System.out
                                    .println("processMessage addChatMessageToDB Single chat, both directions, Group chat outgoing");
                            values.put(ChatMessage.MESSAGE_SENDER, from);
                            values.put(ChatMessage.TEXT_MESSAGE, messageBody);


                        }


                                try {
                                    values.put( ChatMessage.ISFORWARD  , SampleMsg.getInt(IncomingChannelMsg.ISFORWARDED  )   );
                                    values.put(  ChatMessage.CHANNELTITLE , SampleMsg.getString( IncomingChannelMsg.CHANNELTITLE  ) );
                                    values.put( ChatMessage.CHANNELHASHCODE  , SampleMsg.getString( IncomingChannelMsg.CHANNELHASHCODE  )  );
                                    values.put( ChatMessage.ORGINALPACKETID  , SampleMsg.getString( IncomingChannelMsg.ORGINALPACKETID  ) );
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }






                                if (isChannel) {
                            values.put(ChatMessage.CHANNEL_ID  , "1");
                            values.put(ChatMessage.CHANNEL_VIEW  , "1");
                        }


                        Uri uri = mContentResolver.insert(
                                ChatProviderNew.CONTENT_URI_CHAT, values);

                        Cursor c = null;
                        int dbId = 0;
                        try {
                            c = mContentResolver.query(uri, null, null, null,
                                    null);

                            c.moveToFirst();

                            dbId = c.getInt(c.getColumnIndex("_id"));

                        } finally {
                            if (c != null) {
                                c.close();
                            }
                        }

                        NewMessageEvent newMessageEvent = new NewMessageEvent();
                        newMessageEvent.setThreadId(threadId);
                        newMessageEvent.setJsonMessageString(jsonMessageString);
                        if (direction == MyMessageType.OUTGOING_MSG.ordinal()) {

                            newMessageEvent
                                    .setDirection(MyMessageType.OUTGOING_MSG
                                            .ordinal());
                        } else {

                            newMessageEvent
                                    .setDirection(MyMessageType.INCOMING_MSG
                                            .ordinal());
                        }


                        EventBus.getDefault().postSticky(newMessageEvent);
                        System.out.println("Chat test chat message id " + dbId + " thread id " + threadId);

                    } else {
                        //if this message exists previously we add code required below (if required)
                    }

                } finally {
                    chatCursor.close();
                }*/
            }

            return isExistingMessage;

        }

        /**
         * Subscribes to a topic
         * @param clienthandle
         * @param topic
         */
        private void doSubscribe(String clienthandle, String topic) {

            Log.e("Subscribing to topic: ", topic);


            //find our own User object
            User me;
            ContentResolver mContentResolver = getApplicationContext().getContentResolver();
            //reza_ak
/*
            Cursor cursor = mContentResolver.query(UserProvider.CONTENT_URI_USER, null, UserProvider.UserConstants.USER_ID + "=?", new String[]{userIdPub}, null);
            cursor.moveToFirst();
            me = UserProvider.userFromCursor(cursor);
            cursor.close();*/

            String clientId = "102015";

            String[] actionArgs = new String[1];

            if (topic.startsWith("ch")) {
                actionArgs[0] = "channels/" + topic;
            }
            else {
                actionArgs[0] = "groups/" + topic;
            }

            final ActionListener callback = new ActionListener(getApplicationContext(),
                    ActionListener.Action.SUBSCRIBE, clientHandle, actionArgs);

            try {

                if (topic.startsWith("ch")) {
                    mqttClient.subscribe("channels/"+topic, 1, null, callback);
                }
                else {
                    mqttClient.subscribe("groups/"+topic, 1, null, callback);
                }

            }
            catch (MqttSecurityException e) {
                Log.e(this.getClass().getCanonicalName(), "Failed to subscribe to" + topic + " the client with the handle " + clientHandle, e);
                callback.onFailure(null, e);
            }
            catch (MqttException e) {
                Log.e(this.getClass().getCanonicalName(), "Failed to subscribe to" + topic + " the client with the handle " + clientHandle, e);
                callback.onFailure(null, e);
            }

        }

        /**
         * Process the group invitation received
         * @param jsonString
         */
        public void processInvitation(String jsonString) {

            Log.e("PROCCESS STRATED", "Done");
            Log.e("JSON", jsonString);

            try {

                String packetType;
                packetType = Utils.detectPacketType(jsonString);

                JSONObject result		= new JSONObject(jsonString);
                JSONObject topic = null;

                //if this is a channel invitation
                if (packetType.equals("channel_invite")) {
                    topic = result.getJSONObject("channel");
                }
                //if this is a group invitation
                else {
                    topic = result.getJSONObject("topic");
                }


                if (topic.has("is_join")){
                    if (topic.getBoolean("is_join")){
                        return;
                    }
                }



                String hashcode			= topic.getString("hashcode");
                String title			= topic.getString("title");

                JSONObject adminObject	= topic.getJSONObject("admin");
                String ownerId			= adminObject.getString("user_id");
                String description="";
                String linkName="";

                Boolean isChannel = false;
                if (hashcode.startsWith("ch")) {
                    isChannel = true;
                }

                JSONArray users=null;
                if (topic.has("users") && !isChannel) users= topic.getJSONArray("users");

                if (isChannel) {
                    description = topic.getString("description");
                    linkName =  topic.getString("name");
                }

                RokhPref	Session	= new RokhPref(getApplicationContext());
                String clientHandle	= Session.getClientHandle();

                ContentResolver mContentResolver = getApplicationContext().getContentResolver();
                String userId = userIdPub;

//reza_ak
               // Cursor groupCursor = mContentResolver.query( UserProvider.CONTENT_URI_GROUP, new String[] { FriendGroup.DB_ID }, FriendGroup.CHAT_ROOM_NAME + "=?", new String[] { hashcode }, null);

                boolean isUpdate = false;
//reza_ak
            /*    if (groupCursor.getCount() > 0) {
                    isUpdate = true;
                }*/

                String groupName = title;	// Group Alias
                String ownerID = ownerId;
//reza_ak
                //groupCursor.close();

                System.out.println(" invitation group name " + groupName);

                final FriendGroup group = new FriendGroup();
                group.setId(hashcode);
                group.setName(groupName);
                group.setRecordOwnerId(ownerID);
                group.setChatRoomName(hashcode);

                if (isChannel) {
                    group.setGroupDescription(description);
                    group.setGroupLinkName(linkName);
                }
                ContentValues values = new ContentValues();
                values.put(FriendGroup.DB_ID, hashcode);
                values.put(FriendGroup.DB_NAME, groupName);
                values.put(FriendGroup.DB_RECORD_OWNER, ownerID);
                values.put(FriendGroup.CHAT_ROOM_NAME, hashcode);


                if (topic.has("logo")) {
                    if (!topic.getString("logo").equals("")) {
                        values.put(FriendGroup.CHANNEL_LOGO,   topic.getString("logo"));


                    }
                }


                if(isChannel) {
                    values.put(FriendGroup.DB_DESCRIPTION, description);
                    values.put(FriendGroup.DB_LINK_NAME, linkName);
                }

                MessageThread thread = new MessageThread();
                thread.setFriendId(hashcode);
                thread.setGroupChat(true);
                thread.setThreadOwner(userId);

                ContentValues vals = new ContentValues();
                vals.put(MessageThread.THREAD_ID, thread.getThreadId());
                vals.put(MessageThread.FRIEND_ID, thread.getFriendId());
                vals.put(MessageThread.READ_STATUS, 0);
                //reza_ak
                //vals.put(MessageThread.LAST_UPDATED_DATETIME,Utils.formatDate(new Date().getTime(), "yyyy/MM/dd HH:mm:ss"));
                vals.put(MessageThread.IS_GROUP_CHAT, 1);
                vals.put(MessageThread.THREAD_OWNER, thread.getThreadOwner());
//reza_ak
               /* if (!isUpdate) {


                    mContentResolver.insert(UserProvider.CONTENT_URI_GROUP, values);

                    if(hashcode.startsWith("ch")) {
                        vals.put(MessageThread.LAST_MESSAGE, "new_channel_invited");
                    } else if(hashcode.startsWith("g")){
                        vals.put(MessageThread.LAST_MESSAGE, "new_group_invited");
                    }
                    else {
                        vals.put(MessageThread.LAST_MESSAGE, "");
                    }

                    vals.put(MessageThread.LAST_MESSAGE_DIRECTION, MyMessageType.INCOMING_MSG.ordinal());
                    vals.put(MessageThread.LAST_MESSAGE_CONTENT_TYPE, 0);


                    mContentResolver.insert(ChatProviderNew.CONTENT_URI_THREAD, vals);

                } else {

                    mContentResolver.update(UserProvider.CONTENT_URI_GROUP, values, FriendGroup.DB_ID + "=?", new String[] { group.getId() });
                    mContentResolver.update(ChatProviderNew.CONTENT_URI_THREAD, vals, MessageThread.THREAD_ID + "=?", new String[] { thread.getThreadId() });
                }*/
                //
                // requestAndUpdateParticpants(group,list);

                // add admin as member
                FriendGroupMember admin = new FriendGroupMember(group.getId(), ownerID);
                admin.assignUniqueId(userId);

                ContentValues adminCv = new ContentValues();
                adminCv.put(FriendGroupMember.DB_ID, admin.getId());
                adminCv.put(FriendGroupMember.DB_FRIEND, admin.getFriendId());
                adminCv.put(FriendGroupMember.DB_GROUP, admin.getGroupID());
                adminCv.put(FriendGroupMember.DB_FRIEND_IS_ADMIN, 1);
                adminCv.put(FriendGroupMember.PHONE_NUMBER, admin.getPhoneNumber());
//reza_ak
                /*
                if (mContentResolver.update(UserProvider.CONTENT_URI_GROUP_MEMBER, adminCv, FriendGroupMember.DB_GROUP + "=? AND " + FriendGroupMember.DB_FRIEND + "=?", new String[] { admin.getGroupID(), ownerID }) == 0) {
                    mContentResolver.insert(UserProvider.CONTENT_URI_GROUP_MEMBER, adminCv);
                }*/
                //if this is a group we save list of group members
                if (!isChannel) {
                    if (users.length() > 0) {

                        for (int i = 0; i < users.length(); i++) {

                            JSONObject Item = users.getJSONObject(i);
                            String memberId = Item.getString("user_id");
                            String memberPhone = Item.getString("phone");
                            boolean isAdmin = Item.getBoolean("is_admin");

                            //add members
                            FriendGroupMember members = new FriendGroupMember(group.getId(), memberId);

                            members.assignUniqueId(userId);

                            ContentValues groupMember = new ContentValues();
                            groupMember.put(FriendGroupMember.DB_ID, members.getId());
                            groupMember.put(FriendGroupMember.DB_FRIEND, members.getFriendId());
                            groupMember.put(FriendGroupMember.DB_GROUP, members.getGroupID());
                            groupMember.put(FriendGroupMember.PHONE_NUMBER, memberPhone);

                            if (isAdmin) {
                                groupMember.put(FriendGroupMember.DB_FRIEND_IS_ADMIN, 1);
                            }
                            groupMember.put(FriendGroupMember.DB_FRIEND_DID_JOIN, 1);
//reza_ak
/*
                            if (mContentResolver.update(UserProvider.CONTENT_URI_GROUP_MEMBER, groupMember, FriendGroupMember.DB_GROUP + "=? AND " + FriendGroupMember.DB_FRIEND + "=?", new String[]{members.getGroupID(), memberId}) == 0) {
                                mContentResolver.insert(UserProvider.CONTENT_URI_GROUP_MEMBER, groupMember);
                            }*/

                        }

                    }
                }
                //subscribe to group topic in mqtt
                doSubscribe(clientHandle, hashcode);

                //send a new message event so the ChatThreadFragment updates itself
                NewMessageEvent newMessageEvent = new NewMessageEvent();
                newMessageEvent.setThreadId(thread.getThreadId());

                newMessageEvent.setDirection(MyMessageType.INCOMING_MSG.ordinal());
                EventBus.getDefault().postSticky(newMessageEvent);


            } catch (JSONException e) {
                e.printStackTrace();
            }


        }

        private boolean saveOrUpdateThread(String threadId, String receivedJsonMessage, int messageContentType, String friendId, int direction) {


            ContentResolver mContentResolver = getApplicationContext()
                    .getContentResolver();
            // Pars Json String

            JSONObject SampleMsg=null;
            String packetId = null;
            String from = null;
            int fromUserId = 0;
            String to=null;
            String messageTypeDesc = null;
            int messageType = 0;
            String messageBody = null;
            //String groupOwnerId = null;
            int isGroupChat = 0;


            try {
                SampleMsg		= new JSONObject(receivedJsonMessage);
                packetId		= SampleMsg.getString("packetId");
                from			= SampleMsg.getString("from");
                fromUserId		= SampleMsg.getInt("from_userid");
                to				= SampleMsg.getString("to");
                messageTypeDesc	= SampleMsg.getString("messageTypeDesc");
                messageType		= SampleMsg.getInt("messageType");
                messageBody		= SampleMsg.getString("messageBody");
                //groupOwnerId	= SampleMsg.getString("groupOwnerId");
                isGroupChat		= SampleMsg.getInt("isGroupChat");


            } catch (JSONException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            // End Pars

            Cursor threadCursor = null;
            //reza_ak
           /* try {
                threadCursor = mContentResolver.query(
                        ChatProviderNew.CONTENT_URI_THREAD,
                        new String[] { MessageThread.THREAD_ID },
                        MessageThread.THREAD_ID + "=?", new String[] { threadId },
                        null);

                boolean isExistingThread = false;
                if (threadCursor != null && threadCursor.getCount() > 0) {

                    System.out.println("processMessage addChatMessageToDB saveOrUpdateThread isExisting thread");
                    isExistingThread = true;

                }

                String lastMessage = messageBody;
                boolean isRead = false;


                if (messageContentType != MessageContentType.GROUP_INFO.ordinal()) {

                    System.out.println("processMessage addChatMessageToDB saveOrUpdateThread NOT group info");
//reza_ak
                  *//*  switch (Utils.readMessageContentType(messageType)) {
                        case TEXT:
                            int limit;
                            if (messageBody.length()>70) limit = 70;
                            else limit = messageBody.length();

                            lastMessage = messageBody.substring(0,limit);
                            break;

                        case IMAGE:

                            lastMessage = "received_image";
                            break;

                        case VIDEO:
                            lastMessage = "received_video";
                            break;

                        case STICKER:
                            lastMessage = "received_sticker";
                            break;


                        case LOCATION:
                            lastMessage = "received_location";
                            break;

                        case VOICE_RECORD:
                            lastMessage = "received_voice";
                            break;

                        default:
                            System.out.println("processMessage addChatMessageToDB saveOrUpdateThread DEFAULT"+ lastMessage);
                            break;

                    }*//*

                }



                if (isExistingThread) {

                    ContentValues values = new ContentValues();
                    values.put(MessageThread.LAST_MESSAGE, lastMessage.trim());
                    values.put(MessageThread.LAST_MESSAGE_CONTENT_TYPE,messageContentType);
                    values.put(MessageThread.READ_STATUS, isRead ? 1 : 0);
                    values.put(MessageThread.LAST_UPDATED_DATETIME,Utils.formatDate(new Date().getTime(),"yyyy/MM/dd HH:mm:ss"));
                    values.put(MessageThread.LAST_MESSAGE_DIRECTION, direction);
                    values.put(MessageThread.THREAD_OWNER, userIdPub);
                    values.put(MessageThread.IS_GROUP_CHAT,isGroupChat);
                    mContentResolver.update(ChatProviderNew.CONTENT_URI_THREAD, values, MessageThread.THREAD_ID + "=?", new String[] { threadId });
                } else {
                    System.out
                            .println("processMessage addChatMessageToDB saveOrUpdateThread new thread "
                                    + lastMessage);
                    ContentValues values = new ContentValues();
                    values.put(MessageThread.THREAD_ID, threadId);
                    values.put(MessageThread.FRIEND_ID, friendId);
                    values.put(MessageThread.READ_STATUS, isRead ? 1 : 0);
                    values.put(MessageThread.LAST_MESSAGE, lastMessage.trim());
                    values.put(MessageThread.LAST_MESSAGE_CONTENT_TYPE,messageContentType);
                    values.put(MessageThread.LAST_UPDATED_DATETIME, Utils.formatDate(new Date().getTime(), "yyyy/MM/dd HH:mm:ss"));
                    values.put(MessageThread.IS_GROUP_CHAT, isGroupChat);
                    values.put(MessageThread.THREAD_OWNER, userIdPub);
                    values.put(MessageThread.LAST_MESSAGE_DIRECTION, direction);

                    mContentResolver.insert(ChatProviderNew.CONTENT_URI_THREAD,
                            values);
                }

            } finally {
                threadCursor.close();
            }*/
            return true;
        }

        /**
         * Adds new user to list of group members and also adds a new message "user x invited to room"
         * @param jsonMessageString
         * @return
         */
        private boolean addInviteMessageToGroup (String jsonMessageString)
        {

            // Conference service
            // g18261_20150618235025@conference.rabtcdn.com/+987735065830

            ContentResolver mContentResolver = getApplicationContext().getContentResolver();

            JSONObject MessageObject = null;
            JSONObject topicObject = null;
            JSONObject actorObject = null;

            JSONArray invitedUsers = null;
            String hashcode = null;

            String newMemberPhone = null;
            int newMemberUserId = 0;

            String actorPhone = null;
            String packetId = null;
            int actorUserId = 0;



            try {
                MessageObject = new JSONObject(jsonMessageString);
                topicObject = MessageObject.getJSONObject("topic");
                invitedUsers = MessageObject.getJSONArray("target");
                actorObject = MessageObject.getJSONObject("actor");


                hashcode = topicObject.getString("hashcode");

                actorPhone = actorObject.getString("phone");
                actorUserId = actorObject.getInt("user_id");

                //Mast - TODO - the packetId should come from python script

                //packetId = Utils.makePacketId(String.valueOf(actorUserId));
                packetId = MessageObject.getString("packet_id");

            } catch (JSONException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            //String username = newMemberPhone;
            String roomname = null;
            String threadId = null;

            roomname = hashcode;
            threadId = userIdPub + "-" + roomname;

            String userId=null;

            //mast - insert all invited users to group users list
            int i;
            JSONObject item=null;
            for(i=0;i<invitedUsers.length();i++){
                try {
                    item = invitedUsers.getJSONObject(i);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


                try {
                    newMemberPhone = item.getString("phone");
                    newMemberUserId = item.getInt("user_id");
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                userId = String.valueOf(newMemberUserId) ;

////reza_ak
               /* Cursor cursor = mContentResolver.query(
                        UserProvider.CONTENT_URI_GROUP_MEMBER,
                        new String[] { FriendGroupMember.DB_ID },
                        FriendGroupMember.DB_FRIEND + "=? AND "
                                + FriendGroupMember.DB_GROUP + "=?"
                        , new String[] { userId, roomname}, null);*/

                //if invited user doesn't exists previously then insert it to database
                //reza_ak
              /*  if (cursor.getCount() == 0) {

                    //insert new users to list of group members
                    FriendGroupMember newUser = new FriendGroupMember(roomname, userId);
                    newUser.assignUniqueId(userIdPub);

                    ContentValues vals = new ContentValues();
                    vals.put(FriendGroupMember.DB_ID, newUser.getId());
                    vals.put(FriendGroupMember.DB_FRIEND,newUser.getFriendId());
                    vals.put(FriendGroupMember.DB_GROUP,newUser.getGroupID());
                    vals.put(FriendGroupMember.PHONE_NUMBER,newMemberPhone);
                    vals.put(FriendGroupMember.DB_FRIEND_DID_JOIN, "1");

                    //first try to update and if not exists then insert new record to friend group members table

                    //reza_ak
                    *//*   if (mContentResolver.update(
                            UserProvider.CONTENT_URI_GROUP_MEMBER, vals,
                            FriendGroupMember.DB_GROUP + "=? AND "
                                    + FriendGroupMember.DB_FRIEND + "=?",
                            new String[] { roomname, userId }) == 0) {
                        mContentResolver
                                .insert(UserProvider.CONTENT_URI_GROUP_MEMBER,
                                        vals);
                    }*//*


                    //mast - username is phone number of new user
                    String message = "has_joined_the_room";

                    //update the chat thread
                    ContentValues values = new ContentValues();
                    values.put(MessageThread.LAST_MESSAGE, message);
                    values.put(MessageThread.LAST_MESSAGE_CONTENT_TYPE,
                            MessageContentType.GROUP_INFO.ordinal());
                    values.put(MessageThread.READ_STATUS, 0);
                    values.put(MessageThread.LAST_UPDATED_DATETIME, Utils
                            .formatDate(new Date().getTime(),
                                    "yyyy/MM/dd HH:mm:ss"));
                    values.put(MessageThread.LAST_MESSAGE_DIRECTION,
                            MyMessageType.INCOMING_MSG.ordinal());

//reza_ak
                  *//*  mContentResolver.update(
                            ChatProviderNew.CONTENT_URI_THREAD, values,
                            MessageThread.THREAD_ID + "=?",
                            new String[] { threadId });*//*

                    ContentValues chatmessageVals = new ContentValues();

                    //add the group info message that user x was invited to room
                    chatmessageVals.put(ChatMessage.MESSAGE_RECIPIENT,
                            userIdPub);
                    chatmessageVals.put(ChatMessage.MESSAGE_TYPE,
                            MyMessageType.INCOMING_MSG.ordinal());
                    chatmessageVals.put(ChatMessage.PACKET_ID,packetId);
                    chatmessageVals.put(ChatMessage.THREAD_ID, threadId);

                    chatmessageVals.put(ChatMessage.DESCRIPTION, "");
                    chatmessageVals.put(ChatMessage.MESSAGE_CONTENT_TYPE,
                            MessageContentType.GROUP_INFO.ordinal());
                    chatmessageVals.put(ChatMessage.MESSAGE_STATUS,
                            ChatMessage.MessageStatusType.SEEN.ordinal());
                    chatmessageVals.put(ChatMessage.FILE_SIZE, 0);

                    chatmessageVals.put(ChatMessage.GROUP_ID, roomname);

                    chatmessageVals.put(ChatMessage.MESSAGE_SENDER,
                            roomname);
                    chatmessageVals.put(ChatMessage.TEXT_MESSAGE, message);






//reza_ak
                   *//* if (mContentResolver.update(
                            ChatProviderNew.CONTENT_URI_CHAT,
                            chatmessageVals, ChatMessage.PACKET_ID + "=?",
                            new String[] { packetId }) == 0) {
                        mContentResolver.insert(
                                ChatProviderNew.CONTENT_URI_CHAT,
                                chatmessageVals);

                    }*//*

                }
                //if user already was invited and exists in database
                else {
                    //what to do ? nothing is not required
                }

                cursor.close();*/
            }



            //send newMessage event so Chat thread fragment and also chatInitialorGroupActivity update the UI
            //send a new message event so the ChatThreadFragment updates itself
            NewMessageEvent newMessageEvent = new NewMessageEvent();
            newMessageEvent.setThreadId(threadId);
            newMessageEvent.setJsonMessageString(jsonMessageString);
            newMessageEvent.setDirection(MyMessageType.INCOMING_MSG.ordinal());
            EventBus.getDefault().postSticky(newMessageEvent);

            //update list of group members UI
            EventBus.getDefault().postSticky(new UpdateGroupMembersList(threadId, roomname));

            return true;

        }
        /**
         * Handles when a user leaves or ourself leave the room, shows a group info message to user and deletes user from friend group members table
         * @param jsonMessageString
         * @return
         */

        private void DeleteChannelPost(String jsonMessageString) {
            try {

             JSONObject MessageObject = new JSONObject(jsonMessageString);
            String packetType = Utils.detectPacketType(jsonMessageString);
            JSONObject topicObject = null;

                topicObject = MessageObject.getJSONObject("channel");


            String packet_id = topicObject.getString("packet_id");


                jobManager.addJobInBackground(new DeleteChatMessageDBLoadJob(packet_id));
        }
        catch (Exception e)
        {}

        }



        private boolean addLeftRoomToGroup (String jsonMessageString){

            ContentResolver mContentResolver = getApplicationContext().getContentResolver();

            JSONObject MessageObject = null;
            JSONObject topicObject = null;
            JSONObject actorObject = null;

            String hashcode = null;

            String leftMemberPhone = null;
            int leftMemberUserId = 0;
            String packetId = null;

            try {
                MessageObject = new JSONObject(jsonMessageString);
                topicObject = MessageObject.getJSONObject("topic");
                actorObject = MessageObject.getJSONObject("actor");

                hashcode = topicObject.getString("hashcode");
                leftMemberPhone = actorObject.getString("phone");
                leftMemberUserId = actorObject.getInt("user_id");

                //Mast - TODO - the packetId should come from python script

                packetId = Utils.makePacketId(String.valueOf(leftMemberUserId));
                MessageObject.put("packet_id", packetId);
                jsonMessageString = MessageObject.toString();

            } catch (JSONException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            String groupId = hashcode;
            String userId = String.valueOf(leftMemberUserId);

            String threadId = userIdPub + "-" + groupId;

            //delete user from table of friend group members
            //reza_ak
            //mContentResolver.delete(UserProvider.CONTENT_URI_GROUP_MEMBER, FriendGroupMember.DB_GROUP + "=? AND "+ FriendGroupMember.DB_FRIEND + "=?", new String[] {groupId, userId });
            String displayMessage="";
            if(!groupId.substring(0,2).equals("ch")) {
                displayMessage = getContactNameFromPhone(getApplicationContext(), leftMemberPhone) + " " +"left_the_chat";

                //update the chat thread table to show message user x left the room
                ContentValues cvs = new ContentValues();
                cvs.put(MessageThread.LAST_MESSAGE, displayMessage);

                //reza_ak
                /*    mContentResolver.update(ChatProviderNew.CONTENT_URI_THREAD, cvs, MessageThread.THREAD_ID + "=?", new String[]{threadId});
            */
            }
            Log.w("leftuserid", String.valueOf(leftMemberUserId));
            Log.w("currentuserid", CURRENT_USER_ID);

            //if we ourself left the room
            if(String.valueOf(leftMemberUserId).equals(CURRENT_USER_ID)){

                //Mast - unsubscribe from the group we left
                RokhPref Session			= new RokhPref(getApplicationContext());
                clientHandle	= Session.getClientHandle();

                String topic = null;

                if (groupId.startsWith("ch"))
                {
                    topic	= "channels/"+groupId;
                } else {
                    topic	= "groups/"+groupId;
                }

                try {

                    mqttClient.unsubscribe(topic, getApplicationContext(), null);
                }
                catch (MqttSecurityException e) {
                    Log.e(this.getClass().getCanonicalName(), "Failed to unsubscribe to" + topic + " the client with the handle " + clientHandle, e);
                }
                catch (MqttException e) {
                    Log.e(this.getClass().getCanonicalName(), "Failed to unsubscribe to" + topic + " the client with the handle " + clientHandle, e);
                }
//reza_ak
         /*       mContentResolver.delete(ChatProviderNew.CONTENT_URI_THREAD, MessageThread.THREAD_ID + "=?", new String[] { threadId });
         */
            }


            //add user x left room just for rooms
            if (groupId.startsWith("ch")) {
                ContentValues chatmessageVals = new ContentValues();
                //add the group info message that user x left the room to chat messages inside group screen
                chatmessageVals.put(ChatMessage.MESSAGE_RECIPIENT,
                        userIdPub);
                chatmessageVals.put(ChatMessage.MESSAGE_TYPE,
                        MyMessageType.INCOMING_MSG.ordinal());
                chatmessageVals.put(ChatMessage.PACKET_ID, packetId);
                chatmessageVals.put(ChatMessage.THREAD_ID, threadId);

                chatmessageVals.put(ChatMessage.DESCRIPTION, "");
                chatmessageVals.put(ChatMessage.MESSAGE_CONTENT_TYPE,
                        MessageContentType.GROUP_INFO.ordinal());
                chatmessageVals.put(ChatMessage.MESSAGE_STATUS,
                        ChatMessage.MessageStatusType.SEEN.ordinal());
                chatmessageVals.put(ChatMessage.FILE_SIZE, 0);

                chatmessageVals.put(ChatMessage.GROUP_ID, groupId);

                chatmessageVals.put(ChatMessage.MESSAGE_SENDER,
                        groupId);
                chatmessageVals.put(ChatMessage.TEXT_MESSAGE, displayMessage);
//reza_ak
              /*  if (mContentResolver.update(
                        ChatProviderNew.CONTENT_URI_CHAT,
                        chatmessageVals, ChatMessage.PACKET_ID + "=?",
                        new String[]{packetId}) == 0) {
                    mContentResolver.insert(
                            ChatProviderNew.CONTENT_URI_CHAT,
                            chatmessageVals);
                }*/


                //send newMessage event so Chat thread fragment and also chatInitialorGroupActivity update the UI
                //send a new message event so the ChatThreadFragment updates itself
                NewMessageEvent newMessageEvent = new NewMessageEvent();
                newMessageEvent.setThreadId(threadId);
                newMessageEvent.setJsonMessageString(jsonMessageString);
                newMessageEvent.setDirection(MyMessageType.INCOMING_MSG.ordinal());
                newMessageEvent.setJsonMessageString(jsonMessageString);

                EventBus.getDefault().postSticky(newMessageEvent);
            }
            //update list of group members UI
            EventBus.getDefault().postSticky(new UpdateGroupMembersList(threadId, groupId));

            //if we were left from room ourself - close the chat screen
            if(String.valueOf(leftMemberUserId).equals(CURRENT_USER_ID)){
                EventBus.getDefault().postSticky(new CloseGroupActivityEvent(threadId, groupId));
            }

            return true;
        }

        /**
         * kicks user from channel
         * kikAll is used for delete channel, in this case all users get kicked
         * @param jsonMessageString
         * @param kickAll
         * @return
         */
        private boolean  KickFromChannel (String jsonMessageString, Boolean kickAll) {



            boolean isChannel = false ;

            Log.d("jsonMessageString" , jsonMessageString) ;

            try {

                JSONObject MessageObject = new JSONObject(jsonMessageString);
                String packetType = Utils.detectPacketType(jsonMessageString);
                JSONObject topicObject = null;

                if (packetType.equals("delete_channel"))
                    topicObject = MessageObject.getJSONObject("channel");
                else
                    topicObject = MessageObject.getJSONObject("topic");

                String hashcode = topicObject.getString("hashcode");
                String groupId = hashcode;

                int kickedMemberUserId = 0;
                String kickedMemberPhone = null;

                if (MessageObject.has("target")) {
                    JSONObject targetObject = MessageObject.getJSONObject("target");
                    kickedMemberPhone = targetObject.getString("phone");
                    kickedMemberUserId = targetObject.getInt("user_id");
                }

                String threadId = userIdPub+ "-" + groupId;

                isChannel = hashcode.indexOf("ch") != -1;
                if (isChannel) {

                    //if we ourself were kicked from room
                    // delete the chat thread and unsubscribe from group
                    if(String.valueOf(kickedMemberUserId).equals(CURRENT_USER_ID) || kickAll) {
//reza_ak
                    /*  getApplicationContext()
                                .getContentResolver()
                                .delete(ChatProviderNew.CONTENT_URI_CHAT,
                                        ChatMessage.THREAD_ID + "=?",
                                        new String[]{threadId});

                     getApplicationContext()
                                .getContentResolver()
                                .delete(ChatProviderNew.CONTENT_URI_THREAD,
                                        MessageThread.THREAD_ID + "=?",
                                        new String[]{threadId});*/

/*reza_ak

                        getApplicationContext()
                                .getContentResolver()
                                .delete(UserProvider.CONTENT_URI_GROUP,
                                        "did_join_room=? OR friend_group_id=?",
                                        new String[]{groupId, groupId});

                      getApplicationContext()
                                .getContentResolver()
                                .delete(UserProvider.CONTENT_URI_GROUP_MEMBER,
                                        "friend_group_id=? OR friend_id=?",
                                        new String[]{groupId, groupId});
*/


                        String topic = null;

                        if (groupId.startsWith("ch")) {
                            topic = "channels/" + groupId;
                            isChannel = true;
                        } else {
                            topic = "groups/" + groupId;
                        }


                        try {
                            mqttClient.unsubscribe(topic, getApplicationContext(), null);


                        } catch (MqttSecurityException e) {
                            Log.e(this.getClass().getCanonicalName(), "Failed to unsubscribe to" + topic + " the client with the handle " + clientHandle, e);
                        } catch (MqttException e) {
                            Log.e(this.getClass().getCanonicalName(), "Failed to unsubscribe to" + topic + " the client with the handle " + clientHandle, e);
                        }


                        EventBus.getDefault().postSticky(new NewMessageEvent());
                        EventBus.getDefault().postSticky(new CloseGroupActivityEvent(threadId, groupId));

                    }
                }
            }catch (JSONException e ){

            }

            return isChannel  ;
        }


        private boolean addKickedFromRoomToGroup (String jsonMessageString){

            //if x has been kicked from group, we show a notification in group that x has been kicked by admin
            // update friend_group_member --> delete user from group list of members
            // if we ourslef were kicked we delete the group and members and chat thread
            //update UI of group members listview
            //smackableImp --> line 2988

            ContentResolver mContentResolver = getApplicationContext().getContentResolver();

            JSONObject MessageObject = null;
            JSONObject topicObject = null;
            JSONObject actorObject = null;
            JSONObject targetObject = null;

            String hashcode = null;

            boolean isChannel = false ;

            String kickedMemberPhone = null;
            int kickedMemberUserId = 0;
            String packetId = null;

            try {
                MessageObject = new JSONObject(jsonMessageString);
                topicObject = MessageObject.getJSONObject("topic");
                hashcode = topicObject.getString("hashcode");
                actorObject = MessageObject.getJSONObject("actor");
                targetObject = MessageObject.getJSONObject("target");



                kickedMemberPhone = targetObject.getString("phone");
                kickedMemberUserId = targetObject.getInt("user_id");

                //Mast - TODO - the packetId should come from python script
                packetId = Utils.makePacketId(String.valueOf(kickedMemberUserId));
                MessageObject.put("packet_id", packetId);
                jsonMessageString = MessageObject.toString();

            } catch (JSONException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            String groupId = hashcode;
            String userId = String.valueOf(kickedMemberUserId);

            String threadId = userIdPub + "-" + groupId;

            //delete user from table of friend group members
            /*reza_ak
            mContentResolver.delete(UserProvider.CONTENT_URI_GROUP_MEMBER, FriendGroupMember.DB_GROUP + "=? AND "+ FriendGroupMember.DB_FRIEND + "=?", new String[] {groupId, userId });

*/
            String displayMessage = getContactNameFromPhone(getApplicationContext(),kickedMemberPhone) + " "+"kicked_from_chat";

            //update the chat thread table to show message user x left the room
            ContentValues cvs = new ContentValues();
            cvs.put(MessageThread.LAST_MESSAGE, displayMessage);

  //reza_ak
          /*  mContentResolver.update(ChatProviderNew.CONTENT_URI_THREAD, cvs, MessageThread.THREAD_ID + "=?", new String[] { threadId });
*/

            //if we ourself left the room  delete the chat thread and unsubscribe from group
            if(String.valueOf(kickedMemberUserId).equals(CURRENT_USER_ID)){

                displayMessage = "you_were_kicked_from_room";
                //Mast - unsubscribe from the group we are kicked from
                RokhPref Session			= new RokhPref(getApplicationContext());
                clientHandle	= Session.getClientHandle();
                String topic = null;

                if (groupId.startsWith("ch"))
                {
                    topic	= "channels/"+groupId;
                    isChannel  = true ;
                } else {
                    topic	= "groups/"+groupId;
                }


                try {
                    mqttClient.unsubscribe(topic, getApplicationContext(), null);


                }
                catch (MqttSecurityException e) {
                    Log.e(this.getClass().getCanonicalName(), "Failed to unsubscribe to" + topic + " the client with the handle " + clientHandle, e);
                }
                catch (MqttException e) {
                    Log.e(this.getClass().getCanonicalName(), "Failed to unsubscribe to" + topic + " the client with the handle " + clientHandle, e);
                }

  //reza_ak
                /*mContentResolver.delete(ChatProviderNew.CONTENT_URI_THREAD, MessageThread.THREAD_ID + "=?", new String[] { threadId });
*/

            }

            ContentValues chatmessageVals = new ContentValues();
            //add the group info message that user x left the room to chat messages inside group screen
            chatmessageVals.put(ChatMessage.MESSAGE_RECIPIENT,
                    userIdPub);
            chatmessageVals.put(ChatMessage.MESSAGE_TYPE,
                    MyMessageType.INCOMING_MSG.ordinal());
            chatmessageVals.put(ChatMessage.PACKET_ID,packetId);
            chatmessageVals.put(ChatMessage.THREAD_ID, threadId);

            chatmessageVals.put(ChatMessage.DESCRIPTION, "");
            chatmessageVals.put(ChatMessage.MESSAGE_CONTENT_TYPE,
                    MessageContentType.GROUP_INFO.ordinal());
            chatmessageVals.put(ChatMessage.MESSAGE_STATUS,
                    ChatMessage.MessageStatusType.SEEN.ordinal());
            chatmessageVals.put(ChatMessage.FILE_SIZE, 0);

            chatmessageVals.put(ChatMessage.GROUP_ID, groupId);

            chatmessageVals.put(ChatMessage.MESSAGE_SENDER,
                    groupId);
            chatmessageVals.put(ChatMessage.TEXT_MESSAGE, displayMessage);
//reza_ak
          /*  if (mContentResolver.update(
                    ChatProviderNew.CONTENT_URI_CHAT,
                    chatmessageVals, ChatMessage.PACKET_ID + "=?",
                    new String[] { packetId }) == 0) {
                mContentResolver.insert(
                        ChatProviderNew.CONTENT_URI_CHAT,
                        chatmessageVals);
            }*/

            //send newMessage event so Chat thread fragment and also chatInitialorGroupActivity update the UI
            //send a new message event so the ChatThreadFragment updates itself
            NewMessageEvent newMessageEvent = new NewMessageEvent();
            newMessageEvent.setThreadId(threadId);
            newMessageEvent.setJsonMessageString(jsonMessageString);
            newMessageEvent.setDirection(MyMessageType.INCOMING_MSG.ordinal());

            EventBus.getDefault().postSticky(newMessageEvent);

            //update list of group members UI
            EventBus.getDefault().postSticky(new UpdateGroupMembersList(threadId, groupId));

            //if we were kicked from room - close the chat screen
            if(String.valueOf(kickedMemberUserId).equals(CURRENT_USER_ID)){
                EventBus.getDefault().postSticky(new CloseGroupActivityEvent(threadId, groupId));
            }



            if (isChannel) {

                //reza_ak
                /*

                getApplicationContext()
                        .getContentResolver()
                        .delete(ChatProviderNew.CONTENT_URI_CHAT,
                                ChatMessage.THREAD_ID + "=?",
                                new String[]{threadId});

                getApplicationContext()
                        .getContentResolver()
                        .delete(ChatProviderNew.CONTENT_URI_THREAD,
                                MessageThread.THREAD_ID + "=?",
                                new String[]{threadId});



                getApplicationContext()
                        .getContentResolver()
                        .delete(UserProvider.CONTENT_URI_GROUP,
                                "did_join_room=? OR friend_group_id=?",
                                new String[]{groupId, groupId});

                getApplicationContext()
                        .getContentResolver()
                        .delete(UserProvider.CONTENT_URI_GROUP_MEMBER,
                                "friend_group_id=? OR friend_id=?",
                                new String[]{groupId,groupId});
*/

                EventBus.getDefault().postSticky(new NewMessageEvent());
                EventBus.getDefault().postSticky(new CloseGroupActivityEvent(threadId, groupId));
            }


            Log.d("lsdvsdvv" , "ddd") ;


            return true;

        }



        public void onEventMainThread(ChannelOnLeaveEvent event) {
            Log.d("dsssssdvsfdv" , "ss d " + event.groupId) ;
            if (event.groupId != null) {
                String topic = null;

                if (event.groupId.startsWith("ch")) {
                    topic = "channels/" + event.groupId;
                } else {
                    topic = "groups/" + event.groupId;
                }

                try {
                    mqttClient.unsubscribe(topic, getApplicationContext(), null);


                } catch (MqttSecurityException e) {
                    Log.e(this.getClass().getCanonicalName(), "Failed to unsubscribe to" + topic + " the client with the handle " + clientHandle, e);
                } catch (MqttException e) {
                    Log.e(this.getClass().getCanonicalName(), "Failed to unsubscribe to" + topic + " the client with the handle " + clientHandle, e);
                }

            }
        }


        /**
         * Resolves phonenumber to contact name using mobile phonebook, if not found returns phonenumber again
         * @param context
         * @param number
         * @return
         */
        private String getContactNameFromPhone(Context context, String number) {


            String name = null;
            String contactId = null;
            InputStream input = null;  //this will be contact photo as an input Stream

            // define the columns I want the query to return
            String[] projection = new String[] {
                    ContactsContract.PhoneLookup.DISPLAY_NAME,
                    ContactsContract.PhoneLookup._ID};

            // encode the phone number and build the filter URI
            Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

            // query time
            Cursor cursor = context.getContentResolver().query(contactUri, projection, null, null, null);

            if (cursor.moveToFirst()) {

                // Get values from contacts database:
                contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup._ID));
                name =      cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));

                // Get photo of contactId as input stream:
                Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(contactId));
                input = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(), uri);

                Log.v("ffnet", "Started : Contact Found @ " + number);
                Log.v("ffnet", "Started : Contact name  = " + name);
                Log.v("ffnet", "Started : Contact id    = " + contactId);

                cursor.close();
                //if found we return name of contact
                return name;
            } else {

                Log.v("ffnet", "Started : Contact Not Found @ " + number);

                cursor.close();
                //if not found we return phone number again
                return number; // contact not found

            }

            // for contact photo
    	  /*if (input == null) {
    	      Log.v("ffnet", "No photo found, id = " + contactId + " name = " + name);
    	      return contactId; // no photo
    	  } else {
    	      this.type = contactId;
    	      Log.v("ffnet", "Photo is found, id = " + contactId + " name = " + name);
    	  } */



        }

    }




}