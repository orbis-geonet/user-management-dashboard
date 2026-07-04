package to.orbis.dashboard.utils;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import to.orbis.dashboard.models.dto.PointDto;

import java.io.IOException;

public class PointDtoDeserializer extends StdDeserializer<PointDto> {
    protected PointDtoDeserializer(Class<?> vc) {
        super(vc);
    }

    public PointDtoDeserializer() {
        this(null);
    }

    @Override
    public PointDto deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        var node = jsonParser.getCodec().readTree(jsonParser);
        double lng = ((NumericNode) node.get("lng")).asDouble();
        double lat = ((NumericNode) node.get("lat")).asDouble();

        return new PointDto(lng, lat);
    }
}
