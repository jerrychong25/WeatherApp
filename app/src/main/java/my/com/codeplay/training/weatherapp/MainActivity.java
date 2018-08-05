package my.com.codeplay.training.weatherapp;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.GET;

public class MainActivity extends AppCompatActivity {

    private TextView tvLocation, tvTemperature, tvHumidity, tvWindSpeed, tvCloudiness;
    private Button btnRefresh;
    private ImageView ivIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvLocation = (TextView) findViewById(R.id.location);
        tvTemperature = (TextView) findViewById(R.id.temperature);
        tvHumidity = (TextView) findViewById(R.id.humidity);
        tvWindSpeed = (TextView) findViewById(R.id.wind_speed);
        tvCloudiness = (TextView) findViewById(R.id.cloudiness);
        btnRefresh = (Button) findViewById(R.id.button_refresh);
        ivIcon = (ImageView) findViewById(R.id.icon);

        // Set Background Colour To Light Blue
        View root = this.getWindow().getDecorView();
        root.setBackgroundColor(Color.BLUE);

//        // Get latest weather data during initialization
//        WeatherDataRetrival weatherDataRetrival = new WeatherDataRetrival();
//        weatherDataRetrival.execute();

        // Get latest weather data if Refresh button is clicked
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast toast = new Toast(MainActivity.this);
                toast.makeText(MainActivity.this, "Refresh Button is pressed!",
                        Toast.LENGTH_SHORT).show();

                // Method 1 - Original Connection
                WeatherDataRetrival weatherDataRetrival = new WeatherDataRetrival();
                weatherDataRetrival.execute();

