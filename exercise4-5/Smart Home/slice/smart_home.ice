#ifndef SMART_HOME
#define SMART_HOME

[["java:package:pl.agh.edu"]]
[["python:package:gen"]]
module SmartHome {
    module Camera {
        enum Direction {
            NORTH, EAST, SOUTH, WEST
        };

        class CameraStatus {
            Direction direction;
            double zoom;
            optional(1) bool nightVision;
        };

        exception CameraException {
            string reason;
        };

        interface CameraService {
            void turn(Direction direction) throws CameraException;
            void zoom(double value) throws CameraException;
            CameraStatus status();
        };

        interface NightCameraService extends CameraService {
            void nightVision(bool value) throws CameraException;
        };
    };

    module Light {
        exception LightException {
            string reason;
        };

        struct Color {
            int red;
            int green;
            int blue;
        };

        struct LightSetting {
            Color color;
            bool on;
            long delay;
        };

        sequence<LightSetting> LightSequence;

        interface LightService {
            void setLight(bool value);
            bool isOn();
        };

        interface RGBLightService extends LightService {
            void setColor(Color color) throws LightException;
            Color getColor();
        };

        interface SequenceLightService extends RGBLightService {
            void setSequence(LightSequence lightSequence) throws LightException;
        };
    };
    
    exception NoAvailableDevicesException {
        string reason = "No available devices";
    };

    interface AvailableDevicesService {
        string listAvailableDevices() throws NoAvailableDevicesException;
    };
};

#endif