package com.events;

import android.app.Activity;
import android.os.Bundle;

public class MostrarAyuda extends Activity {
	
	 @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        
	        setContentView(R.layout.mostrar_ayuda);
	        setTitle(R.string.titulo_ayuda);
	 }

}
