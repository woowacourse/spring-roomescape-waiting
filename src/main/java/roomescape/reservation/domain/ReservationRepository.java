package roomescape.reservation.domain;

import roomescape.reservation.infrastructure.vo.ThemeBookingCount;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    boolean existsById(Long id);

    boolean existsByParams(Long timeId);

    boolean existsByParams(ReservationDate date, Long timeId, Long themeId);

    Optional<Reservation> findById(Long id);

    Optional<Reservation> findByParams(ReservationDate date, Long timeId, Long themeId);

    List<Long> findTimeIdByParams(ReservationDate date, Long themeId);

    List<Reservation> findAll();

    List<Reservation> findAllByUserId(Long userId);

    Reservation save(Reservation reservation);

    void deleteById(Long id);

    List<ThemeBookingCount> findThemesToBookedCount(ReservationDate startDate, ReservationDate endDate, int count);

    List<Reservation> findAllByParams(Long userId, Long themeId, ReservationDate reservationDate, ReservationDate reservationDate1);
}
