package roomescape.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.TimeZone;

public class CustomLocalDateSerializer extends JsonSerializer<LocalDate> {
    DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public void serialize(
            LocalDate localDate,
            JsonGenerator jsonGenerator,
            SerializerProvider serializerProvider
    ) {
        try {
            DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            jsonGenerator.writeString(DATE_FORMAT.format(localDate));
        } catch (IOException exception) {
            throw new RuntimeException("날짜 변환 과정에서 문제가 발생했습니다.");
        }
    }
}
