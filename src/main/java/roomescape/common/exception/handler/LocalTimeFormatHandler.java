package roomescape.common.exception.handler;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.time.LocalTime;
import org.springframework.stereotype.Component;

@Component
public class LocalTimeFormatHandler implements FormatHandler {
    @Override
    public boolean isSupport(InvalidFormatException e) {
        return e.getTargetType() == LocalTime.class;
    }

    @Override
    public String handle(InvalidFormatException formatException) {
        return "시간 형식은 HH:mm 으로 입력해 주세요. (ex. 10:00)";
    }
}
