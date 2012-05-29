package com.events;


import java.util.Calendar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;



public class Events extends ListActivity {
    private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;
    private static final int ACTIVITY_AUTORES=2;
    private static final int ACTIVITY_AYUDA=3;


    private static final int INSERT_ID = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 1;
    private static final int AUTORES_ID = Menu.FIRST + 2;
    private static final int AYUDA_ID = Menu.FIRST + 3;
    private static final int HELLO_ID = 1;
    
    public static EventsDbAdapter mDbHelper;
    private AlertDialog.Builder mBuilder;
    private MenuItem mItem;
    private int mRowIdVencido;
    private NotificationManager mNotificationManager;
    

    private Calendar ahora = Calendar.getInstance();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.events_list);
        mDbHelper = new EventsDbAdapter(this);
        mDbHelper.open();
        fillData();
        registerForContextMenu(getListView());
        if (comprobarVencimiento()) lanzarNotificacion();
    }

    private void fillData() {
        Cursor eventsCursor = mDbHelper.fetchAllEvents();
        startManagingCursor(eventsCursor);

        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[]{EventsDbAdapter.KEY_TITLE, EventsDbAdapter.KEY_DATE, EventsDbAdapter.KEY_TIME};

        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{R.id.text1, R.id.text2, R.id.text3};

        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter events = 
            new SimpleCursorAdapter(this, R.layout.events_row, eventsCursor, from, to);
        setListAdapter(events);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, INSERT_ID, 0, R.string.menu_insert);
        menu.add(0, AUTORES_ID, 0, R.string.titulo_autores);
        menu.add(0, AYUDA_ID, 0, R.string.titulo_ayuda);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
            case INSERT_ID:
                createEvent();
                return true;
            case AUTORES_ID:
                lanzarAutores();
                return true;
            case AYUDA_ID:
                lanzarAyuda();
                return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, R.string.menu_delete);
        menu.add(0, INSERT_ID, 0, R.string.edit_event);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case DELETE_ID:
            	mItem=item;
            	createDialog();
            	AlertDialog alert = mBuilder.create();
            	alert.show();  
                return true;
            case INSERT_ID:
            	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
            	Intent i = new Intent(this, EventEdit.class);
                i.putExtra(EventsDbAdapter.KEY_ROWID, info.id);
                startActivityForResult(i, ACTIVITY_EDIT);
            	return true;
        }
        return super.onContextItemSelected(item);
    }

    private void createEvent() {
        Intent i = new Intent(this, EventEdit.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }
    
    private void lanzarAutores() {
        Intent i = new Intent(this, MostrarAutores.class);
        startActivityForResult(i, ACTIVITY_AUTORES);
    }
    
    private void lanzarAyuda() {
        Intent i = new Intent(this, MostrarAyuda.class);
        startActivityForResult(i, ACTIVITY_AYUDA);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(this, EventEdit.class);
        i.putExtra(EventsDbAdapter.KEY_ROWID, id);
        startActivityForResult(i, ACTIVITY_EDIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillData();
    }
    
    private void createDialog() {
		mBuilder = new AlertDialog.Builder(this);
		mBuilder.setMessage("¿Estás seguro de borrar esta entrada?")
		       .setCancelable(false)
		       .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	 AdapterContextMenuInfo info = (AdapterContextMenuInfo) mItem.getMenuInfo();
		             mDbHelper.deleteEvent(info.id);
		             fillData();
		           }
		       })
		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       });	
	}
    
	private void lanzarNotificacion() {
		  String ns = Context.NOTIFICATION_SERVICE;
		  mNotificationManager = (NotificationManager) getSystemService(ns);
		  int icon = R.drawable.notification_icon;
		  CharSequence tickerText = "¡Eventos próximos!";
		  long when = System.currentTimeMillis();

		  Notification notification = new Notification(icon, tickerText, when);
		  
		  Context context = getApplicationContext();
		  CharSequence contentTitle = "Eventoss";
		  
		  Cursor evento = mDbHelper.fetchAllEvents();
		  startManagingCursor(evento);
		  evento.moveToPosition(mRowIdVencido);
		  CharSequence contentText = "¡Hoy tienes eventos, el primero es " + evento.getString(
                evento.getColumnIndexOrThrow(EventsDbAdapter.KEY_TITLE)) + "!";
		  
		  Intent notificationIntent = new Intent(this, Notificacion.class);
		  PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		  notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		  
		  

		  mNotificationManager.notify(HELLO_ID, notification);
	}
	
	private boolean comprobarVencimiento() {
	  Cursor event = mDbHelper.fetchAllEvents();
      startManagingCursor(event);
      while (event.moveToNext()) {
      String fechaEvento= event.getString(event.getColumnIndexOrThrow(EventsDbAdapter.KEY_DATE));
      String horaEvento = event.getString(event.getColumnIndexOrThrow(EventsDbAdapter.KEY_TIME));
      if ((fechaEvento.equals(getCurrentDate()))) {
      	mRowIdVencido = event.getPosition();
      	return true;
      }//(horaEvento.equals(getCurrentTime())) && (fechaEvento.equals(getCurrentDate()))
      }
      
		return false;
	}
	
	private static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }
	 
	
	 private String getCurrentTime() {
		return new StringBuilder()
	      .append(pad(ahora.get(Calendar.HOUR_OF_DAY))).append(":")
	      .append(pad(ahora.get(Calendar.MINUTE))).toString();
	 }
	 
	 private String getCurrentDate() {
		 String dev=new StringBuilder()                    
			// Month is 0 based so add 1                    
			.append(ahora.get(Calendar.YEAR)).append("/") 
			.append(ahora.get(Calendar.MONTH) + 1).append("/")
			.append(ahora.get(Calendar.DAY_OF_MONTH)).append(" ").toString();
			return dev;
	 }
	 
}