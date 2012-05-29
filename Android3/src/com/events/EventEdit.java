package com.events;


import java.util.Calendar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;





public class EventEdit extends Activity {
	////////////////////////////////////////////////////////////////////////////////////////////
    private Button mPickDate;    
    private int mYear;    
    private int mMonth;    
    private int mDay;    
    static final int DATE_DIALOG_ID = 0;
    private Calendar c = Calendar.getInstance();   
////////////////////////////////////////////////////////////////////////////////////////////////////
    private EditText mTitleText;
    private Button mTimeText;
    private EditText mContactText;
    private int mHour;
    private int mMinute;
    static final int TIME_DIALOG_ID=1;
    private static final int ACTIVITY_CONTACT=0;
    private Long mRowId;
    private EventsDbAdapter mDbHelper;
    private AlertDialog.Builder mBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new EventsDbAdapter(this);
        mDbHelper.open();
        
        setContentView(R.layout.event_edit);
        setTitle(R.string.edit_event);
        mTitleText = (EditText) findViewById(R.id.title);
        mTimeText = (Button) findViewById(R.id.time);
        mContactText = (EditText) findViewById(R.id.contact);

        Button contactButton = (Button) findViewById(R.id.contact_list);
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
     // capture our View elements              
        mPickDate = (Button) findViewById(R.id.pickDate);
        
     // add a click listener to the button
        mPickDate.setOnClickListener(new View.OnClickListener() {            
        	public void onClick(View v) {                
        		showDialog(DATE_DIALOG_ID);            
        		}        
        	});
        mTimeText.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(TIME_DIALOG_ID);
            }
        });
        
        contactButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	mostrarListaContactos();
            	
            }
        });

        // get the current date     
        mYear = c.get(Calendar.YEAR);        
        mMonth = c.get(Calendar.MONTH);        
        mDay = c.get(Calendar.DAY_OF_MONTH); 
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);
        // display the current time
        updateDisplayTime();
        // display the current date (this method is below)        
        updateDisplay();
