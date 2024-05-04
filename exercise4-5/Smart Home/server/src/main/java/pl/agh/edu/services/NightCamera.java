package pl.agh.edu.services;

import com.zeroc.Ice.Current;
import pl.agh.edu.SmartHome.Camera.CameraException;
import pl.agh.edu.SmartHome.Camera.NightCameraService;

public class NightCamera extends Camera implements NightCameraService {

    public NightCamera(String name) {
        super(name);
        this.cameraStatus.setNightVision(false);
    }

    @Override
    public void nightVision(boolean value, Current current) throws CameraException {
        if (cameraStatus.getNightVision() == value) {
            throw new CameraException("Night Vision is already set to " + value);
        }
        cameraStatus.setNightVision(value);
        System.out.println(name + "\n" + "Is night vision on?: " + cameraStatus.getNightVision());
    }
}
