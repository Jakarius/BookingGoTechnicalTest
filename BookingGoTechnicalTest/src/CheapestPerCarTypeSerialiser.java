import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.List;

public class CheapestPerCarTypeSerialiser implements JsonSerializer<List<TaxiAPIAccessor.CarTypeSupplierPrice>> {

    @Override
    public JsonElement serialize(List<TaxiAPIAccessor.CarTypeSupplierPrice> results, Type type, JsonSerializationContext jsonSerializationContext) {

        JsonArray jArray = new JsonArray();

        for (TaxiAPIAccessor.CarTypeSupplierPrice result : results) {

            JsonObject jResponse = new JsonObject();

            JsonElement jCarType = jsonSerializationContext.serialize(result.carType);
            JsonElement jSupplierID = jsonSerializationContext.serialize(result.supplierID);
            JsonElement jPrice = jsonSerializationContext.serialize(result.price);

            jResponse.add("car_type", jCarType);
            jResponse.add("supplier_id", jSupplierID);
            jResponse.add("price", jPrice);

            jArray.add(jResponse);
        }

        return jArray;
    }
}
