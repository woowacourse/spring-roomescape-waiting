package roomescape.reservation.infrastructure.jpa;

import java.time.LocalDate;
import java.util.List;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;

public interface ReservationCustomRepository {
    List<Reservation> findByMemberIdAndThemeIdAndDate(Long memberId, Long themeId, LocalDate from, LocalDate to);

    boolean existsReservation(LocalDate date, Long timeId, Long themeId,
                              Long memberId, ReservationStatus status);
}
