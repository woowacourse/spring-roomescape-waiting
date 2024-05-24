package roomescape.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.user.Member;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    boolean existsByTimeId(Long timeId);

    boolean existsByThemeId(Long themeId);

    boolean existsByDateAndTimeId(ReservationDate reservationDate, Long timeId);

    boolean existsByDateAndTimeIdAndMemberId(ReservationDate reservationDate, Long timeId, Long memberId);

    List<Reservation> getReservationByThemeIdAndMemberIdAndDateBetween(Long themeId, Long memberId,
                                                                       ReservationDate start, ReservationDate end);

    List<Reservation> findAllByMember(Member member);
}
