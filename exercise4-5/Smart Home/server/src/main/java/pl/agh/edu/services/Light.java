package pl.agh.edu.services;

import com.zeroc.Ice.Current;
import pl.agh.edu.Device;
import pl.agh.edu.SmartHome.Light.LightService;

public class Light implements LightService, Device {
    protected final String name;
    protected boolean on;

    public Light(String name) {
        this.name = name;
        this.on = false;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setLight(boolean value, Current current) {
        on = value;
        System.out.println(name + "\n" + "Is on?: " + on);
    }

    @Override
    public boolean isOn(Current current) {
        return on;
    }
}
