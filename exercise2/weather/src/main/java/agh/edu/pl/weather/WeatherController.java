package agh.edu.pl.weather;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class WeatherController {
    @Value("${API_KEY}")
    private String WEATHER_API_KEY;
    private final String WEATHER_API_URL = "http://api.weatherapi.com/v1";
    private Weather weather;
    private AirQuality airQuality;
    private Location location;
    private List<Weather> forecast;
    private final HttpClient client;

    public WeatherController() {
        client = HttpClient.newHttpClient();
    }

    @GetMapping(value = {"/", "/form"})
    public String form() {
        return "form";
    }

    @PostMapping(path = "/form_post")
    public String getFormData(@RequestParam String location, @RequestParam String type) {
        if (type.equals("current")) {
            if (location.isEmpty()) {
                return "redirect:/error400";
            }
            if (!getWeatherInfo(location)) {
                return "redirect:/error404";
            }
            return "redirect:/current_weather";
        } else if (type.equals("forecast")) {
            if (location.isEmpty()) {
                return "redirect:/error400";
            }
            if (!getForecastInfo(location)) {
                return "redirect:/error404";
            }
            return "redirect:/forecast";
        }
        return "form";
    }

    @GetMapping(path = "/current_weather")
    public String getCurrentWeather(Model model) {
        if (weather == null || location == null || airQuality == null) {
            return "redirect:/error400";
        }
        Information info = new Information(weather, airQuality, location);
        Map<String, Information> map = new HashMap<>();
        map.put("info", info);
        model.addAllAttributes(map);
        return "current_weather";
    }

    @GetMapping(path = "/forecast")
    public String getForecast(Model model) {
        if (forecast == null || location == null) {
            return "redirect:/error400";
        }
        Information info = new Information(location, forecast);
        Map<String, Information> map = new HashMap<>();
        map.put("info", info);
        model.addAllAttributes(map);
        return "forecast";
    }

    private boolean getWeatherInfo(String location) {
        weather = new Weather();
        airQuality = new AirQuality();
        this.location = new Location();
        HttpRequest request = HttpRequest.newBuilder(
                        URI.create(WEATHER_API_URL + "/current.json?key=" + WEATHER_API_KEY + "&q=" + location + "&aqi=yes"))
                .header("accept", "application/json")
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 && response.body() != null) {
                JSONObject jsonResponse = new JSONObject(response.body());
                this.location.setCity(jsonResponse.getJSONObject("location").getString("name"));
                this.location.setCountry(jsonResponse.getJSONObject("location").getString("country"));

                JSONObject currentWeather = jsonResponse.getJSONObject("current");
                weather.setTemperature(currentWeather.getDouble("temp_c"));
                weather.setFeelsLike(currentWeather.getDouble("feelslike_c"));
                weather.setCondition(currentWeather.getJSONObject("condition").getString("text"));
                weather.setConditionIcon(currentWeather.getJSONObject("condition").getString("icon"));
                weather.setWindSpeed(currentWeather.getDouble("wind_kph"));
                weather.setHumidity(currentWeather.getInt("humidity"));
                weather.setCloudCoverage(currentWeather.getInt("cloud"));

                JSONObject currentAirQuality = currentWeather.getJSONObject("air_quality");
                airQuality.setCo(currentAirQuality.getDouble("co"));
                airQuality.setO3(currentAirQuality.getDouble("o3"));
                airQuality.setNo2(currentAirQuality.getDouble("no2"));
                airQuality.setSo2(currentAirQuality.getDouble("so2"));
                airQuality.setPm2_5(currentAirQuality.getDouble("pm2_5"));
                airQuality.setPm10(currentAirQuality.getDouble("pm10"));
                airQuality.setQualityIndex(currentAirQuality.getInt("us-epa-index"));
                return true;
            } else {
                return false;
            }
        } catch (InterruptedException | IOException e) {
            return false;
        }
    }

    private boolean getForecastInfo(String location) {
        forecast = new ArrayList<>();
        this.location = new Location();
        HttpRequest request = HttpRequest.newBuilder(
                        URI.create(WEATHER_API_URL + "/forecast.json?key=" + WEATHER_API_KEY + "&q=" + location + "&days=3"))
                .header("accept", "application/json")
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 && response.body() != null) {
                JSONObject jsonLocation = new JSONObject(response.body()).getJSONObject("location");
                this.location.setCity(jsonLocation.getString("name"));
                this.location.setCountry(jsonLocation.getString("country"));
                JSONArray jsonResponse = new JSONObject(response.body()).getJSONObject("forecast").getJSONArray("forecastday");
                for (Object object: jsonResponse) {
                    JSONObject jsonWeather = (JSONObject) object;
                    Weather weather = new Weather();
                    weather.setDate(jsonWeather.getString("date"));
                    JSONObject day = jsonWeather.getJSONObject("day");
                    weather.setTemperature(day.getDouble("avgtemp_c"));
                    weather.setHumidity(day.getInt("avghumidity"));
                    weather.setCondition(day.getJSONObject("condition").getString("text"));
                    weather.setConditionIcon(day.getJSONObject("condition").getString("icon"));
                    forecast.add(weather);
                }
                return true;
            } else {
                return false;
            }
        } catch (InterruptedException | IOException e) {
            return false;
        }
    }
}
