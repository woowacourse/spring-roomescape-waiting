package roomescape.config.serdes;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class CustomLocalDateDeserializer extends JsonDeserializer<LocalDate> {
    private static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd")
            .toFormatter()
            .withZone(ZoneId.of("Asia/Seoul"));

    @Override
    public LocalDate deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
        try {
            String date = jsonParser.getValueAsString();
            return LocalDate.parse(date, DATE_FORMATTER);
        } catch (IOException e) {
            throw new RuntimeException("날짜 변환 과정에서 오류가 발생되었습니다.");
        }
    }
}
