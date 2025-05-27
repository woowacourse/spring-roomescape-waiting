package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Reservation;


public interface ReservationRepository {

    Reservation save(Reservation reservation);

    void deleteById(Long reservationId);

    Optional<Reservation> findById(Long reservationId);

    List<Reservation> findByMemberId(Long memberId);

    List<Reservation> findReservationsInConditions(Long memberId, Long themeId, LocalDate dateFrom, LocalDate dateTo);

    boolean existsByTimeId(Long reservationTimeId);

    boolean existsDuplicateReservation(LocalDate reservationDate, Long timeId, Long themeId);

    boolean existsByThemeId(Long themeId);

    boolean isBookingSlotEmpty(LocalDate reservationDate, Long timeId, Long themeId);

    boolean hasAlreadyReserved(Long memberId, Long themeId, Long timeId, LocalDate date);
}
