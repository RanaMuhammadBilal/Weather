package com.bildroid.weather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import com.bildroid.weather.BuildConfig;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    ProgressBar loading;
    RelativeLayout Home;
    ImageView Background, SearchButton, Icon;
    TextView CityName, Temperature, Condition, FeelsLike, averageTemp, maxTemp, minTemp, humidity, clouds, rainChance, snowChance;
    TextView sunRise, sunSet, windSpeed;
    ImageButton refreshBtn;
    AutoCompleteTextView EditInput;
    RecyclerView RecycleWeather;
    LottieAnimationView weatherAnimation;
    ArrayList<WeatherModel> weatherModelArrayList;
    WeatherAdapter weatherAdapter;
    LocationManager locationManager;
    int PERMISSION_CODE=1;
    String cityName;
    Button refresh;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loading=findViewById(R.id.loading);
        Home=findViewById(R.id.Home);
        refreshBtn=findViewById(R.id.refresh_btn);
        clouds=findViewById(R.id.clouds);
        snowChance=findViewById(R.id.snowChance);
        rainChance=findViewById(R.id.rainChance);
        FeelsLike=findViewById(R.id.feelsLike);
        sunRise=findViewById(R.id.sunRise);
        windSpeed=findViewById(R.id.windKph);
        sunSet=findViewById(R.id.sunSet);
        averageTemp=findViewById(R.id.averageTemp);
        maxTemp=findViewById(R.id.maxTemp);
        humidity=findViewById(R.id.humididty);
        minTemp=findViewById(R.id.minTemp);
        SearchButton=findViewById(R.id.SearchButton);
        weatherAnimation = findViewById(R.id.weatherAnimation);
        CityName=findViewById(R.id.CityName);
        Temperature=findViewById(R.id.Temperature);
        Condition=findViewById(R.id.Condition);
        EditInput=findViewById(R.id.EditInput);
        RecycleWeather=findViewById(R.id.RecyclerWeather);
        weatherModelArrayList = new ArrayList<>();
        weatherAdapter =new WeatherAdapter(this,weatherModelArrayList);

        String[] itemArray = getResources().getStringArray(R.array.citiesList);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, itemArray);
        EditInput.setAdapter(arrayAdapter);

        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = CityName.getText().toString(); // Get the current city name
                if (city.isEmpty()) {
                    Toast.makeText(MainActivity.this, "City name is empty. Please search for a city first.", Toast.LENGTH_SHORT).show();
                } else {
                    getWeatherInfo(city); // Refresh weather info for the current city
                    Toast.makeText(MainActivity.this, "Weather data refreshed for " + city, Toast.LENGTH_SHORT).show();
                }
            }
        });



        EditInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                if (i == EditorInfo.IME_ACTION_DONE || i == EditorInfo.IME_ACTION_NEXT  ){

                    EditInput.setFocusable(false);

                    String city = EditInput.getText().toString();
                    if (city.isEmpty()){
                        Toast.makeText(MainActivity.this, "Please Enter City Name", Toast.LENGTH_SHORT).show();
                    }else {
                        CityName.setText(cityName);
                        getWeatherInfo(city);
                    }


                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(EditInput.getWindowToken(), 0);
                    EditInput.setFocusable(false);

                }


                return false;
            }
        });



        EditInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditInput.setFocusableInTouchMode(true);
            }
        });

        RecycleWeather.setAdapter(weatherAdapter);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION },PERMISSION_CODE);
        }

        Location locationManager1 = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (locationManager1 != null){
            cityName = "Lahore";
            getWeatherInfo(cityName);
        }
        getWeatherInfo(cityName);
        SearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(EditInput.getWindowToken(), 0);
                EditInput.setFocusable(false);


                String city = EditInput.getText().toString();
                if (city.isEmpty()){
                    Toast.makeText(MainActivity.this, "Please Enter City Name", Toast.LENGTH_SHORT).show();
                }else {
                    CityName.setText(cityName);
                    getWeatherInfo(city);
                }

            }
        });




    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==PERMISSION_CODE){
            if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission  granted", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "Please allow all permissions", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private String getCityName(double longitude, double latitude){
        String cityName = "Not Found";
        Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses = (geocoder.getFromLocation(longitude, latitude, 10));
            for(Address adr : addresses){
                if(adr!=null){
                    String city =adr.getLocality();
                    if (city!=null && city!=""){
                        cityName = city;
                    }else {
                        Log.d("TAG","City Not Found" );
                        Toast.makeText(this, "User City Not Found", Toast.LENGTH_SHORT).show();
                    }
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        return cityName;
    }


    public void getWeatherInfo(String cityName){

        String url = BuildConfig.API_KEY  +cityName + "&days=1&aqi=yes&alerts=yes";
        CityName.setText(cityName);
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loading.setVisibility(View.GONE);
                Home.setVisibility(View.VISIBLE);
                weatherModelArrayList.clear();


                try {
                    String temperature = response.getJSONObject("current").getString("temp_c");
                    String temperatureFeels = response.getJSONObject("current").getString("feelslike_c");
                    String cloudsS = response.getJSONObject("current").getString("cloud");
                    String humidityS = response.getJSONObject("current").getString("humidity");
                    String windSpeedS = response.getJSONObject("current").getString("wind_kph");
                    String maxTempS = response.getJSONObject("forecast")
                            .getJSONArray("forecastday")
                            .getJSONObject(0) // Access the first day
                            .getJSONObject("day")
                            .getString("maxtemp_c");
                    String minTempS = response.getJSONObject("forecast")
                            .getJSONArray("forecastday")
                            .getJSONObject(0) // Access the first day
                            .getJSONObject("day")
                            .getString("mintemp_c");
                    String averageTempS = response.getJSONObject("forecast")
                                    .getJSONArray("forecastday")
                                    .getJSONObject(0)
                                    .getJSONObject("day")
                                    .getString("avgtemp_c");
                    String rainChanceS = response.getJSONObject("forecast")
                            .getJSONArray("forecastday")
                            .getJSONObject(0)
                            .getJSONObject("day")
                            .getString("daily_chance_of_rain");
                    String snowChanceS = response.getJSONObject("forecast")
                            .getJSONArray("forecastday")
                            .getJSONObject(0)
                            .getJSONObject("day")
                            .getString("daily_chance_of_snow");
                    String sunRiseS = response.getJSONObject("forecast")
                            .getJSONArray("forecastday")
                            .getJSONObject(0)
                            .getJSONObject("astro")
                            .getString("sunrise");
                    String sunSetS = response.getJSONObject("forecast")
                            .getJSONArray("forecastday")
                            .getJSONObject(0)
                            .getJSONObject("astro")
                            .getString("sunset");

                    maxTemp.setText("Max Temp "+maxTempS+" ℃");
                    minTemp.setText("Min Temp "+minTempS+" ℃");
                    humidity.setText("Humidity "+humidityS+"%");
                    windSpeed.setText("Wind Speed "+windSpeedS+" kph");
                    clouds.setText("Clouds "+cloudsS+"%" );
                    sunRise.setText("Sunrise "+sunRiseS);
                    sunSet.setText("Sunset "+sunSetS);
                    rainChance.setText("Chances of rain "+rainChanceS+"%");
                    snowChance.setText("Chances of snow "+snowChanceS+"%");
                    averageTemp.setText("Average Temp "+averageTempS+" ℃");
                    FeelsLike.setText("Feels like "+temperatureFeels+" ℃");
                    Temperature.setText(temperature+"℃");
                    int isDay = response.getJSONObject("current").getInt("is_day");
                    String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                    if (condition.equalsIgnoreCase("Clear")) {
                        if(isDay==1){
                            weatherAnimation.setAnimation(R.raw.clear);
                        }else {
                            weatherAnimation.setAnimation(R.raw.clear_night);
                        }
                    } else if (condition.equalsIgnoreCase("Partly cloudy")) {
                        if (isDay==1){
                            weatherAnimation.setAnimation(R.raw.partialy_cloudy);
                        }else {
                            weatherAnimation.setAnimation(R.raw.partally_clouds_night);
                        }
                    } else if (condition.equalsIgnoreCase("Rain")) {
                        weatherAnimation.setAnimation(R.raw.rain);
                    } else if (condition.equalsIgnoreCase("Snow")) {
                        weatherAnimation.setAnimation(R.raw.snow);
                    } else if (condition.equalsIgnoreCase("Overcast")) {
                        weatherAnimation.setAnimation(R.raw.overcast);
                    } else if (condition.equalsIgnoreCase("Thunderstorm")) {
                        weatherAnimation.setAnimation(R.raw.thunderstrom);
                    } else if (condition.equalsIgnoreCase("Fog")) {
                        weatherAnimation.setAnimation(R.raw.fog);
                    } else if (condition.equalsIgnoreCase("Mist")) {
                        weatherAnimation.setAnimation(R.raw.mist);
                    } else if (condition.equalsIgnoreCase("Light snow")) {
                        weatherAnimation.setAnimation(R.raw.snow);
                    }else if (condition.equalsIgnoreCase("Light rain")) {
                        weatherAnimation.setAnimation(R.raw.rain);
                    }else if (condition.equalsIgnoreCase("Heavy rain")) {
                        weatherAnimation.setAnimation(R.raw.rain);
                    }else if (condition.equalsIgnoreCase("Heavy snow")) {
                        weatherAnimation.setAnimation(R.raw.snow);
                    }else if (condition.equalsIgnoreCase("Patchy light rain")) {
                        weatherAnimation.setAnimation(R.raw.rain);
                    } else {
                        if(isDay==1){
                            weatherAnimation.setAnimation(R.raw.clear);
                        }else {
                            weatherAnimation.setAnimation(R.raw.clear_night);
                        } // Default animation
                    }

                    weatherAnimation.playAnimation();
                    Condition.setText(condition);
                    if (isDay==1){
                        //morning
                        Home.setBackgroundResource(R.drawable.day5);
                    }else {
                        Home.setBackgroundResource(R.drawable.night);
                    }

                    JSONObject forecastObj = response.getJSONObject("forecast");
                    JSONObject forecast0 = forecastObj.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourArray = forecast0.getJSONArray("hour");

                    for (int i=0; i <hourArray.length(); i++){

                        JSONObject hourObj = hourArray.getJSONObject(i);
                        String time = hourObj.getString("time");
                        String temp = hourObj.getString("temp_c");
                        String img = hourObj.getJSONObject("condition").getString("icon");
                        String wind = hourObj.getString("wind_kph");
                        weatherModelArrayList.add(new WeatherModel(time,temp,img,wind));
                    }
                    weatherAdapter.notifyDataSetChanged();




                } catch (JSONException e) {
                    throw new RuntimeException(e);

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Please Enter Valid City Name", Toast.LENGTH_SHORT).show();
            }
        });
            requestQueue.add(jsonObjectRequest);

    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder extDialog = new AlertDialog.Builder(this);

        extDialog.setIcon(R.drawable.baseline_exit_to_app_24);
        extDialog.setTitle("Exit");
        extDialog.setMessage("are you sure you want to exit?");
        extDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.super.onBackPressed();
            }
        });
        extDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //null
            }
        });

        extDialog.show();

    }
}