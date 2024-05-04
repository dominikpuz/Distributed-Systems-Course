package pl.agh.edu;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Identity;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;
import pl.agh.edu.services.*;

import java.util.ArrayList;
import java.util.List;


public class Main {
    public static void main(String[] args) {
        Communicator comunicator = null;
        try {
            comunicator = Util.initialize(args);
            String adapterName = System.getenv("adapter");
            ObjectAdapter adapter = comunicator.createObjectAdapter(adapterName);
            List<Device> devices = new ArrayList<>();
            if (adapterName.equals("Adapter1")) {
                Camera camera = new Camera("Camera");
                devices.add(camera);

                Light light = new Light("Light");
                devices.add(light);

                RGBLight rgbLight = new RGBLight("RGBLight");
                devices.add(rgbLight);

                SequenceLight sequenceLight = new SequenceLight("SequenceLight");
                devices.add(sequenceLight);

                AvailableDevices availableDevices = new AvailableDevices(devices);

                adapter.add(camera, new Identity(camera.getName(), "SmartHome"));
                adapter.add(light, new Identity(light.getName(), "SmartHome"));
                adapter.add(rgbLight, new Identity(rgbLight.getName(), "SmartHome"));
                adapter.add(sequenceLight, new Identity(sequenceLight.getName(), "SmartHome"));
                adapter.add(availableDevices, new Identity("AvailableDevices", "SmartHome"));
            } else if (adapterName.equals("Adapter2")) {
                NightCamera nightCamera = new NightCamera("NightCamera");
                devices.add(nightCamera);

                RGBLight rgbLight = new RGBLight("RGBLight");
                devices.add(rgbLight);

                SequenceLight sequenceLight = new SequenceLight("SequenceLight");
                devices.add(sequenceLight);

                AvailableDevices availableDevices = new AvailableDevices(devices);

                adapter.add(nightCamera, new Identity(nightCamera.getName(), "SmartHome"));
                adapter.add(rgbLight, new Identity(rgbLight.getName(), "SmartHome"));
                adapter.add(sequenceLight, new Identity(sequenceLight.getName(), "SmartHome"));
                adapter.add(availableDevices, new Identity("AvailableDevices", "SmartHome"));
            }

            adapter.activate();

            System.out.println("Entering event processing loop...");

            comunicator.waitForShutdown();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        } finally {
            if (comunicator != null) {
                comunicator.destroy();
            }
        }
    }
}