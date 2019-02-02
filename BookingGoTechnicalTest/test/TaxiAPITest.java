import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import java.util.List;

public class TaxiAPITest {

    Coordinate pickup;
    Coordinate dropoff;

    TaxiAPIAccessor accessor;

    @Before
    public void setup() {
        accessor = new TaxiAPIAccessor();
        pickup = new Coordinate(51.470020, -0.454295);
        dropoff = new Coordinate(51.410632, -0.457533);
    }

    @Test
    public void testAPICallSimple() {
        TaxiAPIAccessor accessor = new TaxiAPIAccessor();
//        accessor.printAllRecords("dave");
    }

    @Test
    public void testPriceIsSortedDescending() {
        List<TaxiAPIResponse> responses = accessor.getDescendingPriceResults(2, pickup, dropoff, "dave");
        TaxiAPIResponse singleResponse = responses.get(0);

        int lastPrice = Integer.MAX_VALUE;
        for (TaxiOption option : singleResponse.options) {
            Assert.assertTrue(option.price < lastPrice);
            option.price = lastPrice;
        }

    }

    @Test
    public void testResultsHaveSufficientPassengers() {

        int numberOfPassengers = 5;

        List<TaxiAPIResponse> responses = accessor.getDescendingPriceResults(numberOfPassengers, pickup, dropoff, "dave");
        TaxiAPIResponse singleResponse = responses.get(0);

        for (TaxiOption option : singleResponse.options) {
            Assert.assertTrue(TaxiAPIAccessor.getMaximumPassengersMap().get(option.car_type) >= numberOfPassengers);
        }
    }

}
