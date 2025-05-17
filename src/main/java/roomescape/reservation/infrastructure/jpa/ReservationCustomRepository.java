package roomescape.reservation.infrastructure.jpa;

import java.time.LocalDate;
import java.util.List;
import roomescape.reservation.domain.Reservation;

public interface ReservationCustomRepository {
    List<Reservation> findByMemberIdAndThemeIdAndDate(Long memberId, Long themeId, LocalDate from, LocalDate to);
    List<Reservation> findByMemberId(Long id);
}
