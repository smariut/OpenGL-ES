package com.silviu.animation3d;

import com.silviu.animation3d.AppRenderer;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager.LayoutParams;

public class MainActivity extends Activity  
{
   	AppRenderer mRenderer = null;
	MenuItem[] mMenuList = new MenuItem[10]; //options menu
   	
	@Override 
	public void onCreate(Bundle savedInstanceState) {

		getWindow().setFlags(0xFFFFFFFF, 
				LayoutParams.FLAG_FULLSCREEN|LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		super.onCreate(savedInstanceState);

		//onCreate apelata cand se schimba pozitia telefonului
		if (mRenderer == null)
			mRenderer = new AppRenderer(this); //deschiderea suprafetei openGL
		SharedPreferences sp = getSharedPreferences("FountainGL", 0);
		mRenderer.ShowBall = sp.getBoolean("ShowBall", mRenderer.ShowBall);

	}
	@Override
	protected void onResume() 
	{
		super.onResume();
		mRenderer.onResume();
	}

	@Override
	protected void onPause() 
	{
		super.onPause();
		mRenderer.onPause();
	}	
	
}