/////////////////////////////////////////////////////////////////////////////////////////////////////////////
        Button confirmButton = (Button) findViewById(R.id.confirm);

        mRowId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(EventsDbAdapter.KEY_ROWID);
  if (mRowId == null) {
   Bundle extras = getIntent().getExtras();
   mRowId = extras != null ? extras.getLong(EventsDbAdapter.KEY_ROWID)
         : null;
  }

  populateFields();

        confirmButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                setResult(RESULT_OK);
                //finish();
                String title = mTitleText.getText().toString();
                String contact= mContactText.getText().toString();
                String fechainic = mPickDate.getText().toString();
                String timeFin= mTimeText.getText().toString();
                if (((title != null) && (!title.equals(""))) && ((fechainic != null) && 
                		(!fechainic.equals(""))) && ((timeFin!=null)&&(!timeFin.equals("")))
                		&&((contact != null) && (!contact.equals(""))))  {     
                	if (comprobarFechas())
                		finish();
                	else {
                		mostrarErrorFechas();
                	}
                }
                else {
                	createDialog();
                	AlertDialog alert = mBuilder.create();
                	alert.show(); 
                }
            }

        });
    }
    
    private void createDialog() {
		mBuilder = new AlertDialog.Builder(this);
		mBuilder.setMessage("¡Cuidado! Estas dejando datos en blanco. ")
		       .setCancelable(true).setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	 dialog.cancel();
		        	 finish();
		           }
		       })
		       .setNegativeButton("Rellenar campos", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		             dialog.cancel();
		           }
		       });;	
	}

    private void populateFields() {
        if (mRowId != null) {
            Cursor event = mDbHelper.fetchEvent(mRowId);
            startManagingCursor(event);
            mTitleText.setText(event.getString(
                    event.getColumnIndexOrThrow(EventsDbAdapter.KEY_TITLE)));
            mContactText.setText(event.getString(
                    event.getColumnIndexOrThrow(EventsDbAdapter.KEY_CONTACT)));
            mPickDate.setText(event.getString(
                    event.getColumnIndexOrThrow(EventsDbAdapter.KEY_DATE)));
            mTimeText.setText(event.getString(
                    event.getColumnIndexOrThrow(EventsDbAdapter.KEY_TIME)));
        }
    }
    
    private void populateFieldsDos() {
        if (mRowId != null) {
            Cursor event = mDbHelper.fetchEvent(mRowId);
            startManagingCursor(event);
            mTitleText.setText(event.getString(
                    event.getColumnIndexOrThrow(EventsDbAdapter.KEY_TITLE)));
            mPickDate.setText(event.getString(
                    event.getColumnIndexOrThrow(EventsDbAdapter.KEY_DATE)));
            mTimeText.setText(event.getString(
                    event.getColumnIndexOrThrow(EventsDbAdapter.KEY_TIME)));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        outState.putSerializable(EventsDbAdapter.KEY_ROWID, mRowId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateFields();
    }

    private void saveState() {
        String title = mTitleText.getText().toString();
        String contact = mContactText.getText().toString();
        String date = mPickDate.getText().toString();
        String time = mTimeText.getText().toString();

        if (mRowId == null) {
            long id = mDbHelper.createEvent(title, date, time, contact);
            if (id > 0) {
                mRowId = id;
            }
        } else {
            mDbHelper.updateEvent(mRowId, title, date, time, contact);
        }
    }

 // updates the date in the TextView    
    private void updateDisplay() {        
    	mPickDate.setText(     
    			new StringBuilder()                    
    			// Month is 0 based so add 1                    
    			.append(mYear).append("/") 
    			.append(mMonth + 1).append("/")
    			.append(mDay).append(" "));    
    	}
    
 // updates the time we display in the TextView
    private void updateDisplayTime() {
        mTimeText.setText(
            new StringBuilder()
                    .append(pad(mHour)).append(":")
                    .append(pad(mMinute)));
    }

    private static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }
    
    // the callback received when the user "sets" the date in the dialog    
    private DatePickerDialog.OnDateSetListener mDateSetListener =  new DatePickerDialog.OnDateSetListener() {                
    	public void onDateSet(DatePicker view, int year,                                       
    			int monthOfYear, int dayOfMonth) {                    
    		mYear = year;                    
    		mMonth = monthOfYear;                    
    		mDay = dayOfMonth;                    
    		updateDisplay();                
    		}            
    	};
    	
    	
    	// the callback received when the user "sets" the time in the dialog
    	private TimePickerDialog.OnTimeSetListener mTimeSetListener =
    	    new TimePickerDialog.OnTimeSetListener() {
    	        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
    	            mHour = hourOfDay;
    	            mMinute = minute;
    	            updateDisplayTime();
    	        }
    	    };
    	    
	@Override
	protected Dialog onCreateDialog(int id) {    
		switch (id) {    
		case DATE_DIALOG_ID:        
			return new DatePickerDialog(this,mDateSetListener, mYear, mMonth, mDay);   
		case TIME_DIALOG_ID:
	        return new TimePickerDialog(this,
	                mTimeSetListener, mHour, mMinute, false);
		}    
		return null;
	}
	
	private void mostrarErrorFechas() {
    	Dialog errorFechas = new Dialog(this);

    	errorFechas.setContentView(R.layout.error_tiempo);
    	errorFechas.setTitle("¡Cuidado!");
    	
    	errorFechas.show();
    }
    
    private boolean comprobarFechas() {
    	if (mYear < c.get(Calendar.YEAR)) 
    		return false;
    
    	else if (mYear ==  c.get(Calendar.YEAR) && mMonth <  c.get(Calendar.MONTH)) 
    		return false;
    	
    	else if (mMonth ==  c.get(Calendar.MONTH) && mDay <  c.get(Calendar.DAY_OF_MONTH))
    		return false;
    	
    	else if ( mDay == c.get(Calendar.DAY_OF_MONTH) && mHour <  c.get(Calendar.HOUR_OF_DAY))
    		return false;
    	
    	else if (mHour == c.get(Calendar.HOUR_OF_DAY) && mMinute <  c.get(Calendar.MINUTE))
    		return false;
    	
    	return true;
    }
    
    private void mostrarListaContactos() {
        Intent intentContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI); 
     	  startActivityForResult(intentContact, ACTIVITY_CONTACT);
      }
      
      public void onActivityResult(int requestCode, int resultCode, Intent intent) {
      	if (requestCode == ACTIVITY_CONTACT) {
      	  getContactInfo(intent, mContactText);     
      	    populateFieldsDos();
      		  }
      }
      
      private void getContactInfo(Intent intent, EditText contact) {
      	if (intent != null) {
  	   Cursor cursor =  managedQuery(intent.getData(), null, null, null, null);   
  	   if (cursor.getCount() > 0) {
  	   while (cursor.moveToNext()) {           
  	       contact.setText(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
  	  }
  	   }
  	   cursor.close();
      	}
  	}
    
}
