package roomescape.config.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import roomescape.exception.ErrorType;
import roomescape.exception.InvalidClientFieldWithValueException;

import java.io.IOException;

public class LongDeserializerWithValidation extends JsonDeserializer<Long> {
    @Override
    public Long deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        String fieldName = p.getParsingContext().getCurrentName();
        String value = node.asText();
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            throw new InvalidClientFieldWithValueException(ErrorType.INVALID_DATA_TYPE, fieldName, value);
        }
    }
}
