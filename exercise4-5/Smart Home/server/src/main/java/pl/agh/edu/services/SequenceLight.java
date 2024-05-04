package pl.agh.edu.services;

import com.zeroc.Ice.Current;
import pl.agh.edu.Device;
import pl.agh.edu.SmartHome.Light.LightException;
import pl.agh.edu.SmartHome.Light.LightSetting;
import pl.agh.edu.SmartHome.Light.SequenceLightService;

public class SequenceLight extends RGBLight implements SequenceLightService, Device {
    public SequenceLight(String name) {
        super(name);
    }

    @Override
    public void setSequence(LightSetting[] lightSequence, Current current) throws LightException {
        for (LightSetting lightSetting : lightSequence) {
            setLight(lightSetting.on, current);
            setColor(lightSetting.color, current);
            System.out.println(name + "\n" + "Current configuration:\n" + "Is on?: " + on + "\n" + "color: " + color.red + ", " + color.green + ", " + color.blue);
            try {
                Thread.sleep(lightSetting.delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
