package roomescape.config.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import roomescape.exception.clienterror.InvalidDataTypeException;

public class LocalDateDeserializerWithValidation extends JsonDeserializer<LocalDate> {
    @Override
    public LocalDate deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        String fieldName = p.getParsingContext().getCurrentName();
        String value = node.asText();
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new InvalidDataTypeException(fieldName, value);
        }
    }
}
