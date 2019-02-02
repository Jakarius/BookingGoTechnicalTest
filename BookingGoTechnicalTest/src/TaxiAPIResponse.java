import jdk.nashorn.internal.runtime.ParserException;

import java.util.List;

public class TaxiAPIResponse {

    public String supplier_id;
    public Coordinate pickup;
    public Coordinate dropoff;
    public List<TaxiOption> options;

    public TaxiAPIResponse(String supplier_id, Coordinate pickup, Coordinate dropoff, List<TaxiOption> options) {
        this.supplier_id = supplier_id;
        this.pickup = pickup;
        this.dropoff = dropoff;
        this.options = options;
    }

    @Override
    public String toString() {
        return "TaxiAPIResponse{" +
                "supplier_id='" + supplier_id + '\'' +
                ", pickup=" + pickup +
                ", dropoff=" + dropoff +
                ", options=" + options.toString() +
                '}';
    }

    public String getResultFormattedString() {

        StringBuilder builder = new StringBuilder();

        for (TaxiOption option : options) {
            builder.append(option.car_type)
                    .append(" - ")
                    .append(supplier_id)
                    .append(" - ")
                    .append(option.price)
                    .append("\n");
        }

        return builder.toString();
    }
}

class TaxiOption {

    public String car_type;
    public int price;

    public TaxiOption(String car_type, int price) {
        this.car_type = car_type;
        this.price = price;
    }

    @Override
    public String toString() {
        return "TaxiOption{" +
                "car_type='" + car_type + '\'' +
                ", price=" + price +
                '}';
    }
}

class Coordinate {

    public double first;
    public double second;

    public Coordinate(double first, double second) {
        this.first = first;
        this.second = second;
    }

    public Coordinate(String csv) {
        // Parses comma separated coordinate string like "3.4,2.3"
        String[] split = csv.split(",");
        if (split.length == 2) {
            first = Double.parseDouble(split[0]);
            second = Double.parseDouble(split[1]);
        } else {
            throw new ParserException("Can't parse string to Coordinate");
        }
    }

    @Override
    public String toString() {
        return "coord="+
                "(" + first + ", " + second + ")";
    }
}