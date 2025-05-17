package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;

public interface ReservationRepository {

    List<Reservation> findAll();

    Reservation save(Reservation reservation);

    Optional<Reservation> findById(Long id);

    void deleteById(Long id);

    boolean existsByReservationDateAndReservationTimeId(ReservationDate reservationDate, Long timeId);

    boolean existsByReservationTimeId(Long timeId);

    boolean existsByThemeId(Long themeId);

    List<Reservation> findByFilter(Long memberId, Long themeId, LocalDate start, LocalDate end);

    List<Reservation> findAllByMemberId(Long memberId);

    List<Reservation> findAllByReservationDateBetween(LocalDate start, LocalDate end);
}
