package roomescape.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class SystemDateTime implements DateTime {

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }

    @Override
    public LocalDate nowDate() {
        return LocalDate.now();
    }

}
