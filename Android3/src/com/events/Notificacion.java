package com.events;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class Notificacion extends IntentService {
	  /** 
	   * A constructor is required, and must call the super IntentService(String)
	   * constructor with a name for the worker thread.
	   */
	  public Notificacion() {
	      super("Notificacion");
	  }

	  /**
	   * The IntentService calls this method from the default worker thread with
	   * the intent that started the service. When this method returns, IntentService
	   * stops the service, as appropriate.
	   */
	  @Override
	  protected void onHandleIntent(Intent intent) {
	        
	        
		  String ns = Context.NOTIFICATION_SERVICE;
		  NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
		  int icon = R.drawable.notification_icon;
		  CharSequence tickerText = "Eventos";
		  long when = System.currentTimeMillis();

		  Notification notification = new Notification(icon, tickerText, when);
		  
		  Context context = getApplicationContext();
		  CharSequence contentTitle = "Eventos";
		  CharSequence contentText = "¡Hoy tienes eventos programados!";
		  Intent notificationIntent = new Intent(this, Notificacion.class);
		  PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		  notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		  
		  final int HELLO_ID = 1;

		  mNotificationManager.notify(HELLO_ID, notification);
	  }
	 
	}