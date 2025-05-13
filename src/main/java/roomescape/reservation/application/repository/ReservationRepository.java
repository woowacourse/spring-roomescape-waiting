package roomescape.reservation.application.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import roomescape.reservation.application.dto.CreateReservationRequest;
import roomescape.reservation.domain.Reservation;

public interface ReservationRepository {
    Reservation insert(CreateReservationRequest request);

    List<Reservation> findAllReservations();

    int delete(Long id);

    boolean existsByTimeId(Long timeId);

    boolean existsByDateTime(LocalDateTime reservationDateTime);

    boolean existsByThemeId(Long id);

    List<Long> findBookedTimeIds(LocalDate date, Long themeId);

    List<Reservation> findReservationsBy(Long memberId, Long themeId, LocalDate dateFrom, LocalDate dateTo);
}
