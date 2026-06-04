package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface TimeProvider {
    LocalDate today();

    LocalDateTime currentDateTime();
}
