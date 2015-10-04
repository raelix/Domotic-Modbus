package com.domotica.raspimanager;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import com.google.android.gcm.GCMBaseIntentService;
public class GCMIntentService extends GCMBaseIntentService {
	public static String id = "681925213640";
	public static final String SENDER_ID = "681925213640";
	public static String reg ;
	public static String phone;
	public static String namegetted;
	SQLiteDatabase mydb;
	static String database = "logs";
	public static Activity activity;
	public GCMIntentService() {
		super(SENDER_ID);
	}

	private static final String TAG = "===GCMIntentService===";

	@Override
	protected void onRegistered(Context arg0, String registrationId) {
		Log.i(TAG, "Device registered: regId = " + registrationId);
		reg = registrationId;
	}

	@Override
	protected void onUnregistered(Context arg0, String arg1) {
		Log.i(TAG, "unregistered = " + arg1);
	}

	@Override
	protected void onMessage(Context context, Intent intent) {

		String message = intent.getExtras().getString("message");
		Log.i(TAG, "new message= "+message);
		generateNotification(context, message);
	}

	@Override
	protected void onError(Context arg0, String errorId) {
		Log.i(TAG, "Received error: " + errorId);
	}

	@Override
	protected boolean onRecoverableError(Context context, String errorId) {
		return super.onRecoverableError(context, errorId);
	}

	/**
	 * Si supporrà che esisteranno diversi tipi di ack : 
	 * 1-Notifica l'avvenuta registrazione e lo inserisce nel file xml,					ack &1 &usr &idkey
	 * 2-notifica l'avvenuta ricezione di un sms contenente il numero del mittente ,    ack &2 &mittenteNumero &messaggio
	 * 3-contiene il primo ack dal destinatario, 										ack &3 &destinatario
	 * 4-contiene il secondo ed ultimo ack dal destinatario  							ack &4 &destinatario
	 */
	private  void generateNotification(Context context, String message) {

		
			generaNotifica(context,message);
		
	}



	private static void generaNotifica(Context context,String message){
//		int icon = R.drawable.ic_launcher;
//		CharSequence tickerText = message;
//		long when = System.currentTimeMillis();
//		CharSequence contentTitle = ""+message; 
//		NotificationManager notificationManager =(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//		Notification notification = new Notification(icon, tickerText, when);
//		Intent notificationIntent = new Intent(context, MainActivity.class);
//		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
//		notification.setLatestEventInfo(context, contentTitle, "", pendingIntent);
//		notification.flags|=Notification.FLAG_ONLY_ALERT_ONCE|Notification.FLAG_AUTO_CANCEL;
//		notification.defaults |= Notification.DEFAULT_SOUND|Notification.DEFAULT_LIGHTS;
//		notification.vibrate=new long[] {100L, 100L, 200L, 500L};
//		notificationManager.notify(1, notification);
	
//		RemoteViews remoteViews = new RemoteViews(context.getPackageName(),  
//                R.layout.notify);  
      NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(  
                context).setSmallIcon(R.drawable.ic_launcher).setContentTitle("RaspiServer")
                .setContentText(message).setVibrate(new long[] {100L, 100L, 200L});  
      // Creates an explicit intent for an Activity in your app  
      Intent resultIntent = new Intent(context, MainActivity.class);  
      // The stack builder object will contain an artificial back stack for  
      // the  
      // started Activity.  
      // This ensures that navigating backward from the Activity leads out of  
      // your application to the Home screen.  
      TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);  
      // Adds the back stack for the Intent (but not the Intent itself)  
      stackBuilder.addParentStack(MainActivity.class);  
      
      // Adds the Intent that starts the Activity to the top of the stack  
      stackBuilder.addNextIntent(resultIntent);  
      NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);  
      // mId allows you to update the notification later on.  
      mNotificationManager.notify(1, mBuilder.build()); 
		
		
//		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
//		WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
//		wl.acquire();
	}
	
	
}
