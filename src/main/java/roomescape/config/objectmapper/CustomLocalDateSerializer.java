package roomescape.config.objectmapper;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

@JsonComponent
public class CustomLocalDateSerializer extends JsonSerializer<LocalDate> {
    private static final DateTimeFormatter TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd")
            .toFormatter()
            .withZone(ZoneId.of("Asia/Seoul"));

    @Override
    public void serialize(
            LocalDate localDate,
            JsonGenerator jsonGenerator,
            SerializerProvider serializerProvider
    ) {
        try {
            System.out.println(TIME_FORMATTER.format(localDate));
            jsonGenerator.writeString(TIME_FORMATTER.format(localDate));
        } catch (IOException exception) {
            throw new RuntimeException("날짜 변환 과정에서 문제가 발생했습니다.");
        }
    }
}
