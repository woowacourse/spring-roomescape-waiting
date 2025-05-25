package roomescape.reservation.infrastructure.projection;

import java.time.LocalDate;
import java.time.LocalTime;

public interface WaitingWithRankProjection {
    Long getId();

    LocalDate getDate();

    // ReservationTime
    Long getTimeId();

    LocalTime getTimeStartAt();

    // Theme
    Long getThemeId();

    String getThemeName();

    String getThemeDescription();

    String getThemeThumbnail();

    // Member
    Long getMemberId();

    String getMemberName();

    Long getRank();
}
