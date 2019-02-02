import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.ws.http.HTTPException;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

public class TaxiAPIAccessor {

    private final String apiURI = "https://techtest.rideways.com/";
    private final String glueString = "/?";
    private final String GET_STRING = "GET";

    private static final Map<String, Integer> maximumPassengers = new HashMap<>();

    public static Map<String, Integer> getMaximumPassengersMap() {
        return Collections.unmodifiableMap(maximumPassengers);
    }

    private Comparator<TaxiOption> descendingTaxiPriceComparator = (taxiOption, t1) -> {
        if (taxiOption.price < t1.price) {
            return 1;
        } else if (taxiOption.price > t1.price) {
            return -1;
        } else {
            return 0;
        }
    };

    private Comparator<TaxiSupplierPricePair> descendingTaxiSupplierPricePairComparator = (taxiSupplierPricePair, t1) -> {
        if (taxiSupplierPricePair.price < t1.price) {
            return -1;
        } else if (taxiSupplierPricePair.price > t1.price) {
            return 1;
        } else {
            return 0;
        }
    };

    public TaxiAPIAccessor() {
        maximumPassengers.put("STANDARD", 4);
        maximumPassengers.put("EXECUTIVE", 4);
        maximumPassengers.put("LUXURY", 4);
        maximumPassengers.put("PEOPLE_CARRIER", 6);
        maximumPassengers.put("LUXURY_PEOPLE_CARRIER", 6);
        maximumPassengers.put("MINIBUS", 16);
    }

    public List<TaxiAPIResponse> getAPIResponses(int numberOfPassengers, Coordinate pickup, Coordinate dropoff, String... supplierIDs) {

        // Map the supplier ids onto the search function. Filter out missing results.
        List<TaxiAPIResponse> responses = Arrays.stream(supplierIDs).map(n -> getResponse(n, pickup, dropoff))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        // For each api response, remove options that have less maximum passengers than our number of passengers.
        responses.forEach(n -> n.options.removeIf(o -> maximumPassengers.get(o.car_type) < numberOfPassengers));

        return responses;
    }

    public List<TaxiAPIResponse> getDescendingPriceResults(int numberOfPassengers, Coordinate pickup, Coordinate dropoff, String... supplierIDs) {

        List<TaxiAPIResponse> responses = getAPIResponses(numberOfPassengers, pickup, dropoff, supplierIDs);

        // Sort into descending price order.
        responses.forEach(n -> n.options.sort(descendingTaxiPriceComparator));
        return responses;
    }

    public String getDescendingPriceResultsString(int numberOfPassengers, Coordinate pickup, Coordinate dropoff, boolean jsonResponse, String... supplierIDs) {

        List<TaxiAPIResponse> responses = getDescendingPriceResults(numberOfPassengers, pickup, dropoff, supplierIDs);

        if (responses.isEmpty()) {
            return "No suppliers were able to respond, please try again later.";
        }

        StringBuilder builder = new StringBuilder();
        if (jsonResponse) {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(responses.getClass(), new DescendingPriceQuerySerialiser());

            Gson gson = gsonBuilder.create();
            return gson.toJson(responses, responses.getClass());
        } else {
            responses.forEach(n -> builder.append(n.getResultFormattedString()));
            return builder.toString();
        }
    }

