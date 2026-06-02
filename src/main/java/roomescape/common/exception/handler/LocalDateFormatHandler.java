package roomescape.common.exception.handler;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class LocalDateFormatHandler implements FormatHandler {
    @Override
    public boolean isSupport(InvalidFormatException e) {
        return e.getTargetType() == LocalDate.class;
    }

    @Override
    public String handle(InvalidFormatException formatException) {
        return "날짜는 yyyy-MM-dd 형식으로 입력해 주세요(ex. 2026-12-25)";
    }
}
