package com.nobletecx.lucina;

import android.content.*;
import android.graphics.*;
import android.os.*;
import android.text.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.view.inputmethod.*;
import android.webkit.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.appcompat.app.*;
import androidx.appcompat.widget.*;
import androidx.coordinatorlayout.widget.*;
import androidx.swiperefreshlayout.widget.*;
import com.google.android.material.appbar.*;
import com.google.android.material.bottomsheet.*;
import com.google.android.material.snackbar.*;

import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity
{
	private static final String TAG = MainActivity.class.getName();
	private static final String DUCK_DUCK_GO_HOME = "https://start.duckduckgo.com/?kae=d";
	private static final String DUCK_DUCK_GO_PREFIX = "https://start.duckduckgo.com/?q=";
	private static final String DUCK_DUCK_GO_SUFFIX = "&t=fpas&ia=web&kae=d";
	private static final String GOOGLE_HOME = "https://google.com";
	private static final String GOOGLE_SEARCH_QUERY = "";

	private Toolbar toolbar;
	private TextView logcat, web_error_tv;
	private WebView webview;
	private EditText searchbox, home_searchbox;
	private ImageView src_btn, home_logo;
	private ProgressBar wvProgress;
	private SwipeRefreshLayout refreshLayout;
	private RelativeLayout logcatView, home_content;
	private BottomSheetBehavior behavior;
	private AppBarLayout appbar;
	private CoordinatorLayout.LayoutParams params;
	private ViewGroup rootLayout;

	private boolean devToolsEnabled, resLoadFirstEnabled, homePageEnabled = true;
	private String mUrl, mSearchEngine, intentUrl;
	private Handler handler;
	private Runnable runnable;

	private void showView(View... views)
	{
		for (View view : views)
		{
			view.setVisibility(View.VISIBLE);
		}
	}

	private void hideView(View... views)
	{
		for (View view : views)
		{
			view.setVisibility(View.GONE);
		}
	}

	private void toggleView(View... views)
	{
		for (View view : views)
		{
			if (view.getVisibility() == View.VISIBLE)
			{
				hideView(view);
			}
			else
			{
				showView(view);
			}
		}
	}

	private void performSearch()
	{
		appbar.setExpanded(true, true);
		if (!TextUtils.isEmpty(searchbox.getText().toString().trim()))
		{
			mUrl = searchbox.getText().toString().trim();
			if (Patterns.WEB_URL.matcher(mUrl).matches())
			{
				webview.loadUrl(mUrl);
			}
			else
			{
				webview.loadUrl(DUCK_DUCK_GO_PREFIX + mUrl + DUCK_DUCK_GO_SUFFIX);
			}
		}
		else if (!TextUtils.isEmpty(home_searchbox.getText().toString().trim()))
		{
			mUrl = home_searchbox.getText().toString().trim();
			webview.loadUrl(DUCK_DUCK_GO_PREFIX + mUrl + DUCK_DUCK_GO_SUFFIX);
		}

		home_searchbox.setText("");
		home_searchbox.clearFocus();
		hideView(home_content);
		searchbox.clearFocus();
		InputMethodManager in = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
		in.hideSoftInputFromWindow(searchbox.getWindowToken(), 0);
	}

	private void loadDevTools(WebView view)
	{
		// webview.loadUrl("javascript:(alert("JS DOM Test"))();");
		webview.loadUrl("javascript:(function () { var script = document.createElement('script'); script.src='//cdn.jsdelivr.net/npm/eruda'; document.body.appendChild(script); script.onload = function () { eruda.init() } })();");
		// showSnackBar("Developer Tools DOM Loaded", Snackbar.LENGTH_SHORT);
		// stringBuilder.append("Developer Tools DOM Loaded");
		logcat.setText("\nDeveloper Tools DOM Loaded\n---\n" + logcat.getText());
	}

	private void repeat()
	{
		if (!mUrl.equals("about:blank"))
		{
			webview.loadUrl(mUrl);
		}
	}

	private void refresh()
	{
		if (!mUrl.equals("about:blank"))
		{
			webview.reload();
		}
	}

	private void showSnackBar(String message, int duration)
	{
		rootLayout = (ViewGroup) ((ViewGroup) this.findViewById(android.R.id.content)).getChildAt(0);
		final Snackbar snackBar = Snackbar.make(rootLayout, message, duration);
		snackBar.show();
	}

	// private void showAppBar() {
	// appbar = findViewById(R.id.appbar);
	// appbar.setExpanded(true, true);
	// params = (CoordinatorLayout.LayoutParams) appbar.getLayoutParams();
	// AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();
	// behavior.onNestedFling((CoordinatorLayout) rootLayout, appbar, null, 0, -1000, true);
	// }

    @Override
    protected void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		appbar = findViewById(R.id.appbar);
		toolbar = findViewById(R.id.toolbar);
		webview = findViewById(R.id.webview);
		searchbox = findViewById(R.id.searchbox);
		home_logo = findViewById(R.id.home_logo);
		home_searchbox = findViewById(R.id.home_searchbox);
		src_btn = findViewById(R.id.src_btn);
		wvProgress = findViewById(R.id.wvProgress);
		refreshLayout = findViewById(R.id.refresh_layout);
		logcatView = findViewById(R.id.logcatView);
		logcat = findViewById(R.id.logcat);
		home_content = findViewById(R.id.home_content);
		web_error_tv = findViewById(R.id.web_error_tv);

		setSupportActionBar(toolbar);

		handler = new Handler(Looper.getMainLooper());
		runnable = new Runnable() {
			@Override
			public void run()
			{
				refreshLayout.setRefreshing(false);
			}
		};

		refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
				@Override
				public void onRefresh()
				{
					handler.removeCallbacks(runnable);
					handler.postDelayed(runnable, 10000);
					refresh();
				}
			});

		WebChromeClient wcc = new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int newProgress)
			{
				// appbar.setExpanded(true, true);
				hideView(web_error_tv);
				if (mUrl.equals("about:blank"))
				{
					wvProgress.setVisibility(View.GONE);
					wvProgress.setProgress(0);
					view.setEnabled(false);
					if (!resLoadFirstEnabled)
					{
						view.setVisibility(View.GONE);
					}
					refreshLayout.setRefreshing(false);
					searchbox.setText("");
					src_btn.setImageResource(R.drawable.twotone_search_24);
				}
				else if (newProgress == 100)
				{
					if (!mUrl.equals("about:blank"))
					{
						wvProgress.setVisibility(View.GONE);
						wvProgress.setProgress(0);
						view.setEnabled(true);
						if (!resLoadFirstEnabled)
						{
							view.setVisibility(View.VISIBLE);
						}
						refreshLayout.setRefreshing(false);
						src_btn.setImageResource(R.drawable.twotone_search_24);
					}
				}
				else
				{
					if (!mUrl.equals("about:blank"))
					{
						wvProgress.setVisibility(View.VISIBLE);
						wvProgress.setProgress(newProgress);
						view.setEnabled(false);
						view.setVisibility(View.GONE);
						refreshLayout.setRefreshing(true);
						src_btn.setImageResource(R.drawable.twotone_close_24);
					}
				}
			}

			@Override
			public void onConsoleMessage(String message, int lineNumber, String onConsole)
			{
				// logcat.append("Line " + lineNumber + ".\n" + message + "\n---\nonConsole: " + onConsole + "\n---\n");
				logcat.setText("\n" + lineNumber + ":" + onConsole + "\n> " + message + "\n---\n" + logcat.getText());
			}


			public boolean shouldOverrideUrlLoading(WebView view, String url)
			{
				appbar.setExpanded(true, true);
				hideView(web_error_tv);
				if (url.equals("about:blank"))
				{
					mUrl = url;
					view.clearHistory();
					view.setEnabled(false);
					wvProgress.setProgress(0);
					hideView(wvProgress);
					hideView(view);
					refreshLayout.setRefreshing(false);
					searchbox.setText("");
					src_btn.setImageResource(R.drawable.twotone_search_24);
					showView(home_content);
				}
				else
				{
					view.loadUrl(url);
				}
				return true;
			}

			public void onLoadResource(WebView view, String url)
			{
				if (url.equals("about:blank"))
				{
					mUrl = url;
					view.clearHistory();
					hideView(view);
					showView(home_content);
					searchbox.setText("");
				}
			}

			public void onPageFinished(WebView view, String url)
			{
				if (url.equals("about:blank"))
				{
					mUrl = url;
					view.clearHistory();
					hideView(view);
					showView(home_content);
					searchbox.setText("");
				}
			}
		};

		WebViewClient wvc = new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon)
			{
				appbar.setExpanded(true, true);
				hideView(web_error_tv);
				if (url.equals("about:blank"))
				{
					mUrl = url;
					wvProgress.setVisibility(View.GONE);
					wvProgress.setProgress(0);
					view.setEnabled(false);
					if (!resLoadFirstEnabled)
					{
						view.setVisibility(View.GONE);
					}
					refreshLayout.setRefreshing(false);
					searchbox.setText("");
					src_btn.setImageResource(R.drawable.twotone_search_24);
				}
				else 
				if (!url.equals("about:blank"))
				{
					searchbox.setText(mUrl = url);
				}
			}

			@Override
			public void onLoadResource(WebView view, String url)
			{

			}

			@Override
			public void onPageFinished(WebView view, String url)
			{
				if (url.equals("about:blank"))
				{
					mUrl = url;
					view.clearHistory();
					hideView(view);
					showView(home_content);
					searchbox.setText("");
				}
				else
				if (!url.equals("about:blank"))
				{
					view.setVisibility(View.VISIBLE);
				}

				if (devToolsEnabled)
				{
					loadDevTools(view);
				}
			}

			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
			{
				refreshLayout.setRefreshing(false);
				web_error_tv.setText("Error code: " + errorCode + "\nDescription: " + description + "\nFailing URL: " + failingUrl);
				showSnackBar("Oops...! Unexpected Error.", Snackbar.LENGTH_SHORT);
				showView(web_error_tv);
				hideView(webview);
				hideView(webview);
				hideView(webview);
				// showSnackBar("Error code: " + errorCode + "\nDescription: " + description + "\nFailing URL: " + failingUrl, Snackbar.LENGTH_LONG);
			}
		};

		webview.getSettings().setJavaScriptEnabled(true);
		webview.setSaveEnabled(true);
		// webview.getBackground().setTint(Color.BLACK);
		webview.getSettings().setAllowContentAccess(true);
		webview.getSettings().setAllowFileAccess(true);
		webview.getSettings().setAppCacheEnabled(true);
		webview.getSettings().setDomStorageEnabled(true);
		// webview.getSettings().setForceDarkMode(webview.getSettings().FORCE_DARK_ON);
		webview.getSettings().setLoadsImagesAutomatically(true);
		webview.getSettings().setNeedInitialFocus(true);
		webview.getSettings().setOffscreenPreRaster(true);
		webview.getSettings().setDatabaseEnabled(true);
		webview.getSettings().setSupportZoom(true);
		webview.getSettings().setBuiltInZoomControls(true);
		webview.getSettings().setDisplayZoomControls(false);
		webview.getSettings().setBlockNetworkImage(false);
		webview.getSettings().setBlockNetworkLoads(false);
		webview.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

		webview.setWebChromeClient(wcc);
		webview.setWebViewClient(wvc);

		if (getIntent() != null && getIntent().getData() != null)
		{
			intentUrl = getIntent().getData().toSafeString().trim();
		}
		
		if (!TextUtils.isEmpty(intentUrl))
		{
			webview.loadUrl(mUrl = intentUrl);
		}
		else
		{
			webview.loadUrl(mUrl = "about:blank");
			// webview.loadUrl(mUrl = mSearchEngine = DUCK_DUCK_GO_HOME);
		}

		searchbox.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view)
				{
					//
				}
			});

		searchbox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
				{
					if (actionId == EditorInfo.IME_ACTION_SEARCH)
					{
						performSearch();
						return true;
					}
					return false;
				}
			});

		home_content.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view)
				{
					searchbox.clearFocus();
					home_searchbox.clearFocus();
					InputMethodManager in = (InputMethodManager) MainActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
					in.hideSoftInputFromWindow(searchbox.getWindowToken(), 0);
				}
			});

		home_searchbox.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view)
				{
					//
				}
			});

		home_searchbox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
				{
					if (actionId == EditorInfo.IME_ACTION_SEARCH)
					{
						performSearch();
						return true;
					}
					return false;
				}
			});

		src_btn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view)
				{
					if (!webview.isEnabled())
					{
						webview.setEnabled(true);
						webview.stopLoading();
					}
					else
					{
						performSearch();
					}
				}
			});

		behavior = BottomSheetBehavior.from(logcatView);
		behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
				@Override
				public void onStateChanged(@NonNull View bottomSheet, int newState)
				{

				}

				@Override
				public void onSlide(@NonNull View bottomSheet, float slideOffset)
				{

				}
			});

		logcatView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view)
				{
					behavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
				}
			});
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		if (getIntent() != null && getIntent().getData() != null)
		{
			intentUrl = getIntent().toString().trim();
		}

		if (!TextUtils.isEmpty(intentUrl))
		{
			webview.loadUrl(mUrl = intentUrl);
		}
		logcat.setText("\n" + intentUrl + "\n---\n" + logcat.getText());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();
		if (id == R.id.item)
		{
			refresh();
		}
		else if (id == R.id.item1)
		{
			repeat();
		}
		else if (id == R.id.item2)
		{
			// TODO: Remove auto refresh when we loads DevTools everytime
			if (devToolsEnabled)
			{
				devToolsEnabled = false;
				// item.setTitle("Load Developer Tools DOM");
				logcat.setText("\nDeveloper Tools DOM Unloaded\n---\n" + logcat.getText());
				showSnackBar("Developer Tools unloaded, please refresh the page.", Snackbar.LENGTH_LONG);
			}
			else
			{
				devToolsEnabled = true;
				// item.setTitle("Unload Developer Tools DOM");
				loadDevTools(webview);
				// showSnackBar("Developer Tools enabled, please refresh the page.", Snackbar.LENGTH_LONG);
			}
			invalidateOptionsMenu();
		}
		else if (id == R.id.item3)
		{
			// showSnackBar("Settings are not ready yet", Snackbar.LENGTH_SHORT);
			startActivity(new Intent(this, SettingsActivity.class));
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		MenuItem item = menu.findItem(R.id.item2);
		if (devToolsEnabled)
		{
			item.setTitle("Unload Developer Tools DOM");
		}
		else
		{
			item.setTitle("Load Developer Tools DOM");
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onBackPressed()
	{
		home_searchbox.clearFocus();
		appbar.setExpanded(true, true);
		if (!getSupportActionBar().isShowing())
		{
			getSupportActionBar().show();
		}
		else if (behavior.getState() != BottomSheetBehavior.STATE_COLLAPSED)
		{
			behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
		}
		else if (webview.canGoBack())
		{
			webview.goBack();
		}
		else if (homePageEnabled && home_content.getVisibility() != View.VISIBLE)
		{
			// webview.stopLoading();
			// webview.cancelPendingInputEvents();
			// webview.stopLoading();
			// webview.stopNestedScroll();
			// webview.clearFocus();
			// webview.clearHistory();
			// webview.loadUrl("about:blank");
			// showAppBar();
			hideView(webview);
			searchbox.setText(mUrl = "");
			showView(home_content);
			hideView(webview);
			// home_searchbox.requestFocus();
		}
		else
		{
			super.onBackPressed();
		}
	}
}

