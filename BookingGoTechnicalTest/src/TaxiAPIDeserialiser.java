import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TaxiAPIDeserialiser implements JsonDeserializer<TaxiAPIResponse> {

    @Override
    public TaxiAPIResponse deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {

        final JsonObject jsonObject = jsonElement.getAsJsonObject();

        // Supplier ID
        final JsonElement jsonSupplierID = jsonObject.get("supplier_id");
        final String supplierID = jsonSupplierID.getAsString();

        // Pickup
        final Coordinate pickup = parseCoordinate(jsonObject, "pickup");

        // Dropoff
        final Coordinate dropoff = parseCoordinate(jsonObject, "dropoff");

        // Options
        final JsonArray jsonOptions = jsonObject.getAsJsonArray("options");

        final List<TaxiOption> options = new ArrayList<>();
        for (int i = 0; i < jsonOptions.size(); i++) {
            final JsonObject jsonOption = jsonOptions.get(i).getAsJsonObject();
            options.add(new TaxiOption(jsonOption.get("car_type").getAsString(), jsonOption.get("price").getAsInt()));
        }

        return new TaxiAPIResponse(supplierID, pickup, dropoff, options);
    }

    private Coordinate parseCoordinate(JsonObject obj, String elementKey) throws JsonParseException {
        final JsonElement jsonElement = obj.get(elementKey);
        final String asString = jsonElement.getAsString();
        final String[] commaSplit = asString.split(",");

        if (commaSplit.length != 2) {
            throw new JsonParseException("Invalid JSON in pickup coordinate");
        }

        final double first = Double.parseDouble(commaSplit[0]);
        final double second = Double.parseDouble(commaSplit[1]);
        return new Coordinate(first, second);
    }


}
