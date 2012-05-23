package com.events;


import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;




public class EventEdit extends Activity {
	////////////////////////////////////////////////////////////////////////////////////////////
    private TextView mDateDisplay;    
    private Button mPickDate;    
    private int mYear;    
    private int mMonth;    
    private int mDay;    
    static final int DATE_DIALOG_ID = 0;
////////////////////////////////////////////////////////////////////////////////////////////////////
    private EditText mTitleText;
    private EditText mDateText;
    private EditText mTimeText;
    private Long mRowId;
    private EventsDbAdapter mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new EventsDbAdapter(this);
        mDbHelper.open();
        
        setContentView(R.layout.event_edit);
        setTitle(R.string.edit_event);
        mTitleText = (EditText) findViewById(R.id.title);
        mDateText = (EditText) findViewById(R.id.date);
        mTimeText = (EditText) findViewById(R.id.time);
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
     // capture our View elements        
        mDateDisplay = (TextView) findViewById(R.id.dateDisplay);        
        mPickDate = (Button) findViewById(R.id.pickDate);
        
     // add a click listener to the button
        mPickDate.setOnClickListener(new View.OnClickListener() {            
        	public void onClick(View v) {                
        		showDialog(DATE_DIALOG_ID);            
        		}        
        	});
        
        // get the current date
        final Calendar c = Calendar.getInstance();        
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
                finish();
            }

        });
    }

    private void populateFields() {
        if (mRowId != null) {
            Cursor event = mDbHelper.fetchEvent(mRowId);
            startManagingCursor(event);
            mTitleText.setText(event.getString(
                    event.getColumnIndexOrThrow(EventsDbAdapter.KEY_TITLE)));
            mDateText.setText(event.getString(
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
        String date = mDateText.getText().toString();
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
    	mDateDisplay.setText(     
    			new StringBuilder()                    
    			// Month is 0 based so add 1                    
    			.append(mMonth + 1).append("-")                    
    			.append(mDay).append("-")                    
    			.append(mYear).append(" "));    
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
}
