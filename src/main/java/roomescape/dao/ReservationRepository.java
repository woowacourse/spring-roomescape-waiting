package roomescape.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    boolean existsByTimeId(Long timeId);

    boolean existsByThemeId(Long themeId);

    boolean existsByDateAndTimeId(ReservationDate reservationDate, Long timeId);

    List<Reservation> getReservationByThemeIdAndMemberIdAndDateBetween(Long themeId, Long memberId, ReservationDate start, ReservationDate end);

    List<Reservation> findAllByMemberId(Long memberId);
}
