package pl.agh.edu.services;

import com.zeroc.Ice.Current;
import pl.agh.edu.Device;
import pl.agh.edu.SmartHome.Light.Color;
import pl.agh.edu.SmartHome.Light.LightException;
import pl.agh.edu.SmartHome.Light.RGBLightService;

public class RGBLight extends Light implements RGBLightService, Device {
    protected Color color;

    public RGBLight(String name) {
        super(name);
        this.color = new Color(255,255,255);
    }

    @Override
    public void setColor(Color color, Current current) throws LightException {
        if (color == null) throw new LightException("Color is null");
        if (color.red > 255 || color.green > 255 || color.blue > 255 || color.red < 0 || color.green < 0 || color.blue < 0) {
            throw new LightException("Invalid color");
        }
        this.color = color;
        System.out.println(name + "\n" + "Color: " + "color: " + this.color.red + ", " + this.color.green + ", " + this.color.blue);
    }

    @Override
    public Color getColor(Current current) {
        return color;
    }
}
