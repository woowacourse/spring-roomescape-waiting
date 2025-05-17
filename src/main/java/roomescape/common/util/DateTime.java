package roomescape.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface DateTime {

    LocalDateTime now();

    LocalDate nowDate();
}
