package com.events;



import android.app.ListActivity;
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
    
    private EventsDbAdapter mDbHelper;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.events_list);
        mDbHelper = new EventsDbAdapter(this);
        mDbHelper.open();
        fillData();
        registerForContextMenu(getListView());
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
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case DELETE_ID:
                AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                mDbHelper.deleteEvent(info.id);
                fillData();
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
}