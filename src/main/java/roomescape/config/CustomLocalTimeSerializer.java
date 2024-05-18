package roomescape.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.TimeZone;

public class CustomLocalTimeSerializer extends JsonSerializer<LocalTime> {
    DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm");

    @Override
    public void serialize(
            LocalTime time,
            JsonGenerator jsonGenerator,
            SerializerProvider serializerProvider) {
        try {
            DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            jsonGenerator.writeString(DATE_FORMAT.format(time));
        } catch (IOException exception) {
            throw new RuntimeException("시간 변환 과정에서 문제가 발생했습니다.");
        }
    }
}
