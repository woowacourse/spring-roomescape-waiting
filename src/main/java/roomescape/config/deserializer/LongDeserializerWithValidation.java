package roomescape.config.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import roomescape.exception.clienterror.InvalidDataTypeException;

public class LongDeserializerWithValidation extends JsonDeserializer<Long> {
    @Override
    public Long deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        String fieldName = p.getParsingContext().getCurrentName();
        String value = node.asText();
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            throw new InvalidDataTypeException(fieldName, value);
        }
    }
}
