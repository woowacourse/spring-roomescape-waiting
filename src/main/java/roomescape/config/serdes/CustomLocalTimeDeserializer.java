package roomescape.config.serdes;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

@JsonComponent
public class CustomLocalTimeDeserializer extends JsonDeserializer<LocalTime> {
    private static final DateTimeFormatter TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("HH:mm")
            .toFormatter()
            .withZone(ZoneId.of("Asia/Seoul"));

    @Override
    public LocalTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
        try {
            String time = jsonParser.getValueAsString();
            return LocalTime.parse(time, TIME_FORMATTER);
        } catch (IOException e) {
            throw new RuntimeException("시간 변환 과정에서 오류가 발생되었습니다.");
        }
    }
}
