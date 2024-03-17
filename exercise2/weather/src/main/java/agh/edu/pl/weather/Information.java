package agh.edu.pl.weather;

import lombok.Getter;

import java.util.List;

@Getter
public class Information {
    private Weather weather;
    private AirQuality airQuality;
    private Location location;
    private List<Weather> forecast;

    public Information(Weather weather, AirQuality airQuality, Location location) {
        this.weather = weather;
        this.airQuality = airQuality;
        this.location = location;
    }

    public Information(Location location, List<Weather> forecast) {
        this.location = location;
        this.forecast = forecast;
    }

    public double getAvgTemp() {
        return Math.round(forecast.stream().mapToDouble(Weather::getTemperature).average().orElse(0) * 100.0) / 100.0;
    }
}
