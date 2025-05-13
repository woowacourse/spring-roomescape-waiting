package roomescape.domain;

import java.time.LocalDate;
import java.util.List;

public interface ReservationCustomRepository {
    List<Reservation> findReservationsInConditions(Long memberId, Long themeId, LocalDate dateFrom, LocalDate dateTo);
}
