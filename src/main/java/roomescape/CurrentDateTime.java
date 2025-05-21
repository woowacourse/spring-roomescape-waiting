package roomescape;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public interface CurrentDateTime {
    LocalDate getDate();
    LocalTime getTime();
    LocalDateTime getDateTime();
}
