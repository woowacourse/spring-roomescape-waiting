package roomescape.reservation.domain;

import roomescape.reservation.infrastructure.vo.ThemeBookingCount;
import roomescape.user.domain.UserId;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    boolean existsById(Long id);

    boolean existsByParams(Long timeId);

    boolean existsByParams(ReservationDate date, Long timeId, Long themeId);

    Optional<Reservation> findById(Long id);

    List<Long> findTimeIdByParams(ReservationDate date, Long themeId);

    List<Reservation> findAll();

    List<Reservation> findAllByUserId(UserId userId);

    Reservation save(Reservation reservation);

    void deleteById(Long id);

    List<ThemeBookingCount> findThemesToBookedCount(ReservationDate startDate, ReservationDate endDate, int count);

    List<Reservation> findAllByParams(UserId userId, Long themeId, ReservationDate reservationDate, ReservationDate reservationDate1);
}
