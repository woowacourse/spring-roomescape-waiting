package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    boolean existsByReservationInfoId(Long reservationInfoId);

    boolean existsByReservationInfoThemeId(Long themeId);

    List<Reservation> getReservationByReservationInfoThemeIdAndMemberIdAndReservationInfoDateBetween(Long themeId, Long memberId, ReservationDate start, ReservationDate end);

    List<Reservation> findAllByMemberId(Long memberId);

}
