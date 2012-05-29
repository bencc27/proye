package com.events;

import android.app.Activity;
import android.os.Bundle;

public class MostrarAutores extends Activity {
	
	 @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        
	        setContentView(R.layout.mostrar_autores);
	        setTitle(R.string.titulo_autores);
	 }

}
