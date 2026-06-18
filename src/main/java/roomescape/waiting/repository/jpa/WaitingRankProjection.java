package roomescape.waiting.repository.jpa;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public interface WaitingRankProjection {

    Long getId();

    String getCustomerName();

    LocalDate getReservationDate();

    LocalDateTime getCreatedAt();

    Integer getRank();

    Long getTimeId();

    LocalTime getTimeStartAt();

    Long getThemeId();

    String getThemeName();

    String getThemeDescription();

    String getThemeThumbnailUrl();
}
