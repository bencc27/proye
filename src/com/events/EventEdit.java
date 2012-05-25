package com.events;


import java.util.Calendar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;




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
    private EditText mTimeText;
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
        mTimeText = (EditText) findViewById(R.id.time);
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
     // capture our View elements              
        mPickDate = (Button) findViewById(R.id.pickDate);
        
     // add a click listener to the button
        mPickDate.setOnClickListener(new View.OnClickListener() {            
        	public void onClick(View v) {                
        		showDialog(DATE_DIALOG_ID);            
        		}        
        	});
        
        // get the current date     
        mYear = c.get(Calendar.YEAR);        
        mMonth = c.get(Calendar.MONTH);        
        mDay = c.get(Calendar.DAY_OF_MONTH);       
        
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
                String nombre = mTitleText.getText().toString();
                String fechainic = mPickDate.getText().toString();
                if (((nombre != null) && (!nombre.equals(""))) && ((fechainic != null) && 
                		(!fechainic.equals(""))))  {     
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
		mBuilder.setMessage("Atención! Los datos no están completos. Revisa el nombre, descripción, y fechas. " +
				"Si sales perderás los datos. ¿Quieres continuar de todas formas? ")
		       .setCancelable(false)
		       .setPositiveButton("Sí, quiero continuar", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	 dialog.cancel();
		        	 finish();
		           }
		       })
		       .setNegativeButton("No, seguiré rellenando campos", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		             dialog.cancel();
		           }
		       });	
	}

    private void populateFields() {
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
        String date = mPickDate.getText().toString();
        String time = mTimeText.getText().toString();

        if (mRowId == null) {
            long id = mDbHelper.createEvent(title, date, time);
            if (id > 0) {
                mRowId = id;
            }
        } else {
            mDbHelper.updateEvent(mRowId, title, date, time);
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
    	
	@Override
	protected Dialog onCreateDialog(int id) {    
		switch (id) {    
		case DATE_DIALOG_ID:        
			return new DatePickerDialog(this,mDateSetListener, mYear, mMonth, mDay);    
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
    		
    	return true;
    }
}
