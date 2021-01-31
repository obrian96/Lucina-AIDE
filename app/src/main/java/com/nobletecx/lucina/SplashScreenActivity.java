package com.nobletecx.lucina;

import androidx.appcompat.app.*;
import android.os.*;
import android.content.*;

public class SplashScreenActivity extends AppCompatActivity
{
	private Handler handler;
	private Runnable runnable;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		handler = new Handler(Looper.getMainLooper());
		runnable = new Runnable() {
			@Override
			public void run()
			{
				startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
				overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
				finish();
				return;
			}
		};
	}

	@Override
	protected void onResume()
	{
		if (handler == null) handler = new Handler(Looper.getMainLooper());
		handler.postDelayed(runnable, 2000);
		super.onResume();
	}

	@Override
	protected void onPause()
	{
		if (handler != null) {
			handler.removeCallbacks(runnable);
			handler = null;
		}
		super.onPause();
	}

	@Override
	protected void onDestroy()
	{
		if (handler != null) {
			handler.removeCallbacks(runnable);
			handler = null;
		}
		runnable = null;
		super.onDestroy();
	}
}

