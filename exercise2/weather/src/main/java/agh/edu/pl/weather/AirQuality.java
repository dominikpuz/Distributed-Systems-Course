package agh.edu.pl.weather;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;

@Setter
@Getter
public class AirQuality {
    private double co;
    private double o3;
    private double no2;
    private double so2;
    private double pm2_5;
    private double pm10;
    private int qualityIndex;
    private final String[] airQuality = {"Good", "Moderate", "Unhealthy for sensitive groups",
                                        "Unhealthy", "Very unhealthy", "Hazardous"};

    public String getAirQuality() {
        if (qualityIndex > 0 && qualityIndex < airQuality.length) {
            return airQuality[qualityIndex - 1];
        }
        return "";
    }

    @Override
    public String toString() {
        return "AirQuality{" +
                "co=" + co +
                ", o3=" + o3 +
                ", no2=" + no2 +
                ", so2=" + so2 +
                ", pm2_5=" + pm2_5 +
                ", pm10=" + pm10 +
                ", qualityIndex=" + qualityIndex +
                ", airQuality=" + Arrays.toString(airQuality) +
                '}';
    }
}
