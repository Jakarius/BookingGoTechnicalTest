import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class TaxiRESTAPI {

    static TaxiAPIAccessor accessor = new TaxiAPIAccessor();

    final static String coordinatePattern = "[-+]?[0-9]*\\.?[0-9]+,[-+]?[0-9]*\\.?[0-9]+";

    public static void main(String[] args) {

        try {
            HttpServer server = null;
            server = HttpServer.create(new InetSocketAddress(8000), 0);
            server.createContext("/taxiREST", new TaxiRequestHandler());
            server.setExecutor(null);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class TaxiRequestHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {

            URI incoming = httpExchange.getRequestURI();
            String requestString = incoming.getQuery();

            Map<String, String> params = new HashMap<>();
            String[] split = requestString.split("&");

            int passengers = -1;
            Coordinate pickup = null;
            Coordinate dropoff = null;

            for (String splitted : split) {

                if (splitted.matches("^(passengers|pass)=\\d")) {
                    passengers = Integer.parseInt(splitted.split("=")[1]);
                }

                if (splitted.matches("^(pickup|pick)=" + coordinatePattern)) {
                    String coordString = splitted.split("=")[1];
                    pickup = new Coordinate(coordString);
                }

                if (splitted.matches("^(dropoff|drop)=" + coordinatePattern)) {
                    String coordString = splitted.split("=")[1];
                    dropoff = new Coordinate(coordString);
                }
            }

            System.out.println(passengers + " " + pickup + "  "+ dropoff);

            String jsonResponse = "";
            if (passengers > 0 && pickup != null && dropoff != null) {
                jsonResponse = accessor.getCheapestSupplierPerCarType(passengers,
                        pickup,
                        dropoff,
                        true,
                        "dave", "eric", "jeff");
            }

            System.out.println("SENDING: " + jsonResponse);

            OutputStream out = httpExchange.getResponseBody();

            if (jsonResponse.isEmpty()) {
                httpExchange.sendResponseHeaders(500, 0);
                jsonResponse = "ERROR: HTTP 500\nCheck your parameters were in the right form e.g.\npassengers=1&pickup=1.1,2.2&dropoff=3.3,4.4";
            } else {
                httpExchange.sendResponseHeaders(200, jsonResponse.length());
            }

            out.write(jsonResponse.getBytes());
            out.close();

        }
    }
}
