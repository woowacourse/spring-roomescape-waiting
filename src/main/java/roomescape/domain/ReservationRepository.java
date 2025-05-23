package roomescape.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public interface ReservationRepository {

    List<Reservation> findAll();

    Reservation save(Reservation reservation);

    void deleteById(Long reservationId);

    Optional<Reservation> findById(Long reservationId);

    List<Reservation> findByMemberId(Long memberId);

    boolean existsByTimeId(Long reservationTimeId);

    boolean existsBySchedule(Schedule schedule);

    boolean existsByThemeId(Long themeId);

    List<Reservation> findByThemeIdAndDate(Long themeId, LocalDate reservationDate);

    List<Reservation> findReservationsInConditions(Long memberId, Long themeId, LocalDate dateFrom, LocalDate dateTo);
}
