package com.tac.Weathercast.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.tabs.TabLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.miguelcatalan.materialsearchview.MaterialSearchView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tac.Weathercast.AlarmReceiver;
import com.tac.Weathercast.R;
import com.tac.Weathercast.adapters.ViewPagerAdapter;
import com.tac.Weathercast.adapters.WeatherRecyclerAdapter;
import com.tac.Weathercast.fragments.AboutDialogFragment;
import com.tac.Weathercast.fragments.AmbiguousLocationDialogFragment;
import com.tac.Weathercast.fragments.RecyclerViewFragment;
import com.tac.Weathercast.models.Weather;
import com.tac.Weathercast.tasks.GenericRequestTask;
import com.tac.Weathercast.tasks.ParseResult;
import com.tac.Weathercast.tasks.TaskOutput;
import com.tac.Weathercast.utils.Formatting;
import com.tac.Weathercast.utils.UI;
import com.tac.Weathercast.utils.UnitConvertor;
import com.tac.Weathercast.utils.WeatherSummary;
import com.tac.Weathercast.utils.SomaTheme;
import android.graphics.drawable.GradientDrawable;
import androidx.core.view.WindowInsetsControllerCompat;
import com.tac.Weathercast.widgets.AbstractWidgetProvider;
import com.tac.Weathercast.widgets.DashClockWeatherExtension;

import biz.laenger.android.vpbs.ViewPagerBottomSheetBehavior;

public class MainActivity extends BaseActivity implements LocationListener,CheckRefreshClickListener {
    protected static final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 1;

    // Time in milliseconds; only reload weather if last update is longer ago than this value
    private static final int NO_UPDATE_REQUIRED_THRESHOLD = 300000;
    private static Map<String, Integer> speedUnits = new HashMap<>(3);
    private static Map<String, Integer> pressUnits = new HashMap<>(3);
    private static boolean mappingsInitialised = false;

    private Weather todayWeather = new Weather();

    private TextView citytool;
    private TextView todayTemperature;
    private TextView todayDescription;
    private TextView todaydes;
    private TextView todayWind;
    private TextView todayPressure;
    private TextView todayHumidity;
    private TextView todaySunrise;
    private TextView todaySunset;
    private TextView todayUvIndex;
    private TextView lastUpdate;
    private TextView todayReadingHeadline;
    private TextView todayReadingAdvice;
    private View heroCard;
    private TextView todayFeelsLike;
    private TextView todayWindPill;
    private TextView todayUvPill;
    private ImageView todayIcon;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private View mainLay;
    private TextView currdate;

    private MaterialSearchView searchView;
    private View appView;
    private LinearLayout peekLayout;
    private LocationManager locationManager;
    private ProgressDialog progressDialog;
    private int theme;
    private boolean widgetTransparent;
    private boolean destroyed = false;

    private List<Weather> longTermWeather = new ArrayList<>();
    private List<Weather> longTermTodayWeather = new ArrayList<>();
    private List<Weather> longTermTomorrowWeather = new ArrayList<>();
    private CollapsingToolbarLayout header;

    public String recentCityId="";
    private String condition="Slightly humid with a gentle breeze in Lucknow.";
    private String wetaherArray[]={"Thunderstorm accompanied by gusty winds and lightning is expected in several parts.","Thunderstorm accompanied by gusty winds, rain and lightning is expected in several parts.","Heavy thunderstorm sounds, relaxing pouring rain & lightning.",
    "Thunderstorm accompanied by gusty winds and lightning is expected in several parts.","Snow falling soundlessly in the middle of the night will always fill my heart with sweet clarity.","And when it rains on your parade, look up rather than down. Without the rain, there would be no rainbow.",
    "Some people feel the rain. Others just get wet.","I saw old autumn in the misty morn Stand shadowless like silence, listening To silence.","DUST STORM TO DETERIORATE AIR QUALITY IN SEVERAL REGION.",
    "Haze, pollution causing low visibility over several parts.","Another foogy day and patchy morning with minimum temperature likely to go down.",
    "Sudden, sharp increase in wind speed lasting minutes with the possibility of rain.","Severe weather brings a tornado, flooding and hail to the region.",
    "You can plan whether to observe galaxies or planets or stay home and process image data.","No clouds; just a bright sunny day.","Volcano violently erupts spewing ash and smoke into the sky.",
    "Fraction of the sky obscured by clouds, possibilty of rain. "};
    private Formatting formatting;
    boolean doubleBackToExitPressedOnce = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize the associated SharedPreferences file with default values
        PreferenceManager.setDefaultValues(this, R.xml.prefs, false);
        com.tac.Weathercast.CustomFontApp.applyNightMode(this);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        widgetTransparent = prefs.getBoolean("transparentWidget", false);
        setTheme(theme = UI.getTheme(prefs.getString("theme", "fresh")));
        boolean darkTheme = super.darkTheme;
        boolean blackTheme = super.blackTheme;
        formatting = new Formatting(this);
        // Initiate activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);

        appView = findViewById(R.id.viewApp);
        progressDialog = new ProgressDialog(MainActivity.this);

