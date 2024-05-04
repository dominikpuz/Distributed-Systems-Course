package pl.agh.edu;


import java.util.List;

public class ExampleMethods {
    public double avg(List<Double> values) {
        if (values.isEmpty()) return 0;
        return values.stream().mapToDouble(x -> x).average().getAsDouble();
    }

    public String helloFromServer() {
        return "Hello from Server";
    }

    public double sumRectangles(Rectangle[] rectangles) {
        double sum = 0;
        for (Rectangle rectangle : rectangles) {
            sum += rectangle.x * rectangle.y;
        }
        return sum;
    }
}
