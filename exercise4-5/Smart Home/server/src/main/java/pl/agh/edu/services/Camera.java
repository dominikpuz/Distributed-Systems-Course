package pl.agh.edu.services;

import com.zeroc.Ice.Current;
import pl.agh.edu.Device;
import pl.agh.edu.SmartHome.Camera.CameraException;
import pl.agh.edu.SmartHome.Camera.CameraService;
import pl.agh.edu.SmartHome.Camera.CameraStatus;
import pl.agh.edu.SmartHome.Camera.Direction;

public class Camera implements CameraService, Device {
    protected final CameraStatus cameraStatus;
    protected final String name;

    public Camera(String name) {
        this.cameraStatus = new CameraStatus(Direction.NORTH, 0.0);
        this.name = name;
    }

    @Override
    public void turn(Direction direction, Current current) throws CameraException {
        if (cameraStatus.direction.equals(direction)) {
            throw new CameraException("Camera is already turned in direction: " + direction);
        }
        cameraStatus.direction = direction;
        System.out.println(name + "\n" + "Current direction: " + cameraStatus.direction);
    }

    @Override
    public void zoom(double value, Current current) throws CameraException {
        if (value < 0.0 || value > 3.0) {
            throw new CameraException("Invalid zoom value: " + value);
        }
        cameraStatus.zoom = value;
        System.out.println(name + "\n" + "Current zoom: " + cameraStatus.zoom);

    }

    @Override
    public CameraStatus status(Current current) {
        return cameraStatus;
    }

    @Override
    public String getName() {
        return name;
    }
}