                // Method 2 - Retrofit HTTP Client Connection
                Retrofit request = new Retrofit.Builder().baseUrl("http://api.openweathermap.org/").addConverterFactory(ScalarsConverterFactory.create()).build();
                ApiProvider api = request.create(ApiProvider.class);
                Call<String> call = api.getWeatherData();
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if(response.isSuccessful()) { // responseCode 200-300
                            parseResult(response.body());
                        } else {

                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {

                    }
                });
            }
        });
    }

    interface ApiProvider {
        @GET("/data/2.5/weather?APPID=82445b6c96b99bc3ffb78a4c0e17fca5&mode=json&id=1735161")
        Call<String> getWeatherData();
    }

    private class WeatherDataRetrival extends AsyncTask<Void, Void, String> {

        private static final String WEATHER_SOURCE = "http://api.openweathermap.org/data/2.5/weather?APPID=82445b6c96b99bc3ffb78a4c0e17fca5&mode=json&id=1735161";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... arg0) {

            NetworkInfo networkInfo = ((ConnectivityManager) MainActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
            if(networkInfo != null && networkInfo.isConnected()) {
                // Network Connected
                Log.d("Network", "Network Connected!");

                URL url = null;
                try {
                    url = new URL(WEATHER_SOURCE);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                HttpURLConnection conn = null;
                try {
                    conn = (HttpURLConnection) url.openConnection();
                    Log.d("Network", "Connect Open!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    conn.setRequestMethod("GET");
                    Log.d("Network", "Connect GET!");
                } catch (ProtocolException e) {
                    e.printStackTrace();
                }

                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);

                try {
                    conn.connect();
                    Log.d("Network", "Connect Now!");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    int responseCode = conn.getResponseCode();

                    Log.d("Network", "Connect Get Response!");
                    Log.d("Network", "Responsde Code: "+ String.valueOf(responseCode));

                    if(responseCode == HttpURLConnection.HTTP_OK) {

                        Log.d("Network", "Connect HTTP OK!");

                        BufferedReader bufferedReader = new BufferedReader(
                                new InputStreamReader(conn.getInputStream()));

                        Log.d("Data", "bufferedReader: " + bufferedReader);

                        if (bufferedReader == null) {
                            Log.d("Data", "bufferedReader is null! " );
                        }

                        else if (bufferedReader != null) {

                            Log.d("Data", "bufferedReader not null! " );

                            String readline;
                            StringBuffer stringBuffer = new StringBuffer();

                            while ((readline = bufferedReader.readLine()) != null) {
                                stringBuffer.append(readline);
                            }

                            Log.d("Data", "readline not null! " );

                            return stringBuffer.toString();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                // No Connection
                Log.d("Network", "Network Not Connected!");
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if(result == null) {
                Log.d("Data", "result is null!");
            }

            else if(result != null) {
                Log.d("Data", "result not null!");

                try {
                    final JSONObject weatherJSON = new JSONObject(result);

                    tvLocation.setText(weatherJSON.getString("name") + "," + weatherJSON.getJSONObject("sys").getString("country"));
                    Log.d("Data", "tvLocation: " + weatherJSON.getString("name") + "," + weatherJSON.getJSONObject("sys").getString("country"));

                    tvWindSpeed.setText(String.valueOf(weatherJSON.getJSONObject("wind").getDouble("speed")) + " mps");
                    Log.d("Data", "tvWindSpeed: " + String.valueOf(weatherJSON.getJSONObject("wind").getDouble("speed")) + " mps");

                    tvCloudiness.setText(String.valueOf(weatherJSON.getJSONObject("clouds").getInt("all")) + "%");
                    Log.d("Data", "tvCloudiness: " + String.valueOf(weatherJSON.getJSONObject("clouds").getInt("all")) + "%");

                    final JSONObject mainJSON = weatherJSON.getJSONObject("main");

                    tvTemperature.setText(String.valueOf(Math.round(mainJSON.getDouble("temp")-273)));
                    Log.d("Data", "tvTemperature: " + String.valueOf(mainJSON.getDouble("temp")));

                    tvHumidity.setText(String.valueOf(mainJSON.getInt("humidity")) + "%");
                    Log.d("Data", "tvHumidity: " + String.valueOf(mainJSON.getInt("humidity")) + "%");

                    final JSONArray weatherJSONArray = weatherJSON.getJSONArray("weather");
                    if(weatherJSONArray.length()>0) {
                        int code = weatherJSONArray.getJSONObject(0).getInt("id");
                        Log.d("Data", "code: " + String.valueOf(code));

                        getIcon(code);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private int getIcon (int code) {

        switch (code) {
            // Thunderstorm
            case 200:
            case 201:
            case 202:
            case 210:
            case 211:
            case 212:
            case 221:
            case 230:
            case 231:
            case 232:
                Log.d("Icon", "Icon Set Thunderstorm!");
                ivIcon.setImageResource(R.mipmap.ic_thunderstorm_large);
                return 1;

            // Drizzle
            case 300:
            case 301:
            case 302:
            case 310:
            case 311:
            case 312:
            case 313:
            case 314:
            case 321:
                Log.d("Icon", "Icon Set Drizzle!");
                ivIcon.setImageResource(R.mipmap.ic_drizzle_large);
                return 2;

            // Rain
            case 500:
            case 501:
            case 502:
            case 503:
            case 504:
            case 511:
            case 520:
            case 521:
            case 522:
            case 531:
                Log.d("Icon", "Icon Set Rain!");
                ivIcon.setImageResource(R.mipmap.ic_rain_large);
                return 3;

            // Snow
            case 600:
            case 601:
            case 602:
            case 611:
            case 612:
            case 615:
            case 616:
            case 620:
            case 621:
            case 622:
                Log.d("Icon", "Icon Set Snow!");
                ivIcon.setImageResource(R.mipmap.ic_snow_large);
                return 4;

            // Clear Sky
            case 800:
                Log.d("Icon", "Icon Set Clear Sky!");
                ivIcon.setImageResource(R.mipmap.ic_day_clear_large);
                return 5;

            // Few Clouds
            case 801:
                Log.d("Icon", "Icon Set Few Clouds!");
                ivIcon.setImageResource(R.mipmap.ic_day_few_clouds_large);
                return 6;

            // Scattered Clouds
            case 802:
                Log.d("Icon", "Icon Set Drizzle!");
                ivIcon.setImageResource(R.mipmap.ic_scattered_clouds_large);
                return 7;

            // Broken and Overcast Clouds
            case 803:
            case 804:
                Log.d("Icon", "Icon Set Broken and Overcast Clouds!");
                ivIcon.setImageResource(R.mipmap.ic_broken_clouds_large);
                return 8;

            // Fog
            case 701:
            case 711:
            case 721:
            case 731:
            case 741:
            case 751:
            case 761:
            case 762:
                Log.d("Icon", "Icon Set Fog!");
                ivIcon.setImageResource(R.mipmap.ic_fog_large);
                return 9;

            // Tornado
            case 781:
            case 900:
                Log.d("Icon", "Icon Set Tornado!");
                ivIcon.setImageResource(R.mipmap.ic_tornado_large);
                return 10;

            // Windy
            case 905:
                Log.d("Icon", "Icon Set Windy!");
                ivIcon.setImageResource(R.mipmap.ic_windy_large);
                return 11;

            // Hail
            case 906:
                Log.d("Icon", "Icon Set Hail!");
                ivIcon.setImageResource(R.mipmap.ic_hail_large);
                return 12;
        }

        return 0;
    }

    private void parseResult (String result) {

        if(result == null) {
            Log.d("Data", "result is null!");
        }

        else if(result != null) {
            Log.d("Data", "result not null!");

            try {
                final JSONObject weatherJSON = new JSONObject(result);

                tvLocation.setText(weatherJSON.getString("name") + "," + weatherJSON.getJSONObject("sys").getString("country"));
                Log.d("Data", "tvLocation: " + weatherJSON.getString("name") + "," + weatherJSON.getJSONObject("sys").getString("country"));

                tvWindSpeed.setText(String.valueOf(weatherJSON.getJSONObject("wind").getDouble("speed")) + " mps");
                Log.d("Data", "tvWindSpeed: " + String.valueOf(weatherJSON.getJSONObject("wind").getDouble("speed")) + " mps");

                tvCloudiness.setText(String.valueOf(weatherJSON.getJSONObject("clouds").getInt("all")) + "%");
                Log.d("Data", "tvCloudiness: " + String.valueOf(weatherJSON.getJSONObject("clouds").getInt("all")) + "%");

                final JSONObject mainJSON = weatherJSON.getJSONObject("main");

                tvTemperature.setText(String.valueOf(Math.round(mainJSON.getDouble("temp")-273)));
                Log.d("Data", "tvTemperature: " + String.valueOf(mainJSON.getDouble("temp")));

                tvHumidity.setText(String.valueOf(mainJSON.getInt("humidity")) + "%");
                Log.d("Data", "tvHumidity: " + String.valueOf(mainJSON.getInt("humidity")) + "%");

                final JSONArray weatherJSONArray = weatherJSON.getJSONArray("weather");
                if(weatherJSONArray.length()>0) {
                    int code = weatherJSONArray.getJSONObject(0).getInt("id");
                    Log.d("Data", "code: " + String.valueOf(code));

                    getIcon(code);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
