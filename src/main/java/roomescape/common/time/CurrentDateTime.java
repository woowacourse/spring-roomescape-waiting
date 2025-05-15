package roomescape.common.time;

import java.time.LocalDate;
import java.time.LocalTime;

public interface CurrentDateTime {

    LocalDate getDate();

    LocalTime getTime();
}
