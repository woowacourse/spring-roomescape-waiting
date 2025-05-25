package roomescape.reservation.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;

public interface JpaReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByTimeId(Long timeId);

    boolean existsByDateAndTimeIdAndThemeId(ReservationDate date, Long timeId, Long themeId);

    boolean existsByDateAndTimeIdAndThemeIdAndMemberId(ReservationDate date, Long timeId, Long themeId, Long memberId);

    List<Reservation> findByMemberIdAndThemeIdAndDateBetween(Long memberId, Long themeId, ReservationDate from,
                                                             ReservationDate to);

    List<Reservation> findByDateAndThemeId(ReservationDate date, Long themeId);

    List<Reservation> findAllByMemberId(Long memberId);
}