        // Edge-to-edge: keep headers clear of the status bar / navigation bar
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        final View toolbarContainer = findViewById(R.id.toolbar_container);
        final View bottomSheet = findViewById(R.id.peeklayout);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.viewApp), (v, insets) -> {
            androidx.core.graphics.Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            if (toolbarContainer != null) {
                toolbarContainer.setPadding(toolbarContainer.getPaddingLeft(), bars.top,
                        toolbarContainer.getPaddingRight(), toolbarContainer.getPaddingBottom());
            }
            View topInset = findViewById(R.id.topInset);
            if (topInset != null) {
                android.view.ViewGroup.LayoutParams lp = topInset.getLayoutParams();
                lp.height = bars.top + (int) dp(52);
                topInset.setLayoutParams(lp);
            }
            View nav = findViewById(R.id.bottomNav);
            if (nav != null) {
                nav.setPadding((int) dp(6), 0, (int) dp(6), 0);
                android.view.ViewGroup.MarginLayoutParams mlp =
                        (android.view.ViewGroup.MarginLayoutParams) nav.getLayoutParams();
                mlp.bottomMargin = (int) dp(8) + Math.round(bars.bottom * 0.5f);
                nav.setLayoutParams(mlp);
            }
            return insets;
        });

        // Load toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        if (darkTheme) {
            toolbar.setPopupTheme(R.style.AppTheme_PopupOverlay_Dark);
        } else if (blackTheme) {
            toolbar.setPopupTheme(R.style.AppTheme_PopupOverlay_Black);
        }
        todayTemperature = (TextView) findViewById(R.id.todayTemperature);
        todayDescription = (TextView) findViewById(R.id.todayDescription);
        todaydes=findViewById(R.id.todayDes);
        todayWind = (TextView) findViewById(R.id.todayWind);
        todayPressure = (TextView) findViewById(R.id.todayPressure);
        todayHumidity = (TextView) findViewById(R.id.todayHumidity);
        todaySunrise = (TextView) findViewById(R.id.todaySunrise);
        todaySunset = (TextView) findViewById(R.id.todaySunset);
        todayUvIndex = (TextView) findViewById(R.id.todayUvIndex);
        lastUpdate = (TextView) findViewById(R.id.lastUpdate);
        todayReadingHeadline = findViewById(R.id.todayReadingHeadline);
        todayReadingAdvice = findViewById(R.id.todayReadingAdvice);
        heroCard = findViewById(R.id.heroCard);
        todayFeelsLike = findViewById(R.id.todayFeelsLike);
        todayWindPill = findViewById(R.id.todayWindPill);
        todayUvPill = findViewById(R.id.todayUvPill);
        mainLay=findViewById(R.id.main);
        citytool=findViewById(R.id.citytool);
        todayIcon = findViewById(R.id.todayIcon);
        currdate=findViewById(R.id.todayDate);
        applySomaTheme(800);
        setupBottomNav();

        searchView=findViewById(R.id.search_view);

        String timeStamp = new SimpleDateFormat("EEEE  dd  MMMM  yyyy").format(Calendar.getInstance().getTime());
        currdate.setText(timeStamp);// Get Date String according to date format

        citytool.setText("Lucknow");
        todayIcon.setImageResource(R.drawable.soma_wx_cloudy);
        todaydes.setText("");
        Typeface weatherFont = Typeface.createFromAsset(this.getAssets(), "fonts/weather.ttf");

        // Initialize viewPager
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);

        destroyed = false;

        initMappings();

        // Preload data from cache
        preloadWeather();
        preloadUVIndex();
        updateLastUpdateTime();

        // Set autoupdater
        AlarmReceiver.setRecurringAlarm(this);


        Bundle bundle = getIntent().getExtras();

        if (bundle != null && bundle.getBoolean("shouldRefresh")) {
            refreshWeather();

        }
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // A suggestion tap fills "City, State, CC" — reduce it to the
                // "City,CC" form the OWM lookup understands; plain text passes through.
                String q = query;
                if (q != null && q.contains(",")) {
                    String[] parts = q.split("\\s*,\\s*");
                    if (parts.length >= 2)
                        q = parts[0] + "," + parts[parts.length - 1];
                }
                new FindCitiesByNameTask(getApplicationContext(),
                        MainActivity.this, progressDialog).execute("city", q);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                scheduleCitySuggestions(newText);
                return false;
            }
        });


        searchView.setSuggestions(getResources().getStringArray(R.array.query_suggestions));
        searchView.setSubmitOnClick(true);
        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                try {
                    int owmId = Integer.parseInt(todayWeather.getId());
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                    themeSearchAndToolbar(SomaTheme.forNow(
                            Calendar.getInstance().get(Calendar.HOUR_OF_DAY), owmId,
                            sp.getString("appearance", "auto"), sp.getString("themeBase", "system")), false);
                } catch (Exception ignored) {}
            }

            @Override
            public void onSearchViewClosed() {
                //Do some magic
            }
        });

    }

    @Override
    public void onBackPressed() {
        if (searchView != null && searchView.isSearchOpen()) {
            searchView.closeSearch();
            return;
        }
        View host = findViewById(R.id.navHost);
        com.google.android.material.bottomnavigation.BottomNavigationView nav = findViewById(R.id.bottomNav);
        if (host != null && host.getVisibility() == View.VISIBLE) {
            if (nav != null) nav.setSelectedItemId(R.id.nav_today); else showTab(R.id.nav_today);
            return;
        }
        moveTaskToBack(true);
        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && matches.size() > 0) {
                String searchWrd = matches.get(0);
                if (!TextUtils.isEmpty(searchWrd)) {
                    searchView.setQuery(searchWrd, false);
                }
            }

            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

        public WeatherRecyclerAdapter getAdapter(int id) {
        WeatherRecyclerAdapter weatherRecyclerAdapter;
        if (id == 0) {
            weatherRecyclerAdapter = new WeatherRecyclerAdapter(this, longTermTodayWeather);
        } else if (id == 1) {
            weatherRecyclerAdapter = new WeatherRecyclerAdapter(this, longTermTomorrowWeather);
        } else {
            weatherRecyclerAdapter = new WeatherRecyclerAdapter(this, longTermWeather);
        }
        return weatherRecyclerAdapter;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateTodayWeatherUI();
        updateLongTermWeatherUI();
        updateUVIndexUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        com.tac.Weathercast.CustomFontApp.applyNightMode(this);
        try {
            com.google.android.material.bottomnavigation.BottomNavigationView nav = findViewById(R.id.bottomNav);
            if (nav != null) nav.getMenu().findItem(R.id.nav_today).setChecked(true);
        } catch (Exception ignored) {}
        try {
            int id = Integer.parseInt(todayWeather.getId());
            applySomaTheme(id);
        } catch (Exception ignored) {
            applySomaTheme(800);
        }
        if (UI.getTheme(PreferenceManager.getDefaultSharedPreferences(this).getString("theme", "fresh")) != theme ||
                PreferenceManager.getDefaultSharedPreferences(this).getBoolean("transparentWidget", false) != widgetTransparent) {
            // Restart activity to apply theme
            overridePendingTransition(0, 0);
            finish();
            overridePendingTransition(0, 0);
            startActivity(getIntent());
        } else if (shouldUpdate() && isNetworkAvailable()) {
            getTodayWeather();
            getLongTermWeather();
            getTodayUVIndex();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyed = true;

        if (locationManager != null) {
            try {
                locationManager.removeUpdates(MainActivity.this);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    private void preloadUVIndex() {
        try {
            if (todayWeather.getLat() == 0 && todayWeather.getLon() == 0) return;
            int owmId; try { owmId = Integer.parseInt(todayWeather.getId()); } catch (Exception e) { owmId = 800; }
            todayWeather.setUvIndex(com.tac.Weathercast.utils.UvEstimate.now(
                    todayWeather.getLat(), todayWeather.getLon(), owmId));
        } catch (Exception ignored) {}
    }

    private void preloadWeather() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        String lastToday = sp.getString("lastToday", "");
        if (!lastToday.isEmpty()) {
            new TodayWeatherTask(this, this, progressDialog).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "cachedResponse", lastToday);
        }
        String lastLongterm = sp.getString("lastLongterm", "");
        if (!lastLongterm.isEmpty()) {
            new LongTermWeatherTask(this, this, progressDialog).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "cachedResponse", lastLongterm);
        }

    }

    /** The standalone OWM UV endpoint was retired — estimate it from the sun's
     *  elevation and the current cloud cover instead (0 at night, real curve by day). */
    private void getTodayUVIndex() {
        try {
            int owmId; try { owmId = Integer.parseInt(todayWeather.getId()); } catch (Exception e) { owmId = 800; }
            double uv = com.tac.Weathercast.utils.UvEstimate.now(
                    todayWeather.getLat(), todayWeather.getLon(), owmId);
            todayWeather.setUvIndex(uv);
            updateUVIndexUI();
        } catch (Exception ignored) {}
    }

    private void getTodayWeather() {
        new TodayWeatherTask(this, this, progressDialog).execute();
    }

    private void getLongTermWeather() {
        new LongTermWeatherTask(this, this, progressDialog).execute();
    }

    private void searchCities() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(this.getString(R.string.search_title));
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setMaxLines(1);
        input.setSingleLine(true);
        alert.setView(input, 32, 0, 32, 0);

        alert.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String result = input.getText().toString();
                if (!result.isEmpty()) {
                    new FindCitiesByNameTask(getApplicationContext(),
                            MainActivity.this, progressDialog).execute("city", result);
                }
            }
        });
        alert.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Cancelled
            }
        });
        alert.show();
    }

    private void saveLocation(String result) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        recentCityId = preferences.getString("cityId", "Lucknow");

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("cityId", result);

        editor.commit();

