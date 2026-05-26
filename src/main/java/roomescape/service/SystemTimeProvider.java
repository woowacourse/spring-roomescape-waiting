package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class SystemTimeProvider implements TimeProvider {

    @Override
    public LocalDate today() {
        return LocalDate.now();
    }

    @Override
    public LocalDateTime currentDateTime() {
        return LocalDateTime.now();
    }
}
