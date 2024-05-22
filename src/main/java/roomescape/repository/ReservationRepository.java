package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationInfo;
import roomescape.domain.user.Member;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    boolean existsByReservationInfo(ReservationInfo reservationInfo);
    default boolean notExistsByReservationInfo(final ReservationInfo reservationInfo) {
        return !existsByReservationInfo(reservationInfo);
    }
    boolean existsByMemberAndReservationInfo(Member member, ReservationInfo reservationInfo);

    boolean existsByReservationInfoThemeId(Long themeId);

    List<Reservation> getReservationByReservationInfoThemeIdAndMemberIdAndReservationInfoDateBetween(Long themeId, Long memberId, ReservationDate start, ReservationDate end);

    List<Reservation> findAllByMemberId(Long memberId);

}