//        if (!recentCityId.equals(result)) {
//            // New location, update weather
//            getTodayWeather();
//            getLongTermWeather();
//            getTodayUVIndex();
//        }
    }

    private void aboutDialog() {
        new AboutDialogFragment().show(getSupportFragmentManager(), null);
    }

    public static String getRainString(JSONObject rainObj) {
        String rain = "0";
        if (rainObj != null) {
            rain = rainObj.optString("3h", "fail");
            if ("fail".equals(rain)) {
                rain = rainObj.optString("1h", "0");
            }
        }
        return rain;
    }

    private ParseResult parseTodayJson(String result) {
        try {
            JSONObject reader = new JSONObject(result);

            final String code = reader.optString("cod");
            if ("404".equals(code)) {
                return ParseResult.CITY_NOT_FOUND;
            }

            String city = reader.getString("name");
            String country = "";
            JSONObject countryObj = reader.optJSONObject("sys");
            if (countryObj != null) {
                country = countryObj.getString("country");
                todayWeather.setSunrise(countryObj.getString("sunrise"));
                todayWeather.setSunset(countryObj.getString("sunset"));
            }
            todayWeather.setCity(city);

            todayWeather.setCountry(country);

            if (reader.has("timezone"))
                com.tac.Weathercast.utils.CityTime.setOffsetSeconds(reader.getInt("timezone"));

            JSONObject coordinates = reader.getJSONObject("coord");
            if (coordinates != null) {
                todayWeather.setLat(coordinates.getDouble("lat"));
                todayWeather.setLon(coordinates.getDouble("lon"));
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                sp.edit().putFloat("latitude", (float) todayWeather.getLat()).putFloat("longitude", (float) todayWeather.getLon()).commit();
            }

            JSONObject main = reader.getJSONObject("main");

            todayWeather.setTemperature(main.getString("temp"));
            todayWeather.setDescription(reader.getJSONArray("weather").getJSONObject(0).getString("description"));
            JSONObject windObj = reader.getJSONObject("wind");
            todayWeather.setWind(windObj.getString("speed"));
            if (windObj.has("deg")) {
                todayWeather.setWindDirectionDegree(windObj.getDouble("deg"));
            } else {
                Log.e("parseTodayJson", "No wind direction available");
                todayWeather.setWindDirectionDegree(null);
            }
            todayWeather.setPressure(main.getString("pressure"));
            todayWeather.setHumidity(main.getString("humidity"));

            JSONObject rainObj = reader.optJSONObject("rain");
            String rain;
            if (rainObj != null) {
                rain = getRainString(rainObj);
            } else {
                JSONObject snowObj = reader.optJSONObject("snow");
                if (snowObj != null) {
                    rain = getRainString(snowObj);
                } else {
                    rain = "0";
                }
            }
            todayWeather.setRain(rain);

            final String idString = reader.getJSONArray("weather").getJSONObject(0).getString("id");
            todayWeather.setId(idString);
            todayWeather.setIcon(formatting.setWeatherIcon(Integer.parseInt(idString), Calendar.getInstance().get(Calendar.HOUR_OF_DAY)));

            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
            editor.putString("lastToday", result);
            editor.commit();

        } catch (JSONException e) {
            Log.e("JSONException Data", result);
            e.printStackTrace();
            return ParseResult.JSON_EXCEPTION;
        }

        return ParseResult.OK;
    }

    private ParseResult parseTodayUVIJson(String result) {
        try {
            JSONObject reader = new JSONObject(result);

            final String code = reader.optString("cod");
            if ("404".equals(code)) {
                todayWeather.setUvIndex(-1);
                return ParseResult.CITY_NOT_FOUND;
            }

            double value = reader.getDouble("value");
            todayWeather.setUvIndex(value);
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
            editor.putString("lastUVIToday", result);
            editor.commit();
        } catch (JSONException e) {
            Log.e("JSONException Data", result);
            e.printStackTrace();
            return ParseResult.JSON_EXCEPTION;
        }

        return ParseResult.OK;
    }

    @SuppressLint("SetTextI18n")
    private void updateTodayWeatherUI() {
        try {
            if (todayWeather.getCountry().isEmpty()) {
                preloadWeather();
                return;
            }
        } catch (Exception e) {
            preloadWeather();
            return;
        }
        String city = todayWeather.getCity();
        String country = todayWeather.getCountry();
        DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getApplicationContext());
        citytool.setText(city);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        // Temperature
        float temperature = UnitConvertor.convertTemperature(Float.parseFloat(todayWeather.getTemperature()), sp);
        if (sp.getBoolean("temperatureInteger", false)) {
            temperature = Math.round(temperature);
        }

        // Rain
        double rain = Double.parseDouble(todayWeather.getRain());
        String rainString = UnitConvertor.getRainString(rain, sp);

        // Wind
        double wind;
        try {
            wind = Double.parseDouble(todayWeather.getWind());
        } catch (Exception e) {
            e.printStackTrace();
            wind = 0;
        }
        wind = UnitConvertor.convertWind(wind, sp);

        // Pressure
        double pressure = UnitConvertor.convertPressure((float) Double.parseDouble(todayWeather.getPressure()), sp);

        animateTemperature(temperature);
        todayDescription.setText(todayWeather.getDescription().substring(0, 1).toUpperCase() +
                todayWeather.getDescription().substring(1) + rainString);

        if (sp.getString("speedUnit", "m/s").equals("bft")) {
            todayWind.setText(
                    UnitConvertor.getBeaufortName((int) wind) +
                    (todayWeather.isWindDirectionAvailable() ? " " + getWindDirectionString(sp, this, todayWeather) : ""));
        } else {
            todayWind.setText(new DecimalFormat("0.0").format(wind) + " " +
                    localize(sp, "speedUnit", "m/s") +
                    (todayWeather.isWindDirectionAvailable() ? " " + getWindDirectionString(sp, this, todayWeather) : ""));
        }
        todayPressure.setText( new DecimalFormat("0.0").format(pressure) + " " +
                localize(sp, "pressureUnit", "hPa"));
        todayHumidity.setText(todayWeather.getHumidity() + " %");
        todaySunrise.setText(com.tac.Weathercast.utils.CityTime.hm(todayWeather.getSunrise()));
        todaySunset.setText(com.tac.Weathercast.utils.CityTime.hm(todayWeather.getSunset()));
        citytool=findViewById(R.id.citytool);
        citytool.setText(city);
        updateGlanceCard(sp);
        checkWeather();
        todayIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, GraphActivity.class);
                startActivity(intent);
            }
        });
    }


    /** Repaints the screen with the Soma time-of-day palette, nudged by the sky. */
    private void applySomaTheme(int owmId) {
        try {
            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            String appearance = PreferenceManager.getDefaultSharedPreferences(this)
                    .getString("appearance", "auto");
            String base = PreferenceManager.getDefaultSharedPreferences(this)
                    .getString("themeBase", "system");
            SomaTheme t = SomaTheme.forNow(hour, owmId, appearance, base);

            com.tac.Weathercast.utils.ElementalFieldView field = findViewById(R.id.fieldBg);
            if (field != null) field.setSky(t.sky);
            getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(t.sky[1]));

            if (peekLayout != null) {
                GradientDrawable sheet = new GradientDrawable();
                sheet.setColor(0x33000000);
                sheet.setCornerRadii(new float[]{ dp(28), dp(28), dp(28), dp(28), 0, 0, 0, 0 });
                peekLayout.setBackground(sheet);
            }

            // Apple-style: the hero has no box — the temperature floats on the sky.
            if (heroCard != null) {
                heroCard.setBackground(null);
                heroCard.setElevation(0);
            }
            View glance = findViewById(R.id.glanceCard);
            if (glance != null) {
                glance.setBackgroundResource(R.drawable.soma_reading_card);
                glance.setElevation(0);
            }

            // Bottom nav: a floating glass pill — sky-tinted fill + a top sheen.
            View nav = findViewById(R.id.bottomNav);
            if (nav != null) {
                int glass = (SomaTheme.blend(0xFF0B1220, t.sky[2], 0.24f) & 0x00FFFFFF) | 0xF2000000;
                float r = dp(28);
                GradientDrawable body = new GradientDrawable();
                body.setColor(glass);
                body.setCornerRadius(r);
                body.setStroke(Math.round(dp(1.5f)), 0x52FFFFFF);
                GradientDrawable sheen = new GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[]{ 0x33FFFFFF, 0x0DFFFFFF, 0x00FFFFFF });
                sheen.setCornerRadius(r);
                android.graphics.drawable.LayerDrawable pill =
                        new android.graphics.drawable.LayerDrawable(
                                new android.graphics.drawable.Drawable[]{ body, sheen });
                nav.setBackground(pill);
                nav.setClipToOutline(true);
                nav.setElevation(dp(16));
                if (android.os.Build.VERSION.SDK_INT >= 28) {
                    nav.setOutlineSpotShadowColor(0xB3000B1F);
                    nav.setOutlineAmbientShadowColor(0x66000B1F);
                }
                if (nav instanceof com.google.android.material.bottomnavigation.BottomNavigationView) {
                    android.content.res.ColorStateList csl = new android.content.res.ColorStateList(
                            new int[][]{ new int[]{ android.R.attr.state_checked }, new int[]{} },
                            new int[]{ 0xFFFFFFFF, 0xB8FFFFFF });
                    ((com.google.android.material.bottomnavigation.BottomNavigationView) nav).setItemIconTintList(csl);
                    ((com.google.android.material.bottomnavigation.BottomNavigationView) nav).setItemTextColor(csl);
                }
            }

            // Whole Today surface → white frosted text.
            final View scrollContent = findViewById(R.id.main);
            if (scrollContent != null) {
                com.tac.Weathercast.utils.SkyTint.apply(scrollContent);
                scrollContent.post(() -> com.tac.Weathercast.utils.SkyTint.apply(scrollContent));
            }
            setTextColorSafe(0xFFFFFFFF, todayTemperature, todayDescription, citytool,
                    todayReadingHeadline, todayFeelsLike, todayWindPill, todayUvPill);
            setTextColorSafe(0xF2FFFFFF, todaydes, todayReadingAdvice);
            setTextColorSafe(0xE0FFFFFF, currdate);

            getWindow().setStatusBarColor(0x00000000);
            getWindow().setNavigationBarColor(0x33000000);
            View decor = getWindow().getDecorView();
            WindowInsetsControllerCompat c = new WindowInsetsControllerCompat(getWindow(), decor);
            c.setAppearanceLightStatusBars(!t.isDark);
            c.setAppearanceLightNavigationBars(!t.isDark);

            themeSearchAndToolbar(t, !firstThemeApply);
            firstThemeApply = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private float dp(float v) { return v * getResources().getDisplayMetrics().density; }

    private android.animation.ValueAnimator tempAnimator;

    /** Count the hero temperature up (or down) to its new value. */
    private void animateTemperature(final float target) {
        if (todayTemperature == null) return;
        float from = 0f;
        try {
            String cur = todayTemperature.getText().toString().replace("°", "").trim();
            if (!cur.isEmpty()) from = Float.parseFloat(cur);
        } catch (Exception ignored) {}
        if (Math.abs(from - target) < 0.1f || Math.abs(from - target) > 60f) {
            todayTemperature.setText(new DecimalFormat("0.#").format(target) + "°");
            return;
        }
        if (tempAnimator != null) tempAnimator.cancel();
        final DecimalFormat fmt = new DecimalFormat("0.#");
        tempAnimator = android.animation.ValueAnimator.ofFloat(from, target);
        tempAnimator.setDuration(620);
        tempAnimator.setInterpolator(new android.view.animation.DecelerateInterpolator(1.8f));
        tempAnimator.addUpdateListener(a ->
                todayTemperature.setText(fmt.format((float) a.getAnimatedValue()) + "°"));
        tempAnimator.start();
    }

    private void setTextColorSafe(int color, TextView... views) {
        for (TextView v : views) if (v != null) v.setTextColor(color);
    }

    private long lastAqiFetch = 0;

    private final Handler citySuggestHandler = new Handler();
    private Runnable citySuggestTask;
    private volatile long citySuggestToken;
    private boolean suppressSuggestFetch = false;
    private String lastSuggestQuery = "";

    /** Live city-name autocomplete via the OWM Geocoding API, debounced ~350ms. */
    private void scheduleCitySuggestions(final String text) {
        if (suppressSuggestFetch) { suppressSuggestFetch = false; return; }
        if (citySuggestTask != null) citySuggestHandler.removeCallbacks(citySuggestTask);
        if (text == null || text.trim().length() < 3) return;
        if (text.trim().equalsIgnoreCase(lastSuggestQuery)) return;
        lastSuggestQuery = text.trim();
        final String q = text.trim();
        final long token = ++citySuggestToken;
        citySuggestTask = () -> {
            final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            final String key = sp.getString("apiKey", getString(R.string.apiKey)).replace("\"", "").trim();
            new Thread(() -> {
                try {
                    java.net.HttpURLConnection cn = (java.net.HttpURLConnection) new java.net.URL(
                            "https://api.openweathermap.org/geo/1.0/direct?q="
                                    + java.net.URLEncoder.encode(q, "UTF-8")
                                    + "&limit=5&appid=" + key).openConnection();
                    cn.setConnectTimeout(6000); cn.setReadTimeout(6000);
                    java.io.BufferedReader br = new java.io.BufferedReader(
                            new java.io.InputStreamReader(cn.getInputStream()));
                    StringBuilder s = new StringBuilder(); String ln;
                    while ((ln = br.readLine()) != null) s.append(ln);
                    br.close();
                    org.json.JSONArray arr = new org.json.JSONArray(s.toString());
                    final java.util.ArrayList<String> out = new java.util.ArrayList<>();
                    final String qLower = q.toLowerCase();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject o = arr.getJSONObject(i);
                        String name = o.getString("name");
                        // MaterialSearchView filters suggestions by prefix against the
                        // current query, so keep only server hits the query prefixes.
                        if (!name.toLowerCase().startsWith(qLower)) continue;
                        StringBuilder label = new StringBuilder(name);
                        if (o.has("state") && !o.getString("state").isEmpty())
                            label.append(", ").append(o.getString("state"));
                        if (o.has("country")) label.append(", ").append(o.getString("country"));
                        String v = label.toString();
                        if (!out.contains(v)) out.add(v);
                    }
                    if (token != citySuggestToken || out.isEmpty()) return;
                    runOnUiThread(() -> {
                        if (token != citySuggestToken) return;
                        searchView.setSuggestions(out.toArray(new String[0]));
                        // Nudge MaterialSearchView to re-filter and reveal the list
                        // now that the adapter is populated.
                        suppressSuggestFetch = true;
                        searchView.setQuery(q, false);
                    });
                } catch (Exception ignored) { }
            }).start();
        };
        citySuggestHandler.postDelayed(citySuggestTask, 350);
    }

    /** Real air quality from the OpenWeatherMap Air Pollution API. */
    private void fetchAirQuality(SharedPreferences sp) {
        if (System.currentTimeMillis() - lastAqiFetch < 20 * 60_000L) return;
        final double lat = sp.getFloat("latitude", 0f);
        final double lon = sp.getFloat("longitude", 0f);
        if (lat == 0 && lon == 0) return;
        lastAqiFetch = System.currentTimeMillis();
        final String key = sp.getString("apiKey", getString(R.string.apiKey)).replace("\"", "").trim();
        new Thread(() -> {
            try {
                java.net.HttpURLConnection cn = (java.net.HttpURLConnection) new java.net.URL(
                        "https://api.openweathermap.org/data/2.5/air_pollution?lat=" + lat
                                + "&lon=" + lon + "&appid=" + key).openConnection();
                cn.setConnectTimeout(8000); cn.setReadTimeout(8000);
                java.io.BufferedReader br = new java.io.BufferedReader(
                        new java.io.InputStreamReader(cn.getInputStream()));
                StringBuilder s = new StringBuilder(); String ln;
                while ((ln = br.readLine()) != null) s.append(ln);
                br.close();
                JSONObject main = new JSONObject(s.toString())
                        .getJSONArray("list").getJSONObject(0).getJSONObject("main");
                final int aqi = main.getInt("aqi");
                final String[] labels = { "", "Good", "Fair", "Moderate", "Poor", "Very poor" };
                runOnUiThread(() -> {
                    TextView v = findViewById(R.id.todayAqi);
                    if (v != null && aqi >= 1 && aqi <= 5) v.setText("AQI · " + labels[aqi]);
                });
            } catch (Exception e) {
                lastAqiFetch = 0;
            }
        }).start();
    }

    /** Fills the "At a glance" summary card + hero pills from the current conditions. */
    private void updateGlanceCard(SharedPreferences sp) {
        fetchAirQuality(sp);
        try {
            double tempC = Double.parseDouble(todayWeather.getTemperature()) - 273.15;
            double humidity;
            try { humidity = Double.parseDouble(todayWeather.getHumidity()); } catch (Exception e) { humidity = 50; }
            double windMs;
            try { windMs = Double.parseDouble(todayWeather.getWind()); } catch (Exception e) { windMs = 0; }
            int owmId;
            try { owmId = Integer.parseInt(todayWeather.getId()); } catch (Exception e) { owmId = 800; }
            double uv;
            try {
                uv = com.tac.Weathercast.utils.UvEstimate.now(
                        todayWeather.getLat(), todayWeather.getLon(), owmId);
                todayWeather.setUvIndex(uv);
            } catch (Exception e) { uv = todayWeather.getUvIndex(); }

            long daylight = 0;
            try {
                daylight = (todayWeather.getSunset().getTime() - todayWeather.getSunrise().getTime()) / 60000L;
                if (daylight < 0 || daylight > 24 * 60) daylight = 0;
            } catch (Exception ignored) {}

            boolean isNight;
            try {
                long now = System.currentTimeMillis();
                isNight = now < todayWeather.getSunrise().getTime() || now > todayWeather.getSunset().getTime();
            } catch (Exception e) {
                int hh = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                isNight = hh >= 20 || hh < 6;
            }
            WeatherSummary.Glance g = WeatherSummary.of(tempC, humidity, windMs, uv, owmId, daylight, isNight);
            if (todayReadingHeadline != null) todayReadingHeadline.setText(g.headline);
            if (todayReadingAdvice != null) todayReadingAdvice.setText(g.tip);

            // Apparent temperature (OWM call carries no units param, so approximate it).
            double feelsLikeC = tempC;
            if (windMs > 1.3 && tempC < 12) feelsLikeC = tempC - Math.min(6, windMs);
            else if (humidity > 65 && tempC > 26) feelsLikeC = tempC + (humidity - 65) / 12.0;

            if (todayFeelsLike != null)
                todayFeelsLike.setText(Math.round(UnitConvertor.convertTemperature((float) (feelsLikeC + 273.15), sp)) + "°");
            if (todayWindPill != null)
                todayWindPill.setText(new DecimalFormat("0.#").format(UnitConvertor.convertWind(windMs, sp)) + " " + localize(sp, "speedUnit", "m/s"));
            double uvNow = Math.max(0, uv);
            if (todayUvPill != null)
                todayUvPill.setText(new DecimalFormat("0.#").format(uvNow));

            com.tac.Weathercast.utils.UvBarView uvBar = findViewById(R.id.uvBar);
            if (uvBar != null) uvBar.setLevel(uvNow);
            if (todayUvIndex != null)
                todayUvIndex.setText(UnitConvertor.convertUvIndexToRiskLevel(uvNow));

            // Plan your day — best outdoor window + golden hour
            try {
                java.util.List<Weather> src = (longTermTodayWeather != null && !longTermTodayWeather.isEmpty())
                        ? longTermTodayWeather : longTermWeather;
                com.tac.Weathercast.utils.DayPlanner.Plan p = com.tac.Weathercast.utils.DayPlanner.build(
                        src, todayWeather.getSunrise(), todayWeather.getSunset());
                View planCard = findViewById(R.id.planCard);
                TextView pw = findViewById(R.id.planWindow);
                TextView pwy = findViewById(R.id.planWindowWhy);
                TextView pg = findViewById(R.id.planGolden);
                boolean any = false;
                if (pw != null) {
                    if (p.windowRange != null) {
                        pw.setText("Best window · " + p.windowRange);
                        pwy.setText(p.windowWhy);
                        pw.setVisibility(View.VISIBLE); pwy.setVisibility(View.VISIBLE);
                        ((View) pw.getParent().getParent()).setVisibility(View.VISIBLE);
                        any = true;
                    } else {
                        ((View) pw.getParent().getParent()).setVisibility(View.GONE);
                    }
                }
                if (pg != null && (p.goldenAm != null || p.goldenPm != null)) {
                    String gh = "";
                    if (p.goldenAm != null) gh += p.goldenAm;
                    if (p.goldenPm != null) gh += (gh.isEmpty() ? "" : "   ·   ") + p.goldenPm;
                    pg.setText(gh);
                    any = true;
                }
                if (planCard != null) planCard.setVisibility(any ? View.VISIBLE : View.GONE);
            } catch (Exception ignored) {}

            // --- Advanced "at a glance" widget ---
            try {
                java.util.List<Weather> fut = new java.util.ArrayList<>();
                if (longTermTomorrowWeather != null) fut.addAll(longTermTomorrowWeather);
                if (longTermWeather != null) fut.addAll(longTermWeather);
                com.tac.Weathercast.utils.Briefing.Result br =
                        com.tac.Weathercast.utils.Briefing.build(todayWeather, longTermTodayWeather, fut);

                setTextSafe(R.id.glFeels, Math.round(UnitConvertor.convertTemperature(
                        (float) (feelsLikeC + 273.15), sp)) + "°");

                String rainNext = "—";
                for (com.tac.Weathercast.models.Weather w : (longTermTodayWeather != null ? longTermTodayWeather
                        : new java.util.ArrayList<Weather>())) {
                    int wid; try { wid = Integer.parseInt(w.getId()); } catch (Exception e) { continue; }
                    if (wid >= 200 && wid < 700 && w.getDate() != null
                            && w.getDate().getTime() > System.currentTimeMillis()) {
                        rainNext = "~" + com.tac.Weathercast.utils.CityTime.hm(w.getDate());
                        break;
                    }
                }
                if ("—".equals(rainNext) && owmId >= 200 && owmId < 700) rainNext = "now";
                else if ("—".equals(rainNext)) rainNext = "none";
                setTextSafe(R.id.glRain, rainNext);

                TextView aqiV = findViewById(R.id.todayAqi);
                String aqi = aqiV != null && aqiV.getText().length() > 0
                        ? aqiV.getText().toString().replace("AQI · ", "") : "—";
                setTextSafe(R.id.glAir, aqi);

                bindGlanceInsight(R.id.glInsight1, R.id.glInsight1Text, br, 0);
                bindGlanceInsight(R.id.glInsight2, R.id.glInsight2Text, br, 1);
            } catch (Exception ignored) {}

            // Wind compass
            com.tac.Weathercast.utils.CompassView compass = findViewById(R.id.windCompass);
            if (compass != null) {
                Double deg = todayWeather.getWindDirectionDegree();
                int hr = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                compass.setBearing(deg == null ? null : deg.floatValue(),
                        SomaTheme.forNow(hr, owmId).heroAccent);
            }

            // Daylight / sun arc
            if (daylight > 0) {
                TextView dl = findViewById(R.id.daylightLength);
                if (dl != null) dl.setText((daylight / 60) + "h " + (daylight % 60) + "m");
                com.tac.Weathercast.utils.SunArcView arc = findViewById(R.id.sunArc);
                if (arc != null) {
                    long rise = todayWeather.getSunrise().getTime();
                    long set = todayWeather.getSunset().getTime();
                    float frac = (float) ((System.currentTimeMillis() - rise) / (double) (set - rise));
                    int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                    arc.setDaylightFraction(frac, SomaTheme.forNow(hour, owmId).heroAccent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateUVIndexUI() {
        try {
            if (todayWeather.getCountry().isEmpty()) {
                return;
            }
        } catch (Exception e) {
            preloadUVIndex();
            return;
        }

        // UV Index
        double uvIndex = todayWeather.getUvIndex();
        todayUvIndex.setText(UnitConvertor.convertUvIndexToRiskLevel(uvIndex));
        updateGlanceCard(PreferenceManager.getDefaultSharedPreferences(MainActivity.this));
    }

    public ParseResult parseLongTermJson(String result) {
        int i;
        try {
            JSONObject reader = new JSONObject(result);

            final String code = reader.optString("cod");
            if ("404".equals(code)) {
                if (longTermWeather == null) {
                    longTermWeather = new ArrayList<>();
                    longTermTodayWeather = new ArrayList<>();
                    longTermTomorrowWeather = new ArrayList<>();
                }
                return ParseResult.CITY_NOT_FOUND;
            }

            longTermWeather = new ArrayList<>();
            longTermTodayWeather = new ArrayList<>();
            longTermTomorrowWeather = new ArrayList<>();

            JSONArray list = reader.getJSONArray("list");
            for (i = 0; i < list.length(); i++) {
                Weather weather = new Weather();

                JSONObject listItem = list.getJSONObject(i);
                JSONObject main = listItem.getJSONObject("main");

                weather.setDate(listItem.getString("dt"));
                weather.setTemperature(main.getString("temp"));
                weather.setDescription(listItem.optJSONArray("weather").getJSONObject(0).getString("description"));
                JSONObject windObj = listItem.optJSONObject("wind");
                if (windObj != null) {
                    weather.setWind(windObj.getString("speed"));
                    weather.setWindDirectionDegree(windObj.getDouble("deg"));
                }
                weather.setPressure(main.getString("pressure"));
                weather.setHumidity(main.getString("humidity"));

                JSONObject rainObj = listItem.optJSONObject("rain");
                String rain = "";
                if (rainObj != null) {
                    rain = getRainString(rainObj);
                } else {
                    JSONObject snowObj = listItem.optJSONObject("snow");
                    if (snowObj != null) {
                        rain = getRainString(snowObj);
                    } else {
                        rain = "0";
                    }
                }
                weather.setRain(rain);

                final String idString = listItem.optJSONArray("weather").getJSONObject(0).getString("id");
                weather.setId(idString);

                final String dateMsString = listItem.getString("dt") + "000";
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(Long.parseLong(dateMsString));
                weather.setIcon(formatting.setWeatherIcon(Integer.parseInt(idString), cal.get(Calendar.HOUR_OF_DAY)));
                Calendar today = Calendar.getInstance();
                today.set(Calendar.HOUR_OF_DAY, 0);
                today.set(Calendar.MINUTE, 0);
                today.set(Calendar.SECOND, 0);
                today.set(Calendar.MILLISECOND, 0);

                Calendar tomorrow = (Calendar) today.clone();
                tomorrow.add(Calendar.DAY_OF_YEAR, 1);

                Calendar later = (Calendar) today.clone();
                later.add(Calendar.DAY_OF_YEAR, 2);

                if (cal.before(tomorrow)) {
                    longTermTodayWeather.add(weather);
                } else if (cal.before(later)) {
                    longTermTomorrowWeather.add(weather);
                } else {
                    longTermWeather.add(weather);
                }
            }
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
            editor.putString("lastLongterm", result);
            editor.commit();
        } catch (JSONException e) {
            Log.e("JSONException Data", result);
            e.printStackTrace();
            return ParseResult.JSON_EXCEPTION;
        }

        return ParseResult.OK;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void updateLongTermWeatherUI() {
        if (destroyed) return;
        updateHourlyStrip();
        updateDailyList();
    }

    /** Aggregate the 3-hourly forecast into 7 daily rows and render them inline. */
    private void updateDailyList() {
        try {
            android.widget.LinearLayout list = findViewById(R.id.dailyList);
            if (list == null || longTermWeather == null || longTermWeather.isEmpty()) return;
            list.removeAllViews();

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            java.util.LinkedHashMap<Integer, java.util.List<Weather>> byDay = new java.util.LinkedHashMap<>();
            for (Weather w : longTermWeather) {
                Calendar c = Calendar.getInstance();
                c.setTime(w.getDate());
                int key = c.get(Calendar.YEAR) * 1000 + c.get(Calendar.DAY_OF_YEAR);
                java.util.List<Weather> l = byDay.get(key);
                if (l == null) { l = new ArrayList<>(); byDay.put(key, l); }
                l.add(w);
            }

            float gLo = Float.MAX_VALUE, gHi = -Float.MAX_VALUE;
            java.util.List<float[]> ranges = new ArrayList<>();
            java.util.List<Weather> reps = new ArrayList<>();
            java.util.List<java.util.Date> dates = new ArrayList<>();
            int count = 0;
            for (java.util.List<Weather> day : byDay.values()) {
                if (count++ >= 7) break;
                float lo = Float.MAX_VALUE, hi = -Float.MAX_VALUE;
                Weather rep = day.get(day.size() / 2);
                for (Weather w : day) {
                    try {
                        float t = UnitConvertor.convertTemperature(Float.parseFloat(w.getTemperature()), sp);
                        lo = Math.min(lo, t); hi = Math.max(hi, t);
                    } catch (Exception ignored) {}
                    Calendar c = Calendar.getInstance(); c.setTime(w.getDate());
                    if (c.get(Calendar.HOUR_OF_DAY) >= 12 && c.get(Calendar.HOUR_OF_DAY) <= 15) rep = w;
                }
                gLo = Math.min(gLo, lo); gHi = Math.max(gHi, hi);
                ranges.add(new float[]{ lo, hi });
                reps.add(rep);
                dates.add(day.get(0).getDate());
            }
            if (gHi - gLo < 1f) gHi = gLo + 1f;

            java.text.SimpleDateFormat dayFmt = new java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault());
            for (int i = 0; i < reps.size(); i++) {
                View row = LayoutInflater.from(this).inflate(R.layout.daily_row, list, false);
                Weather rep = reps.get(i);
                float[] r = ranges.get(i);
                ((TextView) row.findViewById(R.id.dayName)).setText(
                        i == 0 ? "Today" : dayFmt.format(dates.get(i)));
                int owmId;
                try { owmId = Integer.parseInt(rep.getId()); } catch (Exception e) { owmId = 800; }
                ((ImageView) row.findViewById(R.id.dayIcon)).setImageResource(
                        Formatting.somaIllustration(owmId, false));
                String d = rep.getDescription();
                ((TextView) row.findViewById(R.id.dayDesc)).setText(
                        d.isEmpty() ? "" : Character.toUpperCase(d.charAt(0)) + d.substring(1));
                ((TextView) row.findViewById(R.id.dayLo)).setText(Math.round(r[0]) + "°");
                ((TextView) row.findViewById(R.id.dayHi)).setText(Math.round(r[1]) + "°");
                // range bar inset within the global span
                View bar = row.findViewById(R.id.dayRange);
                android.widget.LinearLayout.LayoutParams lp =
                        (android.widget.LinearLayout.LayoutParams) bar.getLayoutParams();
                float span = gHi - gLo;
                lp.leftMargin = (int) dp(10 + 42 * (r[0] - gLo) / span);
                lp.rightMargin = (int) dp(10 + 42 * (gHi - r[1]) / span);
                bar.setLayoutParams(lp);
                list.addView(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Weather getTodayWeatherData() { return todayWeather; }
    public java.util.List<Weather> getLongTermWeatherData() { return longTermWeather; }
    public java.util.List<Weather> getLongTermTodayWeatherData() { return longTermTodayWeather; }
    public java.util.List<Weather> getLongTermTomorrowWeatherData() { return longTermTomorrowWeather; }

    /** today + tomorrow + the remaining days, in chronological order. */
    public java.util.List<Weather> getForecastSeries() {
        java.util.ArrayList<Weather> all = new java.util.ArrayList<>();
        if (longTermTodayWeather != null) all.addAll(longTermTodayWeather);
        if (longTermTomorrowWeather != null) all.addAll(longTermTomorrowWeather);
        if (longTermWeather != null) all.addAll(longTermWeather);
        com.tac.Weathercast.utils.ForecastCache.set(all);
        return all;
    }

    private void showTab(int itemId) {
        View scroll = findViewById(R.id.swipeRefresh);
        View bar = findViewById(R.id.toolbar_container);
        View host = findViewById(R.id.navHost);
        boolean today = itemId == R.id.nav_today;
        if (scroll != null) scroll.setVisibility(today ? View.VISIBLE : View.GONE);
        if (bar != null) bar.setVisibility(today ? View.VISIBLE : View.GONE);
        if (host != null) host.setVisibility(today ? View.GONE : View.VISIBLE);
        if (today) {
            androidx.fragment.app.Fragment f = getSupportFragmentManager().findFragmentById(R.id.navHost);
            if (f != null) getSupportFragmentManager().beginTransaction().remove(f).commitAllowingStateLoss();
            return;
        }
        androidx.fragment.app.Fragment frag;
        if (itemId == R.id.nav_hourly) frag = new com.tac.Weathercast.fragments.HourlyFragment();
        else if (itemId == R.id.nav_briefing) frag = new com.tac.Weathercast.fragments.BriefingFragment();
        else if (itemId == R.id.nav_trends) frag = new com.tac.Weathercast.fragments.TrendsFragment();
        else frag = new com.tac.Weathercast.fragments.MoreFragment();
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.soma_item_in, android.R.anim.fade_out)
                .replace(R.id.navHost, frag)
                .commitAllowingStateLoss();
    }

    private void setTextSafe(int id, String s) {
        TextView tv = findViewById(id);
        if (tv != null) tv.setText(s);
    }

    private void bindGlanceInsight(int rowId, int textId, com.tac.Weathercast.utils.Briefing.Result br, int idx) {
        View row = findViewById(rowId);
        TextView tv = findViewById(textId);
        if (row == null || tv == null) return;
        if (br != null && br.insights.size() > idx) {
            com.tac.Weathercast.utils.Briefing.Insight in = br.insights.get(idx);
            android.text.SpannableString ss = new android.text.SpannableString(in.headline + "  " + in.detail);
            int end = in.headline.length();
            ss.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, end, 0);
            ss.setSpan(new android.text.style.ForegroundColorSpan(0xFFFFFFFF), 0, end, 0);
            ss.setSpan(new android.text.style.ForegroundColorSpan(0xC2FFFFFF), end, ss.length(), 0);
            tv.setText(ss);
            row.setVisibility(View.VISIBLE);
        } else {
            row.setVisibility(View.GONE);
        }
    }

    private void wireTrend(int viewId, String type) {
        View v = findViewById(viewId);
        if (v == null) return;
        v.setClickable(true);
        v.setOnClickListener(x -> {
            getForecastSeries();   // warm the cache
            android.content.Intent it = new android.content.Intent(this,
                    com.tac.Weathercast.activities.TrendDetailActivity.class);
            it.putExtra("type", type);
            startActivity(it);
        });
    }

    private void gotoBriefing() {
        com.google.android.material.bottomnavigation.BottomNavigationView nav = findViewById(R.id.bottomNav);
        if (nav != null) nav.setSelectedItemId(R.id.nav_briefing);
    }

    private void setupBottomNav() {
        com.google.android.material.bottomnavigation.BottomNavigationView nav = findViewById(R.id.bottomNav);
        final androidx.core.widget.NestedScrollView scroll = findViewById(R.id.scrollHost);

        androidx.swiperefreshlayout.widget.SwipeRefreshLayout srl = findViewById(R.id.swipeRefresh);
        if (srl != null) {
            srl.setColorSchemeColors(0xFFD8663D);
            srl.setProgressBackgroundColorSchemeColor(
                    androidx.core.content.ContextCompat.getColor(this, R.color.soma_surface));
            srl.setOnRefreshListener(() -> {
                refreshWeather();
                srl.postDelayed(() -> srl.setRefreshing(false), 1400);
            });
        }

        View radarCard = findViewById(R.id.radarCard);
        if (radarCard != null) radarCard.setOnClickListener(x -> {
            android.content.Intent it = new android.content.Intent(this, com.tac.Weathercast.activities.RadarActivity.class);
            try {
                it.putExtra("lat", todayWeather.getLat());
                it.putExtra("lon", todayWeather.getLon());
                it.putExtra("temp", todayWeather.getTemperature());
                it.putExtra("owm", todayWeather.getId());
            } catch (Exception ignored) {}
            startActivity(it);
        });

        wireTrend(R.id.tileWind, "WIND");
        wireTrend(R.id.tileHumidity, "HUMIDITY");
        wireTrend(R.id.tilePressure, "PRESSURE");
        wireTrend(R.id.heroCard, "TEMP");
        View gl = findViewById(R.id.glanceCard);
        if (gl != null) gl.setOnClickListener(x -> gotoBriefing());
        View uv = findViewById(R.id.tileUv);
        if (uv != null) uv.setOnClickListener(x -> gotoBriefing());
        View dl = findViewById(R.id.stats);
        if (dl != null) dl.setOnClickListener(x -> gotoBriefing());

        if (nav == null) return;
        nav.setSelectedItemId(R.id.nav_today);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_today && scroll != null && scroll.getVisibility() == View.VISIBLE) {
                scroll.smoothScrollTo(0, 0);
            }
            showTab(id);
            com.tac.Weathercast.utils.Anim.navPop(nav.findViewById(id));
            return true;
        });

        // Layered parallax — sky sits still, the temperature block drifts,
        // the sun illustration drifts faster, the date fades out first.
        if (scroll != null) {
            final View hero = findViewById(R.id.heroCard);
            final View date = findViewById(R.id.todayDate);
            final View city = findViewById(R.id.citytool);
            scroll.setOnScrollChangeListener((androidx.core.widget.NestedScrollView.OnScrollChangeListener)
                    (v, x, y, ox, oy) -> {
                        if (hero != null) {
                            hero.setTranslationY(y * 0.12f);
                            hero.setAlpha(Math.max(0f, 1f - y / 380f));
                        }
                        if (todayIcon != null) {
                            todayIcon.setTranslationY(y * 0.06f);
                        }
                        if (date != null) date.setAlpha(Math.max(0f, 1f - y / 220f));
                        if (city != null) city.setTranslationY(y * 0.06f);
                    });
        }
    }

    private void updateHourlyStrip() {
        try {
            androidx.recyclerview.widget.RecyclerView strip = findViewById(R.id.hourlyStrip);
            if (strip == null) return;
            java.util.List<com.tac.Weathercast.models.Weather> hourly = new ArrayList<>();
            long now = System.currentTimeMillis() - 3 * 3600_000L;
            for (com.tac.Weathercast.models.Weather w : longTermTodayWeather)
                if (w.getDate().getTime() >= now) hourly.add(w);
            for (com.tac.Weathercast.models.Weather w : longTermTomorrowWeather) {
                if (hourly.size() >= 12) break;
                hourly.add(w);
            }
            if (hourly.isEmpty()) { strip.setVisibility(View.GONE); return; }
            strip.setVisibility(View.VISIBLE);
            strip.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(
                    this, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false));
            strip.setAdapter(new com.tac.Weathercast.adapters.HourlyAdapter(this, hourly));

            // Pressure trend sparkline
            com.tac.Weathercast.utils.SparklineView spark = findViewById(R.id.pressureSpark);
            if (spark != null && longTermWeather != null && longTermWeather.size() >= 3) {
                int m = Math.min(14, longTermWeather.size());
                float[] p = new float[m];
                for (int i = 0; i < m; i++) {
                    try { p[i] = Float.parseFloat(longTermWeather.get(i).getPressure()); }
                    catch (Exception e) { p[i] = i > 0 ? p[i - 1] : 1013f; }
                }
                int hr = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                spark.setData(p, SomaTheme.forNow(hr, 800,
                        PreferenceManager.getDefaultSharedPreferences(this).getString("appearance", "auto")).heroAccent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private boolean shouldUpdate() {
        long lastUpdate = PreferenceManager.getDefaultSharedPreferences(this).getLong("lastUpdate", -1);
        boolean cityChanged = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("cityChanged", false);
        // Update if never checked or last update is longer ago than specified threshold
        return cityChanged || lastUpdate < 0 || (Calendar.getInstance().getTimeInMillis() - lastUpdate) > NO_UPDATE_REQUIRED_THRESHOLD;
    }


    private Menu optionsMenu;
    private int toolbarTint = 0xFF222222;
    private boolean firstThemeApply = true;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        optionsMenu = menu;

        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);

        try {
            int owmId = Integer.parseInt(todayWeather.getId());
            themeSearchAndToolbar(SomaTheme.forNow(
                    Calendar.getInstance().get(Calendar.HOUR_OF_DAY), owmId,
                    PreferenceManager.getDefaultSharedPreferences(this).getString("appearance", "auto"),
                    PreferenceManager.getDefaultSharedPreferences(this).getString("themeBase", "system")), false);
        } catch (Exception ignored) {}
        return true;
    }

    /** Tint the search field, its icons and the toolbar menu icons for the current
     *  palette — with a soft colour cross-fade when the theme changes. */
    private void themeSearchAndToolbar(SomaTheme t, boolean animate) {
        final int ink = 0xFFFFFFFF, muted = 0x9EFFFFFF;
        final int sheet = SomaTheme.blend(0xFF0E1526, t.sky[0], 0.35f) | 0xFF000000;

        // search field internals
        try {
            android.widget.EditText st = searchView.findViewById(R.id.searchTextView);
            View topBar = searchView.findViewById(R.id.search_top_bar);
            android.widget.ImageButton up = searchView.findViewById(R.id.action_up_btn);
            android.widget.ImageButton voice = searchView.findViewById(R.id.action_voice_btn);
            android.widget.ImageButton empty = searchView.findViewById(R.id.action_empty_btn);
            android.widget.ListView sugg = searchView.findViewById(R.id.suggestion_list);
            if (topBar != null) topBar.setBackgroundColor(sheet);
            if (sugg != null) sugg.setBackgroundColor(sheet);
            if (st != null) { st.setTextColor(ink); st.setHintTextColor(muted); }
            for (android.widget.ImageButton b : new android.widget.ImageButton[]{ up, voice, empty })
                if (b != null) b.setColorFilter(ink);
        } catch (Exception ignored) {}

        // toolbar menu icons
        final int from = toolbarTint, to = ink;
        toolbarTint = to;
        if (animate && from != to) {
            android.animation.ValueAnimator va = android.animation.ValueAnimator.ofArgb(from, to);
            va.setDuration(420);
            va.addUpdateListener(a -> tintMenu((int) a.getAnimatedValue()));
            va.start();
            View tb = findViewById(R.id.toolbar);
            if (tb != null) {
                tb.setAlpha(0.35f);
                tb.animate().alpha(1f).setDuration(420).start();
            }
        } else {
            tintMenu(to);
        }
    }

    private void tintMenu(int color) {
        if (optionsMenu == null) return;
        for (int i = 0; i < optionsMenu.size(); i++) {
            android.graphics.drawable.Drawable d = optionsMenu.getItem(i).getIcon();
            if (d != null) { d = d.mutate(); d.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN); }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


//
//        if (id == R.id.action_search) {
//            searchCities();
//            return true;
//        }
//        if (id == R.id.action_update) {
//            getCityByLocation();
//            return true;
//        }
        if(id==R.id.bottommenu) {

            ShowRoundDialogFragment showRoundDialogFragment =
                    ShowRoundDialogFragment.newInstance();

            showRoundDialogFragment.show(getSupportFragmentManager(),
                    "add_menu_fragment");
        }
        return super.onOptionsItemSelected(item);
    }

    public void refreshWeather() {
        if (isNetworkAvailable()) {
            getTodayWeather();
            getLongTermWeather();
            getTodayUVIndex();
            checkWeather();
        }
    }

    public static void initMappings() {
        if (mappingsInitialised)
            return;
        mappingsInitialised = true;
        speedUnits.put("m/s", R.string.speed_unit_mps);
        speedUnits.put("kph", R.string.speed_unit_kph);
        speedUnits.put("mph", R.string.speed_unit_mph);
        speedUnits.put("kn", R.string.speed_unit_kn);

        pressUnits.put("hPa", R.string.pressure_unit_hpa);
        pressUnits.put("kPa", R.string.pressure_unit_kpa);
        pressUnits.put("mm Hg", R.string.pressure_unit_mmhg);
    }

    private String localize(SharedPreferences sp, String preferenceKey, String defaultValueKey) {
        return localize(sp, this, preferenceKey, defaultValueKey);
    }

    public static String localize(SharedPreferences sp, Context context, String preferenceKey, String defaultValueKey) {
        String preferenceValue = sp.getString(preferenceKey, defaultValueKey);
        String result = preferenceValue;
        if ("speedUnit".equals(preferenceKey)) {
            if (speedUnits.containsKey(preferenceValue)) {
                result = context.getString(speedUnits.get(preferenceValue));
            }
        } else if ("pressureUnit".equals(preferenceKey)) {
            if (pressUnits.containsKey(preferenceValue)) {
                result = context.getString(pressUnits.get(preferenceValue));
            }
        }
        return result;
    }

    public static String getWindDirectionString(SharedPreferences sp, Context context, Weather weather) {
        try {
            if (Double.parseDouble(weather.getWind()) != 0) {
                String pref = sp.getString("windDirectionFormat", null);
                if ("arrow".equals(pref)) {
                    return weather.getWindDirection(8).getArrow(context);
                } else if ("abbr".equals(pref)) {
                    return weather.getWindDirection().getLocalizedString(context);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    void getCityByLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Explanation not needed, since user requests this themmself

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_ACCESS_FINE_LOCATION);
            }

        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            progressDialog = new ProgressDialog(this,R.style.CustomDialogTheme);
            progressDialog.setMessage(getString(R.string.getting_location));
            progressDialog.setCancelable(false);
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        locationManager.removeUpdates(MainActivity.this);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }
            });
            progressDialog.show();
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            }
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            }
        } else {
            showLocationSettingsDialog();
        }
    }

    private void showLocationSettingsDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this,R.style.CustomDialogTheme);
        TextView myMsg = new TextView(this);
        myMsg.setText("Turn on the Location.");
        myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsg.setTextSize(18);
        myMsg.setTextColor(Color.BLACK);
        alertDialog.setCustomTitle(myMsg);
        alertDialog.setPositiveButton(R.string.location_settings_button, (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        });
        alertDialog.setNegativeButton(R.string.dialog_cancel, (dialog, which) -> dialog.cancel());
        alertDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
                return;
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        progressDialog.hide();
        try {
            locationManager.removeUpdates(this);
        } catch (SecurityException e) {
            Log.e("LocationManager", "Error while trying to stop listening for location updates. This is probably a permissions issue", e);
        }
        Log.i("LOCATION (" + location.getProvider().toUpperCase() + ")", location.getLatitude() + ", " + location.getLongitude());
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        new ProvideCityNameTask(this, this, progressDialog).execute("coords", Double.toString(latitude), Double.toString(longitude));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }



    @Override
    public void onRefresh() {
        refreshWeather();
    }

    class TodayWeatherTask extends GenericRequestTask {
        public TodayWeatherTask(Context context, MainActivity activity, ProgressDialog progressDialog) {
            super(context, activity, progressDialog);
        }

        @Override
        protected void onPreExecute() {
            loading = 0;
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(TaskOutput output) {
            super.onPostExecute(output);
            // Update widgets
            AbstractWidgetProvider.updateWidgets(MainActivity.this);
            DashClockWeatherExtension.updateDashClock(MainActivity.this);
        }

        @Override
        protected ParseResult parseResponse(String response) {
            return parseTodayJson(response);
        }

        @Override
        protected String getAPIName() {
            return "weather";
        }

        @Override
        protected void updateMainUI() {
            updateTodayWeatherUI();
            updateLastUpdateTime();
            updateUVIndexUI();
        }
    }

    class LongTermWeatherTask extends GenericRequestTask {
        public LongTermWeatherTask(Context context, MainActivity activity, ProgressDialog progressDialog) {
            super(context, activity, progressDialog);
        }

        @Override
        protected ParseResult parseResponse(String response) {
            return parseLongTermJson(response);
        }

        @Override
        protected String getAPIName() {
            return "forecast";
        }

        @Override
        protected void updateMainUI() {
            updateLongTermWeatherUI();
        }
    }

    class FindCitiesByNameTask extends GenericRequestTask {

        public FindCitiesByNameTask(Context context, MainActivity activity, ProgressDialog progressDialog) {
            super(context, activity, progressDialog);
        }

        @Override
        protected void onPreExecute() { /*Nothing*/ }

        @Override
        protected ParseResult parseResponse(String response) {
            try {
                JSONObject reader = new JSONObject(response);

                final String code = reader.optString("cod");
                if ("404".equals(code)) {
                    Log.e("Geolocation", "No city found");
                    return ParseResult.CITY_NOT_FOUND;
                }

//                saveLocation(reader.getString("id"));
                final JSONArray cityList = reader.getJSONArray("list");

                if (cityList.length() > 1) {
                    launchLocationPickerDialog(cityList);
                } else {
                    saveLocation(cityList.getJSONObject(0).getString("id"));
                }

            } catch (JSONException e) {
                Log.e("JSONException Data", response);
                e.printStackTrace();
                return ParseResult.JSON_EXCEPTION;
            }

            return ParseResult.OK;
        }

        @Override
        protected String getAPIName() {
            return "find";
        }

        @Override
        protected void onPostExecute(TaskOutput output) {
            /* Handle possible errors only */
            handleTaskOutput(output);
            refreshWeather();

        }
    }

    private void launchLocationPickerDialog(JSONArray cityList) {
        AmbiguousLocationDialogFragment fragment = new AmbiguousLocationDialogFragment();
        Bundle bundle = new Bundle();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        bundle.putString("cityList", cityList.toString());
        fragment.setArguments(bundle);

        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.add(android.R.id.content, fragment)
                .addToBackStack(null).commit();
    }

    class ProvideCityNameTask extends GenericRequestTask {

        public ProvideCityNameTask(Context context, MainActivity activity, ProgressDialog progressDialog) {
            super(context, activity, progressDialog);
        }

        @Override
        protected void onPreExecute() { /*Nothing*/ }

        @Override
        protected String getAPIName() {
            return "weather";
        }

        @Override
        protected ParseResult parseResponse(String response) {
            Log.i("RESULT", response.toString());
            try {
                JSONObject reader = new JSONObject(response);

                final String code = reader.optString("cod");
                if ("404".equals(code)) {
                    Log.e("Geolocation", "No city found");
                    return ParseResult.CITY_NOT_FOUND;
                }

                saveLocation(reader.getString("id"));

            } catch (JSONException e) {
                Log.e("JSONException Data", response);
                e.printStackTrace();
                return ParseResult.JSON_EXCEPTION;
            }

            return ParseResult.OK;
        }

        @Override
        protected void onPostExecute(TaskOutput output) {
            /* Handle possible errors only */
            handleTaskOutput(output);

            refreshWeather();

        }
    }

    class TodayUVITask extends GenericRequestTask {
        public TodayUVITask(Context context, MainActivity activity, ProgressDialog progressDialog) {
            super(context, activity, progressDialog);
        }

        @Override
        protected void onPreExecute() {
            loading = 0;
            super.onPreExecute();
        }

        @Override
        protected ParseResult parseResponse(String response) {
            return parseTodayUVIJson(response);
        }

        @Override
        protected String getAPIName() {
            return "uvi";
        }

        @Override
        protected void updateMainUI() {
            updateUVIndexUI();
        }
    }

    public static long saveLastUpdateTime(SharedPreferences sp) {
        Calendar now = Calendar.getInstance();
        sp.edit().putLong("lastUpdate", now.getTimeInMillis()).commit();
        return now.getTimeInMillis();
    }

    private void updateLastUpdateTime() {
        updateLastUpdateTime(
                PreferenceManager.getDefaultSharedPreferences(this).getLong("lastUpdate", -1)
        );
    }

    private void updateLastUpdateTime(long timeInMillis) {
        if (timeInMillis < 0) {
            // No time
            lastUpdate.setText("");
        } else {
            lastUpdate.setText(formatTimeWithDayIfNotToday(this, timeInMillis));
        }
    }

    public static String formatTimeWithDayIfNotToday(Context context, long timeInMillis) {
        Calendar now = Calendar.getInstance();
        Calendar lastCheckedCal = new GregorianCalendar();
        lastCheckedCal.setTimeInMillis(timeInMillis);
        Date lastCheckedDate = new Date(timeInMillis);
        String timeFormat = android.text.format.DateFormat.getTimeFormat(context).format(lastCheckedDate);
        if (now.get(Calendar.YEAR) == lastCheckedCal.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == lastCheckedCal.get(Calendar.DAY_OF_YEAR)) {
            // Same day, only show time
            return timeFormat;
        } else {
            return android.text.format.DateFormat.getDateFormat(context).format(lastCheckedDate) + " " + timeFormat;
        }
    }



    @Override
    public void onGraphClick() {
        Intent intent = new Intent(MainActivity.this, GraphActivity.class);
        startActivity(intent);
    }

    @Override
    public void onUpdateClick() {
        getCityByLocation();
    }

    @Override
    public void onShareClick() {
        final String appPackageName = getApplicationContext().getPackageName();
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sendIntent.putExtra(Intent.EXTRA_TEXT, citytool.getText()+"  "+todayTemperature.getText()+" \n"+todayDescription.getText()+"\n\n For more weather updates, check this cool Weather app at: https://play.google.com/store/apps/details?id=" + appPackageName);
        sendIntent.setType("text/plain");
        this.startActivity(sendIntent);
    }

    @Override
    public void onSettingsClick() {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onAboutClick() {
        aboutDialog();
    }

    private String getTimeFromAndroid() {
        Date dt = new Date();
        String time="";
        Calendar c = Calendar.getInstance();
        c.setTime(dt);
        int hours = c.get(Calendar.HOUR_OF_DAY);
        if(hours>=1 && hours<5){
            time="dark";
        }else if(hours>=5 && hours<12){
            time="day";
        }
        else if(hours>=12 && hours<19){
            time="day";
        }else if(hours>=19 && hours<24){
            time="night";
        }
        return time;
    }
    private String getIntervalAndroid() {
        Date dt = new Date();
        String time="";
        Calendar c = Calendar.getInstance();
        c.setTime(dt);
        int hours = c.get(Calendar.HOUR_OF_DAY);
        if(hours>=1 && hours<5){
            time="night";
        }
        else if(hours>=5 && hours<=6) {
            time="dawn";
        }else if(hours>6 && hours<12){
            time="morning";
        }
        else if(hours>=12 && hours<16){
            time="afternoon";
        }else if(hours>=16 && hours<18){
            time="evening";
        }
        else if(hours>=18 && hours<19){
            time="dusk";
        }
        else if(hours>=19 && hours<=24){
            time="night";
        }
        return time;
    }
    private void checkWeather(){
        int owmId;
        try { owmId = Integer.parseInt(todayWeather.getId()); } catch (Exception e) { owmId = 800; }
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        boolean isNight = hour < 6 || hour >= 20;
        todayIcon.setImageResource(Formatting.somaIllustration(owmId, isNight));
        com.tac.Weathercast.utils.WeatherFxView fx = findViewById(R.id.weatherFx);
        if (fx != null) {
            int hr = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            fx.setWeather(owmId, isNight, SomaTheme.forNow(hr, owmId,
                    PreferenceManager.getDefaultSharedPreferences(this).getString("appearance", "auto")).heroAccent);
        }
        todayIcon.setScaleX(0.82f); todayIcon.setScaleY(0.82f); todayIcon.setAlpha(0f);
        todayIcon.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(420)
                .setInterpolator(new android.view.animation.OvershootInterpolator(1.4f)).start();
        applySomaTheme(owmId);

        int group = owmId / 100;
        String note;
        if (owmId == 800) note = isNight ? "Clear skies overhead tonight." : "Clear and open skies today.";
        else if (group == 2) note = "Thunderstorms with gusty wind and lightning likely.";
        else if (group == 3) note = "Light drizzle on and off through the day.";
        else if (group == 5) note = "Rain expected — roads will be slick.";
        else if (group == 6) note = "Snow expected — wrap up and mind your footing.";
        else if (group == 7) note = "Low visibility from haze and mist in places.";
        else if (owmId == 801 || owmId == 802) note = "A few clouds drifting through.";
        else if (group == 8) note = "Mostly grey with an overcast sky.";
        else note = todayWeather.getDescription();
        todaydes.setText(note);
    }

    private void checkWeatherLegacy(){
        String cond=todayWeather.getDescription().toLowerCase();
        String time=getTimeFromAndroid();
        if(cond.contains("thunderstorm")){
            todaydes.setText("Thunderstorm accompanied by gusty winds and lightning is expected in several parts.");
            if(time.equals("day")){
                todayIcon.setImageDrawable(getResources().getDrawable(R.drawable.stormday));

            }else if(time.equals("night")){
                todayIcon.setImageDrawable(getResources().getDrawable(R.drawable.stormnight));
            }
        }
        else if(cond.contains("drizzle")){
            todaydes.setText("Isolated Rain Cools down Few regions, While Others Remain Hotter");
            if(time.equals("day")){
                todayIcon.setImageDrawable(getResources().getDrawable(R.drawable.lightraindrops));
            }else if(time.equals("night")){
                todayIcon.setImageDrawable(getResources().getDrawable(R.drawable.lightrain));

            }
        }
        else if(cond.contains("rain")){
            todaydes.setText("Isolated Rain Cools down Few regions, While Others Remain Hotter");
                if(time.equals("day")){
                    todayIcon.setImageDrawable(getResources().getDrawable(R.drawable.stormday));
                    ;
                }else if(time.equals("night")){
                    todayIcon.setImageDrawable(getResources().getDrawable(R.drawable.rainnight));
                    ;
                }
        }
        else if(cond.contains("snow")){

            todaydes.setText("Time Lapse Ride Through Snow-Covered Streets of the city.");
            if(time.equals("day")){
                todayIcon.setImageDrawable(getResources().getDrawable(R.drawable.showersleet));
                ;
            }else if(time.equals("night")){
                todayIcon.setImageDrawable(getResources().getDrawable(R.drawable.snownight));
                ;
            }
        }
        else if(cond.contains("mist")){
            todaydes.setText("Time Lapse Ride Through mist covered areas of the city.");
            if(time.equals("day")){
                todayIcon.setImageDrawable(getResources().getDrawable(R.drawable.fog));
                ;
            }else if(time.equals("night")){
                todayIcon.setImageDrawable(getResources().getDrawable(R.drawable.mist));
                ;
            }
        }
        else if(cond.contains("smoke")){
            todaydes.setText("DUST STORM TO DETERIORATE AIR QUALITY IN SEVERAL REGION.");
            if(time.equals("day")){
                todayIcon.setImageDrawable(getResources().getDrawable(R.drawable.duskclouds));
                ;
            }else if(time.equals("night")){
                todayIcon.setImageDrawable(getResources().getDrawable(R.drawable.wind));
                ;
            }
        }
        else if(cond.contains("haze")||  cond.contains("clear")){
            todaydes.setText("Clear and blithe day with no clouds in the sky.");
            String interval=getIntervalAndroid();
            switch(interval){
                case "night":
                    todayIcon.setImageDrawable(getResources().getDrawable(R.drawable.mist));
                    ;
                    break;
                case "morning":
                    todayIcon.setImageDrawable(getResources().getDrawable(R.drawable.sunset));
                    ;
                    break;
                case "afternoon":
                    todayIcon.setImageDrawable(getResources().getDrawable(R.drawable.sunny));
                    ;
                    break;
                case "evening":
                    todayIcon.setImageDrawable(getResources().getDrawable(R.drawable.evening));
                    ;
                    break;
                case "dusk":
                    todayIcon.setImageDrawable(getResources().getDrawable(R.drawable.sunset));
                    break;
                case "dawn":
                    todayIcon.setImageDrawable(getResources().getDrawable(R.drawable.sunrise));
                    ;
                    break;
                    default:
                        break;
            }
        }
        else if(cond.equals("dust") || cond.contains("sand")){
            todaydes.setText("Dust storm to deteriorate air quality in several region.");
            if(time.equals("day")){
                todayIcon.setImageDrawable(getResources().getDrawable(R.drawable.dustday));

            }else if(time.equals("night")){
                todayIcon.setImageDrawable(getResources().getDrawable(R.drawable.sand));
                ;
            }
        }
        else if(cond.contains("fog")){
            todaydes.setText("Another foogy day with minimum temperature likely to go down.");
            if(time.equals("day")){
                todayIcon.setImageDrawable(getResources().getDrawable(R.drawable.fog));
                ;
            }else if(time.equals("night")){
                todayIcon.setImageDrawable(getResources().getDrawable(R.drawable.fognight));
                ;
            }
        }
        else if(cond.contains("ash")){
            todaydes.setText("Volcano violently erupts spewing ash and smoke into the sky.");
            if(time.equals("day")){
                todayIcon.setImageDrawable(getResources().getDrawable(R.drawable.duskclouds));
                ;
            }else if(time.equals("night")){
                todayIcon.setImageDrawable(getResources().getDrawable(R.drawable.fognight));
                ;
            }
        }
        else if(cond.contains("squall")){
            todaydes.setText("Sudden, sharp increase in wind speed lasting minutes with the possibility of rain.");
            if(time.equals("day")){
                todayIcon.setImageDrawable(getResources().getDrawable(R.drawable.squall));
                ;
            }else if(time.equals("night")){
                todayIcon.setImageDrawable(getResources().getDrawable(R.drawable.squall));
                ;
            }
        }
        else if(cond.contains("clouds")) {
            todaydes.setText("Fraction of the sky obscured by clouds, possibilty of rain.");
            if(cond.equals("few clouds")|| cond.equals("scattered clouds")){
                todayIcon.setImageDrawable(getResources().getDrawable(R.drawable.scatteredclouds));
            }
            else if(cond.equals("broken clouds")){
                todayIcon.setImageDrawable(getResources().getDrawable(R.drawable.brokenclouds));
            }
            else if(cond.equals("overcast clouds")){
                todayIcon.setImageDrawable(getResources().getDrawable(R.drawable.evening));
            }
        }


    }


}