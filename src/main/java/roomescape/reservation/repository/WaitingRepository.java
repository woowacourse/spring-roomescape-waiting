package roomescape.reservation.repository;

import java.time.LocalDate;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.Waiting;

public interface WaitingRepository {
    Waiting save(Waiting waiting);

    /**
     * TODO
     * 세지 않고 order를 내림차순하고 첫번째 + 1해서 반환한다면?
     */
    long countByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme);
}
