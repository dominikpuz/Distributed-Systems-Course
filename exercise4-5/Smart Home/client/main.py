import sys
import Ice
import gen.SmartHome as SmartHome


def turn_camera(obj):
    direction = input("Choose direction: ")
    match direction.upper():
        case "NORTH":
            obj.turn(SmartHome.Camera.Direction.NORTH)
        case "SOUTH":
            obj.turn(SmartHome.Camera.Direction.SOUTH)
        case "EAST":
            obj.turn(SmartHome.Camera.Direction.EAST)
        case "WEST":
            obj.turn(SmartHome.Camera.Direction.WEST)
        case _:
            print("Invalid direction")


def zoom_camera(obj):
    value = float(input("Choose zoom: "))
    obj.zoom(value)


def set_light(obj):
    value = bool(input("Turn on light?: "))
    obj.setLight(value)


def get_color():
    color = input("Choose color: ")
    color_split = color.split(" ")
    color = SmartHome.Light.Color(int(color_split[0]), int(color_split[1]), int(color_split[2]))
    return color


def set_sequence(obj):
    n = int(input("Sequence len: "))
    sequence = []
    for i in range(n):
        on = bool(input("Turn on light?: "))
        color = get_color()
        delay = int(input("Sequence delay: "))
        setting = SmartHome.Light.LightSetting(color, on, delay)
        sequence.append(setting)
    obj.setSequenceAsync(sequence)


def client():
    with Ice.initialize(sys.argv) as communicator:
        while True:
            device = input("Choose device: ")
            if device == "quit":
                break
            device_split = device.split(".")
            if device_split[0] != "SmartHome1" and device_split[0] != "SmartHome2":
                print("Invalid server")
                continue
            try:
                match device_split[1]:
                    case "Camera":
                        try:
                            obj = SmartHome.Camera.CameraServicePrx.checkedCast(communicator.propertyToProxy(device))
                            action = input("Choose action: ")
                            match action:
                                case "turn":
                                    turn_camera(obj)
                                case "zoom":
                                    zoom_camera(obj)
                                case "status":
                                    print(obj.status())
                                case _:
                                    print("Invalid action")
                        except SmartHome.Camera.CameraException as err:
                            print(err.reason)
                    case "NightCamera":
                        try:
                            obj = SmartHome.Camera.NightCameraServicePrx.checkedCast(communicator.propertyToProxy(device))
                            action = input("Choose action: ")
                            match action:
                                case "turn":
                                    turn_camera(obj)
                                case "zoom":
                                    zoom_camera(obj)
                                case "night vision":
                                    value = bool(input("Turn on night vision?: "))
                                    obj.nightVision(value)
                                case "status":
                                    print(obj.status())
                                case _:
                                    print("Invalid action")
                        except SmartHome.Camera.CameraException as err:
                            print(err.reason)
                    case "AvailableDevices":
                        obj = SmartHome.AvailableDevicesServicePrx.checkedCast(communicator.propertyToProxy(device))
                        print(f'Server {device_split[0]}')
                        print(obj.listAvailableDevices())
                    case "Light":
                        obj = SmartHome.Light.LightServicePrx.checkedCast(communicator.propertyToProxy(device))
                        action = input("Choose action: ")
                        match action:
                            case "set":
                                set_light(obj)
                            case "is on":
                                print(obj.isOn())
                            case _:
                                print("Invalid action")
                    case "RGBLight":
                        obj = SmartHome.Light.RGBLightServicePrx.checkedCast(communicator.propertyToProxy(device))
                        action = input("Choose action: ")
                        match action:
                            case "set":
                                set_light(obj)
                            case "is on":
                                print(obj.isOn())
                            case "set color":
                                color = get_color()
                                obj.setColor(color)
                            case "get color":
                                print(obj.getColor())
                            case _:
                                print("Invalid action")
                    case "SequenceLight":
                        obj = SmartHome.Light.SequenceLightServicePrx.checkedCast(communicator.propertyToProxy(device))
                        action = input("Choose action: ")
                        match action:
                            case "set":
                                set_light(obj)
                            case "is on":
                                print(obj.isOn())
                            case "set color":
                                color = get_color()
                                obj.setColor(color)
                            case "get color":
                                print(obj.getColor())
                            case "set sequence":
                                set_sequence(obj)
                            case _:
                                print("Invalid action")
                    case _:
                        print("Unknown device")
            except Exception as err:
                print(err)
        print("Quitting...")


if __name__ == '__main__':
    client()
