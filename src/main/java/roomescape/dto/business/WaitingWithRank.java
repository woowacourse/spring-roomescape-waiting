package roomescape.dto.business;

import java.time.LocalDate;
import java.time.LocalTime;

public interface WaitingWithRank {

    long getId();

    LocalDate getDate();

    String getThemeName();

    LocalTime getStartAt();

    long getRank();
}
