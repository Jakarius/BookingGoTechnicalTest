
public class GetDaveResults {

    public static void main(String[] args) {

        if (args.length < 4) {
            System.out.println("Invalid arguments: try usage main [pickup_lat] [pickup_long] [dropoff_lat] [dropoff_long]");
            return;
        }

        Coordinate inputPickup = new Coordinate(Double.parseDouble(args[0]), Double.parseDouble(args[1]));
        Coordinate inputDropoff = new Coordinate(Double.parseDouble(args[2]), Double.parseDouble(args[3]));

        TaxiAPIAccessor access = new TaxiAPIAccessor();

        System.out.println(access.getDescendingPriceResultsString(2, inputPickup, inputDropoff, true,"dave"));
    }

}