    public String getCheapestSupplierPerCarType(int numberOfPassengers, Coordinate pickup, Coordinate dropoff, boolean jsonResponse, String... supplierIDs) {

        List<TaxiAPIResponse> responses = getAPIResponses(numberOfPassengers, pickup, dropoff, supplierIDs);

        if (responses.isEmpty()) {
            return "No suppliers were able to respond, please try again later.";
        }

        // Sort into descending price order.
        responses.forEach(n -> n.options.sort(descendingTaxiPriceComparator));

        Map<String, List<TaxiSupplierPricePair>> carTypeToSupplierPricePair = new HashMap<>();
        for (String key : maximumPassengers.keySet()) {
            carTypeToSupplierPricePair.put(key, new ArrayList<>());
        }

        for (TaxiAPIResponse response : responses) {
            for (TaxiOption option : response.options) {
                carTypeToSupplierPricePair.get(option.car_type).add(new TaxiSupplierPricePair(response.supplier_id, option.price));
            }
        }

        for (List<TaxiSupplierPricePair> val : carTypeToSupplierPricePair.values()) {
            val.sort(descendingTaxiSupplierPricePairComparator);
        }

        // Take the cheapest result for each car type.
        List<CarTypeSupplierPrice> carTypeSupplierPrices = new ArrayList<>();
        for (Map.Entry<String, List<TaxiSupplierPricePair>> entry : carTypeToSupplierPricePair.entrySet()) {

            if (entry.getValue().size() > 0) {
                TaxiSupplierPricePair cheapest = entry.getValue().get(0);
                carTypeSupplierPrices.add(new CarTypeSupplierPrice(entry.getKey(), cheapest.supplierID, cheapest.price));
            }

        }

        if (jsonResponse) {

            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(carTypeSupplierPrices.getClass(), new CheapestPerCarTypeSerialiser());

            Gson gson = gsonBuilder.create();
            return gson.toJson(carTypeSupplierPrices, carTypeSupplierPrices.getClass());

        } else {

            StringBuilder builder = new StringBuilder();

            for (Map.Entry<String, List<TaxiSupplierPricePair>> entry : carTypeToSupplierPricePair.entrySet()) {
                if (entry.getValue().size() > 0) {
                    // Sorted so that first element contains cheapest supplier for car type.
                    TaxiSupplierPricePair pair = entry.getValue().get(0);
                    builder.append(entry.getKey())
                            .append(" - ")
                            .append(pair.supplierID)
                            .append(" - ")
                            .append(pair.price)
                            .append("\n");
                }
            }

            return builder.toString();
        }
    }

    class CarTypeSupplierPrice {

        public final String carType;
        public final String supplierID;
        public final int price;

        public CarTypeSupplierPrice(String carType, String supplierID, int price) {
            this.carType = carType;
            this.supplierID = supplierID;
            this.price = price;
        }

        @Override
        public String toString() {
            return carType + " - " + supplierID + " - " + price;
        }
    }

    public Optional<TaxiAPIResponse> getResponse(String supplierID, Coordinate pickup, Coordinate dropoff) {

        StringBuilder build = new StringBuilder();

        build.append(apiURI)
                .append(supplierID)
                .append(glueString)
                .append("pickup=")
                .append(pickup.first).append(",").append(pickup.second)
                .append("&dropoff=")
                .append(dropoff.first).append(",").append(dropoff.second);

        String response = "";
        try {
            response = makeHTTPConnection(build.toString());
            if (response.isEmpty()) {
                return Optional.empty();
            }
        } catch (HTTPException e) {
            System.err.println("Supplier ID " + supplierID + " returned error response, leaving them out of the results.");
            return Optional.empty();
        }


        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(TaxiAPIResponse.class, new TaxiAPIDeserialiser());

        Gson gson = gsonBuilder.create();

        return Optional.of(gson.fromJson(response, TaxiAPIResponse.class));
    }

    public String makeHTTPConnection(String urlString) throws HTTPException {
        try {

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod(GET_STRING);

            // Ignore supplier if takes longer than 2 seconds.
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(5000);

            int status = conn.getResponseCode();

            // APIs can break sometimes....
            if (status == 500) {
                throw new HTTPException(500);
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            conn.disconnect();

            return content.toString();

        } catch (MalformedURLException e) {
            System.err.println("Malformed URL: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    class TaxiSupplierPricePair {

        public final String supplierID;
        public final int price;

        public TaxiSupplierPricePair(String supplierID, int price) {
            this.supplierID = supplierID;
            this.price = price;
        }
    }

}
