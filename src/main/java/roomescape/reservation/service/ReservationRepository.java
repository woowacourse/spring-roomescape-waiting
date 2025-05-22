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

    boolean existsByReservationDateAndReservationTimeIdAndThemeId(ReservationDate reservationDate, Long timeId,
                                                                  Long themeId);

    boolean existsByReservationTimeId(Long timeId);

    boolean existsByThemeId(Long themeId);

    List<Reservation> findByFilter(Long memberId, Long themeId, LocalDate start, LocalDate end);

    List<Reservation> findAllByMemberId(Long memberId);

    Optional<Reservation> findByThemeIdAndReservationTimeIdAndReservationDate_reservationDate(Long themeId, Long timeId,
                                                                                              LocalDate date);
}
