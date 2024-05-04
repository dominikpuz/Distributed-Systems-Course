package pl.agh.edu.services;

import com.zeroc.Ice.Current;
import pl.agh.edu.Device;
import pl.agh.edu.SmartHome.AvailableDevicesService;
import pl.agh.edu.SmartHome.NoAvailableDevicesException;

import java.util.List;

public class AvailableDevices implements AvailableDevicesService {
    private final List<Device> devices;

    public AvailableDevices(List<Device> devices) {
        this.devices = devices;
    }

    @Override
    public String listAvailableDevices(Current current) throws NoAvailableDevicesException {
        if (devices.isEmpty()) {
            throw new NoAvailableDevicesException();
        }
        StringBuilder result = new StringBuilder("Available devices:\n");
        for (Device device : devices) {
            result.append("\t").append(device.getName()).append("\n");
        }
        return result.toString();
    }
}
