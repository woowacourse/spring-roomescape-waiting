package roomescape.infrastructure;

import roomescape.domain.Reservation;

import java.time.LocalDate;
import java.util.List;

public interface ReservationCustomRepository {

    List<Reservation> findAllWithSearchConditions(Long memberId, Long themeId, LocalDate from, LocalDate to);
}
