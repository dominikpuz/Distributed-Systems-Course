package agh.edu.pl.weather;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Weather {
    private double temperature;
    private double feelsLike;
    private String condition;
    private String conditionIcon;
    private double windSpeed;
    private int humidity;
    private int cloudCoverage;
    private String date;

    @Override
    public String toString() {
        return "Weather{" +
                "temperature=" + temperature +
                ", feelsLike=" + feelsLike +
                ", condition='" + condition + '\'' +
                ", conditionIcon='" + conditionIcon + '\'' +
                ", windSpeed=" + windSpeed +
                ", humidity=" + humidity +
                ", cloudCoverage=" + cloudCoverage +
                '}';
    }
}
