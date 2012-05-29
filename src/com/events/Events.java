package com.events;




import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


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
    
    private EventsDbAdapter mDbHelper;
    private AlertDialog.Builder mBuilder;
    private MenuItem mItem;
    private int mRowIdVencido;
    private NotificationManager mNotificationManager;

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
		  CharSequence tickerText = "Préstamos";
		  long when = System.currentTimeMillis();

		  Notification notification = new Notification(icon, tickerText, when);
		  
		  Context context = getApplicationContext();
		  CharSequence contentTitle = "Préstamos";
		  
		  Cursor prestamo = mDbHelper.fetchAllEvents();
		  startManagingCursor(prestamo);
		  prestamo.moveToPosition(mRowIdVencido);
		  CharSequence contentText = "¡Hoy vence tu préstamo con " + prestamo.getString(
                prestamo.getColumnIndexOrThrow(EventsDbAdapter.KEY_TITLE)) + "!";
		  
		  Intent notificationIntent = new Intent(this, Notificacion.class);
		  PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		  notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		  
		  

		  mNotificationManager.notify(HELLO_ID, notification);
	}
	
	private boolean comprobarVencimiento() {
		Cursor prestamo = mDbHelper.fetchAllEvents();
      startManagingCursor(prestamo);
      while (prestamo.moveToNext()) {
      Date ahora = new Date();
      Date fechaPrestamo = convertToDate(prestamo.getString(prestamo.getColumnIndexOrThrow(EventsDbAdapter.KEY_DATE)));
      if (ahora.getDay() == fechaPrestamo.getDay()     && 
      	ahora.getMonth() == fechaPrestamo.getMonth() && 
      	ahora.getYear() == fechaPrestamo.getYear()) {
      	mRowIdVencido = prestamo.getPosition();
      	return true;
      }
      }
      
		return false;
	}
	
	private Date convertToDate(String dateString) { 

		SimpleDateFormat dateFormat = new SimpleDateFormat("y/M/d"); 

		Date convertedDate; 
		try { 
		convertedDate = dateFormat.parse(dateString); 
		} 
		catch (ParseException e) { 
		return null; 
		}
		return convertedDate; 
		} 
}