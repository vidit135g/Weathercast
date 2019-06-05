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
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.tac.Weathercast.Constants;
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
    private ImageView todayIcon;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private RelativeLayout mainLay;
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
        mainLay=findViewById(R.id.main);
        citytool=findViewById(R.id.citytool);
        peekLayout=findViewById(R.id.peeklayout);
        todayIcon = findViewById(R.id.todayIcon);
        currdate=findViewById(R.id.todayDate);
        ViewPagerBottomSheetBehavior behavior = ViewPagerBottomSheetBehavior.from(peekLayout);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;
        int orientation = getResources().getConfiguration().orientation;


        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // In landscape
            behavior.setPeekHeight((height/6-80));
        } else {
            // In portrait
            behavior.setPeekHeight((height/4+50));
        }

        searchView=findViewById(R.id.search_view);

        String timeStamp = new SimpleDateFormat("EEEE  dd  MMMM  yyyy").format(Calendar.getInstance().getTime());
        currdate.setText(timeStamp);// Get Date String according to date format

        citytool.setText("Lucknow");
        todayIcon.setImageDrawable(getResources().getDrawable(R.drawable.sleet));
        todaydes.setText("Haze, pollution causing low visibility over several parts.");
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
                //Do some magic
                new FindCitiesByNameTask(getApplicationContext(),
                        MainActivity.this, progressDialog).execute("city", query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Do some magic
                return false;
            }
        });


        searchView.setSuggestions(getResources().getStringArray(R.array.query_suggestions));
        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                //Do some magic
            }

            @Override
            public void onSearchViewClosed() {
                //Do some magic
            }
        });

    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);

        new Handler().postDelayed(() -> doubleBackToExitPressedOnce=false, 2000);
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
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
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        String lastUVIToday = sp.getString("lastToday", "");
        if (!lastUVIToday.isEmpty()) {
            double latitude = todayWeather.getLat();
            double longitude = todayWeather.getLon();
            if (latitude == 0 && longitude == 0) {
                return;
            }
            new TodayUVITask(this, this, progressDialog).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "coords", Double.toString(latitude), Double.toString(longitude));
        }
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

    private void getTodayUVIndex() {
        double latitude = todayWeather.getLat();
        double longitude = todayWeather.getLon();
        new TodayUVITask(this, this, progressDialog).execute("coords", Double.toString(latitude), Double.toString(longitude));
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
        recentCityId = preferences.getString("cityId", "India");

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
        citytool.setText(city +" ,"+country);

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

        todayTemperature.setText(new DecimalFormat("0.#").format(temperature) + "\u00b0");
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
        todaySunrise.setText(timeFormat.format(todayWeather.getSunrise()));
        todaySunset.setText(timeFormat.format(todayWeather.getSunset()));
        citytool=findViewById(R.id.citytool);
        citytool.setText(city);
        checkWeather();
        todayIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, GraphActivity.class);
                startActivity(intent);
            }
        });
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
        if (destroyed) {
            return;
        }

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        Bundle bundleToday = new Bundle();
        bundleToday.putInt("day", 0);
        RecyclerViewFragment recyclerViewFragmentToday = new RecyclerViewFragment();
        recyclerViewFragmentToday.setArguments(bundleToday);
        viewPagerAdapter.addFragment(recyclerViewFragmentToday, getString(R.string.today));

        Bundle bundleTomorrow = new Bundle();
        bundleTomorrow.putInt("day", 1);
        RecyclerViewFragment recyclerViewFragmentTomorrow = new RecyclerViewFragment();
        recyclerViewFragmentTomorrow.setArguments(bundleTomorrow);
        viewPagerAdapter.addFragment(recyclerViewFragmentTomorrow, getString(R.string.tomorrow));

        Bundle bundle = new Bundle();
        bundle.putInt("day", 2);
        RecyclerViewFragment recyclerViewFragment = new RecyclerViewFragment();
        recyclerViewFragment.setArguments(bundle);
        viewPagerAdapter.addFragment(recyclerViewFragment, getString(R.string.later));

        int currentPage = viewPager.getCurrentItem();

        viewPagerAdapter.notifyDataSetChanged();
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        if (currentPage == 0 && longTermTodayWeather.isEmpty()) {
            currentPage = 1;
        }
        viewPager.setCurrentItem(currentPage, false);
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);

        return true;
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
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_ACCESS_FINE_LOCATION);
            }

        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            progressDialog = new ProgressDialog(this);
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
        alertDialog.setTitle(R.string.location_settings);
        alertDialog.setMessage(R.string.location_settings_message);
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
            lastUpdate.setText(getString(R.string.last_update, formatTimeWithDayIfNotToday(this, timeInMillis)));
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