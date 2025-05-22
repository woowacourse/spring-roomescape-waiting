package roomescape.reservation.domain;

import roomescape.reservation.infrastructure.vo.ThemeBookingCount;
import roomescape.time.domain.ReservationTimeId;
import roomescape.user.domain.UserId;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    boolean existsByParams(Long id);

    boolean existsByParams(ReservationTimeId timeId);

    boolean existsByParams(ReservationDate date, ReservationTimeId timeId, Long themeId);

    Optional<Reservation> findById(Long id);

    List<ReservationTimeId> findTimeIdByParams(ReservationDate date, Long themeId);

    List<Reservation> findAll();

    List<Reservation> findAllByUserId(UserId userId);

    Reservation save(Reservation reservation);

    void deleteById(Long id);

    List<ThemeBookingCount> findThemesToBookedCount(ReservationDate startDate, ReservationDate endDate, int count);

    List<Reservation> findAllByParams(UserId userId, Long themeId, ReservationDate reservationDate, ReservationDate reservationDate1);
}
