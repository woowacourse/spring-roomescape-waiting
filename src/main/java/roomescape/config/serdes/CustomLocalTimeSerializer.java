package roomescape.config.serdes;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

@JsonComponent
public class CustomLocalTimeSerializer extends JsonSerializer<LocalTime> {
    private static final DateTimeFormatter TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("HH:mm")
            .toFormatter()
            .withZone(ZoneId.of("Asia/Seoul"));

    @Override
    public void serialize(
            LocalTime time,
            JsonGenerator jsonGenerator,
            SerializerProvider serializerProvider) {
        try {
            System.out.println(TIME_FORMATTER.format(time));
            jsonGenerator.writeString(TIME_FORMATTER.format(time));
        } catch (IOException exception) {
            throw new RuntimeException("시간 변환 과정에서 문제가 발생했습니다.");
        }
    }
}
