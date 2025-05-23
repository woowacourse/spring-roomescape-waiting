package roomescape.persistence.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public interface MemberBookingProjection {
    Long getId();
    String getMemberName();
    String getType();
    String getThemeName();
    LocalDate getDate();
    LocalTime getTime();
    int getRank();
}
