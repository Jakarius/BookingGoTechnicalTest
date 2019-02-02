import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.List;

public class DescendingPriceQuerySerialiser implements JsonSerializer<List<TaxiAPIResponse>> {

    @Override
    public JsonElement serialize(List<TaxiAPIResponse> taxiAPIResponses, Type type, JsonSerializationContext jsonSerializationContext) {

        JsonArray jArray = new JsonArray();
        for (TaxiAPIResponse response : taxiAPIResponses) {

            for (TaxiOption option : response.options) {
                JsonObject jResponse = new JsonObject();

                JsonElement jCarType = jsonSerializationContext.serialize(option.car_type);
                JsonElement jPrice = jsonSerializationContext.serialize(option.price);

                jResponse.add("car_type", jCarType);
                jResponse.add("price", jPrice);

                jArray.add(jResponse);
            }
        }
        return jArray;
    }
}
