public class PassengerFilter {

    public static void main(String[] args) {

        if (args.length < 5) {
            System.out.println("Invalid arguments: try usage main [pickup_lat] [pickup_long] [dropoff_lat] [dropoff_long] [number_of_passengers]");
            return;
        }

        Coordinate inputPickup = new Coordinate(Double.parseDouble(args[0]), Double.parseDouble(args[1]));
        Coordinate inputDropoff = new Coordinate(Double.parseDouble(args[2]), Double.parseDouble(args[3]));
        int numberOfPassengers = Integer.parseInt(args[4]);

        TaxiAPIAccessor access = new TaxiAPIAccessor();

        System.out.println(access.getCheapestSupplierPerCarType(numberOfPassengers, inputPickup, inputDropoff, true, "dave", "eric", "jeff"));
    }
}
